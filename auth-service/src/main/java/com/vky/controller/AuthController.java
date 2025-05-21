package com.vky.controller;

import com.vky.dto.request.LoginRequestDTO;
import com.vky.dto.request.RegisterRequestDTO;
import com.vky.dto.request.CheckOtpRequestDTO;
import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import com.vky.dto.response.*;
import com.vky.repository.entity.Auth;
import com.vky.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO) {
        authService.register(registerRequestDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", loginResponseDTO));
    }


    @GetMapping("/authenticate")
    public Boolean authenticate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         return authentication != null && authentication.isAuthenticated() &&
                 !authentication.getName().equals("anonymousUser");
    }

    @PostMapping("/save-verified-account")
    public ResponseEntity<Void> saveVerifiedAccount(@RequestBody UUID id) {
        authService.saveVerifiedAccount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("create-forgot-password")
    public ResponseEntity<HttpResponse> createForgotPassword(@RequestBody String email) {
        Optional<Auth> auth = authService.createForgotPassword(email);

        return auth.<ResponseEntity<HttpResponse>>map(data -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Success", true))
                        .message("Doğrulama Kodu Gönderildi")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build())).orElseGet(() -> ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Unsuccessful", false))
                        .message("Doğrulama Kodu Gönderilemedi")
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build()));
    }

    @PostMapping("/check-otp")
    ResponseEntity<HttpResponse> checkOtp(@RequestBody CheckOtpRequestDTO checkOtpRequestDTO) {
        HttpResponse response = this.authService.findByEmailOtp(checkOtpRequestDTO);
        System.out.println("response: " + response);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reset-password")
    ResponseEntity<HttpResponse> resetPassword(@RequestBody ForgotPasswordResetPasswordRequestDTO
                                                       forgotPasswordResetPasswordRequestDTO) {
        System.out.println("authService: " + forgotPasswordResetPasswordRequestDTO.toString());
        this.authService.resetPassword(forgotPasswordResetPasswordRequestDTO);
        return ResponseEntity.ok().build();
    }


}
