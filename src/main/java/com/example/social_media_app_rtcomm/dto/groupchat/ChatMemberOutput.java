package com.example.social_media_app_rtcomm.dto.groupchat;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatMemberOutput {
    private Long id;
    private String fullName;
    private String imageUrl;
    private String role;
}
