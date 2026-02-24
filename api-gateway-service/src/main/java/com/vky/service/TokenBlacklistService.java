package com.vky.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlacklisted(String token) {
        String hash = DigestUtils.sha256Hex(token);
        return redisTemplate.opsForValue().get("blacklist:" + hash) != null;
    }
}
