package com.vky.service;

import com.vky.dto.request.ForgotPasswordRequestDTO;
import com.vky.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {
    private final MailService mailService;
    private final OtpUtil otpUtil;
    private final RedisTemplate<String, Object> redisTemplate;


    public void createForgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        String otp = otpUtil.generateOtp();
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

        this.mailService.sendHtmlEmailWithEmbeddedFilesForgotPassword(forgotPasswordRequestDTO.getEmail(),  otp);
    }

}
