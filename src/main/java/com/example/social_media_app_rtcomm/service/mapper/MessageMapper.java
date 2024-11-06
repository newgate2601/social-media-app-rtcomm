package com.example.social_media_app_rtcomm.service.mapper;

import com.example.social_media_app_rtcomm.dto.message.MessageInput;
import com.example.social_media_app_rtcomm.dto.message.MessageOutputList;
import com.example.social_media_app_rtcomm.entity.ChatEntity;
import com.example.social_media_app_rtcomm.entity.ChatOutput;
import com.example.social_media_app_rtcomm.entity.MessageEntity;
import org.mapstruct.Mapper;

@Mapper
public interface MessageMapper {
    MessageEntity getEntityFromInput(MessageInput messageInput);
    MessageOutputList getOutputFromEntity(MessageEntity messageEntity);
    ChatOutput getOutputFromEntity(ChatEntity chatEntity);
}
