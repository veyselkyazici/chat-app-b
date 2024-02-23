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
    private String userEmail;
    private UUID friendUserId;
    private String friendUserEmail;
}
