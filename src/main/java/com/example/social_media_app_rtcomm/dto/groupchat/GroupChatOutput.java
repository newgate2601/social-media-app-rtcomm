package com.example.social_media_app_rtcomm.dto.groupchat;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GroupChatOutput {
    private Long id;
    private String name;
    private String img;
}
