package com.popcap.SexyAppFramework;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidHttpProxy {
    private ConnectivityManager mConnectivityManager;
    private final int NET_NOT_REACHABLE = 0;
    private final int NET_REACHABLE_WWAN = 1;
    private final int NET_REACHABLE_WIFI = 2;
    private final int NET_REACHABILITY_UNKNOWN = 3;

    public AndroidHttpProxy(Context ctxt) {
        this.mConnectivityManager = (ConnectivityManager) ctxt.getSystemService("connectivity");
        LogNetworkInfo();
    }

    public boolean Startup() {
        return true;
    }

    public void Shutdown() {
    }

    void LogNetworkInfo() {
        ConnectivityManager.isNetworkTypeValid(1);
        this.mConnectivityManager.getNetworkPreference();
        this.mConnectivityManager.getBackgroundDataSetting();
        ConnectivityManager.isNetworkTypeValid(1);
        ConnectivityManager.isNetworkTypeValid(0);
        NetworkInfo activeInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (activeInfo != null) {
            activeInfo.toString();
        }
        NetworkInfo[] infos = this.mConnectivityManager.getAllNetworkInfo();
        if (infos != null) {
            for (NetworkInfo info : infos) {
                Log.i("network", info.toString());
            }
        }
    }

    public int GetNetworkStatus() {
        NetworkInfo activeInfo;
        boolean bHasWifi = ConnectivityManager.isNetworkTypeValid(1);
        boolean bHasMobile = ConnectivityManager.isNetworkTypeValid(0);
        boolean bHasWiMax = ConnectivityManager.isNetworkTypeValid(6);
        if ((!bHasWifi && !bHasMobile && !bHasWiMax) || (activeInfo = this.mConnectivityManager.getActiveNetworkInfo()) == null) {
            return 0;
        }
        if (activeInfo.getState() == NetworkInfo.State.CONNECTED) {
            if (activeInfo.getType() != 0 && activeInfo.getType() != 6) {
                if (activeInfo.getType() == 1) {
                    return 2;
                }
            }
            return 1;
        }
        return 3;
    }
}
