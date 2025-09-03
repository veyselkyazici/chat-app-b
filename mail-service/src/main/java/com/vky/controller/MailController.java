package com.vky.controller;

import com.vky.HttpResponse;
import com.vky.dto.request.*;
import com.vky.service.ConfirmationService;
import com.vky.service.ForgotPasswordService;
import com.vky.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

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
        return ResponseEntity.ok("Davet başarıyla gönderildi.");
    }
}
