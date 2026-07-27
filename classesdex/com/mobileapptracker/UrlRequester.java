package com.mobileapptracker;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONObject;
import org.json.JSONTokener;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class UrlRequester {
    private HttpClient a;

    public UrlRequester() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        BasicHttpParams basicHttpParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(basicHttpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(basicHttpParams, "UTF-8");
        HttpConnectionParams.setSocketBufferSize(basicHttpParams, 8192);
        HttpConnectionParams.setConnectionTimeout(basicHttpParams, 60000);
        HttpConnectionParams.setSoTimeout(basicHttpParams, 60000);
        this.a = new DefaultHttpClient(new ThreadSafeClientConnManager(basicHttpParams, schemeRegistry), basicHttpParams);
    }

    public JSONObject requestUrl(String url, JSONObject json) {
        HttpResponse httpResponseExecute;
        if (json == null || json.length() == 0) {
            try {
                httpResponseExecute = this.a.execute(new HttpGet(url));
            } catch (Exception e) {
                e.printStackTrace();
                httpResponseExecute = null;
            }
        } else {
            try {
                StringEntity stringEntity = new StringEntity(json.toString());
                stringEntity.setContentType("application/json");
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(stringEntity);
                httpResponseExecute = this.a.execute(httpPost);
            } catch (Exception e2) {
                e2.printStackTrace();
                httpResponseExecute = null;
            }
        }
        if (httpResponseExecute != null) {
            try {
                StatusLine statusLine = httpResponseExecute.getStatusLine();
                if (statusLine.getStatusCode() == 200) {
                    Log.d("MobileAppTracker", "Request was sent");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponseExecute.getEntity().getContent(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    while (true) {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line).append("\n");
                    }
                    return sb.length() > 0 ? new JSONObject(new JSONTokener(sb.toString())) : new JSONObject();
                }
                Log.d("MobileAppTracker", "Request failed with status " + statusLine.getStatusCode());
                httpResponseExecute.getEntity().getContent().close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
        return null;
    }
}
