package com.vky.service;

import com.vky.controller.TypingMessage;
import com.vky.manager.IUserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserTypingStatusService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IUserManager userManager;


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
