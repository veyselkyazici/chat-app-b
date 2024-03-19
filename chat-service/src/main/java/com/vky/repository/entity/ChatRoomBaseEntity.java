package com.vky.repository.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@SuperBuilder
@NoArgsConstructor
@Data
public class ChatRoomBaseEntity {
    @Id
    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private boolean isDeleted;


    @CreatedDate
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = null;
    }

    @LastModifiedDate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
