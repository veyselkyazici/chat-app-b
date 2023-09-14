package com.vky.manager;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(url = "${raceapplication.url.auth}api/v1/auth",name = "mail-service-userprofile",dismiss404 = true)
public interface IAuthManager {

    @PostMapping("/save-verified-account")
    ResponseEntity<Void> saveVerifiedAccount(UUID id);

    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO);
}
