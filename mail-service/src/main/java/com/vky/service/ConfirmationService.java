package com.vky.service;

import com.vky.dto.request.CreateConfirmationRequestDTO;
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
                .authId(createConfirmationRequestDTO.getId())
                .build();
        this.confirmationRepository.save(confirmation);
        this.sendEMailVerification(createConfirmationRequestDTO, confirmation);
    }

    public void sendEMailVerification(CreateConfirmationRequestDTO createConfirmationRequestDTO, Confirmation confirmation) {
        this.mailService.sendHtmlEmailWithEmbeddedFiles(createConfirmationRequestDTO.getEmail(), confirmation.getVerificationToken());
    }
    public Boolean verifyToken(String verificationToken) {
        Confirmation confirmation = confirmationRepository.findByVerificationToken(verificationToken);
        System.out.println("AUthId: " + confirmation.getAuthId());
        System.out.println("Confirmation: " + confirmation.toString());
        authManager.saveVerifiedAccount(confirmation.getAuthId());
        return Boolean.TRUE;
    }



}
