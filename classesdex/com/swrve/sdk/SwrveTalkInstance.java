package com.swrve.sdk;

import android.content.Context;
import android.util.Log;
import com.swrve.sdk.messaging.SwrveButton;
import com.swrve.sdk.messaging.SwrveMessage;
import com.swrve.sdk.messaging.SwrveMessageFormat;
import java.io.File;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveTalkInstance {
    protected static final String LOG_TAG = "SwrveMessagingSDK";
    private static SwrveTalk instance = null;

    public static synchronized SwrveTalk init(Context context, Swrve swrve) {
        try {
        } catch (Exception exp) {
            Log.e(LOG_TAG, "Could not initialize a singleton SwrveTalk", exp);
        }
        if (instance == null) {
            instance = new SwrveTalk(context, swrve);
        }
        return instance;
    }

    public static SwrveTalk getInstance() {
        return instance;
    }

    public static SwrveMessage getMessageForEvent(String event) {
        if (instance != null) {
            return instance.getMessageForEvent(event);
        }
        return null;
    }

    public static SwrveMessage getMessageForId(int messageId) {
        if (instance != null) {
            return instance.getMessageForId(messageId);
        }
        return null;
    }

    public static void setDownloadingEnabled(boolean enabled) {
        if (instance != null) {
            instance.setDownloadingEnabled(enabled);
        }
    }

    public static void buttonWasPressedByUser(SwrveButton button) {
        if (instance != null) {
            instance.buttonWasPressedByUser(button);
        }
    }

    public static void messageWasShownToUser(SwrveMessageFormat messageFormat) {
        if (instance != null) {
            instance.messageWasShownToUser(messageFormat);
        }
    }

    public static String getAppStoreURLForGame(int gameId) {
        if (instance != null) {
            return instance.getAppStoreURLForGame(gameId);
        }
        return null;
    }

    public static void reloadCampaigns() {
        if (instance != null) {
            instance.reloadCampaigns();
        }
    }

    public static void onDestroy() {
        if (instance != null) {
            instance.onDestroy();
            instance = null;
        }
    }

    public File getCacheDir() {
        if (instance != null) {
            return instance.getCacheDir();
        }
        return null;
    }

    public Swrve getAnalytics() {
        if (instance != null) {
            return instance.getAnalytics();
        }
        return null;
    }
}
