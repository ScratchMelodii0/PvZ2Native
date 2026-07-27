package com.popcap.SexyAppFramework.GooglePlay;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.popcap.SexyAppFramework.SexyAppFrameworkActivity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/* JADX INFO: compiled from: GooglePlayAchievements.java */
/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class ResetAchievementsTask extends AsyncTask<Void, Void, Void> {
    public String mAccountName;
    public Context mContext;
    public String mScope;

    public ResetAchievementsTask(Context con, String name, String sc) {
        this.mContext = con;
        this.mAccountName = name;
        this.mScope = sc;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Void doInBackground(Void... params) {
        try {
            String accesstoken = GoogleAuthUtil.getToken(this.mContext, this.mAccountName, this.mScope);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://www.googleapis.com/games/v1management/achievements/reset?access_token=" + accesstoken);
            client.execute(post);
            Log.w("ResetAchievements", "Reset achievements done.");
            return null;
        } catch (Exception e) {
            Log.e("ResetAchievements", "Failed to reset: " + e.getMessage(), e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Void result) {
        SexyAppFrameworkActivity.instance().startActivityForResult(SexyAppFrameworkActivity.instance().getGamesClient().getAchievementsIntent(), 0);
    }
}
