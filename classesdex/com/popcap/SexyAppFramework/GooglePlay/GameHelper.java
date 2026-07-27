package com.popcap.SexyAppFramework.GooglePlay;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.OnSignOutCompleteListener;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.plus.PlusClient;
import java.util.Vector;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GameHelper implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, OnSignOutCompleteListener {
    public static final int CLIENT_ALL = 7;
    public static final int CLIENT_APPSTATE = 4;
    public static final int CLIENT_GAMES = 1;
    public static final int CLIENT_NONE = 0;
    public static final int CLIENT_PLUS = 2;
    static final int RC_RESOLVE = 9001;
    static final int RC_UNUSED = 9002;
    Activity mActivity;
    String mInvitationId;
    String[] mScopes;
    GamesClient mGamesClient = null;
    PlusClient mPlusClient = null;
    AppStateClient mAppStateClient = null;
    int mRequestedClients = 0;
    int mConnectedClients = 0;
    int mClientCurrentlyConnecting = 0;
    ProgressDialog mProgressDialog = null;
    boolean mAutoSignIn = true;
    boolean mUserInitiatedSignIn = false;
    ConnectionResult mConnectionResult = null;
    boolean mSignInError = false;
    boolean mExpectingActivityResult = false;
    boolean mSignedIn = false;
    boolean mDebugLog = false;
    String mDebugTag = "BaseGameActivity";
    String mSigningInMessage = "";
    String mSigningOutMessage = "";
    String mUnknownErrorMessage = "Unknown error";
    GameHelperListener mListener = null;

    public interface GameHelperListener {
        void onSignInFailed();

        void onSignInSucceeded();
    }

    public GameHelper(Activity activity) {
        this.mActivity = null;
        this.mActivity = activity;
    }

    public void setSigningInMessage(String message) {
        this.mSigningInMessage = message;
    }

    public void setSigningOutMessage(String message) {
        this.mSigningOutMessage = message;
    }

    public void setUnknownErrorMessage(String message) {
        this.mUnknownErrorMessage = message;
    }

    public void setup(GameHelperListener listener) {
        setup(listener, 1, new String[0]);
    }

    public void setup(GameHelperListener listener, int clientsToUse, String... additionalScopes) {
        this.mListener = listener;
        this.mRequestedClients = clientsToUse;
        Vector<String> scopesVector = new Vector<>();
        if ((clientsToUse & 1) != 0) {
            scopesVector.add(Scopes.GAMES);
        }
        if ((clientsToUse & 2) != 0) {
            scopesVector.add(Scopes.PLUS_LOGIN);
        }
        if ((clientsToUse & 4) != 0) {
            scopesVector.add(Scopes.APP_STATE);
        }
        if (additionalScopes != null) {
            for (String scope : additionalScopes) {
                scopesVector.add(scope);
            }
        }
        this.mScopes = new String[scopesVector.size()];
        scopesVector.copyInto(this.mScopes);
        if ((clientsToUse & 1) != 0) {
            debugLog("onCreate: creating GamesClient");
            this.mGamesClient = new GamesClient.Builder(getContext(), this, this).setGravityForPopups(49).setScopes(this.mScopes).create();
        }
        if ((clientsToUse & 2) != 0) {
            debugLog("onCreate: creating GamesPlusClient");
            this.mPlusClient = new PlusClient.Builder(getContext(), this, this).setScopes(this.mScopes).build();
        }
        if ((clientsToUse & 4) != 0) {
            debugLog("onCreate: creating AppStateClient");
            this.mAppStateClient = new AppStateClient.Builder(getContext(), this, this).setScopes(this.mScopes).create();
        }
    }

    public GamesClient getGamesClient() {
        if (this.mGamesClient == null) {
            throw new IllegalStateException("No GamesClient. Did you request it at setup?");
        }
        return this.mGamesClient;
    }

    public AppStateClient getAppStateClient() {
        if (this.mAppStateClient == null) {
            throw new IllegalStateException("No AppStateClient. Did you request it at setup?");
        }
        return this.mAppStateClient;
    }

    public PlusClient getPlusClient() {
        if (this.mPlusClient == null) {
            throw new IllegalStateException("No PlusClient. Did you request it at setup?");
        }
        return this.mPlusClient;
    }

    public boolean isSignedIn() {
        return this.mSignedIn;
    }

    public boolean hasSignInError() {
        return this.mSignInError;
    }

    public ConnectionResult getSignInError() {
        if (this.mSignInError) {
            return this.mConnectionResult;
        }
        return null;
    }

    public void onStart(Activity act) {
        this.mActivity = act;
        silentConnect();
    }

    public void silentConnect() {
        debugLog("onStart: Checking for status before init");
        if (this.mExpectingActivityResult) {
            debugLog("onStart: won't connect because we're expecting activity result.");
        } else if (!this.mAutoSignIn) {
            debugLog("onStart: not signing in because user specifically signed out.");
        } else {
            debugLog("onStart: connecting clients.");
            startConnections();
        }
    }

    public void onStop() {
        debugLog("onStop: disconnecting clients.");
        killConnections(7);
        this.mSignedIn = false;
        this.mSignInError = false;
        dismissDialog();
        this.mProgressDialog = null;
        this.mActivity = null;
    }

    public void showAlert(String title, String message) {
        new AlertDialog.Builder(getContext()).setTitle(title).setMessage(message).setNeutralButton(R.string.ok, (DialogInterface.OnClickListener) null).create().show();
    }

    public void showAlert(String message) {
        new AlertDialog.Builder(getContext()).setMessage(message).setNeutralButton(R.string.ok, (DialogInterface.OnClickListener) null).create().show();
    }

    public String getInvitationId() {
        return this.mInvitationId;
    }

    public void enableDebugLog(boolean enabled, String tag) {
        this.mDebugLog = enabled;
        this.mDebugTag = tag;
    }

    public String getScopes() {
        StringBuilder scopeStringBuilder = new StringBuilder();
        if (this.mScopes != null) {
            String[] arr$ = this.mScopes;
            for (String scope : arr$) {
                addToScope(scopeStringBuilder, scope);
            }
        }
        return scopeStringBuilder.toString();
    }

    public String[] getScopesArray() {
        return this.mScopes;
    }

    public void signOut() {
        this.mConnectionResult = null;
        this.mAutoSignIn = false;
        this.mSignedIn = false;
        this.mSignInError = false;
        if (this.mPlusClient != null && this.mPlusClient.isConnected()) {
            this.mPlusClient.clearDefaultAccount();
        }
        if (this.mGamesClient != null && this.mGamesClient.isConnected()) {
            showProgressDialog(false);
            this.mGamesClient.signOut(this);
        }
        killConnections(6);
    }

    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_RESOLVE) {
            this.mExpectingActivityResult = false;
            debugLog("onActivityResult, req " + requestCode + " response " + responseCode);
            if (responseCode == -1) {
                debugLog("responseCode == RESULT_OK. So connecting.");
                connectCurrentClient();
            } else {
                if (responseCode == 0) {
                    this.mAutoSignIn = false;
                    this.mConnectionResult = null;
                    this.mUserInitiatedSignIn = false;
                    dismissDialog();
                    return;
                }
                debugLog("responseCode != RESULT_OK, so not reconnecting.");
                giveUp();
            }
        }
    }

    public void beginUserInitiatedSignIn() {
        if (!this.mSignedIn) {
            this.mAutoSignIn = true;
            int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
            debugLog("isGooglePlayServicesAvailable returned " + result);
            if (result != 0) {
                debugLog("Google Play services not available. Show error dialog.");
                Dialog errorDialog = getErrorDialog(result);
                errorDialog.show();
                if (this.mListener != null) {
                    this.mListener.onSignInFailed();
                    return;
                }
                return;
            }
            this.mUserInitiatedSignIn = true;
            if (this.mConnectionResult != null) {
                debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.");
                showProgressDialog(true);
                resolveConnectionResult();
            } else {
                debugLog("beginUserInitiatedSignIn: starting new sign-in flow.");
                startConnections();
            }
        }
    }

    Context getContext() {
        return this.mActivity;
    }

    void addToScope(StringBuilder scopeStringBuilder, String scope) {
        if (scopeStringBuilder.length() == 0) {
            scopeStringBuilder.append("oauth2:");
        } else {
            scopeStringBuilder.append(" ");
        }
        scopeStringBuilder.append(scope);
    }

    void startConnections() {
        this.mConnectedClients = 0;
        this.mInvitationId = null;
        connectNextClient();
    }

    void showProgressDialog(boolean signIn) {
    }

    void dismissDialog() {
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        this.mProgressDialog = null;
    }

    void connectNextClient() {
        int pendingClients = this.mRequestedClients & (this.mConnectedClients ^ (-1));
        if (pendingClients == 0) {
            debugLog("All clients now connected. Sign-in successful.");
            succeedSignIn();
            return;
        }
        showProgressDialog(true);
        if (this.mGamesClient != null && (pendingClients & 1) != 0) {
            debugLog("Connecting GamesClient.");
            this.mClientCurrentlyConnecting = 1;
        } else if (this.mPlusClient != null && (pendingClients & 2) != 0) {
            debugLog("Connecting PlusClient.");
            this.mClientCurrentlyConnecting = 2;
        } else if (this.mAppStateClient != null && (pendingClients & 4) != 0) {
            debugLog("Connecting AppStateClient.");
            this.mClientCurrentlyConnecting = 4;
        } else {
            throw new AssertionError("Not all clients connected, yet no one is next. R=" + this.mRequestedClients + ", C=" + this.mConnectedClients);
        }
        connectCurrentClient();
    }

    void connectCurrentClient() {
        switch (this.mClientCurrentlyConnecting) {
            case 1:
                this.mGamesClient.connect();
                break;
            case 2:
                this.mPlusClient.connect();
                break;
            case 4:
                this.mAppStateClient.connect();
                break;
        }
    }

    void killConnections(int whatClients) {
        if ((whatClients & 1) != 0 && this.mGamesClient != null && this.mGamesClient.isConnected()) {
            this.mConnectedClients &= -2;
            this.mGamesClient.disconnect();
        }
        if ((whatClients & 2) != 0 && this.mPlusClient != null && this.mPlusClient.isConnected()) {
            this.mConnectedClients &= -3;
            this.mPlusClient.disconnect();
        }
        if ((whatClients & 4) != 0 && this.mAppStateClient != null && this.mAppStateClient.isConnected()) {
            this.mConnectedClients &= -5;
            this.mAppStateClient.disconnect();
        }
    }

    public void reconnectClients(int whatClients) {
        showProgressDialog(true);
        if ((whatClients & 1) != 0 && this.mGamesClient != null && this.mGamesClient.isConnected()) {
            this.mConnectedClients &= -2;
            this.mGamesClient.reconnect();
        }
        if ((whatClients & 4) != 0 && this.mAppStateClient != null && this.mAppStateClient.isConnected()) {
            this.mConnectedClients &= -5;
            this.mAppStateClient.reconnect();
        }
        if ((whatClients & 2) != 0 && this.mPlusClient != null && this.mPlusClient.isConnected()) {
            this.mConnectedClients &= -3;
            this.mPlusClient.disconnect();
            this.mPlusClient.connect();
        }
    }

    @Override // com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        Log.e("OAUTH", "*** Connected");
        this.mListener.onSignInSucceeded();
        debugLog("onConnected: connected! client=" + this.mClientCurrentlyConnecting);
        this.mConnectedClients |= this.mClientCurrentlyConnecting;
        if (this.mClientCurrentlyConnecting == 1 && connectionHint != null) {
            debugLog("onConnected: connection hint provided. Checking for invite.");
            Invitation inv = (Invitation) connectionHint.getParcelable(GamesClient.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                debugLog("onConnected: connection hint has a room invite!");
                this.mInvitationId = inv.getInvitationId();
                debugLog("Invitation ID: " + this.mInvitationId);
            }
        }
        connectNextClient();
    }

    void succeedSignIn() {
        debugLog("All requested clients connected. Sign-in succeeded!");
        this.mSignedIn = true;
        this.mSignInError = false;
        this.mAutoSignIn = true;
        this.mUserInitiatedSignIn = false;
        dismissDialog();
        if (this.mListener != null) {
            this.mListener.onSignInSucceeded();
        }
    }

    @Override // com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("OAUTH", "*** Connect Failed");
        this.mListener.onSignInSucceeded();
        this.mConnectionResult = result;
        debugLog("onConnectionFailed: result " + result.getErrorCode());
        dismissDialog();
        if (!this.mUserInitiatedSignIn) {
            debugLog("onConnectionFailed: since user didn't initiate sign-in, failing now.");
            this.mConnectionResult = result;
            if (this.mListener != null) {
                this.mListener.onSignInFailed();
                return;
            }
            return;
        }
        debugLog("onConnectionFailed: since user initiated sign-in, trying to resolve problem.");
        resolveConnectionResult();
    }

    void resolveConnectionResult() {
        debugLog("resolveConnectionResult: trying to resolve result: " + this.mConnectionResult);
        if (this.mConnectionResult.hasResolution()) {
            debugLog("result has resolution. Starting it.");
            try {
                this.mExpectingActivityResult = true;
                this.mConnectionResult.startResolutionForResult(this.mActivity, RC_RESOLVE);
                return;
            } catch (IntentSender.SendIntentException e) {
                debugLog("SendIntentException.");
                connectCurrentClient();
                return;
            }
        }
        debugLog("resolveConnectionResult: result has no resolution. Giving up.");
        giveUp();
    }

    void giveUp() {
        this.mSignInError = true;
        this.mAutoSignIn = false;
        dismissDialog();
        debugLog("giveUp: giving up on connection. " + (this.mConnectionResult == null ? "(no connection result)" : "Status code: " + this.mConnectionResult.getErrorCode()));
        if (this.mConnectionResult != null) {
            Dialog errorDialog = getErrorDialog(this.mConnectionResult.getErrorCode());
            errorDialog.show();
            if (this.mListener != null) {
                this.mListener.onSignInFailed();
                return;
            }
            return;
        }
        Log.e("GameHelper", "giveUp() called with no mConnectionResult");
    }

    @Override // com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
    public void onDisconnected() {
        debugLog("onDisconnected.");
        this.mConnectionResult = null;
        this.mAutoSignIn = false;
        this.mSignedIn = false;
        this.mSignInError = false;
        this.mInvitationId = null;
        this.mConnectedClients = 0;
        if (this.mListener != null) {
            this.mListener.onSignInFailed();
        }
    }

    Dialog getErrorDialog(int errorCode) {
        debugLog("Making error dialog for error: " + errorCode);
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this.mActivity, RC_UNUSED, null);
        return errorDialog != null ? errorDialog : new AlertDialog.Builder(getContext()).setMessage(this.mUnknownErrorMessage).setNeutralButton(R.string.ok, (DialogInterface.OnClickListener) null).create();
    }

    void debugLog(String message) {
        Log.d(this.mDebugTag, message);
    }

    @Override // com.google.android.gms.games.OnSignOutCompleteListener
    public void onSignOutComplete() {
        dismissDialog();
        if (this.mGamesClient.isConnected()) {
            this.mGamesClient.disconnect();
        }
    }
}
