package com.example.social_media_app_rtcomm.controller;

import com.example.social_media_app_rtcomm.dto.notification.EventNotificationRequest;
import com.example.social_media_app_rtcomm.service.EventNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/event-notification")
@AllArgsConstructor
@CrossOrigin
public class EventNotificationController {
    private final EventNotificationService eventNotificationService;

    @PostMapping
    @Operation(summary = "Tạo mới event notification")
    public void createEventNotification(@RequestBody EventNotificationRequest eventNotificationRequest){
        eventNotificationService.createEventNotification(eventNotificationRequest);
    }
}
