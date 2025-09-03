package com.vky.config;

import com.vky.exception.ContactsServiceException;
import com.vky.exception.ErrorMessage;
import com.vky.exception.ErrorType;
import com.vky.utility.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Lazy
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    WebSocketInterceptor(JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                handleConnect(accessor);
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                handleDisconnect(accessor);
            }
        } catch (Exception e) {
            cleanupOnError(accessor);
            throw e;
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
        token = token.substring(7);

        if (!jwtTokenProvider.isValidToken(token)) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        String userId = jwtTokenProvider.extractAuthId(token).toString();
        String sessionId = accessor.getSessionId();

        String redisKey = "contacts-user:" + userId;

        // Eski session varsa disconnect et
        Object oldSessionId = redisTemplate.opsForValue().get(redisKey);
        if (oldSessionId != null && !oldSessionId.equals(sessionId)) {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/disconnect",
                    "You have been disconnected due to login from another device"
            );
        }

        // Yeni session'u Redis'e kaydet
        redisTemplate.opsForValue().set(redisKey, sessionId);
        accessor.setUser(() -> userId);
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        String redisKey = "contacts-user:" + userId;

        // Session Redis’ten sil
        redisTemplate.delete(redisKey);
    }

    private void cleanupOnError(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        redisTemplate.delete("contacts-user:" + userId);
    }
}
