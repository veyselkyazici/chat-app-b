package com.vky.dto.response;

import com.vky.repository.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserProfileIdStringDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Image image;
}
