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

    public WebSocketConfig(FrontendStreamHandler frontendStreamHandler) {
        this.frontendStreamHandler = frontendStreamHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 原有的控制接口
        registry.addHandler(new RobotControlHandler(), "/ws/robot")
                .setAllowedOrigins("*");

        // 前端接收视频流的 WebSocket 接口
        registry.addHandler(frontendStreamHandler, "/ws/stream")
                .setAllowedOrigins("*");
    }
}