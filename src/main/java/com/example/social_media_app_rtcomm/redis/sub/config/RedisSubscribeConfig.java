package com.example.social_media_app_rtcomm.redis.sub.config;

import com.example.social_media_app_rtcomm.redis.sub.RedisMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisSubscribeConfig {
    //use PUBLISH default-channel "your-message"
    //get all channel: PUBSUB CHANNELS
    // https://chatgpt.com/c/6719792a-c634-8010-9825-60e5c4e9e1d5
    // redis delete channel if no subscriber: https://github.com/redis/redis/issues/12617
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.addMessageListener(listenerAdapter, new ChannelTopic("default-channel"));
//        container.addMessageListener(listenerAdapter, new ChannelTopic("default-channel1"));
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }
}
