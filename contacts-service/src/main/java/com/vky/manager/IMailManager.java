package com.vky.manager;

import com.vky.dto.request.SendInvitationEmailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(url = "${raceapplication.url.mail}api/v1/mail",name = "mail-service",dismiss404 = true)
public interface IMailManager {
    @PostMapping("/send-invitation-email")
    ResponseEntity<String> sendInvitation(SendInvitationEmailDTO sendInvitationEmailDTO);
}