package com.example.social_media_app_rtcomm.service;

import com.example.social_media_app_rtcomm.base.filter.Filter;
import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.UserDto;
import com.example.social_media_app_rtcomm.dto.message.MessageOutput;
import com.example.social_media_app_rtcomm.dto.message.MessageOutputList;
import com.example.social_media_app_rtcomm.entity.*;
import com.example.social_media_app_rtcomm.feign.impl.UaaServiceProxy;
import com.example.social_media_app_rtcomm.repository.*;
import com.example.social_media_app_rtcomm.security.TokenHelper;
import com.example.social_media_app_rtcomm.service.mapper.MessageMapper;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class GetChatService {
    private final TokenHelper tokenHelper;
    private final EntityManager entityManager;
    private final CustomRepository customRepository;
    private final UserChatMapRepository userChatRepository;
    private final UaaServiceProxy uaaServiceProxy;
    private final MessageMapper messageMapper;
    private final EventNotificationRepository eventNotificationRepository;
    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public ChatEntity getChatBy(Long userId1, Long userId2){
        // userId 1 laf minh, userId2 la ban be
        return chatRepository.findByUserId1AndUserId2(userId1, userId2);
    }

    @Transactional
    public Page<MessageOutputList> getMessages(String accessToken, Long chatId, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        eventNotificationRepository.deleteAllByUserIdAndChatId(userId, chatId);

        Page<MessageEntity> messageEntities = Filter.builder(MessageEntity.class, entityManager)
                .search()
                .isEqual("chatId1", chatId)
                .isEqual("chatId2", chatId)
                .isEqual("groupChatId", chatId)
                .orderBy("createdAt", Common.DESC)
                .getPage(pageable);

        List<Long> userIds = new ArrayList<>();
        ChatEntity chatEntity = customRepository.getChat(chatId);
        if (Common.USER.equals(chatEntity.getChatType())) {
            userIds.add(chatEntity.getUserId1());
            userIds.add(chatEntity.getUserId2());
        } else if (Common.GROUP.equals(chatEntity.getChatType())) {
            userIds.addAll(
                    userChatRepository.findAllByChatId(chatId).stream()
                            .map(UserChatMapEntity::getUserId)
                            .toList()
            );
        }
        Map<Long, UserDto> userEntityMap = uaaServiceProxy.getUsersBy(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return messageEntities.map(
                messageEntity -> {
                    MessageOutputList messageOutput = messageMapper.getOutputFromEntity(messageEntity);
                    if (userEntityMap.containsKey(messageEntity.getSenderId())) {
                        UserDto userEntity = userEntityMap.get(messageEntity.getSenderId());
                        messageOutput.setUserId(userEntity.getId());
                        messageOutput.setFullName(userEntity.getFullName());
                        messageOutput.setImageUrl(userEntity.getImageUrl());
                        messageOutput.setIsMe(userId.equals(userEntity.getId()));
                    }
                    return messageOutput;
                }
        );
    }

    @Transactional(readOnly = true)
    public Page<ChatOutput> getChatList(String search, String accessToken, Pageable pageable) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        List<Long> chatIds = null;
        List<UserChatMapEntity> userChatMapEntities = userChatRepository.findAllByUserId(userId);
        if (Objects.nonNull(userChatMapEntities) && !userChatMapEntities.isEmpty()) {
            chatIds = userChatMapEntities.stream()
                    .map(UserChatMapEntity::getChatId)
                    .toList();
        }

        Page<ChatEntity> chatEntities = Filter.builder(ChatEntity.class, entityManager)
                .search()
                .isIn("id", chatIds)
                .isEqual("userId1", userId)
                .filter()
                .isContain("name", search)
                .isNotNull("newestChatTime")
                .orderBy("newestChatTime", Common.DESC)
                .getPage(pageable);

        Map<Long, List<EventNotificationEntity>> eventNotificationMap =
                eventNotificationRepository.findAllByUserIdAndEventType(userId, Common.MESSAGE).stream()
                        .collect(Collectors.groupingBy(EventNotificationEntity::getChatId));

        return chatEntities.map(chatEntity -> {
            ChatOutput chatOutput = messageMapper.getOutputFromEntity(chatEntity);
            if (eventNotificationMap.containsKey(chatOutput.getId())) {
                chatOutput.setMessageCount(eventNotificationMap.get(chatOutput.getId()).size());
            } else {
                chatOutput.setMessageCount(0);
            }
            chatOutput.setIsMe(userId.equals(chatEntity.getNewestUserId()));
            return chatOutput;
        });
    }
}
