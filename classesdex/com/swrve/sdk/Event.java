package com.swrve.sdk;

import com.facebook.internal.ServerProtocol;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class Event {
    private static final Object BATCH_API_VERSION = "2";

    public static String eventAsJSON(String type, Map<String, Object> parameters, Map<String, String> payload) throws JSONException {
        long timestamp = new Date().getTime();
        JSONObject obj = new JSONObject();
        obj.put(ServerProtocol.DIALOG_PARAM_TYPE, type);
        obj.put("timestamp", timestamp);
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
            }
        }
        if (payload != null) {
            obj.put("payload", SwrveHelper.mapToJSONObject(payload));
        }
        return obj.toString();
    }

    public static String eventsAsBatch(String userId, String appVersion, String sessionToken, Collection<String> events) throws JSONException {
        JSONObject batch = new JSONObject();
        batch.put("user", userId);
        batch.put("session_token", sessionToken);
        batch.put("version", BATCH_API_VERSION);
        batch.put("app_version", appVersion);
        batch.put("data", collectionToJSONArray(events));
        return batch.toString();
    }

    private static JSONArray collectionToJSONArray(Collection<String> collection) throws JSONException {
        JSONArray obj = new JSONArray();
        for (String entry : collection) {
            obj.put(new JSONObject(entry));
        }
        return obj;
    }
}
