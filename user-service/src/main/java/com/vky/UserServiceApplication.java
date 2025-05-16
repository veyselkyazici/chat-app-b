package com.vky;

import com.vky.repository.IPrivacySettingsRepository;
import com.vky.repository.IUserKeyRepository;
import com.vky.repository.IUserProfileRepository;
import com.vky.repository.entity.PrivacySettings;
import com.vky.repository.entity.UserKey;
import com.vky.repository.entity.UserProfile;
import com.vky.repository.entity.enums.VisibilityOption;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class UserServiceApplication {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(UserServiceApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(IUserProfileRepository userProfileRepository, IPrivacySettingsRepository privacySettingsRepository, IUserKeyRepository userKeyRepository) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
//        return args -> {
//            for (int i = 6; i <= 50; i++) {
//
//                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
//                generator.initialize(4096);
//                KeyPair keyPair = generator.generateKeyPair();
//
//                // 2. PBKDF2 ile AES anahtarı (SHA-256, 150k iterasyon)
//                byte[] salt = new byte[16];
//                byte[] iv = new byte[12]; // GCM için 12 byte
//                new SecureRandom().nextBytes(salt);
//                new SecureRandom().nextBytes(iv);
//
//                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//                KeySpec spec = new PBEKeySpec("asdasd".toCharArray(), salt, 150000, 256);
//                SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
//
//                // 3. Private Key'i AES-GCM ile şifrele
//                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//                cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
//                byte[] encryptedPrivateKey = cipher.doFinal(keyPair.getPrivate().getEncoded());
//
//                String username = "User" + i;
//                String email = username.toLowerCase() + "@gmail.com";
//                UUID authId = UUID.nameUUIDFromBytes(String.format("User%04d", i).getBytes());
//                UUID userId = UUID.nameUUIDFromBytes(String.format("User%03d", i).getBytes());
//
//                PrivacySettings privacySettings = privacySettingsRepository.save((PrivacySettings.builder().isDeleted(false).id(userId).aboutVisibility(VisibilityOption.EVERYONE).lastSeenVisibility(VisibilityOption.EVERYONE).onlineStatusVisibility(VisibilityOption.EVERYONE).profilePhotoVisibility(VisibilityOption.EVERYONE)
//                        .readReceipts(true).id(userId).createdAt(Instant.now()).build()));
//                privacySettingsRepository.save(privacySettings);
//                UserKey userKey = UserKey.builder()
//                        .publicKey(keyPair.getPublic().getEncoded())
//                        .encryptedPrivateKey(encryptedPrivateKey)
//                        .salt(salt)
//                        .iv(iv)
//                        .build();
//
//                userKey = userKeyRepository.save(userKey);
//                UserProfile user = UserProfile.builder()
//                        .email(email)
//                        .id(userId)
//                        .authId(authId)
//                        .privacySettings(privacySettings)
//                        .userKey(userKey)
//                        .about(username)
//                        .firstName(username)
//                        .lastSeen(Instant.now())
//                        .build();
//
//                user = userProfileRepository.save(user);
//
//                userKey.setUser(user);
//                userKeyRepository.save(userKey);
//                System.out.println("Kullanıcı eklendi: " + username);
//            }
//        };
//    }
}