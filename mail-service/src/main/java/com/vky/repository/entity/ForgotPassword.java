package com.vky.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@Entity
@Table(name  = "forgot_password")
@EqualsAndHashCode(callSuper = false)
public class ForgotPassword extends BaseEntityEmail{
    private String password;
    private String newPassword;
    private LocalDateTime expiryDate;
}
