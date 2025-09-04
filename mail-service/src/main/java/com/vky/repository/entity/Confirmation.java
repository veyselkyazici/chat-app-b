package com.vky.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
