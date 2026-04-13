package org.example.lawnmpwer.robot.websocket; // ⚠️请注意包名与你本地保持一致

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lawnmpwer.robot.dto.RobotRegisterMessage;
import org.example.lawnmpwer.robot.service.RobotSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
//小车控制指令处理
@Slf4j
@Component
@RequiredArgsConstructor
public class RobotControlHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RobotSessionManager robotSessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("小车 WebSocket 已连接, sessionId={}, remote={}",
                session.getId(),
                session.getRemoteAddress());

        // 给小车回个提示，方便调试
        session.sendMessage(new TextMessage("{\"type\":\"connected\",\"message\":\"robot websocket connected\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到小车 WebSocket 消息, sessionId={}, payload={}", session.getId(), payload);

        JsonNode root = objectMapper.readTree(payload);
        String type = root.path("type").asText("");

        // 1. 注册消息
        if ("register".equalsIgnoreCase(type)) {
            RobotRegisterMessage registerMessage = objectMapper.readValue(payload, RobotRegisterMessage.class);
            String robotId = registerMessage.getRobotId();

            if (robotId == null || robotId.isBlank()) {
                log.warn("注册失败：robotId 为空, sessionId={}", session.getId());
                session.sendMessage(new TextMessage("{\"type\":\"register_result\",\"success\":false,\"message\":\"robotId is blank\"}"));
                return;
            }

            robotSessionManager.registerRobot(robotId, session);

            session.sendMessage(new TextMessage(
                    String.format("{\"type\":\"register_result\",\"success\":true,\"robotId\":\"%s\"}", robotId)
            ));
            return;
        }

        // 2. ACK / 状态消息，先只打日志
        if ("ack".equalsIgnoreCase(type) || "status".equalsIgnoreCase(type)) {
            log.info(" 收到小车ACK/状态消息: {}", payload);
            return;
        }

        // 3. 其他未知消息
        log.warn("未识别的小车 WebSocket 消息类型, type={}, payload={}", type, payload);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("小车 WebSocket 通信异常, sessionId={}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("小车 WebSocket 已断开, sessionId={}, code={}, reason={}",
                session.getId(),
                status.getCode(),
                status.getReason());

        robotSessionManager.removeBySessionId(session.getId());
    }
}