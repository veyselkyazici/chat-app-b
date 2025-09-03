package com.vky.repository;

import com.vky.repository.entity.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IContactsRepository extends JpaRepository<Contacts, UUID> {
    Optional<Contacts> findContactsByUserContactEmailAndUserId(String contactEmail, UUID userId);
    @Query("""
        SELECT new com.vky.repository.ContactWithRelationshipDTO(
            c.id,
            c.userId,
            c.userContactId,
            c.userContactName,
            CASE 
                WHEN ur.userId = :userId THEN ur.userHasAddedRelatedUser 
                ELSE ur.relatedUserHasAddedUser 
            END,
            CASE 
                WHEN ur.relatedUserId = :userId THEN ur.userHasAddedRelatedUser 
                ELSE ur.relatedUserHasAddedUser 
            END
        )
        FROM Contacts c
        LEFT JOIN UserRelationship ur 
            ON (ur.userId = c.userId AND ur.relatedUserId = c.userContactId)
            OR (ur.userId = c.userContactId AND ur.relatedUserId = c.userId)
        WHERE c.userId = :userId AND c.isDeleted = false ORDER BY UPPER(c.userContactName) ASC
    """)
    List<ContactWithRelationshipDTO> findContactsAndRelationshipsByUserId(UUID userId);

    @Query("SELECT new com.vky.repository.ContactWithRelationshipDTO(c.id, c.userId, c.userContactId, c.userContactName, " +
            "ur.userHasAddedRelatedUser, ur.relatedUserHasAddedUser) " +
            "FROM Contacts c " +
            "LEFT JOIN UserRelationship ur " +
            "ON ( (c.userId = ur.userId AND c.userContactId = ur.relatedUserId) " +
            "   OR (c.userId = ur.relatedUserId AND c.userContactId = ur.userId) ) " +
            "WHERE (c.userId = :userId AND c.userContactId = :userContactId) ")
    Optional<ContactWithRelationshipDTO> findContactWithRelationship(
            @Param("userId") UUID userId,
            @Param("userContactId") UUID userContactId
    );

    @Query("""
        SELECT c 
        FROM Contacts c 
        WHERE c.userId = :userId AND c.userContactId IN :userContactIds
    """)
    List<Contacts> findContactsForUser(
            @Param("userId") UUID userId,
            @Param("userContactIds") List<UUID> userContactIds
    );
}
