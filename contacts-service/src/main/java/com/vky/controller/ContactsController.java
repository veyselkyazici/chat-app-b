package com.vky.controller;

import com.vky.dto.request.*;
import com.vky.dto.response.ApiResponse;
import com.vky.dto.response.DeleteContactResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.service.ContactsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@RestController
public class ContactsController {
    private final ContactsService contactsService;

    @PostMapping("/add-contact")
    public ResponseEntity<Void> addContact(@RequestBody ContactRequestDTO contactRequestDTO, @RequestHeader("X-Id") String tokenUserId) {
        contactsService.addContact(contactRequestDTO, tokenUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-contact-list")
    public CompletableFuture<ResponseEntity<ApiResponse<List<FeignClientUserProfileResponseDTO>>>> getFriendListAsync(@RequestHeader("X-Id") String tokenUserId) {
        return contactsService.getContactList(tokenUserId)
                .thenApply(dto -> ResponseEntity.ok(new ApiResponse<>(true, "success", dto)))
                .completeOnTimeout(
                        ResponseEntity.status(408).body(new ApiResponse<>(false, "Request timeout", null)),
                        60, TimeUnit.SECONDS
                );
    }

    @PostMapping("/get-contact-information-of-existing-chats")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>>  getContactInformationOfExistingChats(@RequestBody ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {
        return contactsService.getContactInformationOfExistingChats(contactInformationOfExistingChatsRequestDTO);
    }

    @PostMapping("/get-contact-information-of-existing-chat")
    public CompletableFuture<FeignClientUserProfileResponseDTO>  getContactInformationOfExistingChat(@RequestBody ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO) {
        System.out.println("USERID 123>> " + contactInformationOfExistingChatRequestDTO);
        return contactsService.getContactInformationOfSingleChat(contactInformationOfExistingChatRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DeleteContactResponseDTO>> deleteContact(@PathVariable("id") UUID id, @RequestHeader("X-Id") String tokenUserId) {
        return ResponseEntity.ok(new ApiResponse<>(true,"success",contactsService.deleteContact(id,tokenUserId)));
    }

    @MessageMapping("/updated-privacy-send-message")
    public void typing(@Payload UpdatePrivacySettingsRequestDTO updatePrivacySettingsRequestDTO) {
        contactsService.sendUpdatedPrivacySettings(updatePrivacySettingsRequestDTO);
    }
    @MessageMapping("/updated-profile-photo-send-message")
    public void typing(@Payload UpdatedProfilePhotoRequestDTO dto) {
        contactsService.sendUpdatedProfilePhoto(dto);
    }

}
