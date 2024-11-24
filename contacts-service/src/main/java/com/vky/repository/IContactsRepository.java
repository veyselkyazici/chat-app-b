package com.vky.repository;

import com.vky.repository.entity.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface IContactsRepository extends JpaRepository<Contacts, UUID> {
    boolean existsContactsByUserContactEmailAndUserId(String contactEmail, UUID userId);
    Optional<Contacts> findContactsByUserContactEmailAndUserId(String contactEmail, UUID userId);
    List<Contacts> findContactsByUserIdOrderByUserContactName(UUID userId);
    @Query("SELECT new com.vky.repository.ContactWithRelationshipDTO(c.id, c.userId, c.userContactId, c.userContactName, " +
            "COALESCE(r.userHasAddedRelatedUser, false), COALESCE(r.relatedUserHasAddedUser, false)) " +
            "FROM Contacts c " +
            "LEFT JOIN UserRelationship r " +
            "ON c.userContactId = r.relatedUserId " +
            "WHERE c.userId = :userId")
    List<ContactWithRelationshipDTO> findContactsAndRelationshipsByUserId(@Param("userId") UUID userId);
    @Query("SELECT c.userId FROM Contacts c WHERE c.userContactId = :userId AND c.userId IN :contactIds")
    Set<UUID> findReversedContactIds(@Param("userId") UUID userId, @Param("contactIds") Set<UUID> contactIds);

    @Query("SELECT c FROM Contacts c WHERE c.userId = :userId " +
            "UNION " +
            "SELECT c FROM Contacts c WHERE c.userContactId = :userId")
    List<Contacts> findUserContactsByUserIdOrUserContactId(@Param("userId") UUID userId);

    @Query("SELECT c FROM Contacts c WHERE (c.userId = :userId AND c.userContactId IN :userContactIds) OR (c.userId IN :userContactIds AND c.userContactId = :userId)")
    List<Contacts> findContactsByUserIdAndUserContactIds(
            @Param("userId") UUID userId,
            @Param("userContactIds") List<UUID> userContactIds
    );
    @Query("SELECT new com.vky.repository.ContactWithRelationshipDTO(c.id, c.userId, c.userContactId, c.userContactName, ur.userHasAddedRelatedUser, ur.relatedUserHasAddedUser) " +
            "FROM Contacts c " +
            "LEFT JOIN UserRelationship ur ON ((c.userId = ur.userId AND c.userContactId = ur.relatedUserId) " +
            "   OR (c.userId = ur.relatedUserId AND c.userContactId = ur.userId) ) " +
            "WHERE c.userId = :userId AND c.userContactId IN :userContactIds")
    List<ContactWithRelationshipDTO> findContactsWithRelationships(
            @Param("userId") UUID userId,
            @Param("userContactIds") List<UUID> userContactIds
    );
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



    /**@Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendships f " +
                "WHERE f.userId = :userId AND f.friendId = :friendId")
    boolean existsByUserIdAndFriendId(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    List<Friendships> findByUserIdAndFriendshipStatus(UUID userId, ContactsStatus friendshipStatus);

    @Query("SELECT f FROM Friendships f " +
            "WHERE f.friendId = :friendId " +
            "AND f.friendshipStatus = :friendshipStatus " +
            "ORDER BY f.createdAt DESC")
    List<Friendships> findSentFriendshipsOrderByCreatedAtDesc(
            @Param("friendId") UUID friendId,
            @Param("friendshipStatus") ContactsStatus friendshipStatus
    );

    Optional<Friendships> findOptionalByUserIdAndFriendId(UUID userId, UUID friendId);
    @Query("SELECT f FROM Friendships f " +
            "WHERE f.userId = :userId " +
            "AND f.friendshipStatus = :friendshipStatus " +
            "ORDER BY f.createdAt DESC")
    Optional<List<Friendships>> friendRequestReplyNotifications(@Param("userId") UUID userId, @Param("friendshipStatus") ContactsStatus friendshipStatus);*/
}
