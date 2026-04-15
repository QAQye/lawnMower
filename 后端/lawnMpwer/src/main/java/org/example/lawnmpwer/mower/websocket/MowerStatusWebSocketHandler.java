package org.example.lawnmpwer.mower.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MowerStatusWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MowerStatusWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public MowerStatusWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("设备状态 WebSocket 已连接: sessionId={}", session.getId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        resp.put("type", "connected");
        resp.put("msg", "设备状态 WebSocket 连接成功");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("设备状态 WebSocket 已断开: sessionId={}, code={}, reason={}",
                session.getId(), status.getCode(), status.getReason());
    }

    public void broadcastStatus(String robotId,
                                String ipAddress,
                                Integer status,
                                String rawStatus,
                                Long timestamp) {
        try {
            Map<String, Object> resp = new HashMap<>();
            resp.put("type", "mower_status");
            resp.put("robotId", robotId);
            resp.put("ipAddress", ipAddress);
            resp.put("status", status);      // 1 在线，0 离线
            resp.put("rawStatus", rawStatus);
            resp.put("timestamp", timestamp != null ? timestamp : System.currentTimeMillis());

            String text = objectMapper.writeValueAsString(resp);

            Iterator<Map.Entry<String, WebSocketSession>> it = sessions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, WebSocketSession> entry = it.next();
                WebSocketSession session = entry.getValue();

                if (session == null || !session.isOpen()) {
                    it.remove();
                    continue;
                }

                session.sendMessage(new TextMessage(text));
            }

        } catch (Exception e) {
            log.error("广播设备状态失败", e);
        }
    }
}