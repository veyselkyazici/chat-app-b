package com.vky.controller;

import com.vky.dto.UserStatusMessage;
import com.vky.service.StatusBroadcastService;
import com.vky.service.UserStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class StatusController {

    private final ApplicationEventPublisher eventPublisher;
    private final StatusBroadcastService statusBroadcastService;

//    @MessageMapping("/request-status-snapshot")
//    public void requestSnapshot(
//            Principal principal,
//            @Payload RequestOnlineStatus req
//    ) {
//
//        statusBroadcastService.sendSingleSnapshot(
//                principal.getName(),
//                req.getTargetUserId()
//        );
//    }
    public record SnapshotRequest(String targetUserId) {}
    @MessageMapping("/request-status-snapshot")
    public void requestSnapshot(Principal principal, SnapshotRequest req) {
        String viewerId = principal.getName();

        String targetId = req.targetUserId();

        statusBroadcastService.requestSnapshot(viewerId, targetId);
    }
    @MessageMapping("/user-status")
    public void updateStatus(
            Principal principal,
            @Payload UserStatusMessage request
    ) {

        String userId = principal.getName();
        String status = request.getStatus();

        // offline sadece DISCONNECT ile olur
        if ("offline".equals(status)) return;

        eventPublisher.publishEvent(
                new UserStatusEvent(
                        userId,
                        status,
                        null,
                        false
                )
        );
    }
}


