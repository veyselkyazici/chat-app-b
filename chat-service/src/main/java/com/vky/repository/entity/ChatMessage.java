package com.vky.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Document(collection = "chat_messages")
public class ChatMessage extends BaseEntity {
    private String chatRoomId;
    private String senderId;
    private String recipientId;

    private byte[] encryptedMessageContent;
    private byte[] iv;
    private byte[] encryptedKeyForRecipient;

    private byte[] encryptedKeyForSender;

    private boolean isSeen;
    private Instant fullDateTime;
}
