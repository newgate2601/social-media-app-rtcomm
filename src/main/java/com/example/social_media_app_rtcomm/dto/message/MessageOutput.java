package com.example.social_media_app_rtcomm.dto.message;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageOutput {
    private String type;
    private OffsetDateTime createdAt;
    private String message;
    private Long chatId;
}
