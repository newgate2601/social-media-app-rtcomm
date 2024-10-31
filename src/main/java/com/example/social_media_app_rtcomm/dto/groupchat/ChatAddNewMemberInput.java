package com.example.social_media_app_rtcomm.dto.groupchat;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatAddNewMemberInput {
    private Long groupChatId;
    private List<Long> userIds;
}
