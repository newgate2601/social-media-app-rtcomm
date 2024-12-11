package com.example.social_media_app_rtcomm.service;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.notification.EventNotificationRequest;
import com.example.social_media_app_rtcomm.dto.notification.EventNotificationResponse;
import com.example.social_media_app_rtcomm.entity.EventNotificationEntity;
import com.example.social_media_app_rtcomm.repository.EventNotificationRepository;
import com.example.social_media_app_rtcomm.security.TokenHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class EventNotificationService {
    private final EventNotificationRepository eventNotificationRepository;
    private final TokenHelper tokenHelper;

    @Transactional(readOnly = true)
    public EventNotificationResponse getNotificationCount(String accessToken){
        Long userId = tokenHelper.getUserIdFromToken(accessToken);
        EventNotificationResponse eventNotificationResponse = new EventNotificationResponse();
        List<EventNotificationEntity> messageEventEntities
                = eventNotificationRepository.findAllByUserIdAndEventType(userId, Common.MESSAGE);
        if (Objects.isNull(messageEventEntities) || messageEventEntities.isEmpty()){
            eventNotificationResponse.setMessageCount(0);
        }
        else {
            Set<Long> messageEventCount = new HashSet<>();
            for (EventNotificationEntity eventNotificationEntity : messageEventEntities){
                messageEventCount.add(eventNotificationEntity.getChatId());
            }
            eventNotificationResponse.setMessageCount(messageEventCount.size());
        }

        List<EventNotificationEntity> notificationEventEntities
                = eventNotificationRepository.findAllByUserIdAndEventType(userId, Common.NOTIFICATION);
        if (Objects.isNull(notificationEventEntities) || notificationEventEntities.isEmpty()){
            eventNotificationResponse.setNotificationCount(0);
        }
        else {
            eventNotificationResponse.setNotificationCount(notificationEventEntities.size());
        }
        return eventNotificationResponse;
    }

    @Transactional
    public void deleteAllEventNotificationByType(Long userId, String type){
        eventNotificationRepository.deleteAllByUserIdAndEventType(userId, type);
    }

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
