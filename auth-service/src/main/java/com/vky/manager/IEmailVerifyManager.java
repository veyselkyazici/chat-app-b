package com.vky.manager;

import com.vky.dto.request.CreateConfirmationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${raceapplication.url.mail}api/v1/confirmation",name = "mail-service-confirmation",dismiss404 = true)
public interface IEmailVerifyManager {
    @PostMapping("/create-confirmation")
    ResponseEntity<Void> createConfirmation(CreateConfirmationRequestDTO createConfirmationRequestDTO);

}
