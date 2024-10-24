package com.example.social_media_app_rtcomm.redis.sub.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class RedisDynamicSubscriber {
    private final RedisMessageListenerContainer redisContainer;
    private final MessageListenerAdapter listenerAdapter;
    private final Map<String, Boolean> userSubscribed = new HashMap<>();
    // if login success
    // case1: not subscribe previous -> subscribe
    public void subscribeToChannel(String channelName) {
        redisContainer.addMessageListener(listenerAdapter, new ChannelTopic(channelName));
        log.error("Subscribe with channel: " + channelName + " successful !!!");
    }

    public void subscribeToChannelAfterWSConnect(String channelName) {
        if (userSubscribed.containsKey(channelName)) {
            log.error("user subcribed, cannot re-subcribe with channel: " + channelName);
            return;
        }
        redisContainer.addMessageListener(listenerAdapter, new ChannelTopic(channelName));
        userSubscribed.put(channelName, true);
        log.error("Subscribe with channel: " + channelName + " successful !!!");
    }

    public void unsubscribeFromChannel(String channelName) {
        if (!userSubscribed.containsKey(channelName)) {
            log.warn("Not subscribed to channel: " + channelName + " because don't have WS connection");
            return;
        }

        // Remove the message listener for the specific channel
        redisContainer.removeMessageListener(listenerAdapter, new ChannelTopic(channelName));
        userSubscribed.remove(channelName); // Remove from tracking

        log.info("Unsubscribed from channel: " + channelName + " successfully!!!");
    }
}
