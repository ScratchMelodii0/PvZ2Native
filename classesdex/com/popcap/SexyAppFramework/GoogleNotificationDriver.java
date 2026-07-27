package com.popcap.SexyAppFramework;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GoogleNotificationDriver {
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_USER_ALIAS = "userAlias";
    public static final long REGISTRATION_EXPIRY_TIME_MS = 604800000;
    private static final String TAG = "GoogleNotificationDriver";
    private long mNativeNotificationManagerImplPointer;
    private String mSenderId;
    private boolean mRegisteringWithGcm = false;
    private final Context mContext = SexyAppFrameworkActivity.instance();
    private GoogleCloudMessaging mGcm = GoogleCloudMessaging.getInstance(this.mContext);

    native void DidRegisterForRemoteNotifications(long j, String str);

    native void FailedRegisterForRemoteNotifications(long j);

    public GoogleNotificationDriver(long nativeNotificationManagerImplPointer, String senderId) {
        this.mNativeNotificationManagerImplPointer = nativeNotificationManagerImplPointer;
        Log.i(TAG, "incoming senderId: " + senderId);
        if (senderId == null || senderId.isEmpty()) {
            try {
                ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(), 128);
                this.mSenderId = ai.metaData.getString("pushNoteSenderId");
                return;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        this.mSenderId = senderId;
    }

    public void RegisterForRemoteNotifications() {
        boolean hasPermissions = this.mContext.checkCallingOrSelfPermission("com.google.android.c2dm.permission.RECEIVE") == 0;
        boolean hasPermissions2 = hasPermissions && this.mContext.checkCallingOrSelfPermission(new StringBuilder().append(this.mContext.getPackageName()).append(".permission.C2D_MESSAGE").toString()) == 0;
        if (!hasPermissions2) {
            Log.e(TAG, "Skipping registration due to missing permissions.");
            return;
        }
        String regid = getRegistrationId(this.mContext);
        Log.i(TAG, "Got registrationId: " + regid);
        if (regid.length() == 0) {
            Log.i(TAG, "Starting background registration");
            registerWithGCM();
        } else {
            DidRegisterForRemoteNotifications(this.mNativeNotificationManagerImplPointer, regid);
        }
    }

    public void UnregisterForRemoteNotifications() {
        unregisterWithGCM();
    }

    public void UpdateTokenRegistration(String userAlias) {
        registerWithGCM();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setRegistrationId(Context context, String regId) {
        SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "GCM: Saving regId " + regId + " on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;
        Log.v(TAG, "GCM: Setting registration expiry time to " + expirationTime);
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getRegistrationId(Context context) {
        SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isRegistrationExpired()) {
            Log.v(TAG, "GCM: App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }

    private void setUserAlias(Context context, String userAlias) {
        String currentUserAlias = getUserAlias(context);
        if (!currentUserAlias.equals(userAlias)) {
            SharedPreferences prefs = getGCMPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_USER_ALIAS, userAlias);
            editor.commit();
        }
    }

    private String getUserAlias(Context context) {
        SharedPreferences prefs = getGCMPreferences(context);
        String userAlias = prefs.getString(PROPERTY_USER_ALIAS, "");
        if (userAlias.length() == 0) {
            return "";
        }
        return userAlias;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(SexyAppFrameworkActivity.class.getSimpleName(), 0);
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("GCM: Could not get package name: " + e);
        }
    }

    private boolean isRegistrationExpired() {
        SharedPreferences prefs = getGCMPreferences(this.mContext);
        long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1L);
        return System.currentTimeMillis() > expirationTime;
    }

    private void registerWithGCM() {
        boolean startRegistration = false;
        synchronized (this) {
            if (!this.mRegisteringWithGcm) {
                this.mRegisteringWithGcm = true;
                startRegistration = true;
            }
        }
        if (startRegistration) {
            new Thread(new Runnable() { // from class: com.popcap.SexyAppFramework.GoogleNotificationDriver.1
                @Override // java.lang.Runnable
                public void run() {
                    Log.i(GoogleNotificationDriver.TAG, "Enter registerWithGCM::run");
                    try {
                        if (GoogleNotificationDriver.this.mGcm == null) {
                            GoogleNotificationDriver.this.mGcm = GoogleCloudMessaging.getInstance(GoogleNotificationDriver.this.mContext);
                        }
                        String registrationId = GoogleNotificationDriver.this.getRegistrationId(GoogleNotificationDriver.this.mContext);
                        if (registrationId.length() == 0) {
                            Log.v(GoogleNotificationDriver.TAG, "Re-registering GCM");
                            registrationId = GoogleNotificationDriver.this.mGcm.register(GoogleNotificationDriver.this.mSenderId);
                            GoogleNotificationDriver.this.setRegistrationId(GoogleNotificationDriver.this.mContext, registrationId);
                        }
                        Log.v(GoogleNotificationDriver.TAG, "GCM: registrationId: " + registrationId);
                        synchronized (GoogleNotificationDriver.this) {
                            GoogleNotificationDriver.this.mRegisteringWithGcm = false;
                        }
                        GoogleNotificationDriver.this.DidRegisterForRemoteNotifications(GoogleNotificationDriver.this.mNativeNotificationManagerImplPointer, registrationId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(GoogleNotificationDriver.TAG, "Failed to register with GCM: " + e.getMessage());
                        GoogleNotificationDriver.this.FailedRegisterForRemoteNotifications(GoogleNotificationDriver.this.mNativeNotificationManagerImplPointer);
                    }
                }
            }).start();
        }
    }

    private void unregisterWithGCM() {
    }
}
