package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FeignClientIdsResponseDTO {
    private UUID userId;
    private UUID friendUserId;
    private String userEmail;
    private String friendUserEmail;
}
