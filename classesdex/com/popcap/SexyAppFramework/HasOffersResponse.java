package com.popcap.SexyAppFramework;

import android.util.Log;
import com.mobileapptracker.MATResponse;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class HasOffersResponse implements MATResponse {
    private static final String TAG = "HasOffersResponse";

    HasOffersResponse() {
    }

    @Override // com.mobileapptracker.MATResponse
    public void didSucceedWithData(JSONObject response) {
        Log.v(TAG, "HasOffers response: " + response.toString());
    }
}
