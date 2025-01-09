package com.vky.controller;

import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.request.ContactRequestDTO;
import com.vky.dto.response.DeleteContactResponseDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import com.vky.dto.response.UserProfileResponseDTO;
import com.vky.service.ContactsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@RestController
public class ContactsController {
    private final ContactsService contactsService;

    @PostMapping("/add-contact")
    public ResponseEntity<Void> addContact(@RequestBody ContactRequestDTO contactRequestDTO) {
        contactsService.addContact(contactRequestDTO);
        return ResponseEntity.ok().build();
    }

    //    @GetMapping("/get-contact-list")
//    public ResponseEntity<List<FeignClientUserProfileResponseDTO>> getFriendList(@RequestParam("userId") UUID userId) {
//        System.out.println("USERID>> " + userId);
//        return ResponseEntity.ok(contactsService.getContactList(userId));
//    }
    @GetMapping("/get-contact-list")
    public CompletableFuture<ResponseEntity<List<FeignClientUserProfileResponseDTO>>> getFriendListAsync(@RequestParam("userId") UUID userId) {
        System.out.println("USERID>> " + userId);
        return contactsService.getContactListAsync(userId)
                .thenApply(ResponseEntity::ok);
    }

    //    @PostMapping("/get-contact-information-of-existing-chats")
//    public List<FeignClientUserProfileResponseDTO> getContactInformationOfExistingChats(@RequestBody ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {
//        System.out.println("USERID 123>> " + contactInformationOfExistingChatsRequestDTO);
//        return contactsService.getContactInformationOfExistingChats(contactInformationOfExistingChatsRequestDTO);
//    }
    @PostMapping("/get-contact-information-of-existing-chats")
    public CompletableFuture<List<FeignClientUserProfileResponseDTO>> getContactInformationOfExistingChats(@RequestBody ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO) {
        return contactsService.getContactInformationOfExistingChatsAsync(contactInformationOfExistingChatsRequestDTO);
    }

    @PostMapping("/get-contact-information-of-existing-chat")
    public FeignClientUserProfileResponseDTO getContactInformationOfExistingChat(@RequestBody ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO) {
        System.out.println("USERID 123>> " + contactInformationOfExistingChatRequestDTO);
        return contactsService.getContactInformationOfSingleChat(contactInformationOfExistingChatRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteContactResponseDTO> deleteContact(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(contactsService.deleteContact(id));
    }

    @MessageMapping("/update-privacy")
    public void typing(@Payload UserProfileResponseDTO userProfileResponseDTO) {
        System.out.println("Message > " + userProfileResponseDTO);
        contactsService.sendUpdatedPrivacySettings(userProfileResponseDTO);
    }

}
