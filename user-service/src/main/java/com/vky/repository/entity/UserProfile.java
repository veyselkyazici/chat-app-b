package com.vky.repository.entity;

import com.vky.repository.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@ToString(exclude = {"privacySettings","image"},callSuper = true)
@Table(name = "users")
@Entity
@Where(clause = "is_deleted = false")
@EqualsAndHashCode(exclude = {"privacySettings","image"},callSuper = true)
public class UserProfile extends BaseEntity{
    private UUID authId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String about;
    private Status status;
    private LocalDateTime lastSeen;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "privacy_settings_id", referencedColumnName = "id")
    private PrivacySettings privacySettings;
}
