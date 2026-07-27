package com.popcap.PvZ2;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.ea.game.pvz2_na.R;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.popcap.SexyAppFramework.AndroidUtil;
import com.popcap.SexyAppFramework.SexyAppFrameworkActivity;
import java.io.File;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class PvZ2GameActivity extends SexyAppFrameworkActivity implements IDownloaderClient {
    private static final String LOG_TAG = "PvZ2GameActivity";
    private TextView mAverageSpeed;
    private View mCellMessage;
    private View mDashboard;
    private IStub mDownloaderClientStub;
    private ProgressBar mPB;
    private Button mPauseButton;
    private TextView mProgressFraction;
    private TextView mProgressPercent;
    private IDownloaderService mRemoteService;
    private int mState;
    private boolean mStatePaused;
    private TextView mStatusText;
    private TextView mTimeRemaining;
    private Button mWiFiSettingsButton;

    static {
        System.loadLibrary("PVZ2");
        AddSplashScreenImage(R.drawable.splash_hvga, 480, 320);
        AddSplashScreenImage(R.drawable.splash_wvga, 800, 480);
        AddSplashScreenImage(R.drawable.splash_fwvga, 854, 480);
        AddSplashScreenImage(R.drawable.splash_qhd, 960, 540);
        AddSplashScreenImage(R.drawable.splash_wsvga, 1024, 600);
        AddSplashScreenImage(R.drawable.splash_wxga, 1280, 768);
        AddSplashScreenImage(R.drawable.splash_10in, 1200, 800);
        AddSplashScreenImage(R.drawable.splash_1080p, 1920, 1080);
        AddSplashScreenImage(R.drawable.splash_wqxga, 2560, 1600);
    }

    boolean expansionFileDelivered() {
        String path = SexyAppFrameworkActivity.FrameworkInfo_SysGetMainExpansionFilePath();
        File fileForNewFile = new File(path);
        return (path == null || path.isEmpty() || !fileForNewFile.exists()) ? false : true;
    }

    public boolean isGooglePlayExpansionEnabled() {
        ApplicationInfo aInfo;
        Context ctxt = getApplicationContext();
        try {
            aInfo = ctxt.getPackageManager().getApplicationInfo(ctxt.getPackageName(), 128);
        } catch (PackageManager.NameNotFoundException e) {
            aInfo = null;
        }
        if (aInfo == null) {
            return false;
        }
        boolean isEnabled = aInfo.metaData.getBoolean("GooglePlayExpansionEnabled");
        return isEnabled;
    }

    @Override // com.popcap.SexyAppFramework.SexyAppFrameworkActivity, com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        int applicationFlags = getApplicationInfo().flags;
        if ((applicationFlags & 2) != 0) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
        int newOrientation = CalcDesiredOrientationPolicy();
        int oldOrientation = getRequestedOrientation();
        if (newOrientation != oldOrientation) {
            setRequestedOrientation(newOrientation);
        }
        Log.d(LOG_TAG, String.format("PVZ2GameActivity::onCreate : Orientation new %d old %d", Integer.valueOf(newOrientation), Integer.valueOf(oldOrientation)));
        Log.d(LOG_TAG, String.format("PVZ2GameActivity::onCreate : Device ID [%s]  Model [%s]", AndroidUtil.getDeviceId(), AndroidUtil.getDeviceName()));
        super.onCreate(savedInstanceState);
        if (isGooglePlayExpansionEnabled() && !expansionFileDelivered()) {
            try {
                Intent launchIntent = getIntent();
                Intent intentToLaunchThisActivityFromNotification = new Intent(this, getClass());
                intentToLaunchThisActivityFromNotification.setFlags(335544320);
                intentToLaunchThisActivityFromNotification.setAction(launchIntent.getAction());
                if (launchIntent.getCategories() != null) {
                    for (String category : launchIntent.getCategories()) {
                        intentToLaunchThisActivityFromNotification.addCategory(category);
                    }
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentToLaunchThisActivityFromNotification, 134217728);
                int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this, pendingIntent, (Class<?>) PvZ2DownloaderService.class);
                if (startResult != 0) {
                    initializeDownloadUI();
                    return;
                }
                Log.e(LOG_TAG, "No expansion file present, but nothing to download.");
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOG_TAG, "No need to download!");
                e.printStackTrace();
            }
        }
        super.launchGame();
    }

    @Override // com.popcap.SexyAppFramework.SexyAppFrameworkActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onResume() {
        int newOrientation = CalcDesiredOrientationPolicy();
        int oldOrientation = getRequestedOrientation();
        if (newOrientation != oldOrientation) {
            setRequestedOrientation(newOrientation);
        }
        super.onResume();
    }

    private void deleteOldExpansionFiles() {
        File expansionDir = new File(Helpers.getSaveFilePath(this));
        File currentExpansionFile = new File(SexyAppFrameworkActivity.FrameworkInfo_SysGetMainExpansionFilePath());
        Log.i(LOG_TAG, "Current OBB: " + currentExpansionFile.getAbsolutePath());
        File[] arr$ = expansionDir.listFiles();
        for (File file : arr$) {
            if (currentExpansionFile.compareTo(file) != 0) {
                Log.i(LOG_TAG, "Deleting old file: " + file.getAbsolutePath());
                file.delete();
            }
        }
    }

    private int CalcDesiredOrientationPolicy() {
        if (Build.VERSION.SDK_INT >= 18) {
            return 11;
        }
        return Settings.System.getInt(getContentResolver(), "accelerometer_rotation", 0) != 0 ? 6 : 0;
    }

    private void setState(int newState) {
        if (this.mState != newState) {
            this.mState = newState;
            this.mStatusText.setText(Helpers.getDownloaderStringResourceIDFromState(newState));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setButtonPausedState(boolean paused) {
        this.mStatePaused = paused;
        int stringResourceID = paused ? R.string.text_button_resume : R.string.text_button_pause;
        this.mPauseButton.setText(stringResourceID);
    }

    private void initializeDownloadUI() {
        this.mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this, PvZ2DownloaderService.class);
        setContentView(R.layout.main);
        this.mPB = (ProgressBar) findViewById(R.id.progressBar);
        this.mStatusText = (TextView) findViewById(R.id.statusText);
        this.mProgressFraction = (TextView) findViewById(R.id.progressAsFraction);
        this.mProgressPercent = (TextView) findViewById(R.id.progressAsPercentage);
        this.mAverageSpeed = (TextView) findViewById(R.id.progressAverageSpeed);
        this.mTimeRemaining = (TextView) findViewById(R.id.progressTimeRemaining);
        this.mDashboard = findViewById(R.id.downloaderDashboard);
        this.mCellMessage = findViewById(R.id.approveCellular);
        this.mPauseButton = (Button) findViewById(R.id.pauseButton);
        this.mWiFiSettingsButton = (Button) findViewById(R.id.wifiSettingsButton);
        this.mPauseButton.setOnClickListener(new View.OnClickListener() { // from class: com.popcap.PvZ2.PvZ2GameActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (PvZ2GameActivity.this.mStatePaused) {
                    PvZ2GameActivity.this.mRemoteService.requestContinueDownload();
                } else {
                    PvZ2GameActivity.this.mRemoteService.requestPauseDownload();
                }
                PvZ2GameActivity.this.setButtonPausedState(!PvZ2GameActivity.this.mStatePaused);
            }
        });
        this.mWiFiSettingsButton.setOnClickListener(new View.OnClickListener() { // from class: com.popcap.PvZ2.PvZ2GameActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                PvZ2GameActivity.this.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
            }
        });
        Button resumeOnCell = (Button) findViewById(R.id.resumeOverCellular);
        resumeOnCell.setOnClickListener(new View.OnClickListener() { // from class: com.popcap.PvZ2.PvZ2GameActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                PvZ2GameActivity.this.mRemoteService.setDownloadFlags(1);
                PvZ2GameActivity.this.mRemoteService.requestContinueDownload();
                PvZ2GameActivity.this.mCellMessage.setVisibility(8);
            }
        });
    }

    @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
    public void onServiceConnected(Messenger m) {
        this.mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        this.mRemoteService.onClientUpdated(this.mDownloaderClientStub.getMessenger());
    }

    @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
    public void onDownloadStateChanged(int newState) {
        boolean paused;
        boolean indeterminate;
        setState(newState);
        boolean showDashboard = true;
        boolean showCellMessage = false;
        switch (newState) {
            case 1:
                paused = false;
                indeterminate = true;
                break;
            case 2:
            case 3:
                showDashboard = true;
                paused = false;
                indeterminate = true;
                break;
            case 4:
                paused = false;
                showDashboard = true;
                indeterminate = false;
                break;
            case 5:
                this.mDownloaderClientStub.disconnect(this);
                deleteOldExpansionFiles();
                if (Build.VERSION.SDK_INT >= 11) {
                    recreate();
                    return;
                }
                Intent intent = getIntent();
                intent.addFlags(AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return;
            case 6:
            case 10:
            case 11:
            case 13:
            case IDownloaderClient.STATE_FAILED_SDCARD_FULL /* 17 */:
            default:
                paused = true;
                indeterminate = true;
                showDashboard = true;
                break;
            case 7:
                paused = true;
                indeterminate = false;
                break;
            case 8:
            case 9:
                showDashboard = false;
                paused = true;
                indeterminate = false;
                showCellMessage = true;
                break;
            case 12:
            case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE /* 14 */:
                paused = true;
                indeterminate = false;
                break;
            case IDownloaderClient.STATE_FAILED_UNLICENSED /* 15 */:
            case 16:
            case IDownloaderClient.STATE_FAILED_CANCELED /* 18 */:
            case 19:
                paused = true;
                showDashboard = false;
                indeterminate = false;
                break;
        }
        int newDashboardVisibility = showDashboard ? 0 : 8;
        if (this.mDashboard.getVisibility() != newDashboardVisibility) {
            this.mDashboard.setVisibility(newDashboardVisibility);
        }
        int cellMessageVisibility = showCellMessage ? 0 : 8;
        if (this.mCellMessage.getVisibility() != cellMessageVisibility) {
            this.mCellMessage.setVisibility(cellMessageVisibility);
        }
        this.mPB.setIndeterminate(indeterminate);
        setButtonPausedState(paused);
    }

    @Override // com.google.android.vending.expansion.downloader.IDownloaderClient
    public void onDownloadProgress(DownloadProgressInfo progress) {
        this.mAverageSpeed.setText(getString(2130968596, new Object[]{Helpers.getSpeedString(progress.mCurrentSpeed)}));
        this.mTimeRemaining.setText(getString(2130968597, new Object[]{Helpers.getTimeRemaining(progress.mTimeRemaining)}));
        progress.mOverallTotal = progress.mOverallTotal;
        this.mPB.setMax((int) (progress.mOverallTotal >> 8));
        this.mPB.setProgress((int) (progress.mOverallProgress >> 8));
        this.mProgressPercent.setText(Long.toString((progress.mOverallProgress * 100) / progress.mOverallTotal) + "%");
        this.mProgressFraction.setText(Helpers.getDownloadProgressString(progress.mOverallProgress, progress.mOverallTotal));
    }

    @Override // com.popcap.SexyAppFramework.SexyAppFrameworkActivity, com.popcap.SexyAppFramework.GooglePlay.BaseGameActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onStart() {
        if (this.mDownloaderClientStub != null) {
            this.mDownloaderClientStub.connect(this);
        }
        super.onStart();
    }

    @Override // com.popcap.SexyAppFramework.SexyAppFrameworkActivity, android.app.Activity
    public void onRestart() {
        if (this.mDownloaderClientStub != null) {
            this.mDownloaderClientStub.disconnect(this);
        }
        super.onRestart();
    }

    @Override // com.popcap.SexyAppFramework.SexyAppFrameworkActivity
    public Point getDisplaySize() {
        Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if (width < height) {
            height = width;
            width = height;
        }
        Point size = new Point(width, height);
        return size;
    }
}
