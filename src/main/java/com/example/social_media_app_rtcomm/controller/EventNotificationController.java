package com.example.social_media_app_rtcomm.controller;

import com.example.social_media_app_rtcomm.common.Common;
import com.example.social_media_app_rtcomm.dto.notification.EventNotificationRequest;
import com.example.social_media_app_rtcomm.dto.notification.EventNotificationResponse;
import com.example.social_media_app_rtcomm.service.EventNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/event-notification")
@AllArgsConstructor
public class EventNotificationController {
    private final EventNotificationService eventNotificationService;

    @GetMapping
    @Operation(summary = "Hiển thị chấm đỏ thông báo sau khi quay trở lại web app")
    public EventNotificationResponse getNotificationCount(@RequestHeader(Common.AUTHORIZATION) String accessToken){
        return eventNotificationService.getNotificationCount(accessToken);
    }

    @DeleteMapping
    @Operation(summary = "Xóa event notification theo type")
    public void deleteAllEventNotificationByType(
            @RequestParam Long userId,
            @RequestParam String type){
        eventNotificationService.deleteAllEventNotificationByType(userId, type);
    }

    @PostMapping
    @Operation(summary = "Tạo mới event notification")
    public void createEventNotification(@RequestBody EventNotificationRequest eventNotificationRequest){
        eventNotificationService.createEventNotification(eventNotificationRequest);
    }
}
