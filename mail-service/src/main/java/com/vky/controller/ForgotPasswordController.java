package com.vky.controller;

import com.vky.HttpResponse;
import com.vky.dto.request.ForgotPasswordCheckOtpRequestDTO;
import com.vky.dto.request.ForgotPasswordRequestDTO;
import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import com.vky.repository.entity.ForgotPassword;
import com.vky.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {
    private final ForgotPasswordService forgotPasswordService;
    @PostMapping("/create-forgot-password")
    public ResponseEntity<Void> createForgotPassword(@RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        this.forgotPasswordService.createForgotPassword(forgotPasswordRequestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-otp")
    public ResponseEntity<HttpResponse> checkOtp(@RequestBody ForgotPasswordCheckOtpRequestDTO forgotPasswordCheckOtpRequestDTO) {
        Optional<ForgotPassword> forgotPassword= this.forgotPasswordService.checkOtp(forgotPasswordCheckOtpRequestDTO);
        return forgotPassword.<ResponseEntity<HttpResponse>>map(data -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Success", true,"forgotPasswordId", forgotPassword.get().getId()))
                        .message("Otp Matched")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build())).orElseGet(() -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Unsuccessful", false))
                        .message("Otp not Matched")
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<HttpResponse> resetPassword(@RequestBody ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO) {
        System.out.println("maılService: " + forgotPasswordResetPasswordRequestDTO.toString());
        Optional<ForgotPassword> forgotPassword = this.forgotPasswordService.resetPassword(
                forgotPasswordResetPasswordRequestDTO.getForgotPasswordId(), forgotPasswordResetPasswordRequestDTO.getNewPassword());
        return forgotPassword.<ResponseEntity<HttpResponse>>map(data -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Success", true, "authId", data.getAuthId()))
                        .message("Password Change Successful")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build())).orElseGet(() -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Unsuccessful", false))
                        .message("Password Change Unsuccessful")
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build()));

    }
}
