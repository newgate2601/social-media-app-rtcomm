package com.example.social_media_app_rtcomm.service.mapper;

import com.example.social_media_app_rtcomm.dto.groupchat.CreateGroupChatInput;
import com.example.social_media_app_rtcomm.dto.groupchat.GroupChatOutput;
import com.example.social_media_app_rtcomm.entity.ChatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface ChatMapper {
//    ChatOutput getOutputFromEntity(ChatEntity chatEntity);
    GroupChatOutput getGroupChatOutputFromEntity(ChatEntity chatEntity);
    ChatEntity getEntityFromInput(CreateGroupChatInput chatInput);
//    void updateEntityFromInput(@MappingTarget ChatEntity chatEntity, CreateGroupChatInput chatInput);
}
