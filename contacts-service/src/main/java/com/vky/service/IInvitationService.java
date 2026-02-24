package com.vky.service;

import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.request.SendInvitationDTO;
import com.vky.repository.entity.Invitation;

import java.util.List;
import java.util.UUID;

public interface IInvitationService {
    boolean isExistsInvitation(UUID uuid, String email);

    List<Invitation> getInvitations(String email);

    List<Invitation> findInvitationByInviterUserIdOrderByContactName(UUID userId);

    void deleteInvitation(UUID id, String tokenUserId);

    void sendInvitation(SendInvitationDTO sendInvitationDTO, String tokenUserId);

    Invitation addInvitation(ContactRequestDTO contactRequestDTO, String userId);

    void saveInvitation(Invitation invitation);
}
