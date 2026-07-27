package com.google.android.gms.games.multiplayer;

import android.os.Parcelable;
import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.games.Game;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface Invitation extends Parcelable, Freezable<Invitation>, Participatable {
    int aL();

    long getCreationTimestamp();

    Game getGame();

    String getInvitationId();

    Participant getInviter();

    int getVariant();
}
