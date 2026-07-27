package com.google.android.vending.expansion.downloader.impl;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import com.android.vending.expansion.downloader.R;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.impl.DownloadNotification;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class V14CustomNotification implements DownloadNotification.ICustomNotification {
    int mIcon;
    PendingIntent mPendingIntent;
    CharSequence mTicker;
    long mTimeRemaining;
    CharSequence mTitle;
    long mTotalKB = -1;
    long mCurrentKB = -1;

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setIcon(int icon) {
        this.mIcon = icon;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setTitle(CharSequence title) {
        this.mTitle = title;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setTotalBytes(long totalBytes) {
        this.mTotalKB = totalBytes;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setCurrentBytes(long currentBytes) {
        this.mCurrentKB = currentBytes;
    }

    void setProgress(Notification.Builder builder) {
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public Notification updateNotification(Context c) {
        Notification.Builder builder = new Notification.Builder(c);
        builder.setContentTitle(this.mTitle);
        if (this.mTotalKB > 0 && -1 != this.mCurrentKB) {
            builder.setProgress((int) (this.mTotalKB >> 8), (int) (this.mCurrentKB >> 8), false);
        } else {
            builder.setProgress(0, 0, true);
        }
        builder.setContentText(Helpers.getDownloadProgressString(this.mCurrentKB, this.mTotalKB));
        builder.setContentInfo(c.getString(R.string.time_remaining_notification, Helpers.getTimeRemaining(this.mTimeRemaining)));
        if (this.mIcon != 0) {
            builder.setSmallIcon(this.mIcon);
        } else {
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
        }
        builder.setOngoing(true);
        builder.setTicker(this.mTicker);
        builder.setContentIntent(this.mPendingIntent);
        builder.setOnlyAlertOnce(true);
        return builder.getNotification();
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setPendingIntent(PendingIntent contentIntent) {
        this.mPendingIntent = contentIntent;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setTicker(CharSequence ticker) {
        this.mTicker = ticker;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setTimeRemaining(long timeRemaining) {
        this.mTimeRemaining = timeRemaining;
    }
}
