package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.ApiResponse;
import com.vky.dto.response.ContactResponseDTO;
import com.vky.exception.ContactsServiceException;
import com.vky.exception.ErrorMessage;
import com.vky.service.ContactsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@RestController
public class ContactsController {
    private final ContactsService contactsService;

    @PostMapping("/add-contact")
    public ResponseEntity<ApiResponse<Void>> addContact(
            @RequestBody ContactRequestDTO dto,
            @RequestHeader("X-Id") String tokenUserId) {
        contactsService.addContact(dto, tokenUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Contact added successfully", null));
    }

    @GetMapping("/get-contact-list")
    public CompletableFuture<ResponseEntity<ApiResponse<List<ContactResponseDTO>>>> getFriendListAsync(@RequestHeader("X-Id") String tokenUserId) {
        return contactsService.getContactList(tokenUserId)
                .thenApply(dto -> ResponseEntity.ok(new ApiResponse<>(true, "success", dto)))
                .completeOnTimeout(
                        ResponseEntity.status(408).body(new ApiResponse<>(false, "Request timeout", null)),
                        60, TimeUnit.SECONDS
                );
    }

    @PostMapping("/get-contact-information-of-existing-chats")
    public CompletableFuture<List<ContactResponseDTO>>  getContactInformationOfExistingChats(@RequestBody ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {
        return contactsService.getContactInformationOfExistingChats(contactInformationOfExistingChatsRequestDTO);
    }

    @PostMapping("/get-contact-information-of-existing-chat")
    public CompletableFuture<ContactResponseDTO>  getContactInformationOfExistingChat(@RequestBody ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO) {
        System.out.println("USERID 123>> " + contactInformationOfExistingChatRequestDTO);
        return contactsService.getContactInformationOfSingleChat(contactInformationOfExistingChatRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable("id") UUID id, @RequestHeader("X-Id") String tokenUserId) {
        contactsService.deleteContact(id,tokenUserId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/updated-privacy-send-message")
    public void typing(@Payload UpdatePrivacySettingsRequestDTO updatePrivacySettingsRequestDTO) {
        contactsService.sendUpdatedPrivacySettings(updatePrivacySettingsRequestDTO);
    }
    @MessageMapping("/updated-user-profile-send-message")
    public void updatedUserProfileSendMessage(@Payload UpdatedProfilePhotoRequestDTO dto) {
        contactsService.sendUserProfile(dto);
    }
}
