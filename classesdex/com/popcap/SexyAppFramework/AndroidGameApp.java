package com.popcap.SexyAppFramework;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;
import com.popcap.SexyAppFramework.GooglePlay.GooglePlayAchievements;
import com.popcap.SexyAppFramework.GooglePlay.GooglePlayConnect;
import com.popcap.SexyAppFramework.GooglePlay.GooglePlayLeaderboard;
import com.popcap.SexyAppFramework.cloud.Cloud;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidGameApp implements AudioManager.OnAudioFocusChangeListener {
    private static final String EXP_PATH = "/Android/obb/";
    private static final int UI_ORIENTATION_FACE_DOWN = 6;
    private static final int UI_ORIENTATION_FACE_UP = 5;
    private static final int UI_ORIENTATION_LANDSCAPE_LEFT = 4;
    private static final int UI_ORIENTATION_LANDSCAPE_RIGHT = 3;
    private static final int UI_ORIENTATION_PORTRAIT = 1;
    private static final int UI_ORIENTATION_PORTRAIT_UPSIDE_DOWN = 2;
    private static final int UI_ORIENTATION_UNKNOWN = 0;
    private Activity mActivity;
    private AndroidNotification mAndroidNotificationManager;
    private boolean mCanUseWakeLock;
    private int mCurrentOrientation;
    private AndroidFacebookDriver mFBDriver;
    private AndroidSurfaceView mGLView;
    private AndroidHttpProxy mHttpProxy;
    private GooglePlayAchievements mPlayAchievementsClass;
    private GooglePlayConnect mPlayConnectClass;
    private GooglePlayLeaderboard mPlayLeaderboardClass;
    private AndroidUIEventManager mUIEventManager;
    public boolean mbAppIsSuspended;
    public boolean mbNativeInitialized;
    private PowerManager.WakeLock mlWakeLock = null;
    private String mCommandLineFlag = null;
    private boolean mbGotFocusEvent = false;
    private boolean mbGotResumedEvent = false;
    private boolean mbAppIsFocusedState = false;
    public boolean mbIsKeyboardShowing = false;
    private boolean mbMonitoringAudioFocus = false;

    private native boolean Native_GameAppInitialize(AndroidSurfaceView androidSurfaceView, AndroidHttpProxy androidHttpProxy, AndroidFacebookDriver androidFacebookDriver, Cloud cloud, GooglePlayConnect googlePlayConnect, GooglePlayAchievements googlePlayAchievements, GooglePlayLeaderboard googlePlayLeaderboard, AndroidNotification androidNotification);

    private native void Native_GameAppTeardown();

    private native void Native_NotifyAudioGainedFocus();

    private native void Native_NotifyAudioLostFocus();

    private native void Native_applicationDidBecomeActive();

    private native void Native_applicationDidEnterBackground();

    private native void Native_applicationDidFinishLaunching();

    private native void Native_applicationWillBecomeForeground();

    private native void Native_applicationWillFinishLaunching(String str);

    private native void Native_applicationWillLoseFocus();

    private native void Native_applicationWillResignActive();

    private native void Native_applicationWillTerminate();

    private native void Native_onOrientationChanged(int i, int i2);

    private native void Native_setStartUrl(String str);

    public AndroidGameApp(Activity InActivity, AndroidSurfaceView InGLView, AndroidUIEventManager InUIEventManager, Cloud cloud) {
        this.mFBDriver = null;
        this.mPlayConnectClass = null;
        this.mPlayAchievementsClass = null;
        this.mPlayLeaderboardClass = null;
        this.mCanUseWakeLock = false;
        this.mActivity = InActivity;
        this.mGLView = InGLView;
        this.mHttpProxy = new AndroidHttpProxy(this.mActivity.getApplicationContext());
        this.mHttpProxy.Startup();
        this.mUIEventManager = InUIEventManager;
        this.mbAppIsSuspended = false;
        this.mFBDriver = new AndroidFacebookDriver();
        this.mPlayConnectClass = new GooglePlayConnect();
        this.mPlayAchievementsClass = new GooglePlayAchievements();
        this.mPlayLeaderboardClass = new GooglePlayLeaderboard();
        this.mAndroidNotificationManager = new AndroidNotification();
        this.mbNativeInitialized = Native_GameAppInitialize(this.mGLView, this.mHttpProxy, this.mFBDriver, cloud, this.mPlayConnectClass, this.mPlayAchievementsClass, this.mPlayLeaderboardClass, this.mAndroidNotificationManager);
        this.mCurrentOrientation = Device_GetCurrentUIOrientation();
        this.mCanUseWakeLock = this.mActivity.checkCallingOrSelfPermission("android.permission.WAKE_LOCK") == 0;
        Log.v("AndroidGameApp", String.format("Constructor: CurrentUIOrientation %d", Integer.valueOf(this.mCurrentOrientation)));
    }

    public void onActivityCreate(Bundle savedInstanceState, Intent intent) {
        this.mCommandLineFlag = intent.getStringExtra("commandLine");
        Native_applicationWillFinishLaunching(this.mCommandLineFlag);
        this.mFBDriver.onCreate(savedInstanceState);
        if (this.mCanUseWakeLock && this.mlWakeLock == null) {
            PowerManager pm = (PowerManager) this.mActivity.getSystemService("power");
            this.mlWakeLock = pm.newWakeLock(268435466, "PopCapWakeLock");
            this.mlWakeLock.acquire();
        }
    }

    private boolean RequestAudioFocus() {
        AudioManager am = (AudioManager) this.mActivity.getSystemService("audio");
        if (am != null) {
            int result = am.requestAudioFocus(this, 3, 1);
            if (result == 1) {
                this.mbMonitoringAudioFocus = true;
                Native_NotifyAudioGainedFocus();
                return true;
            }
        }
        return false;
    }

    private void ReleaseAudioFocus() {
        AudioManager am = (AudioManager) this.mActivity.getSystemService("audio");
        if (am != null) {
            am.abandonAudioFocus(this);
            this.mbMonitoringAudioFocus = false;
        }
    }

    private boolean IsUserMusicPlaying() {
        AudioManager am = (AudioManager) this.mActivity.getSystemService("audio");
        if (am != null) {
            return am.isMusicActive();
        }
        return false;
    }

    @Override // android.media.AudioManager.OnAudioFocusChangeListener
    public void onAudioFocusChange(int focusChange) {
        Log.i("AudioFocus", "FocusChange: " + focusChange);
        if (focusChange == -1 || focusChange == -2 || focusChange == -3) {
            Native_NotifyAudioLostFocus();
        } else if (focusChange == 1) {
            Native_NotifyAudioGainedFocus();
        }
    }

    public void onActivityStart() {
        Native_applicationDidFinishLaunching();
    }

    public void onActivityRestart() {
    }

    public void onActivityResume() {
        if (!RequestAudioFocus()) {
            Log.e("AudioFocus", "Failed to gain audio focus!");
        }
        this.mbGotResumedEvent = true;
        Native_applicationWillBecomeForeground();
        if (this.mCanUseWakeLock) {
            if (this.mlWakeLock == null) {
                PowerManager pm = (PowerManager) this.mActivity.getSystemService("power");
                this.mlWakeLock = pm.newWakeLock(10, "PopCapWakeLock");
            }
            if (!this.mlWakeLock.isHeld()) {
                this.mlWakeLock.acquire();
            }
        }
        if (this.mFBDriver != null) {
            this.mFBDriver.onResume();
        }
        TryGainFocus();
    }

    public void onActivityPause() {
        this.mbGotResumedEvent = false;
        if (this.mFBDriver != null) {
            this.mFBDriver.onPause();
        }
        TryLoseFocus();
        if (this.mlWakeLock != null && this.mlWakeLock.isHeld()) {
            this.mlWakeLock.release();
        }
        Native_applicationDidEnterBackground();
        if (this.mGLView.mbDrawingFrames) {
            Object waitObj = new Object();
            synchronized (waitObj) {
                this.mGLView.mbNotifyOnDrawEventObject = waitObj;
                try {
                    waitObj.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        this.mGLView.mbDrawingFrames = false;
        if (this.mbMonitoringAudioFocus) {
            ReleaseAudioFocus();
        }
    }

    public void onActivityStop() {
    }

    public void onActivityDestroy() {
        Native_applicationWillTerminate();
        this.mlWakeLock = null;
        if (this.mFBDriver != null) {
            this.mFBDriver.onDestroy();
            this.mFBDriver = null;
        }
        this.mHttpProxy.Shutdown();
        this.mGLView = null;
        this.mHttpProxy = null;
        this.mUIEventManager = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mFBDriver != null) {
            this.mFBDriver.onSaveInstanceState(outState);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mFBDriver != null) {
            this.mFBDriver.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void HandleOnConfigurationChanged(Configuration newConfig) {
        int newOrientation = Device_GetCurrentUIOrientation();
        if (this.mCurrentOrientation != newOrientation) {
            Native_onOrientationChanged(this.mCurrentOrientation, newOrientation);
            this.mCurrentOrientation = newOrientation;
        }
    }

    public String Util_GetUUIDString() {
        return UUID.randomUUID().toString();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            this.mbGotFocusEvent = true;
            TryGainFocus();
        } else {
            this.mbGotFocusEvent = false;
            TryLoseFocus();
        }
    }

    private void TryGainFocus() {
        this.mCurrentOrientation = Device_GetCurrentUIOrientation();
        Object[] objArr = new Object[4];
        objArr[0] = this.mbAppIsFocusedState ? "true" : "false";
        objArr[1] = this.mbGotFocusEvent ? "true" : "false";
        objArr[2] = this.mbGotResumedEvent ? "true" : "false";
        objArr[3] = Integer.valueOf(this.mCurrentOrientation);
        Log.v("AndroidGameApp", String.format("TryGainFocus IsFocused=%s GotFocus=%s GotResumed=%s Orientation=%d", objArr));
        if (!this.mbAppIsFocusedState && this.mbGotFocusEvent && this.mbGotResumedEvent) {
            this.mUIEventManager.HandleGotFocus();
            Native_applicationDidBecomeActive();
            this.mbAppIsFocusedState = true;
            this.mGLView.SetFocusState(true);
        }
    }

    private void TryLoseFocus() {
        if (this.mbAppIsFocusedState) {
            if (!this.mbGotFocusEvent || !this.mbGotResumedEvent) {
                this.mbAppIsFocusedState = false;
                this.mbIsKeyboardShowing = false;
                this.mGLView.SetFocusState(false);
                this.mUIEventManager.HandleLostFocus();
                Native_applicationWillResignActive();
            }
        }
    }

    public void setStartUrl(String $startUrl) {
        Native_setStartUrl($startUrl);
    }

    public boolean Config_ConfigKeyExists(String InKeyName) {
        SharedPreferences prefs = this.mActivity.getPreferences(0);
        return prefs.contains(InKeyName);
    }

    public void Config_ConfigEraseKey(String InKeyName) {
        SharedPreferences.Editor prefsEditor = this.mActivity.getPreferences(0).edit();
        prefsEditor.remove(InKeyName).commit();
    }

    private String Config_ConfigReadString(String InKeyName) {
        SharedPreferences prefs = this.mActivity.getPreferences(0);
        try {
            String result = prefs.getString(InKeyName, null);
            return result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private int Config_ConfigReadInteger(String InKeyName) {
        SharedPreferences prefs = this.mActivity.getPreferences(0);
        try {
            int result = prefs.getInt(InKeyName, 0);
            return result;
        } catch (ClassCastException e) {
            return 0;
        }
    }

    private boolean Config_ConfigReadBoolean(String InKeyName) {
        SharedPreferences prefs = this.mActivity.getPreferences(0);
        try {
            boolean result = prefs.getBoolean(InKeyName, false);
            return result;
        } catch (ClassCastException e) {
            return false;
        }
    }

    private boolean Config_ConfigWriteString(String InKeyName, String InValue) {
        SharedPreferences.Editor prefsEditor = this.mActivity.getPreferences(0).edit();
        return prefsEditor.putString(InKeyName, InValue).commit();
    }

    private boolean Config_ConfigWriteInteger(String InKeyName, int InValue) {
        SharedPreferences.Editor prefsEditor = this.mActivity.getPreferences(0).edit();
        return prefsEditor.putInt(InKeyName, InValue).commit();
    }

    private boolean Config_ConfigWriteBoolean(String InKeyName, boolean InValue) {
        SharedPreferences.Editor prefsEditor = this.mActivity.getPreferences(0).edit();
        return prefsEditor.putBoolean(InKeyName, InValue).commit();
    }

    private boolean UI_ProcessEvents(ByteBuffer OutData) {
        return this.mUIEventManager.ProcessEvents(OutData);
    }

    private void UI_DidRecieveFocus() {
        SexyAppFrameworkActivity.instance().RemoveSplashScreen();
    }

    private void UI_UpdateSelection(int theStart, int theEnd, int theCandidateStart, int theCandidateEnd) {
        InputMethodManager imm = (InputMethodManager) this.mActivity.getSystemService("input_method");
        imm.updateSelection(this.mGLView, theStart, theEnd, theCandidateStart, theCandidateEnd);
    }

    private String Info_SysGetProductVersion() {
        PackageInfo pInfo;
        int version = -1;
        Context ctxt = this.mActivity.getApplicationContext();
        try {
            pInfo = ctxt.getPackageManager().getPackageInfo(ctxt.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            pInfo = null;
        }
        if (pInfo != null) {
            version = pInfo.versionCode;
        }
        return Integer.toString(version);
    }

    private String Info_SysGetTimeBombDate() {
        ApplicationInfo aInfo;
        String TBDt = "L";
        Context ctxt = this.mActivity.getApplicationContext();
        try {
            aInfo = ctxt.getPackageManager().getApplicationInfo(ctxt.getPackageName(), 128);
        } catch (PackageManager.NameNotFoundException e) {
            aInfo = null;
        }
        if (aInfo != null) {
            TBDt = aInfo.metaData.getString("TBDt");
        }
        return TBDt.substring(1);
    }

    private String Info_SysGetProductVersionString() {
        String aProductVersion = null;
        Context ctxt = this.mActivity.getApplicationContext();
        PackageInfo pInfo = null;
        try {
            pInfo = ctxt.getPackageManager().getPackageInfo(ctxt.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            aProductVersion = null;
        }
        if (pInfo != null) {
            String aProductVersion2 = pInfo.versionName;
            return aProductVersion2;
        }
        return aProductVersion;
    }

    private String Info_SysGetPackageName() {
        Context ctxt = this.mActivity.getApplicationContext();
        return ctxt.getPackageName();
    }

    private String Info_SysGetActivityName() {
        ComponentName info = this.mActivity.getComponentName();
        String name = info.getClassName();
        return name;
    }

    private String Info_SysGetIntentExtraDataString(String keyStr) {
        Intent intent = this.mActivity.getIntent();
        if (!intent.hasExtra("data.fields")) {
            Bundle fields = new Bundle();
            intent.putExtra("data.fields", fields);
        }
        if (!intent.hasExtra("data.fields")) {
            return null;
        }
        Bundle extraBundle = intent.getExtras().getBundle("data.fields");
        if (extraBundle.isEmpty()) {
            return null;
        }
        return extraBundle.getString(keyStr, null);
    }

    private String Info_SysGetUserCurrencySymbol() {
        Currency aCurrency = Currency.getInstance(Locale.getDefault());
        String aUserCurrencySymbol = aCurrency.getSymbol(Locale.getDefault());
        return aUserCurrencySymbol;
    }

    public String Info_SysGetUserLocale() {
        return Locale.getDefault().toString();
    }

    public String Info_SysGetCountryCodeString() {
        String aCountryCode = Locale.getDefault().getCountry();
        return aCountryCode;
    }

    private void Deprecated_DisplayGetSize(Display disp, Point pnt) {
        pnt.x = disp.getWidth();
        pnt.y = disp.getHeight();
    }

    private int Device_GetCurrentUIOrientation() {
        try {
            Display disp = this.mActivity.getWindowManager().getDefaultDisplay();
            Point tmpPnt = new Point();
            if (Build.VERSION.SDK_INT >= 13) {
                disp.getSize(tmpPnt);
            } else {
                Deprecated_DisplayGetSize(disp, tmpPnt);
            }
            int width = tmpPnt.x;
            int height = tmpPnt.y;
            int rotation = disp.getRotation();
            int format = 0;
            if (width > 0 && height > 0) {
                format = width >= height ? 2 : 1;
            }
            if (format == 2) {
                if (rotation == 0 || rotation == 1) {
                    return 4;
                }
                return 3;
            }
            if (format == 1) {
                return (rotation == 0 || rotation == 1) ? 1 : 2;
            }
            return 0;
        } catch (Exception ex) {
            Log.e("Exception", ex.toString());
            return 0;
        }
    }

    private boolean Device_IsSupportedUIOrientation(int UIOrientation) {
        return UIOrientation == 4 || UIOrientation == 3 || UIOrientation == 1 || UIOrientation == 2;
    }

    public String Device_GetDeviceName() {
        return AndroidUtil.getDeviceName();
    }

    public boolean Device_IsKeyboardShowing() {
        return this.mbIsKeyboardShowing;
    }

    public void Device_ShowKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.mActivity.getSystemService("input_method");
        if (imm != null) {
            imm.showSoftInput(this.mGLView, 2, null);
            this.mbIsKeyboardShowing = true;
        }
    }

    public void Device_HideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.mActivity.getSystemService("input_method");
        if (imm != null) {
            imm.hideSoftInputFromWindow(this.mGLView.getWindowToken(), 0);
            this.mbIsKeyboardShowing = false;
        }
    }

    private String Device_GetCachesDir() {
        Context ctxt = this.mActivity.getApplicationContext();
        return ctxt.getCacheDir().getAbsolutePath();
    }

    private void Device_ExitToHome() {
        Intent aGoToHomeIntent = new Intent("android.intent.action.MAIN");
        aGoToHomeIntent.addCategory("android.intent.category.HOME");
        aGoToHomeIntent.setFlags(268435456);
        this.mActivity.startActivity(aGoToHomeIntent);
    }

    private String Resources_GetResourceFolder() {
        File privateFilesDir = this.mActivity.getFilesDir();
        return privateFilesDir.toString();
    }

    private String Resources_GetUserDataFolder() {
        Context ctxt = this.mActivity.getApplicationContext();
        File dirFile = ctxt.getExternalFilesDir(null);
        if (dirFile == null) {
            dirFile = ctxt.getFilesDir();
        }
        if (dirFile == null) {
            return null;
        }
        String dirName = dirFile.getAbsolutePath();
        return dirName;
    }

    private String Resources_GetCacheDataFolder() {
        return Resources_GetUserDataFolder();
    }

    private String Resources_GetAppSupportDataFolder() {
        return Resources_GetUserDataFolder();
    }

    private String Resources_GetAssetFileInfo(String InFilename, long[] outFileInfo) {
        AssetManager assetMgr = this.mActivity.getAssets();
        if (assetMgr != null && InFilename.length() > 0) {
            String actualFilename = AndroidAssetUtils.GetAssetNameFromFileName(InFilename);
            try {
                AssetFileDescriptor afd = assetMgr.openFd(actualFilename);
                if (afd != null) {
                    outFileInfo[0] = afd.getStartOffset();
                    outFileInfo[1] = afd.getLength();
                    afd.close();
                    return this.mActivity.getPackageResourcePath();
                }
            } catch (IOException ex) {
                Log.e("GetAssetFileInfo", ex.toString());
            }
        }
        return null;
    }

    private long Resources_GetAssetFileSize(String InFilename) {
        AssetManager assetMgr = this.mActivity.getAssets();
        if (assetMgr != null && InFilename.length() > 0) {
            String actualFilename = AndroidAssetUtils.GetAssetNameFromFileName(InFilename);
            try {
                AssetFileDescriptor afd = assetMgr.openFd(actualFilename);
                return afd.getLength();
            } catch (IOException ex) {
                Log.e("GetAssetFileSize", ex.toString());
            }
        }
        return -1L;
    }

    private long Resources_GetFileSystemBlockCount(String InFilename) {
        try {
            StatFs stat = new StatFs(InFilename);
            long n = stat.getBlockCountLong();
            return n;
        } catch (Exception ex) {
            Log.e("GetFileSystemBlockCount", ex.toString());
            return -1L;
        }
    }

    private long Resources_GetFileSystemBlocksFree(String InFilename) {
        try {
            StatFs stat = new StatFs(InFilename);
            long n = stat.getAvailableBlocksLong();
            return n;
        } catch (Exception ex) {
            Log.e("GetFileSystemBlocksFree", ex.toString());
            return -1L;
        }
    }

    private long Resources_GetFileSystemBlockSize(String InFilename) {
        try {
            StatFs stat = new StatFs(InFilename);
            long n = stat.getBlockSizeLong();
            return n;
        } catch (Exception ex) {
            Log.e("GetFileSystemBlockSize", ex.toString());
            return -1L;
        }
    }

    private String Resources_GetExternalStorageDirectory() {
        try {
            if (!Environment.getExternalStorageState().equals("mounted")) {
                return null;
            }
            String sdCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
            return sdCardRoot;
        } catch (Exception e) {
            return null;
        }
    }

    private String Diag_GetDeviceID() {
        String strDeviceID = Settings.Secure.getString(this.mActivity.getApplicationContext().getContentResolver(), "android_id");
        return !strDeviceID.isEmpty() ? strDeviceID : "UNKNOWN/EMULATOR";
    }

    private String Diag_GetOSVersion() {
        return Build.VERSION.RELEASE;
    }

    private String Diag_GetHardwareModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return model.startsWith(manufacturer) ? model : manufacturer + " " + model;
    }

    private void DEBUG_PauseInJava(String FileName, int LineNo) {
        Log.e("PauseInJava", String.format("File: \"%s\"(%d)", FileName, Integer.valueOf(LineNo)));
    }

    private boolean Web_SysOpenURL(String theURL) {
        Intent i = new Intent("android.intent.action.VIEW", Uri.parse(theURL));
        this.mActivity.startActivity(i);
        return true;
    }

    public boolean Device_IsTablet() {
        Context context = this.mActivity.getApplicationContext();
        return (context.getResources().getConfiguration().screenLayout & 15) >= 3;
    }
}
