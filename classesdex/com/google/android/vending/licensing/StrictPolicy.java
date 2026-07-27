package com.google.android.vending.licensing;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class StrictPolicy implements Policy {
    private int mLastResponse = Policy.RETRY;

    @Override // com.google.android.vending.licensing.Policy
    public void processServerResponse(int response, ResponseData rawData) {
        this.mLastResponse = response;
    }

    @Override // com.google.android.vending.licensing.Policy
    public boolean allowAccess() {
        return this.mLastResponse == 256;
    }
}
