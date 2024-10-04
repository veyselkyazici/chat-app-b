package com.vky.service;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.request.NewUserCreateDTO;
import com.vky.dto.request.PrivacySettingsRequestDTO;
import com.vky.dto.request.UserLastSeenRequestDTO;
import com.vky.dto.response.*;
import com.vky.mapper.IPrivactSettingsMapper;
import com.vky.mapper.IUserProfileMapper;
import com.vky.rabbitmq.producer.RabbitMQProducer;
import com.vky.repository.IUserProfileRepository;
import com.vky.repository.entity.PrivacySettings;
import com.vky.repository.entity.UserProfile;
import com.vky.utility.JwtTokenManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final IUserProfileRepository userProfileRepository;
    private final JwtTokenManager jwtTokenManager;
    private final RabbitMQProducer rabbitMQProducer;

    public UserProfileService(IUserProfileRepository userProfileRepository, JwtTokenManager jwtTokenManager, RabbitMQProducer rabbitMQProducer) {
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public void createUserProfile(NewUserCreateDTO userCreateDto) {

        UserProfile savedUserProfile = userProfileRepository.save(UserProfile.builder()
                .authId(userCreateDto.getAuthId())
                .email(userCreateDto.getEmail())
                .privacySettings(new PrivacySettings())
                .build());
        rabbitMQProducer.checkContactUser(savedUserProfile);
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

    public TokenResponseDTO tokenExractAuthId(String authorization) {
        TokenResponseDTO responseDTO = new TokenResponseDTO();
        String token = authorization.substring(7);
        UUID authId = jwtTokenManager.extractAuthId(token);
        Optional<UserProfile> userProfile = userProfileRepository.findByAuthId(authId);
        userProfile.ifPresent(user -> {
            responseDTO.setUserId(user.getId());
            responseDTO.setAuthId(user.getAuthId());
            responseDTO.setEmail(user.getEmail());
        });
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
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByAuthId(authId);
        userProfileOptional.ifPresent(userProfile -> {
            userProfile.setAbout(about);
            userProfileRepository.save(userProfile);
        });
    }

    public List<UserProfileResponseDTO> findByKeywordIgnoreCaseUsers(String search) {
        List<UserProfile> userProfileList = userProfileRepository.findByKeywordIgnoreCaseUsers(search);
        return userProfileList.stream()
                .map(IUserProfileMapper.INSTANCE::toUserProfileDTO)
                .toList();
    }


    //    public List<FeignClientUserProfileResponseDTO> getUserList(List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
//        System.out.println("REQUESTDTO > " + userProfileRequestDTOList);
//        Map<UUID, FeignClientUserProfileRequestDTO> contactNameMap = userProfileRequestDTOList.stream()
//                .collect(Collectors.toMap(
//                        dto -> dto.getUserProfileResponseDTO().getId(),
//                        Function.identity()
//                ));
//        List<UUID> userIdList = new ArrayList<>(contactNameMap.keySet());
//
//        List<UserProfile> userProfiles = this.userProfileRepository.findAllById(userIdList);
//
//        return userProfiles.stream()
//                .filter(Objects::nonNull)
//                .map(userProfile -> {
//                    PrivacySettingsResponseDTO privacySettingsDTO = IPrivactSettingsMapper.INSTANCE.toPrivacySettingsResponseDTO(userProfile.getPrivacySettings());
//
//                    return FeignClientUserProfileResponseDTO.builder()
//                            .id(contactNameMap.get(userProfile.getId()).getId())
//                            .userProfileResponseDTO(
//                                    UserProfileResponseDTO.builder()
//                                            .id(userProfile.getId())
//                                            .email(userProfile.getEmail())
//                                            .firstName(userProfile.getFirstName())
//                                            .lastName(userProfile.getLastName())
//                                            .about(userProfile.getAbout())
//                                            .privacySettings(privacySettingsDTO)
//                                            .build())
//                            .userContactName(contactNameMap.get(userProfile.getId()).getUserContactName())
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }
    public List<FeignClientUserProfileResponseDTO> getUserList(List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        System.out.println("REQUESTDTO > " + userProfileRequestDTOList);
        Map<UUID, FeignClientUserProfileRequestDTO> contactNameMap = userProfileRequestDTOList.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getUserProfileResponseDTO().getId(),
                        Function.identity()
                ));
        List<UUID> userIdList = new ArrayList<>(contactNameMap.keySet());

        List<UserProfile> userProfiles = this.userProfileRepository.findAllById(userIdList);

        List<FeignClientUserProfileResponseDTO> dto = userProfiles.stream()
                .filter(Objects::nonNull)
                .map(userProfile -> FeignClientUserProfileResponseDTO.builder()
                        .id(contactNameMap.get(userProfile.getId()).getId())
                        .userProfileResponseDTO(
                                UserProfileResponseDTO.builder()
                                        .id(userProfile.getId())
                                        .email(userProfile.getEmail())
                                        .firstName(userProfile.getFirstName())
                                        .lastName(userProfile.getLastName())
                                        .about(userProfile.getAbout())
                                        .privacySettings(contactNameMap.get(userProfile.getId()).getUserProfileResponseDTO().getPrivacySettings() != null ? PrivacySettingsResponseDTO.builder()
                                                .id(userProfile.getPrivacySettings().getId())
                                                .aboutVisibility(userProfile.getPrivacySettings().getAboutVisibility())
                                                .lastSeenVisibility(userProfile.getPrivacySettings().getLastSeenVisibility())
                                                .profilePhotoVisibility(userProfile.getPrivacySettings().getProfilePhotoVisibility())
                                                .onlineStatusVisibility(userProfile.getPrivacySettings().getOnlineStatusVisibility())
                                                .readReceipts(userProfile.getPrivacySettings().isReadReceipts())
                                                .isInContactList(contactNameMap.get(userProfile.getId()).getUserProfileResponseDTO().getPrivacySettings().isInContactList()).build() : IPrivactSettingsMapper.INSTANCE.toPrivacySettingsResponseDTO(userProfile.getPrivacySettings()))
                                        .build())
                        .userContactName(contactNameMap.get(userProfile.getId()).getUserContactName())
                        .build())
                .collect(Collectors.toList());
        System.out.println("DTO > " + dto);
        return dto;
    }

    public UserProfileResponseDTO getUserById(UUID userId) {
        UserProfile userProfile = this.userProfileRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found witdh ID: " + userId));
        return IUserProfileMapper.INSTANCE.toUserProfileDTO(userProfile);
    }

    public UserProfileResponseDTO getUserByEmail(String contactEmail) {
        return userProfileRepository.findUserProfileByEmailIgnoreCase(contactEmail).map(IUserProfileMapper.INSTANCE::toUserProfileDTO).orElse(null);
    }

    public void updateUserLastSeen(UserLastSeenRequestDTO userLastSeenRequestDTO) {

        Optional<UserProfile> userProfile = this.userProfileRepository.findById(userLastSeenRequestDTO.getUserId());

        userProfile.ifPresent(profile -> {
            profile.setLastSeen(LocalDateTime.now());
            userProfileRepository.save(profile);
        });
    }

    public UserLastSeenResponseDTO getUserLastSeen(UUID userId) {

        Optional<UserProfile> userProfile = this.userProfileRepository.findById(userId);
        if (userProfile.isPresent()) {
            UserProfile profile = userProfile.get();
            UserLastSeenResponseDTO responseDTO = new UserLastSeenResponseDTO();
            responseDTO.setLastSeen(profile.getLastSeen());
            responseDTO.setId(profile.getId());
            return responseDTO;
        } else {

            return null;
        }
    }

    public List<UserProfile> getUsers() {
        return this.userProfileRepository.findAll();
    }

    public UserProfileResponseDTO updatePrivacySettings(UUID userId, PrivacySettingsRequestDTO privacySettingsRequestDTO) {
        UserProfile userProfile = this.userProfileRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found witdh ID: " + userId));

        PrivacySettings privacySettings = userProfile.getPrivacySettings();
        if (privacySettings == null) {
            privacySettings = new PrivacySettings();
        }

        privacySettings.setProfilePhotoVisibility(privacySettingsRequestDTO.getProfilePhotoVisibility());
        privacySettings.setLastSeenVisibility(privacySettingsRequestDTO.getLastSeenVisibility());
        privacySettings.setOnlineStatusVisibility(privacySettingsRequestDTO.getOnlineStatusVisibility());
        privacySettings.setAboutVisibility(privacySettingsRequestDTO.getAboutVisibility());
        privacySettings.setReadReceipts(privacySettingsRequestDTO.isReadReceipts());

        userProfile.setPrivacySettings(privacySettings);
        userProfileRepository.save(userProfile);
        return IUserProfileMapper.INSTANCE.toUserProfileDTO(userProfile);
    }
}
