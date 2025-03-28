package com.vky.repository;

import com.vky.repository.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface IAuthRepository extends JpaRepository<Auth, UUID> {
    //Optional<Auth> findOptionalByUsernameIgnoreCaseAndPassword(String username, String password);
    Optional<Auth> findByEmailIgnoreCase(String email);
    Optional<Auth> findAuthByAndEmailIgnoreCase(String email);
    Optional<Auth> findAuthByEmailIgnoreCaseAndIsApprovedTrue(String email);
    @Query("SELECT COUNT(a)>0 FROM Auth a WHERE (a.email) = (?1)")
    Boolean isExistsEmail(String email);
}
