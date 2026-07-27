package com.facebook.android;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class DialogError extends Throwable {
    private static final long serialVersionUID = 1;
    private int mErrorCode;
    private String mFailingUrl;

    @Deprecated
    public DialogError(String message, int errorCode, String failingUrl) {
        super(message);
        this.mErrorCode = errorCode;
        this.mFailingUrl = failingUrl;
    }

    @Deprecated
    public int getErrorCode() {
        return this.mErrorCode;
    }

    @Deprecated
    public String getFailingUrl() {
        return this.mFailingUrl;
    }
}
