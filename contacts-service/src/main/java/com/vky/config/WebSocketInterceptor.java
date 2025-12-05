package com.vky.config;

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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;


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

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null)
            return message;

        try {

            switch (accessor.getCommand()) {
                case CONNECT -> {
                    return handleConnect(accessor, message);
                }
                case DISCONNECT -> {
                    handleDisconnect(accessor);
                }
            }

            return message;

        } catch (Exception e) {
            cleanupOnError(accessor);
            return buildErrorFrame("INTERNAL_ERROR",
                    "Unexpected error: " + e.getMessage(),
                    accessor);
        }
    }

    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> originalMessage) {

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            return buildErrorFrame(
                    "INVALID_TOKEN",
                    "Missing or invalid Authorization header",
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
        String sessionId = accessor.getSessionId();

        String redisKey = "contacts-user:" + userId;

        Object oldSessionId = redisTemplate.opsForValue().get(redisKey);
        if (oldSessionId != null && !oldSessionId.equals(sessionId)) {

            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/disconnect",
                    "Another device logged in"
            );
        }

        // Yeni session
        redisTemplate.opsForValue().set(redisKey, sessionId);

        accessor.setUser(() -> userId);

        return originalMessage;
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        String redisKey = "contacts-user:" + userId;

        redisTemplate.delete(redisKey);
    }

    private void cleanupOnError(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        redisTemplate.delete("contacts-user:" + userId);
    }

    private Message<byte[]> buildErrorFrame(String code, String body, StompHeaderAccessor originalAccessor) {

        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setSessionId(originalAccessor.getSessionId());
        errorAccessor.setMessage(code); // frontend → frame.headers.message ile bunu okuyacak

        byte[] payload = body != null
                ? body.getBytes(StandardCharsets.UTF_8)
                : new byte[0];

        return MessageBuilder.createMessage(payload, errorAccessor.getMessageHeaders());
    }
}
