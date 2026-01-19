package com.vky.manager;

import com.vky.dto.request.SendInvitationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "mail-service", path = "/api/v1/mail",dismiss404 = true)
public interface IMailManager {
    @PostMapping("/send-invitation-email")
    ResponseEntity<Void> sendInvitation(SendInvitationDTO sendInvitationDTO);
}