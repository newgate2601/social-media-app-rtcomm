package com.example.social_media_app_rtcomm.dto.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageOutputList {
    private Long id;
    private Long userId;
    private String message;
    private String fullName;
    private String imageUrl;
    private Boolean isMe;
    private LocalDateTime createdAt;
}
