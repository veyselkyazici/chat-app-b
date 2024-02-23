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
public class FeignClientUserProfileResponseDTO {
    private UUID id;
    private String email;
    private String about;
    private String friendName;
    private UUID imageId;
}
