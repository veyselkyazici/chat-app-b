package com.vky.service;

import com.vky.dto.request.*;
import com.vky.dto.response.*;

import java.util.UUID;

public interface IAuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    LoginResponseDTO refreshAuthenticationToken(String refreshToken);

    void register(RegisterRequestDTO registerRequestDTO);

    boolean saveVerifiedAccountId(UUID id);

    ForgotPasswordResponseDTO createForgotPassword(String email);

    CheckOtpResponseDTO checkOtp(CheckOtpRequestDTO checkOtpRequestDTO);

    void resetPassword(ForgotPasswordResetPasswordRequestDTO forgotPasswordResetPasswordRequestDTO);

    void changePassword(ChangePasswordRequestDTO changePasswordRequestDTO, String userId);
}
