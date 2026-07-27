package com.mobileapptracker;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
final class c implements Runnable {
    final /* synthetic */ MobileAppTracker a;
    private String b;
    private String c;
    private String d;
    private double e;
    private String f;
    private String g;
    private String h;
    private boolean i;

    public c(MobileAppTracker mobileAppTracker, String str, String str2, String str3, double d, String str4, String str5, String str6, boolean z) {
        this.a = mobileAppTracker;
        this.b = null;
        this.c = null;
        this.d = null;
        this.e = 0.0d;
        this.f = null;
        this.g = null;
        this.h = null;
        this.i = false;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = d;
        this.f = str4;
        this.g = str5;
        this.h = str6;
        this.i = z;
    }

    @Override // java.lang.Runnable
    public final void run() {
        if (this.i) {
            this.b = this.a.a(this.b, this.d, this.e, this.f);
        }
        if (this.d.equals("open") && !this.a.a("mat_last_open", "lastOpenDate")) {
            if (this.a.f) {
                Log.d("MobileAppTracker", "SDK has already sent an open today, not sending request");
                return;
            }
            return;
        }
        Log.d("MobileAppTracker", "Sending " + this.d + " event to server...");
        JSONObject jSONObject = new JSONObject();
        try {
            if (this.c != null) {
                jSONObject.put("data", new JSONArray(this.c));
            }
            if (this.g != null) {
                jSONObject.put("store_iap_data", this.g);
            }
            if (this.h != null) {
                jSONObject.put("store_iap_signature", this.h);
            }
        } catch (JSONException e) {
            if (this.a.f) {
                Log.d("MobileAppTracker", "Could not build JSON for event items or verification values");
            }
            e.printStackTrace();
        }
        JSONObject jSONObjectRequestUrl = this.a.c.requestUrl(this.b, jSONObject);
        if (jSONObjectRequestUrl == null) {
            try {
                this.a.a(this.b, this.c, this.d, this.e, this.f, this.g, this.h, false);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
                Thread.currentThread().interrupt();
            }
            if (this.a.f) {
                Log.d("MobileAppTracker", "Request failed: track will be queued");
                return;
            }
            return;
        }
        if (this.a.b != null) {
            this.a.b.didSucceedWithData(jSONObjectRequestUrl);
        }
        if (this.d.equals("install")) {
            try {
                this.a.b(jSONObjectRequestUrl.getString("log_id"));
            } catch (JSONException e3) {
                e3.printStackTrace();
                Log.d("MobileAppTracker", "Install log id could not be found in response");
            }
        } else if (this.d.equals("update")) {
            try {
                this.a.c(jSONObjectRequestUrl.getString("log_id"));
            } catch (JSONException e4) {
                Log.d("MobileAppTracker", "Update log id could not be found in response");
                e4.printStackTrace();
            }
        }
        if (this.a.f) {
            Log.d("MobileAppTracker", "Server response: " + jSONObjectRequestUrl.toString());
            if (jSONObjectRequestUrl.length() > 0) {
                try {
                    if (!jSONObjectRequestUrl.getString("log_action").equals("null")) {
                        JSONObject jSONObject2 = jSONObjectRequestUrl.getJSONObject("log_action");
                        if (jSONObject2.has("conversion")) {
                            JSONObject jSONObject3 = jSONObject2.getJSONObject("conversion");
                            if (jSONObject3.has("status")) {
                                if (jSONObject3.getString("status").equals("rejected")) {
                                    Log.d("MobileAppTracker", "Event was rejected by server: status code " + jSONObject3.getString("status_code"));
                                } else {
                                    Log.d("MobileAppTracker", "Event was accepted by server");
                                }
                            }
                        }
                    } else if (jSONObjectRequestUrl.has("options")) {
                        JSONObject jSONObject4 = jSONObjectRequestUrl.getJSONObject("options");
                        if (jSONObject4.has("conversion_status")) {
                            Log.d("MobileAppTracker", "Event was " + jSONObject4.getString("conversion_status") + " by server");
                        }
                    }
                } catch (JSONException e5) {
                    e5.printStackTrace();
                    Log.d("MobileAppTracker", "Server response status could not be parsed");
                }
            }
        }
    }
}
