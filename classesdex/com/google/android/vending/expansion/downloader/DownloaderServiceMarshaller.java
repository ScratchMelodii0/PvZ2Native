package com.google.android.vending.expansion.downloader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class DownloaderServiceMarshaller {
    public static final int MSG_REQUEST_ABORT_DOWNLOAD = 1;
    public static final int MSG_REQUEST_CLIENT_UPDATE = 6;
    public static final int MSG_REQUEST_CONTINUE_DOWNLOAD = 4;
    public static final int MSG_REQUEST_DOWNLOAD_STATE = 5;
    public static final int MSG_REQUEST_PAUSE_DOWNLOAD = 2;
    public static final int MSG_SET_DOWNLOAD_FLAGS = 3;
    public static final String PARAMS_FLAGS = "flags";
    public static final String PARAM_MESSENGER = "EMH";

    private static class Proxy implements IDownloaderService {
        private Messenger mMsg;

        private void send(int method, Bundle params) {
            Message m = Message.obtain((Handler) null, method);
            m.setData(params);
            try {
                this.mMsg.send(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public Proxy(Messenger msg) {
            this.mMsg = msg;
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderService
        public void requestAbortDownload() {
            send(1, new Bundle());
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderService
        public void requestPauseDownload() {
            send(2, new Bundle());
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderService
        public void setDownloadFlags(int flags) {
            Bundle params = new Bundle();
            params.putInt(DownloaderServiceMarshaller.PARAMS_FLAGS, flags);
            send(3, params);
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderService
        public void requestContinueDownload() {
            send(4, new Bundle());
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderService
        public void requestDownloadStatus() {
            send(5, new Bundle());
        }

        @Override // com.google.android.vending.expansion.downloader.IDownloaderService
        public void onClientUpdated(Messenger clientMessenger) {
            Bundle bundle = new Bundle(1);
            bundle.putParcelable("EMH", clientMessenger);
            send(6, bundle);
        }
    }

    private static class Stub implements IStub {
        private IDownloaderService mItf;
        final Messenger mMessenger = new Messenger(new Handler() { // from class: com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller.Stub.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Stub.this.mItf.requestAbortDownload();
                        break;
                    case 2:
                        Stub.this.mItf.requestPauseDownload();
                        break;
                    case 3:
                        Stub.this.mItf.setDownloadFlags(msg.getData().getInt(DownloaderServiceMarshaller.PARAMS_FLAGS));
                        break;
                    case 4:
                        Stub.this.mItf.requestContinueDownload();
                        break;
                    case 5:
                        Stub.this.mItf.requestDownloadStatus();
                        break;
                    case 6:
                        Stub.this.mItf.onClientUpdated((Messenger) msg.getData().getParcelable("EMH"));
                        break;
                }
            }
        });

        public Stub(IDownloaderService itf) {
            this.mItf = null;
            this.mItf = itf;
        }

        @Override // com.google.android.vending.expansion.downloader.IStub
        public Messenger getMessenger() {
            return this.mMessenger;
        }

        @Override // com.google.android.vending.expansion.downloader.IStub
        public void connect(Context c) {
        }

        @Override // com.google.android.vending.expansion.downloader.IStub
        public void disconnect(Context c) {
        }
    }

    public static IDownloaderService CreateProxy(Messenger msg) {
        return new Proxy(msg);
    }

    public static IStub CreateStub(IDownloaderService itf) {
        return new Stub(itf);
    }
}
