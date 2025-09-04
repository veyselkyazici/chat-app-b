package com.vky.service;

import com.vky.rabbitmq.RabbitMQProducer;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnreadMessageCountService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserChatSettingsService userChatSettingsService;
    private final RabbitMQProducer rabbitMQProducer;
    private final ChatMessageService chatMessageService;
    private static final Duration REDIS_TTL = Duration.ofHours(24);

    public int incrementUnreadCount(String chatRoomId, String recipientId, String senderId) {
        String redisKey = generateUnreadKey(chatRoomId, recipientId);

        Integer currentCount = (Integer) redisTemplate.opsForValue().get(redisKey);
        if (currentCount == null) {
            currentCount = setUnreadCountFromMongo(chatRoomId, recipientId);
        }
        redisTemplate.opsForValue().increment(redisKey, 1);

        currentCount = getUnreadCount(chatRoomId, recipientId);

        rabbitMQProducer.updateUnreadCountToMongo(chatRoomId, recipientId, senderId, currentCount);
        return currentCount;
    }

    public int resetUnreadCount(String chatRoomId, String recipientId, String senderId) {
        String redisKey = generateUnreadKey(chatRoomId, recipientId);

        Integer currentCount = (Integer) redisTemplate.opsForValue().get(redisKey);
        if (currentCount == null) {

            currentCount = setUnreadCountFromMongo(chatRoomId, recipientId);
        }
        chatMessageService.setIsSeenUpdateForUnreadMessageCount(chatRoomId,recipientId,currentCount);
        redisTemplate.opsForValue().set(redisKey, 0, REDIS_TTL);

        return currentCount;
    }

    public Integer getUnreadCount(String chatRoomId, String recipientId) {
        String redisKey = generateUnreadKey(chatRoomId, recipientId);
        Object value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            return setUnreadCountFromMongo(chatRoomId, recipientId);
        }
        return (Integer) value;
    }

    private int setUnreadCountFromMongo(String chatRoomId, String recipientId) {
        String redisKey = generateUnreadKey(chatRoomId, recipientId);

        Optional<UserChatSettings> optionalUserChatSettings =
                userChatSettingsService.findUserChatSettingsByChatRoomIdAndUserId(chatRoomId, recipientId);
        int unreadCount = optionalUserChatSettings.map(UserChatSettings::getUnreadMessageCount).orElse(0);

        redisTemplate.opsForValue().set(redisKey, unreadCount, REDIS_TTL);
        return unreadCount;
    }

    public String generateUnreadKey(String chatRoomId, String recipientId) {
        return String.format("unread:%s:%s", chatRoomId, recipientId);
    }

    public void deleteUnreadMessageCount(String chatRoomId, String userId) {
        String redisKey = generateUnreadKey(chatRoomId, userId);
        redisTemplate.delete(redisKey);
    }
}



