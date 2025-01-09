package com.vky.manager;

import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.TokenResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.repository.ContactWithRelationshipDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {
    @GetMapping("/get-user")
    UserProfileResponseDTO getUserByEmail(@RequestParam("contactEmail") String contactEmail);
    @GetMapping("/get-user-email-by-id")
    String getUserByEmailByIdd(@RequestParam("id") UUID id);

    @PostMapping("/get-users-of-contacts")
    List<FeignClientUserProfileResponseDTO> getUsersOfContacts(List<FeignClientUserProfileRequestDTO> userRequestDTOList);

    @PostMapping("/get-users-of-chats")
    List<FeignClientUserProfileResponseDTO> getUsersOfChats(List<ContactWithRelationshipDTO> userRequestDTOList);
    @PostMapping("/feign-get-user-by-id")
    UserProfileResponseDTO getFeignUserById(@RequestBody UUID userId);
}
