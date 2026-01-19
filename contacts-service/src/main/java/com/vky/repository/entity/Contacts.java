package com.vky.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "contacts")
@Entity
@EqualsAndHashCode(callSuper = true)
public class Contacts extends BaseEntity{
    private UUID userId;
    private String userEmail;
    private UUID userContactId;
    private String userContactName;
    private String userContactEmail;





//    private UUID friendId;
//    private String friendEmail;
//    @Enumerated(EnumType.STRING)
//    @Column(name = "friendship_status")
//    private FriendshipStatus friendshipStatus;
}
