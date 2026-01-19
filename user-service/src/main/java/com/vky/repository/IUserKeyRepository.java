package com.vky.repository;

import com.vky.repository.entity.UserKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IUserKeyRepository extends JpaRepository<UserKey, UUID> {
}
