package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.exception.ErrorType;
import com.vky.exception.UserManagerException;
import com.vky.mapper.IUserProfileMapper;
import com.vky.repository.entity.UserProfile;
import com.vky.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;


    @GetMapping("/hello")
    public ResponseEntity<String> sayHello()
    {
        return ResponseEntity.ok("Hello, ");
    }
    @GetMapping("/hello1")
    public ResponseEntity<UserProfile> sayHello1()
    {
        List<UserProfile> userProfiles = userProfileService.getUsers();
        System.out.println(userProfiles.get(0));
        System.out.println(userProfiles.get(0).getPrivacySettings()); // PrivacySettings(profilePhotoVisibility=EVERYONE, lastSeenVisibility=EVERYONE, onlineStatusVisibility=EVERYONE, readReceipts=true)
        System.out.println(userProfiles.get(0)); // UserProfile(authId=a0e09c3d-0d9e-43a0-9541-c7823653ec96, email=veyselkaraniyazici@gmail.com, firstName=Veysel Karani YAZICI, lastName=null, phone=null, about=Meşgul, status=null, lastSeen=2024-09-06T19:16:36.619169, image=null, privacySettings=PrivacySettings(profilePhotoVisibility=EVERYONE, lastSeenVisibility=EVERYONE, onlineStatusVisibility=EVERYONE, readReceipts=true))
        return ResponseEntity.ok().body(userProfiles.get(0));
    }
    @PostMapping("/create-new-user")
    public ResponseEntity<Boolean> newUserCreate(@RequestBody @Valid NewUserCreateDTO userCreateDto) {
        try {
            userProfileService.createUserProfile(userCreateDto);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            throw new UserManagerException(ErrorType.USER_DONT_CREATE);
        }
    }
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

    @PostMapping("/find-by-keyword-ignore-case-users")
    public ResponseEntity<List<UserProfileResponseDTO>> findByKeywordIgnoreCaseUsers(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization, @RequestBody SearchDTO search) {

        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);
            List<UserProfileResponseDTO> userProfileDTOList = userProfileService.findByKeywordIgnoreCaseUsers(search.getEmailOrFirstNameOrLastName());
            return ResponseEntity.ok(userProfileDTOList);
    }

    @GetMapping("/feign-client-get-userId")
    public TokenResponseDTO feignClientGetUserByToken(@RequestHeader("Authorization") String authorization) {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);
            return tokenResponseDto;
    }

    @PostMapping("/get-userId")
    public ResponseEntity<UserIdResponseDTO> getUserIdByToken(@RequestBody UserIdRequestDTO userIdRequestDTO) {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(userIdRequestDTO.getToken());
            UserIdResponseDTO userIdResponseDto = new UserIdResponseDTO();
            userIdResponseDto.setUserId(tokenResponseDto.getUserId());
            return ResponseEntity.ok(userIdResponseDto);
    }

    @PostMapping("/get-user-with-privacy-settings-by-token")
    public ResponseEntity<UserProfileResponseDTO> getUserWithPrivacySettingsByToken(@RequestBody UserIdRequestDTO userIdRequestDTO) {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(userIdRequestDTO.getToken());
        UserProfileResponseDTO userProfileResponseDTO = userProfileService.getUserById(tokenResponseDto.getUserId());
        return ResponseEntity.ok(userProfileResponseDTO);
    }

    @PostMapping("/get-user-by-id")
    public ResponseEntity<UserProfileResponseDTO> getUserById(@RequestBody UUID userId) {
        UserProfileResponseDTO userProfileResponseDTO = this.userProfileService.getUserById(userId);
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
    @PostMapping("/get-user-list")
    public List<FeignClientUserProfileResponseDTO> getUserList(@RequestBody List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        return this.userProfileService.getUserList(userProfileRequestDTOList);
    }

    @PostMapping("/get-user-listt")
    public List<FeignClientUserProfileResponseDTO> getUserListt(@RequestBody List<ContactWithRelationshipDTO> userProfileRequestDTOList) {
        return this.userProfileService.getUserListt(userProfileRequestDTOList);
    }
    @GetMapping("/feign-client-get-user")
    public TokenResponseDTO feignClientGetUser(@RequestHeader("Authorization") String authorization) {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);
        return tokenResponseDto;
    }


    @PutMapping("/{userId}/privacy-settings")
    public ResponseEntity<UserProfileResponseDTO> updatePrivacySettings(
            @PathVariable UUID userId,
            @RequestBody PrivacySettingsRequestDTO privacySettingsRequestDTO) {

        UserProfileResponseDTO response = userProfileService.updatePrivacySettings(userId, privacySettingsRequestDTO);
        System.out.println("RESPONSE > " + response);
        return ResponseEntity.ok(response);
    }
}

