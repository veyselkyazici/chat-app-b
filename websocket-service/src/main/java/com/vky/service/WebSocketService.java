package com.vky.service;

import com.vky.dto.MessageRequestDTO;
import com.vky.dto.TypingMessage;
import com.vky.dto.UnreadMessageCountDTO;
import com.vky.rabbitmq.RabbitMQProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitMQProducer rabbitMQProducer;

    public void sendMessage(MessageRequestDTO dto) {
        rabbitMQProducer.sendToChatIncoming(dto);
    }

    public void readMessage(UnreadMessageCountDTO dto) {
        rabbitMQProducer.sendReadEvent(dto);
    }

    public void setTyping(TypingMessage msg) {
        String key = "typing:" + msg.userId();

        redisTemplate.opsForHash().put(key, "isTyping", msg.isTyping());
        redisTemplate.opsForHash().put(key, "chatRoomId", msg.chatRoomId());
        redisTemplate.opsForHash().put(key, "contactId", msg.friendId());
        redisTemplate.expire(key, 10, TimeUnit.SECONDS);

        messagingTemplate.convertAndSendToUser(msg.friendId(), "/queue/typing", msg);
        messagingTemplate.convertAndSendToUser(msg.friendId(), "/queue/message-box-typing", msg);
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
                String.valueOf(typingHash.getOrDefault("isTyping", "false")));

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

    public void ping(String redisKey) {
        redisTemplate.expire(redisKey, Duration.ofSeconds(15));
    }

    // public void sendUpdatedPrivacy(UpdateSettingsRequestDTO dto) {
    // rabbitMQProducer.publishPrivacyToContacts(dto);
    // }
    //
    // public void sendUpdatedProfile(UpdatedProfilePhotoRequestDTO dto) {
    // rabbitMQProducer.publishProfileToContacts(dto);
    // }
}
