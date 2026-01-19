package com.vky.service;

import com.vky.dto.request.UnreadMessageCountDTO;
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

    private static final Duration REDIS_TTL = Duration.ofHours(24);

    public int incrementUnreadCount(UnreadMessageCountDTO dto) {

        String key = generateUnreadKey(dto.chatRoomId(), dto.recipientId());

        Integer current = (Integer) redisTemplate.opsForValue().get(key);
        if (current == null) {
            current = loadUnreadFromMongo(dto.chatRoomId(), dto.recipientId());
        }

        // Redis'te arttır
        int updated = redisTemplate.opsForValue().increment(key, 1).intValue();

        // Mongo'da unread güncelle (senkron, basit çözüm)
        userChatSettingsService.setUnreadCount(
                dto.chatRoomId(),
                dto.recipientId(),
                updated);

        return updated;
    }

    public int resetUnreadCount(String chatRoomId, String recipientId) {

        String key = generateUnreadKey(chatRoomId, recipientId);

        Integer unread = (Integer) redisTemplate.opsForValue().get(key);
        if (unread == null) {
            unread = loadUnreadFromMongo(chatRoomId, recipientId);
        }

        // Redis sıfırla (TTL'ı da yenile)
        redisTemplate.opsForValue().set(key, 0, REDIS_TTL);

        // Mongo sıfırla
        userChatSettingsService.resetUnread(chatRoomId, recipientId);

        return unread;
    }

    private int loadUnreadFromMongo(String chatRoomId, String userId) {

        Optional<UserChatSettings> settings = userChatSettingsService
                .findUserChatSettingsByChatRoomIdAndUserId(chatRoomId, userId);

        int unread = settings.map(UserChatSettings::getUnreadMessageCount).orElse(0);

        String redisKey = generateUnreadKey(chatRoomId, userId);
        redisTemplate.opsForValue().set(redisKey, unread, REDIS_TTL);

        return unread;
    }

    public int getUnreadCount(String chatRoomId, String userId) {
        String key = generateUnreadKey(chatRoomId, userId);

        Integer unread = (Integer) redisTemplate.opsForValue().get(key);

        if (unread == null) {
            unread = loadUnreadFromMongo(chatRoomId, userId);
        }

        return unread;
    }

    public String generateUnreadKey(String chatRoomId, String recipientId) {
        return String.format("unread:%s:%s", chatRoomId, recipientId);
    }

    public void deleteUnreadMessageCount(String chatRoomId, String userId) {
        String redisKey = generateUnreadKey(chatRoomId, userId);
        redisTemplate.delete(redisKey);
    }
}
