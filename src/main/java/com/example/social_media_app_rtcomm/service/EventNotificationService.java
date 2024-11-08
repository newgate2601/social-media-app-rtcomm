package com.example.social_media_app_rtcomm.service;

import com.example.social_media_app_rtcomm.dto.notification.EventNotificationRequest;
import com.example.social_media_app_rtcomm.entity.EventNotificationEntity;
import com.example.social_media_app_rtcomm.repository.EventNotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class EventNotificationService {
    private final EventNotificationRepository eventNotificationRepository;

    @Transactional
    public void createEventNotification(EventNotificationRequest eventNotificationRequest){
        eventNotificationRepository.save(
                EventNotificationEntity.builder()
                        .userId(eventNotificationRequest.getUserId())
                        .eventType(eventNotificationRequest.getEventType())
                        .build()
        );
        log.error("Success save event notification with userId = " + eventNotificationRequest.getUserId()
        + " and eventType = " + eventNotificationRequest.getEventType());
    }

}
