package com.google.android.gms.games.multiplayer;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.games.GameEntity;
import java.util.ArrayList;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class a implements Parcelable.Creator<InvitationEntity> {
    static void a(InvitationEntity invitationEntity, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 1, (Parcelable) invitationEntity.getGame(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1000, invitationEntity.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, invitationEntity.getInvitationId(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, invitationEntity.getCreationTimestamp());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 4, invitationEntity.aL());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, (Parcelable) invitationEntity.getInviter(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.b(parcel, 6, invitationEntity.getParticipants(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 7, invitationEntity.getVariant());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: H, reason: merged with bridge method [inline-methods] */
    public InvitationEntity[] newArray(int i) {
        return new InvitationEntity[i];
    }

    @Override // android.os.Parcelable.Creator
    /* JADX INFO: renamed from: p, reason: merged with bridge method [inline-methods] */
    public InvitationEntity createFromParcel(Parcel parcel) {
        int iF = 0;
        ArrayList arrayListC = null;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        long jG = 0;
        ParticipantEntity participantEntity = null;
        int iF2 = 0;
        String strL = null;
        GameEntity gameEntity = null;
        int iF3 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    gameEntity = (GameEntity) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, GameEntity.CREATOR);
                    break;
                case 2:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 3:
                    jG = com.google.android.gms.common.internal.safeparcel.a.g(parcel, iB);
                    break;
                case 4:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 5:
                    participantEntity = (ParticipantEntity) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, ParticipantEntity.CREATOR);
                    break;
                case 6:
                    arrayListC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, ParticipantEntity.CREATOR);
                    break;
                case 7:
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
        return new InvitationEntity(iF3, gameEntity, strL, jG, iF2, participantEntity, arrayListC, iF);
    }
}
