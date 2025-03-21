package com.vky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(IUserProfileRepository userProfileRepository, IPrivacySettingsRepository privacySettingsRepository) {
//        return args -> {
//            for (int i = 6; i <= 50; i++) {
//                String username = "User" + i;
//                String email = username.toLowerCase() + "@gmail.com";
//                UUID authId = UUID.nameUUIDFromBytes(String.format("User%04d", i).getBytes());
//                UUID userId = UUID.nameUUIDFromBytes(String.format("User%03d", i).getBytes());
//
//                PrivacySettings privacySettings = privacySettingsRepository.save((PrivacySettings.builder().isDeleted(false).id(userId).aboutVisibility(VisibilityOption.EVERYONE).lastSeenVisibility(VisibilityOption.EVERYONE).onlineStatusVisibility(VisibilityOption.EVERYONE).profilePhotoVisibility(VisibilityOption.EVERYONE)
//                        .readReceipts(true).id(userId).createdAt(LocalDateTime.now()).build()));
//                privacySettingsRepository.save(privacySettings);
//                UserProfile user = UserProfile.builder()
//                        .email(email)
//                        .id(userId)
//                        .authId(authId)
//                        .privacySettings(privacySettings)
//                        .about(username)
//                        .firstName(username)
//                        .lastSeen(LocalDateTime.now())
//                        .build();
//                userProfileRepository.save(user); // AuthService içinde kaydetme işlemi yapılır
//                System.out.println("Kullanıcı eklendi: " + username);
//            }
//        };
//    }
}