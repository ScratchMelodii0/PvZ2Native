package com.mobileapptracker;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
final class d implements Runnable {
    final /* synthetic */ MobileAppTracker a;
    private Context b;

    public d(MobileAppTracker mobileAppTracker, Context context) {
        this.a = mobileAppTracker;
        this.b = context;
    }

    @Override // java.lang.Runnable
    public final void run() {
        String userAgentString;
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                userAgentString = WebSettings.getDefaultUserAgent(this.b);
            } else {
                WebView webView = new WebView(this.b);
                userAgentString = webView.getSettings().getUserAgentString();
                webView.destroy();
            }
            this.a.b("ua", userAgentString);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MobileAppTracker", "Could not get user agent");
        }
    }
}
