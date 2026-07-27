package com.swrve.sdk.localstorage;

import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface IFastInsertLocalStorage {
    void addMultipleClickThrus(List<Map.Entry<Integer, String>> list);

    void addMultipleEvent(List<String> list);

    void setMultipleCacheEntries(List<Map.Entry<String, Map.Entry<String, String>>> list);
}
