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
@Where(clause = "is_deleted = false")
@EqualsAndHashCode(callSuper = true)
public class UserKey extends BaseEntity{
    //Lob büyük veri setleri için kullanılır ve Fetch.LAZY gibi davranır
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "public_key")
    private byte[] publicKey;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "encrypted_private_key")
    private byte[] encryptedPrivateKey;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "salt")
    private byte[] salt;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "iv")
    private byte[] iv;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserProfile user;
}
