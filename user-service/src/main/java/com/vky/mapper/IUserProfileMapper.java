package com.vky.mapper;

import com.vky.dto.request.UpdateSettingsDTO;
import com.vky.dto.response.*;
import com.vky.repository.entity.PrivacySettings;
import com.vky.repository.entity.UserKey;
import com.vky.repository.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IUserProfileMapper {
    IUserProfileMapper INSTANCE = Mappers.getMapper(IUserProfileMapper.class);
    FindUserProfileByAuthIdResponseDTO toFindUserProfileResponseDTO(UserProfile userProfile);
    PrivacySettingsResponseDTO toPrivacySettingsResponseDTO(PrivacySettings privacySettings);

    @Mapping(target = "publicKey", expression = "java(mapByteArrayToBase64(userKey.getPublicKey()))")
    @Mapping(target = "encryptedPrivateKey", expression = "java(mapByteArrayToBase64(userKey.getEncryptedPrivateKey()))")
    @Mapping(target = "salt", expression = "java(mapByteArrayToBase64(userKey.getSalt()))")
    @Mapping(target = "iv", expression = "java(mapByteArrayToBase64(userKey.getIv()))")
    UserKeyResponseDTO toUserKeyResponseDTO(UserKey userKey);


    UpdateSettingsDTO toUserProfileWithoutKeyDTO(UserProfile userProfile);


    @Mapping(target = "userProfileResponseDTO", source = "userProfile")
    ContactResponseDTO toFeignClientResponse(UserProfile userProfile);

    default UserProfileResponseDTO toUserProfileDTO(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        return UserProfileResponseDTO.builder()
                .id(userProfile.getId())
                .email(userProfile.getEmail())
                .about(userProfile.getAbout())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .image(userProfile.getImage())
                .updatedAt(userProfile.getUpdatedAt())
                .privacySettings(toPrivacySettingsResponseDTO(userProfile.getPrivacySettings() != null ? userProfile.getPrivacySettings() : null))
                .userKey(userProfile.getUserKey() != null ? toUserKeyResponseDTO(userProfile.getUserKey()) : null)
                .build();
    }

    default List<ContactResponseDTO> toFeignClientResponseList(List<UserProfile> userProfiles) {
        if (userProfiles == null) {
            return Collections.emptyList();
        }
        return userProfiles.stream()
                .filter(Objects::nonNull)
                .map(this::toFeignClientResponse)
                .collect(Collectors.toList());
    }

    default String mapByteArrayToBase64(byte[] bytes) {
        return bytes != null ? Base64.getEncoder().encodeToString(bytes) : null;
    }
}
