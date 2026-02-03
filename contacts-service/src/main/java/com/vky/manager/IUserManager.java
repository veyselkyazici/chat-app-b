package com.vky.manager;

import com.vky.dto.request.UpdateLastSeenRequestDTO;
import com.vky.dto.request.UpdateSettingsDTO;
import com.vky.dto.response.ContactResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/user",dismiss404 = true)
public interface IUserManager {
    @GetMapping("/get-user")
    UserProfileResponseDTO getUserByEmail(@RequestParam("contactEmail") String contactEmail);
    @GetMapping("/get-user-email-by-id")
    String getUserByEmailByIdd(@RequestParam("id") UUID id);

    @PostMapping("/get-users")
    List<ContactResponseDTO> getUsers(List<UUID> ids);
    @PostMapping("/get-user-by-id")
    UserProfileResponseDTO getUserById(@RequestBody UUID userId);

    @PutMapping("/internal/update-user-last-seen")
    void updateLastSeen(@RequestBody UpdateLastSeenRequestDTO request);

    @PostMapping("/get-user-by-id-with-out-user-key")
    UpdateSettingsDTO getFeignUserByIdWithOutUserKey(@RequestBody UUID userId);
}
