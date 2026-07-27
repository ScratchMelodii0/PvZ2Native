package com.google.android.gms.internal;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import java.util.ArrayList;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class cp implements Parcelable.Creator<co> {
    static void a(co coVar, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 1, coVar.getId(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1000, coVar.i());
        com.google.android.gms.common.internal.safeparcel.b.b(parcel, 2, coVar.cB(), false);
        com.google.android.gms.common.internal.safeparcel.b.b(parcel, 3, coVar.cC(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, (Parcelable) coVar.cD(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, coVar.cE(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, coVar.cF(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, coVar.cG(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, coVar.cH(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 9, coVar.cI(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 10, coVar.cJ());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: I, reason: merged with bridge method [inline-methods] */
    public co createFromParcel(Parcel parcel) {
        int iF = 0;
        Bundle bundleN = null;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        Bundle bundleN2 = null;
        String strL = null;
        String strL2 = null;
        String strL3 = null;
        Uri uri = null;
        ArrayList arrayListC = null;
        ArrayList arrayListC2 = null;
        String strL4 = null;
        int iF2 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    strL4 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 2:
                    arrayListC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, x.CREATOR);
                    break;
                case 3:
                    arrayListC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, Uri.CREATOR);
                    break;
                case 4:
                    uri = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 5:
                    strL3 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 6:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 7:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 8:
                    bundleN2 = com.google.android.gms.common.internal.safeparcel.a.n(parcel, iB);
                    break;
                case 9:
                    bundleN = com.google.android.gms.common.internal.safeparcel.a.n(parcel, iB);
                    break;
                case 10:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
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
        return new co(iF2, strL4, arrayListC2, arrayListC, uri, strL3, strL2, strL, bundleN2, bundleN, iF);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: ai, reason: merged with bridge method [inline-methods] */
    public co[] newArray(int i) {
        return new co[i];
    }
}
