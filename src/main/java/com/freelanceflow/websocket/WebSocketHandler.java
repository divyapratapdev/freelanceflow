package com.freelanceflow.websocket;

import com.freelanceflow.auth.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    private final JwtUtil jwtUtil;
    
    // ConcurrentHashMap mapping userId to list of active sessions (for multiple tabs)
    private final Map<Long, List<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public WebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid or missing JWT"));
            return;
        }

        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
        log.info("WebSocket connected for user: {} (session: {})", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            List<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        log.info("WebSocket disconnected for session: {}", session.getId());
    }

    public void sendDashboardUpdate(Long userId, String payloadJson) {
        List<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        TextMessage message = new TextMessage(payloadJson);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                log.error("Failed to send WS message to user {}", userId, e);
            }
        }
    }

    private Long extractUserIdFromSession(WebSocketSession session) {
        if (session.getUri() == null) return null;
        
        String query = session.getUri().getQuery();
        if (query == null) return null;

        Map<String, String> queryParams = UriComponentsBuilder.fromUri(session.getUri())
                .build().getQueryParams().toSingleValueMap();

        String token = queryParams.get("token");
        if (token == null) return null;

        return jwtUtil.validateAndGetUserId(token);
    }
}
