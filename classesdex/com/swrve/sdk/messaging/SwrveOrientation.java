package com.swrve.sdk.messaging;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public enum SwrveOrientation {
    Portrait,
    Landscape,
    Both;

    public static SwrveOrientation parse(int androidOrientation) {
        return androidOrientation == 1 ? Portrait : Landscape;
    }

    public static SwrveOrientation parse(String orientation) {
        if (orientation.toLowerCase().equals("portrait")) {
            return Portrait;
        }
        if (orientation.toLowerCase().equals("both")) {
            return Both;
        }
        return Landscape;
    }
}
