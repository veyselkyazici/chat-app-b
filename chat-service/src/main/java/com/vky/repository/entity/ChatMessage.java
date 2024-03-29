package com.vky.repository.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

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
    private String message;
    private boolean isSeen;
    private LocalDateTime fullDateTime;
    private LocalTime timeOnly;
}
