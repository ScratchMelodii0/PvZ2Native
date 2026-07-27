package com.swrve.sdk.rest;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface IRESTResponseListener {
    void onException(Exception exc);

    void onResponse(int i, String str);
}
