package com.popcap.SexyAppFramework;

import android.util.Log;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidAssetUtils {
    private static final String sAssetPrefix = "ASSET:";

    public static String GetAssetNameFromFileName(String InFilename) {
        String actualFilename;
        try {
            if (InFilename.startsWith(sAssetPrefix)) {
                actualFilename = InFilename.substring(sAssetPrefix.length());
            } else {
                actualFilename = InFilename;
            }
            String actualFilename2 = actualFilename.replace('\\', '/');
            if (!actualFilename2.endsWith(".smf") && !actualFilename2.endsWith(".rton")) {
                return actualFilename2 + ".smf";
            }
            return actualFilename2;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Error", ex.toString());
            return "";
        }
    }
}
