package android.support.v4.os;

import android.os.Parcelable;

/* JADX INFO: compiled from: ParcelableCompatHoneycombMR2.java */
/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class ParcelableCompatCreatorHoneycombMR2Stub {
    ParcelableCompatCreatorHoneycombMR2Stub() {
    }

    static <T> Parcelable.Creator<T> instantiate(ParcelableCompatCreatorCallbacks<T> callbacks) {
        return new ParcelableCompatCreatorHoneycombMR2(callbacks);
    }
}
