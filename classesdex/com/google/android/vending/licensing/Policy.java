package com.google.android.vending.licensing;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface Policy {
    public static final int LICENSED = 256;
    public static final int NOT_LICENSED = 561;
    public static final int RETRY = 291;

    boolean allowAccess();

    void processServerResponse(int i, ResponseData responseData);
}
