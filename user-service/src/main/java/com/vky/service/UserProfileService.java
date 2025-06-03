package com.vky.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vky.controller.ContactWithRelationshipDTO;
import com.vky.dto.request.*;
import com.vky.dto.response.*;
import com.vky.exception.ErrorType;
import com.vky.exception.UserNotFoundException;
import com.vky.mapper.IUserProfileMapper;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.rabbitmq.producer.RabbitMQProducer;
import com.vky.repository.IUserProfileRepository;
import com.vky.repository.entity.PrivacySettings;
import com.vky.repository.entity.UserKey;
import com.vky.repository.entity.UserProfile;
import com.vky.utility.JwtTokenManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final IUserProfileRepository userProfileRepository;
    private final JwtTokenManager jwtTokenManager;
    private final RabbitMQProducer rabbitMQProducer;
    private final String uploadDir = "user-service/uploads/profile_photos/";
    private final Cloudinary cloudinary;

    public UserProfileService(IUserProfileRepository userProfileRepository, JwtTokenManager jwtTokenManager, RabbitMQProducer rabbitMQProducer, Cloudinary cloudinary) {
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.rabbitMQProducer = rabbitMQProducer;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public void createUserProfile(CreateUser createUser) {

        UserProfile savedUserProfile = UserProfile.builder()
                .authId(createUser.getAuthId())
                .email(createUser.getEmail())
                .privacySettings(new PrivacySettings())
                .build();
        UserKey userKey = new UserKey();
        userKey.setPublicKey(createUser.getPublicKey());
        userKey.setIv(createUser.getIv());
        userKey.setSalt(createUser.getSalt());
        userKey.setEncryptedPrivateKey(createUser.getEncryptedPrivateKey());
        userKey.setUser(savedUserProfile);
        savedUserProfile.setUserKey(userKey);

        userProfileRepository.save(savedUserProfile);
        rabbitMQProducer.checkContactUser(savedUserProfile);
    }

    public FindUserProfileByAuthIdResponseDTO findByAuthId(UUID authId) {
        Optional<FindUserProfileByAuthIdResponseDTO> userProfile = userProfileRepository.findDtoByAuthId(authId);
        if (userProfile.isPresent()) {
            return userProfile.get();
        }
        return null;
    }
    @Transactional(readOnly = true)
    public UserProfileResponseDTO findWithUserKeyByAuthId(UUID authId) {
        return userProfileRepository.findWithUserKeyByAuthId(authId)
                .map(IUserProfileMapper.INSTANCE::toUserProfileDTO)
                .orElseThrow(() -> new UserNotFoundException(ErrorType.USER_NOT_FOUND));
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

    public UpdateUserDTO updateUserName(UpdateUserDTO dto) {
        UserProfile userProfile = userProfileRepository.findById(dto.getId())
                .orElseThrow(() -> new UserNotFoundException(ErrorType.USER_NOT_FOUND));

        userProfile.setFirstName(dto.getValue());
        UserProfile updatedUserProfile = userProfileRepository.save(userProfile);
        dto.setValue(updatedUserProfile.getFirstName());
        return dto;
    }

    public UpdateUserDTO updateUserAbout(UpdateUserDTO dto) {
        UserProfile userProfile = userProfileRepository.findById(dto.getId())
                .orElseThrow(() -> new UserNotFoundException(ErrorType.USER_NOT_FOUND));

        userProfile.setFirstName(dto.getValue());
        UserProfile updatedUserProfile = userProfileRepository.save(userProfile);
        dto.setValue(updatedUserProfile.getAbout());
        return dto;
    }


    public List<FeignClientUserProfileResponseDTO> getUserList(List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {
        System.out.println("REQUESTDTO > " + userProfileRequestDTOList);

        Map<UUID, FeignClientUserProfileRequestDTO> contactNameMap = userProfileRequestDTOList.stream()
                .collect(Collectors.toMap(
                        FeignClientUserProfileRequestDTO::getId,
                        Function.identity()
                ));

        List<UUID> userIdList = new ArrayList<>(contactNameMap.keySet());

        List<UserProfile> userProfiles = this.userProfileRepository.findAllById(userIdList);

        List<FeignClientUserProfileResponseDTO> dto = userProfiles.stream()
                .filter(Objects::nonNull)
                .map(userProfile -> {
                    FeignClientUserProfileRequestDTO requestDTO = contactNameMap.get(userProfile.getId());

                    UserProfileResponseDTO userProfileResponseDTO = UserProfileResponseDTO.builder()
                            .id(userProfile.getId())
                            .email(userProfile.getEmail())
                            .firstName(userProfile.getFirstName())
                            .lastName(userProfile.getLastName())
                            .about(userProfile.getAbout())
                            .privacySettings(
                                    PrivacySettingsResponseDTO.builder()
                                            .id(userProfile.getPrivacySettings().getId())
                                            .aboutVisibility(userProfile.getPrivacySettings().getAboutVisibility())
                                            .lastSeenVisibility(userProfile.getPrivacySettings().getLastSeenVisibility())
                                            .profilePhotoVisibility(userProfile.getPrivacySettings().getProfilePhotoVisibility())
                                            .onlineStatusVisibility(userProfile.getPrivacySettings().getOnlineStatusVisibility())
                                            .readReceipts(userProfile.getPrivacySettings().isReadReceipts())
                                            .build()
                            )
                            .build();

                    return FeignClientUserProfileResponseDTO.builder()
                            .userProfileResponseDTO(userProfileResponseDTO)
                            .build();
                })
                .collect(Collectors.toList());

        System.out.println("DTO > " + dto);
        return dto;
    }
    @Async("taskExecutor")
    @Transactional(readOnly = true)
    // LAZY alanlar için ya servis katmanında Transactional(Hibernate session açık tutar) veya repositoryde EntityGraph kullanılmalı (UserKey de @Lob alanlar bulunduğu için varsayılan olarak LAZY davranırlar ve EntityGraph burada çalışamz)
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getUsersOfContactsAsync(List<FeignClientUserProfileRequestDTO> userProfileRequestDTOList) {

        Map<UUID, FeignClientUserProfileRequestDTO> contactNameMap = userProfileRequestDTOList.stream()
                .collect(Collectors.toMap(
                        FeignClientUserProfileRequestDTO::getId,
                        Function.identity()
                ));

        List<UUID> userIdList = new ArrayList<>(contactNameMap.keySet());

        List<UserProfile> userProfiles = this.userProfileRepository.findUsersByIdList(userIdList);

        List<FeignClientUserProfileResponseDTO> dto = userProfiles.stream()
                .filter(Objects::nonNull)
                .map(userProfile -> {
                    FeignClientUserProfileRequestDTO requestDTO = contactNameMap.get(userProfile.getId());

                    UserProfileResponseDTO userProfileResponseDTO = UserProfileResponseDTO.builder()
                            .id(userProfile.getId())
                            .email(userProfile.getEmail())
                            .firstName(userProfile.getFirstName())
                            .lastName(userProfile.getLastName())
                            .about(userProfile.getAbout())
                            .imagee(userProfile.getImage() != null ? userProfile.getImage() : null)
                            .privacySettings(
                                    PrivacySettingsResponseDTO.builder()
                                            .id(userProfile.getPrivacySettings().getId())
                                            .aboutVisibility(userProfile.getPrivacySettings().getAboutVisibility())
                                            .lastSeenVisibility(userProfile.getPrivacySettings().getLastSeenVisibility())
                                            .profilePhotoVisibility(userProfile.getPrivacySettings().getProfilePhotoVisibility())
                                            .onlineStatusVisibility(userProfile.getPrivacySettings().getOnlineStatusVisibility())
                                            .readReceipts(userProfile.getPrivacySettings().isReadReceipts())
//                                            .isInContactList(userProfile.getPrivacySettings().isInContactList())  // userProfiles'tan al
                                            .build()
                            )
                            .userKey(UserKeyResponseDTO.builder()
                                    .iv(Base64.getEncoder().encodeToString(userProfile.getUserKey().getIv()))
                                    .publicKey(Base64.getEncoder().encodeToString(userProfile.getUserKey().getPublicKey()))
                                    .encryptedPrivateKey(Base64.getEncoder().encodeToString(userProfile.getUserKey().getEncryptedPrivateKey()))
                                    .salt(Base64.getEncoder().encodeToString(userProfile.getUserKey().getSalt()))
                                    .build())
                            .build();

                    return FeignClientUserProfileResponseDTO.builder()
                            .userProfileResponseDTO(userProfileResponseDTO)
                            .build();
                })
                .collect(Collectors.toList());

        System.out.println("DTO > " + dto);
        return CompletableFuture.completedFuture(dto);
    }
    public List<FeignClientUserProfileResponseDTO> getUserListt(List<ContactWithRelationshipDTO> userProfileRequestDTOList) {

        Map<UUID, ContactWithRelationshipDTO> contactNameMap = userProfileRequestDTOList.stream()
                .collect(Collectors.toMap(
                        ContactWithRelationshipDTO::getUserContactId,
                        Function.identity()
                ));
        List<UUID> userIdList = new ArrayList<>(contactNameMap.keySet());

        List<UserProfile> userProfiles = this.userProfileRepository.findAllById(userIdList);

        List<FeignClientUserProfileResponseDTO> dto = userProfiles.stream()
                .filter(Objects::nonNull)
                .map(userProfile -> {
                    ContactWithRelationshipDTO contactDTO = contactNameMap.get(userProfile.getId());

                    // userProfile içindeki PrivacySettings doğrudan kullanılıyor
                    PrivacySettingsResponseDTO privacySettingsDTO = userProfile.getPrivacySettings() != null ?
                            PrivacySettingsResponseDTO.builder()
                                    .id(userProfile.getPrivacySettings().getId())
                                    .aboutVisibility(userProfile.getPrivacySettings().getAboutVisibility())
                                    .lastSeenVisibility(userProfile.getPrivacySettings().getLastSeenVisibility())
                                    .profilePhotoVisibility(userProfile.getPrivacySettings().getProfilePhotoVisibility())
                                    .onlineStatusVisibility(userProfile.getPrivacySettings().getOnlineStatusVisibility())
                                    .readReceipts(userProfile.getPrivacySettings().isReadReceipts())
                                    .build() : null;

                    return FeignClientUserProfileResponseDTO.builder()
                            .userProfileResponseDTO(
                                    UserProfileResponseDTO.builder()
                                            .id(userProfile.getId())
                                            .email(userProfile.getEmail())
                                            .firstName(userProfile.getFirstName())
                                            .lastName(userProfile.getLastName())
                                            .about(userProfile.getAbout())
                                            .privacySettings(privacySettingsDTO)  // userProfile'dan gelen PrivacySettings
                                            .userKey(UserKeyResponseDTO.builder()
                                                .iv(Base64.getEncoder().encodeToString(userProfile.getUserKey().getIv()))
                                                .publicKey(Base64.getEncoder().encodeToString(userProfile.getUserKey().getPublicKey()))
                                                .encryptedPrivateKey(Base64.getEncoder().encodeToString(userProfile.getUserKey().getEncryptedPrivateKey()))
                                                .salt(Base64.getEncoder().encodeToString(userProfile.getUserKey().getSalt()))
                                                    .build())
                                            .build())
                            .build();
                })
                .toList();
        return dto;
    }

    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getUsersOfChatsAsync(List<ContactWithRelationshipDTO> userProfileRequestDTOList) {

        Map<UUID, ContactWithRelationshipDTO> contactNameMap = userProfileRequestDTOList.stream()
                .collect(Collectors.toMap(
                        ContactWithRelationshipDTO::getUserContactId,
                        Function.identity()
                ));
        List<UUID> userIdList = new ArrayList<>(contactNameMap.keySet());

        List<UserProfile> userProfiles = this.userProfileRepository.findAllById(userIdList);

        List<FeignClientUserProfileResponseDTO> dto = userProfiles.stream()
                .filter(Objects::nonNull)
                .map(userProfile -> {
                    ContactWithRelationshipDTO contactDTO = contactNameMap.get(userProfile.getId());

                    // userProfile içindeki PrivacySettings doğrudan kullanılıyor
                    PrivacySettingsResponseDTO privacySettingsDTO = userProfile.getPrivacySettings() != null ?
                            PrivacySettingsResponseDTO.builder()
                                    .id(userProfile.getPrivacySettings().getId())
                                    .aboutVisibility(userProfile.getPrivacySettings().getAboutVisibility())
                                    .lastSeenVisibility(userProfile.getPrivacySettings().getLastSeenVisibility())
                                    .profilePhotoVisibility(userProfile.getPrivacySettings().getProfilePhotoVisibility())
                                    .onlineStatusVisibility(userProfile.getPrivacySettings().getOnlineStatusVisibility())
                                    .readReceipts(userProfile.getPrivacySettings().isReadReceipts())
                                    .build() : null;

                    return FeignClientUserProfileResponseDTO.builder()
                            .userProfileResponseDTO(
                                    UserProfileResponseDTO.builder()
                                            .id(userProfile.getId())
                                            .email(userProfile.getEmail())
                                            .firstName(userProfile.getFirstName())
                                            .lastName(userProfile.getLastName())
                                            .about(userProfile.getAbout())
                                            .imagee(userProfile.getImage() != null ? userProfile.getImage() : null)
                                            .privacySettings(privacySettingsDTO)
                                            .userKey(UserKeyResponseDTO.builder()
                                                    .iv(Base64.getEncoder().encodeToString(userProfile.getUserKey().getIv()))
                                                    .publicKey(Base64.getEncoder().encodeToString(userProfile.getUserKey().getPublicKey()))
                                                    .encryptedPrivateKey(Base64.getEncoder().encodeToString(userProfile.getUserKey().getEncryptedPrivateKey()))
                                                    .salt(Base64.getEncoder().encodeToString(userProfile.getUserKey().getSalt()))
                                                    .build())
                                            .build())
                            .build();
                })
                .toList();
        return CompletableFuture.completedFuture(dto);
    }
    public UserProfileResponseDTO getUserById(UUID userId) {
        UserProfile userProfile = this.userProfileRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found witdh ID: " + userId));
        return IUserProfileMapper.INSTANCE.toUserProfileDTO(userProfile);
    }
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getUserByEmail(String contactEmail) {
        return userProfileRepository.findUserProfileByEmailIgnoreCase(contactEmail).map(IUserProfileMapper.INSTANCE::toUserProfileDTO).orElse(null);
    }

    public void updateUserLastSeen(UserLastSeenRequestDTO userLastSeenRequestDTO) {
        UserProfile userProfile = userProfileRepository.findById(userLastSeenRequestDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ErrorType.USER_NOT_FOUND));

        userProfile.setLastSeen(Instant.now());
            userProfileRepository.save(userProfile);
    }

    public UserLastSeenResponseDTO getUserLastSeen(UUID userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorType.USER_NOT_FOUND));
            UserLastSeenResponseDTO responseDTO = new UserLastSeenResponseDTO();
            responseDTO.setLastSeen(userProfile.getLastSeen());
            responseDTO.setId(userProfile.getId());
            return responseDTO;
    }

    public UserProfileResponseDTO updatePrivacySettings(UUID userId, PrivacySettingsRequestDTO privacySettingsRequestDTO) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorType.USER_NOT_FOUND));

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

    public UserProfilePhotoURLResponseDTO uploadProfilePhoto(UUID userId, MultipartFile file) {
        try {
            UserProfile user = userProfileRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getImage() != null && !user.getImage().isEmpty()) {
                String publicId = extractPublicIdFromUrl(user.getImage());

                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String profilePictureUrl = uploadResult.get("url").toString();


            user.setImage(profilePictureUrl);
            userProfileRepository.save(user);
            return UserProfilePhotoURLResponseDTO.builder().url(user.getImage()).build();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }


    public String extractPublicIdFromUrl(String profilePictureUrl) {
        String[] parts = profilePictureUrl.split("/");
        String publicIdWithExtension = parts[parts.length - 1];
        return publicIdWithExtension.split("\\.")[0];
    }

}
