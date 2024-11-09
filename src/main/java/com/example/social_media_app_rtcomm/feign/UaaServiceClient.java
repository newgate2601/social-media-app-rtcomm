package com.example.social_media_app_rtcomm.feign;

import com.example.social_media_app_rtcomm.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

//@FeignClient("UAA-SERVICE")
@FeignClient(url = "localhost:8085", name = "UAA-SERVICE")
public interface UaaServiceClient {

    @GetMapping(value = "/api/v1/user/tiny/list", produces = "application/json")
    List<UserDto> getUsersBy(@RequestParam List<Long> ids);
}
