package com.vky.service;

import com.vky.rabbitmq.RabbitMQProducer;
import com.vky.repository.entity.UserChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnreadMessageCountService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserChatSettingsService userChatSettingsService;
    private final RabbitMQProducer rabbitMQProducer;

    public int incrementUnreadCount(String chatRoomId, String recipientId) {
        String redisKey = generateUnreadKey(chatRoomId, recipientId);

        // Redis'teki sayacı artır
        redisTemplate.opsForValue().increment(redisKey, 1);

        // Yeni unread count'u al
        int currentCount = getUnreadCount(chatRoomId, recipientId);

        rabbitMQProducer.updateUnreadCountToMongo(chatRoomId, recipientId, currentCount);
        return currentCount;
    }

    public void resetUnreadCount(String chatRoomId, String recipientId) {
        String redisKey = generateUnreadKey(chatRoomId, recipientId);

        // Redis'teki sayacı sıfırla
        redisTemplate.opsForValue().set(redisKey, 0);

        rabbitMQProducer.updateUnreadCountToMongo(chatRoomId, recipientId, 0);
    }

    public Integer getUnreadCount(String chatRoomId, String recipientId) {
        String redisKey = generateUnreadKey(chatRoomId, recipientId);
        Object value = redisTemplate.opsForValue().get(redisKey);

        // Redis'teki veri yoksa MongoDB'den al ve Redis'e set et
        if (value == null) {
            Optional<UserChatSettings> optionalUserChatSettings = userChatSettingsService.findUserChatSettingsByChatRoomIdAndUserId(chatRoomId, recipientId);
            int unreadCount = optionalUserChatSettings.map(UserChatSettings::getUnreadMessageCount).orElse(0);

            // Redis'e set
            redisTemplate.opsForValue().set(redisKey, unreadCount);
            return unreadCount;
        }
        return (Integer) value;
    }

    private String generateUnreadKey(String chatRoomId, String recipientId) {
        return String.format("unread:%s:%s", chatRoomId, recipientId);
    }
}



