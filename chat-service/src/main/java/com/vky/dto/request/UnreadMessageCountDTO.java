package com.vky.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnreadMessageCountDTO {
    private String chatRoomId;
    private String userId;
    private int unreadMessageCount;
}
