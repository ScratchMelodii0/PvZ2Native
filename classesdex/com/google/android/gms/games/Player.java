package com.google.android.gms.games;

import android.database.CharArrayBuffer;
import android.net.Uri;
import android.os.Parcelable;
import com.google.android.gms.common.data.Freezable;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface Player extends Parcelable, Freezable<Player> {
    String getDisplayName();

    void getDisplayName(CharArrayBuffer charArrayBuffer);

    Uri getHiResImageUri();

    Uri getIconImageUri();

    String getPlayerId();

    long getRetrievedTimestamp();

    boolean hasHiResImage();

    boolean hasIconImage();
}
