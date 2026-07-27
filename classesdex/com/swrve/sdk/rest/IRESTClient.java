package com.swrve.sdk.rest;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface IRESTClient {
    public static final int ERROR_CODE = 503;

    void get(String str, IRESTResponseListener iRESTResponseListener);

    void get(String str, Map<String, String> map, IRESTResponseListener iRESTResponseListener) throws UnsupportedEncodingException;

    void post(String str, String str2, IRESTResponseListener iRESTResponseListener);

    void post(String str, String str2, IRESTResponseListener iRESTResponseListener, String str3);
}
