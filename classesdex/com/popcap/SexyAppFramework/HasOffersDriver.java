package com.popcap.SexyAppFramework;

import android.content.Context;
import android.util.Log;
import com.mobileapptracker.MATEventItem;
import com.mobileapptracker.MobileAppTracker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class HasOffersDriver {
    private static final String TAG = "HasOffersDriver";
    private Context mContext;
    private MobileAppTracker mTracker;

    public HasOffersDriver() {
        Log.v(TAG, "Creating HasOffers driver");
        this.mContext = SexyAppFrameworkActivity.instance();
    }

    public void init(String advertiserId, String appKey) {
        boolean collectDeviceId = true;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == -1) {
            collectDeviceId = false;
        }
        this.mTracker = new MobileAppTracker(this.mContext, advertiserId, appKey, collectDeviceId, true);
        HasOffersResponse response = new HasOffersResponse();
        this.mTracker.setMATResponse(response);
        this.mTracker.trackInstall();
    }

    public void sendAction(String action, String[] params) {
        Log.v(TAG, "HasOffers::sendAction=" + action);
        HashMap<String, String> unflattened = unflattenParams(params);
        List<MATEventItem> eventList = new ArrayList<>();
        Iterator<Map.Entry<String, String>> it = unflattened.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> next = it.next();
            Log.v(TAG, "HasOffers::event=" + next.getKey() + ":" + next.getValue());
            MATEventItem item = new MATEventItem(next.getKey(), next.getValue(), null, null, null, null);
            eventList.add(item);
            it.remove();
        }
        this.mTracker.trackAction(action, eventList);
    }

    public void sendPurchaseAction(String action, double price, String currency) {
        Log.v(TAG, "HasOffers::sendPurchaseAction=" + action + "," + price + "," + currency);
        this.mTracker.trackAction(action, price, currency);
    }

    private HashMap<String, String> unflattenParams(String[] flattened) {
        HashMap<String, String> unflattened = new HashMap<>();
        if (flattened != null && flattened.length % 2 == 0) {
            boolean skip = false;
            for (int i = 0; i < flattened.length; i++) {
                if (!skip) {
                    unflattened.put(flattened[i], flattened[i + 1]);
                    skip = true;
                } else {
                    skip = false;
                }
            }
        }
        Log.v(TAG, "HasOffers::unflattenParams=" + unflattened.toString());
        return unflattened;
    }
}
