package com.google.android.gms.maps.model;

import android.os.Parcel;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class i {
    static void a(Tile tile, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, tile.i());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 2, tile.width);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 3, tile.height);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, tile.data, false);
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }
}
