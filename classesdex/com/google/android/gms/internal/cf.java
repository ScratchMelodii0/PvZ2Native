package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.internal.cc;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class cf implements Parcelable.Creator<cc.b> {
    static void a(cc.b bVar, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        Set<Integer> setBH = bVar.bH();
        if (setBH.contains(1)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, bVar.i());
        }
        if (setBH.contains(2)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, (Parcelable) bVar.cl(), i, true);
        }
        if (setBH.contains(3)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, (Parcelable) bVar.cm(), i, true);
        }
        if (setBH.contains(4)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 4, bVar.getLayout());
        }
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: A, reason: merged with bridge method [inline-methods] */
    public cc.b createFromParcel(Parcel parcel) {
        cc.b.C0017b c0017b = null;
        int iF = 0;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        HashSet hashSet = new HashSet();
        cc.b.a aVar = null;
        int iF2 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(1);
                    break;
                case 2:
                    cc.b.a aVar2 = (cc.b.a) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, cc.b.a.CREATOR);
                    hashSet.add(2);
                    aVar = aVar2;
                    break;
                case 3:
                    cc.b.C0017b c0017b2 = (cc.b.C0017b) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, cc.b.C0017b.CREATOR);
                    hashSet.add(3);
                    c0017b = c0017b2;
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
        return new cc.b(hashSet, iF2, aVar, c0017b, iF);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: aa, reason: merged with bridge method [inline-methods] */
    public cc.b[] newArray(int i) {
        return new cc.b[i];
    }
}
