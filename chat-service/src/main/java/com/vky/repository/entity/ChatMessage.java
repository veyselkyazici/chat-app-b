package com.vky.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Document
public class ChatMessage extends BaseEntity{
    private String chatRoomId;
    private String senderId;
    private String recipientId;
    private String messageContent;
    private boolean isSeen;
    private LocalDateTime fullDateTime;
}
