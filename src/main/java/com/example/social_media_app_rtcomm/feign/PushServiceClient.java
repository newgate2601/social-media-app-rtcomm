package com.example.social_media_app_rtcomm.feign;

import com.example.social_media_app_rtcomm.dto.message.MessageOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("WEB-PUSH-SERVICE")
//@FeignClient(url = "localhost:8089", name = "WEB-PUSH-SERVICE")
public interface PushServiceClient {
    @PostMapping(value = "/api/webpush/push-message", produces = "application/json")
    void sendMessageToAllDevices(@RequestParam Long userId,
                                 @RequestBody MessageOutput pushMessage);
}
