package com.vky.controller;

import com.vky.dto.request.NewUserCreateDTO;
import com.vky.exception.ErrorType;
import com.vky.exception.UserManagerException;
import com.vky.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(("/api/v1/user"))
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;

    @PostMapping("/create-new-user")
    public ResponseEntity<Boolean> newUserCreate(@RequestBody @Valid NewUserCreateDTO userCreateDto)
    {
        System.out.println(userCreateDto.toString());
        try {
            userProfileService.createUserProfile(userCreateDto);
            return ResponseEntity.ok(true);
        }catch (Exception e)
        {
            throw new UserManagerException(ErrorType.USER_DONT_CREATE);
        }
    }

}
