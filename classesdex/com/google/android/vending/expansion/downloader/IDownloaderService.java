package com.google.android.vending.expansion.downloader;

import android.os.Messenger;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface IDownloaderService {
    public static final int FLAGS_DOWNLOAD_OVER_CELLULAR = 1;

    void onClientUpdated(Messenger messenger);

    void requestAbortDownload();

    void requestContinueDownload();

    void requestDownloadStatus();

    void requestPauseDownload();

    void setDownloadFlags(int i);
}
