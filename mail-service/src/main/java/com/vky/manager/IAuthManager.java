package com.vky.manager;

import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(name = "auth-service", path = "/api/v1/auth",dismiss404 = true)
public interface IAuthManager {

    @PostMapping("/save-verified-account-id")
    ResponseEntity<Void> saveVerifiedAccountId(UUID id);

    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO);
}
