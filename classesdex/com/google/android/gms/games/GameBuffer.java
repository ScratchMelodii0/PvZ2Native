package com.google.android.gms.games;

import com.google.android.gms.common.data.DataBuffer;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class GameBuffer extends DataBuffer<Game> {
    public GameBuffer(com.google.android.gms.common.data.d dataHolder) {
        super(dataHolder);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.android.gms.common.data.DataBuffer
    public Game get(int position) {
        return new b(this.S, position);
    }
}
