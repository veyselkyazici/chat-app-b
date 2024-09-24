package com.vky.dto.request;

import com.vky.dto.response.UserProfileResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FeignClientUserProfileRequestDTO {
    private UUID id;
    private UserProfileResponseDTO userProfileResponseDTO;
    private String userContactName;
}
