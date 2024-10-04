package com.vky.repository;

import com.vky.repository.entity.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IContactsRepository extends JpaRepository<Contacts, UUID> {
    boolean existsContactsByUserContactEmailAndUserId(String contactEmail, UUID userId);
    List<Contacts> findContactsByUserIdOrderByUserContactName(UUID userId);
    @Query("SELECT c FROM Contacts c WHERE c.userId = :userId AND c.userContactId IN :userContactIds ORDER BY c.userContactName ASC")
    List<Contacts> findContactsByUserIdAndUserContactIds(
            @Param("userId") UUID userId,
            @Param("userContactIds") List<UUID> userContactIds
    );
    @Query("SELECT c.userId FROM Contacts c WHERE c.userContactId = :userId AND c.userId IN :contactIds")
    Set<UUID> findReversedContactIds(@Param("userId") UUID userId, @Param("contactIds") Set<UUID> contactIds);

    @Query("SELECT c.userContactId FROM Contacts c WHERE c.userId = :userId " +
            "UNION " +
            "SELECT c.userId FROM Contacts c WHERE c.userContactId = :userId")
    List<UUID> findUserContactsByUserIdOrUserContactId(@Param("userId") UUID userId);




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
