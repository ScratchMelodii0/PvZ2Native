package com.swrve.sdk;

import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface ISwrveUserResourcesListener {
    void onUserResourcesError(Exception exc);

    void onUserResourcesSuccess(Map<String, Map<String, String>> map, String str);
}
