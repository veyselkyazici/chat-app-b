package com.vky.manager;

import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.response.ContactResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "contacts-service", path = "/api/v1/contacts",dismiss404 = true)
public interface IContactsManager {
    @PostMapping("/get-contact-information-of-existing-chats")
    List<ContactResponseDTO> getContactInformationOfExistingChats(@RequestBody ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO);

    @PostMapping("/get-contact-information-of-existing-chat")
    ContactResponseDTO getContactInformationOfExistingChat(@RequestBody ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO);
}
