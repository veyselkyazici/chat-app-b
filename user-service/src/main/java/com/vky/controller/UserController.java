package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getUserWithUserKeyByAuthId(@RequestHeader("X-Id") String tokenUserId) {
        UserProfileResponseDTO responseDTO = userProfileService.findWithUserKeyByAuthId(tokenUserId);
        System.out.println(responseDTO.getUserKey());
        return ResponseEntity.ok(new ApiResponse<>(true, "success", responseDTO));
    }

    @PutMapping("/update-user-name")
    public ResponseEntity<ApiResponse<UpdateUserDTO>>  updateUserName(@RequestBody @Valid UpdateUserDTO dto,@RequestHeader("X-Id") String tokenUserId)
    {
        return ResponseEntity.ok(new ApiResponse<>(true, "success", userProfileService.updateUserName(dto,tokenUserId)));

    }

    @PutMapping("/update-user-about")
    public ResponseEntity<ApiResponse<UpdateUserDTO>>  userAboutUpdate(@RequestBody @Valid UpdateUserDTO dto,@RequestHeader("X-Id") String tokenUserId)
    {
        return ResponseEntity.ok(new ApiResponse<>(true, "success", userProfileService.updateUserAbout(dto,tokenUserId)));
    }

    @PutMapping("/internal/update-user-last-seen")
    public ResponseEntity<Void> updateUserLastSeen(
            @RequestBody UpdateLastSeenRequestDTO request
    ) {
        userProfileService.updateUserLastSeen(request.getUserId(), request.getLastSeen());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/get-user-last-seen")
    public UserLastSeenResponseDTO getUserLastSeen(@RequestParam UUID userId) {
        return this.userProfileService.getUserLastSeen(userId);
    }
    @PostMapping("/get-user-by-id")
    public UserProfileResponseDTO getFeignUserById(@RequestBody UUID userId) {
        return this.userProfileService.getUserById(userId);
    }

    @GetMapping("/get-user-email-by-id")
    public String getUserEmailById(@RequestParam("id") UUID id) {
        UserProfileResponseDTO userProfileResponseDTO = this.userProfileService.getUserById(id);
        return userProfileResponseDTO.getEmail();
    }
    @PostMapping("/get-users")
    public List<ContactResponseDTO> getUsersOfContacts(@RequestBody List<UUID> ids) {
        return this.userProfileService.getUsers(ids);
    }

    @PutMapping("/privacy-settings")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updatePrivacySettings(
            @RequestBody PrivacySettingsRequestDTO privacySettingsRequestDTO, @RequestHeader("X-Id") String tokenUserId) {

        UserProfileResponseDTO response = userProfileService.updatePrivacySettings(privacySettingsRequestDTO, tokenUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "success", response));
    }


    @PostMapping("/upload-profile-picture")
    public ResponseEntity<ApiResponse<UserProfilePhotoURLResponseDTO>> uploadProfilePicture(@RequestParam("file") MultipartFile file, @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity.ok(new ApiResponse<>(true,"success",userProfileService.uploadProfilePhoto(file, tokenUserId)));
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
                null
        );

        return ResponseEntity.ok(response);
    }
}

