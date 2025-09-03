package com.vky.manager;

import com.vky.dto.request.ContactInformationOfExistingChatRequestDTO;
import com.vky.dto.request.ContactInformationOfExistingChatsRequestDTO;
import com.vky.dto.response.FeignClientUserProfileResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(url = "${raceapplication.url.contacts}api/v1/contacts",name = "contacts-service",dismiss404 = true)
public interface IContactsManager {
    @PostMapping("/get-contact-information-of-existing-chats")
    List<FeignClientUserProfileResponseDTO> getContactInformationOfExistingChats(@RequestBody ContactInformationOfExistingChatsRequestDTO contactInformationOfExistingChatsRequestDTO);

    @PostMapping("/get-contact-information-of-existing-chat")
    FeignClientUserProfileResponseDTO getContactInformationOfExistingChat(@RequestBody ContactInformationOfExistingChatRequestDTO contactInformationOfExistingChatRequestDTO);
}
