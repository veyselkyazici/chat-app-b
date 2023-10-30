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
public class NewUserCreateDTO {
    /**
     * yeni uyelik acmis birisinin outh_id bilgisinin tutuldugu alan
     */
    private UUID authId;
    private String email;
}
