package com.vky.service;

import com.vky.dto.PrivacySettingsResponseDTO;
import com.vky.manager.IUserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PrivacyCache {

    private static final String KEY_PREFIX = "privacy:";
    private final RedisTemplate<String, PrivacySettingsResponseDTO> privacyRedisTemplate;
    private final IUserManager userManager;

    private final Duration ttl = Duration.ofDays(1);

    private String key(String userId) {
        return KEY_PREFIX + userId;
    }

    public void put(String userId, PrivacySettingsResponseDTO dto) {
        if (userId == null || dto == null) return;
        privacyRedisTemplate.opsForValue().set(key(userId), dto, ttl);
    }

    public PrivacySettingsResponseDTO get(String userId) {
        if (userId == null) return null;
        return privacyRedisTemplate.opsForValue().get(key(userId));
    }


    public PrivacySettingsResponseDTO getOrLoad(String userId) {
        if (userId == null) return null;

        String redisKey = key(userId);
        PrivacySettingsResponseDTO cached = privacyRedisTemplate.opsForValue().get(redisKey);
        if (cached != null) return cached;

        PrivacySettingsResponseDTO fetched = null;
        try {
            fetched = userManager.getPrivacy(userId);
        } catch (Exception e) {
            return null;
        }

        if (fetched != null) {
            privacyRedisTemplate.opsForValue().set(redisKey, fetched, ttl);
        }
        return fetched;
    }

    public void evict(String userId) {
        if (userId == null) return;
        privacyRedisTemplate.delete(key(userId));
    }
}

