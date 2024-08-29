package com.vky.repository;

public enum ContactsStatus {
    SENT("Arkadaşlık İsteği Gönderildi"),
    APPROVED("Arkadaşlık İsteği Onaylandı"),
    DENIED("Arkadaşlık İsteği Reddedildi");

    private final String label;

    ContactsStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}