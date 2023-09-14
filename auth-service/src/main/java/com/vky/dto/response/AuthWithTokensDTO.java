package com.vky.dto.response;

import com.vky.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AuthWithTokensDTO {
    private UUID id;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean isEnabled;
    private List<TokenDTO> tokens;
}
