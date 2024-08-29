package com.vky.manager;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.dto.request.ForgotPasswordCheckOtpRequestDTO;
import com.vky.dto.request.ForgotPasswordRequestDTO;
import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import com.vky.dto.response.HttpResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(url = "${raceapplication.url.mail}api/v1/mail",name = "mail-service",dismiss404 = true)
public interface IMailManager {
    @PostMapping("/create-forgot-password")
    ResponseEntity<Void> createForgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO);

    @PostMapping("/check-otp")
    ResponseEntity<HttpResponse> checkOtp(ForgotPasswordCheckOtpRequestDTO forgotPasswordCheckOtpRequestDTO);

    @PostMapping("/reset-password")
    ResponseEntity<HttpResponse> resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO);

    @PostMapping("/create-confirmation")
    ResponseEntity<Void> createConfirmation(CreateConfirmationRequestDTO createConfirmationRequestDTO);
}
