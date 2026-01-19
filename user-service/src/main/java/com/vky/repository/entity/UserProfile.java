package com.vky.repository.entity;

import com.vky.repository.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@ToString(exclude = {"privacySettings","userKey"},callSuper = true)
@Table(name = "users")
@Entity
@EqualsAndHashCode(exclude = {"privacySettings", "userKey"},callSuper = true)
public class UserProfile extends BaseEntityManualId{
    private UUID authId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String about;
    private Status status;
    private Instant lastSeen;
    private String image;

    /** Default
    @OneToOne	EAGER
    @ManyToOne	EAGER

     @OneToMany	LAZY
    @ManyToMany	LAZY
    **/
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PrivacySettings privacySettings;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key_id", unique = true)
    private UserKey userKey;

}

