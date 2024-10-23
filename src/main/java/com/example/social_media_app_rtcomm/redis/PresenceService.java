package com.example.social_media_app_rtcomm.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final String CACHE_NAME = "presence";

    @Autowired
    public PresenceService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void plus1ToSession(String userId) {
        Integer sessionAmount = get(userId);
        redisTemplate.opsForHash().put(CACHE_NAME, userId, sessionAmount + 1);
    }

    public void minus1ToSession(String userId) {
        Integer sessionAmount = get(userId);
        if (sessionAmount == 0) {
            sessionAmount++;
        }
        redisTemplate.opsForHash().put(CACHE_NAME, userId, sessionAmount - 1);
    }

    public Integer get(String userId) {
        Object amountSession = redisTemplate.opsForHash().get(CACHE_NAME, userId);
        if (amountSession == null) {
            return 0;
        }
        return (Integer) amountSession;
    }

    public void delete(String userId) {
        redisTemplate.opsForHash().delete(CACHE_NAME, userId);
    }
}
