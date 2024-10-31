package com.example.social_media_app_rtcomm.dto.groupchat;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupChatInput {
    @NotEmpty
    private String name;
    @NotEmpty
    private List<Long> userIds;
}
