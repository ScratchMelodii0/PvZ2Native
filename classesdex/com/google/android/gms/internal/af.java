package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.internal.ae;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class af implements Parcelable.Creator<ae.a> {
    static void a(ae.a aVar, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, aVar.i());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 2, aVar.R());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, aVar.X());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 4, aVar.S());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, aVar.Y());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, aVar.Z(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 7, aVar.aa());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, aVar.ac(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 9, (Parcelable) aVar.ae(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: i, reason: merged with bridge method [inline-methods] */
    public ae.a createFromParcel(Parcel parcel) {
        z zVar = null;
        int iF = 0;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        String strL = null;
        String strL2 = null;
        boolean zC = false;
        int iF2 = 0;
        boolean zC2 = false;
        int iF3 = 0;
        int iF4 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF4 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 2:
                    iF3 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 3:
                    zC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 4:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 5:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 6:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 7:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 8:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 9:
                    zVar = (z) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, z.CREATOR);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new ae.a(iF4, iF3, zC2, iF2, zC, strL2, iF, strL, zVar);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: r, reason: merged with bridge method [inline-methods] */
    public ae.a[] newArray(int i) {
        return new ae.a[i];
    }
}
