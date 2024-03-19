package com.vky;

import com.vky.repository.entity.ChatMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.time.LocalTime;

@SpringBootApplication
public class ChatServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);


        ChatMessage message1 = ChatMessage.builder()
                .id("1234")
                .chatRoomId("room1")
                .senderId("sender1")
                .recipientId("recipient1")
                .message("Hello")
                .isSeen(false)
                .fullDateTime(LocalDateTime.now())
                .timeOnly(LocalTime.now())
                .build();

        ChatMessage message2 = ChatMessage.builder()
                .id("1234")
                .chatRoomId("room1")
                .senderId("sender1")
                .recipientId("recipient1")
                .message("Hello")
                .isSeen(false)
                .fullDateTime(LocalDateTime.now())
                .timeOnly(LocalTime.now())
                .build();
        System.out.println(message2 == message1 ? "asdf" : "abc");
        System.out.println("EqualsAndHashCode(callSuper = false) true olduğunda:");
        System.out.println("message1.equals(message2): " + message1.equals(message2)); // false
        System.out.println("message1.hashCode(): " + message1.hashCode());
        System.out.println("message2.hashCode(): " + message2.hashCode());

        System.out.println("message1 ID: " + message1.getId());
        System.out.println("message2 ID: " + message2.getId());
    }
}
