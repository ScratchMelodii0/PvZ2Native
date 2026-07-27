package com.google.android.vending.expansion.downloader.impl;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Messenger;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class DownloadNotification implements IDownloaderClient {
    static final String LOGTAG = "DownloadNotification";
    static final int NOTIFICATION_ID = LOGTAG.hashCode();
    private IDownloaderClient mClientProxy;
    private PendingIntent mContentIntent;
    private final Context mContext;
    private String mCurrentText;
    private String mCurrentTitle;
    private CharSequence mLabel;
    private final NotificationManager mNotificationManager;
    private DownloadProgressInfo mProgressInfo;
    private int mState = -1;
    final ICustomNotification mCustomNotification = CustomNotificationFactory.createCustomNotification();
    private Notification mNotification = new Notification();
    private Notification mCurrentNotification = this.mNotification;

    public interface ICustomNotification {
        void setCurrentBytes(long j);

        void setIcon(int i);

        void setPendingIntent(PendingIntent pendingIntent);

        void setTicker(CharSequence charSequence);

        void setTimeRemaining(long j);

        void setTitle(CharSequence charSequence);

        void setTotalBytes(long j);

        Notification updateNotification(Context context);
    }

    public PendingIntent getClientIntent() {
        return this.mContentIntent;
    }

    public void setClientIntent(PendingIntent mClientIntent) {
        this.mContentIntent = mClientIntent;
    }

    public void resendState() {
        if (this.mClientProxy != null) {
            this.mClientProxy.onDownloadStateChanged(this.mState);
        }
    }

    @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
    public void onDownloadStateChanged(int newState) {
        int iconResource;
        int stringDownloadID;
        boolean ongoingEvent;
        if (this.mClientProxy != null) {
            this.mClientProxy.onDownloadStateChanged(newState);
        }
        if (newState != this.mState) {
            this.mState = newState;
            if (newState != 1 && this.mContentIntent != null) {
                switch (newState) {
                    case 0:
                        iconResource = R.drawable.stat_sys_warning;
                        stringDownloadID = com.android.vending.expansion.downloader.R.string.state_unknown;
                        ongoingEvent = false;
                        break;
                    case 1:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE /* 14 */:
                    default:
                        iconResource = R.drawable.stat_sys_warning;
                        stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                        ongoingEvent = true;
                        break;
                    case 2:
                    case 3:
                        iconResource = R.drawable.stat_sys_download_done;
                        stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                        ongoingEvent = true;
                        break;
                    case 4:
                        iconResource = R.drawable.stat_sys_download;
                        stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                        ongoingEvent = true;
                        break;
                    case 5:
                    case 7:
                        iconResource = R.drawable.stat_sys_download_done;
                        stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                        ongoingEvent = false;
                        break;
                    case IDownloaderClient.STATE_FAILED_UNLICENSED /* 15 */:
                    case 16:
                    case IDownloaderClient.STATE_FAILED_SDCARD_FULL /* 17 */:
                    case IDownloaderClient.STATE_FAILED_CANCELED /* 18 */:
                    case 19:
                        iconResource = R.drawable.stat_sys_warning;
                        stringDownloadID = Helpers.getDownloaderStringResourceIDFromState(newState);
                        ongoingEvent = false;
                        break;
                }
                this.mCurrentText = this.mContext.getString(stringDownloadID);
                this.mCurrentTitle = this.mLabel.toString();
                this.mCurrentNotification.tickerText = ((Object) this.mLabel) + ": " + this.mCurrentText;
                this.mCurrentNotification.icon = iconResource;
                this.mCurrentNotification.setLatestEventInfo(this.mContext, this.mCurrentTitle, this.mCurrentText, this.mContentIntent);
                if (ongoingEvent) {
                    this.mCurrentNotification.flags |= 2;
                } else {
                    this.mCurrentNotification.flags &= -3;
                    this.mCurrentNotification.flags |= 16;
                }
                this.mNotificationManager.notify(NOTIFICATION_ID, this.mCurrentNotification);
            }
        }
    }

    @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
    public void onDownloadProgress(DownloadProgressInfo progress) {
        this.mProgressInfo = progress;
        if (this.mClientProxy != null) {
            this.mClientProxy.onDownloadProgress(progress);
        }
        if (progress.mOverallTotal <= 0) {
            this.mNotification.tickerText = this.mCurrentTitle;
            this.mNotification.icon = R.drawable.stat_sys_download;
            this.mNotification.setLatestEventInfo(this.mContext, this.mLabel, this.mCurrentText, this.mContentIntent);
            this.mCurrentNotification = this.mNotification;
        } else {
            this.mCustomNotification.setCurrentBytes(progress.mOverallProgress);
            this.mCustomNotification.setTotalBytes(progress.mOverallTotal);
            this.mCustomNotification.setIcon(R.drawable.stat_sys_download);
            this.mCustomNotification.setPendingIntent(this.mContentIntent);
            this.mCustomNotification.setTicker(((Object) this.mLabel) + ": " + this.mCurrentText);
            this.mCustomNotification.setTitle(this.mLabel);
            this.mCustomNotification.setTimeRemaining(progress.mTimeRemaining);
            this.mCurrentNotification = this.mCustomNotification.updateNotification(this.mContext);
        }
        this.mNotificationManager.notify(NOTIFICATION_ID, this.mCurrentNotification);
    }

    public void setMessenger(Messenger msg) {
        this.mClientProxy = DownloaderClientMarshaller.CreateProxy(msg);
        if (this.mProgressInfo != null) {
            this.mClientProxy.onDownloadProgress(this.mProgressInfo);
        }
        if (this.mState != -1) {
            this.mClientProxy.onDownloadStateChanged(this.mState);
        }
    }

    DownloadNotification(Context ctx, CharSequence applicationLabel) {
        this.mContext = ctx;
        this.mLabel = applicationLabel;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
    }

    @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
    public void onServiceConnected(Messenger m) {
    }
}
