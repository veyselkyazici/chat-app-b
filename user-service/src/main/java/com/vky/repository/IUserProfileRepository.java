package com.vky.repository;

import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.repository.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserProfileRepository extends JpaRepository<UserProfile, UUID> {

    @Query("SELECT COUNT(a)>0 FROM UserProfile a WHERE  a.authId = ?1")
    Boolean isExists(UUID authid);

    Optional<UserProfile> findByAuthId(UUID authId);
    Optional<UserProfile> findUserProfileByEmailIgnoreCase(String email);


    UserProfile findByEmailIgnoreCase(String email);
    @Query("SELECT u FROM UserProfile u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserProfile> findByKeywordIgnoreCaseUsers(@Param("keyword") String keyword);

}
