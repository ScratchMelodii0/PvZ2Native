package com.swrve.sdk;

import com.swrve.sdk.localstorage.MemoryCachedLocalStorage;
import com.swrve.sdk.rest.IRESTResponseListener;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
abstract class RESTCacheResponseListener implements IRESTResponseListener {
    private String cacheCategory;
    private String defaultValue;
    private MemoryCachedLocalStorage memorylocalStorage;
    private String userId;

    public abstract void onResponseCached(int i, String str);

    public RESTCacheResponseListener(MemoryCachedLocalStorage memoryLocalStorage, String userId, String cacheCategory, String defaultValue) {
        this.memorylocalStorage = memoryLocalStorage;
        this.userId = userId;
        this.cacheCategory = cacheCategory;
        this.defaultValue = defaultValue;
    }

    @Override // com.swrve.sdk.rest.IRESTResponseListener
    public void onResponse(int responseStatus, String responseBody) {
        String rawResponse = responseStatus == 200 ? responseBody : this.memorylocalStorage.getCacheEntryForUser(this.userId, this.cacheCategory);
        if (responseStatus == 200) {
            this.memorylocalStorage.setCacheEntryForUser(this.userId, this.cacheCategory, responseBody);
            if (this.memorylocalStorage.getSecondaryStorage() != null) {
                this.memorylocalStorage.getSecondaryStorage().setCacheEntryForUser(this.userId, this.cacheCategory, responseBody);
            }
        }
        if (rawResponse == null || rawResponse.equals("")) {
            rawResponse = this.defaultValue;
        }
        onResponseCached(responseStatus, rawResponse);
    }
}
