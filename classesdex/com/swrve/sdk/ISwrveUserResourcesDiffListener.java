package com.swrve.sdk;

import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface ISwrveUserResourcesDiffListener {
    void onUserResourcesDiffError(Exception exc);

    void onUserResourcesDiffSuccess(Map<String, Map<String, String>> map, Map<String, Map<String, String>> map2, String str);
}
