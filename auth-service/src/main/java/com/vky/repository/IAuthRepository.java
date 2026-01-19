package com.vky.repository;

import com.vky.repository.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IAuthRepository extends JpaRepository<Auth, UUID> {
    Optional<Auth> findByEmailIgnoreCase(String email);
    Optional<Auth> findAuthByAndEmailIgnoreCase(String email);
}
