package com.vky.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FriendRequestReplyRequestDTOWS {
    private boolean isAccepted;
    private UUID userId;
    private UUID friendUserId;
}
