package com.vky.service;

import java.time.Instant;

public record UserStatusEvent(String userId, String status, Instant lastSeen, boolean initial) {

}
