package com.facebook;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class FacebookException extends RuntimeException {
    static final long serialVersionUID = 1;

    public FacebookException() {
    }

    public FacebookException(String message) {
        super(message);
    }

    public FacebookException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FacebookException(Throwable throwable) {
        super(throwable);
    }
}
