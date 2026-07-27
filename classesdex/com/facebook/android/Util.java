package com.facebook.android;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class Util {
    private static final String UTF8 = "UTF-8";

    @Deprecated
    public static String encodePostBody(Bundle parameters, String boundary) {
        if (parameters == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : parameters.keySet()) {
            Object parameter = parameters.get(key);
            if (parameter instanceof String) {
                sb.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n" + ((String) parameter));
                sb.append("\r\n--" + boundary + "\r\n");
            }
        }
        return sb.toString();
    }

    @Deprecated
    public static String encodeUrl(Bundle parameters) {
        if (parameters == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : parameters.keySet()) {
            Object parameter = parameters.get(key);
            if (parameter instanceof String) {
                if (first) {
                    first = false;
                } else {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(key) + "=" + URLEncoder.encode(parameters.getString(key)));
            }
        }
        return sb.toString();
    }

    @Deprecated
    public static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String[] array = s.split("&");
            for (String parameter : array) {
                String[] v = parameter.split("=");
                try {
                    if (v.length == 2) {
                        params.putString(URLDecoder.decode(v[0], UTF8), URLDecoder.decode(v[1], UTF8));
                    } else if (v.length == 1) {
                        params.putString(URLDecoder.decode(v[0], UTF8), "");
                    }
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        return params;
    }

    @Deprecated
    public static Bundle parseUrl(String url) {
        try {
            URL u = new URL(url.replace("fbconnect", "http"));
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    @Deprecated
    public static String openUrl(String url, String method, Bundle params) throws IOException {
        if (method.equals("GET")) {
            url = url + "?" + encodeUrl(params);
        }
        Utility.logd("Facebook-Util", method + " URL: " + url);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", System.getProperties().getProperty("http.agent") + " FacebookAndroidSDK");
        if (!method.equals("GET")) {
            Bundle dataparams = new Bundle();
            for (String key : params.keySet()) {
                Object parameter = params.get(key);
                if (parameter instanceof byte[]) {
                    dataparams.putByteArray(key, (byte[]) parameter);
                }
            }
            if (!params.containsKey("method")) {
                params.putString("method", method);
            }
            if (params.containsKey("access_token")) {
                String decoded_token = URLDecoder.decode(params.getString("access_token"));
                params.putString("access_token", decoded_token);
            }
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            os.write(("--3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f\r\n").getBytes());
            os.write(encodePostBody(params, "3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f").getBytes());
            os.write(("\r\n--3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f\r\n").getBytes());
            if (!dataparams.isEmpty()) {
                for (String key2 : dataparams.keySet()) {
                    os.write(("Content-Disposition: form-data; filename=\"" + key2 + "\"\r\n").getBytes());
                    os.write(("Content-Type: content/unknown\r\n\r\n").getBytes());
                    os.write(dataparams.getByteArray(key2));
                    os.write(("\r\n--3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f\r\n").getBytes());
                }
            }
            os.flush();
        }
        try {
            String response = read(conn.getInputStream());
            return response;
        } catch (FileNotFoundException e) {
            String response2 = read(conn.getErrorStream());
            return response2;
        }
    }

    @Deprecated
    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

    @Deprecated
    public static JSONObject parseJson(String response) throws JSONException, FacebookError {
        if (response.equals("false")) {
            throw new FacebookError("request failed");
        }
        if (response.equals("true")) {
            response = "{value : true}";
        }
        JSONObject json = new JSONObject(response);
        if (json.has("error")) {
            JSONObject error = json.getJSONObject("error");
            throw new FacebookError(error.getString("message"), error.getString(ServerProtocol.DIALOG_PARAM_TYPE), 0);
        }
        if (json.has("error_code") && json.has("error_msg")) {
            throw new FacebookError(json.getString("error_msg"), "", Integer.parseInt(json.getString("error_code")));
        }
        if (json.has("error_code")) {
            throw new FacebookError("request failed", "", Integer.parseInt(json.getString("error_code")));
        }
        if (json.has("error_msg")) {
            throw new FacebookError(json.getString("error_msg"));
        }
        if (json.has("error_reason")) {
            throw new FacebookError(json.getString("error_reason"));
        }
        return json;
    }

    @Deprecated
    public static void showAlert(Context context, String title, String text) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(text);
        alertBuilder.create().show();
    }
}
