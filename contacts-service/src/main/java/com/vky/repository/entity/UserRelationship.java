package com.vky.repository.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "user_relationships")
public class UserRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID userId;
    private UUID relatedUserId;

    private boolean userHasAddedRelatedUser;
    private boolean relatedUserHasAddedUser;

    // Diğer gerekli alanlar
}
