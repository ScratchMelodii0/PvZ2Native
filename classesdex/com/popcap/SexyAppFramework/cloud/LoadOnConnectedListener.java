package com.popcap.SexyAppFramework.cloud;

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesClient;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class LoadOnConnectedListener implements GooglePlayServicesClient.ConnectionCallbacks {
    private Cloud cloud;

    public LoadOnConnectedListener(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override // com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
    public void onConnected(Bundle arg0) {
        Log.d("Cloud", "silently connected, calling loadState");
        this.cloud.loadState();
    }

    @Override // com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
    public void onDisconnected() {
    }
}
