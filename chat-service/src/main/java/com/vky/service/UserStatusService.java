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

        UserStatusMessage message = new UserStatusMessage();
        // Redis üzerinden user:{id} hash’ini oku
        Map<Object, Object> userHash = redisTemplate.opsForHash().entries("chat-user:" + contactId);

        if (userHash != null && "online".equals(userHash.get("status"))) {
            // Kullanıcı online
            message.setUserId(contactId);
            message.setStatus("online");
            message.setLastSeen(null);
        } else {
            // Kullanıcı offline, lastSeen’i al
            Instant lastSeen = null;

            if (userHash != null && userHash.get("lastSeen") != null) {
                lastSeen = Instant.parse(userHash.get("lastSeen").toString());
            } else {
                // Redis’te yoksa DB’den çek
                UUID userIdUUID = UUID.fromString(contactId);
                UserLastSeenResponseDTO userLastSeenResponseDTO = userManager.getUserLastSeen(userIdUUID);
                lastSeen = userLastSeenResponseDTO.getLastSeen();
            }

            message.setUserId(contactId);
            message.setStatus("offline");
            message.setLastSeen(lastSeen);
        }
        return message;
    }

    public void ping(String userId, long ttlSeconds) {
        String key = "chat-user:" + userId;

        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    public void isTyping(TypingMessage message) {

        messagingTemplate.convertAndSendToUser(message.getFriendId(), "/queue/typing", message);
        messagingTemplate.convertAndSendToUser(message.getFriendId(), "/queue/message-box-typing", message);
    }
}
