package com.popcap.SexyAppFramework.cloud;

import android.util.Log;
import com.google.android.gms.appstate.OnStateLoadedListener;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class LoadPcpIdListener implements OnStateLoadedListener {
    private Cloud cloud;

    public LoadPcpIdListener(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override // com.google.android.gms.appstate.OnStateLoadedListener
    public void onStateConflict(int arg0, String arg1, byte[] arg2, byte[] arg3) {
        Log.d("Cloud", "onStateConflict");
    }

    private boolean dataCanBeRead(byte[] data) {
        return (data == null || data.length == 0) ? false : true;
    }

    @Override // com.google.android.gms.appstate.OnStateLoadedListener
    public void onStateLoaded(int statusCode, int key, byte[] data) {
        if (dataCanBeRead(data)) {
            this.cloud.pcpId = new String(data, Cloud.UTF8);
            Log.d("Cloud", "silently loaded with data: " + this.cloud.pcpId);
        }
        this.cloud.Native_CloudStateLoaded(this.cloud.pcpId);
    }
}
