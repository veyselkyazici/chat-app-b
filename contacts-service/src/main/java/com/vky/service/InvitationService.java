package com.vky.service;

import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.request.SendInvitationDTO;
import com.vky.dto.request.SendInvitationEmailDTO;
import com.vky.dto.response.DeleteContactResponseDTO;
import com.vky.exception.InvitationNotFoundException;
import com.vky.manager.IMailManager;
import com.vky.manager.IUserManager;
import com.vky.repository.IInvitationRepository;
import com.vky.repository.entity.Invitation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {
    private final IInvitationRepository invitationRepository;
    private final IMailManager mailManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final IUserManager userManager;

    public boolean isExistsInvitation(UUID uuid, String email) {
        return invitationRepository.existsByInviterUserIdAndInviteeEmailIgnoreCase(uuid, email);
    }
    public Invitation findInvitationAndDelete(UUID uuid, String email) {
        Invitation invitation = invitationRepository.findByInviterUserIdAndInviteeEmailIgnoreCase(uuid, email)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation not found for user id: " + uuid + " and email: " + email));

        invitation.setDeleted(true);
        return invitationRepository.save(invitation);
    }



    public Invitation findByInvitedUserEmailAndIsDeletedFalse(String email) {
        return invitationRepository.findByInviteeEmail(email).orElse(null);
    }

    public List<Invitation> findInvitationByInviterUserIdOrderByContactName(UUID userId) {
        return invitationRepository.findInvitationByInviterUserIdOrderByContactName(userId);
    }

    public DeleteContactResponseDTO deleteInvitation(UUID id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation not found for id: " + id));

        invitation.setDeleted(true);
        invitationRepository.save(invitation);

        DeleteContactResponseDTO dto = new DeleteContactResponseDTO(
                invitation.getId(),
                invitation.getInviteeEmail(),
                invitation.getContactName(),
                invitation.getInviterUserId(),
                null,false
        );

        messagingTemplate.convertAndSendToUser(
                invitation.getInviterUserId().toString(),
                "queue/delete/contact",
                dto
        );

        return dto;
    }

    public String sendInvitation(SendInvitationDTO sendInvitationDTO) {
        String inviterEmail = userManager.getUserByEmailByIdd(sendInvitationDTO.getInviterUserId());
        SendInvitationEmailDTO sendInvitationEmailDTO = new SendInvitationEmailDTO(sendInvitationDTO.getInvitationId() ,sendInvitationDTO.getInviteeEmail()
        , sendInvitationDTO.getContactName(), sendInvitationDTO.getInviterUserId(), sendInvitationDTO.isInvited(), inviterEmail);
        ResponseEntity<String> response = mailManager.sendInvitation(sendInvitationEmailDTO);
        if (response.getStatusCode() == HttpStatus.OK) {
            Invitation invitation = invitationRepository.findById(sendInvitationDTO.getInvitationId()).orElseThrow(() -> new InvitationNotFoundException("Kullanıcı bulunamadı."));
            invitation.setInvited(true);
        }
        return response.getBody();
    }


    public Invitation addInvitation(ContactRequestDTO contactRequestDTO) {
        return invitationRepository.save(Invitation.builder()
                .inviterUserId(contactRequestDTO.userId())
                .inviteeEmail(contactRequestDTO.userContactEmail())
                .contactName(contactRequestDTO.userContactName())
                .build());

    }
}
