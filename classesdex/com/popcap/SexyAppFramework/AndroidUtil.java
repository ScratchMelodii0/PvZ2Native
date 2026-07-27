package com.popcap.SexyAppFramework;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.google.android.vending.expansion.downloader.Constants;
import java.util.TimeZone;
import java.util.UUID;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidUtil {
    private static final String TAG = "AndroidUtil";

    public static String getTimezone() {
        TimeZone timezone = TimeZone.getDefault();
        int secondsFromGMT = timezone.getRawOffset() / 1000;
        String offsetSign = "+";
        if (secondsFromGMT < 0) {
            offsetSign = Constants.FILENAME_SEQUENCE_SEPARATOR;
            secondsFromGMT *= -1;
        }
        float hoursFromUTC = secondsFromGMT / 3600.0f;
        int intHoursFromUTC = Math.round(hoursFromUTC);
        int leftoverSecondsFromUTC = secondsFromGMT - (intHoursFromUTC * 3600);
        int minutesFromUTC = leftoverSecondsFromUTC / 60;
        String utcTimezone = String.format("UTC%s%02d:%02d", offsetSign, Integer.valueOf(intHoursFromUTC), Integer.valueOf(minutesFromUTC));
        return utcTimezone;
    }

    public static String getPackageName() {
        Context context = SexyAppFrameworkActivity.instance();
        return context.getPackageName();
    }

    public static String getDeviceId() {
        Context context = SexyAppFrameworkActivity.instance();
        if (context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            return null;
        }
        String deviceId = ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
        return deviceId;
    }

    public static String getAndroidId() {
        Context context = SexyAppFrameworkActivity.instance();
        return Settings.Secure.getString(context.getContentResolver(), "android_id");
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return model.startsWith(manufacturer) ? model : manufacturer + " " + model;
    }

    public static UUID getRandomUUID() {
        return UUID.randomUUID();
    }
}
