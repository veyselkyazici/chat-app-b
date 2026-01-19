package com.vky.repository.entity;

import com.vky.repository.entity.enums.VisibilityOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Table(name = "privacy_settings")
@Entity
@EqualsAndHashCode(callSuper = true)
public class PrivacySettings extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_photo_visibility")
    private VisibilityOption profilePhotoVisibility = VisibilityOption.EVERYONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_seen_visibility")
    private VisibilityOption lastSeenVisibility = VisibilityOption.EVERYONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "online_status_visibility")
    private VisibilityOption onlineStatusVisibility = VisibilityOption.EVERYONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "about_visibility")
    private VisibilityOption aboutVisibility = VisibilityOption.EVERYONE;

    @OneToOne(mappedBy = "privacySettings")
    private UserProfile userProfile;

    @Column(name = "read_receipts")
    private boolean readReceipts = true;
}
