package com.facebook.internal;

import com.facebook.Request;
import com.facebook.RequestBatch;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class CacheableRequestBatch extends RequestBatch {
    private String cacheKey;
    private boolean forceRoundTrip;

    public CacheableRequestBatch() {
    }

    public CacheableRequestBatch(Request... requests) {
        super(requests);
    }

    public final String getCacheKeyOverride() {
        return this.cacheKey;
    }

    public final void setCacheKeyOverride(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public final boolean getForceRoundTrip() {
        return this.forceRoundTrip;
    }

    public final void setForceRoundTrip(boolean forceRoundTrip) {
        this.forceRoundTrip = forceRoundTrip;
    }
}
