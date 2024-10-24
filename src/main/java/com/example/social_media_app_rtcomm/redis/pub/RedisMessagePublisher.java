package com.example.social_media_app_rtcomm.redis.pub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisMessagePublisher {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisMessagePublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // user_id = channel_name
    public void publish(String channelName, String message) {
        redisTemplate.convertAndSend(channelName, message);
        log.error("Publish message to channel: " + channelName + " successful !!!");
    }
}
