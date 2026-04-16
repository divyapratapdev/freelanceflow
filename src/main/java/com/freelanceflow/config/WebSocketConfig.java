package com.freelanceflow.websocket;

import com.freelanceflow.auth.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    public WebSocketConfig(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Auth is checked via query param token in WebSocketHandler
        // setAllowedOriginPatterns("*") is required instead of setAllowedOrigins("*")
        registry.addHandler(webSocketHandler, "/ws")
                .setAllowedOriginPatterns("*");
    }
}
