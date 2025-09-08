package com.vky.manager;

import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/user",dismiss404 = true)
public interface IUserManager {
    @GetMapping("/get-user")
    UserProfileResponseDTO getUserByEmail(@RequestParam("contactEmail") String contactEmail);
    @GetMapping("/get-user-email-by-id")
    String getUserByEmailByIdd(@RequestParam("id") UUID id);

    @PostMapping("/get-users")
    List<FeignClientUserProfileResponseDTO> getUsers(List<UUID> ids);
    @PostMapping("/get-user-by-id")
    UserProfileResponseDTO getFeignUserById(@RequestBody UUID userId);
}
