package android.support.v4.app;

import android.app.Activity;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class ActivityCompat extends ContextCompat {
    public static boolean invalidateOptionsMenu(Activity activity) {
        if (Build.VERSION.SDK_INT < 11) {
            return false;
        }
        ActivityCompatHoneycomb.invalidateOptionsMenu(activity);
        return true;
    }
}
