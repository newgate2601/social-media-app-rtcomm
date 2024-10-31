package com.example.social_media_app_rtcomm.dto.groupchat;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatDeleteMemberInput {
    private Long groupChatId;
    private Long userId;
}
