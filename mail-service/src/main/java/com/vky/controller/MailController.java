package com.vky.controller;

import com.vky.HttpResponse;
import com.vky.dto.request.*;
import com.vky.repository.entity.ForgotPassword;
import com.vky.service.ConfirmationService;
import com.vky.service.ForgotPasswordService;
import com.vky.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
public class MailController {
    private final ConfirmationService confirmationService;
    private final ForgotPasswordService forgotPasswordService;
    private final MailService mailService;
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
    @PostMapping("/create-confirmation")
    public ResponseEntity<Void> createConfirmation(@RequestBody CreateConfirmationRequestDTO createConfirmationRequestDTO) {
        confirmationService.createConfirmation(createConfirmationRequestDTO);
        return ResponseEntity.ok().build();
    }
    @GetMapping()
    public ResponseEntity<HttpResponse> confirmUserAccount(@RequestParam("token") String verificationToken) {
        Boolean isSuccess = confirmationService.verifyToken(verificationToken);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Success", isSuccess))
                        .message("Account Verified")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }


    @PostMapping("/send-invitation-email")
    public ResponseEntity<String> sendInvitationEmail(@RequestBody SendInvitationEmailDTO sendInvitationEmailDTO){
        mailService.sendInvitationEmail(sendInvitationEmailDTO);
        if(1 ==1) {
            throw new RuntimeException();
        }
        return ResponseEntity.ok("Davet başarıyla gönderildi.");
    }
}
