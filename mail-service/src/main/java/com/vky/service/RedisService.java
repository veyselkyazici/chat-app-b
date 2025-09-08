package com.vky.service;

import com.vky.dto.request.ForgotPasswordRequestDTO;
import com.vky.repository.entity.Confirmation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveForgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO, String otp) {
        String redisKey = "reset_password:" + forgotPasswordRequestDTO.getAuthId();

        Map<String, String> resetData = Map.of(
                "otp",            otp,
                "email",          forgotPasswordRequestDTO.getEmail(),
                "attempts",       "0",
                "attempts_limit", "3",
                "created_at",     Instant.now().toString()
        );

        redisTemplate.opsForHash().putAll(redisKey, resetData);
        redisTemplate.expire(redisKey, Duration.ofMinutes(3));

    }

    public void saveConfirmation(Confirmation confirmation) {
        String redisKey = "confirmation:" + confirmation.getVerificationToken();
        Map<String, String> confirmationData = Map.of(
                "authId", confirmation.getAuthId() != null ? confirmation.getAuthId().toString() : "",
                "email", confirmation.getEmail(),
                "isUsed", String.valueOf(confirmation.isUsed()),
                "expiresAt", confirmation.getExpiresAt().toString()
        );

        redisTemplate.opsForHash().putAll(redisKey, confirmationData);

        long secondsToExpire = Duration.between(Instant.now(), confirmation.getExpiresAt()).getSeconds();
        redisTemplate.expire(redisKey, Duration.ofSeconds(secondsToExpire));
    }

    public Map<Object, Object> getConfirmation(String token) {
        String redisKey = "confirmation:" + token;
        return redisTemplate.opsForHash().entries(redisKey);
    }

    public void deleteConfirmation(String token) {
        String redisKey = "confirmation:" + token;
        redisTemplate.delete(redisKey);
    }
}
