package com.vky.repository;

import com.vky.repository.entity.UserKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IUserKeyRepository extends JpaRepository<UserKey, UUID> {
}
