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
//    CommandLineRunner loadData(IChatMessageRepository chatMessageRepository, IChatRoomRepository chatRoomRepository, IUserChatSettingsRepository chatSettingsRepository) {
//        return args -> {
//            List<ChatRoom> chatRooms = new ArrayList<>();
//            List<ChatMessage> chatMessages = new ArrayList<>();
//            List<UserChatSettings> userChatSettingsList = new ArrayList<>();
//            UUID fixedUUID = UUID.fromString("7c6f47eb-dad2-44b3-a036-ffbf92343ae2");
//
//            for (int i = 0; i < 80; i++) {
//                String otherUserId = UUID.randomUUID().toString();
//
//                String chatRoomId = UUID.randomUUID().toString();
//                ChatRoom chatRoom = ChatRoom.builder()
//                        .participantIds(List.of(fixedUUID.toString(), otherUserId))
//                        .build();
//                chatRooms.add(chatRoom);
//
//                ChatMessage chatMessage = ChatMessage.builder()
//                        .chatRoomId(chatRoomId)
//                        .senderId(fixedUUID.toString())
//                        .recipientId(otherUserId)
//                        .messageContent("Hello, this is a message in chat room " + (i + 1))
//                        .isSeen(false)
//                        .fullDateTime(Instant.now())
//                        .build();
//
//                if (chatMessage.getChatRoomId() != null && chatMessage.getMessageContent() != null) {
//                    chatMessages.add(chatMessage);
//                } else {
//                    System.err.println("Failed to create a valid ChatMessage for chat room ID: " + chatRoomId);
//                }
//
//                UserChatSettings userChatSettings1 = UserChatSettings.builder()
//                        .userId(fixedUUID.toString())
//                        .chatRoomId(chatRoomId)
//                        .unreadMessageCount(0)
//                        .isArchived(false)
//                        .isPinned(false)
//                        .isBlocked(false)
//                        .build();
//
//                UserChatSettings userChatSettings2 = UserChatSettings.builder()
//                        .userId(otherUserId)
//                        .chatRoomId(chatRoomId)
//                        .unreadMessageCount(1)
//                        .isArchived(false)
//                        .isPinned(false)
//                        .isBlocked(false)
//                        .build();
//
//                if (userChatSettings1.getUserId() != null && userChatSettings1.getChatRoomId() != null &&
//                        userChatSettings2.getUserId() != null && userChatSettings2.getChatRoomId() != null) {
//                    userChatSettingsList.add(userChatSettings1);
//                    userChatSettingsList.add(userChatSettings2);
//                } else {
//                    System.err.println("Failed to create valid UserChatSettings for chat room ID: " + chatRoomId);
//                }
//            }
//
//            // Save all entities if they were created properly
//            if (!chatRooms.isEmpty() && !chatMessages.isEmpty() && !userChatSettingsList.isEmpty()) {
//                chatRoomRepository.saveAll(chatRooms);
//                chatMessageRepository.saveAll(chatMessages);
//                chatSettingsRepository.saveAll(userChatSettingsList);
//            } else {
//                System.err.println("No entities were saved. Please check the data creation logic.");
//            }
//        };
//    }
}
