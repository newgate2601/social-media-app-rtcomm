package com.example.social_media_app_rtcomm.repository;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.entity.ChatEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class CustomRepository {
    private final ChatRepository chatRepository;

    public ChatEntity getChat(Long chatId){
        return chatRepository.findById(chatId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }
}
