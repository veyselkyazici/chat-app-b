package com.vky;

import com.vky.repository.IPrivacySettingsRepository;
import com.vky.repository.IUserKeyRepository;
import com.vky.repository.IUserProfileRepository;
import com.vky.repository.entity.PrivacySettings;
import com.vky.repository.entity.UserKey;
import com.vky.repository.entity.UserProfile;
import com.vky.repository.entity.enums.VisibilityOption;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(IUserProfileRepository userProfileRepository, IPrivacySettingsRepository privacySettingsRepository, IUserKeyRepository userKeyRepository) throws Exception {
//
//        return args -> {
//            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
//            generator.initialize(2048);
//            KeyPair keyPair = generator.generateKeyPair();
//
//            // Anahtarları ekrana yazdır (kaydetmek için)
//            System.out.println("=== KEY PAIR DETAILS ===");
//            System.out.println("Public Key Algorithm: " + keyPair.getPublic().getAlgorithm());
//            System.out.println("Private Key Algorithm: " + keyPair.getPrivate().getAlgorithm());
//            System.out.println("Public Key Format: " + keyPair.getPublic().getFormat());
//            System.out.println("Private Key Format: " + keyPair.getPrivate().getFormat());
//
//            System.out.println("=== PUBLIC KEY (Base64) ===");
//            System.out.println(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
//
//            System.out.println("=== PRIVATE KEY (Base64) ===");
//            System.out.println(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
//            SecureRandom secureRandom = new SecureRandom();
//            String masterKey = System.getenv("MASTER_KEY");
//            byte[] salt = new byte[16];
//            byte[] iv = new byte[12]; // GCM için 12 byte
//            secureRandom.nextBytes(salt);
//            secureRandom.nextBytes(iv);
//            saveVeysel(salt,iv,keyPair,masterKey,userProfileRepository,privacySettingsRepository,userKeyRepository);
//            for (int i = 0; i <= 200; i++) {
//
//
//                // 2. PBKDF2 ile AES anahtarı (SHA-256, 150k iterasyon)
//
//
//                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//                KeySpec spec = new PBEKeySpec("Asdasd.123".toCharArray(), salt, 100, 256);
//                SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
//                // 3. Private Key'i AES-GCM ile şifrele
//                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//                cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
//                byte[] encryptedPrivateKey = cipher.doFinal(keyPair.getPrivate().getEncoded());
//
//
//
//                KeySpec specMaster = new PBEKeySpec(masterKey.toCharArray(), salt, 100, 256);
//                SecretKey tmp = factory.generateSecret(specMaster);
//                SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
//
//                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
//                byte[] encryptedText = cipher.doFinal(keyPair.getPrivate().getEncoded());
//
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                outputStream.write(salt);
//                outputStream.write(iv);
//                outputStream.write(encryptedText);
//
//
//
//
//
//
//                String username = "User" + i;
//                String email = username.toLowerCase() + "@gmailgmail.com";
//                UUID authId = UUID.nameUUIDFromBytes(String.format("User%04d", i).getBytes());
//
//                PrivacySettings privacySettings = privacySettingsRepository.save((PrivacySettings.builder().isDeleted(false).aboutVisibility(VisibilityOption.EVERYONE).lastSeenVisibility(VisibilityOption.EVERYONE).onlineStatusVisibility(VisibilityOption.EVERYONE).profilePhotoVisibility(VisibilityOption.EVERYONE)
//                        .readReceipts(true).createdAt(Instant.now()).build()));
//                privacySettingsRepository.save(privacySettings);
//                UserKey userKey = UserKey.builder()
//                        .publicKey(keyPair.getPublic().getEncoded())
//                        .encryptedPrivateKey(encryptedPrivateKey)
//                        .salt(salt)
//                        .encryptedPrivateKeyWithMasterKey(outputStream.toByteArray())
//                        .iv(iv)
//                        .build();
//
//                userKey = userKeyRepository.save(userKey);
//                UserProfile user = UserProfile.builder()
//                        .email(email)
//                        .id(authId)
//                        .authId(authId)
//                        .privacySettings(privacySettings)
//                        .userKey(userKey)
//                        .about(username)
//                        .firstName(username)
//                        .lastSeen(Instant.now())
//                        .updatedAt(Instant.now())
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
//
//    private void saveVeysel(byte[] salt, byte[] iv, KeyPair keyPair, String masterKey, IUserProfileRepository userProfileRepository, IPrivacySettingsRepository privacySettingsRepository, IUserKeyRepository userKeyRepository) throws Exception {
//        System.out.println("=== PUBLIC KEY (Base64) ===");
//        System.out.println(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
//
//        System.out.println("=== PRIVATE KEY (Base64) ===");
//        System.out.println(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
//
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//        KeySpec spec = new PBEKeySpec("Asdasd.123".toCharArray(), salt, 100, 256);
//        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
//        // 3. Private Key'i AES-GCM ile şifrele
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
//        byte[] encryptedPrivateKey = cipher.doFinal(keyPair.getPrivate().getEncoded());
//
//
//
//        KeySpec specMaster = new PBEKeySpec(masterKey.toCharArray(), salt, 100, 256);
//        SecretKey tmp = factory.generateSecret(specMaster);
//        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
//
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
//        byte[] encryptedText = cipher.doFinal(keyPair.getPrivate().getEncoded());
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        outputStream.write(salt);
//        outputStream.write(iv);
//        outputStream.write(encryptedText);
//
//
//
//
//
//
//        String username = "Veysel";
//        String email = "veysel.06.fb@hotmail.com";
//        UUID authId = UUID.nameUUIDFromBytes(String.format("User%04d", 500).getBytes());
//
//        PrivacySettings privacySettings = privacySettingsRepository.save((PrivacySettings.builder().isDeleted(false).aboutVisibility(VisibilityOption.EVERYONE).lastSeenVisibility(VisibilityOption.EVERYONE).onlineStatusVisibility(VisibilityOption.EVERYONE).profilePhotoVisibility(VisibilityOption.EVERYONE)
//                .readReceipts(true).createdAt(Instant.now()).build()));
//        privacySettingsRepository.save(privacySettings);
//        UserKey userKey = UserKey.builder()
//                .publicKey(keyPair.getPublic().getEncoded())
//                .encryptedPrivateKey(encryptedPrivateKey)
//                .salt(salt)
//                .encryptedPrivateKeyWithMasterKey(outputStream.toByteArray())
//                .iv(iv)
//                .build();
//
//        userKey = userKeyRepository.save(userKey);
//        UserProfile user = UserProfile.builder()
//                .email(email)
//                .id(authId)
//                .authId(authId)
//                .privacySettings(privacySettings)
//                .userKey(userKey)
//                .about(username)
//                .firstName(username)
//                .lastSeen(Instant.now())
//                .updatedAt(Instant.now())
//                .build();
//
//        user = userProfileRepository.save(user);
//
//        userKey.setUser(user);
//        userKeyRepository.save(userKey);
//        System.out.println("Kullanıcı eklendi: " + username);
//    }
}