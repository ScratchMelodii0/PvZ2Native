package com.swrve.sdk.messaging.view;

import android.content.Context;
import android.util.Log;
import com.swrve.sdk.messaging.SwrveMessage;
import com.swrve.sdk.messaging.SwrveMessageFormat;
import com.swrve.sdk.messaging.SwrveOrientation;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveMessageViewFactory {
    protected static final String LOG_TAG = "SwrveMessagingSDK";
    private static SwrveMessageViewFactory instance;

    public static SwrveMessageViewFactory getInstance() {
        if (instance == null) {
            instance = new SwrveMessageViewFactory();
        }
        return instance;
    }

    public SwrveMessageView buildLayout(Context context, SwrveMessage message, SwrveMessageFormat format) {
        return new SwrveMessageView(context, message, format);
    }

    public SwrveMessageView buildLayout(Context context, SwrveMessage message) {
        return buildLayout(context, message, getDeviceOrientation(context));
    }

    public SwrveMessageView buildLayout(Context context, SwrveMessage message, SwrveOrientation orientation) {
        SwrveMessageFormat format;
        if (message != null) {
            try {
                if (message.getFormats().size() > 0) {
                    Log.i(LOG_TAG, "Creating layout for message " + message.getId() + " with orientation " + orientation.toString());
                    if (orientation != SwrveOrientation.Both || (format = message.getFormat(getDeviceOrientation(context))) == null) {
                        format = message.getFormat(orientation);
                    }
                    if (format != null) {
                        return buildLayout(context, message, format);
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error while building SwrveMessageView view", e);
            }
        }
        return null;
    }

    protected SwrveOrientation getDeviceOrientation(Context context) {
        return SwrveOrientation.parse(context.getResources().getConfiguration().orientation);
    }
}
