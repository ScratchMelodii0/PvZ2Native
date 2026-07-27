package com.mobileapptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
final class b extends BroadcastReceiver {
    final /* synthetic */ MobileAppTracker a;

    b(MobileAppTracker mobileAppTracker) {
        this.a = mobileAppTracker;
    }

    @Override // android.content.BroadcastReceiver
    public final void onReceive(Context context, Intent intent) {
        if (!this.a.c() || this.a.a() <= 0) {
            return;
        }
        try {
            this.a.b();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
