package com.popcap.SexyAppFramework.GooglePlay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.plus.PlusClient;
import com.popcap.SexyAppFramework.GooglePlay.GameHelper;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public abstract class BaseGameActivity extends FragmentActivity implements GameHelper.GameHelperListener {
    public static final int CLIENT_ALL = 7;
    public static final int CLIENT_APPSTATE = 4;
    public static final int CLIENT_GAMES = 1;
    public static final int CLIENT_PLUS = 2;
    private String[] mAdditionalScopes;
    protected GameHelper mHelper;
    protected int mRequestedClients;

    protected BaseGameActivity() {
        this.mRequestedClients = 1;
        this.mAdditionalScopes = new String[]{Scopes.APP_STATE};
        this.mHelper = new GameHelper(this);
    }

    protected BaseGameActivity(int requestedClients) {
        this.mRequestedClients = 1;
        this.mAdditionalScopes = new String[]{Scopes.APP_STATE};
        setRequestedClients(requestedClients, new String[0]);
    }

    protected void setRequestedClients(int requestedClients, String... additionalScopes) {
        this.mRequestedClients = requestedClients;
        this.mAdditionalScopes = additionalScopes;
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onCreate(Bundle b) {
        super.onCreate(b);
    }

    protected void launchGame() {
        this.mHelper = new GameHelper(this);
        this.mHelper.setup(this, this.mRequestedClients, this.mAdditionalScopes);
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        this.mHelper.onStart(this);
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        this.mHelper.onStop();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        this.mHelper.onActivityResult(request, response, data);
    }

    public void beginSilentSignIn() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() { // from class: com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity.1
            @Override // java.lang.Runnable
            public void run() {
                BaseGameActivity.this.mHelper.silentConnect();
            }
        });
    }

    public GamesClient getGamesClient() {
        return this.mHelper.getGamesClient();
    }

    public AppStateClient getAppStateClient() {
        return this.mHelper.getAppStateClient();
    }

    public PlusClient getPlusClient() {
        return this.mHelper.getPlusClient();
    }

    public boolean isSignedIn() {
        return this.mHelper.isSignedIn();
    }

    public void beginUserInitiatedSignIn() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() { // from class: com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity.2
            @Override // java.lang.Runnable
            public void run() {
                BaseGameActivity.this.mHelper.beginUserInitiatedSignIn();
            }
        });
    }

    public void signOut() {
        this.mHelper.signOut();
    }

    public void showAlert(String title, String message) {
        this.mHelper.showAlert(title, message);
    }

    public void showAlert(String message) {
        this.mHelper.showAlert(message);
    }

    public void enableDebugLog(boolean enabled, String tag) {
        this.mHelper.enableDebugLog(enabled, tag);
    }

    public String getInvitationId() {
        return this.mHelper.getInvitationId();
    }

    public void reconnectClients(int whichClients) {
        this.mHelper.reconnectClients(whichClients);
    }

    public String getScopes() {
        return this.mHelper.getScopes();
    }

    public String[] getScopesArray() {
        return this.mHelper.getScopesArray();
    }

    public boolean hasSignInError() {
        return this.mHelper.hasSignInError();
    }

    public ConnectionResult getSignInError() {
        return this.mHelper.getSignInError();
    }

    public void setSignInMessages(String signingInMessage, String signingOutMessage) {
        this.mHelper.setSigningInMessage(signingInMessage);
        this.mHelper.setSigningOutMessage(signingOutMessage);
    }

    @Override // com.popcap.SexyAppFramework.GooglePlay.GameHelper.GameHelperListener
    public void onSignInFailed() {
    }
}
