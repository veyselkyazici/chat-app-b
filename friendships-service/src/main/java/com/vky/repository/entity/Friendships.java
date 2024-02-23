package com.vky.repository.entity;

import com.vky.repository.FriendshipStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Table(name = "friendships")
@Entity
@EqualsAndHashCode(callSuper = false)
public class Friendships extends BaseEntity{
    private UUID userId;
    private String userEmail;
    private UUID friendUserId;
    private String friendUserEmail;
    @Enumerated(EnumType.STRING)
    @Column(name = "friendship_status")
    private FriendshipStatus friendshipStatus;
}
