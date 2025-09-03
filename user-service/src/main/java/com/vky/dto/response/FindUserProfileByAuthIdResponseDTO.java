package com.vky.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindUserProfileByAuthIdResponseDTO {
    private UUID id;
    private UUID authId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String about;
    private String image;
    private Instant updatedAt;

    public FindUserProfileByAuthIdResponseDTO(UUID authId, String email, String firstName, Instant updatedAt) {
        this.authId = authId;
        this.email = email;
        this.firstName = firstName;
        this.updatedAt = updatedAt;
    }
}
