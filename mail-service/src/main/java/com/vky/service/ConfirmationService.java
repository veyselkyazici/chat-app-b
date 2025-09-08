package com.vky.service;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.exception.ErrorType;
import com.vky.exception.MailServiceException;
import com.vky.manager.IAuthManager;
import com.vky.repository.ConfirmationRepository;
import com.vky.repository.entity.Confirmation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class ConfirmationService {
    private final ConfirmationRepository confirmationRepository;
    private final IAuthManager authManager;
    private final MailService mailService;
    private final RedisService redisService;

    public ConfirmationService(ConfirmationRepository confirmationRepository, IAuthManager authManager, MailService mailService, RedisService redisService) {
        this.confirmationRepository = confirmationRepository;
        this.authManager = authManager;
        this.mailService = mailService;
        this.redisService = redisService;
    }

    @Transactional
    public void createConfirmation(CreateConfirmationRequestDTO createConfirmationRequestDTO) {
        Confirmation confirmation = Confirmation.builder()
                .verificationToken(UUID.randomUUID().toString())
                .authId(createConfirmationRequestDTO.getId() != null ? createConfirmationRequestDTO.getId() : null)
                .email(createConfirmationRequestDTO.getEmail())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .isUsed(false)
                .build();


        this.confirmationRepository.save(confirmation);
        redisService.saveConfirmation(confirmation);
        this.sendEMailVerification(createConfirmationRequestDTO, confirmation);
    }

    public void sendEMailVerification(CreateConfirmationRequestDTO createConfirmationRequestDTO, Confirmation confirmation) {
        this.mailService.sendHtmlEmailWithEmbeddedFiles(createConfirmationRequestDTO.getEmail(), confirmation.getVerificationToken());
    }
    public void verifyToken(String verificationToken) {
        Map<Object, Object> redisData = redisService.getConfirmation(verificationToken);
        if (redisData.isEmpty()) {
            throw new MailServiceException(ErrorType.TOKEN_NOT_FOUND);
        }

        boolean isUsed = Boolean.parseBoolean((String) redisData.get("isUsed"));

        Instant expiresAt = Instant.parse((String) redisData.get("expiresAt"));

        if (isUsed) {
            throw new MailServiceException(ErrorType.TOKEN_ALREADY_USER);
        }
        if (expiresAt.isBefore(Instant.now())) {
            throw new MailServiceException(ErrorType.TOKEN_EXPIRED);
        }

        Confirmation confirmation = confirmationRepository
                .findByVerificationToken(verificationToken);
        confirmation.setUsed(true);
        confirmationRepository.save(confirmation);

        redisService.deleteConfirmation(verificationToken);

        authManager.saveVerifiedAccountId(confirmation.getAuthId());
    }

    @Transactional
    public void resendConfirmation(String email) {
        Confirmation confirmation = confirmationRepository.findTopByEmailOrderByCreatedAtDesc(email);

        if (confirmation == null) {
            throw new MailServiceException(ErrorType.TOKEN_NOT_FOUND);
        }

        if (confirmation.isUsed()) {
            throw new MailServiceException(ErrorType.TOKEN_ALREADY_USER);
        }

        if (confirmation.getExpiresAt().isAfter(Instant.now())) {
            sendEMailVerification(
                    CreateConfirmationRequestDTO.builder()
                            .email(email)
                            .id(confirmation.getAuthId())
                            .build(),
                    confirmation
            );
            return;
        }

        confirmation.setVerificationToken(UUID.randomUUID().toString());
        confirmation.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        confirmationRepository.save(confirmation);
        redisService.saveConfirmation(confirmation);

        sendEMailVerification(
                CreateConfirmationRequestDTO.builder()
                        .email(email)
                        .id(confirmation.getAuthId())
                        .build(),
                confirmation
        );
    }


}
