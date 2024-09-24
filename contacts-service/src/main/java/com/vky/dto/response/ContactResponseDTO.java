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
public class ContactResponseDTO  {
    private UUID id;
    private UserProfileResponseDTO userProfileResponseDTO;
    private String userContactName;
}