package com.vky.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EditProfileRequestDTO {
    @NotNull
    String token;
    String username;
    String name;
    String surname;
    String email;
    String phone;
    String photo;
    String address;
    String about;
}
