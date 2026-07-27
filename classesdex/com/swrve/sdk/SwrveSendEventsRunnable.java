package com.swrve.sdk;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveSendEventsRunnable implements Runnable {
    private WeakReference<Context> contextReference;
    private ExecutorService executor;
    private long sendEventsDelay;
    private WeakReference<Swrve> swrveReference;

    public SwrveSendEventsRunnable(Context context, Swrve swrve, ExecutorService executor, long sendEventsDelay) {
        this.contextReference = new WeakReference<>(context);
        this.swrveReference = new WeakReference<>(swrve);
        this.executor = executor;
        this.sendEventsDelay = sendEventsDelay;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Thread.sleep(this.sendEventsDelay);
            boolean callAgain = false;
            if (this.contextReference.get() != null && this.swrveReference.get() != null) {
                Swrve swrve = this.swrveReference.get();
                if (!swrve.destroyed) {
                    callAgain = true;
                    swrve.sendQueuedEvents();
                }
            }
            if (callAgain) {
                this.executor.execute(this);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
