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
    @NonNull
    private Long chatId;
    @NotBlank
    private String message;
    private String receiverId;
}
