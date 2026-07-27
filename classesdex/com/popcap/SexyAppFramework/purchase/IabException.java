package com.popcap.SexyAppFramework.purchase;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class IabException extends Exception {
    private static final long serialVersionUID = 7676912630130593597L;
    IabResult mResult;

    public IabException(IabResult r) {
        this(r, (Exception) null);
    }

    public IabException(int response, String message) {
        this(new IabResult(response, message));
    }

    public IabException(IabResult r, Exception cause) {
        super(r.getMessage(), cause);
        this.mResult = r;
    }

    public IabException(int response, String message, Exception cause) {
        this(new IabResult(response, message), cause);
    }

    public IabResult getResult() {
        return this.mResult;
    }
}
