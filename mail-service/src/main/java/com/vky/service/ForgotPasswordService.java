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
    private final RedisService redisService;


    public void createForgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        String otp = otpUtil.generateOtp();
        redisService.saveForgotPassword(forgotPasswordRequestDTO, otp);
        this.mailService.sendHtmlEmailWithEmbeddedFilesForgotPassword(forgotPasswordRequestDTO.getEmail(),  otp);
    }

}
