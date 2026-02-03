package com.vky.controller;

import com.vky.dto.request.PrivacySettingsRequestDTO;
import com.vky.dto.request.UpdateSettingsDTO;
import com.vky.dto.request.UpdateUserDTO;
import com.vky.dto.request.UpdateUserProfileDTO;
import com.vky.dto.response.*;
import com.vky.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;

    @GetMapping("/get-user")
    public ResponseEntity<UserProfileResponseDTO> getUserByEmail(@RequestParam String contactEmail) {
        return ResponseEntity.ok(userProfileService.getUserByEmail(contactEmail));
    }

    @PostMapping("/get-user-with-user-key-by-auth-id")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getUserWithUserKeyByAuthId(
            @RequestHeader("X-Id") String tokenUserId) {
        UserProfileResponseDTO responseDTO = userProfileService.findWithUserKeyByAuthId(tokenUserId);
        System.out.println(responseDTO.userKey());
        return ResponseEntity.ok(new ApiResponse<>(true, "success", responseDTO));
    }

    @PutMapping("/update-user-name")
    public ResponseEntity<ApiResponse<UpdateUserDTO>> updateUserName(@RequestBody @Valid UpdateUserDTO dto,
            @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity
                .ok(new ApiResponse<>(true, "success", userProfileService.updateUserName(dto, tokenUserId)));

    }

    @PutMapping("/update-user-about")
    public ResponseEntity<ApiResponse<UpdateUserDTO>> userAboutUpdate(@RequestBody @Valid UpdateUserDTO dto,
            @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity
                .ok(new ApiResponse<>(true, "success", userProfileService.updateUserAbout(dto, tokenUserId)));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<UpdateUserProfileDTO>> updateProfile(
            @RequestBody @Valid UpdateUserProfileDTO dto,
            @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity
                .ok(new ApiResponse<>(true, "success", userProfileService.updateUserProfile(dto, tokenUserId)));
    }

    @PostMapping("/get-user-by-id")
    public UserProfileResponseDTO getFeignUserById(@RequestBody UUID userId) {
        return this.userProfileService.getUserById(userId);
    }

    @GetMapping("/get-user-email-by-id")
    public String getUserEmailById(@RequestParam("id") UUID id) {
        UserProfileResponseDTO userProfileResponseDTO = this.userProfileService.getUserById(id);
        return userProfileResponseDTO.email();
    }

    @PostMapping("/get-users")
    public List<ContactResponseDTO> getUsersOfContacts(@RequestBody List<UUID> ids) {
        return this.userProfileService.getUsers(ids);
    }

    @PostMapping("/get-user-by-id-with-out-user-key")
    public UpdateSettingsDTO getFeignUserByIdWithOutUserKey(@RequestBody UUID userId) {
        return this.userProfileService.getFeignUserByIdWithOutUserKey(userId);
    }

    @PutMapping("/privacy-settings")
    public ResponseEntity<ApiResponse<UpdateSettingsDTO>> updatePrivacySettings(
            @RequestBody PrivacySettingsRequestDTO privacySettingsRequestDTO,
            @RequestHeader("X-Id") String tokenUserId) {

        UpdateSettingsDTO response = userProfileService.updatePrivacySettings(privacySettingsRequestDTO,
                tokenUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "success", response));
    }

    @PutMapping("/{userId}/last-seen")
    public void updateLastSeen(@PathVariable String userId, @RequestBody LastSeenDTO req) {
        userProfileService.updateUserLastSeen(UUID.fromString(userId), req.lastSeen());
    }

    @GetMapping("/api/v1/users/{userId}/last-seen")
    LastSeenDTO getLastSeen(@PathVariable String userId) {
        return userProfileService.getLastSeen(userId);
    }

    @GetMapping("/{userId}/privacy")
    public ResponseEntity<PrivacySettingsResponseDTO> getPrivacy(
            @PathVariable String userId) {
        PrivacySettingsResponseDTO dto = userProfileService.getPrivacySettings(userId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<ApiResponse<UserProfilePhotoURLResponseDTO>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file, @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity
                .ok(new ApiResponse<>(true, "success", userProfileService.uploadProfilePhoto(file, tokenUserId)));
    }

    @PostMapping("/reset-user-key")
    void resetUserKey(@RequestBody ResetUserKeyDTO resetUserKeyDTO) {
        this.userProfileService.resetUserKey(resetUserKeyDTO);
    }

    @PatchMapping("/remove-profile-picture")
    public ResponseEntity<ApiResponse<Void>> removeProfilePicture(@RequestHeader("X-Id") String tokenUserId) {
        userProfileService.removeProfilePicture(tokenUserId);

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "Profile picture removed successfully",
                null);

        return ResponseEntity.ok(response);
    }
}
