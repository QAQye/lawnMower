package org.example.lawnmpwer.robot.stream.service;

import org.example.lawnmpwer.robot.websocket.FrontendStreamHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CarStreamService extends TextWebSocketHandler {

    private static final String ROBOT_IP = "192.168.1.11";
    private static final int YOLO_WS_PORT = 8765;

    private final AtomicReference<String> latestPayload = new AtomicReference<>("{}");
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    private volatile WebSocketSession carSession;
    private volatile boolean connecting = false;

    private final FrontendStreamHandler frontendStreamHandler;

    // 使用 @Lazy 配合，防止项目启动时循环依赖
    public CarStreamService(@Lazy FrontendStreamHandler frontendStreamHandler) {
        this.frontendStreamHandler = frontendStreamHandler;
    }

    public void connectToRobot() {
        if (connecting || (carSession != null && carSession.isOpen())) {
            return;
        }

        connecting = true;
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            client.setUserProperties(Map.of(
                    "org.apache.tomcat.websocket.textBufferSize", 10 * 1024 * 1024,
                    "org.apache.tomcat.websocket.binaryBufferSize", 10 * 1024 * 1024
            ));

            String wsUrl = "ws://" + ROBOT_IP + ":" + YOLO_WS_PORT;
            System.out.println("🚀 [网关] 正在连接小车: " + wsUrl);

            client.execute(this, wsUrl);
        } catch (Exception e) {
            System.err.println("❌ [网关] 连接失败: " + e.getMessage());
            // 仅当还有前端观众时，才安排重连
            if (frontendStreamHandler.hasViewers()) {
                scheduleReconnect();
            }
        } finally {
            connecting = false;
        }
    }

    // 主动断开小车连接
    public void disconnectFromRobot() {
        if (carSession != null && carSession.isOpen()) {
            try {
                carSession.close();
                carSession = null;
                System.out.println("🛑 [网关] 前端无人观看，已主动断开与小车的连接，让小车休息。");
            } catch (Exception e) {
                System.err.println("❌ [网关] 关闭小车连接异常: " + e.getMessage());
            }
        }
    }

    private void scheduleReconnect() {
        reconnectExecutor.schedule(() -> {
            if (frontendStreamHandler.hasViewers()) {
                connectToRobot();
            } else {
                System.out.println("🛑 [网关] 取消重连：前端已无观众。");
            }
        }, 3, TimeUnit.SECONDS);

        System.out.println("🔄 [网关] 3 秒后重试连接...");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(10 * 1024 * 1024);
        session.setBinaryMessageSizeLimit(10 * 1024 * 1024);

        this.carSession = session;
        System.out.println("✅ [网关] 成功连接到小车 YOLO 视频流服务端");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        latestPayload.set(message.getPayload());
        // 瞬间将画面推给所有人，实现低延迟
        frontendStreamHandler.broadcast(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("[网关] WebSocket 传输异常: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        this.carSession = null;
        System.out.println("⚠️ [网关] 与小车连接断开: " + status);

        if (frontendStreamHandler.hasViewers()) {
            scheduleReconnect();
        }
    }

    public String getLatestPayload() {
        return latestPayload.get();
    }
}