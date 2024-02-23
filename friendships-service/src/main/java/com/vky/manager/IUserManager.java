package com.vky.manager;

import com.vky.dto.request.FeignClientIdsRequestDTO;
import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.FeignClientIdsResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {
    @PostMapping("/find-ids")
    FeignClientIdsResponseDTO findIds(FeignClientIdsRequestDTO dto);
    @PostMapping("/get-user-id")
    UUID getUserId(String token);

    @PostMapping("/get-user-list")
    List<FeignClientUserProfileResponseDTO> getUserList(List<FeignClientUserProfileRequestDTO> userRequestDTOList);
}
