package org.example.lawnmpwer.robot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RobotSessionManager {

    /**
     * robotId -> WebSocketSession
     */
    private final Map<String, WebSocketSession> robotSessionMap = new ConcurrentHashMap<>();

    /**
     * sessionId -> robotId
     * 用于连接关闭时反查并清理
     */
    private final Map<String, String> sessionRobotMap = new ConcurrentHashMap<>();

    /**
     * 注册小车连接
     */
    public void registerRobot(String robotId, WebSocketSession session) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId 不能为空");
        }

        WebSocketSession oldSession = robotSessionMap.put(robotId, session);
        sessionRobotMap.put(session.getId(), robotId);

        if (oldSession != null && oldSession.isOpen() && !oldSession.getId().equals(session.getId())) {
            try {
                oldSession.close();
            } catch (IOException e) {
                log.warn("关闭旧连接失败, robotId={}", robotId, e);
            }
        }

        log.info(" 小车注册成功, robotId={}, sessionId={}", robotId, session.getId());
        log.info(" 当前在线小车: {}", robotSessionMap.keySet());
    }

    /**
     * 根据 sessionId 移除连接
     */
    public void removeBySessionId(String sessionId) {
        String robotId = sessionRobotMap.remove(sessionId);
        if (robotId != null) {
            robotSessionMap.remove(robotId);
            log.info("⚠ 小车连接已移除, robotId={}, sessionId={}", robotId, sessionId);
            log.info("⚠ 当前在线小车: {}", robotSessionMap.keySet());
        }
    }

    /**
     * 是否在线
     */
    public boolean isRobotOnline(String robotId) {
        WebSocketSession session = robotSessionMap.get(robotId);
        return session != null && session.isOpen();
    }

    /**
     * 发送文本消息给指定小车
     */
    public boolean sendToRobot(String robotId, String payload) {
        WebSocketSession session = robotSessionMap.get(robotId);

        if (session == null) {
            log.warn("❌ 发送失败：未找到小车连接, robotId={}", robotId);
            return false;
        }

        if (!session.isOpen()) {
            log.warn("❌ 发送失败：小车连接已关闭, robotId={}", robotId);
            robotSessionMap.remove(robotId);
            sessionRobotMap.remove(session.getId());
            return false;
        }

        synchronized (session) {
            try {
                session.sendMessage(new TextMessage(payload));
                log.info("✅ 已通过 WebSocket 向小车发送消息, robotId={}, payload={}", robotId, payload);
                return true;
            } catch (IOException e) {
                log.error("❌ WebSocket 发送失败, robotId={}", robotId, e);
                return false;
            }
        }
    }

    /**
     * 获取在线设备列表
     */
    public Set<String> getOnlineRobotIds() {
        return robotSessionMap.keySet();
    }

    /**
     * 获取在线数量
     */
    public int onlineCount() {
        return robotSessionMap.size();
    }
}