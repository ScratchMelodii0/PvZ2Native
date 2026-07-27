package com.google.android.gms.maps.model;

import android.os.Parcel;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class e {
    static void a(LatLng latLng, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, latLng.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, latLng.latitude);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, latLng.longitude);
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }
}
