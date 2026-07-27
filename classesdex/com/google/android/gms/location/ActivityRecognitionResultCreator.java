package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import java.util.ArrayList;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class ActivityRecognitionResultCreator implements Parcelable.Creator<ActivityRecognitionResult> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void a(ActivityRecognitionResult activityRecognitionResult, Parcel parcel, int i) {
        int iD = b.d(parcel);
        b.b(parcel, 1, activityRecognitionResult.fp, false);
        b.c(parcel, 1000, activityRecognitionResult.i());
        b.a(parcel, 2, activityRecognitionResult.fq);
        b.a(parcel, 3, activityRecognitionResult.fr);
        b.C(parcel, iD);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public ActivityRecognitionResult createFromParcel(Parcel parcel) {
        long jG = 0;
        int iC = com.google.android.gms.common.internal.safeparcel.a.c(parcel);
        int iF = 0;
        ArrayList arrayListC = null;
        long jG2 = 0;
        while (parcel.dataPosition() < iC) {
            int iB = com.google.android.gms.common.internal.safeparcel.a.b(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.m(iB)) {
                case 1:
                    arrayListC = com.google.android.gms.common.internal.safeparcel.a.c(parcel, iB, DetectedActivity.CREATOR);
                    break;
                case 2:
                    jG2 = com.google.android.gms.common.internal.safeparcel.a.g(parcel, iB);
                    break;
                case 3:
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
        return new ActivityRecognitionResult(iF, arrayListC, jG2, jG);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // android.os.Parcelable.Creator
    public ActivityRecognitionResult[] newArray(int size) {
        return new ActivityRecognitionResult[size];
    }
}
