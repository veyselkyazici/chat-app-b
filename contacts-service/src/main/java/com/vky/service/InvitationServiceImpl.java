package com.vky.service;

import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.request.SendInvitationDTO;
import com.vky.exception.ContactsServiceException;
import com.vky.exception.ErrorType;
import com.vky.manager.IMailManager;
import com.vky.repository.IInvitationRepository;
import com.vky.repository.entity.Invitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements IInvitationService {
    private final IInvitationRepository invitationRepository;
    private final IMailManager mailManager;

    @Override
    public boolean isExistsInvitation(UUID uuid, String email) {
        return invitationRepository.existsByInviterUserIdAndInviteeEmailIgnoreCaseAndIsDeletedFalse(uuid, email);
    }

    @Override
    public List<Invitation> getInvitations(String email) {
        return invitationRepository.findAllByInviteeEmailIgnoreCaseAndIsDeletedFalse(email);
    }

    @Override
    public List<Invitation> findInvitationByInviterUserIdOrderByContactName(UUID userId) {
        return invitationRepository.findByInviterUserIdAndIsDeletedFalseOrderByContactName(userId);
    }

    @Override
    public void deleteInvitation(UUID id, String tokenUserId) {
        Invitation invitation = invitationRepository
                .findByIdAndInviterUserIdAndIsDeletedFalse(id, UUID.fromString(tokenUserId))
                .orElseThrow(() -> new ContactsServiceException(ErrorType.USER_NOT_FOUD));

        invitation.setDeleted(true);
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void sendInvitation(SendInvitationDTO sendInvitationDTO, String tokenUserId) {
        if (sendInvitationDTO.inviteeEmail() == null
                || !tokenUserId.equals(sendInvitationDTO.inviterUserId().toString())) {
            throw new ContactsServiceException(ErrorType.USER_NOT_FOUD);
        }
        Invitation invitation = invitationRepository.findById(sendInvitationDTO.invitationId())
                .orElseThrow(() -> new ContactsServiceException(ErrorType.USER_NOT_FOUD));
        mailManager.sendInvitation(sendInvitationDTO);
        if (invitation.isInvited()) {
            throw new ContactsServiceException(ErrorType.ALREADY_INVITED);
        }
        invitation.setInvited(true);
        invitationRepository.save(invitation);
    }

    @Override
    public Invitation addInvitation(ContactRequestDTO contactRequestDTO, String userId) {
        return invitationRepository.save(Invitation.builder()
                .inviterUserId(UUID.fromString(userId))
                .inviteeEmail(contactRequestDTO.userContactEmail())
                .contactName(contactRequestDTO.userContactName())
                .inviterEmail(contactRequestDTO.addedByEmail())
                .build());

    }

    @Override
    public void saveInvitation(Invitation invitation) {
        invitationRepository.save(invitation);
    }
}
