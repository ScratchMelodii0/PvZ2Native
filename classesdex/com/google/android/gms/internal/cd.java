package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.internal.cc;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class cd implements Parcelable.Creator<cc> {
    static void a(cc ccVar, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        Set<Integer> setBH = ccVar.bH();
        if (setBH.contains(1)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, ccVar.i());
        }
        if (setBH.contains(2)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, ccVar.getAboutMe(), true);
        }
        if (setBH.contains(3)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, (Parcelable) ccVar.cc(), i, true);
        }
        if (setBH.contains(4)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, ccVar.getBirthday(), true);
        }
        if (setBH.contains(5)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, ccVar.getBraggingRights(), true);
        }
        if (setBH.contains(6)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 6, ccVar.getCircledByCount());
        }
        if (setBH.contains(7)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, (Parcelable) ccVar.cd(), i, true);
        }
        if (setBH.contains(8)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, ccVar.getCurrentLocation(), true);
        }
        if (setBH.contains(9)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 9, ccVar.getDisplayName(), true);
        }
        if (setBH.contains(12)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 12, ccVar.getGender());
        }
        if (setBH.contains(14)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 14, ccVar.getId(), true);
        }
        if (setBH.contains(15)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 15, (Parcelable) ccVar.ce(), i, true);
        }
        if (setBH.contains(16)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 16, ccVar.isPlusUser());
        }
        if (setBH.contains(19)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 19, (Parcelable) ccVar.cf(), i, true);
        }
        if (setBH.contains(18)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 18, ccVar.getLanguage(), true);
        }
        if (setBH.contains(21)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 21, ccVar.getObjectType());
        }
        if (setBH.contains(20)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 20, ccVar.getNickname(), true);
        }
        if (setBH.contains(23)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 23, ccVar.ch(), true);
        }
        if (setBH.contains(22)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 22, ccVar.cg(), true);
        }
        if (setBH.contains(25)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 25, ccVar.getRelationshipStatus());
        }
        if (setBH.contains(24)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 24, ccVar.getPlusOneCount());
        }
        if (setBH.contains(27)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 27, ccVar.getUrl(), true);
        }
        if (setBH.contains(26)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 26, ccVar.getTagline(), true);
        }
        if (setBH.contains(29)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 29, ccVar.isVerified());
        }
        if (setBH.contains(28)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 28, ccVar.ci(), true);
        }
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: Y, reason: merged with bridge method [inline-methods] */
    public cc[] newArray(int i) {
        return new cc[i];
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: y, reason: merged with bridge method [inline-methods] */
    public cc createFromParcel(Parcel parcel) {
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        HashSet hashSet = new HashSet();
        int iF = 0;
        String strL = null;
        cc.a aVar = null;
        String strL2 = null;
        String strL3 = null;
        int iF2 = 0;
        cc.b bVar = null;
        String strL4 = null;
        String strL5 = null;
        int iF3 = 0;
        String strL6 = null;
        cc.c cVar = null;
        boolean zC = false;
        String strL7 = null;
        cc.d dVar = null;
        String strL8 = null;
        int iF4 = 0;
        ArrayList arrayListC = null;
        ArrayList arrayListC2 = null;
        int iF5 = 0;
        int iF6 = 0;
        String strL9 = null;
        String strL10 = null;
        ArrayList arrayListC3 = null;
        boolean zC2 = false;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(1);
                    break;
                case 2:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(2);
                    break;
                case 3:
                    cc.a aVar2 = (cc.a) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, cc.a.CREATOR);
                    hashSet.add(3);
                    aVar = aVar2;
                    break;
                case 4:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(4);
                    break;
                case 5:
                    strL3 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(5);
                    break;
                case 6:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(6);
                    break;
                case 7:
                    cc.b bVar2 = (cc.b) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, cc.b.CREATOR);
                    hashSet.add(7);
                    bVar = bVar2;
                    break;
                case 8:
                    strL4 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(8);
                    break;
                case 9:
                    strL5 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(9);
                    break;
                case 10:
                case 11:
                case 13:
                case IDownloaderClient.STATE_FAILED_SDCARD_FULL /* 17 */:
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
                case 12:
                    iF3 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(12);
                    break;
                case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE /* 14 */:
                    strL6 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(14);
                    break;
                case IDownloaderClient.STATE_FAILED_UNLICENSED /* 15 */:
                    cc.c cVar2 = (cc.c) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, cc.c.CREATOR);
                    hashSet.add(15);
                    cVar = cVar2;
                    break;
                case 16:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    hashSet.add(16);
                    break;
                case IDownloaderClient.STATE_FAILED_CANCELED /* 18 */:
                    strL7 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(18);
                    break;
                case 19:
                    cc.d dVar2 = (cc.d) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, cc.d.CREATOR);
                    hashSet.add(19);
                    dVar = dVar2;
                    break;
                case 20:
                    strL8 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(20);
                    break;
                case 21:
                    iF4 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(21);
                    break;
                case 22:
                    arrayListC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, cc.f.CREATOR);
                    hashSet.add(22);
                    break;
                case 23:
                    arrayListC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, cc.g.CREATOR);
                    hashSet.add(23);
                    break;
                case 24:
                    iF5 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(24);
                    break;
                case 25:
                    iF6 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(25);
                    break;
                case 26:
                    strL9 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(26);
                    break;
                case 27:
                    strL10 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(27);
                    break;
                case 28:
                    arrayListC3 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, cc.h.CREATOR);
                    hashSet.add(28);
                    break;
                case 29:
                    zC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    hashSet.add(29);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new cc(hashSet, iF, strL, aVar, strL2, strL3, iF2, bVar, strL4, strL5, iF3, strL6, cVar, zC, strL7, dVar, strL8, iF4, arrayListC, arrayListC2, iF5, iF6, strL9, strL10, arrayListC3, zC2);
    }
}
