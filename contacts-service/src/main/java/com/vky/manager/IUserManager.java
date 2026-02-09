package com.vky.manager;

import com.vky.dto.response.ContactResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/user", dismiss404 = true)
public interface IUserManager {
    @GetMapping("/get-user")
    UserProfileResponseDTO getUserByEmail(@RequestParam("contactEmail") String contactEmail, @RequestHeader("X-Id") String requesterId);

    @GetMapping("/get-user-email-by-id")
    String getUserByEmailByIdd(@RequestParam("id") UUID id);

    @PostMapping("/get-users")
    List<ContactResponseDTO> getUsers(@RequestBody List<UUID> ids, @RequestHeader("X-Id") String requesterId);

    @PostMapping("/get-user-by-id")
    UserProfileResponseDTO getUserById(@RequestBody UUID userId, @RequestHeader("X-Id") String requesterId);
}
