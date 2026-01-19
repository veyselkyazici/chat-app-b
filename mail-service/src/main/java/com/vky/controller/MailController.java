package com.vky.controller;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.dto.request.ForgotPasswordRequestDTO;
import com.vky.dto.request.ResendConfirmationRequestDTO;
import com.vky.dto.request.SendInvitationDTO;
import com.vky.service.ConfirmationService;
import com.vky.service.ForgotPasswordService;
import com.vky.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
public class MailController {
    private final ConfirmationService confirmationService;
    private final ForgotPasswordService forgotPasswordService;
    private final MailService mailService;

    @PostMapping("/create-forgot-password")
    public void createForgotPassword(@RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        this.forgotPasswordService.createForgotPassword(forgotPasswordRequestDTO);
    }

    @PostMapping("/create-confirmation")
    void createConfirmation(@RequestBody CreateConfirmationRequestDTO createConfirmationRequestDTO) {
        confirmationService.createConfirmation(createConfirmationRequestDTO);
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<Void> resendConfirmationMail(
            @RequestBody ResendConfirmationRequestDTO resendConfirmationRequestDTO) {
        confirmationService.resendConfirmation(resendConfirmationRequestDTO.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public ResponseEntity<Void> confirmUserAccount(@RequestParam("token") String verificationToken) {
        confirmationService.verifyToken(verificationToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-invitation-email")
    void sendInvitationEmail(@RequestBody SendInvitationDTO sendInvitationDTO) {
        mailService.sendInvitationEmail(sendInvitationDTO);
    }
}
