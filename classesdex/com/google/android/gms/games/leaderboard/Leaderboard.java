package com.google.android.gms.games.leaderboard;

import android.database.CharArrayBuffer;
import android.net.Uri;
import java.util.ArrayList;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface Leaderboard {
    public static final int SCORE_ORDER_LARGER_IS_BETTER = 1;
    public static final int SCORE_ORDER_SMALLER_IS_BETTER = 0;

    String getDisplayName();

    void getDisplayName(CharArrayBuffer charArrayBuffer);

    Uri getIconImageUri();

    String getLeaderboardId();

    int getScoreOrder();

    ArrayList<LeaderboardVariant> getVariants();
}
