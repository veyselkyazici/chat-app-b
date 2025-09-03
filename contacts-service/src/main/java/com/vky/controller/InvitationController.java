package com.vky.controller;

import com.vky.dto.request.SendInvitationDTO;
import com.vky.dto.response.DeleteContactResponseDTO;
import com.vky.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invitation")
public class InvitationController {

    private final InvitationService invitationService;

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteContactResponseDTO> deleteContact(@PathVariable("id") UUID id, @RequestHeader("X-Id") String tokenUserId) {
        DeleteContactResponseDTO responseDTO = invitationService.deleteInvitation(id, tokenUserId);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/send-invitation")
    public ResponseEntity<String> sendInvitation(@RequestBody SendInvitationDTO sendInvitation, @RequestHeader("X-Id") String tokenUserId) {
        String response = invitationService.sendInvitation(sendInvitation, tokenUserId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
