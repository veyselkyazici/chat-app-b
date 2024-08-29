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
public class UserProfileResponseDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String imagee;
    private String about;
}
