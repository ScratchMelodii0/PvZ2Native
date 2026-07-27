package com.google.android.vending.expansion.downloader;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class DownloaderClientMarshaller {
    public static final int DOWNLOAD_REQUIRED = 2;
    public static final int LVL_CHECK_REQUIRED = 1;
    public static final int MSG_ONDOWNLOADPROGRESS = 11;
    public static final int MSG_ONDOWNLOADSTATE_CHANGED = 10;
    public static final int MSG_ONSERVICECONNECTED = 12;
    public static final int NO_DOWNLOAD_REQUIRED = 0;
    public static final String PARAM_MESSENGER = "EMH";
    public static final String PARAM_NEW_STATE = "newState";
    public static final String PARAM_PROGRESS = "progress";

    private static class Proxy implements IDownloaderClient {
        private Messenger mServiceMessenger;

        @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
        public void onDownloadStateChanged(int newState) {
            Bundle params = new Bundle(1);
            params.putInt(DownloaderClientMarshaller.PARAM_NEW_STATE, newState);
            send(10, params);
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
        public void onDownloadProgress(DownloadProgressInfo progress) {
            Bundle params = new Bundle(1);
            params.putParcelable(DownloaderClientMarshaller.PARAM_PROGRESS, progress);
            send(11, params);
        }

        private void send(int method, Bundle params) {
            Message m = Message.obtain((Handler) null, method);
            m.setData(params);
            try {
                this.mServiceMessenger.send(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public Proxy(Messenger msg) {
            this.mServiceMessenger = msg;
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
        public void onServiceConnected(Messenger m) {
        }
    }

    private static class Stub implements IStub {
        private boolean mBound;
        private Context mContext;
        private Class<?> mDownloaderServiceClass;
        private IDownloaderClient mItf;
        private Messenger mServiceMessenger;
        final Messenger mMessenger = new Messenger(new Handler() { // from class: com.google.android.vending.expansion.downloader.DownloaderClientMarshaller.Stub.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        Stub.this.mItf.onDownloadStateChanged(msg.getData().getInt(DownloaderClientMarshaller.PARAM_NEW_STATE));
                        break;
                    case 11:
                        Bundle bun = msg.getData();
                        if (Stub.this.mContext != null) {
                            bun.setClassLoader(Stub.this.mContext.getClassLoader());
                            DownloadProgressInfo dpi = (DownloadProgressInfo) msg.getData().getParcelable(DownloaderClientMarshaller.PARAM_PROGRESS);
                            Stub.this.mItf.onDownloadProgress(dpi);
                        }
                        break;
                    case 12:
                        Stub.this.mItf.onServiceConnected((Messenger) msg.getData().getParcelable("EMH"));
                        break;
                }
            }
        });
        private ServiceConnection mConnection = new ServiceConnection() { // from class: com.google.android.vending.expansion.downloader.DownloaderClientMarshaller.Stub.2
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName className, IBinder service) {
                Stub.this.mServiceMessenger = new Messenger(service);
                Stub.this.mItf.onServiceConnected(Stub.this.mServiceMessenger);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName className) {
                Stub.this.mServiceMessenger = null;
            }
        };

        public Stub(IDownloaderClient itf, Class<?> downloaderService) {
            this.mItf = null;
            this.mItf = itf;
            this.mDownloaderServiceClass = downloaderService;
        }

        @Override // com.google.android.vending.expansion.downloader.IStub
        public void connect(Context c) {
            this.mContext = c;
            Intent bindIntent = new Intent(c, this.mDownloaderServiceClass);
            bindIntent.putExtra("EMH", this.mMessenger);
            if (c.bindService(bindIntent, this.mConnection, 2)) {
                this.mBound = true;
            }
        }

        @Override // com.google.android.vending.expansion.downloader.IStub
        public void disconnect(Context c) {
            if (this.mBound) {
                c.unbindService(this.mConnection);
                this.mBound = false;
            }
            this.mContext = null;
        }

        @Override // com.google.android.vending.expansion.downloader.IStub
        public Messenger getMessenger() {
            return this.mMessenger;
        }
    }

    public static IDownloaderClient CreateProxy(Messenger msg) {
        return new Proxy(msg);
    }

    public static IStub CreateStub(IDownloaderClient itf, Class<?> downloaderService) {
        return new Stub(itf, downloaderService);
    }

    public static int startDownloadServiceIfRequired(Context context, PendingIntent notificationClient, Class<?> serviceClass) throws PackageManager.NameNotFoundException {
        return DownloaderService.startDownloadServiceIfRequired(context, notificationClient, serviceClass);
    }

    public static int startDownloadServiceIfRequired(Context context, Intent notificationClient, Class<?> serviceClass) throws PackageManager.NameNotFoundException {
        return DownloaderService.startDownloadServiceIfRequired(context, notificationClient, serviceClass);
    }
}
