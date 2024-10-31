package com.example.social_media_app_rtcomm.repository;

import com.example.social_media_app_rtcomm.entity.UserChatMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChatMapRepository extends JpaRepository<UserChatMapEntity, Long> {
    List<UserChatMapEntity> findAllByChatId(Long groupId);
    List<UserChatMapEntity> findAllByUserId(Long userId);
    void deleteByUserIdAndChatId(Long userId,Long groupId);
    Long countByChatId(Long groupId);
    Boolean existsByUserIdAndChatId(Long userId, Long chatId);
}
