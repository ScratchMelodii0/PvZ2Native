package com.google.android.gms.common.data;

import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class DataBufferUtils {
    private DataBufferUtils() {
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T, E extends Freezable<T>> ArrayList<T> freezeAndClose(DataBuffer<E> dataBuffer) {
        ArrayList<T> arrayList = (ArrayList<T>) new ArrayList(dataBuffer.getCount());
        try {
            Iterator<E> it = dataBuffer.iterator();
            while (it.hasNext()) {
                arrayList.add(it.next().freeze());
            }
            return arrayList;
        } finally {
            dataBuffer.close();
        }
    }
}
