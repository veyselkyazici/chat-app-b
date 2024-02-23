package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.exception.ErrorType;
import com.vky.exception.UserManagerException;
import com.vky.manager.IAuthManager;
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

        TokenResponseDTO tokenResponseDto = userProfileService.authenticate(authorization);
        if (tokenResponseDto.getTokenIsValid()) {
            userProfileService.updateUserName(tokenResponseDto.getAuthId(), dto.getName());
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }
    @PutMapping("/update-user-surname")
    public ResponseEntity<Boolean>  userSurnameUpdate(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization,
                                                            @RequestBody UpdateUserSurnameRequestDTO dto)
    {
        System.out.println(dto.getSurname());
        TokenResponseDTO tokenResponseDto = userProfileService.authenticate(authorization);
        if (tokenResponseDto.getTokenIsValid()) {
            userProfileService.updateUserSurname(tokenResponseDto.getAuthId(), dto.getSurname());
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @PutMapping("/update-user-phone")
    public ResponseEntity<Boolean>  userPhoneUpdate(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization,
                                                            @RequestBody UpdateUserPhone dto)
    {
        TokenResponseDTO tokenResponseDto = userProfileService.authenticate(authorization);
        if (tokenResponseDto.getTokenIsValid()) {
            userProfileService.updateUserPhone(tokenResponseDto.getAuthId(), dto.getPhoneNumber());
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }
    
    @PutMapping("/update-user-about")
    public ResponseEntity<Boolean>  userAboutUpdate(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization,
                                                            @RequestBody UpdateUserAbout body)
    {
        System.out.println("ABOUT: " + body.getAbout());
        TokenResponseDTO tokenResponseDto = userProfileService.authenticate(authorization);
        if (tokenResponseDto.getTokenIsValid()) {
            userProfileService.updateUserAbout(tokenResponseDto.getAuthId(), body.getAbout());
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @PostMapping("/find-by-keyword-ignore-case-users")
    public ResponseEntity<List<UserProfileDTO>> findByKeywordIgnoreCaseUsers(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorization, @RequestBody SearchDTO search) {

        TokenResponseDTO tokenResponseDto = userProfileService.authenticate(authorization);
        if (tokenResponseDto.getTokenIsValid()) {
            List<UserProfileDTO> userProfileDTOList = userProfileService.findByKeywordIgnoreCaseUsers(search.getEmailOrFirstNameOrLastName());
            return ResponseEntity.ok(userProfileDTOList);
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/find-ids")
    public FeignClientIdsResponseDTO findIds(@RequestBody FeignClientIdsRequestDTO dto) {
        System.out.println(dto);
        TokenResponseDTO tokenResponseDto = userProfileService.authenticate(dto.getToken());
        if (tokenResponseDto.getTokenIsValid()) {
            return userProfileService.findIds(dto.getEmail(), tokenResponseDto.getAuthId());
        }
        return null;
    }
    @PostMapping("/get-user-id")
    public UUID getUserId(@RequestBody String token) {
        TokenResponseDTO tokenResponseDto = userProfileService.authenticate(token);
        if (tokenResponseDto.getTokenIsValid()) {
            return this.userProfileService.getUserId(tokenResponseDto.getAuthId());
        }
        return null;
    }
    @PostMapping("/get-user-list")
    public List<FeignClientUserProfileResponseDTO> getUserList(@RequestBody List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        return this.userProfileService.getUserList(userProfileRequestDTOList);
    }
}

