package com.vky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

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
//        PublicKey recipientPublicKey = loadPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjoQyPJZ/ORHKmYPaVukwBF7n31p2P+f2rwaol2kyHnwX8z296kbGscUzn/P+RZf9Sd29A5wFPgUHde6GrAhwejtsoLbPWLCTddQIeWnm4/pzfd1g2pFBf0br0r/5ubwNPgglQB3nWrBO2SeajS3S3x6HDJK8bkkY5PicpheI8UoUSPTcIyV78xHJ3e0iBl3t/kht/CiQbo/kjKjT55fXf0cLJY+n0kSWaePq9fazSmvuR0f+gdm0GLdtNuwWFZ5ZZIXmxB+VKjpYmzymp4z3U1WIzcJX222XtBaP2scrgQ2FvMwFR3Vs5QV98CDcI/o1J8D5unpzwGvbWFnW+V4qZQIDAQAB");
//
//
//        return args -> {
//            for (int i = 0; i <= 200; i++) {
//                if (i == 119) {
//                    continue;
//                }
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
//                rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey, oaepParams);
//                byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());
//                String userId = "01e8ce0f-dab8-3994-8703-8f7f02a8d5c9";
//                String username = "User" + i;
//
//
//                String friendId = UUID.nameUUIDFromBytes(String.format("User%04d", i).getBytes()).toString();
//
//                List<String> participants = Arrays.asList(userId, friendId);
//                ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().participantIds(participants).build());
//                if (i == 100) {
//                    for (int j = 0; j < 200; j++) {
//                        // a. Her mesaj için yeni AES anahtarı
//                        KeyGenerator keyGenExtra = KeyGenerator.getInstance("AES");
//                        keyGenExtra.init(256);
//                        SecretKey aesKeyExtra = keyGenExtra.generateKey();
//
//                        // b. Yeni IV üret
//                        byte[] ivExtra = new byte[12];
//                        new SecureRandom().nextBytes(ivExtra);
//
//                        // c. Mesajı AES-GCM ile şifrele
//                        Cipher aesCipherExtra = Cipher.getInstance("AES/GCM/NoPadding");
//                        aesCipherExtra.init(Cipher.ENCRYPT_MODE, aesKeyExtra, new GCMParameterSpec(128, ivExtra));
//                        String extraMessage = "Ekstra mesaj " + j;
//                        byte[] encryptedMessageExtra = aesCipherExtra.doFinal(extraMessage.getBytes(StandardCharsets.UTF_8));
//
//                        // d. AES anahtarını RSA ile şifrele
//                        Cipher rsaCipherExtra = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
//                        OAEPParameterSpec oaepParamsExtra = new OAEPParameterSpec(
//                                "SHA-256",
//                                "MGF1",
//                                MGF1ParameterSpec.SHA256,
//                                PSource.PSpecified.DEFAULT
//                        );
//                        rsaCipherExtra.init(Cipher.ENCRYPT_MODE, recipientPublicKey, oaepParamsExtra);
//                        byte[] encryptedAESKeyExtra = rsaCipherExtra.doFinal(aesKeyExtra.getEncoded());
//
//                        // fullDateTime rastgele üret
//                        long startEpoch = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli(); // son 30 gün
//                        long endEpoch = Instant.now().toEpochMilli();
//                        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
//                        Instant randomDateTime = Instant.ofEpochMilli(randomEpoch);
//
//                        ChatMessage extraChatMessage = ChatMessage.builder()
//                                .chatRoomId(chatRoom.getId())
//                                .senderId(userId)
//                                .recipientId(friendId)
//                                .encryptedMessageContent(encryptedMessageExtra)
//                                .iv(ivExtra)
//                                .encryptedKeyForRecipient(encryptedAESKeyExtra)
//                                .encryptedKeyForSender(encryptedAESKeyExtra)
//                                .fullDateTime(randomDateTime)
//                                .isSeen(false)
//                                .build();
//                        chatMessageRepository.save(extraChatMessage);
//                    }
//                    System.out.println("i==100 için 200 ekstra mesaj eklendi.");
//                }
//
//                ChatMessage chatMessage = ChatMessage.builder()
//                        .chatRoomId(chatRoom.getId())
//                        .senderId(userId)
//                        .recipientId(friendId)
//                        .encryptedMessageContent(encryptedMessage)
//                        .iv(iv)
//                        .encryptedKeyForRecipient(encryptedAESKey)
//                        .encryptedKeyForSender(encryptedAESKey)
//                        .fullDateTime(Instant.now())
//                        .isSeen(false)
//                        .build();
//                chatMessageRepository.save(chatMessage);
//
//                UserChatSettings userChatSettings = new UserChatSettings();
//                userChatSettings.setUserId(userId);
//                userChatSettings.setDeleted(false);
//                userChatSettings.setPinned(false);
//                userChatSettings.setBlocked(false);
//                userChatSettings.setArchived(false);
//                userChatSettings.setDeletedTime(null);
//                userChatSettings.setUnblockedTime(null);
//                userChatSettings.setBlockedTime(null);
//                userChatSettings.setUnreadMessageCount(0);
//                userChatSettings.setChatRoomId(chatRoom.getId());
//                userChatSettingsRepository.save(userChatSettings);
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
//                userChatSettings1.setUnreadMessageCount(1);
//                userChatSettings1.setChatRoomId(chatRoom.getId());
//                userChatSettingsRepository.save(userChatSettings1);
//
//                System.out.println("Kullanıcı eklendi: " + username + " ChatRoomId > " + userChatSettings.getChatRoomId());
//            }
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
