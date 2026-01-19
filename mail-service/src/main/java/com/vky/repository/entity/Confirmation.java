package com.vky.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@Table(name  = "confirmations")
@EqualsAndHashCode(callSuper = true)
public class Confirmation extends BaseEntityEmail{

    private String verificationToken;
    private UUID authId;
    private boolean isUsed;
    private String email;
    private Instant expiresAt;
    @Override
    public String toString() {
        return "Confirmation{" +
                "id=" + getId() +
                ", verificationToken='" + getVerificationToken() + "'" +
                ", authId=" + getAuthId() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
