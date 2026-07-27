package com.google.android.gms.auth;

import android.content.Intent;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GooglePlayServicesAvailabilityException extends UserRecoverableAuthException {
    private final int y;

    GooglePlayServicesAvailabilityException(int connectionStatusCode, String msg, Intent intent) {
        super(msg, intent);
        this.y = connectionStatusCode;
    }

    public int getConnectionStatusCode() {
        return this.y;
    }
}
