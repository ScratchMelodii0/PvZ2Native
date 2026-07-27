package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class MarkerOptionsCreator implements Parcelable.Creator<MarkerOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void a(MarkerOptions markerOptions, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, markerOptions.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, (Parcelable) markerOptions.getPosition(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, markerOptions.getTitle(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, markerOptions.getSnippet(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, markerOptions.bq(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, markerOptions.getAnchorU());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, markerOptions.getAnchorV());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, markerOptions.isDraggable());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 9, markerOptions.isVisible());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 10, markerOptions.isFlat());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 11, markerOptions.getRotation());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 12, markerOptions.getInfoWindowAnchorU());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 13, markerOptions.getInfoWindowAnchorV());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public MarkerOptions createFromParcel(Parcel parcel) {
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        int iF = 0;
        LatLng latLng = null;
        String strL = null;
        String strL2 = null;
        IBinder iBinderM = null;
        float fI = BitmapDescriptorFactory.HUE_RED;
        float fI2 = BitmapDescriptorFactory.HUE_RED;
        boolean zC = false;
        boolean zC2 = false;
        boolean zC3 = false;
        float fI3 = BitmapDescriptorFactory.HUE_RED;
        float fI4 = 0.5f;
        float fI5 = BitmapDescriptorFactory.HUE_RED;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 2:
                    latLng = (LatLng) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, LatLng.CREATOR);
                    break;
                case 3:
                    strL = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 4:
                    strL2 = com.google.android.gms.common.internal.safeparcel.a.l(parcel, iB);
                    break;
                case 5:
                    iBinderM = com.google.android.gms.common.internal.safeparcel.a.m(parcel, iB);
                    break;
                case 6:
                    fI = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 7:
                    fI2 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 8:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 9:
                    zC2 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 10:
                    zC3 = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 11:
                    fI3 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 12:
                    fI4 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 13:
                    fI5 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new MarkerOptions(iF, latLng, strL, strL2, iBinderM, fI, fI2, zC, zC2, zC3, fI3, fI4, fI5);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public MarkerOptions[] newArray(int size) {
        return new MarkerOptions[size];
    }
}
