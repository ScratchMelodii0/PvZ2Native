package com.popcap.SexyAppFramework.GooglePlay;

import com.google.android.gms.games.GamesClient;
import com.popcap.SexyAppFramework.SexyAppFrameworkActivity;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GooglePlayLeaderboard {
    static int REQUEST_LEADERBOARDS = 67;
    static int REQUEST_ALL_LEADERBOARDS = 68;

    GamesClient GetGamesClient() {
        return SexyAppFrameworkActivity.instance().getGamesClient();
    }

    void Play_SubmitScoreToLeaderboard(int score, String leaderboard_id) {
        GamesClient gamesClient = GetGamesClient();
        if (gamesClient.isConnected()) {
            gamesClient.submitScore(leaderboard_id, score);
        }
    }

    void Play_ShowLeaderboardView(String leaderboard_id) {
        GamesClient gamesClient = GetGamesClient();
        if (gamesClient.isConnected()) {
            SexyAppFrameworkActivity.instance().startActivityForResult(GetGamesClient().getLeaderboardIntent(leaderboard_id), REQUEST_LEADERBOARDS);
        }
    }

    void Play_ShowLeaderboardViewAll() {
        GamesClient gamesClient = GetGamesClient();
        if (gamesClient.isConnected()) {
            SexyAppFrameworkActivity.instance().startActivityForResult(GetGamesClient().getAllLeaderboardsIntent(), REQUEST_ALL_LEADERBOARDS);
        }
    }
}
