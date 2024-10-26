package com.example.social_media_app_rtcomm.controller;

import com.example.social_media_app_rtcomm.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/chat")
@AllArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/create-chat-after-accept-friend")
    public void createChatForUsersAfterAcceptFriend(@RequestParam Long receiverId,
                                                    @RequestParam Long senderId){
        chatService.createChatForUsersAfterAcceptFriend(receiverId, senderId);
    }

}
