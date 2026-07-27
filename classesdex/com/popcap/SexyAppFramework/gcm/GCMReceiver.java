package com.popcap.SexyAppFramework.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.popcap.SexyAppFramework.AndroidNotification;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GCMReceiver extends BroadcastReceiver {
    private static final int OK = -1;
    private static final String TAG = "GCMReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String messageType = gcm.getMessageType(intent);
            Log.i(TAG, "GCM: messageType: " + messageType);
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(TAG, "GCM: Send error: " + intent.getExtras().toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.i(TAG, "GCM: Deleted messages on server: " + intent.getExtras().toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i(TAG, "GCM message: " + intent.getExtras().toString());
                Log.i(TAG, "Creating Notification=" + intent.getExtras().toString());
                String title = intent.getExtras().getString("data.title");
                if (title == null || title.isEmpty()) {
                    String packageName = context.getPackageName();
                    int resId = context.getResources().getIdentifier("app_name", "string", packageName);
                    title = context.getString(resId);
                    Log.i(TAG, "title=" + title);
                }
                String message = intent.getExtras().getString("data.message");
                String activity = intent.getExtras().getString("data.activity");
                String icon = intent.getExtras().getString("data.icon");
                String sound = intent.getExtras().getString("data.sound");
                String notificationId = intent.getExtras().getString("data.id");
                String action = intent.getExtras().getString("data.action");
                Bundle fields = new Bundle();
                fields.putString("userInfo", intent.getExtras().getString("data.fields"));
                fields.putString("notificationType", "RN");
                AndroidNotification notification = new AndroidNotification();
                notification.createBasicNotification(context, activity, action, title, message, icon, sound, Integer.parseInt(notificationId), fields);
            }
            setResultCode(-1);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
