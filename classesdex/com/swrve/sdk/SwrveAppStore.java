package com.swrve.sdk;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public enum SwrveAppStore {
    GooglePlay,
    AmazonAppStore;

    public String toArgument() {
        return this == AmazonAppStore ? "amazon" : "google";
    }
}
