package com.vky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsEvent<T> {

    private String targetUserId;   // mesajı alacak kullanıcı
    private String type;           // "message-deliver", "block", "unblock", "error", "read"
    private T payload;             // gönderilen payload

    public static <T> WsEvent<T> delivery(String targetUserId, T payload) {
        return new WsEvent<>(targetUserId, "delivery", payload);
    }

    public static <T> WsEvent<T> block(String targetUserId, T payload) {
        return new WsEvent<>(targetUserId, "block", payload);
    }

    public static <T> WsEvent<T> unblock(String targetUserId, T payload) {
        return new WsEvent<>(targetUserId, "unblock", payload);
    }

    public static <T> WsEvent<T> readRecipient(String targetUserId, T payload) {
        return new WsEvent<>(targetUserId, "read-recipient", payload);
    }

    public static <T> WsEvent<T> readMessages(String targetUserId, T payload) {
        return new WsEvent<>(targetUserId, "read-messages", payload);
    }
    public static <T> WsEvent<T> error(String targetUserId, T payload) {
        return new WsEvent<>(targetUserId, "error", payload);
    }


}
