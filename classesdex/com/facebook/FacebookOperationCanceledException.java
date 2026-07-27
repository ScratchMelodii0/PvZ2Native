package com.facebook;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class FacebookOperationCanceledException extends FacebookException {
    static final long serialVersionUID = 1;

    public FacebookOperationCanceledException() {
    }

    public FacebookOperationCanceledException(String message) {
        super(message);
    }

    public FacebookOperationCanceledException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FacebookOperationCanceledException(Throwable throwable) {
        super(throwable);
    }
}
