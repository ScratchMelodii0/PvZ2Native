package com.swrve.sdk.localstorage;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface IFlushableLocalStorage {
    void flushCache(IFastInsertLocalStorage iFastInsertLocalStorage);

    void flushClickThrus(IFastInsertLocalStorage iFastInsertLocalStorage);

    void flushEvents(IFastInsertLocalStorage iFastInsertLocalStorage);
}
