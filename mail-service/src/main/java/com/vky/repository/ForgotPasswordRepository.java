package com.vky.repository;

import com.vky.repository.entity.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {
    Optional<ForgotPassword> findFirstByAuthIdAndExpiryDateAfterOrderByExpiryDateDesc(UUID authId, LocalDateTime now);

    Optional<ForgotPassword> findForgotPasswordByAuthId(UUID authId);

}
