package com.vky.config;

import com.vky.security.JwtTokenProvider;
import com.vky.service.UserStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null)
            return message;

        return switch (accessor.getCommand()) {
            case CONNECT -> onConnect(accessor, message);
            case DISCONNECT -> {
//                onDisconnect(accessor);
                yield message;
            }
            case SEND, SUBSCRIBE -> onActivity(accessor, message);
            default -> message;
        };
    }

    private Message<?> onConnect(StompHeaderAccessor accessor, Message<?> message) {

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return error("INVALID_TOKEN", accessor);
        }

        String token = authHeader.substring(7);
        String userId = jwtTokenProvider.extractAuthId(token).toString();

        accessor.setUser(() -> userId);

        redisTemplate.opsForValue().set(
                "session:" + userId,
                accessor.getSessionId(),
                Duration.ofSeconds(15)
        );

        eventPublisher.publishEvent(
                new UserStatusEvent(userId, "online", null, false)
        );

        return message;
    }

    private Message<?> onActivity(StompHeaderAccessor accessor, Message<?> message) {
        if (accessor.getUser() == null) return message;

        String userId = accessor.getUser().getName();
        String key = "session:" + userId;

        Boolean existed = redisTemplate.hasKey(key);

        redisTemplate.opsForValue().set(
                key,
                accessor.getSessionId(),
                Duration.ofSeconds(15)
        );

        if (!existed) {
            eventPublisher.publishEvent(
                    new UserStatusEvent(userId, "online", null, false)
            );
        }

        return message;
    }

//    private void onDisconnect(StompHeaderAccessor accessor) {
//        if (accessor.getUser() == null) return;
//
//        String userId = accessor.getUser().getName();
//
//        redisTemplate.delete("session:" + userId);
//
//        eventPublisher.publishEvent(
//                new UserStatusEvent(
//                        userId,
//                        "offline",
//                        Instant.now(),
//                        false
//                )
//        );
//    }

    private Message<?> error(String code, StompHeaderAccessor accessor) {
        StompHeaderAccessor errorAcc =
                StompHeaderAccessor.create(StompCommand.ERROR);
        errorAcc.setSessionId(accessor.getSessionId());
        errorAcc.setMessage(code);
        return MessageBuilder.createMessage(
                new byte[0],
                errorAcc.getMessageHeaders()
        );
    }
}




