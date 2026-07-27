package com.popcap.SexyAppFramework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class ScheduledNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "ScheduledNotificationReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        try {
            Log.i(TAG, "Creating Notification=" + intent.getExtras().toString());
            String title = intent.getExtras().getString("data.title");
            if (title == null || title.isEmpty()) {
                title = "FIXME!! Zippity Doo Dah!!";
            }
            String message = intent.getExtras().getString("data.message");
            String activity = intent.getExtras().getString("data.activity");
            String action = intent.getExtras().getString("data.action");
            String icon = intent.getExtras().getString("data.icon");
            String sound = intent.getExtras().getString("data.sound");
            Integer notificationId = Integer.valueOf(intent.getExtras().getInt("data.id"));
            Bundle fields = new Bundle();
            fields.putBundle("userInfo", intent.getExtras().getBundle("data.fields"));
            AndroidNotification notification = new AndroidNotification();
            notification.createBasicNotification(context, activity, action, title, message, icon, sound, notificationId.intValue(), fields);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
