package com.vky.repository.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ChatNotification extends BaseEntity {
    private String senderEmail;
    private String recipientEmail;
    private String content;

    public ChatNotification(String id, String senderEmail, String recipientEmail, String content) {
        this.setId(id);
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.content = content;
    }
}
