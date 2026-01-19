package com.vky.repository;

import com.vky.repository.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IInvitationRepository extends JpaRepository<Invitation, UUID> {
    boolean existsByInviterUserIdAndInviteeEmailIgnoreCaseAndIsDeletedFalse(UUID invitedByUserId, String invitedUserEmail);
    //@Query("SELECT i.inviterUserId FROM Invitation i WHERE i.inviteeEmail = :email")
    List<Invitation> findAllByInviteeEmailIgnoreCaseAndIsDeletedFalse(String email);
    Optional<Invitation> findByIdAndInviterUserIdAndIsDeletedFalse(UUID id,  UUID invitedByUserId);
    List<Invitation> findByInviterUserIdAndIsDeletedFalseOrderByContactName(UUID userId);
}