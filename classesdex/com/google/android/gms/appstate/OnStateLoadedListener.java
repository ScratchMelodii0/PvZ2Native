package com.google.android.gms.appstate;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface OnStateLoadedListener {
    void onStateConflict(int i, String str, byte[] bArr, byte[] bArr2);

    void onStateLoaded(int i, int i2, byte[] bArr);
}
