package com.swrve.sdk.localstorage;

import java.util.Collection;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface ILocalStorage {
    void addClickThru(int i, String str);

    void addEvent(String str) throws Exception;

    void close();

    Map<Map.Entry<String, String>, String> getAllCacheEntries();

    String getCacheEntryForUser(String str, String str2);

    Map<Long, Map.Entry<Integer, String>> getFirstNClickThrus(Integer num);

    Map<Long, String> getFirstNEvents(Integer num);

    void removeClickThrusById(long j);

    void removeEventsById(Collection<Long> collection);

    void reset();

    void setCacheEntryForUser(String str, String str2, String str3);
}
