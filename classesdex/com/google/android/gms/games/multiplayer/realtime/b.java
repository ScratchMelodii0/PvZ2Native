package com.google.android.gms.games.multiplayer.realtime;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.games.multiplayer.ParticipantEntity;
import java.util.ArrayList;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class b implements Parcelable.Creator<RoomEntity> {
    static void a(RoomEntity roomEntity, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 1, roomEntity.getRoomId(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1000, roomEntity.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, roomEntity.getCreatorId(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, roomEntity.getCreationTimestamp());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 4, roomEntity.getStatus());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, roomEntity.getDescription(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 6, roomEntity.getVariant());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, roomEntity.getAutoMatchCriteria(), false);
        com.google.android.gms.common.internal.safeparcel.b.b(parcel, 8, roomEntity.getParticipants(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 9, roomEntity.getAutoMatchWaitEstimateSeconds());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: K, reason: merged with bridge method [inline-methods] */
    public RoomEntity[] newArray(int i) {
        return new RoomEntity[i];
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: s */
    public RoomEntity createFromParcel(Parcel parcel) {
        int iF = 0;
        ArrayList arrayListC = null;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        long jG = 0;
        Bundle bundleN = null;
        int iF2 = 0;
        String strL = null;
        int iF3 = 0;
        String strL2 = null;
        String strL3 = null;
        int iF4 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    strL3 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 2:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 3:
                    jG = com.google.android.gms.common.internal.safeparcel.a.g(parcel, iB);
                    break;
                case 4:
                    iF3 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 5:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 6:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 7:
                    bundleN = com.google.android.gms.common.internal.safeparcel.a.n(parcel, iB);
                    break;
                case 8:
                    arrayListC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, ParticipantEntity.CREATOR);
                    break;
                case 9:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 1000:
                    iF4 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new RoomEntity(iF4, strL3, strL2, jG, iF3, strL, iF2, bundleN, arrayListC, iF);
    }
}
