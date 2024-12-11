package com.example.social_media_app_rtcomm.dto.notification;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventNotificationResponse {
    private Integer notificationCount;
    private Integer messageCount;
}
