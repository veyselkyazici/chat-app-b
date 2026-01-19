package com.vky.dto.response;

import lombok.*;
import java.util.List;

@Builder(toBuilder = true)
public record ReCaptchaResponseDTO(
        boolean success,
        float score,
        String action,
        String challenge_ts,
        String hostname,
        List<String> errorCodes) {
}
