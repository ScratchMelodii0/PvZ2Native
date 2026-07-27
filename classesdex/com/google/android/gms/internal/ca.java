package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class ca implements Parcelable.Creator<bz> {
    static void a(bz bzVar, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        Set<Integer> setBH = bzVar.bH();
        if (setBH.contains(1)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, bzVar.i());
        }
        if (setBH.contains(2)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, bzVar.getId(), true);
        }
        if (setBH.contains(4)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, (Parcelable) bzVar.bY(), i, true);
        }
        if (setBH.contains(5)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, bzVar.getStartDate(), true);
        }
        if (setBH.contains(6)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, (Parcelable) bzVar.bZ(), i, true);
        }
        if (setBH.contains(7)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, bzVar.getType(), true);
        }
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: X, reason: merged with bridge method [inline-methods] */
    public bz[] newArray(int i) {
        return new bz[i];
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: x, reason: merged with bridge method [inline-methods] */
    public bz createFromParcel(Parcel parcel) {
        String strL = null;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        HashSet hashSet = new HashSet();
        int iF = 0;
        bx bxVar = null;
        String strL2 = null;
        bx bxVar2 = null;
        String strL3 = null;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(1);
                    break;
                case 2:
                    strL3 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(2);
                    break;
                case 3:
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
                case 4:
                    bx bxVar3 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(4);
                    bxVar2 = bxVar3;
                    break;
                case 5:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(5);
                    break;
                case 6:
                    bx bxVar4 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(6);
                    bxVar = bxVar4;
                    break;
                case 7:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(7);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new bz(hashSet, iF, strL3, bxVar2, strL2, bxVar, strL);
    }
}
