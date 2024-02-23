package com.vky.repository;

import com.vky.repository.entity.Friendships;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFriendshipsRepository extends JpaRepository<Friendships, UUID> {
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendships f " +
                "WHERE f.userId = :userId AND f.friendUserId = :friendUserId")
    boolean existsByUserIdAndFriendUserId(@Param("userId") UUID userId, @Param("friendUserId") UUID friendUserId);

    List<Friendships> findByUserId(UUID userId);

    @Query("SELECT f FROM Friendships f " +
            "WHERE f.friendUserId = :friendUserId " +
            "AND f.friendshipStatus = :friendshipStatus " +
            "ORDER BY f.createdAt DESC")
    List<Friendships> findSentFriendshipsOrderByCreatedAtDesc(
            @Param("friendUserId") UUID friendUserId,
            @Param("friendshipStatus") FriendshipStatus friendshipStatus
    );

    Optional<Friendships> findOptionalByUserIdAndFriendUserId(UUID userId, UUID friendUserId);
    @Query("SELECT f FROM Friendships f " +
            "WHERE f.userId = :userId " +
            "AND f.friendshipStatus = :friendshipStatus " +
            "ORDER BY f.createdAt DESC")
    Optional<List<Friendships>> friendRequestReplyNotifications(@Param("userId") UUID userId, @Param("friendshipStatus") FriendshipStatus friendshipStatus);
}
