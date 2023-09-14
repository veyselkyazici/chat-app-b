package com.vky.service;

import com.vky.dto.request.NewUserCreateDTO;
import com.vky.repository.IUserProfileRepository;
import com.vky.repository.entity.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    private final IUserProfileRepository userProfileRepository;

    public UserProfileService(IUserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile createUserProfile(NewUserCreateDTO userCreateDto)
    {
        return userProfileRepository.save(UserProfile.builder()
            .authId(userCreateDto.getAuthId())
            .build());
    }

    public void saveUser(UserProfile userProfile) {
        userProfileRepository.save(userProfile);
    }


}
