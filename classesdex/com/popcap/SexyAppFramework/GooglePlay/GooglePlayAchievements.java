package com.popcap.SexyAppFramework.GooglePlay;

import android.util.Log;
import com.google.android.gms.games.GamesClient;
import com.popcap.SexyAppFramework.SexyAppFrameworkActivity;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GooglePlayAchievements {
    static int REQUEST_ACHIEVEMENTS = 66;

    GamesClient GetGamesClient() {
        return SexyAppFrameworkActivity.instance().getGamesClient();
    }

    void Play_QueueAchievement(String achievementID) {
        GamesClient gamesClient = GetGamesClient();
        if (gamesClient.isConnected()) {
            gamesClient.unlockAchievement(achievementID);
        }
    }

    void Play_QueueAchievement_Percentage(String achievementID, float percentage) {
        GamesClient gamesClient = GetGamesClient();
        if (gamesClient.isConnected()) {
            if (percentage == 100.0f) {
                gamesClient.unlockAchievement(achievementID);
            } else {
                int percentInteger = (int) percentage;
                gamesClient.incrementAchievement(achievementID, percentInteger);
            }
        }
    }

    void Play_SubmitAchievements() {
    }

    void Play_ResetAchievements() {
        GamesClient gamesClient = GetGamesClient();
        if (gamesClient.isConnected()) {
            Log.w("ResetAchievements", "Request to reset achievements.");
            String accountName = gamesClient.getCurrentAccountName();
            String scopes = SexyAppFrameworkActivity.instance().getScopes();
            new ResetAchievementsTask(SexyAppFrameworkActivity.instance(), accountName, scopes).execute((Void) null);
        }
    }

    void Play_FetchAchievements() {
    }

    void Play_ShowAchievementView() {
        GamesClient gamesClient = GetGamesClient();
        if (gamesClient.isConnected()) {
            SexyAppFrameworkActivity.instance().startActivityForResult(gamesClient.getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
        }
    }
}
