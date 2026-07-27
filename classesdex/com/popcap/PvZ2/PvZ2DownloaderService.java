package com.popcap.PvZ2;

import android.util.Log;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class PvZ2DownloaderService extends DownloaderService {
    public static final byte[] SALT = {31, 69, 57, -99, 101, -45, 126, 30, 15, 61, -61, 101, -103, 84, 47, 41, 46, 92, -1, 11};

    private native String Native_getGooglePlayAPIKey();

    @Override // com.google.android.vending.expansion.downloader.impl.DownloaderService
    public String getPublicKey() {
        String apiKey = Native_getGooglePlayAPIKey();
        return apiKey;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloaderService
    public byte[] getSALT() {
        Log.e("LVLDownloader", "getting sale.");
        return SALT;
    }

    @Override // com.google.android.vending.expansion.downloader.impl.DownloaderService
    public String getAlarmReceiverClassName() {
        return PvZ2AlarmReceiver.class.getName();
    }
}
