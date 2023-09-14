package com.vky.dto;

import com.vky.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminGenerateRequestDTO {
    private String email;
    private String password;
    private Role role;
}
