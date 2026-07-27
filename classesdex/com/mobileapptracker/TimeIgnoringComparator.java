package com.mobileapptracker;

import java.util.Calendar;
import java.util.Comparator;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class TimeIgnoringComparator implements Comparator {
    @Override // java.util.Comparator
    public int compare(Calendar c1, Calendar c2) {
        return c1.get(1) != c2.get(1) ? c1.get(1) - c2.get(1) : c1.get(2) != c2.get(2) ? c1.get(2) - c2.get(2) : c1.get(5) - c2.get(5);
    }
}
