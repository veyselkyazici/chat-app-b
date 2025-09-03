package com.vky.repository;

import com.vky.repository.entity.PrivacySettings;
import com.vky.repository.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPrivacySettingsRepository extends JpaRepository<PrivacySettings, UUID> {
}
