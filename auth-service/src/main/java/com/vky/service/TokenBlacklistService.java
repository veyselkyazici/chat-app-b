package com.vky.service;

import com.vky.util.JwtTokenManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenManager jwtTokenManager;

    public void blacklistToken(String accessToken) {
        long ttlSeconds = jwtTokenManager.getRemainingTTL(accessToken);

        UUID authId = jwtTokenManager.extractAuthId(accessToken);

        redisTemplate.delete("refreshToken:" + authId);

        String hash = DigestUtils.sha256Hex(accessToken);

        redisTemplate.opsForValue().set(
                "blacklist:" + hash,
                "true",
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }
}
