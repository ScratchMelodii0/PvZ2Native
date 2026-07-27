package com.popcap.SexyAppFramework.cloud;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.popcap.SexyAppFramework.SexyAppFrameworkActivity;
import java.nio.charset.Charset;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class Cloud {
    private static final int CONNECTION_REQUEST_CODE = 9001;
    static final int PCP_ID = 1;
    static final Charset UTF8 = Charset.forName("UTF-8");
    private AppStateClient interactiveAppStateClient;
    private AppStateClient silentAppStateClient;
    private String[] scopes = {Scopes.APP_STATE};
    private GooglePlayServicesClient.OnConnectionFailedListener noOpFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener() { // from class: com.popcap.SexyAppFramework.cloud.Cloud.1
        @Override // com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult arg0) {
        }
    };
    protected String pcpId = "";
    private GooglePlayServicesClient.ConnectionCallbacks noOpConnectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() { // from class: com.popcap.SexyAppFramework.cloud.Cloud.2
        @Override // com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
        public void onDisconnected() {
        }

        @Override // com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
        }
    };

    public native void Native_CloudStateLoaded(String str);

    public Cloud(final SexyAppFrameworkActivity activity) {
        this.silentAppStateClient = new AppStateClient.Builder(activity.getApplicationContext(), new LoadOnConnectedListener(this), this.noOpFailedListener).setScopes(this.scopes).create();
        GooglePlayServicesClient.OnConnectionFailedListener failedListener = new GooglePlayServicesClient.OnConnectionFailedListener() { // from class: com.popcap.SexyAppFramework.cloud.Cloud.3
            @Override // com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener
            public void onConnectionFailed(ConnectionResult connectionResult) {
                if (connectionResult.getErrorCode() == 2) {
                    Dialog g = GooglePlayServicesUtil.getErrorDialog(2, activity, 10075);
                    g.show();
                }
                if (connectionResult.hasResolution()) {
                    try {
                        connectionResult.startResolutionForResult(activity, Cloud.CONNECTION_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("Cloud", "Exception trying to resolve: ", e);
                    }
                }
            }
        };
        try {
            this.interactiveAppStateClient = new AppStateClient.Builder(activity.getApplicationContext(), this.noOpConnectionCallbacks, failedListener).setScopes(this.scopes).create();
        } catch (Exception e) {
            Log.d("Cloud", e.getMessage());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == CONNECTION_REQUEST_CODE) {
            Cloud_Connect();
        }
    }

    public void Cloud_Connect() {
        this.interactiveAppStateClient.connect();
    }

    public void Cloud_SetPcpId(String pcpId) {
        savePcpIdWithAppStateClientWhenActive(pcpId, this.silentAppStateClient);
        savePcpIdWithAppStateClientWhenActive(pcpId, this.interactiveAppStateClient);
    }

    public String Cloud_GetPcpId() {
        return this.pcpId;
    }

    private void savePcpIdWithAppStateClientWhenActive(String pcpId, AppStateClient appStateClient) {
        if (appStateClient.isConnected()) {
            Log.d("Cloud", "appStateClient connected.  Updating state with id: " + pcpId);
            appStateClient.updateState(1, pcpId.getBytes(UTF8));
        }
    }

    public void Cloud_initiateSync() {
        if (this.interactiveAppStateClient.isConnected()) {
            this.interactiveAppStateClient.loadState(new LoadPcpIdListener(this), 1);
        }
    }

    public void Cloud_attemptSilentSync() {
        try {
            this.silentAppStateClient.connect();
        } catch (Exception e) {
            Log.d("Cloud", e.getMessage());
        }
    }

    public void loadState() {
        this.silentAppStateClient.loadState(new LoadPcpIdListener(this), 1);
    }
}
