package com.swrve.sdk;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveHelper {
    private static final String CHARSET = "UTF-8";
    private static final String LOG_TAG = "SwrveSDK";

    public static String generateSessionToken(String apiKey, int gameId, String userId) {
        String timestamp = Long.toString(new Date().getTime() / 1000);
        String hexDigest = md5(userId + timestamp + apiKey);
        return gameId + "=" + userId + "=" + timestamp + "=" + hexDigest;
    }

    public static String md5(String text) {
        if (text == null) {
            return null;
        }
        try {
            byte[] bytesOfMessage = text.getBytes();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hash = md5.digest(bytesOfMessage);
            StringBuilder hexDigest = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                if ((hash[i] & 255) < 16) {
                    hexDigest.append("0");
                }
                hexDigest.append(Integer.toHexString(hash[i] & 255));
            }
            return hexDigest.toString();
        } catch (NoSuchAlgorithmException nsae) {
            Log.wtf(LOG_TAG, "Couldn't find MD5 - what a strange JVM", nsae);
            return "";
        }
    }

    public static String sha1(byte[] bytesOfMessage) {
        if (bytesOfMessage.length == 0) {
            return null;
        }
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] hash = sha1.digest(bytesOfMessage);
            StringBuilder hexDigest = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                if ((hash[i] & 255) < 16) {
                    hexDigest.append("0");
                }
                hexDigest.append(Integer.toHexString(hash[i] & 255));
            }
            return hexDigest.toString();
        } catch (NoSuchAlgorithmException nsae) {
            Log.wtf(LOG_TAG, "Couldn't find SHA1 - what a strange JVM", nsae);
            return "";
        }
    }

    public static String generateUUID(String value) {
        return UUID.nameUUIDFromBytes(md5(value).getBytes()).toString();
    }

    public static JSONObject mapToJSONObject(Map<String, String> map) throws JSONException {
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            obj.put(entry.getKey(), entry.getValue());
        }
        return obj;
    }

    public static Map<String, String> JSONToMap(JSONObject obj) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<String> it = obj.keys();
        while (it.hasNext()) {
            String key = it.next();
            map.put(key, obj.getString(key));
        }
        return map;
    }

    public static String encodeParameters(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder body = new StringBuilder();
        boolean firstElement = true;
        for (Map.Entry<String, String> pairs : params.entrySet()) {
            if (firstElement) {
                firstElement = false;
            } else {
                body.append("&");
            }
            body.append(URLEncoder.encode(pairs.getKey(), CHARSET) + "=" + URLEncoder.encode(pairs.getValue(), CHARSET));
        }
        return body.toString();
    }
}
