package com.example.social_media_app_rtcomm.service;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.message.MessageInput;
import com.example.social_media_app_rtcomm.entity.ChatEntity;
import com.example.social_media_app_rtcomm.entity.MessageEntity;
import com.example.social_media_app_rtcomm.entity.UserChatMapEntity;
import com.example.social_media_app_rtcomm.redis.PresenceService;
import com.example.social_media_app_rtcomm.redis.pub.RedisMessagePublisher;
import com.example.social_media_app_rtcomm.repository.ChatRepository;
import com.example.social_media_app_rtcomm.repository.CustomRepository;
import com.example.social_media_app_rtcomm.repository.MessageRepository;
import com.example.social_media_app_rtcomm.repository.UserChatMapRepository;
import com.example.social_media_app_rtcomm.security.TokenHelper;
import com.example.social_media_app_rtcomm.service.mapper.MessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void sendMessage(String messageJson, String accessToken) {
        try {
            MessageInput messageInput = objectMapper.readValue(messageJson, MessageInput.class);
            LocalDateTime now = LocalDateTime.now();

            Long senderId = tokenHelper.getUserIdFromToken(accessToken);

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
                    assert chatId2 != null;
                    messageInput.setChatId(chatId2);
                    sendMessageUserToUser(String.valueOf(chatEntity.getUserId2()), messageInput);
                }
                else if (chatEntity.getChatType().equals(Common.GROUP)) {
                    sendMessageToUsersInGroup(senderId, messageInput);
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessageToUsersInGroup(Long senderId, MessageInput messageInput){
        List<UserChatMapEntity> userChatMapEntities =
                userChatMapRepository.findAllByChatId(messageInput.getChatId());
        if (Objects.isNull(userChatMapEntities)
                || userChatMapEntities.isEmpty()
                || userChatMapEntities.size() == 1){
            return;
        }
        List<Long> receiverIds = userChatMapEntities.stream()
                .map(UserChatMapEntity::getUserId)
                .distinct()
                .filter(id -> !id.equals(senderId))
                .toList();
        for (Long receiverId : receiverIds){
            sendMessageUserToUser(String.valueOf(receiverId), messageInput);
        }
    }

    private void sendMessageUserToUser(String receiverId, MessageInput messageInput){
        Integer amountSessionOfReceiver = presenceService.get(receiverId);
        messageInput.setReceiverId(receiverId);
        if (!Objects.isNull(amountSessionOfReceiver) && amountSessionOfReceiver > 0){
            redisMessagePublisher.publish(receiverId, messageInput);
        }
    }

    @Transactional
    public void createChatForUsersAfterAcceptFriend(Long receiverId, Long senderId){
        // lay thong tin receiver, sender tu uaa service

        chatRepository.save(
                ChatEntity.builder()
//                        .name(receiver.getFullName())
//                        .imageUrl(receiver.getImageUrl())
                        .chatType(Common.USER)
                        .userId1(receiverId)
                        .userId2(senderId)
                        .build()
        );

        chatRepository.save(
                ChatEntity.builder()
//                        .name(sender.getFullName())
//                        .imageUrl(sender.getImageUrl())
                        .chatType(Common.USER)
                        .userId2(receiverId)
                        .userId1(senderId)
                        .build()
        );
    }
}
