package com.vky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener implements MessageListener {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String key = message.toString();

        if (!key.startsWith("session:")) return;

        String userId = key.substring("session:".length());

        eventPublisher.publishEvent(
                new UserStatusEvent(
                        userId,
                        "offline",
                        Instant.now(),
                        false
                )
        );
    }
}
