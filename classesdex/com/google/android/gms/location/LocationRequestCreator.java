package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class LocationRequestCreator implements Parcelable.Creator<LocationRequest> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void a(LocationRequest locationRequest, Parcel parcel, int i) {
        int iD = b.d(parcel);
        b.c(parcel, 1, locationRequest.mPriority);
        b.c(parcel, 1000, locationRequest.i());
        b.a(parcel, 2, locationRequest.fB);
        b.a(parcel, 3, locationRequest.fC);
        b.a(parcel, 4, locationRequest.fD);
        b.a(parcel, 5, locationRequest.fw);
        b.c(parcel, 6, locationRequest.fE);
        b.a(parcel, 7, locationRequest.fF);
        b.C(parcel, iD);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public LocationRequest createFromParcel(Parcel parcel) {
        boolean zC = false;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        int iF = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
        long jG = 3600000;
        long jG2 = 600000;
        long jG3 = Long.MAX_VALUE;
        int iF2 = Integer.MAX_VALUE;
        float fI = BitmapDescriptorFactory.HUE_RED;
        int iF3 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 2:
                    jG = com.google.android.gms.common.internal.safeparcel.a.g(parcel, iB);
                    break;
                case 3:
                    jG2 = com.google.android.gms.common.internal.safeparcel.a.g(parcel, iB);
                    break;
                case 4:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 5:
                    jG3 = com.google.android.gms.common.internal.safeparcel.a.g(parcel, iB);
                    break;
                case 6:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 7:
                    fI = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
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
        return new LocationRequest(iF3, iF, jG, jG2, zC, jG3, iF2, fI);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public LocationRequest[] newArray(int size) {
        return new LocationRequest[size];
    }
}
