package com.example.social_media_app_rtcomm.service;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.UserDto;
import com.example.social_media_app_rtcomm.dto.groupchat.*;
import com.example.social_media_app_rtcomm.entity.ChatEntity;
import com.example.social_media_app_rtcomm.entity.UserChatMapEntity;
import com.example.social_media_app_rtcomm.feign.impl.UaaServiceProxy;
import com.example.social_media_app_rtcomm.repository.ChatRepository;
import com.example.social_media_app_rtcomm.repository.CustomRepository;
import com.example.social_media_app_rtcomm.repository.UserChatMapRepository;
import com.example.social_media_app_rtcomm.security.TokenHelper;
import com.example.social_media_app_rtcomm.service.mapper.ChatMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupChatService {
    private final ChatMapper chatMapper;
    private final ChatRepository chatRepository;
    private final UserChatMapRepository userChatMapRepository;
    private final CustomRepository customRepository;
    private final TokenHelper tokenHelper;
    private final UaaServiceProxy uaaServiceProxy;

    private ChatEntity detail(Long chatId) { // Class
        return chatRepository.findById(chatId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }

    @Transactional(readOnly = true)
    public Page<GroupChatOutput> getGroups(String accessToken, String search, Pageable pageable){
        // lay map => lay chatId
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        List<Long> chatIds = userChatMapRepository.findAllByUserId(userId).stream().map(
                UserChatMapEntity::getChatId
        ).collect(Collectors.toList());

        Page<ChatEntity> groupChatEntities = Page.empty();
        if (Objects.isNull(search)){
            groupChatEntities = chatRepository.findAllByIdIn(chatIds, pageable);
        } else{
            groupChatEntities = chatRepository.findAllByNameContainingIgnoreCaseAndIdIn(search, chatIds, pageable);
        }

        return groupChatEntities.map(chatEntity -> {
            GroupChatOutput groupChatOutPut = chatMapper.getGroupChatOutputFromEntity(chatEntity);
            groupChatOutPut.setImg(Common.DEFAULT_IMAGE_URL);
            return groupChatOutPut;
        });
    }


    @Transactional
    public void createGroupChat(CreateGroupChatInput createGroupChatInput, String accessToken) {
        Long managerId = tokenHelper.getUserIdFromToken(accessToken);
        if (createGroupChatInput.getUserIds().contains(managerId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        ChatEntity chatEntity = chatMapper.getEntityFromInput(createGroupChatInput);

        chatEntity.setManagerId(managerId);
        chatEntity.setNewestChatTime(LocalDateTime.now());
        chatEntity.setChatType(Common.GROUP);
        chatEntity.setNewestUserId(managerId);
        chatEntity.setNewestMessage("Created Group");
        chatEntity.setImageUrl("https://res.cloudinary.com/ds9ipqi3z/image/upload/v1713964597/o01ocuafi6hrpnd6basc.jpg");

        chatRepository.save(chatEntity);

        userChatMapRepository.save(
                UserChatMapEntity.builder()
                        .userId(managerId)
                        .chatId(chatEntity.getId())
                        .build()
        );

        for (Long userId : createGroupChatInput.getUserIds()) {
            userChatMapRepository.save(
                    UserChatMapEntity.builder()
                            .userId(userId)
                            .chatId(chatEntity.getId())
                            .build()
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ChatMemberOutput> getGroupChatMembersBy(Long groupChatId, String accessToken) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (Boolean.FALSE.equals(userChatMapRepository.existsByUserIdAndChatId(userId, groupChatId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        List<UserChatMapEntity> userChatEntities = userChatMapRepository.findAllByChatId(groupChatId);
        ChatEntity chatEntity = detail(groupChatId);
        Long managerId = chatEntity.getManagerId();

        List<UserDto> userEntities = uaaServiceProxy.getUsersBy(
                userChatEntities.stream().map(UserChatMapEntity::getUserId).collect(Collectors.toList())
        );

        List<ChatMemberOutput> groupMemberOutputs = new ArrayList<>();
        for (UserDto user : userEntities) {
            if (Objects.equals(user.getId(), managerId)) {
                groupMemberOutputs.add(
                        ChatMemberOutput.builder()
                                .id(user.getId())
                                .fullName(user.getFullName())
                                .imageUrl(user.getImageUrl())
                                .role(Common.ADMIN)
                                .build()
                );
            } else {
                groupMemberOutputs.add(
                        ChatMemberOutput.builder()
                                .id(user.getId())
                                .fullName(user.getFullName())
                                .imageUrl(user.getImageUrl())
                                .role(Common.MEMBER)
                                .build()
                );
            }
        }
        return groupMemberOutputs;
    }

    @Transactional
    public void addNewMemberToGroupChat(ChatAddNewMemberInput chatAddNewMemberInput, String accessToken) {
        List<UserChatMapEntity> userChatMapEntities =
                userChatMapRepository.findAllByChatId(chatAddNewMemberInput.getGroupChatId());

        List<Long> userIdsInGroup = userChatMapEntities.stream()
                .map(UserChatMapEntity::getUserId)
                .toList();

        for (Long newUserId : chatAddNewMemberInput.getUserIds()) {
            if (!userIdsInGroup.contains(newUserId)) {
                userChatMapRepository.save(
                        UserChatMapEntity.builder()
                                .chatId(chatAddNewMemberInput.getGroupChatId())
                                .userId(newUserId)
                                .build()
                );
            }
        }
    }

    @Transactional
    public void deleteMember(String accessToken, ChatDeleteMemberInput chatDeleteMemberInput) {
        ChatEntity chatEntity = chatRepository.findById(chatDeleteMemberInput.getGroupChatId()).get();
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (!Objects.equals(chatEntity.getManagerId(), userId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Objects.equals(chatDeleteMemberInput.getUserId(), userId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        userChatMapRepository.deleteByUserIdAndChatId(
                chatDeleteMemberInput.getUserId(),
                chatDeleteMemberInput.getGroupChatId()
        );
    }

    @Transactional
    public void leaveTheGroupChat(String accessToken, Long chatId) {
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        if (userChatMapRepository.countByChatId(chatId) > 1) {
            userChatMapRepository.deleteByUserIdAndChatId(
                    userId,
                    chatId
            );
        } else {
            userChatMapRepository.deleteByUserIdAndChatId(
                    userId,
                    chatId
            );
//            chatRepository.deleteById(chatLeaveTheGroupInput.getGroupId());
        }
    }
}
