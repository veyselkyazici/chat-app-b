package com.vky.controller;

import com.vky.dto.request.SendInvitationDTO;
import com.vky.service.IInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invitation")
public class InvitationController {

    private final IInvitationService invitationService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable("id") UUID id, @RequestHeader("X-Id") String tokenUserId) {
        invitationService.deleteInvitation(id, tokenUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-invitation")
    public ResponseEntity<Void> sendInvitation(@RequestBody SendInvitationDTO sendInvitation,
            @RequestHeader("X-Id") String tokenUserId) {
        invitationService.sendInvitation(sendInvitation, tokenUserId);
        return ResponseEntity.ok().build();
    }
}
