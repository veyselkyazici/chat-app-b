package com.vky.repository;

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

    Optional<UserProfile> findUserProfileByEmailIgnoreCaseAndIsDeletedFalse(String email);
    // JPQL entity adı ve field adları ile çalışır
    // LAZY alanlar için ya servis katmanında @Transactional(hibernate session açık tutar) veya burada @EntityGraph(ilişkileri EAGER yükler(@Lob alanlarda çalışmaz (Lob alanlar varsayılan LAZY)))kullanılmalı
    @Query("SELECT u FROM UserProfile u WHERE u.id IN :ids AND u.isDeleted = false")
    List<UserProfile> findUsersByIdList(@Param("ids") List<UUID> userIdList);

    Optional<UserProfile> findWithUserKeyByAuthIdAndIsDeletedFalse(UUID authId);

    // Native Query tablo ve kolon adlarını kullanır @EntityGraph(Eager loading için kullanılır) çalışmaz
    @Query(value = "SELECT * FROM users WHERE id IN :ids", nativeQuery = true)
    List<UserProfile> findUsersByIdListNative(@Param("ids") List<UUID> userIdList);
}
