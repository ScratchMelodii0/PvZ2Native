package com.google.android.gms.games.multiplayer;

import com.google.android.gms.common.data.DataBuffer;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class ParticipantBuffer extends DataBuffer<Participant> {
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.android.gms.common.data.DataBuffer
    public Participant get(int position) {
        return new d(this.S, position);
    }
}
