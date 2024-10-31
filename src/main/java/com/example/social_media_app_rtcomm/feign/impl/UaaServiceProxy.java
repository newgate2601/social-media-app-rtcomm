package com.example.social_media_app_rtcomm.feign.impl;

import com.example.social_media_app_rtcomm.dto.UserDto;
import com.example.social_media_app_rtcomm.feign.UaaServiceClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@AllArgsConstructor
public class UaaServiceProxy {
    private final UaaServiceClient uaaServiceClient;

    public List<UserDto> getUsersBy(@RequestParam List<Long> ids){
        return uaaServiceClient.getUsersBy(ids);
    }
}
