package com.google.android.gms.appstate;

import com.google.android.gms.common.data.Freezable;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface AppState extends Freezable<AppState> {
    byte[] getConflictData();

    String getConflictVersion();

    int getKey();

    byte[] getLocalData();

    String getLocalVersion();

    boolean hasConflict();
}
