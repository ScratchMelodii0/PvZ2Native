package com.google.android.gms.games;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class c implements Parcelable.Creator<PlayerEntity> {
    static void a(PlayerEntity playerEntity, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 1, playerEntity.getPlayerId(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1000, playerEntity.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, playerEntity.getDisplayName(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, (Parcelable) playerEntity.getIconImageUri(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, (Parcelable) playerEntity.getHiResImageUri(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, playerEntity.getRetrievedTimestamp());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: A, reason: merged with bridge method [inline-methods] */
    public PlayerEntity[] newArray(int i) {
        return new PlayerEntity[i];
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: o, reason: merged with bridge method [inline-methods] */
    public PlayerEntity createFromParcel(Parcel parcel) {
        Uri uri = null;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        int iF = 0;
        long jG = 0;
        Uri uri2 = null;
        String strL = null;
        String strL2 = null;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 2:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 3:
                    uri2 = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 4:
                    uri = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 5:
                    jG = com.google.android.gms.common.internal.safeparcel.a.g(parcel, iB);
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
        return new PlayerEntity(iF, strL2, strL, uri2, uri, jG);
    }
}
