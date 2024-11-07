package com.example.social_media_app_rtcomm.service;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.message.MessageInput;
import com.example.social_media_app_rtcomm.entity.ChatEntity;
import com.example.social_media_app_rtcomm.entity.EventNotificationEntity;
import com.example.social_media_app_rtcomm.entity.MessageEntity;
import com.example.social_media_app_rtcomm.entity.UserChatMapEntity;
import com.example.social_media_app_rtcomm.redis.PresenceService;
import com.example.social_media_app_rtcomm.redis.pub.RedisMessagePublisher;
import com.example.social_media_app_rtcomm.repository.*;
import com.example.social_media_app_rtcomm.security.TokenHelper;
import com.example.social_media_app_rtcomm.service.mapper.MessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final CustomRepository customRepository;
    private final MessageMapper messageMapper;
    private final PresenceService presenceService;
    private final RedisMessagePublisher redisMessagePublisher;
    private final TokenHelper tokenHelper;
    private final UserChatMapRepository userChatMapRepository;
    private final EventNotificationRepository eventNotificationRepository;

    @Transactional
    public void sendMessage(MessageInput messageInput, WebSocketSession session) {
        LocalDateTime now = LocalDateTime.now();
        Long senderId = (Long) session.getAttributes().get(Common.USER_ID);
        String imageUrl = (String) session.getAttributes().get(Common.IMAGE_URL);
        String fullName = (String) session.getAttributes().get(Common.FULL_NAME);

        messageInput.setUserId(senderId);
        messageInput.setImageUrl(imageUrl);
        messageInput.setFullName(fullName);

        ChatEntity chatEntity = customRepository.getChat(messageInput.getChatId());
        chatEntity.setNewestUserId(senderId);
        chatEntity.setNewestMessage(messageInput.getMessage());
        chatEntity.setNewestChatTime(now);

        MessageEntity messageEntity = messageMapper.getEntityFromInput(messageInput);
        messageEntity.setSenderId(senderId);
        messageEntity.setCreatedAt(LocalDateTime.now());
        Long chatId2;
        if (chatEntity.getChatType().equals(Common.USER)) {
            ChatEntity chatEntity2 = chatRepository.findByUserId1AndUserId2(chatEntity.getUserId2(), chatEntity.getUserId1());
            chatId2 = chatEntity2.getId();
            messageEntity.setChatId1(chatEntity.getId());
            messageEntity.setChatId2(chatEntity2.getId());
            chatEntity2.setNewestMessage(messageInput.getMessage());
            chatEntity2.setNewestUserId(senderId);
            chatEntity2.setNewestChatTime(now);
            chatRepository.save(chatEntity2);
        } else {
            chatId2 = null;
            messageEntity.setGroupChatId(chatEntity.getId());
        }
        messageRepository.save(messageEntity);
        CompletableFuture.runAsync(() -> {
            chatRepository.save(chatEntity);
            // if chat user-user
            if (chatEntity.getChatType().equals(Common.USER)) {
                eventNotificationRepository.save(
                        EventNotificationEntity.builder()
                                .eventType(Common.MESSAGE)
                                .userId(chatEntity.getUserId1())
                                .imageUrl(imageUrl)
                                .fullName(fullName)
                                .state(Common.NEW_EVENT)
                                .chatId(chatId2)
                                .createdAt(now)
                                .message(messageInput.getMessage())
                                .build()
                );
                assert chatId2 != null;
                messageInput.setChatId(chatId2);
                sendMessageUserToUser(String.valueOf(chatEntity.getUserId2()), messageInput);
            } else if (chatEntity.getChatType().equals(Common.GROUP)) {
                sendMessageToUsersInGroup(senderId, fullName, imageUrl, messageInput);
            }
        });
    }

    private void sendMessageToUsersInGroup(Long senderId, String fullName, String imageUrl,
                                           MessageInput messageInput) {
        List<UserChatMapEntity> userChatMapEntities =
                userChatMapRepository.findAllByChatId(messageInput.getChatId());
        if (Objects.isNull(userChatMapEntities)
                || userChatMapEntities.isEmpty()
                || userChatMapEntities.size() == 1) {
            return;
        }
        List<Long> receiverIds = userChatMapEntities.stream()
                .map(UserChatMapEntity::getUserId)
                .distinct()
                .filter(id -> !id.equals(senderId))
                .toList();
        for (Long receiverId : receiverIds) {
            eventNotificationRepository.save(
                    EventNotificationEntity.builder()
                            .eventType(Common.MESSAGE)
                            .userId(receiverId)
                            .imageUrl(imageUrl)
                            .fullName(fullName)
                            .state(Common.NEW_EVENT)
                            .chatId(messageInput.getChatId())
                            .createdAt(LocalDateTime.now())
                            .message(messageInput.getMessage())
                            .build()
            );
            sendMessageUserToUser(String.valueOf(receiverId), messageInput);
        }
    }

    private void sendMessageUserToUser(String receiverId, MessageInput messageInput) {
        Integer amountSessionOfReceiver = presenceService.get(receiverId);
        messageInput.setReceiverId(receiverId);
        if (!Objects.isNull(amountSessionOfReceiver) && amountSessionOfReceiver > 0) {
            redisMessagePublisher.publish(receiverId, messageInput);
        }
    }

    @Transactional
    public void createChatForUsersAfterAcceptFriend(Long receiverId,
//                                                    String fullName, String imageUrl,
                                                    Long senderId) {
        // lay thong tin receiver, sender tu uaa service

        chatRepository.save(
                ChatEntity.builder()
//                        .name(fullName)
//                        .imageUrl(imageUrl)
                        .chatType(Common.USER)
                        .userId1(receiverId)
                        .userId2(senderId)
                        .build()
        );

        chatRepository.save(
                ChatEntity.builder()
//                        .name(fullName)
//                        .imageUrl(imageUrl)
                        .chatType(Common.USER)
                        .userId2(receiverId)
                        .userId1(senderId)
                        .build()
        );
    }
}
