package com.vky.service;

import com.vky.controller.TypingMessage;
import com.vky.controller.UserStatusMessage;
import com.vky.dto.response.UserLastSeenResponseDTO;
import com.vky.manager.IUserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserStatusService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IUserManager userManager;
    public void updateStatus(String userId, String status, long ttlSeconds) {
        Instant now = Instant.now();
        String key = "chat-user:" + userId;
        redisTemplate.opsForHash().put(key, "status", status);
        redisTemplate.opsForHash().put(key, "lastSeen", now.toString());
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);

        UserStatusMessage message = UserStatusMessage.builder()
                .userId(userId)
                .status(status)
                .lastSeen(now)
                .build();

        messagingTemplate.convertAndSendToUser(userId, "/queue/online-status", message);
    }
    public UserStatusMessage isOnline(String contactId) {
        UserStatusMessage message;
        Map<Object, Object> userHash = redisTemplate.opsForHash().entries("chat-user:" + contactId);

        if ("online".equals(userHash.get("status"))) {
            message = buildUserStatusMessage(contactId,"online",null);
        } else {
            Instant lastSeen = null;

            if (userHash.get("lastSeen") != null) {
                lastSeen = Instant.parse(userHash.get("lastSeen").toString());
            } else {
                UUID userIdUUID = UUID.fromString(contactId);
                UserLastSeenResponseDTO userLastSeenResponseDTO = userManager.getUserLastSeen(userIdUUID);
                lastSeen = userLastSeenResponseDTO.getLastSeen();
            }
            message = buildUserStatusMessage(contactId,"offline",lastSeen);
        }
        return message;
    }

    public void ping(String userId, long ttlSeconds) {
        String key = "chat-user:" + userId;

        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    public void setTyping(TypingMessage message) {
        String key = "typing:" + message.getUserId();
        redisTemplate.opsForHash().put(key, "isTyping", message.isTyping());
        redisTemplate.opsForHash().put(key, "chatRoomId", message.getChatRoomId());
        redisTemplate.opsForHash().put(key, "contactId", message.getFriendId());
        redisTemplate.expire(key, 10, TimeUnit.SECONDS);

        messagingTemplate.convertAndSendToUser(message.getFriendId(), "/queue/typing", message);
        messagingTemplate.convertAndSendToUser(message.getFriendId(), "/queue/message-box-typing", message);
    }
    private UserStatusMessage buildUserStatusMessage(String contactId, String status, Instant lastSeen) {
        return UserStatusMessage.builder()
                .userId(contactId)
                .status(status)
                .lastSeen(lastSeen)
                .build();
    }

    public TypingMessage isTyping(String contactId, String userId) {
        String key = "typing:" + contactId;
        Map<Object, Object> typingHash = redisTemplate.opsForHash().entries(key);

        if (typingHash.isEmpty()) {
            return buildTypingMessage(userId, contactId, null, false);
        }

        String storedContactId = (String) typingHash.get("contactId");
        if (storedContactId == null || !storedContactId.equals(userId)) {
            return buildTypingMessage(userId, contactId, null, false);
        }

        boolean isTyping = Boolean.parseBoolean(
                String.valueOf(typingHash.getOrDefault("isTyping", "false"))
        );

        String chatRoomId = typingHash.get("chatRoomId") != null
                ? typingHash.get("chatRoomId").toString()
                : null;

        return buildTypingMessage(userId, storedContactId, chatRoomId, isTyping);
    }

    private TypingMessage buildTypingMessage(String userId, String friendId, String chatRoomId, boolean isTyping) {
        return TypingMessage.builder()
                .userId(userId)
                .friendId(friendId)
                .chatRoomId(chatRoomId)
                .isTyping(isTyping)
                .build();
    }
}
