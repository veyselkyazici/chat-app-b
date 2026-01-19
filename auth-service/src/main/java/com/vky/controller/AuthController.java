package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.ApiResponse;
import com.vky.dto.response.CheckOtpResponseDTO;
import com.vky.dto.response.ForgotPasswordResponseDTO;
import com.vky.dto.response.LoginResponseDTO;
import com.vky.service.AuthService;
import com.vky.service.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

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
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refreshAuthenticationToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginResponseDTO dto = authService.refreshAuthenticationToken(refreshTokenRequest.getRefreshToken());

        return ResponseEntity.ok(new ApiResponse<>(true,"success", dto));
    }

    @GetMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestHeader("X-Id") String userId) {
        return ResponseEntity.ok(Map.of("userId", userId, "authenticated", true));
    }


    @PostMapping("/save-verified-account-id")
    public ResponseEntity<Void> saveVerifiedAccount(@RequestBody UUID id) {
        boolean isSuccess = authService.saveVerifiedAccountId(id);

        if (isSuccess) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("create-forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponseDTO>> createForgotPassword(@RequestBody CreateForgotPasswordRequestDTO createForgotPasswordRequestDTO) {
        ForgotPasswordResponseDTO forgotPassword = authService.createForgotPassword(createForgotPasswordRequestDTO.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful", forgotPassword));
    }

    @PostMapping("/check-otp")
    public ResponseEntity<ApiResponse<CheckOtpResponseDTO>> checkOtp(@RequestBody CheckOtpRequestDTO checkOtpRequestDTO) {

        CheckOtpResponseDTO dto = this.authService.checkOtp(checkOtpRequestDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "", dto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody ForgotPasswordResetPasswordRequestDTO
                                                       forgotPasswordResetPasswordRequestDTO) {
        this.authService.resetPassword(forgotPasswordResetPasswordRequestDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "", ""));
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String accessHeader,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String token = null;

        if (accessHeader != null && accessHeader.startsWith("Bearer ")) {
            token = accessHeader.substring(7);
        }

        if (token == null && body != null && body.containsKey("token")) {
            token = body.get("token");
        }

        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestHeader("X-Id") String userId, @RequestBody ChangePasswordRequestDTO
                                                                changePasswordRequestDTO) {
        this.authService.changePassword(changePasswordRequestDTO, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "", ""));
    }

}
