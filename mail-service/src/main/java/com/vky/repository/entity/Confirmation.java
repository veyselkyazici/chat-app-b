package com.vky.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@Table(name  = "confirmations")
@EqualsAndHashCode(callSuper = false)
public class Confirmation extends BaseEntityEmail{
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
