package org.example.lawnmpwer.infrastructure.config;

import org.example.lawnmpwer.robot.websocket.FrontendStreamHandler;
import org.example.lawnmpwer.robot.websocket.RobotControlHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final FrontendStreamHandler frontendStreamHandler;
    private final RobotControlHandler robotControlHandler;

    public WebSocketConfig(FrontendStreamHandler frontendStreamHandler,
                           RobotControlHandler robotControlHandler) {
        this.frontendStreamHandler = frontendStreamHandler;
        this.robotControlHandler = robotControlHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 小车控制 WebSocket 接口
        registry.addHandler(robotControlHandler, "/ws/robot/control")
                .setAllowedOrigins("*");

        // 如果你前端或旧代码里还在用 /ws/robot，也一起保留，避免旧逻辑失效
        registry.addHandler(robotControlHandler, "/ws/robot")
                .setAllowedOrigins("*");

        // 前端接收视频流的 WebSocket 接口（保留你原来的逻辑）
        registry.addHandler(frontendStreamHandler, "/ws/stream")
                .setAllowedOrigins("*");

        registry.addHandler(frontendStreamHandler, "/*")
                .setAllowedOrigins("*");
    }
}