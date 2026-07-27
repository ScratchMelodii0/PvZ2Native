package com.popcap.SexyAppFramework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.widget.WebDialog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidFacebookDriver {
    private static final String TAG = "Facebook.Driver";
    private volatile String appFriends;
    private final BroadcastReceiver mBroadcastReceiver;
    private final SexyDialogListener mDialogListener;
    private final SexySessionStatusCallback mSessionStatusCallback;
    private volatile String nonAppFriends;
    private volatile String nonMobileFriends;
    private String mFBAppID = null;
    private long mNativeAppDriverPtr = 0;
    private final LocalBroadcastManager mBroadcastManager = LocalBroadcastManager.getInstance(SexyAppFrameworkActivity.instance());
    private Map<String, String> fbPicUrls = new HashMap();
    private Map<String, String> fbNames = new HashMap();

    native void DialogDidComplete(long j, String str);

    native void DialogDidFail(long j, String str);

    native void DialogWasCanceled(long j);

    native void OnSessionLoggedOutHook(long j, boolean z);

    native void OnSessionOpenedHook(long j, String str, long j2);

    /* JADX INFO: Access modifiers changed from: private */
    public JSONObject ConvertBundleToJSON(Bundle aBundle) {
        if (aBundle != null && !aBundle.isEmpty()) {
            Set<String> aBundleSet = aBundle.keySet();
            JSONObject aJSONObject = new JSONObject();
            for (String key : aBundleSet) {
                String value = aBundle.getString(key);
                try {
                    aJSONObject.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("ConvertBundleToJSON error", e.toString());
                }
            }
            return aJSONObject;
        }
        return null;
    }

    private class SexySessionStatusCallback implements Session.StatusCallback {
        private SexySessionStatusCallback() {
        }

        @Override // com.facebook.Session.StatusCallback
        public void call(Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                AndroidFacebookDriver.this.OnSessionOpenedHook(AndroidFacebookDriver.this.mNativeAppDriverPtr, session.getAccessToken(), session.getExpirationDate().getTime());
                Session.setActiveSession(session);
            } else if (state.isClosed()) {
                if (AndroidFacebookDriver.this.mNativeAppDriverPtr != 0) {
                    AndroidFacebookDriver.this.OnSessionLoggedOutHook(AndroidFacebookDriver.this.mNativeAppDriverPtr, false);
                }
                Session.setActiveSession(null);
            }
        }
    }

    private class SexyDialogListener implements WebDialog.OnCompleteListener {
        private SexyDialogListener() {
        }

        @Override // com.facebook.widget.WebDialog.OnCompleteListener
        public void onComplete(Bundle values, FacebookException error) {
            if (error == null) {
                String postID = values.getString("post_id");
                if (postID != null) {
                    AndroidFacebookDriver.this.DialogDidComplete(AndroidFacebookDriver.this.mNativeAppDriverPtr, AndroidFacebookDriver.this.ConvertBundleToJSON(values).toString());
                    return;
                } else if (values.containsKey("request")) {
                    AndroidFacebookDriver.this.DialogDidComplete(AndroidFacebookDriver.this.mNativeAppDriverPtr, AndroidFacebookDriver.this.ConvertBundleToJSON(values).toString());
                    return;
                } else {
                    AndroidFacebookDriver.this.DialogWasCanceled(AndroidFacebookDriver.this.mNativeAppDriverPtr);
                    return;
                }
            }
            if (error instanceof FacebookOperationCanceledException) {
                AndroidFacebookDriver.this.DialogWasCanceled(AndroidFacebookDriver.this.mNativeAppDriverPtr);
            } else {
                AndroidFacebookDriver.this.DialogDidFail(AndroidFacebookDriver.this.mNativeAppDriverPtr, values != null ? AndroidFacebookDriver.this.ConvertBundleToJSON(values).toString() : "{}");
            }
        }
    }

    private class SexyDialogRunnable implements Runnable {
        private String mName;
        private Bundle mParams;

        SexyDialogRunnable(String name, Bundle params) {
            this.mName = name;
            this.mParams = params;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (Session.getActiveSession() != null) {
                WebDialog.Builder aDialogBuilder = new WebDialog.Builder(SexyAppFrameworkActivity.instance(), Session.getActiveSession(), this.mName, this.mParams);
                aDialogBuilder.setOnCompleteListener(AndroidFacebookDriver.this.mDialogListener);
                WebDialog aDialog = aDialogBuilder.build();
                aDialog.show();
            }
        }
    }

    private class ActiveSessionBroadcastReceiver extends BroadcastReceiver {
        private ActiveSessionBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Session session;
            if (Session.ACTION_ACTIVE_SESSION_SET.equals(intent.getAction())) {
                Session session2 = Session.getActiveSession();
                if (session2 != null && AndroidFacebookDriver.this.mSessionStatusCallback != null) {
                    session2.addCallback(AndroidFacebookDriver.this.mSessionStatusCallback);
                    return;
                }
                return;
            }
            if (Session.ACTION_ACTIVE_SESSION_UNSET.equals(intent.getAction()) && (session = Session.getActiveSession()) != null && AndroidFacebookDriver.this.mSessionStatusCallback != null) {
                session.removeCallback(AndroidFacebookDriver.this.mSessionStatusCallback);
            }
        }
    }

    AndroidFacebookDriver() {
        this.mBroadcastReceiver = new ActiveSessionBroadcastReceiver();
        this.mSessionStatusCallback = new SexySessionStatusCallback();
        this.mDialogListener = new SexyDialogListener();
    }

    public void onCreate(Bundle savedInstanceState) {
        Session session = Session.getActiveSession();
        if (session == null && savedInstanceState != null) {
            Session.restoreSession(SexyAppFrameworkActivity.instance(), null, this.mSessionStatusCallback, savedInstanceState);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Session.ACTION_ACTIVE_SESSION_SET);
        filter.addAction(Session.ACTION_ACTIVE_SESSION_UNSET);
        this.mBroadcastManager.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void onResume() {
        NotifyFacebookMobileAppInstallAd();
        Session session = Session.getActiveSession();
        if (session != null) {
            if (this.mSessionStatusCallback != null) {
                session.addCallback(this.mSessionStatusCallback);
            }
            if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState())) {
                session.openForRead(null);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, String.format("AndroidFacebookDriver.onActivityResult %d %d %s", Integer.valueOf(requestCode), Integer.valueOf(resultCode), data));
        if (resultCode != 0) {
            Session session = Session.getActiveSession();
            if (session != null) {
                session.onActivityResult(SexyAppFrameworkActivity.instance(), requestCode, resultCode, data);
                return;
            }
            return;
        }
        CloseAndClearSession();
        if (this.mNativeAppDriverPtr != 0) {
            OnSessionLoggedOutHook(this.mNativeAppDriverPtr, true);
        }
        Session.setActiveSession(null);
    }

    public void onSaveInstanceState(Bundle outState) {
        Session.saveSession(Session.getActiveSession(), outState);
    }

    public void onPause() {
        Session session;
        if (this.mSessionStatusCallback != null && (session = Session.getActiveSession()) != null) {
            session.removeCallback(this.mSessionStatusCallback);
        }
    }

    public void onDestroy() {
        this.mBroadcastManager.unregisterReceiver(this.mBroadcastReceiver);
    }

    public void InitWithAppId(String appId, long aNativeAppDriverPtr) {
        this.mFBAppID = appId;
        this.mNativeAppDriverPtr = aNativeAppDriverPtr;
        Session session = Session.getActiveSession();
        if (session == null) {
            Session session2 = new Session.Builder(SexyAppFrameworkActivity.instance()).setApplicationId(this.mFBAppID).setTokenCachingStrategy(null).build();
            Session.setActiveSession(session2);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Session.ACTION_ACTIVE_SESSION_SET);
        filter.addAction(Session.ACTION_ACTIVE_SESSION_UNSET);
        this.mBroadcastManager.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public boolean OpenSessionForRead(String readPermissions, boolean allowLoginUI) {
        if (Session.getActiveSession() == null) {
            Session session = new Session.Builder(SexyAppFrameworkActivity.instance()).setApplicationId(this.mFBAppID).setTokenCachingStrategy(null).build();
            Session.setActiveSession(session);
        }
        if (Session.getActiveSession().getState() == SessionState.OPENED) {
            return true;
        }
        if ((!allowLoginUI || Session.getActiveSession().getState() == SessionState.OPENING) && Session.getActiveSession().getState() != SessionState.CREATED_TOKEN_LOADED) {
            return false;
        }
        Session.OpenRequest aReadRequest = new Session.OpenRequest(SexyAppFrameworkActivity.instance()).setCallback((Session.StatusCallback) this.mSessionStatusCallback).setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
        List<String> aReadPermissionsList = null;
        if (readPermissions != null) {
            aReadPermissionsList = Arrays.asList(readPermissions);
        }
        aReadRequest.setPermissions(aReadPermissionsList);
        try {
            Session.getActiveSession().openForRead(aReadRequest);
            return false;
        } catch (FacebookException ex) {
            ex.printStackTrace();
            Log.e("FacebookException in openSessionForRead", ex.toString());
            return false;
        }
    }

    public synchronized String getAppFriends() {
        return this.appFriends;
    }

    public synchronized String getNonAppFriends() {
        return this.nonAppFriends;
    }

    public String GetAccessToken() {
        if (Session.getActiveSession() == null) {
            return "";
        }
        String aAccessToken = Session.getActiveSession().getAccessToken();
        return aAccessToken;
    }

    public long GetExpirationDate() {
        if (Session.getActiveSession() == null) {
            return -1L;
        }
        long aExpirationDate = Session.getActiveSession().getExpirationDate().getTime();
        return aExpirationDate;
    }

    public boolean IsSessionOpen() {
        if (Session.getActiveSession() == null) {
            return false;
        }
        boolean bSessionIsOpen = Session.getActiveSession().isOpened();
        return bSessionIsOpen;
    }

    public boolean IsSessionOpening() {
        return Session.getActiveSession() != null && Session.getActiveSession().getState() == SessionState.OPENING;
    }

    public long GetSessionState() {
        long aSessionState = 0;
        if (Session.getActiveSession() == null) {
            return 0L;
        }
        switch (Session.getActiveSession().getState()) {
            case CREATED:
                aSessionState = 0;
                break;
            case CREATED_TOKEN_LOADED:
                aSessionState = 1;
                break;
            case OPENING:
                aSessionState = 2;
                break;
            case OPENED:
                aSessionState = 513;
                break;
            case OPENED_TOKEN_UPDATED:
                aSessionState = 514;
                break;
            case CLOSED_LOGIN_FAILED:
                aSessionState = 257;
                break;
            case CLOSED:
                aSessionState = 258;
                break;
        }
        return aSessionState;
    }

    public void CloseAndClearSession() {
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().closeAndClearTokenInformation();
        }
    }

    public boolean SessionIsValid() {
        if (Session.getActiveSession() == null) {
            return false;
        }
        boolean bSessionIsValid = Session.getActiveSession().getState().isOpened();
        return bSessionIsValid;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getUidsFromJsonResponse(Response response) {
        StringBuilder sb = new StringBuilder();
        try {
            JSONArray jsonUids = response.getGraphObject().getInnerJSONObject().getJSONArray("data");
            for (int x = 0; x < jsonUids.length(); x++) {
                String uid = jsonUids.getJSONObject(x).getString("uid");
                sb.append(uid).append(",");
                String fbName = jsonUids.getJSONObject(x).getString("name");
                if (fbName != null) {
                    this.fbNames.put(uid, fbName);
                }
                String picURL = jsonUids.getJSONObject(x).getString("pic_square");
                if (picURL != null) {
                    this.fbPicUrls.put(uid, picURL);
                }
            }
            if (sb.length() <= 0) {
                return "";
            }
            String uidString = sb.substring(0, sb.length() - 1);
            return uidString;
        } catch (NullPointerException e) {
            Log.e("our_facebook", "NullPointerException parsing friend uids from facebook: " + e.getMessage());
            return "";
        } catch (JSONException e2) {
            Log.e("our_facebook", "exception parsing json data from facebook: " + e2.getMessage());
            return "";
        }
    }

    public void RefreshFriendsLists(final String fqlFormat, final String nonMobileFql) {
        SexyAppFrameworkActivity.instance().runOnUiThread(new Runnable() { // from class: com.popcap.SexyAppFramework.AndroidFacebookDriver.1
            @Override // java.lang.Runnable
            public void run() {
                Session session = Session.getActiveSession();
                Request requestAppFriends = AndroidFacebookDriver.this.buildFqlRequest(session, String.format(fqlFormat, "1"), new Request.Callback() { // from class: com.popcap.SexyAppFramework.AndroidFacebookDriver.1.1
                    @Override // com.facebook.Request.Callback
                    public void onCompleted(Response response) {
                        AndroidFacebookDriver.this.appFriends = AndroidFacebookDriver.this.getUidsFromJsonResponse(response);
                    }
                });
                Request requestNonAppFriends = AndroidFacebookDriver.this.buildFqlRequest(session, String.format(fqlFormat, "0"), new Request.Callback() { // from class: com.popcap.SexyAppFramework.AndroidFacebookDriver.1.2
                    @Override // com.facebook.Request.Callback
                    public void onCompleted(Response response) {
                        AndroidFacebookDriver.this.nonAppFriends = AndroidFacebookDriver.this.getUidsFromJsonResponse(response);
                    }
                });
                Request requestNonMobileFriends = AndroidFacebookDriver.this.buildFqlRequest(session, nonMobileFql, new Request.Callback() { // from class: com.popcap.SexyAppFramework.AndroidFacebookDriver.1.3
                    @Override // com.facebook.Request.Callback
                    public void onCompleted(Response response) {
                        AndroidFacebookDriver.this.nonMobileFriends = AndroidFacebookDriver.this.getUidsFromJsonResponse(response);
                    }
                });
                Request.executeBatchAsync(requestAppFriends, requestNonAppFriends, requestNonMobileFriends);
            }
        });
    }

    public String GetFriendPictureURL(String friendUID) {
        return this.fbPicUrls.containsKey(friendUID) ? this.fbPicUrls.get(friendUID) : "";
    }

    public String GetFriendName(String friendUID) {
        return this.fbNames.containsKey(friendUID) ? this.fbNames.get(friendUID) : "";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Request buildFqlRequest(Session session, String fql, Request.Callback callback) {
        Bundle params = new Bundle();
        params.putString("q", fql);
        Request request = new Request(session, "/fql", params, HttpMethod.GET, callback);
        return request;
    }

    public void Dialog(String name, String jsonParams) {
        JSONObject aJsonObject = null;
        Bundle params = new Bundle();
        try {
            JSONObject aJsonObject2 = new JSONObject(jsonParams);
            aJsonObject = aJsonObject2;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSONException in Dialog", e.toString());
        }
        Iterator<String> jsonObjectIter = aJsonObject.keys();
        while (jsonObjectIter.hasNext()) {
            String key = jsonObjectIter.next();
            String value = null;
            try {
                value = aJsonObject.getString(key);
            } catch (JSONException e2) {
                e2.printStackTrace();
                Log.e("JSONException in Dialog", e2.toString());
            }
            params.putString(key, value);
        }
        SexyDialogRunnable aFeedDialog = new SexyDialogRunnable(name, params);
        SexyAppFrameworkActivity.instance().runOnUiThread(aFeedDialog);
    }

    public void NotifyFacebookMobileAppInstallAd() {
        if (this.mFBAppID != null) {
            Settings.publishInstallAsync(SexyAppFrameworkActivity.instance().getApplicationContext(), this.mFBAppID);
        }
    }
}
