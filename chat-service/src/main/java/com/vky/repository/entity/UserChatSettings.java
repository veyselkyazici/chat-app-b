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
@Document
public class UserChatSettings extends BaseEntity{
    private String userId;
    private String chatRoomId;
    private int unreadMessageCount;
    private boolean isArchived;
    private boolean isPinned;
    private boolean isBlocked;
    private Instant deletedTime;
}
