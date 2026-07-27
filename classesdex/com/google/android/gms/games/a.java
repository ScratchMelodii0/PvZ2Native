package com.google.android.gms.games;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.vending.expansion.downloader.IDownloaderClient;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class a implements Parcelable.Creator<GameEntity> {
    static void a(GameEntity gameEntity, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 1, gameEntity.getApplicationId(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, gameEntity.getDisplayName(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, gameEntity.getPrimaryCategory(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, gameEntity.getSecondaryCategory(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, gameEntity.getDescription(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, gameEntity.getDeveloperName(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, (Parcelable) gameEntity.getIconImageUri(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, (Parcelable) gameEntity.getHiResImageUri(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 9, (Parcelable) gameEntity.getFeaturedImageUri(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 10, gameEntity.isPlayEnabledGame());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 11, gameEntity.isInstanceInstalled());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 12, gameEntity.getInstancePackageName(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 13, gameEntity.getGameplayAclStatus());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 14, gameEntity.getAchievementTotalCount());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 15, gameEntity.getLeaderboardCount());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1000, gameEntity.i());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: n, reason: merged with bridge method [inline-methods] */
    public GameEntity createFromParcel(Parcel parcel) {
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        int iF = 0;
        String strL = null;
        String strL2 = null;
        String strL3 = null;
        String strL4 = null;
        String strL5 = null;
        String strL6 = null;
        Uri uri = null;
        Uri uri2 = null;
        Uri uri3 = null;
        boolean zC = false;
        boolean zC2 = false;
        String strL7 = null;
        int iF2 = 0;
        int iF3 = 0;
        int iF4 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 2:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 3:
                    strL3 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 4:
                    strL4 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 5:
                    strL5 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 6:
                    strL6 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 7:
                    uri = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 8:
                    uri2 = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 9:
                    uri3 = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 10:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 11:
                    zC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 12:
                    strL7 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 13:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE /* 14 */:
                    iF3 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case IDownloaderClient.STATE_FAILED_UNLICENSED /* 15 */:
                    iF4 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 1000:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new GameEntity(iF, strL, strL2, strL3, strL4, strL5, strL6, uri, uri2, uri3, zC, zC2, strL7, iF2, iF3, iF4);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: z, reason: merged with bridge method [inline-methods] */
    public GameEntity[] newArray(int i) {
        return new GameEntity[i];
    }
}
