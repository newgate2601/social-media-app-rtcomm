package com.example.social_media_app_rtcomm.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatForUserDto {
    private Long receiverId;
    private String receiverFullName;
    private String receiverImageUrl;
    private Long senderId;
    private String senderFullName;
    private String senderImageUrl;
}
