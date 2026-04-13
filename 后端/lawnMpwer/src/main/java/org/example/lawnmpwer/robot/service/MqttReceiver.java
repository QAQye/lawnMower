package org.example.lawnmpwer.robot.service;
//mqtt接收服务

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lawnmpwer.robot.dto.FrontendControlMessage;
import org.example.lawnmpwer.robot.dto.RobotWsControlMessage;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttReceiver {

    private final ObjectMapper objectMapper;
    private final RobotSessionManager robotSessionManager;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void receive(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            log.info("收到前端 MQTT 消息: {}", payload);

            FrontendControlMessage cmd = objectMapper.readValue(payload, FrontendControlMessage.class);

            log.info("解析后: userId={}, robotId={}, type={}, command={}, targetIp={}, timestamp={}",
                    cmd.getUserId(),
                    cmd.getRobotId(),
                    cmd.getType(),
                    cmd.getCommand(),
                    cmd.getTargetIp(),
                    cmd.getTimestamp()
            );

            if (cmd.getRobotId() == null || cmd.getRobotId().isBlank()) {
                log.warn("MQTT 转发失败：robotId 为空");
                return;
            }

            if (cmd.getCommand() == null || cmd.getCommand().isBlank()) {
                log.warn("MQTT 转发失败：command 为空");
                return;
            }

            // 构造发给小车的 WebSocket 消息
            RobotWsControlMessage wsMessage = new RobotWsControlMessage(
                    "control",
                    cmd.getRobotId(),
                    cmd.getCommand(),
                    cmd.getTimestamp() != null ? cmd.getTimestamp() : System.currentTimeMillis()
            );

            String wsPayload = objectMapper.writeValueAsString(wsMessage);

            boolean sent = robotSessionManager.sendToRobot(cmd.getRobotId(), wsPayload);

            if (sent) {
                log.info("MQTT -> WebSocket 转发成功, robotId={}, command={}",
                        cmd.getRobotId(), cmd.getCommand());
            } else {
                log.warn("MQTT -> WebSocket 转发失败, robotId={}, command={}",
                        cmd.getRobotId(), cmd.getCommand());
            }

        } catch (Exception e) {
            log.error("处理 MQTT 消息失败", e);
        }
    }
}