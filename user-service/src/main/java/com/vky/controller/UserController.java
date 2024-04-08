package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.exception.ErrorType;
import com.vky.exception.UserManagerException;
import com.vky.manager.IAuthManager;
import com.vky.repository.entity.UserProfile;
import com.vky.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping(("/api/v1/user"))
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;

    @GetMapping("/hello")
    public ResponseEntity<String>  sayHello()
    {
        System.out.println("helloooo");
        return ResponseEntity.ok("Hello, ");
    }

    @PostMapping("/create-new-user")
    public ResponseEntity<Boolean> newUserCreate(@RequestBody @Valid NewUserCreateDTO userCreateDto) {
        System.out.println(userCreateDto.toString());
        try {
            userProfileService.createUserProfile(userCreateDto);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            throw new UserManagerException(ErrorType.USER_DONT_CREATE);
        }
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
        System.out.println(dto.getSurname());
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
        System.out.println("ABOUT: " + body.getAbout());
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);

            userProfileService.updateUserAbout(tokenResponseDto.getAuthId(), body.getAbout());
            return ResponseEntity.ok(true);
    }

    @PostMapping("/find-by-keyword-ignore-case-users")
    public ResponseEntity<List<UserProfileDTO>> findByKeywordIgnoreCaseUsers(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization, @RequestBody SearchDTO search) {

        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);
            List<UserProfileDTO> userProfileDTOList = userProfileService.findByKeywordIgnoreCaseUsers(search.getEmailOrFirstNameOrLastName());
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
            System.out.println("userIdResponseDto: " + userIdResponseDto);
            return ResponseEntity.ok(userIdResponseDto);
    }

    @PostMapping("/get-userEmail-ById")
    public String getUserEmailById(@RequestBody UUID userId) {
        UserProfile userProfile = this.userProfileService.getUserById(userId);
        return userProfile.getEmail();
    }

    @PostMapping("/get-user-list")
    public List<FeignClientUserProfileResponseDTO> getUserList(@RequestBody List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        return this.userProfileService.getUserList(userProfileRequestDTOList);
    }

    @GetMapping("/feign-client-get-user")
    public TokenResponseDTO feignClientGetUser(@RequestHeader("Authorization") String authorization) {
        TokenResponseDTO tokenResponseDto = userProfileService.tokenExractAuthId(authorization);
        return tokenResponseDto;
    }
}

