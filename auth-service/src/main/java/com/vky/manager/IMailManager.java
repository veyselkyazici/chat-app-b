package com.vky.manager;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.dto.request.ForgotPasswordRequestDTO;
import com.vky.dto.request.ResendConfirmationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "mail-service", path = "/api/v1/mail", dismiss404 = true)
public interface IMailManager {
    @PostMapping("/create-forgot-password")
    void createForgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO);

    @PostMapping("/create-confirmation")
    void createConfirmation(CreateConfirmationRequestDTO createConfirmationRequestDTO);

    @PostMapping("/resend-confirmation")
    ResponseEntity<Void> resendConfirmation(ResendConfirmationRequestDTO resendConfirmationRequestDTO);
}
