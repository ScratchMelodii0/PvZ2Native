package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.internal.cc;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class ch implements Parcelable.Creator<cc.b.C0017b> {
    static void a(cc.b.C0017b c0017b, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        Set<Integer> setBH = c0017b.bH();
        if (setBH.contains(1)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, c0017b.i());
        }
        if (setBH.contains(2)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 2, c0017b.getHeight());
        }
        if (setBH.contains(3)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, c0017b.getUrl(), true);
        }
        if (setBH.contains(4)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 4, c0017b.getWidth());
        }
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: C, reason: merged with bridge method [inline-methods] */
    public cc.b.C0017b createFromParcel(Parcel parcel) {
        int iF = 0;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        HashSet hashSet = new HashSet();
        String strL = null;
        int iF2 = 0;
        int iF3 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF3 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(1);
                    break;
                case 2:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(2);
                    break;
                case 3:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(3);
                    break;
                case 4:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(4);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new cc.b.C0017b(hashSet, iF3, iF2, strL, iF);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: ac, reason: merged with bridge method [inline-methods] */
    public cc.b.C0017b[] newArray(int i) {
        return new cc.b.C0017b[i];
    }
}
