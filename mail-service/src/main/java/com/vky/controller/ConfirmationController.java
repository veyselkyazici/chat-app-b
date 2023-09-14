package com.vky.controller;

import com.vky.HttpResponse;
import com.vky.dto.request.CreateConfirmationRequestDTO;
import com.vky.service.ConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/confirmation")
@RequiredArgsConstructor
public class ConfirmationController {
    private final ConfirmationService confirmationService;

    @PostMapping("/create-confirmation")
    public ResponseEntity<Void> createConfirmation(@RequestBody CreateConfirmationRequestDTO createConfirmationRequestDTO) {
        confirmationService.createConfirmation(createConfirmationRequestDTO);
        System.out.println(createConfirmationRequestDTO.toString());
        return ResponseEntity.ok().build();
    }
    @GetMapping()
    public ResponseEntity<HttpResponse> confirmUserAccount(@RequestParam("token") String verificationToken) {
        System.out.println("Token: " + verificationToken);
        Boolean isSuccess = confirmationService.verifyToken(verificationToken);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("Success", isSuccess))
                        .message("Account Verified")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }


}
