package com.vky.controller;

import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.response.ApiResponse;
import com.vky.dto.response.ContactResponseDTO;
import com.vky.service.ContactsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{userId}/snapshot")
    public RelationshipSnapshotDTO snapshot(@PathVariable UUID userId) {
        return contactsService.snapshot(userId);
    }

    public record RelationshipSnapshotDTO(String userId, List<String> relatedUserIds, List<String> outgoingContactIds) {}

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
}
