package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.facebook.Request;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class by implements Parcelable.Creator<bx> {
    static void a(bx bxVar, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        Set<Integer> setBH = bxVar.bH();
        if (setBH.contains(1)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, bxVar.i());
        }
        if (setBH.contains(2)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, (Parcelable) bxVar.bI(), i, true);
        }
        if (setBH.contains(3)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, bxVar.getAdditionalName(), true);
        }
        if (setBH.contains(4)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, (Parcelable) bxVar.bJ(), i, true);
        }
        if (setBH.contains(5)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, bxVar.getAddressCountry(), true);
        }
        if (setBH.contains(6)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, bxVar.getAddressLocality(), true);
        }
        if (setBH.contains(7)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, bxVar.getAddressRegion(), true);
        }
        if (setBH.contains(8)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 8, bxVar.bK(), true);
        }
        if (setBH.contains(9)) {
            com.google.android.gms.common.internal.safeparcel.b.c(parcel, 9, bxVar.getAttendeeCount());
        }
        if (setBH.contains(10)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 10, bxVar.bL(), true);
        }
        if (setBH.contains(11)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 11, (Parcelable) bxVar.bM(), i, true);
        }
        if (setBH.contains(12)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 12, bxVar.bN(), true);
        }
        if (setBH.contains(13)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 13, bxVar.getBestRating(), true);
        }
        if (setBH.contains(14)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 14, bxVar.getBirthDate(), true);
        }
        if (setBH.contains(15)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 15, (Parcelable) bxVar.bO(), i, true);
        }
        if (setBH.contains(17)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 17, bxVar.getContentSize(), true);
        }
        if (setBH.contains(16)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 16, bxVar.getCaption(), true);
        }
        if (setBH.contains(19)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 19, bxVar.bP(), true);
        }
        if (setBH.contains(18)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 18, bxVar.getContentUrl(), true);
        }
        if (setBH.contains(21)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 21, bxVar.getDateModified(), true);
        }
        if (setBH.contains(20)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 20, bxVar.getDateCreated(), true);
        }
        if (setBH.contains(23)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 23, bxVar.getDescription(), true);
        }
        if (setBH.contains(22)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 22, bxVar.getDatePublished(), true);
        }
        if (setBH.contains(25)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 25, bxVar.getEmbedUrl(), true);
        }
        if (setBH.contains(24)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 24, bxVar.getDuration(), true);
        }
        if (setBH.contains(27)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 27, bxVar.getFamilyName(), true);
        }
        if (setBH.contains(26)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 26, bxVar.getEndDate(), true);
        }
        if (setBH.contains(29)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 29, (Parcelable) bxVar.bQ(), i, true);
        }
        if (setBH.contains(28)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 28, bxVar.getGender(), true);
        }
        if (setBH.contains(31)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 31, bxVar.getHeight(), true);
        }
        if (setBH.contains(30)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 30, bxVar.getGivenName(), true);
        }
        if (setBH.contains(34)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 34, (Parcelable) bxVar.bR(), i, true);
        }
        if (setBH.contains(32)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 32, bxVar.getId(), true);
        }
        if (setBH.contains(33)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 33, bxVar.getImage(), true);
        }
        if (setBH.contains(38)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 38, bxVar.getLongitude());
        }
        if (setBH.contains(39)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 39, bxVar.getName(), true);
        }
        if (setBH.contains(36)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 36, bxVar.getLatitude());
        }
        if (setBH.contains(37)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 37, (Parcelable) bxVar.bS(), i, true);
        }
        if (setBH.contains(42)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 42, bxVar.getPlayerType(), true);
        }
        if (setBH.contains(43)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 43, bxVar.getPostOfficeBoxNumber(), true);
        }
        if (setBH.contains(40)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 40, (Parcelable) bxVar.bT(), i, true);
        }
        if (setBH.contains(41)) {
            com.google.android.gms.common.internal.safeparcel.b.b(parcel, 41, bxVar.bU(), true);
        }
        if (setBH.contains(46)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 46, (Parcelable) bxVar.bV(), i, true);
        }
        if (setBH.contains(47)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 47, bxVar.getStartDate(), true);
        }
        if (setBH.contains(44)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 44, bxVar.getPostalCode(), true);
        }
        if (setBH.contains(45)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 45, bxVar.getRatingValue(), true);
        }
        if (setBH.contains(51)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 51, bxVar.getThumbnailUrl(), true);
        }
        if (setBH.contains(50)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 50, (Parcelable) bxVar.bW(), i, true);
        }
        if (setBH.contains(49)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 49, bxVar.getText(), true);
        }
        if (setBH.contains(48)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 48, bxVar.getStreetAddress(), true);
        }
        if (setBH.contains(55)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 55, bxVar.getWidth(), true);
        }
        if (setBH.contains(54)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 54, bxVar.getUrl(), true);
        }
        if (setBH.contains(53)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 53, bxVar.getType(), true);
        }
        if (setBH.contains(52)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 52, bxVar.getTickerSymbol(), true);
        }
        if (setBH.contains(56)) {
            com.google.android.gms.common.internal.safeparcel.b.a(parcel, 56, bxVar.getWorstRating(), true);
        }
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: W, reason: merged with bridge method [inline-methods] */
    public bx[] newArray(int i) {
        return new bx[i];
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: w, reason: merged with bridge method [inline-methods] */
    public bx createFromParcel(Parcel parcel) {
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        HashSet hashSet = new HashSet();
        int iF = 0;
        bx bxVar = null;
        ArrayList<String> arrayListX = null;
        bx bxVar2 = null;
        String strL = null;
        String strL2 = null;
        String strL3 = null;
        ArrayList arrayListC = null;
        int iF2 = 0;
        ArrayList arrayListC2 = null;
        bx bxVar3 = null;
        ArrayList arrayListC3 = null;
        String strL4 = null;
        String strL5 = null;
        bx bxVar4 = null;
        String strL6 = null;
        String strL7 = null;
        String strL8 = null;
        ArrayList arrayListC4 = null;
        String strL9 = null;
        String strL10 = null;
        String strL11 = null;
        String strL12 = null;
        String strL13 = null;
        String strL14 = null;
        String strL15 = null;
        String strL16 = null;
        String strL17 = null;
        bx bxVar5 = null;
        String strL18 = null;
        String strL19 = null;
        String strL20 = null;
        String strL21 = null;
        bx bxVar6 = null;
        double dJ = 0.0d;
        bx bxVar7 = null;
        double dJ2 = 0.0d;
        String strL22 = null;
        bx bxVar8 = null;
        ArrayList arrayListC5 = null;
        String strL23 = null;
        String strL24 = null;
        String strL25 = null;
        String strL26 = null;
        bx bxVar9 = null;
        String strL27 = null;
        String strL28 = null;
        String strL29 = null;
        bx bxVar10 = null;
        String strL30 = null;
        String strL31 = null;
        String strL32 = null;
        String strL33 = null;
        String strL34 = null;
        String strL35 = null;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(1);
                    break;
                case 2:
                    bx bxVar11 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(2);
                    bxVar = bxVar11;
                    break;
                case 3:
                    arrayListX = com.google.android.gms.common.internal.safeparcel.a.x(parcel, iB);
                    hashSet.add(3);
                    break;
                case 4:
                    bx bxVar12 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(4);
                    bxVar2 = bxVar12;
                    break;
                case 5:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(5);
                    break;
                case 6:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(6);
                    break;
                case 7:
                    strL3 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(7);
                    break;
                case 8:
                    arrayListC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, bx.CREATOR);
                    hashSet.add(8);
                    break;
                case 9:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    hashSet.add(9);
                    break;
                case 10:
                    arrayListC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, bx.CREATOR);
                    hashSet.add(10);
                    break;
                case 11:
                    bx bxVar13 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(11);
                    bxVar3 = bxVar13;
                    break;
                case 12:
                    arrayListC3 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, bx.CREATOR);
                    hashSet.add(12);
                    break;
                case 13:
                    strL4 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(13);
                    break;
                case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE /* 14 */:
                    strL5 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(14);
                    break;
                case IDownloaderClient.STATE_FAILED_UNLICENSED /* 15 */:
                    bx bxVar14 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(15);
                    bxVar4 = bxVar14;
                    break;
                case 16:
                    strL6 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(16);
                    break;
                case IDownloaderClient.STATE_FAILED_SDCARD_FULL /* 17 */:
                    strL7 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(17);
                    break;
                case IDownloaderClient.STATE_FAILED_CANCELED /* 18 */:
                    strL8 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(18);
                    break;
                case 19:
                    arrayListC4 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, bx.CREATOR);
                    hashSet.add(19);
                    break;
                case 20:
                    strL9 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(20);
                    break;
                case 21:
                    strL10 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(21);
                    break;
                case 22:
                    strL11 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(22);
                    break;
                case 23:
                    strL12 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(23);
                    break;
                case 24:
                    strL13 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(24);
                    break;
                case 25:
                    strL14 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(25);
                    break;
                case 26:
                    strL15 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(26);
                    break;
                case 27:
                    strL16 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(27);
                    break;
                case 28:
                    strL17 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(28);
                    break;
                case 29:
                    bx bxVar15 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(29);
                    bxVar5 = bxVar15;
                    break;
                case 30:
                    strL18 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(30);
                    break;
                case 31:
                    strL19 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(31);
                    break;
                case AccessibilityNodeInfoCompat.ACTION_LONG_CLICK /* 32 */:
                    strL20 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(32);
                    break;
                case 33:
                    strL21 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(33);
                    break;
                case 34:
                    bx bxVar16 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(34);
                    bxVar6 = bxVar16;
                    break;
                case 35:
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
                case 36:
                    dJ = com.google.android.gms.common.internal.safeparcel.a.j(parcel, iB);
                    hashSet.add(36);
                    break;
                case 37:
                    bx bxVar17 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(37);
                    bxVar7 = bxVar17;
                    break;
                case 38:
                    dJ2 = com.google.android.gms.common.internal.safeparcel.a.j(parcel, iB);
                    hashSet.add(38);
                    break;
                case 39:
                    strL22 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(39);
                    break;
                case 40:
                    bx bxVar18 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(40);
                    bxVar8 = bxVar18;
                    break;
                case 41:
                    arrayListC5 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, bx.CREATOR);
                    hashSet.add(41);
                    break;
                case 42:
                    strL23 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(42);
                    break;
                case 43:
                    strL24 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(43);
                    break;
                case 44:
                    strL25 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(44);
                    break;
                case 45:
                    strL26 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(45);
                    break;
                case 46:
                    bx bxVar19 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(46);
                    bxVar9 = bxVar19;
                    break;
                case 47:
                    strL27 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(47);
                    break;
                case 48:
                    strL28 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(48);
                    break;
                case 49:
                    strL29 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(49);
                    break;
                case Request.MAXIMUM_BATCH_SIZE /* 50 */:
                    bx bxVar20 = (bx) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, bx.CREATOR);
                    hashSet.add(50);
                    bxVar10 = bxVar20;
                    break;
                case 51:
                    strL30 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(51);
                    break;
                case 52:
                    strL31 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(52);
                    break;
                case 53:
                    strL32 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(53);
                    break;
                case 54:
                    strL33 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(54);
                    break;
                case 55:
                    strL34 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(55);
                    break;
                case 56:
                    strL35 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    hashSet.add(56);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new bx(hashSet, iF, bxVar, arrayListX, bxVar2, strL, strL2, strL3, arrayListC, iF2, arrayListC2, bxVar3, arrayListC3, strL4, strL5, bxVar4, strL6, strL7, strL8, arrayListC4, strL9, strL10, strL11, strL12, strL13, strL14, strL15, strL16, strL17, bxVar5, strL18, strL19, strL20, strL21, bxVar6, dJ, bxVar7, dJ2, strL22, bxVar8, arrayListC5, strL23, strL24, strL25, strL26, bxVar9, strL27, strL28, strL29, bxVar10, strL30, strL31, strL32, strL33, strL34, strL35);
    }
}
