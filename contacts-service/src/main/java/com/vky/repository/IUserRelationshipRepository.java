package com.vky.repository;

import com.vky.repository.entity.UserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRelationshipRepository extends JpaRepository<UserRelationship, UUID> {
    Optional<UserRelationship> findByUserIdAndRelatedUserId(UUID userId, UUID relatedUserId);
    @Query("SELECT ur FROM UserRelationship ur " +
            "WHERE ur.userId = :id OR ur.relatedUserId = :id")
    List<UserRelationship> findByUserIdOrRelatedUserId(UUID id);
    @Query("SELECT ur FROM UserRelationship ur WHERE " +
            "(ur.userId = :userId AND ur.relatedUserId = :relatedUserId) OR " +
            "(ur.userId = :relatedUserId AND ur.relatedUserId = :userId)")
    Optional<UserRelationship> findRelationshipBetweenUsers(
            @Param("userId") UUID userId,
            @Param("relatedUserId") UUID relatedUserId);

    @Query("""
        SELECT ur 
        FROM UserRelationship ur 
        WHERE 
            (ur.userId = :userId AND ur.relatedUserId IN :chatIds) OR 
            (ur.relatedUserId = :userId AND ur.userId IN :chatIds)
    """)
    List<UserRelationship> findRelationshipsForUser(
            @Param("userId") UUID userId,
            @Param("chatIds") List<UUID> chatIds
    );

    List<UserRelationship> findByUserId(UUID userId);
}
