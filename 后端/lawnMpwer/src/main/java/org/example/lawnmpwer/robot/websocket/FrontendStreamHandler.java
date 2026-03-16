package org.example.lawnmpwer.robot.websocket;

import org.example.lawnmpwer.robot.stream.service.CarStreamService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FrontendStreamHandler extends TextWebSocketHandler {

    // 线程安全地存储所有打开网页的用户
    private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private final CarStreamService carStreamService;

    // 使用 @Lazy 延迟加载，避免循环依赖报错
    public FrontendStreamHandler(@Lazy CarStreamService carStreamService) {
        this.carStreamService = carStreamService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("📺 [前端推流] 网页打开！当前观看人数: " + sessions.size());

        // 第一个观众进入时，连接小车
        if (sessions.size() == 1) {
            carStreamService.connectToRobot();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("📺 [前端推流] 网页关闭" + sessions.size());

        // 最后一个观众离开时，断开小车连接
        if (sessions.isEmpty()) {
            carStreamService.disconnectFromRobot();
        }
    }

    // 广播画面给所有在线用户
    public void broadcast(String payload) {
        TextMessage message = new TextMessage(payload);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    System.err.println("❌ 发送画面失败: " + e.getMessage());
                }
            }
        }
    }

    // 提供给 CarStreamService 判断是否还有人观看
    public boolean hasViewers() {
        return !sessions.isEmpty();
    }
}