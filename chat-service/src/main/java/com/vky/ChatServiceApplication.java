package com.vky;

import com.vky.repository.IChatMessageRepository;
import com.vky.repository.IChatRoomRepository;
import com.vky.repository.IUserChatSettingsRepository;
import com.vky.repository.entity.ChatMessage;
import com.vky.repository.entity.ChatRoom;
import com.vky.repository.entity.UserChatSettings;
import com.vky.repository.entity.enums.VisibilityOption;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootApplication
@EnableFeignClients
public class ChatServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(IChatRoomRepository chatRoomRepository, IChatMessageRepository chatMessageRepository, IUserChatSettingsRepository userChatSettingsRepository) {
//        return args -> {
//            for (int i = 6; i <= 50; i++) {
//                String username = "User" + i;
//                String email = username.toLowerCase() + "@gmail.com";
//                String friendId = UUID.nameUUIDFromBytes(String.format("User%03d", i).getBytes()).toString();
//
//                String userId = "b87ddabf-c7bf-4de2-b7e3-23ab264ba662";
//                List<String> ids = new ArrayList<>();
//                ids.add(userId);
//                ids.add(friendId);
//               ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().participantIds(ids).build());
//
//
//               ChatMessage chatMessage = ChatMessage.builder()
//                       .chatRoomId(chatRoom.getId())
//                       .messageContent(username)
//                       .fullDateTime(Instant.now())
//                       .senderId(userId)
//                       .recipientId(friendId)
//                       .build();
//               chatMessageRepository.save(chatMessage);
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
//                System.out.println("Kullanıcı eklendi: " + username + " ChatRoomId > " + userChatSettings.getChatRoomId());
//            }
//        };
//    }
}
