package com.vky.repository;

public enum FriendshipStatus {
    SENT("Arkadaşlık İsteği Gönderildi"),
    APPROVED("Arkadaşlık İsteği Onaylandı"),
    DENIED("Arkadaşlık İsteği Reddedildi");

    private final String label;

    FriendshipStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}