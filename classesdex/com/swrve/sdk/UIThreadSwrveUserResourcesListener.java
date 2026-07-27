package com.swrve.sdk;

import android.app.Activity;
import com.swrve.sdk.runnable.UIThreadSwrveResourcesRunnable;
import java.lang.ref.WeakReference;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class UIThreadSwrveUserResourcesListener implements ISwrveUserResourcesListener {
    private final WeakReference<Activity> context;
    private final UIThreadSwrveResourcesRunnable uiWork;

    public UIThreadSwrveUserResourcesListener(Activity context, UIThreadSwrveResourcesRunnable uiWork) {
        this.context = new WeakReference<>(context);
        this.uiWork = uiWork;
    }

    @Override // com.swrve.sdk.ISwrveUserResourcesListener
    public void onUserResourcesSuccess(Map<String, Map<String, String>> resources, String resourcesAsJSON) {
        if (this.context.get() != null && !this.context.get().isFinishing()) {
            this.uiWork.setData(resources, resourcesAsJSON);
            this.context.get().runOnUiThread(this.uiWork);
        }
    }

    @Override // com.swrve.sdk.ISwrveUserResourcesListener
    public void onUserResourcesError(Exception exception) {
        if (this.context.get() != null && !this.context.get().isFinishing()) {
            this.uiWork.setException(exception);
            this.context.get().runOnUiThread(this.uiWork);
        }
    }
}
