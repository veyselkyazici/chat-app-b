package com.vky.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Table(name = "user_keys")
@Entity
@EqualsAndHashCode(callSuper = true)
public class UserKey extends BaseEntity{
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "public_key", nullable = false)
    private byte[] publicKey;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "encrypted_private_key", nullable = false)
    private byte[] encryptedPrivateKey;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "salt", nullable = false)
    private byte[] salt;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "iv", nullable = false)
    private byte[] iv;

    @OneToOne(mappedBy = "userKey", fetch = FetchType.LAZY)
    private UserProfile user;
}
