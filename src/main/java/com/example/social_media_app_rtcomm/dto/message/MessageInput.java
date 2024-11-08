package com.example.social_media_app_rtcomm.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageInput implements Serializable {
    private Long chatId;
    private String message;
    private String receiverId;
    private String fullName;
    private String imageUrl;
    private Long userId; // sender
    private String accessToken;
    private String type;
}
