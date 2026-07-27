package com.google.android.gms.internal;

import android.net.Uri;
import android.widget.ImageView;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class g extends ImageView {
    private Uri bk;
    private int bl;

    public void a(Uri uri) {
        this.bk = uri;
    }

    public void k(int i) {
        this.bl = i;
    }

    public int t() {
        return this.bl;
    }
}
