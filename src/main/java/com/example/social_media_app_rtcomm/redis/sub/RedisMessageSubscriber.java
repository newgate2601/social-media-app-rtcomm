package com.example.social_media_app_rtcomm.redis.sub;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.message.MessageInput;
import com.example.social_media_app_rtcomm.dto.message.MessageOutput;
import com.example.social_media_app_rtcomm.websocket.ChatMessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class RedisMessageSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.error("Join to consume message !!!");
        try {
            MessageInput messageInput = objectMapper.readValue(message.getBody(), MessageInput.class);
            log.info("Message received: " + messageInput.getMessage());

            MessageOutput messageOutput = MessageOutput.builder()
                    .type(Common.CHAT)
                    .createdAt(OffsetDateTime.now())
                    .message(messageInput.getMessage())
                    .chatId(messageInput.getChatId())
                    .imageUrl(messageInput.getImageUrl())
                    .fullName(messageInput.getFullName())
                    .userId(messageInput.getUserId())
                    .build();

            String messageOutputJson = objectMapper.writeValueAsString(messageOutput);
            Long receiverId = Long.valueOf(messageInput.getReceiverId());
            if (ChatMessageHandler.webSocketSessions.containsKey(receiverId)){
                List<WebSocketSession> webSocketSessions =
                        ChatMessageHandler.webSocketSessions.get(receiverId);
                for (WebSocketSession webSocketSession : webSocketSessions){
                    webSocketSession.sendMessage(new TextMessage(messageOutputJson));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
