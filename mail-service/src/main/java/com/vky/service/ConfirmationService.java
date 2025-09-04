package com.vky.service;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.exception.ErrorType;
import com.vky.exception.MailServiceException;
import com.vky.manager.IAuthManager;
import com.vky.repository.ConfirmationRepository;
import com.vky.repository.entity.Confirmation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConfirmationService {
    private final ConfirmationRepository confirmationRepository;
    private final IAuthManager authManager;
    private final MailService mailService;

    public ConfirmationService(ConfirmationRepository confirmationRepository, IAuthManager authManager, MailService mailService) {
        this.confirmationRepository = confirmationRepository;
        this.authManager = authManager;
        this.mailService = mailService;
    }

    public void createConfirmation(CreateConfirmationRequestDTO createConfirmationRequestDTO) {
        Confirmation confirmation = Confirmation.builder()
                .verificationToken(UUID.randomUUID().toString())
                .authId(createConfirmationRequestDTO.getId() != null ? createConfirmationRequestDTO.getId() : null)
                .email(createConfirmationRequestDTO.getEmail())
                .isUsed(false)
                .build();
        this.confirmationRepository.save(confirmation);
        this.sendEMailVerification(createConfirmationRequestDTO, confirmation);
    }

    public void sendEMailVerification(CreateConfirmationRequestDTO createConfirmationRequestDTO, Confirmation confirmation) {
        this.mailService.sendHtmlEmailWithEmbeddedFiles(createConfirmationRequestDTO.getEmail(), confirmation.getVerificationToken());
    }
    public void verifyToken(String verificationToken) {
        Confirmation confirmation = confirmationRepository.findByVerificationToken(verificationToken);
        if (confirmation == null) {
            throw new MailServiceException(ErrorType.TOKEN_NOT_FOUND);
        }

        if (confirmation.isUsed()) {
            throw new MailServiceException(ErrorType.TOKEN_ALREADY_USER);
        }
        confirmation.setUsed(true);
        confirmationRepository.save(confirmation);
        authManager.saveVerifiedAccountId(confirmation.getAuthId());
    }

}
