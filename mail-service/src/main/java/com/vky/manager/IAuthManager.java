package com.vky.manager;

import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(url = "${raceapplication.url.auth}api/v1/auth",name = "mail-service-userprofile",dismiss404 = true)
public interface IAuthManager {

    @PostMapping("/save-verified-account-id")
    ResponseEntity<Void> saveVerifiedAccountId(UUID id);

    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO);
}
