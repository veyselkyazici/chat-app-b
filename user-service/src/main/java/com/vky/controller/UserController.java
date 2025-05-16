package com.vky.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.exception.ErrorType;
import com.vky.exception.UserManagerException;
import com.vky.mapper.IUserProfileMapper;
import com.vky.repository.entity.UserProfile;
import com.vky.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;


//    @PostMapping("/create-new-user")
//    public ResponseEntity<Boolean> newUserCreate(@RequestBody @Valid NewUserCreateDTO userCreateDto) {
//        try {
//            userProfileService.createUserProfile(userCreateDto);
//            return ResponseEntity.ok(true);
//        } catch (Exception e) {
//            throw new UserManagerException(ErrorType.USER_DONT_CREATE);
//        }
//    }
    @GetMapping("/get-user")
    public ResponseEntity<UserProfileResponseDTO> getUserByEmail(@RequestParam String contactEmail) {
        return ResponseEntity.ok(userProfileService.getUserByEmail(contactEmail));
    }

    @PostMapping("/find-by-authId")
    public ResponseEntity<FindUserProfileByAuthIdResponseDTO> findByAuthId(@RequestBody FindUserProfileByAuthIdRequestDTO requestDTO) {
        FindUserProfileByAuthIdResponseDTO responseDTO = userProfileService.findByAuthId(requestDTO.getAuthId());
        if (responseDTO != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/get-user-with-user-key-by-auth-id")
    public ResponseEntity<UserProfileResponseDTO> getUserWithUserKeyByAuthId(@RequestBody FindUserProfileByAuthIdRequestDTO requestDTO) {
        UserProfileResponseDTO responseDTO = userProfileService.findWithUserKeyByAuthId(requestDTO.getAuthId());

        if (responseDTO != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update-user-name")
    public ResponseEntity<Boolean>  updateUserName(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization,
                                                      @RequestBody UpdateUserNameRequestDTO dto)
    {

        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);

            userProfileService.updateUserName(tokenResponseDto.getAuthId(), dto.getName());
            return ResponseEntity.ok(true);

    }
    @PutMapping("/update-user-surname")
    public ResponseEntity<Boolean>  userSurnameUpdate(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization,
                                                            @RequestBody UpdateUserSurnameRequestDTO dto)
    {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);

            userProfileService.updateUserSurname(tokenResponseDto.getAuthId(), dto.getSurname());
            return ResponseEntity.ok(true);
    }

    @PutMapping("/update-user-phone")
    public ResponseEntity<Boolean>  userPhoneUpdate(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization,
                                                            @RequestBody UpdateUserPhone dto)
    {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);

            userProfileService.updateUserPhone(tokenResponseDto.getAuthId(), dto.getPhoneNumber());
            return ResponseEntity.ok(true);

    }
    
    @PutMapping("/update-user-about")
    public ResponseEntity<Boolean>  userAboutUpdate(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization,
                                                            @RequestBody UpdateUserAbout body)
    {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);

            userProfileService.updateUserAbout(tokenResponseDto.getAuthId(), body.getAbout());
            return ResponseEntity.ok(true);
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
//    @PostMapping("/get-user-list")
//    public List<FeignClientUserProfileResponseDTO> getUserList(@RequestBody List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
//        return this.userProfileService.getUserList(userProfileRequestDTOList);
//    }
    @PostMapping("/get-users-of-contacts")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getUsersOfContacts(@RequestBody List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        return this.userProfileService.getUsersOfContactsAsync(userProfileRequestDTOList);
    }
//    @PostMapping("/get-user-listt")
//    public List<FeignClientUserProfileResponseDTO> getUserListt(@RequestBody List<ContactWithRelationshipDTO> userProfileRequestDTOList) {
//        return this.userProfileService.getUserListt(userProfileRequestDTOList);
//    }
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
    public ResponseEntity<String> uploadProfilePicture(@PathVariable UUID userId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userProfileService.uploadProfilePicture(userId, file));
    }


}

