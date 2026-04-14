package org.example.lawnmpwer.robot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MqttReceiver {

    private static final Logger log = LoggerFactory.getLogger(MqttReceiver.class);

    private final ObjectMapper objectMapper;

    public MqttReceiver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void receive(Message<?> message) {
        String topic = String.valueOf(message.getHeaders().get("mqtt_receivedTopic"));
        String payload = String.valueOf(message.getPayload());

        try {
            JsonNode root = objectMapper.readTree(payload);
            String robotId = root.path("robotId").asText("");
            String type = root.path("type").asText("");

            log.info("收到小车 MQTT 消息: topic={}, robotId={}, type={}, payload={}", topic, robotId, type, payload);
        } catch (Exception e) {
            log.info("收到小车 MQTT 原始消息: topic={}, payload={}", topic, payload);
        }

        if (topic.endsWith("/ack")) {
            log.info("收到小车 ACK: {}", payload);
        } else if (topic.endsWith("/status")) {
            log.info("收到小车状态: {}", payload);
        } else if (topic.endsWith("/online")) {
            log.info("小车上线: {}", payload);
        } else if (topic.endsWith("/offline")) {
            log.info("小车离线: {}", payload);
        }
    }
}