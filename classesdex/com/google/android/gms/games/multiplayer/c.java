package com.google.android.gms.games.multiplayer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.games.PlayerEntity;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class c implements Parcelable.Creator<ParticipantEntity> {
    static void a(ParticipantEntity participantEntity, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 1, participantEntity.getParticipantId(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1000, participantEntity.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, participantEntity.getDisplayName(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, (Parcelable) participantEntity.getIconImageUri(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, (Parcelable) participantEntity.getHiResImageUri(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 5, participantEntity.getStatus());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, participantEntity.aM(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, participantEntity.isConnectedToRoom());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, (Parcelable) participantEntity.getPlayer(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 9, participantEntity.aN());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: I, reason: merged with bridge method [inline-methods] */
    public ParticipantEntity[] newArray(int i) {
        return new ParticipantEntity[i];
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: q */
    public ParticipantEntity createFromParcel(Parcel parcel) {
        int iF = 0;
        PlayerEntity playerEntity = null;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        boolean zC = false;
        String strL = null;
        int iF2 = 0;
        Uri uri = null;
        Uri uri2 = null;
        String strL2 = null;
        String strL3 = null;
        int iF3 = 0;
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
                    uri2 = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 4:
                    uri = (Uri) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, Uri.CREATOR);
                    break;
                case 5:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 6:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 7:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 8:
                    playerEntity = (PlayerEntity) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, PlayerEntity.CREATOR);
                    break;
                case 9:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 1000:
                    iF3 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new ParticipantEntity(iF3, strL3, strL2, uri2, uri, iF2, strL, zC, playerEntity, iF);
    }
}
