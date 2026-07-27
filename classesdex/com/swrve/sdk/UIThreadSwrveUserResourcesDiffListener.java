package com.swrve.sdk;

import android.app.Activity;
import com.swrve.sdk.runnable.UIThreadSwrveResourcesDiffRunnable;
import java.lang.ref.WeakReference;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class UIThreadSwrveUserResourcesDiffListener implements ISwrveUserResourcesDiffListener {
    private final WeakReference<Activity> context;
    private final UIThreadSwrveResourcesDiffRunnable uiWork;

    public UIThreadSwrveUserResourcesDiffListener(Activity context, UIThreadSwrveResourcesDiffRunnable uiWork) {
        this.context = new WeakReference<>(context);
        this.uiWork = uiWork;
    }

    @Override // com.swrve.sdk.ISwrveUserResourcesDiffListener
    public void onUserResourcesDiffSuccess(Map<String, Map<String, String>> oldResources, Map<String, Map<String, String>> newResources, String resourcesAsJSON) {
        if (this.context.get() != null && !this.context.get().isFinishing()) {
            this.uiWork.setData(oldResources, newResources, resourcesAsJSON);
            this.context.get().runOnUiThread(this.uiWork);
        }
    }

    @Override // com.swrve.sdk.ISwrveUserResourcesDiffListener
    public void onUserResourcesDiffError(Exception exception) {
        if (this.context.get() != null && !this.context.get().isFinishing()) {
            this.uiWork.setException(exception);
            this.context.get().runOnUiThread(this.uiWork);
        }
    }
}
