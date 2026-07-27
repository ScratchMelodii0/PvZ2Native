package com.google.android.gms.common.data;

import android.database.CursorWindow;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class e implements Parcelable.Creator<d> {
    static void a(d dVar, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 1, dVar.j(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1000, dVar.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, (Parcelable[]) dVar.k(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 3, dVar.getStatusCode());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, dVar.l(), false);
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public d createFromParcel(Parcel parcel) {
        int iF = 0;
        Bundle bundleN = null;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        CursorWindow[] cursorWindowArr = null;
        String[] strArrW = null;
        int iF2 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    strArrW = com.google.android.gms.common.internal.safeparcel.a.w(parcel, iB);
                    break;
                case 2:
                    cursorWindowArr = (CursorWindow[]) com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB, CursorWindow.CREATOR);
                    break;
                case 3:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 4:
                    bundleN = com.google.android.gms.common.internal.safeparcel.a.n(parcel, iB);
                    break;
                case 1000:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        d dVar = new d(iF2, strArrW, cursorWindowArr, iF, bundleN);
        dVar.h();
        return dVar;
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: g, reason: merged with bridge method [inline-methods] */
    public d[] newArray(int i) {
        return new d[i];
    }
}
