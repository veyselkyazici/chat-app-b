package com.vky.service;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.exception.ErrorType;
import com.vky.exception.MailServiceException;
import com.vky.manager.IAuthManager;
import com.vky.repository.ConfirmationRepository;
import com.vky.repository.entity.Confirmation;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class ConfirmationService {
    private final ConfirmationRepository confirmationRepository;
    private final IAuthManager authManager;
    private final MailService mailService;
    private final RedisService redisService;

    public ConfirmationService(ConfirmationRepository confirmationRepository, IAuthManager authManager,
            MailService mailService, RedisService redisService) {
        this.confirmationRepository = confirmationRepository;
        this.authManager = authManager;
        this.mailService = mailService;
        this.redisService = redisService;
    }

    @Transactional
    public void createConfirmation(CreateConfirmationRequestDTO createConfirmationRequestDTO) {
        Confirmation confirmation = Confirmation.builder()
                .verificationToken(UUID.randomUUID().toString())
                .authId(createConfirmationRequestDTO.id() != null ? createConfirmationRequestDTO.id() : null)
                .email(createConfirmationRequestDTO.email())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .isUsed(false)
                .build();

        this.confirmationRepository.save(confirmation);
        redisService.saveConfirmation(confirmation);
        this.sendEMailVerification(createConfirmationRequestDTO, confirmation);
    }

    public void sendEMailVerification(CreateConfirmationRequestDTO createConfirmationRequestDTO,
            Confirmation confirmation) {
        this.mailService.sendHtmlEmailWithEmbeddedFiles(createConfirmationRequestDTO.email(),
                confirmation.getVerificationToken());
    }

    @Transactional
    public void verifyToken(String token) {
        // 1) Redis first
        Map<Object, Object> redisData = redisService.getConfirmation(token);

        if (!redisData.isEmpty()) {
            boolean isUsed = Boolean.parseBoolean(String.valueOf(redisData.get("isUsed")));
            Instant expiresAt = Instant.parse(String.valueOf(redisData.get("expiresAt")));

            if (isUsed) {
                throw new MailServiceException(ErrorType.TOKEN_ALREADY_USED);
            }
            if (expiresAt.isBefore(Instant.now())) {
                redisService.deleteConfirmation(token);
                throw new MailServiceException(ErrorType.TOKEN_EXPIRED);
            }

            Confirmation c = confirmationRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new MailServiceException(ErrorType.TOKEN_NOT_FOUND));

            if (c.isUsed()) {
                redisService.deleteConfirmation(token);
                throw new MailServiceException(ErrorType.TOKEN_ALREADY_USED);
            }
            if (c.getExpiresAt().isBefore(Instant.now())) {
                redisService.deleteConfirmation(token);
                throw new MailServiceException(ErrorType.TOKEN_EXPIRED);
            }

            c.setUsed(true);
            confirmationRepository.save(c);

            redisService.deleteConfirmation(token);

            if (c.getAuthId() == null) {
                throw new MailServiceException(ErrorType.AUTH_ID_MISSING);
            }
            authManager.saveVerifiedAccountId(c.getAuthId());
            return;
        }

        Confirmation c = confirmationRepository.findByVerificationToken(token)
                .orElseThrow(() -> new MailServiceException(ErrorType.TOKEN_NOT_FOUND));

        if (c.isUsed()) {
            throw new MailServiceException(ErrorType.TOKEN_ALREADY_USED);
        }
        if (c.getExpiresAt().isBefore(Instant.now())) {
            throw new MailServiceException(ErrorType.TOKEN_EXPIRED);
        }

        c.setUsed(true);
        confirmationRepository.save(c);
        redisService.deleteConfirmation(token);

        if (c.getAuthId() == null) {
            throw new MailServiceException(ErrorType.AUTH_ID_MISSING);
        }
        authManager.saveVerifiedAccountId(c.getAuthId());
    }


    @Transactional
    public void resendConfirmation(String email) {
        Confirmation last = confirmationRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (last == null) throw new MailServiceException(ErrorType.TOKEN_NOT_FOUND);
        if (last.isUsed()) throw new MailServiceException(ErrorType.TOKEN_ALREADY_USED);

        last.setUsed(true);
        confirmationRepository.save(last);
        redisService.deleteConfirmation(last.getVerificationToken());

        Confirmation fresh = Confirmation.builder()
                .verificationToken(UUID.randomUUID().toString())
                .authId(last.getAuthId())
                .email(email)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .isUsed(false)
                .build();

        confirmationRepository.save(fresh);
        redisService.saveConfirmation(fresh);

        sendEMailVerification(
                CreateConfirmationRequestDTO.builder()
                        .email(email)
                        .id(fresh.getAuthId())
                        .build(),
                fresh
        );
    }



}
