package com.vky.service;

import com.vky.dto.request.FeignClientIdsRequestDTO;
import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.request.NewUserCreateDTO;
import com.vky.dto.response.*;
import com.vky.exception.AuthenticationException;
import com.vky.exception.ErrorType;
import com.vky.mapper.IUserProfileMapper;
import com.vky.repository.IUserProfileRepository;
import com.vky.repository.entity.UserProfile;
import com.vky.utility.JwtTokenManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final IUserProfileRepository userProfileRepository;
    private final JwtTokenManager jwtTokenManager;

    public UserProfileService(IUserProfileRepository userProfileRepository, JwtTokenManager jwtTokenManager) {
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenManager = jwtTokenManager;
    }

    public void createUserProfile(NewUserCreateDTO userCreateDto) {
        userProfileRepository.save(UserProfile.builder()
                .authId(userCreateDto.getAuthId())
                .email(userCreateDto.getEmail())
                .build());
    }

//    public Boolean updateUserProfile(EditProfileRequestDto dto, Long authid){
//        UserProfile userProfile = IUserProfileMapper.INSTANCE.toUserProfile(dto);
//        UserProfile optionalUserProfile = repository.findByAuthid(authid);
//        if(optionalUserProfile==null) return false;
//        try{
//            userProfile.setId(optionalUserProfile.getId());
//            update(userProfile);
//            return true;
//        }catch (Exception e){
//            return false;
//        }
//    }

    public FindUserProfileByAuthIdResponseDTO findByAuthId(UUID authId) {
        return userProfileRepository.findByAuthId(authId)
                .map(IUserProfileMapper.INSTANCE::userProfileToDTO)
                .orElse(null);

    }

    public TokenResponseDTO authenticate(String authorization) {
        TokenResponseDTO responseDTO = new TokenResponseDTO();
        try {
            if (authorization == null || authorization.isEmpty()) {
                throw new AuthenticationException(ErrorType.AUTHORIZATION_EMPTY);
            }
            if (!authorization.startsWith("Bearer ")) {
                throw new AuthenticationException(ErrorType.INVALID_AUTHORIZATION_FORMAT);
            }
            String token = authorization.substring(7);
            System.out.println(this.jwtTokenManager.isValidToken(token));
            if (this.jwtTokenManager.isValidToken(token)) {
                UUID authId = jwtTokenManager.extractAuthId(token);
                String email = jwtTokenManager.extractUsername(token);
                responseDTO.setAuthId(authId);
                responseDTO.setEmail(email);
                responseDTO.setTokenIsValid(true);
                responseDTO.setMessage("Valid Token");
            } else {
                responseDTO.setAuthId(null);
                responseDTO.setEmail(null);
                responseDTO.setTokenIsValid(false);
                responseDTO.setMessage("Inalid Token");
            }
        } catch (Exception e) {
            responseDTO.setTokenIsValid(false);
            responseDTO.setMessage("Authentication failed: " + e.getMessage());
        }
        return responseDTO;
    }

    public void updateUserName(UUID authId, String name) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByAuthId(authId);
        userProfileOptional.ifPresent(userProfile -> {
            userProfile.setFirstName(name);
            userProfileRepository.save(userProfile);
        });
    }

    public void updateUserSurname(UUID authId, String surname) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByAuthId(authId);
        userProfileOptional.ifPresent(userProfile -> {
            userProfile.setLastName(surname);
            userProfileRepository.save(userProfile);
            System.out.println(userProfile.getLastName());
        });
    }

    public void updateUserPhone(UUID authId, String phoneNumber) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByAuthId(authId);
        userProfileOptional.ifPresent(userProfile -> {
            userProfile.setPhone(phoneNumber);
            userProfileRepository.save(userProfile);
        });
    }

    public void updateUserAbout(UUID authId, String about) {
        System.out.println("about: " + about);
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByAuthId(authId);
        userProfileOptional.ifPresent(userProfile -> {
            userProfile.setAbout(about);
            userProfileRepository.save(userProfile);
        });
    }

    public List<UserProfileDTO> findByKeywordIgnoreCaseUsers(String search) {
        List<UserProfile> userProfileList = userProfileRepository.findByKeywordIgnoreCaseUsers(search);
        System.out.println("userProfileList: " + userProfileList);
        return userProfileList.stream()
                .map(IUserProfileMapper.INSTANCE::toUserProfileDTO)
                .toList();
    }

    public FeignClientIdsResponseDTO findIds(String email, UUID authId) {
        Optional<UserProfile> userOptional = this.userProfileRepository.findByAuthId(authId);
        UserProfile friend = this.userProfileRepository.findByEmailIgnoreCase(email);
        if (userOptional.isEmpty() || friend == null) {
            return null;
        }
        UserProfile user = userOptional.get();
        return FeignClientIdsResponseDTO.builder()
                .userId(user.getId())
                .friendUserId(friend.getId())
                .friendUserEmail(friend.getEmail())
                .userEmail(user.getEmail())
                .build();
    }
    public UUID getUserId(UUID authId) {
        Optional<UserProfile> userProfile = this.userProfileRepository.findByAuthId(authId);
        return userProfile.get().getId();
    }

    public List<FeignClientUserProfileResponseDTO> getUserList(List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        List<UUID> userIdList = userProfileRequestDTOList.stream()
                .map(FeignClientUserProfileRequestDTO::getFriendUserId)
                .collect(Collectors.toList());

        List<UserProfile> userProfiles = this.userProfileRepository.findAllById(userIdList);

        return userProfiles.stream()
                .filter(Objects::nonNull)
                .map(userProfile -> FeignClientUserProfileResponseDTO.builder()
                        .id(userProfile.getId())
                        .friendName(null)
                        .about(userProfile.getAbout())
                        .email(userProfile.getEmail())
                        .imageId(null)
                        .build())
                .collect(Collectors.toList());
    }


}
