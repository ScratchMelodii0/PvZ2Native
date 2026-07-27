package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import java.util.ArrayList;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class PolygonOptionsCreator implements Parcelable.Creator<PolygonOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void a(PolygonOptions polygonOptions, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, polygonOptions.i());
        com.google.android.gms.common.internal.safeparcel.b.b(parcel, 2, polygonOptions.getPoints(), false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 3, polygonOptions.br(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, polygonOptions.getStrokeWidth());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 5, polygonOptions.getStrokeColor());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 6, polygonOptions.getFillColor());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, polygonOptions.getZIndex());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, polygonOptions.isVisible());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 9, polygonOptions.isGeodesic());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public PolygonOptions createFromParcel(Parcel parcel) {
        float fI = BitmapDescriptorFactory.HUE_RED;
        boolean zC = false;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        ArrayList arrayListC = null;
        ArrayList arrayList = new ArrayList();
        boolean zC2 = false;
        int iF = 0;
        int iF2 = 0;
        float fI2 = 0.0f;
        int iF3 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF3 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 2:
                    arrayListC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, LatLng.CREATOR);
                    break;
                case 3:
                    com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, arrayList, getClass().getClassLoader());
                    break;
                case 4:
                    fI2 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 5:
                    iF2 = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 6:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 7:
                    fI = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 8:
                    zC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 9:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new PolygonOptions(iF3, arrayListC, arrayList, fI2, iF2, iF, fI, zC2, zC);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public PolygonOptions[] newArray(int size) {
        return new PolygonOptions[size];
    }
}
