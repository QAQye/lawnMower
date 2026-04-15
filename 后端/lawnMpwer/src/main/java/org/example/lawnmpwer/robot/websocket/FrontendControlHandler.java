package org.example.lawnmpwer.robot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.lawnmpwer.robot.dto.FrontendControlMessage;
import org.example.lawnmpwer.robot.service.MqttSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Component
public class FrontendControlHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FrontendControlHandler.class);

    private final ObjectMapper objectMapper;
    private final MqttSender mqttSender;

    public FrontendControlHandler(ObjectMapper objectMapper, MqttSender mqttSender) {
        this.objectMapper = objectMapper;
        this.mqttSender = mqttSender;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("前端 WebSocket 已连接: sessionId={}", session.getId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        resp.put("type", "connected");
        resp.put("msg", "前端控制 WebSocket 连接成功");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            FrontendControlMessage cmd = objectMapper.readValue(message.getPayload(), FrontendControlMessage.class);

            if (cmd.getRobotId() == null || cmd.getRobotId().isBlank()) {
                sendError(session, "robotId 不能为空");
                return;
            }
            if (cmd.getCommand() == null || cmd.getCommand().isBlank()) {
                sendError(session, "command 不能为空");
                return;
            }

            String command = cmd.getCommand().trim().toLowerCase();
            String topic = resolveTopic(cmd.getRobotId(), command);

            Map<String, Object> mqttPayload = new HashMap<>();
            mqttPayload.put("type", "control");
            mqttPayload.put("frontendType", cmd.getType());
            mqttPayload.put("robotId", cmd.getRobotId());
            mqttPayload.put("command", command);
            mqttPayload.put("userId", cmd.getUserId());
            mqttPayload.put("targetIp", cmd.getTargetIp());
            mqttPayload.put("timestamp",
                    cmd.getTimestamp() != null ? cmd.getTimestamp() : System.currentTimeMillis());

            String payloadJson = objectMapper.writeValueAsString(mqttPayload);
            mqttSender.publish(topic, payloadJson);

            Map<String, Object> resp = new HashMap<>();
            resp.put("ok", true);
            resp.put("type", "control_result");
            resp.put("msg", "控制指令已转发到 MQTT");
            resp.put("topic", topic);
            resp.put("robotId", cmd.getRobotId());
            resp.put("frontendType", cmd.getType());
            resp.put("command", command);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));

            log.info("前端指令转发成功: robotId={}, frontendType={}, command={}, topic={}",
                    cmd.getRobotId(), cmd.getType(), command, topic);

        } catch (Exception e) {
            log.error("处理前端控制消息失败", e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    private String resolveTopic(String robotId, String command) {
        // 远程监控视频
        if ("start_cam".equals(command) || "stop_cam".equals(command)) {
            return "robot/" + robotId + "/video/control";
        }
        // 杂草检测视频
        if ("start_ai".equals(command) || "stop_ai".equals(command)) {
            return "robot/" + robotId + "/ai/control";
        }
        // 其余命令仍走底盘/云台/除草控制总线
        return "robot/" + robotId + "/control";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("前端 WebSocket 已断开: sessionId={}, code={}, reason={}",
                session.getId(), status.getCode(), status.getReason());
    }

    private void sendError(WebSocketSession session, String errorMsg) throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", false);
        resp.put("type", "error");
        resp.put("msg", errorMsg);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }
}