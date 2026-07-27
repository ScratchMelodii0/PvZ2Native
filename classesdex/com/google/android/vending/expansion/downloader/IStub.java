package com.google.android.vending.expansion.downloader;

import android.content.Context;
import android.os.Messenger;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface IStub {
    void connect(Context context);

    void disconnect(Context context);

    Messenger getMessenger();
}
