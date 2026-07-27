package com.mobileapptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Observable;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class Tracker extends BroadcastReceiver {
    SharedPreferences a;

    public class ObservableChanged extends Observable {
        protected ObservableChanged() {
        }

        @Override // java.util.Observable
        public boolean hasChanged() {
            return true;
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("referrer");
        if (stringExtra != null) {
            Log.d("MobileAppTracker", "Received install referrer " + stringExtra);
            this.a = context.getSharedPreferences("mat_referrer", 0);
            SharedPreferences.Editor editorEdit = this.a.edit();
            editorEdit.putString("referrer", stringExtra);
            editorEdit.commit();
        }
    }
}
