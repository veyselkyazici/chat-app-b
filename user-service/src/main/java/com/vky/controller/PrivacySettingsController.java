package com.vky.controller;

import com.vky.service.PrivacySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/privacy-settings")
public class PrivacySettingsController {
    private final PrivacySettingsService privacySettingsService;
}
