package com.swrve.sdk.runnable;

import com.swrve.sdk.ISwrveUserResourcesListener;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public abstract class UIThreadSwrveResourcesRunnable extends UIThreadSwrveRunnable implements ISwrveUserResourcesListener {
    private Map<String, Map<String, String>> resources;
    private String resourcesAsJSON;

    @Override // java.lang.Runnable
    public void run() {
        if (this.exception != null) {
            onUserResourcesError(this.exception);
        } else {
            onUserResourcesSuccess(this.resources, this.resourcesAsJSON);
        }
    }

    public void setData(Map<String, Map<String, String>> resources, String resourcesAsJSON) {
        this.resources = resources;
        this.resourcesAsJSON = resourcesAsJSON;
    }
}
