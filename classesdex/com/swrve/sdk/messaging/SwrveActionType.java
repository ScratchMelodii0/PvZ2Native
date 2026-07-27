package com.swrve.sdk.messaging;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public enum SwrveActionType {
    Dismiss,
    Custom,
    Install;

    public static SwrveActionType parse(String type) {
        if (type.toUpperCase().equals("INSTALL")) {
            return Install;
        }
        if (type.toUpperCase().equals("DISMISS")) {
            return Dismiss;
        }
        return Custom;
    }
}
