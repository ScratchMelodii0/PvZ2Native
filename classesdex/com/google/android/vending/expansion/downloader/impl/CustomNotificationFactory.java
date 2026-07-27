package com.google.android.vending.expansion.downloader.impl;

import android.os.Build;
import com.google.android.vending.expansion.downloader.impl.DownloadNotification;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class CustomNotificationFactory {
    public static DownloadNotification.ICustomNotification createCustomNotification() {
        return Build.VERSION.SDK_INT > 13 ? new V14CustomNotification() : new V3CustomNotification();
    }
}
