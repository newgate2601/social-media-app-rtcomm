package com.example.social_media_app_rtcomm.repository;

import com.example.social_media_app_rtcomm.entity.EventNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EventNotificationRepository extends JpaRepository<EventNotificationEntity, Long> {
    List<EventNotificationEntity> findAllByUserIdAndState(Long userId, String state);
    List<EventNotificationEntity> findAllByUserId(Long userId);
    List<EventNotificationEntity> findAllByIdIn(Collection<Long> ids);
    List<EventNotificationEntity> findAllByUserIdAndEventType(Long userId, String eventType);
    void deleteAllByUserIdAndChatId(Long userId, Long chatId);
    void deleteAllByUserIdAndEventType(Long userId, String eventType);
}
