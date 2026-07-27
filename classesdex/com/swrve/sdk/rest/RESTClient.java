package com.swrve.sdk.rest;

import android.util.Log;
import com.swrve.sdk.SwrveHelper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class RESTClient implements IRESTClient {
    private static final String CHARSET = "UTF-8";
    private static final int MIN_ERROR_CODE = 400;

    @Override // com.swrve.sdk.rest.IRESTClient
    public void get(String endpoint, IRESTResponseListener callback) {
        HttpURLConnection urlConnection = null;
        String responseBody = null;
        int responseCode = IRESTClient.ERROR_CODE;
        try {
            try {
                URL url = new URL(endpoint);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(15000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept-Charset", CHARSET);
                responseCode = urlConnection.getResponseCode();
                if (responseCode < 400) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    responseBody = getResponseBody(in);
                } else {
                    InputStream in2 = new BufferedInputStream(urlConnection.getErrorStream());
                    responseBody = getResponseBody(in2);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onException(e);
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (callback != null) {
                callback.onResponse(responseCode, responseBody);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override // com.swrve.sdk.rest.IRESTClient
    public void get(String endpoint, Map<String, String> params, IRESTResponseListener callback) throws UnsupportedEncodingException {
        get(endpoint + "?" + SwrveHelper.encodeParameters(params), callback);
    }

    @Override // com.swrve.sdk.rest.IRESTClient
    public void post(String endpoint, String encodedBody, IRESTResponseListener callback) {
        post(endpoint, encodedBody, callback, "application/json");
    }

    @Override // com.swrve.sdk.rest.IRESTClient
    public void post(String endpoint, String encodedBody, IRESTResponseListener callback, String contentType) {
        HttpURLConnection urlConnection = null;
        String responseBody = null;
        int responseCode = IRESTClient.ERROR_CODE;
        try {
            try {
                byte[] bytes = encodedBody.getBytes(CHARSET);
                URL url = new URL(endpoint);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(15000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", contentType);
                urlConnection.setRequestProperty("Accept-Charset", CHARSET);
                urlConnection.setFixedLengthStreamingMode(bytes.length);
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                Log.i("SwrveSDK", "Url: " + urlConnection.toString());
                Log.i("SwrveSDK", "Body: " + encodedBody);
                OutputStream os = urlConnection.getOutputStream();
                os.write(bytes);
                os.close();
                responseCode = urlConnection.getResponseCode();
                if (responseCode < 400) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    responseBody = getResponseBody(in);
                } else {
                    InputStream in2 = new BufferedInputStream(urlConnection.getErrorStream());
                    responseBody = getResponseBody(in2);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onException(e);
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (callback != null) {
                callback.onResponse(responseCode, responseBody);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static String getResponseBody(InputStream is) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder body = new StringBuilder();
        while (true) {
            try {
                String line = rd.readLine();
                if (line == null) {
                    break;
                }
                body.append(line);
            } catch (IOException e) {
            }
        }
        return body.toString();
    }
}
