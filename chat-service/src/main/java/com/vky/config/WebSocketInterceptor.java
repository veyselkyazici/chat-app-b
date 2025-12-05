package com.vky.config;

import com.vky.controller.UserStatusMessage;
import com.vky.expcetion.ChatServiceException;
import com.vky.expcetion.ErrorMessage;
import com.vky.expcetion.ErrorType;
import com.vky.service.UserStatusService;
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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
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

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        try {
            StompCommand command = accessor.getCommand();
            if (command == null) {
                return message;
            }

            if (StompCommand.CONNECT.equals(command)) {
                return handleConnect(accessor, message);

            } else if (StompCommand.SEND.equals(command) ||
                    StompCommand.SUBSCRIBE.equals(command)) {

                validateActiveSession(accessor);

            } else if (StompCommand.DISCONNECT.equals(command)) {

                handleDisconnect(accessor);
            }

            return message;

        } catch (ChatServiceException cse) {
            handleErrorAndCleanup(accessor, cse);
            return buildErrorFrame(
                    String.valueOf(cse.getErrorType().getCode()),
                    cse.getMessage(),
                    accessor
            );

        } catch (Exception e) {
            handleErrorAndCleanup(accessor, e);
            return buildErrorFrame(
                    String.valueOf(ErrorType.INTERNAL_ERROR.getCode()),
                    "Unexpected error: " + e.getMessage(),
                    accessor
            );
        }
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
                    "EXPIRED_TOKEN",              // frame.headers.message
                    "JWT token expired or invalid", // frame body
                    accessor
            );
        }

        String userId = jwtTokenProvider.extractAuthId(token).toString();
        String sessionId = accessor.getSessionId();
        String redisKey = "chat-user:" + userId;

        Object oldSessionId = redisTemplate.opsForHash().get(redisKey, "sessionId");
        if (oldSessionId != null && !oldSessionId.equals(sessionId)) {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/disconnect",
                    "Another device logged in"
            );
        }

        redisTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
        redisTemplate.opsForHash().put(redisKey, "status", "online");
        redisTemplate.opsForHash().put(redisKey, "lastSeen", Instant.now().toString());

        UserStatusMessage statusMessage = UserStatusMessage.builder()
                .userId(userId)
                .status("online")
                .lastSeen(Instant.now())
                .build();

        messagingTemplate.convertAndSendToUser(userId, "/queue/online-status", statusMessage);

        redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);

        accessor.setUser(userId::toString);

        return originalMessage;
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
            redisTemplate.opsForHash().put("chat-user:" + userId, "status", "offline");
            redisTemplate.opsForHash().put("chat-user:" + userId, "lastSeen", Instant.now().toString());
            redisTemplate.opsForHash().delete("chat-user:" + userId, "sessionId");
            redisTemplate.opsForHash().delete("typing:" + userId);
        }
    }

    private void handleErrorAndCleanup(StompHeaderAccessor accessor, Exception e) {
        if (accessor.getUser() != null) {
            String userId = accessor.getUser().getName();

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