package com.vky.service;

import com.vky.HttpResponse;
import com.vky.dto.request.ForgotPasswordCheckOtpRequestDTO;
import com.vky.dto.request.ForgotPasswordRequestDTO;
import com.vky.dto.request.ForgotPasswordResetPasswordRequestDTO;
import com.vky.manager.IAuthManager;
import com.vky.repository.ForgotPasswordRepository;
import com.vky.repository.entity.ForgotPassword;
import com.vky.utils.OtpUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class ForgotPasswordService {
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final IAuthManager authManager;
    private final MailService mailService;
    private final OtpUtil otpUtil;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordService(ForgotPasswordRepository forgotPasswordRepository, IAuthManager authManager, MailService mailService, OtpUtil otpUtil, PasswordEncoder passwordEncoder) {
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.authManager = authManager;
        this.mailService = mailService;
        this.otpUtil = otpUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public void createForgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        System.out.println("AUTHID: " + forgotPasswordRequestDTO.getAuthId());
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .password(forgotPasswordRequestDTO.getPassword())
                .newPassword(null)
                .authId(forgotPasswordRequestDTO.getAuthId())
                .verificationToken(this.otpUtil.generateOtp())
                .build();
        this.mailService.sendHtmlEmailWithEmbeddedFilesForgotPassword(forgotPasswordRequestDTO.getEmail(),  forgotPassword.getVerificationToken());
        forgotPassword.setExpiryDate(LocalDateTime.now().plusMinutes(3));
        this.forgotPasswordRepository.save(forgotPassword);
    }


    public Optional<ForgotPassword> checkOtp(ForgotPasswordCheckOtpRequestDTO forgotPasswordCheckOtpRequestDTO) {
        System.out.println("AUTHID: " + forgotPasswordCheckOtpRequestDTO.getAuthId());
        Optional<ForgotPassword> forgotPassword = this.forgotPasswordRepository.findFirstByAuthIdAndExpiryDateAfterOrderByExpiryDateDesc(
                forgotPasswordCheckOtpRequestDTO.getAuthId(), LocalDateTime.now());
//        System.out.println("AuthID: " + forgotPassword.get().getAuthId());
//        System.out.println("ForgotPasswordID: " + forgotPassword.get().getId());
        if (forgotPassword.isPresent() && forgotPasswordCheckOtpRequestDTO.getOtp().equals(forgotPassword.get().getVerificationToken()) ) {
            return forgotPassword;
        }
        return Optional.empty();
    }

    public Optional<ForgotPassword> resetPassword(Long forgotPasswordId, String newPassword) {
        Optional<ForgotPassword> forgotPassword = this.forgotPasswordRepository.findById(forgotPasswordId);
        if (forgotPassword.isPresent()) {
            forgotPassword.get().setNewPassword(newPassword);
            this.forgotPasswordRepository.save(forgotPassword.get());
            return forgotPassword;
        }
        return Optional.empty();
    }
}
