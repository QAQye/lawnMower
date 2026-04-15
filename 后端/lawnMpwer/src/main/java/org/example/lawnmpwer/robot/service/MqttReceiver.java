package org.example.lawnmpwer.robot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.lawnmpwer.mower.service.LawnmowerInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MqttReceiver {

    private static final Logger log = LoggerFactory.getLogger(MqttReceiver.class);

    private final ObjectMapper objectMapper;
    private final LawnmowerInfoService lawnmowerInfoService;

    public MqttReceiver(ObjectMapper objectMapper, LawnmowerInfoService lawnmowerInfoService) {
        this.objectMapper = objectMapper;
        this.lawnmowerInfoService = lawnmowerInfoService;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void receive(Message<?> message) {
        String topic = String.valueOf(message.getHeaders().get("mqtt_receivedTopic"));
        String payload = String.valueOf(message.getPayload());

        try {
            JsonNode root = objectMapper.readTree(payload);
            String robotId = text(root, "robotId");
            String type = text(root, "type");

            log.info("收到小车 MQTT 消息: topic={}, robotId={}, type={}, payload={}", topic, robotId, type, payload);

            // ===== 新增：在线/状态/离线 自动写入数据库 =====
            if (topic.endsWith("/online")) {
                String mowerName = firstNonBlank(
                        text(root, "name"),
                        text(root, "robotName"),
                        robotId
                );
                String ipAddress = firstNonBlank(
                        text(root, "ipAddress"),
                        text(root, "ip"),
                        text(root, "targetIp")
                );

                lawnmowerInfoService.saveOrUpdateFromMqtt(robotId, mowerName, ipAddress, 1);
                log.info("小车上线并已写入数据库: robotId={}, name={}, ip={}", robotId, mowerName, ipAddress);
                return;
            }

            if (topic.endsWith("/status")) {
                String mowerName = firstNonBlank(
                        text(root, "name"),
                        text(root, "robotName"),
                        robotId
                );
                String ipAddress = firstNonBlank(
                        text(root, "ipAddress"),
                        text(root, "ip"),
                        text(root, "targetIp")
                );

                Integer status = resolveStatus(root, 1);

                lawnmowerInfoService.saveOrUpdateFromMqtt(robotId, mowerName, ipAddress, status);
                log.info("收到状态并已更新数据库: robotId={}, name={}, ip={}, status={}",
                        robotId, mowerName, ipAddress, status);
                return;
            }

            if (topic.endsWith("/offline")) {
                String mowerName = firstNonBlank(
                        text(root, "name"),
                        text(root, "robotName"),
                        robotId
                );
                String ipAddress = firstNonBlank(
                        text(root, "ipAddress"),
                        text(root, "ip"),
                        text(root, "targetIp")
                );

                lawnmowerInfoService.saveOrUpdateFromMqtt(robotId, mowerName, ipAddress, 0);
                log.info("小车离线并已更新数据库: robotId={}, name={}, ip={}", robotId, mowerName, ipAddress);
                return;
            }

            // ===== 你原来其他逻辑继续保留 =====
            if (topic.endsWith("/ack")) {
                log.info("收到小车 ACK: {}", payload);
            } else if (topic.endsWith("/detection/stats")) {
                // 这里继续保留你原来写的识别统计处理逻辑
                log.info("收到识别统计: {}", payload);
            }

        } catch (Exception e) {
            log.info("收到小车 MQTT 原始消息: topic={}, payload={}", topic, payload);
        }
    }

    private String text(JsonNode root, String field) {
        if (root == null || field == null) {
            return "";
        }
        return root.path(field).asText("").trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private Integer resolveStatus(JsonNode root, int defaultStatus) {
        String statusText = text(root, "status");

        if ("offline".equalsIgnoreCase(statusText) || "0".equals(statusText)) {
            return 0;
        }
        if ("online".equalsIgnoreCase(statusText) || "1".equals(statusText)) {
            return 1;
        }

        if (root.has("online")) {
            return root.path("online").asBoolean() ? 1 : 0;
        }

        return defaultStatus;
    }
}