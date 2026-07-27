package com.google.android.gms.internal;

import java.util.HashMap;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class ar {
    public static void a(StringBuilder sb, HashMap<String, String> map) {
        boolean z;
        sb.append("{");
        boolean z2 = true;
        for (String str : map.keySet()) {
            if (z2) {
                z = false;
            } else {
                sb.append(",");
                z = z2;
            }
            String str2 = map.get(str);
            sb.append("\"").append(str).append("\":");
            if (str2 == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(str2).append("\"");
            }
            z2 = z;
        }
        sb.append("}");
    }
}
