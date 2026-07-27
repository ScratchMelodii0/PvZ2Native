package com.facebook.android;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class FacebookError extends RuntimeException {
    private static final long serialVersionUID = 1;
    private int mErrorCode;
    private String mErrorType;

    @Deprecated
    public FacebookError(String message) {
        super(message);
        this.mErrorCode = 0;
    }

    @Deprecated
    public FacebookError(String message, String type, int code) {
        super(message);
        this.mErrorCode = 0;
        this.mErrorType = type;
        this.mErrorCode = code;
    }

    @Deprecated
    public int getErrorCode() {
        return this.mErrorCode;
    }

    @Deprecated
    public String getErrorType() {
        return this.mErrorType;
    }
}
