package com.vky;

import com.vky.repository.IChatMessageRepository;
import com.vky.repository.IChatRoomRepository;
import com.vky.repository.IUserChatSettingsRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

@SpringBootApplication
@EnableFeignClients
public class ChatServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(IChatRoomRepository chatRoomRepository, IChatMessageRepository chatMessageRepository, IUserChatSettingsRepository userChatSettingsRepository) throws Exception {
//
//
//        PublicKey recipientPublicKey = loadPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmekfq4/6VcePleal9rlHB9HPtsCOBIbEaXNwRZ3IvNaknfFJgz27vmj+Hy60StTmlSjJw8iWNnVg/sM8Cz7evEKupy+1yyXtAx2d/EYfVrZdJy3Pws4B6910nAL4felHEcUFopUIPJu6xG1Kzt141Vqm2sd9GKLYRA1ChAgJaA20s1IaadYz6HkkfLv85lObWwgkJelUm73yQA4AZzZSZ0Ob6PPZvo6RctYplyGEIAz3NKGfCLjqqKeffurGgTg7Z/HiWOhaPFCyKXFZcj6JimCTxDv3sZ1j7XPVALix1lkLPH8hhej3I9v2yiFyuUIdTuf33xkk8SRrOJD1MbGUFQIDAQAB");
//
//
//        return args -> {
//            for (int i = 0; i <= 200; i++) {
//                // a. Her kullanıcı için yeni AES anahtarı
//                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//                keyGen.init(256);
//                SecretKey aesKey = keyGen.generateKey();
//
//                // b. Yeni IV üret
//                byte[] iv = new byte[12];
//                new SecureRandom().nextBytes(iv);
//
//                // c. Mesajı AES-GCM ile şifrele
//                Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
//                aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
//                String message = "Merhaba " + "User" + i;
//                byte[] encryptedMessage = aesCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
//
//                // d. AES anahtarını RSA ile şifrele
//                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
//                OAEPParameterSpec oaepParams = new OAEPParameterSpec(
//                        "SHA-256",
//                        "MGF1",
//                        MGF1ParameterSpec.SHA256,
//                        PSource.PSpecified.DEFAULT
//                );
//                rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey,oaepParams);
//                byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());
//                String userId = "19039fbc-3adc-3722-b6ad-ca4063cf3618";
//                String username = "User" + i;
//
//
//
//                String friendId = UUID.nameUUIDFromBytes(String.format("User%04d", i).getBytes()).toString();
//
//                    List<String> participants = Arrays.asList(userId, friendId);
//                    ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().participantIds(participants).build());
//
//                    // f. ChatMessage kaydı
//                    ChatMessage chatMessage = ChatMessage.builder()
//                            .chatRoomId(chatRoom.getId())
//                            .senderId(userId)
//                            .recipientId(friendId)
//                            .encryptedMessageContent(encryptedMessage)
//                            .iv(iv)
//                            .encryptedKeyForRecipient(encryptedAESKey)
//                            .encryptedKeyForSender(encryptedAESKey)
//                            .fullDateTime(Instant.now())
//                            .isSeen(false)
//                            .build();
//                    chatMessageRepository.save(chatMessage);
//
//                    UserChatSettings userChatSettings = new UserChatSettings();
//                    userChatSettings.setUserId(userId);
//                    userChatSettings.setDeleted(false);
//                    userChatSettings.setPinned(false);
//                    userChatSettings.setBlocked(false);
//                    userChatSettings.setArchived(false);
//                    userChatSettings.setDeletedTime(null);
//                    userChatSettings.setUnblockedTime(null);
//                    userChatSettings.setBlockedTime(null);
//                    userChatSettings.setUnreadMessageCount(0);
//                    userChatSettings.setChatRoomId(chatRoom.getId());
//                    userChatSettingsRepository.save(userChatSettings);
//
//                UserChatSettings userChatSettings1 = new UserChatSettings();
//                userChatSettings1.setUserId(friendId);
//                userChatSettings1.setDeleted(false);
//                userChatSettings1.setPinned(false);
//                userChatSettings1.setBlocked(false);
//                userChatSettings1.setArchived(false);
//                userChatSettings1.setDeletedTime(null);
//                userChatSettings1.setUnblockedTime(null);
//                userChatSettings1.setBlockedTime(null);
//                userChatSettings1.setUnreadMessageCount(0);
//                userChatSettings1.setChatRoomId(chatRoom.getId());
//                userChatSettingsRepository.save(userChatSettings1);
//
//                    System.out.println("Kullanıcı eklendi: " + username + " ChatRoomId > " + userChatSettings.getChatRoomId());
//                }
//
//        };
//    }
//
//    public static PublicKey loadPublicKey(String base64PublicKey) throws Exception {
//        byte[] decoded = Base64.getDecoder().decode(base64PublicKey);
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        return kf.generatePublic(keySpec);
//    }
//
//    public static PrivateKey loadPrivateKey(String base64PrivateKey) throws Exception {
//        byte[] decoded = Base64.getDecoder().decode(base64PrivateKey);
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        return kf.generatePrivate(keySpec);
//    }
}
