package com.vky.config;

import com.vky.exception.ErrorType;
import com.vky.service.ContactsWebSocketService;
import com.vky.service.UserRelationshipService;
import com.vky.utility.JwtTokenProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRelationshipService userRelationshipService;

    WebSocketInterceptor(JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate, UserRelationshipService userRelationshipService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.userRelationshipService = userRelationshipService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        try {
            switch (accessor.getCommand()) {
                case CONNECT -> handleConnect(accessor, message);
                case DISCONNECT -> handleDisconnect(accessor);
                case SUBSCRIBE, SEND -> validateActiveSession(accessor);
            }
        } catch (Exception e) {
            cleanupOnError(accessor);
            throw e;
        }

        return message;
    }

    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> originalMessage) {

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return buildErrorFrame(
                    String.valueOf(ErrorType.UNAUTHORIZED_ACCESS.getCode()),
                    "Invalid JWT token",
                    accessor
            );
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.isValidToken(token)) {
            return buildErrorFrame(
                    "EXPIRED_TOKEN",
                    "JWT token expired or invalid",
                    accessor
            );
        }

        String userId = jwtTokenProvider.extractAuthId(token).toString();
        UUID userUuid = UUID.fromString(userId);
        String sessionId = accessor.getSessionId();
        String redisKey = "contacts-user:" + userId;

        Object oldSessionId = redisTemplate.opsForHash().get(redisKey, "sessionId");

        if (oldSessionId != null && !oldSessionId.equals(sessionId)) {
            userRelationshipService.disconnectMessage(userId);
        }

        // REDIS UPDATE
        redisTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
        redisTemplate.opsForHash().put(redisKey, "status", "online");
        redisTemplate.opsForHash().put(redisKey, "lastSeen", Instant.now().toString());
        redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);

        userRelationshipService.userStatusMessage(userUuid, "online");

        accessor.setUser(() -> userId);

        return originalMessage;
    }

    private void validateActiveSession(StompHeaderAccessor accessor) {

        if (accessor.getUser() == null) {
            throw new IllegalStateException("No active session");
        }

        String userId = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();
        String redisKey = "contacts-user:" + userId;

        String activeSession = (String) redisTemplate.opsForHash().get(redisKey, "sessionId");

        if (activeSession == null) {

            redisTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
            redisTemplate.opsForHash().put(redisKey, "status", "online");
            redisTemplate.opsForHash().put(redisKey, "lastSeen", Instant.now().toString());
            redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);

            return;
        }

        if (!sessionId.equals(activeSession)) {
            throw new IllegalStateException("Session is no longer active");
        }

        redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);
    }


    private void handleDisconnect(StompHeaderAccessor accessor) {

        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();
        String redisKey = "contacts-user:" + userId;

        String activeSession = (String) redisTemplate.opsForHash().get(redisKey, "sessionId");

        if (sessionId.equals(activeSession)) {
            redisTemplate.opsForHash().put(redisKey, "status", "offline");
            redisTemplate.opsForHash().put(redisKey, "lastSeen", Instant.now().toString());
            redisTemplate.opsForHash().delete(redisKey, "sessionId");
        }
    }
    private void cleanupOnError(StompHeaderAccessor accessor) {

        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        redisTemplate.delete("contacts-user:" + userId);
    }

    private Message<byte[]> buildErrorFrame(String code, String body, StompHeaderAccessor originalAccessor) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setSessionId(originalAccessor.getSessionId());
        errorAccessor.setMessage(code);

        byte[] payload = body != null
                ? body.getBytes(StandardCharsets.UTF_8)
                : new byte[0];

        return MessageBuilder.createMessage(payload, errorAccessor.getMessageHeaders());
    }
}
