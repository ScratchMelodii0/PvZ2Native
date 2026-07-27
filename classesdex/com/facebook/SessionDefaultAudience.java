package com.facebook;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public enum SessionDefaultAudience {
    NONE(null),
    ONLY_ME("SELF"),
    FRIENDS("ALL_FRIENDS"),
    EVERYONE("EVERYONE");

    private final String nativeProtocolAudience;

    SessionDefaultAudience(String protocol) {
        this.nativeProtocolAudience = protocol;
    }

    String getNativeProtocolAudience() {
        return this.nativeProtocolAudience;
    }
}
