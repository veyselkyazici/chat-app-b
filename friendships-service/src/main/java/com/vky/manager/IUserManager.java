package com.vky.manager;

import com.vky.dto.request.FeignClientIdsRequestDTO;
import com.vky.dto.request.FeignClientUserProfileRequestDTO;
import com.vky.dto.response.FeignClientIdsResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.TokenResponseDTO;
import com.vky.dto.response.UserIdResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(url = "${raceapplication.url.user}api/v1/user",name = "user-service-userprofile",dismiss404 = true)
public interface IUserManager {
    @GetMapping("/feign-client-get-userId")
    TokenResponseDTO feignClientGetUserId(@RequestHeader("Authorization") String authorization);

    @PostMapping("/get-user-list")
    List<FeignClientUserProfileResponseDTO> getUserList(List<FeignClientUserProfileRequestDTO> userRequestDTOList);
}
