package com.google.android.gms.maps;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.maps.model.CameraPosition;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GoogleMapOptionsCreator implements Parcelable.Creator<GoogleMapOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void a(GoogleMapOptions googleMapOptions, Parcel parcel, int i) {
        int iD = b.d(parcel);
        b.c(parcel, 1, googleMapOptions.i());
        b.a(parcel, 2, googleMapOptions.aZ());
        b.a(parcel, 3, googleMapOptions.ba());
        b.c(parcel, 4, googleMapOptions.getMapType());
        b.a(parcel, 5, (Parcelable) googleMapOptions.getCamera(), i, false);
        b.a(parcel, 6, googleMapOptions.bb());
        b.a(parcel, 7, googleMapOptions.bc());
        b.a(parcel, 8, googleMapOptions.bd());
        b.a(parcel, 9, googleMapOptions.be());
        b.a(parcel, 10, googleMapOptions.bf());
        b.a(parcel, 11, googleMapOptions.bg());
        b.C(parcel, iD);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public GoogleMapOptions createFromParcel(Parcel parcel) {
        byte bD = 0;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        CameraPosition cameraPosition = null;
        byte bD2 = 0;
        byte bD3 = 0;
        byte bD4 = 0;
        byte bD5 = 0;
        byte bD6 = 0;
        int iF = 0;
        byte bD7 = 0;
        byte bD8 = 0;
        int iF2 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 2:
                    bD8 = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                case 3:
                    bD7 = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                case 4:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 5:
                    cameraPosition = (CameraPosition) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, CameraPosition.CREATOR);
                    break;
                case 6:
                    bD6 = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                case 7:
                    bD5 = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                case 8:
                    bD4 = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                case 9:
                    bD3 = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                case 10:
                    bD2 = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                case 11:
                    bD = com.google.android.gms.common.internal.safeparcel.a.d(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new GoogleMapOptions(iF2, bD8, bD7, iF, cameraPosition, bD6, bD5, bD4, bD3, bD2, bD);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public GoogleMapOptions[] newArray(int size) {
        return new GoogleMapOptions[size];
    }
}
