package com.vky.repository;

import com.vky.repository.entity.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> {
    Optional<Confirmation> findByVerificationToken(String verificationToken);
    Confirmation findTopByEmailOrderByCreatedAtDesc(String email);
}
