package com.vky.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.vky.WsAuthException;
import com.vky.dto.AuthSession;
import com.vky.security.JwtTokenProvider;
import com.vky.service.UserStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
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
            throw new WsAuthException("INVALID_TOKEN");
        }

        String token = authHeader.substring(7);

        final AuthSession session;
        try {
            session = jwtTokenProvider.extractSession(token);
        } catch (TokenExpiredException e) {
            throw new WsAuthException("TOKEN_EXPIRED");
        } catch (JWTVerificationException e) {
            throw new WsAuthException("INVALID_TOKEN");
        }

        String userId = session.userId();
        String jti = session.jti();

        String activeJtiKey = "auth:active:" + userId;
        String activeJti = (String) redisTemplate.opsForValue().get(activeJtiKey);

        if (activeJti == null || !activeJti.equals(jti)) {
            throw new WsAuthException("INVALID_SESSION");
        }

        accessor.setUser(() -> userId);

        String sessionKey = "session:" + userId;
        String oldSessionId = (String) redisTemplate.opsForValue().get(sessionKey);

        String newSessionId = accessor.getSessionId();
        if (oldSessionId != null && !oldSessionId.equals(newSessionId)) {
            eventPublisher.publishEvent(new WsKickEvent(userId, oldSessionId, "NEW_LOGIN"));
        }

        redisTemplate.opsForValue().set(sessionKey, newSessionId, Duration.ofSeconds(15));

        eventPublisher.publishEvent(new UserStatusEvent(userId, "online", null, false));
        return message;
    }

    public record WsKickEvent(String userId, String sessionId, String reason) {}
    private Message<?> onActivity(StompHeaderAccessor accessor, Message<?> message) {
        if (accessor.getUser() == null) return message;

        String userId = accessor.getUser().getName();

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                jwtTokenProvider.validateAndGet(token);
            } catch (TokenExpiredException e) {
                throw new WsAuthException("TOKEN_EXPIRED");
            } catch (JWTVerificationException e) {
                throw new WsAuthException("INVALID_TOKEN");
            }
        }
        String key = "session:" + userId;
        boolean wasOnline = Boolean.TRUE.equals(redisTemplate.hasKey(key));

        redisTemplate.opsForValue().set(key, accessor.getSessionId(), Duration.ofSeconds(15));

        if (!wasOnline) {
            eventPublisher.publishEvent(new UserStatusEvent(userId, "online", null, false));
        }

        return message;
    }
}




