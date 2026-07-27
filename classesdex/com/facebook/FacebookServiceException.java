package com.facebook;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class FacebookServiceException extends FacebookException {
    private static final long serialVersionUID = 1;
    private final FacebookRequestError error;

    public FacebookServiceException(FacebookRequestError error, String errorMessage) {
        super(errorMessage);
        this.error = error;
    }

    public final FacebookRequestError getRequestError() {
        return this.error;
    }

    @Override // java.lang.Throwable
    public final String toString() {
        return "{FacebookServiceException: httpResponseCode: " + this.error.getRequestStatusCode() + ", facebookErrorCode: " + this.error.getErrorCode() + ", facebookErrorType: " + this.error.getErrorType() + ", message: " + this.error.getErrorMessage() + "}";
    }
}
