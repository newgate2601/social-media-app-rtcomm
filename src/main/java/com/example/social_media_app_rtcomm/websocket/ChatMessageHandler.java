package com.example.social_media_app_rtcomm.websocket;

import com.example.social_media_app_rtcomm.redis.PresenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ChatMessageHandler extends TextWebSocketHandler {
    List<WebSocketSession> webSocketSessions = Collections.synchronizedList(new ArrayList<>());
    private final PresenceService presenceService;

    public ChatMessageHandler(PresenceService presenceService){
        this.presenceService = presenceService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        webSocketSessions.add(session);
        String userId = getUserIdBy(session);
        presenceService.plus1ToSession(userId);
        log.error("Connect ok with userId = " + userId);
        log.error("Amount session of userId = " + presenceService.get(userId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        webSocketSessions.remove(session);
        String userId = getUserIdBy(session);
        presenceService.minus1ToSession(userId);
        log.error("Logout ok with userId = " + userId);
        log.error("Amount session of userId = " + presenceService.get(userId));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        for (WebSocketSession webSocketSession : webSocketSessions) {
            webSocketSession.sendMessage(message);
        }
    }

    private String getUserIdBy(WebSocketSession session){
        HttpHeaders headers = session.getHandshakeHeaders();
        return headers.getFirst("Authorization");
    }
}
