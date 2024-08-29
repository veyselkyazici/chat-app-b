package com.vky.repository;

import com.vky.repository.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IInvitationRepository extends JpaRepository<Invitation, UUID> {
    boolean existsByInviterUserIdAndInviteeEmailIgnoreCase(UUID invitedByUserId, String invitedUserEmail);
    Optional<Invitation> findByInviterUserIdAndInviteeEmailIgnoreCase(UUID invitedByUserId, String invitedUserEmail);
    Optional<Invitation> findByInviteeEmail(String invitedUserEmail);

    List<Invitation> findInvitationByInviterUserIdOrderByContactName(UUID userId);
}
