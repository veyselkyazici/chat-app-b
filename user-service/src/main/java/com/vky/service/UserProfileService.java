package com.vky.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vky.dto.request.PrivacySettingsRequestDTO;
import com.vky.dto.request.UpdateSettingsRequestDTO;
import com.vky.dto.request.UpdateUserDTO;
import com.vky.dto.response.*;
import com.vky.exception.ErrorType;
import com.vky.exception.UserServiceException;
import com.vky.mapper.IUserProfileMapper;
import com.vky.rabbitmq.model.CreateUser;
import com.vky.rabbitmq.producer.RabbitMQProducer;
import com.vky.repository.IUserProfileRepository;
import com.vky.repository.entity.PrivacySettings;
import com.vky.repository.entity.UserKey;
import com.vky.repository.entity.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final IUserProfileRepository userProfileRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final Cloudinary cloudinary;

    public UserProfileService(IUserProfileRepository userProfileRepository, RabbitMQProducer rabbitMQProducer,
            Cloudinary cloudinary) {
        this.userProfileRepository = userProfileRepository;
        this.rabbitMQProducer = rabbitMQProducer;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public void createUserProfile(CreateUser createUser) {
        UserProfile userProfile = UserProfile.builder()
                .authId(createUser.getAuthId())
                .email(createUser.getEmail())
                .privacySettings(new PrivacySettings())
                .about("Welcome to vkychatapp")
                .id(createUser.getAuthId())
                .lastSeen(Instant.now())
                .build();

        UserKey userKey = new UserKey();
        userKey.setPublicKey(createUser.getPublicKey());
        userKey.setIv(createUser.getIv());
        userKey.setSalt(createUser.getSalt());
        userKey.setEncryptedPrivateKey(createUser.getEncryptedPrivateKey());
        userKey.setUser(userProfile);
        userProfile.setUserKey(userKey);

        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        rabbitMQProducer.checkContactUser(UserProfileResponseDTO.builder()
                .about(savedUserProfile.getAbout())
                .email(savedUserProfile.getEmail())
                .firstName(savedUserProfile.getFirstName())
                .id(savedUserProfile.getId())
                .imagee(savedUserProfile.getImage())
                .userKey(UserKeyResponseDTO.builder()
                        .publicKey(Base64.getEncoder().encodeToString(savedUserProfile.getUserKey().getPublicKey()))
                        .salt(Base64.getEncoder().encodeToString(savedUserProfile.getUserKey().getSalt()))
                        .encryptedPrivateKey(Base64.getEncoder()
                                .encodeToString(savedUserProfile.getUserKey().getEncryptedPrivateKey()))
                        .iv(Base64.getEncoder().encodeToString(savedUserProfile.getUserKey().getIv()))
                        .build())
                .privacySettings(PrivacySettingsResponseDTO.builder()
                        .aboutVisibility(savedUserProfile.getPrivacySettings().getAboutVisibility())
                        .profilePhotoVisibility(savedUserProfile.getPrivacySettings().getProfilePhotoVisibility())
                        .readReceipts(savedUserProfile.getPrivacySettings().isReadReceipts())
                        .id(savedUserProfile.getPrivacySettings().getId())
                        .lastSeenVisibility(savedUserProfile.getPrivacySettings().getLastSeenVisibility())
                        .onlineStatusVisibility(savedUserProfile.getPrivacySettings().getOnlineStatusVisibility())
                        .build())
                .build());
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDTO findWithUserKeyByAuthId(String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        return userProfileRepository.findWithUserKeyByAuthIdAndIsDeletedFalse(userId)
                .map(IUserProfileMapper.INSTANCE::toUserProfileDTO)
                .orElseThrow(() -> new UserServiceException(ErrorType.USER_NOT_FOUND));
    }

    public UpdateUserDTO updateUserName(UpdateUserDTO dto, String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException(ErrorType.USER_NOT_FOUND));
        if (userProfile.getFirstName() == null || !userProfile.getFirstName().equals(dto.value())) {
            userProfile.setFirstName(dto.value());
            userProfile.setUpdatedAt(Instant.now());
            userProfileRepository.save(userProfile);
        }
        return dto;
    }

    public UpdateUserDTO updateUserAbout(UpdateUserDTO dto, String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException(ErrorType.USER_NOT_FOUND));
        if (userProfile.getAbout() == null || !userProfile.getAbout().equals(dto.value())) {
            userProfile.setAbout(dto.value());
            userProfile.setUpdatedAt(Instant.now());
            userProfileRepository.save(userProfile);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    // LAZY alanlar için ya servis katmanında Transactional(Hibernate session açık
    // tutar) veya repositoryde EntityGraph kullanılmalı (UserKey de @Lob alanlar
    // bulunduğu için varsayılan olarak LAZY davranırlar ve EntityGraph burada
    // çalışamaz)
    public List<ContactResponseDTO> getUsers(List<UUID> ids) {

        List<UserProfile> userProfiles = this.userProfileRepository.findUsersByIdList(ids);

        return userProfiles.stream()
                .filter(Objects::nonNull)
                .map(userProfile -> {

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
                                            .lastSeenVisibility(
                                                    userProfile.getPrivacySettings().getLastSeenVisibility())
                                            .profilePhotoVisibility(
                                                    userProfile.getPrivacySettings().getProfilePhotoVisibility())
                                            .onlineStatusVisibility(
                                                    userProfile.getPrivacySettings().getOnlineStatusVisibility())
                                            .readReceipts(userProfile.getPrivacySettings().isReadReceipts())
                                            .build())
                            .userKey(UserKeyResponseDTO.builder()
                                    .iv(Base64.getEncoder().encodeToString(userProfile.getUserKey().getIv()))
                                    .publicKey(
                                            Base64.getEncoder().encodeToString(userProfile.getUserKey().getPublicKey()))
                                    .salt(Base64.getEncoder().encodeToString(userProfile.getUserKey().getSalt()))
                                    .build())
                            .build();
                    System.out.println(userProfileResponseDTO);
                    return ContactResponseDTO.builder()
                            .userProfileResponseDTO(userProfileResponseDTO)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getUserById(UUID userId) {
        UserProfile userProfile = this.userProfileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found witdh ID: " + userId));
        return IUserProfileMapper.INSTANCE.toUserProfileDTO(userProfile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getUserByEmail(String contactEmail) {
        return userProfileRepository.findUserProfileByEmailIgnoreCaseAndIsDeletedFalse(contactEmail)
                .map(IUserProfileMapper.INSTANCE::toUserProfileDTO).orElse(null);
    }

    public void updateUserLastSeen(UUID userId, String lastSeen) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException(ErrorType.USER_NOT_FOUND));

        userProfile.setLastSeen(Instant.parse(lastSeen));
        userProfileRepository.save(userProfile);
    }

    @Transactional
    public UpdateSettingsRequestDTO updatePrivacySettings(PrivacySettingsRequestDTO privacySettingsRequestDTO,
            String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException(ErrorType.USER_NOT_FOUND));

        PrivacySettings privacySettings = userProfile.getPrivacySettings();
        if (privacySettings == null) {
            privacySettings = new PrivacySettings();
        }

        privacySettings.setProfilePhotoVisibility(privacySettingsRequestDTO.profilePhotoVisibility());
        privacySettings.setLastSeenVisibility(privacySettingsRequestDTO.lastSeenVisibility());
        privacySettings.setOnlineStatusVisibility(privacySettingsRequestDTO.onlineStatusVisibility());
        privacySettings.setAboutVisibility(privacySettingsRequestDTO.aboutVisibility());
        privacySettings.setReadReceipts(privacySettingsRequestDTO.readReceipts());
        userProfile.setUpdatedAt(Instant.now());
        userProfile.setPrivacySettings(privacySettings);
        userProfileRepository.save(userProfile);

        UpdateSettingsRequestDTO dto = IUserProfileMapper.INSTANCE.toUserProfileWithoutKeyDTO(userProfile);
        dto = dto.toBuilder().privacy(privacySettingsRequestDTO.privacy()).build();
        rabbitMQProducer.privacyUpdated(dto);
        return dto;
    }

    public UserProfilePhotoURLResponseDTO uploadProfilePhoto(MultipartFile file, String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        try {
            UserProfile user = userProfileRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getImage() != null && !user.getImage().isEmpty()) {
                String publicId = extractPublicIdFromUrl(user.getImage());

                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String profilePictureUrl = uploadResult.get("secure_url").toString();

            user.setImage(profilePictureUrl);
            user.setUpdatedAt(Instant.now());
            userProfileRepository.save(user);
            UpdateSettingsRequestDTO dto = IUserProfileMapper.INSTANCE.toUserProfileWithoutKeyDTO(user);
            rabbitMQProducer.publishProfileUpdated(dto);
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

    @Transactional
    public void resetUserKey(ResetUserKeyDTO resetUserKeyDTO) {
        UserProfile userProfile = userProfileRepository.findById(resetUserKeyDTO.userId())
                .orElseThrow(() -> new UserServiceException(ErrorType.USER_NOT_FOUND));
        userProfile.getUserKey().setEncryptedPrivateKey(resetUserKeyDTO.encryptedPrivateKey());
        userProfile.getUserKey().setIv(resetUserKeyDTO.iv());
        userProfile.getUserKey().setSalt(resetUserKeyDTO.salt());
        if (resetUserKeyDTO.publicKey() != null) {
            userProfile.getUserKey().setPublicKey(resetUserKeyDTO.publicKey());
        }
        userProfileRepository.save(userProfile);
    }

    public void removeProfilePicture(String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);

        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            String publicId = extractPublicIdFromUrl(user.getImage());
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                throw new RuntimeException("Error deleting image from Cloudinary", e);
            }
        }
        user.setImage(null);
        user.setUpdatedAt(Instant.now());
        userProfileRepository.save(user);
    }

    public UpdateSettingsRequestDTO getFeignUserByIdWithOutUserKey(UUID userId) {
        UserProfile userProfile = this.userProfileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found witdh ID: " + userId));
        return IUserProfileMapper.INSTANCE.toUserProfileWithoutKeyDTO(userProfile);
    }

    @Transactional(readOnly = true)
    public PrivacySettingsResponseDTO getPrivacySettings(String userId) {
        UserProfile user = userProfileRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        PrivacySettings privacy = user.getPrivacySettings();

        return PrivacySettingsResponseDTO.builder()
                .onlineStatusVisibility(privacy.getOnlineStatusVisibility())
                .lastSeenVisibility(privacy.getLastSeenVisibility())
                .profilePhotoVisibility(privacy.getProfilePhotoVisibility())
                .aboutVisibility(privacy.getAboutVisibility())
                .readReceipts(privacy.isReadReceipts())
                .build();
    }

    public LastSeenDTO getLastSeen(String userId) {
        UserProfile user = userProfileRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        return LastSeenDTO.builder().lastSeen(user.getLastSeen().toString()).build();
    }
}
