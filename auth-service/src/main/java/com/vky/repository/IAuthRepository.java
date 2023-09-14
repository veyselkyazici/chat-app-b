package com.vky.repository;

import com.vky.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface IAuthRepository extends JpaRepository<Auth, UUID> {
    //Optional<Auth> findOptionalByUsernameIgnoreCaseAndPassword(String username, String password);
    Optional<Auth> findByEmail(String email);
    @Query("SELECT COUNT(a)>0 FROM Auth a WHERE (a.email) = (?1)")
    Boolean isExistsEmail(String email);
}
