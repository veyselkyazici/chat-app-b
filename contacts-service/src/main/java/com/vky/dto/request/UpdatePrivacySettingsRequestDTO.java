package com.vky.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vky.dto.response.PrivacySettingsResponseDTO;
import com.vky.dto.response.UserKeyResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdatePrivacySettingsRequestDTO {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String imagee;
        private String about;
        private String updatedAt;
        private PrivacySettingsResponseDTO privacySettings;
        private UserKeyResponseDTO userKey;
}
