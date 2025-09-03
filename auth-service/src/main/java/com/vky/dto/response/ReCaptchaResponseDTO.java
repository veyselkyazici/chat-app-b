package com.vky.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReCaptchaResponseDTO {
    private boolean success;
    private float score;
    private String action;
    private String challenge_ts;
    private String hostname;
    private List<String> errorCodes;

}
