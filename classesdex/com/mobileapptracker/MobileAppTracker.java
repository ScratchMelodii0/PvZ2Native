package com.mobileapptracker;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import com.google.android.gms.plus.PlusShare;
import com.google.android.vending.expansion.downloader.Constants;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class MobileAppTracker {
    public static final int GENDER_FEMALE = 1;
    public static final int GENDER_MALE = 0;
    private static final Uri a = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
    private MATResponse b;
    private UrlRequester c;
    private boolean d;
    private boolean e;
    private boolean f;
    private boolean g;
    private boolean h;
    private boolean i;
    private String j;
    private String k;
    private ConcurrentHashMap l;
    private Context m;
    private Encryption n;
    private ScheduledExecutorService o;
    private List p;
    private Semaphore q;
    private SharedPreferences r;
    private SharedPreferences s;

    public MobileAppTracker(Context context, String advertiserId, String key) {
        this(context, advertiserId, key, true, true);
    }

    public MobileAppTracker(Context context, String advertiserId, String key, boolean collectDeviceId, boolean collectMacAddress) {
        this.d = false;
        this.e = false;
        this.f = false;
        this.g = false;
        this.h = false;
        this.i = true;
        if (this.h) {
            return;
        }
        this.h = true;
        this.m = context;
        this.o = Executors.newSingleThreadScheduledExecutor();
        this.q = new Semaphore(1, true);
        this.c = new UrlRequester();
        this.l = new ConcurrentHashMap();
        this.p = Arrays.asList("ir", "d", "db", "dm", "ma", "ov", "cc", "l", "an", "pn", "av", "dc", "ad", "android_id_md5", "android_id_sha1", "android_id_sha256", "r", "c", "id", "ua", "tpid", "ar", "ti", "age", "gender", "latitude", "longitude", "altitude", "connection_type", "mobile_country_code", "mobile_network_code", "screen_density", "screen_layout_size", "android_purchase_status", "event_referrer", "app_ad_tracking");
        this.g = a(context, advertiserId, key, collectDeviceId, collectMacAddress);
        this.n = new Encryption(key, "heF9BATUfWuISyO8");
        this.r = context.getSharedPreferences("mat_queue", 0);
        this.s = context.getSharedPreferences("mat_install", 0);
        this.j = this.s.getString("install", "");
        if (this.g && a() > 0 && c()) {
            try {
                b();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        b bVar = new b(this);
        if (this.d) {
            context.getApplicationContext().unregisterReceiver(bVar);
            this.d = false;
        }
        context.getApplicationContext().registerReceiver(bVar, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        this.d = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int a() {
        return this.r.getInt("queuesize", 0);
    }

    private synchronized int a(String str, String str2, double d, String str3, String str4, String str5) {
        boolean z;
        int i;
        if (this.g) {
            if (c() && a() > 0) {
                try {
                    b();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
            this.l.remove("android_purchase_status");
            this.l.remove("ei");
            this.l.remove("en");
            a("conversion");
            char[] charArray = str.toCharArray();
            int length = charArray.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    z = false;
                    break;
                }
                if (Character.isLetter(charArray[i2])) {
                    z = true;
                    break;
                }
                i2++;
            }
            if (!z) {
                setEventId(str);
            } else if (str.equals("open")) {
                a("open");
            } else if (str.equals("close")) {
                i = -1;
            } else if (str.equals("install")) {
                a("install");
            } else if (str.equals("update")) {
                a("update");
            } else {
                setEventName(str);
            }
            String strD = d();
            if (strD == null) {
                Log.d("MobileAppTracker", "Error constructing url for tracking call");
                i = -1;
            } else {
                String action = getAction();
                if (c()) {
                    try {
                        this.o.schedule(new c(this, strD, str2, action, d, str3, str4, str5, true), Constants.ACTIVE_THREAD_WATCHDOG, TimeUnit.MILLISECONDS);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        Log.d("MobileAppTracker", "Request could not be executed from track");
                    }
                    i = 1;
                } else {
                    if (!action.equals("open")) {
                        if (this.f) {
                            Log.d("MobileAppTracker", "Not online: track will be queued");
                        }
                        try {
                            a(strD, str2, action, d, str3, str4, str5, true);
                        } catch (InterruptedException e3) {
                            e3.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    }
                    i = 1;
                }
            }
        } else {
            i = -1;
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:97:0x0304  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.String a(java.lang.String r10, java.lang.String r11, double r12, java.lang.String r14) {
        /*
            Method dump skipped, instruction units count: 775
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mobileapptracker.MobileAppTracker.a(java.lang.String, java.lang.String, double, java.lang.String):java.lang.String");
    }

    private void a(int i) {
        SharedPreferences.Editor editorEdit = this.r.edit();
        if (i < 0) {
            i = 0;
        }
        editorEdit.putInt("queuesize", i);
        editorEdit.commit();
    }

    private void a(String str) {
        b("ac", str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void a(String str, String str2, String str3, double d, String str4, String str5, String str6, boolean z) throws InterruptedException {
        this.q.acquire();
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("link", str);
            if (str2 != null) {
                jSONObject.put("event_items", str2);
            }
            jSONObject.put("action", str3);
            jSONObject.put("revenue", d);
            if (str4 == null) {
                str4 = "USD";
            }
            jSONObject.put("currency", str4);
            if (str5 != null) {
                jSONObject.put("iap_data", str5);
            }
            if (str6 != null) {
                jSONObject.put("iap_signature", str6);
            }
            jSONObject.put("should_build_data", z);
            SharedPreferences.Editor editorEdit = this.r.edit();
            int iA = a() + 1;
            a(iA);
            editorEdit.putString(Integer.valueOf(iA).toString(), jSONObject.toString());
            editorEdit.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            this.q.release();
        }
    }

    private void a(SimpleDateFormat simpleDateFormat, Calendar calendar, String str, String str2) {
        String str3 = simpleDateFormat.format(calendar.getTime());
        this.s = this.m.getSharedPreferences(str, 0);
        SharedPreferences.Editor editorEdit = this.s.edit();
        editorEdit.putString(str2, str3);
        editorEdit.commit();
    }

    private boolean a(Context context, String str, String str2, boolean z, boolean z2) {
        ApplicationInfo applicationInfo;
        WifiManager wifiManager;
        WifiInfo connectionInfo;
        try {
            setAdvertiserId(str);
            setKey(str2);
            a("conversion");
            this.s = context.getSharedPreferences("mat_id", 0);
            String string = this.s.getString("mat_id", "");
            if (string.length() == 0) {
                string = UUID.randomUUID().toString();
                SharedPreferences.Editor editorEdit = this.s.edit();
                editorEdit.putString("mat_id", string);
                editorEdit.commit();
            }
            b("mi", string);
            b("ad", Settings.Secure.getString(context.getContentResolver(), "android_id"));
            b("dm", Build.MODEL);
            b("db", Build.MANUFACTURER);
            b("ov", Build.VERSION.RELEASE);
            if (z) {
                b("d", ((TelephonyManager) context.getSystemService("phone")).getDeviceId());
            }
            if (z2 && (wifiManager = (WifiManager) context.getSystemService("wifi")) != null && (connectionInfo = wifiManager.getConnectionInfo()) != null && connectionInfo.getMacAddress() != null) {
                b("ma", connectionInfo.getMacAddress());
            }
            if (((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1).isConnected()) {
                b("connection_type", "WIFI");
            } else {
                b("connection_type", "mobile");
            }
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager != null) {
                if (telephonyManager.getNetworkCountryIso() != null) {
                    b("cc", telephonyManager.getNetworkCountryIso());
                } else if (z && telephonyManager.getSimCountryIso() != null) {
                    b("cc", telephonyManager.getSimCountryIso());
                }
                b("dc", telephonyManager.getNetworkOperatorName());
                String networkOperator = telephonyManager.getNetworkOperator();
                if (networkOperator != null) {
                    try {
                        String strSubstring = networkOperator.substring(0, 3);
                        String strSubstring2 = networkOperator.substring(3);
                        b("mobile_country_code", strSubstring);
                        b("mobile_network_code", strSubstring2);
                    } catch (IndexOutOfBoundsException e) {
                        Log.d("MobileAppTracker", "MCC/MNC not found");
                    }
                }
            } else {
                b("cc", Locale.getDefault().getCountry());
            }
            b("l", Locale.getDefault().getDisplayLanguage(Locale.US));
            setCurrencyCode("USD");
            String packageName = context.getPackageName();
            setPackageName(packageName);
            PackageManager packageManager = context.getPackageManager();
            try {
                applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e2) {
                applicationInfo = null;
                Log.d("MobileAppTracker", "ApplicationInfo not found");
                e2.printStackTrace();
            }
            if (applicationInfo != null) {
                b("an", packageManager.getApplicationLabel(applicationInfo).toString());
                Date date = new Date(new File(packageManager.getApplicationInfo(packageName, 0).sourceDir).lastModified());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                b("id", simpleDateFormat.format(date));
            }
            try {
                b(packageManager.getPackageInfo(packageName, 0).versionCode);
            } catch (PackageManager.NameNotFoundException e3) {
                Log.d("MobileAppTracker", "App version not found");
                b(0);
            }
            this.s = context.getSharedPreferences("mat_referrer", 0);
            setReferrer(this.s.getString("referrer", ""));
            this.s = context.getSharedPreferences("mat_install", 0);
            this.j = this.s.getString("install", "");
            new Handler(Looper.getMainLooper()).post(new d(this, context));
            b("screen_density", Float.toString(context.getResources().getDisplayMetrics().density));
            WindowManager windowManager = (WindowManager) context.getSystemService("window");
            b("screen_layout_size", String.valueOf(Integer.toString(windowManager.getDefaultDisplay().getWidth())) + "x" + Integer.toString(windowManager.getDefaultDisplay().getHeight()));
            return true;
        } catch (Exception e4) {
            e4.printStackTrace();
            Log.d("MobileAppTracker", "MobileAppTracker initialization failed");
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean a(String str, String str2) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        this.s = this.m.getSharedPreferences(str, 0);
        String string = this.s.getString(str2, "");
        if (string.length() <= 0) {
            a(simpleDateFormat, calendar, str, str2);
            return true;
        }
        Calendar calendar2 = Calendar.getInstance();
        try {
            calendar2.setTime(simpleDateFormat.parse(string));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (new TimeIgnoringComparator().compare(calendar, calendar2) != 1) {
            return false;
        }
        a(simpleDateFormat, calendar, str, str2);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void b() {
        this.q.acquire();
        try {
            int iA = a();
            if (iA != 0) {
                for (int i = iA > 50 ? (iA - 50) + 1 : 1; i <= iA; i++) {
                    String string = Integer.valueOf(i).toString();
                    String string2 = this.r.getString(string, null);
                    if (string2 != null) {
                        try {
                            JSONObject jSONObject = new JSONObject(string2);
                            String string3 = jSONObject.getString("link");
                            String string4 = jSONObject.has("event_items") ? jSONObject.getString("event_items") : null;
                            String string5 = jSONObject.getString("action");
                            double d = jSONObject.getDouble("revenue");
                            String string6 = jSONObject.getString("currency");
                            String string7 = jSONObject.has("iap_data") ? jSONObject.getString("iap_data") : null;
                            String string8 = jSONObject.has("iap_signature") ? jSONObject.getString("iap_signature") : null;
                            boolean z = jSONObject.getBoolean("should_build_data");
                            a(a() - 1);
                            SharedPreferences.Editor editorEdit = this.r.edit();
                            editorEdit.remove(string);
                            editorEdit.commit();
                            try {
                                this.o.execute(new c(this, string3, string4, string5, d, string6, string7, string8, z));
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("MobileAppTracker", "Request could not be executed from queue");
                            }
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                            a(a() - 1);
                            SharedPreferences.Editor editorEdit2 = this.r.edit();
                            editorEdit2.remove(string);
                            editorEdit2.commit();
                            this.q.release();
                        }
                    }
                }
                this.q.release();
            }
        } finally {
            this.q.release();
        }
    }

    private void b(int i) {
        b("av", Integer.toString(i));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void b(String str) {
        this.s = this.m.getSharedPreferences("mat_log_id_install", 0);
        SharedPreferences.Editor editorEdit = this.s.edit();
        editorEdit.putString("logId", str);
        editorEdit.commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void b(String str, String str2) {
        if (str == null || str2 == null) {
            return;
        }
        try {
            str2 = URLEncoder.encode(str2, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.l.put(str, str2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void c(String str) {
        this.s = this.m.getSharedPreferences("mat_log_id_update", 0);
        SharedPreferences.Editor editorEdit = this.s.edit();
        editorEdit.putString("logId", str);
        editorEdit.commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean c() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.m.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String d() {
        String strEncode;
        StringBuilder sbAppend = new StringBuilder(this.i ? "https://" : "http://").append(getAdvertiserId()).append(".");
        if (this.f) {
            sbAppend.append("debug.engine.mobileapptracking.com");
        } else {
            sbAppend.append("engine.mobileapptracking.com");
        }
        sbAppend.append("/serve?s=android&ver=2.5&pn=").append(getPackageName());
        for (String str : this.l.keySet()) {
            if (!this.p.contains(str)) {
                sbAppend.append("&").append(str).append("=").append((String) this.l.get(str));
            }
        }
        if (this.e) {
            sbAppend.append("&skip_dup=1");
        }
        if (this.f) {
            sbAppend.append("&debug=1");
        }
        try {
            Cursor cursorQuery = this.m.getContentResolver().query(Uri.parse("content://" + getPackageName() + "/referrer_apps"), null, null, null, "publisher_package_name desc");
            if (cursorQuery != null && cursorQuery.moveToFirst()) {
                String string = cursorQuery.getString(cursorQuery.getColumnIndex(MATProvider.TRACKING_ID));
                try {
                    strEncode = URLEncoder.encode(string, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    strEncode = string;
                }
                this.l.put("ti", strEncode);
                cursorQuery.close();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.d("MobileAppTracker", "Error reading app-to-app values");
        }
        return sbAppend.toString();
    }

    public String getAction() {
        return (String) this.l.get("ac");
    }

    public String getAdvertiserId() {
        return (String) this.l.get("adv");
    }

    public int getAge() {
        if (this.l.get("age") == null) {
            return 0;
        }
        return Integer.parseInt((String) this.l.get("age"));
    }

    public double getAltitude() {
        if (this.l.get("altitude") == null) {
            return 0.0d;
        }
        return Double.parseDouble((String) this.l.get("altitude"));
    }

    public String getAndroidId() {
        return (String) this.l.get("ad");
    }

    public String getAndroidIdMd5() {
        return (String) this.l.get("android_id_md5");
    }

    public String getAndroidIdSha1() {
        return (String) this.l.get("android_id_sha1");
    }

    public String getAndroidIdSha256() {
        return (String) this.l.get("android_id_sha256");
    }

    public String getAppName() {
        return (String) this.l.get("an");
    }

    public int getAppVersion() {
        if (this.l.get("av") == null) {
            return 0;
        }
        return Integer.parseInt((String) this.l.get("av"));
    }

    public String getCarrier() {
        return (String) this.l.get("dc");
    }

    public String getConnectionType() {
        return (String) this.l.get("connection_type");
    }

    public String getCountryCode() {
        return (String) this.l.get("cc");
    }

    public String getCurrencyCode() {
        return (String) this.l.get("c");
    }

    public String getDeviceBrand() {
        return (String) this.l.get("db");
    }

    public String getDeviceId() {
        return (String) this.l.get("d");
    }

    public String getDeviceModel() {
        return (String) this.l.get("dm");
    }

    public String getEventId() {
        return (String) this.l.get("ei");
    }

    public String getEventName() {
        return (String) this.l.get("en");
    }

    public String getEventReferrer() {
        return (String) this.l.get("event_referrer");
    }

    public int getGender() {
        if (this.l.get("gender") == null) {
            return 0;
        }
        return Integer.parseInt((String) this.l.get("gender"));
    }

    public String getInstallDate() {
        return (String) this.l.get("id");
    }

    public String getInstallLogId() {
        this.s = this.m.getSharedPreferences("mat_log_id_install", 0);
        return this.s.getString("logId", "");
    }

    public final String getKey() {
        return this.k;
    }

    public String getLanguage() {
        return (String) this.l.get("l");
    }

    public double getLatitude() {
        if (this.l.get("latitude") == null) {
            return 0.0d;
        }
        return Double.parseDouble((String) this.l.get("latitude"));
    }

    public double getLongitude() {
        if (this.l.get("longitude") == null) {
            return 0.0d;
        }
        return Double.parseDouble((String) this.l.get("longitude"));
    }

    public String getMCC() {
        return (String) this.l.get("mobile_country_code");
    }

    public String getMNC() {
        return (String) this.l.get("mobile_network_code");
    }

    public String getMacAddress() {
        return (String) this.l.get("ma");
    }

    public String getMatId() {
        return (String) this.l.get("mi");
    }

    public String getOsId() {
        return (String) this.l.get("oi");
    }

    public String getOsVersion() {
        return (String) this.l.get("ov");
    }

    public String getPackageName() {
        return (String) this.l.get("pn");
    }

    public String getRefId() {
        return (String) this.l.get("ar");
    }

    public String getReferrer() {
        return (String) this.l.get("ir");
    }

    public String getRevenue() {
        return (String) this.l.get("r");
    }

    public String getScreenDensity() {
        return (String) this.l.get("screen_density");
    }

    public String getScreenSize() {
        return (String) this.l.get("screen_layout_size");
    }

    public String getSiteId() {
        return (String) this.l.get("si");
    }

    public String getTRUSTeId() {
        return (String) this.l.get("tpid");
    }

    public String getUpdateLogId() {
        this.s = this.m.getSharedPreferences("mat_log_id_update", 0);
        return this.s.getString("logId", "");
    }

    public String getUserAgent() {
        return (String) this.l.get("ua");
    }

    public String getUserId() {
        return (String) this.l.get("ui");
    }

    public boolean isInitialized() {
        return this.g;
    }

    public void setAdvertiserId(String advertiser_id) {
        b("adv", advertiser_id);
    }

    public void setAge(int age) {
        b("age", Integer.toString(age));
    }

    public void setAllowDuplicates(boolean allow) {
        this.e = allow;
    }

    public void setAltitude(double altitude) {
        b("altitude", Double.toString(altitude));
    }

    public void setAppAdTracking(boolean appAdTracking) {
        if (appAdTracking) {
            b("app_ad_tracking", Integer.toString(1));
        } else {
            b("app_ad_tracking", Integer.toString(0));
        }
    }

    public void setCurrencyCode(String currency_code) {
        b("c", currency_code);
    }

    public void setDebugMode(boolean debug) {
        this.f = debug;
    }

    public void setEventId(String event_id) {
        b("ei", event_id);
    }

    public void setEventName(String event_name) {
        b("en", event_name);
    }

    public void setEventReferrer(String referrerPackage) {
        b("event_referrer", referrerPackage);
    }

    public void setGender(int gender) {
        b("gender", Integer.toString(gender));
    }

    public void setHttpsEncryption(boolean use_https) {
        this.i = use_https;
    }

    public void setKey(String key) {
        this.k = key;
        this.n = new Encryption(key, "heF9BATUfWuISyO8");
    }

    public void setLatitude(double latitude) {
        b("latitude", Double.toString(latitude));
    }

    public void setLongitude(double longitude) {
        b("longitude", Double.toString(longitude));
    }

    public void setMATResponse(MATResponse response) {
        this.b = response;
    }

    public void setOsId(String os_id) {
        b("oi", os_id);
    }

    public void setPackageName(String package_name) {
        b("pn", package_name);
    }

    public void setRefId(String ref_id) {
        b("ar", ref_id);
    }

    public void setReferrer(String referrer) {
        b("ir", referrer);
    }

    public void setRevenue(double revenue) {
        b("r", Double.toString(revenue));
    }

    public void setSiteId(String site_id) {
        b("si", site_id);
    }

    public void setTRUSTeId(String tpid) {
        b("tpid", tpid);
    }

    public String setTracking(String publisherAdvertiserId, String targetPackageName, String publisherId, String campaignId, boolean doRedirect) {
        String string = "";
        String string2 = "";
        StringBuilder sb = new StringBuilder("https://engine.mobileapptracking.com/serve?action=click&sdk=android");
        sb.append("&publisher_advertiser_id=").append(publisherAdvertiserId);
        sb.append("&package_name=").append(targetPackageName);
        if (publisherId != null) {
            sb.append("&publisher_id=").append(publisherId);
        }
        if (campaignId != null) {
            sb.append("&campaign_id=").append(campaignId);
        }
        sb.append("&response_format=json");
        JSONObject jSONObjectRequestUrl = this.c.requestUrl(sb.toString(), null);
        if (jSONObjectRequestUrl != null) {
            try {
                string = jSONObjectRequestUrl.getString(MATProvider.TRACKING_ID);
                string2 = jSONObjectRequestUrl.getString(PlusShare.KEY_CALL_TO_ACTION_URL);
            } catch (JSONException e) {
                Log.d("MobileAppTracker", "Unable to get tracking ID or redirect url from app-to-app tracking");
                return "";
            }
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MATProvider.PUBLISHER_PACKAGE_NAME, getPackageName());
        contentValues.put(MATProvider.TRACKING_ID, string);
        this.m.getContentResolver().insert(Uri.parse("content://" + targetPackageName + "/referrer_apps"), contentValues);
        if (!doRedirect) {
            return string2;
        }
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(string2));
            intent.addFlags(268435456);
            this.m.startActivity(intent);
            return string2;
        } catch (ActivityNotFoundException e2) {
            Log.d("MobileAppTracker", "Unable to start activity to open " + string2);
            return string2;
        }
    }

    public void setUseAndroidIdMd5() {
        b("android_id_md5", this.n.md5(Settings.Secure.getString(this.m.getContentResolver(), "android_id")));
        b("ad", "");
    }

    public void setUseAndroidIdSha1() {
        b("android_id_sha1", this.n.sha1(Settings.Secure.getString(this.m.getContentResolver(), "android_id")));
        b("ad", "");
    }

    public void setUseAndroidIdSha256() {
        b("android_id_sha256", this.n.sha256(Settings.Secure.getString(this.m.getContentResolver(), "android_id")));
        b("ad", "");
    }

    public void setUserId(String user_id) {
        b("ui", user_id);
    }

    public int trackAction(String event) {
        return a(event, null, 0.0d, null, null, null);
    }

    public int trackAction(String event, double revenue) {
        return a(event, null, revenue, null, null, null);
    }

    public int trackAction(String event, double revenue, String currency) {
        return a(event, null, revenue, currency, null, null);
    }

    public int trackAction(String event, double revenue, String currency, String inAppPurchaseData, String inAppSignature) {
        return a(event, null, revenue, currency, inAppPurchaseData, inAppSignature);
    }

    public int trackAction(String event, MATEventItem eventItem) {
        JSONArray jSONArray = new JSONArray();
        jSONArray.put(eventItem.toJSON());
        return a(event, jSONArray.toString(), 0.0d, null, null, null);
    }

    public int trackAction(String event, MATEventItem eventItem, String inAppPurchaseData, String inAppSignature) {
        JSONArray jSONArray = new JSONArray();
        jSONArray.put(eventItem.toJSON());
        return a(event, jSONArray.toString(), 0.0d, null, inAppPurchaseData, inAppSignature);
    }

    public int trackAction(String event, List list) {
        JSONArray jSONArray = new JSONArray();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= list.size()) {
                return a(event, jSONArray.toString(), 0.0d, null, null, null);
            }
            jSONArray.put(((MATEventItem) list.get(i2)).toJSON());
            i = i2 + 1;
        }
    }

    public int trackAction(String event, List list, String inAppPurchaseData, String inAppSignature) {
        JSONArray jSONArray = new JSONArray();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= list.size()) {
                return a(event, jSONArray.toString(), 0.0d, null, inAppPurchaseData, inAppSignature);
            }
            jSONArray.put(((MATEventItem) list.get(i2)).toJSON());
            i = i2 + 1;
        }
    }

    public int trackInstall() {
        return trackInstall(this.m);
    }

    public synchronized int trackInstall(Context context) {
        int iA;
        this.s = context.getSharedPreferences("mat_install", 0);
        this.j = this.s.getString("install", "");
        if (this.j.equals("")) {
            this.s = context.getSharedPreferences("mat_install", 0);
            SharedPreferences.Editor editorEdit = this.s.edit();
            editorEdit.putString("install", "installed");
            editorEdit.commit();
            this.s = context.getSharedPreferences("mat_app_version", 0);
            SharedPreferences.Editor editorEdit2 = this.s.edit();
            editorEdit2.putString("version", Integer.toString(getAppVersion()));
            editorEdit2.commit();
            iA = a("install", null, 0.0d, null, null, null);
        } else {
            this.s = context.getSharedPreferences("mat_app_version", 0);
            String string = this.s.getString("version", "");
            if (string.length() == 0 || Integer.parseInt(string) == getAppVersion()) {
                if (this.f) {
                    Log.d("MobileAppTracker", "Install has been tracked before");
                }
                iA = 2;
            } else {
                if (this.f) {
                    Log.d("MobileAppTracker", "App version has changed since last trackInstall, sending update to server");
                }
                a("update", null, 0.0d, null, null, null);
                SharedPreferences.Editor editorEdit3 = this.s.edit();
                editorEdit3.putString("version", Integer.toString(getAppVersion()));
                editorEdit3.commit();
                iA = 3;
            }
        }
        return iA;
    }

    public int trackPurchase(String event, int purchaseStatus, double revenue, String currency) {
        b("android_purchase_status", Integer.toString(purchaseStatus));
        return a(event, null, revenue, currency, null, null);
    }

    public int trackUpdate() {
        this.s = this.m.getSharedPreferences("mat_install", 0);
        this.j = this.s.getString("install", "");
        if (this.j.equals("")) {
            this.s = this.m.getSharedPreferences("mat_install", 0);
            SharedPreferences.Editor editorEdit = this.s.edit();
            editorEdit.putString("install", "installed");
            editorEdit.commit();
            this.s = this.m.getSharedPreferences("mat_app_version", 0);
            SharedPreferences.Editor editorEdit2 = this.s.edit();
            editorEdit2.putString("version", Integer.toString(getAppVersion()));
            editorEdit2.commit();
            this.j = "installed";
            return a("update", null, 0.0d, null, null, null);
        }
        this.s = this.m.getSharedPreferences("mat_app_version", 0);
        String string = this.s.getString("version", "");
        if (string.length() == 0 || Integer.parseInt(string) == getAppVersion()) {
            if (this.f) {
                Log.d("MobileAppTracker", "Update has been tracked before");
            }
            return 2;
        }
        if (this.f) {
            Log.d("MobileAppTracker", "App version has changed since last trackInstall, sending update to server");
        }
        a("update", null, 0.0d, null, null, null);
        SharedPreferences.Editor editorEdit3 = this.s.edit();
        editorEdit3.putString("version", Integer.toString(getAppVersion()));
        editorEdit3.commit();
        return 3;
    }
}
