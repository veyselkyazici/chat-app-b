package com.vky.repository;

import com.vky.repository.entity.PrivacySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface IPrivacySettingsRepository extends JpaRepository<PrivacySettings, UUID> {
}
