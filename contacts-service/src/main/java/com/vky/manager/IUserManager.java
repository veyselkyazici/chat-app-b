package com.vky.manager;

import com.vky.dto.request.FeignClientIdsRequestDTO;
import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.*;
import com.vky.repository.ContactWithRelationshipDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {
    @GetMapping("/feign-client-get-userId")
    TokenResponseDTO feignClientGetUserId(@RequestHeader("Authorization") String authorization);
    @GetMapping("/get-user")
    UserProfileResponseDTO getUserByEmail(@RequestParam("contactEmail") String contactEmail);
    @GetMapping("/get-user-email-by-id")
    String getUserByEmailByIdd(@RequestParam("id") UUID id);
    @PostMapping("/get-user-list")
    List<FeignClientUserProfileResponseDTO> getUserList(List<FeignClientUserProfileRequestDTO> userRequestDTOList);
    @PostMapping("/get-user-listt")
    List<FeignClientUserProfileResponseDTO> getUserListt(List<ContactWithRelationshipDTO> userRequestDTOList);
}
