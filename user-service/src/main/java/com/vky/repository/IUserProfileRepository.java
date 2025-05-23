package com.vky.repository;

import com.vky.dto.response.FindUserProfileByAuthIdResponseDTO;
import com.vky.repository.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByAuthId(UUID authId);

    @Query("SELECT new com.vky.dto.response.FindUserProfileByAuthIdResponseDTO(u.authId, u.email, u.firstName, u.updatedAt) FROM UserProfile u WHERE u.authId = :authId")
    Optional<FindUserProfileByAuthIdResponseDTO> findDtoByAuthId(UUID authId);

    Optional<UserProfile> findUserProfileByEmailIgnoreCase(String email);
    // JPQL entity adı ve field adları ile çalışır
    // LAZY alanlar için ya servis katmanında @Transactional(hibernate session açık tutar) veya burada @EntityGraph(ilişkileri EAGER yükler(@Lob alanlarda çalışmaz (Lob alanlar varsayılan LAZY)))kullanılmalı
    @Query("SELECT u FROM UserProfile u WHERE u.id IN :ids")
    List<UserProfile> findUsersByIdList(@Param("ids") List<UUID> userIdList);

    Optional<UserProfile> findWithUserKeyByAuthId(UUID authId);

    // Native Query tablo ve kolon adlarını kullanır @EntityGraph çalışmaz
    @Query(value = "SELECT * FROM users WHERE id IN :ids", nativeQuery = true)
    List<UserProfile> findUsersByIdListNative(@Param("ids") List<UUID> userIdList);
}
