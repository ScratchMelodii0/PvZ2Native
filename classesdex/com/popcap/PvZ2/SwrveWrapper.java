package com.popcap.PvZ2;

import android.util.Log;
import com.swrve.sdk.ISwrveUserResourcesListener;
import com.swrve.sdk.SwrveInstance;
import com.swrve.sdk.config.SwrveConfig;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveWrapper implements ISwrveUserResourcesListener {
    @Override // com.swrve.sdk.ISwrveUserResourcesListener
    public native void onUserResourcesSuccess(Map<String, Map<String, String>> map, String str);

    private void Init(int gameId, String apiKey, String userId, String eventsServer) {
        SwrveConfig swrveConfig = new SwrveConfig();
        swrveConfig.setEventsUrl(eventsServer);
        swrveConfig.setIdentifierRules(7);
        SwrveInstance.init(PvZ2GameActivity.instance(), gameId, apiKey, userId, swrveConfig);
    }

    private void GetUserResources() {
        SwrveInstance.getUserResources(this);
    }

    private Map<String, String> ConvertJsonToMap(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<?> jsonKeys = jsonObject.keys();
            Map<String, String> jsonMap = new HashMap<>();
            while (jsonKeys.hasNext()) {
                String key = jsonKeys.next();
                jsonMap.put(key, jsonObject.getString(key));
            }
            return jsonMap;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void UserUpdate(String jsonString) {
        Map<String, String> jsonMap = ConvertJsonToMap(jsonString);
        SwrveInstance.userUpdate(jsonMap);
    }

    private void Event(String name, String jsonString) {
        Map<String, String> jsonMap = ConvertJsonToMap(jsonString);
        SwrveInstance.event(name, jsonMap);
    }

    @Override // com.swrve.sdk.ISwrveUserResourcesListener
    public void onUserResourcesError(Exception exception) {
        Log.e("SWRVE", "Failed to get user resources: " + exception.getMessage());
    }
}
