package com.vky;

public class WsAuthException extends RuntimeException {
    private final String code;

    public WsAuthException(String code) {
        super(code);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
