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
import java.util.concurrent.CompletableFuture;

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
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getUserWithUserKeyByAuthId(@RequestBody @Valid FindUserProfileByAuthIdRequestDTO requestDTO) {
        UserProfileResponseDTO responseDTO = userProfileService.findWithUserKeyByAuthId(requestDTO.getAuthId());
        return ResponseEntity.ok(new ApiResponse<>(true, "success", responseDTO));
    }

    @PutMapping("/update-user-name")
    public ResponseEntity<ApiResponse<UpdateUserDTO>>  updateUserName(@RequestBody @Valid UpdateUserDTO dto)
    {
        return ResponseEntity.ok(new ApiResponse<>(true, "success", userProfileService.updateUserName(dto)));

    }

    @PutMapping("/update-user-about")
    public ResponseEntity<ApiResponse<UpdateUserDTO>>  userAboutUpdate(@RequestBody @Valid UpdateUserDTO dto)
    {
        return ResponseEntity.ok(new ApiResponse<>(true, "success", userProfileService.updateUserAbout(dto)));
    }

    @PutMapping("/update-user-last-seen")
    public ResponseEntity<Void> updateUserLastSeen(@RequestBody UserLastSeenRequestDTO userLastSeenRequestDTO) {
        this.userProfileService.updateUserLastSeen(userLastSeenRequestDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/get-user-last-seen")
    public UserLastSeenResponseDTO getUserLastSeen(@RequestParam UUID userId) {
        UserLastSeenResponseDTO response = this.userProfileService.getUserLastSeen(userId);
        return response;
    }

    @PostMapping("/get-user-with-privacy-settings-by-token")
    public ResponseEntity<UserProfileResponseDTO> getUserWithPrivacySettingsByToken(@RequestBody UserIdRequestDTO userIdRequestDTO) {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(userIdRequestDTO.getToken());
        UserProfileResponseDTO userProfileResponseDTO = userProfileService.getUserById(tokenResponseDto.getUserId());
        return ResponseEntity.ok(userProfileResponseDTO);
    }

    @PostMapping("/feign-get-user-by-id")
    public UserProfileResponseDTO getFeignUserById(@RequestBody UUID userId) {
        return this.userProfileService.getUserById(userId);
    }
    @PostMapping("/get-userEmail-ById")
    public String getUserEmailById(@RequestBody UUID userId) {
        UserProfileResponseDTO userProfileResponseDTO = this.userProfileService.getUserById(userId);
        return userProfileResponseDTO.getEmail();
    }
    @GetMapping("/get-user-email-by-id")
    public String getUserEmailByIdd(@RequestParam("id") UUID id) {
        UserProfileResponseDTO userProfileResponseDTO = this.userProfileService.getUserById(id);
        return userProfileResponseDTO.getEmail();
    }
    @PostMapping("/get-users-of-contacts")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getUsersOfContacts(@RequestBody List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        return this.userProfileService.getUsersOfContactsAsync(userProfileRequestDTOList);
    }

    @PostMapping("/get-users-of-chats")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getUsersOfChats(@RequestBody List<ContactWithRelationshipDTO> userProfileRequestDTOList) {
        return this.userProfileService.getUsersOfChatsAsync(userProfileRequestDTOList);
    }

    @PutMapping("/{userId}/privacy-settings")
    public ResponseEntity<UserProfileResponseDTO> updatePrivacySettings(
            @PathVariable UUID userId,
            @RequestBody PrivacySettingsRequestDTO privacySettingsRequestDTO) {

        UserProfileResponseDTO response = userProfileService.updatePrivacySettings(userId, privacySettingsRequestDTO);
        System.out.println("RESPONSE > " + response);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{userId}/upload-profile-picture")
    public ResponseEntity<ApiResponse<UserProfilePhotoURLResponseDTO>> uploadProfilePicture(@PathVariable UUID userId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(new ApiResponse<>(true,"success",userProfileService.uploadProfilePhoto(userId, file)));
    }


}

