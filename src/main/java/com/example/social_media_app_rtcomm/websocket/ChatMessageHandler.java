package com.example.social_media_app_rtcomm.websocket;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.Credentials;
import com.example.social_media_app_rtcomm.dto.message.MessageInput;
import com.example.social_media_app_rtcomm.redis.PresenceService;
import com.example.social_media_app_rtcomm.redis.pub.RedisMessagePublisher;
import com.example.social_media_app_rtcomm.redis.sub.config.RedisDynamicSubscriber;
import com.example.social_media_app_rtcomm.security.TokenHelper;
import com.example.social_media_app_rtcomm.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public static Map<Long, List<WebSocketSession>> webSocketSessions = new HashMap<>();
    private final PresenceService presenceService;
    private final RedisDynamicSubscriber redisDynamicSubscriber;
    private final ChatService chatService;
    private final TokenHelper tokenHelper;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        String sessionId = session.getId();
        log.error("Success establish ws connection with sessionId = " + sessionId);
        String successConnectMessage = "Success establish ws connection, please continue send access_token !!!";
        session.sendMessage(new TextMessage(successConnectMessage));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Long userId = getUserId(session);

        List<WebSocketSession> webSocketSessionsOfCurrentRequest = webSocketSessions.get(userId);
        webSocketSessionsOfCurrentRequest.remove(session);
        if (webSocketSessionsOfCurrentRequest.isEmpty()) {
            webSocketSessions.remove(userId);
        } else {
            webSocketSessions.put(userId, webSocketSessionsOfCurrentRequest);
        }

        String userIdString = String.valueOf(userId);
        presenceService.minus1ToSession(userIdString);
        log.error("Amount session of userId = " + presenceService.get(userIdString));
        if (presenceService.get(userIdString) == 0){
            redisDynamicSubscriber.unsubscribeFromChannel(userIdString);
            presenceService.delete(userIdString);
        }
        log.error("Logout ok with userId = " + userId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            String messageContent = textMessage.getPayload();

            log.error("Received message: " + messageContent + " !!!");

            MessageInput messageInput = objectMapper.readValue(messageContent, MessageInput.class);
            if (Objects.isNull(messageInput.getAccessToken())){
                chatService.sendMessage(messageInput, session);
            } else {
                log.error("Received token: " + messageInput.getAccessToken() + " !!!");
                handleFirstMessage(session, messageInput);
            }
        }
    }

    private void handleFirstMessage(WebSocketSession currentSession, MessageInput messageInput) throws Exception {
        String accessToken = messageInput.getAccessToken();
        Long userId = tokenHelper.getUserIdFromToken(accessToken);

        currentSession.getAttributes().put(Common.USER_ID, userId);
        currentSession.getAttributes().put(Common.FULL_NAME, tokenHelper.getFullNameFromToken(accessToken));
        currentSession.getAttributes().put(Common.IMAGE_URL, tokenHelper.getImageUrlFromToken(accessToken));

        List<WebSocketSession> webSocketSessionOfCurrentUser;
        if (webSocketSessions.containsKey(userId)) {
            webSocketSessionOfCurrentUser = webSocketSessions.get(userId);
        } else {
            webSocketSessionOfCurrentUser = new ArrayList<>();
        }
        webSocketSessionOfCurrentUser.add(currentSession);
        webSocketSessions.put(userId, webSocketSessionOfCurrentUser);

        String userIdString = String.valueOf(userId);
        presenceService.plus1ToSession(userIdString);
        log.error("Connect ok with userId = " + userId);
        log.error("Amount session of userId = " + presenceService.get(userIdString));
        redisDynamicSubscriber.subscribeToChannelAfterWSConnect(userIdString);
    }

    private Long getUserId(WebSocketSession session){
        return (Long) session.getAttributes().get(Common.USER_ID);
    }
}
