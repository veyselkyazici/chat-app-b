package com.vky.repository;

import com.vky.repository.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("SELECT COUNT(a)>0 FROM UserProfile a WHERE  a.authId = ?1")
    Boolean isExists(UUID authid);

    Optional<UserProfile> findOptionalByAuthId(UUID authId);

}
