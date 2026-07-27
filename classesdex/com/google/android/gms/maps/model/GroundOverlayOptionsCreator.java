package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GroundOverlayOptionsCreator implements Parcelable.Creator<GroundOverlayOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void a(GroundOverlayOptions groundOverlayOptions, Parcel parcel, int i) {
        int iD = com.google.android.gms.common.internal.safeparcel.b.d(parcel);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, groundOverlayOptions.i());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, groundOverlayOptions.bp(), false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 3, (Parcelable) groundOverlayOptions.getLocation(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 4, groundOverlayOptions.getWidth());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 5, groundOverlayOptions.getHeight());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 6, (Parcelable) groundOverlayOptions.getBounds(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 7, groundOverlayOptions.getBearing());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 8, groundOverlayOptions.getZIndex());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 9, groundOverlayOptions.isVisible());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 10, groundOverlayOptions.getTransparency());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 11, groundOverlayOptions.getAnchorU());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 12, groundOverlayOptions.getAnchorV());
        com.google.android.gms.common.internal.safeparcel.b.C(parcel, iD);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public GroundOverlayOptions createFromParcel(Parcel parcel) {
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        int iF = 0;
        IBinder iBinderM = null;
        LatLng latLng = null;
        float fI = BitmapDescriptorFactory.HUE_RED;
        float fI2 = BitmapDescriptorFactory.HUE_RED;
        LatLngBounds latLngBounds = null;
        float fI3 = BitmapDescriptorFactory.HUE_RED;
        float fI4 = BitmapDescriptorFactory.HUE_RED;
        boolean zC = false;
        float fI5 = BitmapDescriptorFactory.HUE_RED;
        float fI6 = BitmapDescriptorFactory.HUE_RED;
        float fI7 = BitmapDescriptorFactory.HUE_RED;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    iF = com.google.android.gms.common.internal.safeparcel.a.f(parcel, iB);
                    break;
                case 2:
                    iBinderM = com.google.android.gms.common.internal.safeparcel.a.m(parcel, iB);
                    break;
                case 3:
                    latLng = (LatLng) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, LatLng.CREATOR);
                    break;
                case 4:
                    fI = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 5:
                    fI2 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 6:
                    latLngBounds = (LatLngBounds) com.google.android.gms.common.internal.safeparcel.a.a(parcel, iB, LatLngBounds.CREATOR);
                    break;
                case 7:
                    fI3 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 8:
                    fI4 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 9:
                    zC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB);
                    break;
                case 10:
                    fI5 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 11:
                    fI6 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                case 12:
                    fI7 = com.google.android.gms.common.internal.safeparcel.a.i(parcel, iB);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, iB);
                    break;
            }
        }
        if (parcel.dataPosition() != iC) {
            throw new a.C0001a("Overread allowed size end=" + iC, parcel);
        }
        return new GroundOverlayOptions(iF, iBinderM, latLng, fI, fI2, latLngBounds, fI3, fI4, zC, fI5, fI6, fI7);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public GroundOverlayOptions[] newArray(int size) {
        return new GroundOverlayOptions[size];
    }
}
