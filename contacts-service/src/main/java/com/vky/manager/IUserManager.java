package com.vky.manager;

import com.vky.dto.request.UpdateLastSeenRequestDTO;
import com.vky.dto.response.ContactResponseDTO;
import com.vky.dto.response.UserLastSeenResponseDTO;
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
    UserProfileResponseDTO getFeignUserById(@RequestBody UUID userId);

    @GetMapping("/get-user-last-seen")
    UserLastSeenResponseDTO getUserLastSeen(@RequestParam("userId") UUID userId);

    @PutMapping("/internal/update-user-last-seen")
    void updateLastSeen(@RequestBody UpdateLastSeenRequestDTO request);
}
