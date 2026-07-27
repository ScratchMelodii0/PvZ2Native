package com.swrve.sdk.runnable;

import com.swrve.sdk.ISwrveUserResourcesDiffListener;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public abstract class UIThreadSwrveResourcesDiffRunnable extends UIThreadSwrveRunnable implements ISwrveUserResourcesDiffListener {
    private Map<String, Map<String, String>> newResourcesValues;
    private Map<String, Map<String, String>> oldResourcesValues;
    private String resourcesAsJSON;

    @Override // java.lang.Runnable
    public void run() {
        onUserResourcesDiffSuccess(this.oldResourcesValues, this.newResourcesValues, this.resourcesAsJSON);
    }

    public void setData(Map<String, Map<String, String>> oldResourcesValues, Map<String, Map<String, String>> newResourcesValues, String resourcesAsJSON) {
        this.oldResourcesValues = oldResourcesValues;
        this.newResourcesValues = newResourcesValues;
        this.resourcesAsJSON = resourcesAsJSON;
    }
}
