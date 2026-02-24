package com.vky.service;

import com.vky.dto.request.PrivacySettingsRequestDTO;
import com.vky.dto.request.UpdateUserDTO;
import com.vky.dto.request.UpdateUserProfileDTO;
import com.vky.dto.request.UpdateSettingsDTO;
import com.vky.dto.response.*;
import com.vky.rabbitmq.model.CreateUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IUserProfileService {
    void createUserProfile(CreateUser createUser);

    UserProfileResponseDTO findWithUserKeyByAuthId(String tokenUserId);

    UpdateUserDTO updateUserName(UpdateUserDTO dto, String tokenUserId);

    UpdateUserDTO updateUserAbout(UpdateUserDTO dto, String tokenUserId);

    UpdateUserProfileDTO updateUserProfile(UpdateUserProfileDTO dto, String tokenUserId);

    List<ContactResponseDTO> getUsers(List<UUID> ids, String requesterId);

    UserProfileResponseDTO getUserById(UUID userId, String requesterId);

    UserProfileResponseDTO getUserById(UUID userId);

    UserProfileResponseDTO getUserByEmail(String contactEmail, String requesterId);

    void updateUserLastSeen(UUID userId, String lastSeen);

    UpdateSettingsDTO updatePrivacySettings(PrivacySettingsRequestDTO privacySettingsRequestDTO, String tokenUserId);

    UserProfilePhotoURLResponseDTO uploadProfilePhoto(MultipartFile file, String tokenUserId);

    String extractPublicIdFromUrl(String profilePictureUrl);

    void resetUserKey(ResetUserKeyDTO resetUserKeyDTO);

    void removeProfilePicture(String tokenUserId);

    PrivacySettingsResponseDTO getPrivacySettings(String userId);

    LastSeenDTO getLastSeen(String targetUserId, String requesterId);
}
