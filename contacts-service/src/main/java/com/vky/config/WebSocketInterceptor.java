package com.vky.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vky.dto.request.UpdateLastSeenRequestDTO;
import com.vky.manager.IUserManager;
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
    private final IUserManager userManager;

    private static final String REDIS_KEY_PREFIX = "contacts-user:";

    public WebSocketInterceptor(JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate, UserRelationshipService userRelationshipService, IUserManager userManager) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.userRelationshipService = userRelationshipService;
        this.userManager = userManager;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        try {
            StompCommand command = accessor.getCommand();

            return switch (command) {
                case CONNECT -> handleConnect(accessor, message);
                case DISCONNECT -> {
                    handleDisconnect(accessor);
                    yield message;
                }
                case SUBSCRIBE, SEND -> handleSubscribeOrSend(accessor, message);
                default -> message;
            };

        } catch (TokenExpiredException e) {
            return buildErrorFrame("EXPIRED_TOKEN", "Token expired", accessor);
        } catch (JWTVerificationException e) {
            return buildErrorFrame("INVALID_TOKEN", "Invalid token", accessor);
        } catch (Exception e) {
            return buildErrorFrame("INTERNAL_ERROR", "Unexpected error", accessor);
        }
    }

    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> originalMessage) {

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return buildErrorFrame("401", "Missing or invalid Authorization header", accessor);
        }

        String token = authHeader.substring(7);

        DecodedJWT verifiedToken = jwtTokenProvider.validateAndGet(token);
        String userId = verifiedToken.getClaim("id").asString();

        if (userId == null) {
            return buildErrorFrame("INVALID_TOKEN", "Missing claim 'id'", accessor);
        }

        String sessionId = accessor.getSessionId();
        String redisKey = REDIS_KEY_PREFIX + userId;

        Object oldSession = redisTemplate.opsForHash().get(redisKey, "sessionId");

        if (oldSession != null && !oldSession.equals(sessionId)) {
            userRelationshipService.disconnectMessage(userId);
        }

        redisTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
        redisTemplate.opsForHash().put(redisKey, "status", "online");
        redisTemplate.expire(redisKey, 300, TimeUnit.SECONDS);

        userRelationshipService.userStatusMessage(UUID.fromString(userId), "online");

        accessor.setUser(() -> userId);

        return originalMessage;
    }


    private Message<?> handleSubscribeOrSend(StompHeaderAccessor accessor, Message<?> originalMessage) {

        if (accessor.getUser() == null) {
            return null;
        }

        String userId = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();
        String redisKey = REDIS_KEY_PREFIX + userId;

        String activeSession = (String) redisTemplate.opsForHash().get(redisKey, "sessionId");

        if (activeSession == null) {
            redisTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
            redisTemplate.opsForHash().put(redisKey, "status", "online");
            redisTemplate.opsForHash().put(redisKey, "lastSeen", Instant.now().toString());
            redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);
            return originalMessage;
        }

        if (!sessionId.equals(activeSession)) {
            return buildErrorFrame("INVALID_SESSION", "Session is no longer active", accessor);
        }

        redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);

        return originalMessage;
    }


    private void handleDisconnect(StompHeaderAccessor accessor) {

        if (accessor.getUser() == null) return;

        String userId = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();
        String redisKey = REDIS_KEY_PREFIX + userId;

        String activeSession = (String) redisTemplate.opsForHash().get(redisKey, "sessionId");

        if (sessionId.equals(activeSession)) {
            Instant now = Instant.now();

            redisTemplate.opsForHash().put(redisKey, "status", "offline");
            redisTemplate.opsForHash().put(redisKey, "lastSeen", now.toString());
            redisTemplate.opsForHash().delete(redisKey, "sessionId");

            userManager.updateLastSeen(new UpdateLastSeenRequestDTO(UUID.fromString(userId),Instant.now()));

            userRelationshipService.userStatusMessage(UUID.fromString(userId), "offline");
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
