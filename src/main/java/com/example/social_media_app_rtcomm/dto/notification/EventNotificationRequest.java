package com.example.social_media_app_rtcomm.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventNotificationRequest {
    private Long userId;
    private String eventType;
}
