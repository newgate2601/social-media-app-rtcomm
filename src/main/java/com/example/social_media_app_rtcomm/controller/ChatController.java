package com.example.social_media_app_rtcomm.controller;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.chat.CreateChatForUserDto;
import com.example.social_media_app_rtcomm.dto.message.MessageOutputList;
import com.example.social_media_app_rtcomm.entity.ChatOutput;
import com.example.social_media_app_rtcomm.service.ChatService;
import com.example.social_media_app_rtcomm.service.GetChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/chat")
@AllArgsConstructor
@CrossOrigin
public class ChatController {
    private final ChatService chatService;
    private final GetChatService getChatService;

    @Operation(summary = "Lấy danh sách tin nhắn trong cuộc trò chuyện")
    @GetMapping("/messages")
    public Page<MessageOutputList> getMessages(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                               @RequestParam Long chatId,
                                               @ParameterObject Pageable pageable){
        return getChatService.getMessages(accessToken, chatId, pageable);
    }

    @Operation(summary = "Lấy danh sách các cuộc chat của user")
    @GetMapping
    public Page<ChatOutput> getChatList(@RequestParam(required = false) String search,
                                        @RequestHeader(Common.AUTHORIZATION) String accessToken,
                                        @ParameterObject Pageable pageable){
        return getChatService.getChatList(search, accessToken, pageable);
    }

    @PostMapping("/create-chat-after-accept-friend")
    @Operation(summary = "Tạo mới cuộc trò chuyện sau khi kết bạn")
    public void createChatForUsersAfterAcceptFriend(@RequestBody CreateChatForUserDto createChatForUserDto){
        chatService.createChatForUsersAfterAcceptFriend(createChatForUserDto);
    }
}
