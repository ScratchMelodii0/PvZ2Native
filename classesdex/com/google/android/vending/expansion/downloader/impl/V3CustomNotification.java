package com.google.android.vending.expansion.downloader.impl;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;
import com.android.vending.expansion.downloader.R;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.impl.DownloadNotification;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class V3CustomNotification implements DownloadNotification.ICustomNotification {
    int mIcon;
    PendingIntent mPendingIntent;
    CharSequence mTicker;
    long mTimeRemaining;
    CharSequence mTitle;
    long mTotalBytes = -1;
    long mCurrentBytes = -1;
    Notification mNotification = new Notification();

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
        this.mTotalBytes = totalBytes;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public void setCurrentBytes(long currentBytes) {
        this.mCurrentBytes = currentBytes;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloadNotification.ICustomNotification
    public Notification updateNotification(Context c) {
        Notification n = this.mNotification;
        n.icon = this.mIcon;
        n.flags |= 2;
        if (Build.VERSION.SDK_INT > 10) {
            n.flags |= 8;
        }
        RemoteViews expandedView = new RemoteViews(c.getPackageName(), R.layout.status_bar_ongoing_event_progress_bar);
        expandedView.setTextViewText(R.id.title, this.mTitle);
        expandedView.setViewVisibility(R.id.description, 0);
        expandedView.setTextViewText(R.id.description, Helpers.getDownloadProgressString(this.mCurrentBytes, this.mTotalBytes));
        expandedView.setViewVisibility(R.id.progress_bar_frame, 0);
        expandedView.setProgressBar(R.id.progress_bar, (int) (this.mTotalBytes >> 8), (int) (this.mCurrentBytes >> 8), this.mTotalBytes <= 0);
        expandedView.setViewVisibility(R.id.time_remaining, 0);
        expandedView.setTextViewText(R.id.time_remaining, c.getString(R.string.time_remaining_notification, Helpers.getTimeRemaining(this.mTimeRemaining)));
        expandedView.setTextViewText(R.id.progress_text, Helpers.getDownloadProgressPercent(this.mCurrentBytes, this.mTotalBytes));
        expandedView.setImageViewResource(R.id.appIcon, this.mIcon);
        n.contentView = expandedView;
        n.contentIntent = this.mPendingIntent;
        return n;
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
