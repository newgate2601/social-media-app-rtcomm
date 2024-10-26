package com.example.social_media_app_rtcomm.websocket;

import com.example.social_media_app_rtcomm.redis.PresenceService;
import com.example.social_media_app_rtcomm.redis.pub.RedisMessagePublisher;
import com.example.social_media_app_rtcomm.redis.sub.config.RedisDynamicSubscriber;
import com.example.social_media_app_rtcomm.service.ChatService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.*;

@Component
@Slf4j
@AllArgsConstructor
public class ChatMessageHandler extends TextWebSocketHandler {
    public static Map<String, List<WebSocketSession>> webSocketSessions = new HashMap<>();
    private final PresenceService presenceService;
    private final RedisDynamicSubscriber redisDynamicSubscriber;
    private final RedisMessagePublisher redisMessagePublisher;
    private final ChatService chatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        String userId = getUserIdBy(session);
        List<WebSocketSession> webSocketSessionOfCurrentRequest;
        if (webSocketSessions.containsKey(userId)) {
            webSocketSessionOfCurrentRequest = webSocketSessions.get(userId);
        }
        else {
            webSocketSessionOfCurrentRequest = new ArrayList<>();
        }
        webSocketSessionOfCurrentRequest.add(session);
        webSocketSessions.put(userId, webSocketSessionOfCurrentRequest);

        presenceService.plus1ToSession(userId);
        log.error("Connect ok with userId = " + userId);
        log.error("Amount session of userId = " + presenceService.get(userId));
        redisDynamicSubscriber.subscribeToChannelAfterWSConnect(userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
//        webSocketSessions.remove(session);
        String userId = getUserIdBy(session);
        List<WebSocketSession> webSocketSessionsOfCurrentRequest = webSocketSessions.get(userId);
        webSocketSessionsOfCurrentRequest.remove(session);
        if (webSocketSessionsOfCurrentRequest.isEmpty()) {
            webSocketSessions.remove(userId);
        } else {
            webSocketSessions.put(userId, webSocketSessionsOfCurrentRequest);
        }

        presenceService.minus1ToSession(userId);
        log.error("Amount session of userId = " + presenceService.get(userId));
        if (presenceService.get(userId) == 0){
            redisDynamicSubscriber.unsubscribeFromChannel(userId);
            presenceService.delete(userId);
        }
        log.error("Logout ok with userId = " + userId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//        super.handleMessage(session, message);
//        for (WebSocketSession webSocketSession : webSocketSessions) {
//            webSocketSession.sendMessage(message);
//        }

        if (message instanceof TextMessage) {
            // Cast the message to TextMessage
            TextMessage textMessage = (TextMessage) message;

            // Get the message content as a string
            String messageContent = textMessage.getPayload();
            log.info("Received message: " + messageContent);

            chatService.sendMessage(messageContent, getUserIdBy(session));
        }

//        String userId = getUserIdBy(session);
//        if ("1".equals(userId)) {
//            redisMessagePublisher.publish("2", "1 send message");
//        }
//        if ("2".equals(userId)) {
//            redisMessagePublisher.publish("1", "2 send message");
//        }
    }

    private String getUserIdBy(WebSocketSession session){
        HttpHeaders headers = session.getHandshakeHeaders();
        return headers.getFirst("Authorization");
    }
}
