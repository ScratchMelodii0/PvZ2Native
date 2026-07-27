package com.popcap.SexyAppFramework;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.impl.DownloadInfo;
import com.google.android.vending.expansion.downloader.impl.DownloadsDB;
import com.popcap.SexyAppFramework.AndroidSplashScreen;
import com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity;
import com.popcap.SexyAppFramework.cloud.Cloud;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SexyAppFrameworkActivity extends BaseGameActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "SexyAppFramework";
    private static GooglePlayPurchaseDriver mPurchaseDriver;
    private static AndroidSplashScreen mSplashScreen;
    private Cloud mCloud;
    private AndroidSurfaceView mGLView;
    private AndroidGameApp mGameApp;
    private Bundle mSavedInstanceState;
    private AndroidUIEventManager mUIEventManager;
    private static SexyAppFrameworkActivity sTheActivity = null;
    private static ArrayList<AndroidSplashScreen.SplashImage> mSplashScreenImages = null;

    public static SexyAppFrameworkActivity instance() {
        return sTheActivity;
    }

    public static String getAndroidPackageName() {
        return instance().getPackageName();
    }

    public SexyAppFrameworkActivity() {
        synchronized (SexyAppFrameworkActivity.class) {
            sTheActivity = this;
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != 0) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override // com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        checkPlayServices();
        super.onCreate(savedInstanceState);
        this.mSavedInstanceState = savedInstanceState;
        this.mUIEventManager = new AndroidUIEventManager();
    }

    public Point getDisplaySize() {
        Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        Point size = new Point(width, height);
        return size;
    }

    @Override // com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity
    public void launchGame() {
        Log.d(TAG, "SexyAppFrameworkActivity::LaunchGame");
        ActivityManager am = (ActivityManager) getSystemService("activity");
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        boolean bGLES2Supported = info.reqGlEsVersion >= 131072 || Build.FINGERPRINT.startsWith("generic");
        int iGLESVersionSupported = bGLES2Supported ? 2 : 1;
        this.mGLView = new AndroidSurfaceView(getApplicationContext(), getWindowManager(), this.mUIEventManager, iGLESVersionSupported);
        this.mCloud = new Cloud(sTheActivity);
        this.mGameApp = new AndroidGameApp(this, this.mGLView, this.mUIEventManager, this.mCloud);
        Point displaySize = getDisplaySize();
        this.mGLView.SetScreenSizeInPixels(displaySize.x, displaySize.y);
        this.mGLView.init(8, 8, 8, 0, 16, 0);
        setContentView(this.mGLView);
        AddSplashScreen();
        this.mGameApp.onActivityCreate(this.mSavedInstanceState, getIntent());
        Uri data = getIntent().getData();
        if (data != null) {
            this.mGameApp.setStartUrl(data.toString());
            getIntent().setData(null);
        }
        this.mGameApp.onActivityStart();
        super.launchGame();
    }

    public void AddSplashScreen() {
        if (!mSplashScreenImages.isEmpty()) {
            if (mSplashScreen == null) {
                mSplashScreen = new AndroidSplashScreen(getApplicationContext());
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(-2, -2);
                addContentView(mSplashScreen, lp);
                return;
            }
            return;
        }
        Log.e(TAG, "No splash screen specified for app. Add a call AddSplashScreenImage in your source wrapper to enable a splash screen.");
    }

    public void RemoveSplashScreen() {
        runOnUiThread(new Runnable() { // from class: com.popcap.SexyAppFramework.SexyAppFrameworkActivity.1
            @Override // java.lang.Runnable
            public void run() {
                if (SexyAppFrameworkActivity.this.SplashScreenShowing()) {
                    ViewGroup vg = (ViewGroup) SexyAppFrameworkActivity.mSplashScreen.getParent();
                    vg.removeView(SexyAppFrameworkActivity.mSplashScreen);
                    AndroidSplashScreen unused = SexyAppFrameworkActivity.mSplashScreen = null;
                }
            }
        });
    }

    public boolean SplashScreenShowing() {
        return mSplashScreen != null;
    }

    @Override // android.app.Activity
    public void onRestart() {
        Log.i(TAG, "onRestart");
        super.onRestart();
        if (this.mGameApp != null) {
            this.mGameApp.onActivityRestart();
        }
    }

    @Override // com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onResume() {
        Log.i(TAG, "onResume");
        checkPlayServices();
        super.onResume();
        if (this.mGLView != null && this.mGameApp != null) {
            this.mGLView.onResume();
            this.mGameApp.onActivityResume();
            this.mGLView.requestFocus();
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        if (this.mGLView != null && this.mGameApp != null) {
            this.mGameApp.onActivityPause();
            this.mGLView.onPause();
        }
    }

    @Override // com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
        if (this.mGameApp != null) {
            this.mGameApp.onActivityStop();
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        if (this.mGameApp != null) {
            this.mGameApp.onActivityDestroy();
            this.mGameApp = null;
        }
        this.mGLView = null;
        this.mUIEventManager = null;
        synchronized (SexyAppFrameworkActivity.class) {
            if (this == sTheActivity) {
                sTheActivity = null;
            }
        }
        System.exit(0);
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mGameApp != null) {
            this.mGameApp.onSaveInstanceState(outState);
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mGameApp.HandleOnConfigurationChanged(newConfig);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (this.mGameApp != null) {
            this.mGameApp.onWindowFocusChanged(hasFocus);
        }
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    public void setPurchaseDriver(GooglePlayPurchaseDriver driver) {
        mPurchaseDriver = driver;
    }

    @Override // com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, String.format("onActivityResult: requestCode: %d; resultCode: %d", Integer.valueOf(requestCode), Integer.valueOf(resultCode)));
        boolean isPurchaseResult = mPurchaseDriver != null && mPurchaseDriver.onActivityResult(requestCode, resultCode, data);
        if (!isPurchaseResult) {
            super.onActivityResult(requestCode, resultCode, data);
            this.mCloud.onActivityResult(requestCode, resultCode, data);
            if (this.mGameApp != null) {
                this.mGameApp.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    protected static void AddSplashScreenImage(int aBGResourceID, int aBGResourceWidth, int aBGResourceHeight) {
        if (mSplashScreenImages == null) {
            mSplashScreenImages = new ArrayList<>();
        }
        mSplashScreenImages.add(new AndroidSplashScreen.SplashImage(aBGResourceID, aBGResourceWidth, aBGResourceHeight));
    }

    public static class SplashImageComparator implements Comparator<AndroidSplashScreen.SplashImage> {
        @Override // java.util.Comparator
        public int compare(AndroidSplashScreen.SplashImage lhs, AndroidSplashScreen.SplashImage rhs) {
            return lhs.GetIntendedArea().compareTo(rhs.GetIntendedArea());
        }
    }

    public static int GetBestFitSplashImage(int aDisplayWidth, int aDisplayHeight) {
        int aSplashResourceID = -1;
        if (!mSplashScreenImages.isEmpty()) {
            Collections.sort(mSplashScreenImages, new SplashImageComparator());
            Rect aDisplayRect = new Rect(0, 0, aDisplayWidth, aDisplayHeight);
            Iterator<AndroidSplashScreen.SplashImage> it = mSplashScreenImages.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                AndroidSplashScreen.SplashImage img = it.next();
                Rect aIntendedDisplayRect = img.GetIntendedDisplayRect();
                if (aIntendedDisplayRect.contains(aDisplayRect)) {
                    aSplashResourceID = img.GetResourceID();
                    break;
                }
            }
        }
        if (aSplashResourceID == -1) {
            Log.e(TAG, String.format("No splash screen for display size: %dx%d", Integer.valueOf(aDisplayWidth), Integer.valueOf(aDisplayHeight)));
        }
        return aSplashResourceID;
    }

    public static String FrameworkInfo_SysGetMainExpansionFilePath() {
        String path = "";
        if (Environment.getExternalStorageState().equals("mounted")) {
            instance().getApplicationContext();
            DownloadsDB db = DownloadsDB.getDB(sTheActivity);
            DownloadInfo[] infos = db.getDownloads();
            if (infos != null) {
                for (DownloadInfo info : infos) {
                    info.logVerboseInfo();
                    if (info.mIndex == 0) {
                        String fileName = info.mFileName;
                        path = Helpers.generateSaveFileName(instance(), fileName);
                    }
                }
            }
            if (path.isEmpty()) {
                Log.e(TAG, "Could not determine OBB path.");
            }
        } else {
            Log.e(TAG, "External Storage not mounted. Can not download OBB.");
        }
        Log.e(TAG, "OBB file loc: " + path);
        return path;
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        Log.d(TAG, "Back Button pressed.");
        this.mUIEventManager.onBackButtonPressed();
    }

    public void onKeyboardDismissedWithBackButton() {
        if (this.mGameApp != null && this.mGameApp.mbIsKeyboardShowing) {
            this.mGameApp.mbIsKeyboardShowing = false;
        }
    }

    @Override // com.popcap.SexyAppFramework.GooglePlay.GameHelper.GameHelperListener
    public void onSignInSucceeded() {
        this.mCloud.Cloud_attemptSilentSync();
    }
}
