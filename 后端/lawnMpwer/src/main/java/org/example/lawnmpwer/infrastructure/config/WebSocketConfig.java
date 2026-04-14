package org.example.lawnmpwer.infrastructure.config;

import org.example.lawnmpwer.robot.websocket.FrontendControlHandler;
import org.example.lawnmpwer.robot.websocket.FrontendStreamHandler;
import org.example.lawnmpwer.robot.websocket.RobotControlHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RobotControlHandler robotControlHandler;
    private final FrontendStreamHandler frontendStreamHandler;
    private final FrontendControlHandler frontendControlHandler;

    public WebSocketConfig(RobotControlHandler robotControlHandler,
                           FrontendStreamHandler frontendStreamHandler,
                           FrontendControlHandler frontendControlHandler) {
        this.robotControlHandler = robotControlHandler;
        this.frontendStreamHandler = frontendStreamHandler;
        this.frontendControlHandler = frontendControlHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(frontendControlHandler, "/ws/frontend/control")
                .setAllowedOriginPatterns("*");

        registry.addHandler(frontendStreamHandler, "/ws/frontend/stream")
                .setAllowedOriginPatterns("*");

        // 兼容旧链路，过渡期先保留
        registry.addHandler(robotControlHandler, "/ws/robot/control")
                .setAllowedOriginPatterns("*");
    }
}