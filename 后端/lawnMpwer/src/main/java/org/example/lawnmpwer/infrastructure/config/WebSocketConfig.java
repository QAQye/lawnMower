package org.example.lawnmpwer.infrastructure.config;

import org.example.lawnmpwer.mower.websocket.MowerStatusWebSocketHandler;
import org.example.lawnmpwer.robot.websocket.FrontendControlHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final FrontendControlHandler frontendControlHandler;
    private final MowerStatusWebSocketHandler mowerStatusWebSocketHandler;

    public WebSocketConfig(FrontendControlHandler frontendControlHandler,
                           MowerStatusWebSocketHandler mowerStatusWebSocketHandler) {
        this.frontendControlHandler = frontendControlHandler;
        this.mowerStatusWebSocketHandler = mowerStatusWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(frontendControlHandler, "/ws/frontend/control")
                .setAllowedOriginPatterns("*");

        registry.addHandler(mowerStatusWebSocketHandler, "/ws/mower/status")
                .setAllowedOriginPatterns("*");
    }
}