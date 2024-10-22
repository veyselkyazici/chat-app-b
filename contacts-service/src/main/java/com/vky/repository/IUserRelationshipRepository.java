package com.vky.repository;

import com.vky.repository.entity.UserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRelationshipRepository extends JpaRepository<UserRelationship, UUID> {
    Optional<UserRelationship> findByUserIdAndRelatedUserId(UUID userId, UUID relatedUserId);
    @Query("SELECT ur FROM UserRelationship ur " +
            "WHERE ur.userId = :id OR ur.relatedUserId = :id")
    List<UserRelationship> findByUserIdOrRelatedUserId(UUID id);
}
