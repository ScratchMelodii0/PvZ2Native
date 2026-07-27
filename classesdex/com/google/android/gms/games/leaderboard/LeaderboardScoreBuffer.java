package com.google.android.gms.games.leaderboard;

import com.google.android.gms.common.data.DataBuffer;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class LeaderboardScoreBuffer extends DataBuffer<LeaderboardScore> {
    private final b ep;

    public LeaderboardScoreBuffer(com.google.android.gms.common.data.d dataHolder) {
        super(dataHolder);
        this.ep = new b(dataHolder.l());
    }

    public b aF() {
        return this.ep;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.android.gms.common.data.DataBuffer
    public LeaderboardScore get(int position) {
        return new d(this.S, position);
    }
}
