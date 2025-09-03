package com.vky.config;

import com.vky.expcetion.ChatServiceException;
import com.vky.expcetion.ErrorMessage;
import com.vky.expcetion.ErrorType;
import com.vky.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
            } else if (StompCommand.SEND.equals(accessor.getCommand()) ||
                    StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                validateActiveSession(accessor);
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                handleDisconnect(accessor);
            }
        } catch (Exception e) {
            handleErrorAndCleanup(accessor, e);
            throw e;
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ChatServiceException(ErrorType.UNAUTHORIZED_ACCESS, "Invalid JWT token");
        }
        token = token.substring(7);
//        if (!"test-error".equals(token)) {
//            throw new ChatServiceException(ErrorType.UNAUTHORIZED_ACCESS, "Manual test error");
//        }
        if (!jwtTokenProvider.isValidToken(token)) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        String userId = jwtTokenProvider.extractAuthId(token).toString();
        String sessionId = accessor.getSessionId();

        // Tek session kontrolü
        String redisKey = "chat-user:" + userId;
        Object oldSessionId = redisTemplate.opsForHash().get(redisKey, "sessionId");
        if (oldSessionId != null && !oldSessionId.equals(sessionId)) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/disconnect", "Another device logged in");
        }

        // Redis'e güncelleme
        redisTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
        redisTemplate.opsForHash().put(redisKey, "status", "online");
        redisTemplate.opsForHash().put(redisKey, "lastSeen", Instant.now().toString());

        // TTL ayarla (örn. 30 saniye, düzenli heartbeat ile yenilenebilir)
        redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);

        accessor.setUser(userId::toString);
    }

    private void validateActiveSession(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new ChatServiceException(ErrorType.USER_CHAT_SETTINGS_NOT_FOUND);

        }

        String userId = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();
        String redisKey = "chat-user:" + userId;

        String activeSession = (String) redisTemplate.opsForHash().get(redisKey, "sessionId");

        if (activeSession == null) {
            redisTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
            redisTemplate.opsForHash().put(redisKey, "status", "online");
            redisTemplate.opsForHash().put(redisKey, "lastSeen", Instant.now().toString());

            redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);

            return;
        }

        if (!sessionId.equals(activeSession)) {
            throw new IllegalStateException("This session is no longer active");
        }

        redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();
        String activeSession = (String) redisTemplate.opsForHash().get("user:" + userId, "sessionId");

        if (sessionId.equals(activeSession)) {
            // Kullanıcı offline yap
            redisTemplate.opsForHash().put("chat-user:" + userId, "status", "offline");
            redisTemplate.opsForHash().put("chat-user:" + userId, "lastSeen", Instant.now().toString());
            redisTemplate.opsForHash().delete("chat-user:" + userId, "sessionId");
        }
    }

    private void handleErrorAndCleanup(StompHeaderAccessor accessor, Exception e) {
        if (accessor.getUser() != null) {
            String userId = accessor.getUser().getName();

            // Redis temizliği
            redisTemplate.delete("chat-user:" + userId);

            ErrorMessage errorPayload; // Hata mesajını burada tanımla

            if (e instanceof ChatServiceException cse) {
                errorPayload = ErrorMessage.builder()
                        .code(cse.getErrorType().getCode())
                        .message(cse.getMessage())
                        .fields(Collections.emptyList())
                        .build();
            } else {
                errorPayload = ErrorMessage.builder()
                        .code(ErrorType.INTERNAL_ERROR.getCode())
                        .message("Unexpected error: " + e.getMessage())
                        .fields(Collections.emptyList())
                        .build();
            }

            try {
                messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/error",
                        errorPayload
                );
            } catch (Exception ignored) {}
        }
    }
}