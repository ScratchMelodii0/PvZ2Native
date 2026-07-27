package com.popcap.SexyAppFramework;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.facebook.android.Facebook;
import com.facebook.internal.ServerProtocol;
import com.google.android.vending.expansion.downloader.Constants;
import com.swrve.sdk.localstorage.SQLiteLocalStorage;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class NanigansDriver {
    private static final String DELIMITER = "::";
    private static final String TAG = "NanigansDriver";
    private static final String mNanigansURL = "http://api.nanigans.com/mobile.php";
    private Context mContext = SexyAppFrameworkActivity.instance();

    public void trackEvent(String facebookAppId, String uid, String type, String name, String[] value) {
        TrackEventInBackgroundTaskRunner task = new TrackEventInBackgroundTaskRunner(facebookAppId, uid, type, name, value);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(task);
    }

    public void trackEvent(String facebookAppId, String uid, String type, String name, String value) {
        if (value != null) {
            trackEvent(facebookAppId, uid, type, name, new String[]{"value", value});
        } else {
            trackEvent(facebookAppId, uid, type, name, new String[0]);
        }
    }

    class TrackEventInBackgroundTaskRunner implements Runnable {
        private String mFacebookAppId;
        private String mName;
        private String mType;
        private String mUid;
        private String[] mValue;

        TrackEventInBackgroundTaskRunner(String facebookAppId, String uid, String type, String name, String[] value) {
            this.mFacebookAppId = facebookAppId;
            this.mUid = uid;
            this.mType = type;
            this.mName = name;
            this.mValue = value;
        }

        class TrackEventInBackgroundTask extends AsyncTask<String, Void, Void> {
            TrackEventInBackgroundTask() {
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(String... params) {
                String fbAppid = params[0];
                String uid = params[1];
                String type = params[2];
                String name = params[3];
                String[] value = params[4].split(NanigansDriver.DELIMITER);
                NanigansDriver.this.sendEvent(fbAppid, uid, type, name, value);
                return null;
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            TrackEventInBackgroundTask task = new TrackEventInBackgroundTask();
            StringBuilder sb = new StringBuilder();
            String delimeter = "";
            String[] arr$ = this.mValue;
            for (String s : arr$) {
                sb.append(delimeter).append(s);
                delimeter = NanigansDriver.DELIMITER;
            }
            String joinedValues = sb.toString();
            task.execute(this.mFacebookAppId, this.mUid, this.mType, this.mName, joinedValues);
        }
    }

    public void sendEvent(String facebookAppId, String uid, String type, String name, String[] extras) {
        Log.v(TAG, "NanigansDriver::sendEvent");
        Uri.Builder queryStringBuilder = new Uri.Builder().appendQueryParameter(ServerProtocol.DIALOG_PARAM_TYPE, type).appendQueryParameter("name", name).appendQueryParameter(SQLiteLocalStorage.COLUMN_USER_ID, uid).appendQueryParameter("fb_app_id", facebookAppId).appendQueryParameter("nan_hash", AndroidUtil.getRandomUUID().toString().replaceAll(Constants.FILENAME_SEQUENCE_SEPARATOR, "")).appendQueryParameter("nan_os", Integer.toString(Build.VERSION.SDK_INT));
        String androidId = AndroidUtil.getAndroidId();
        String deviceId = AndroidUtil.getDeviceId();
        String timezone = Calendar.getInstance().getTimeZone().getDisplayName(false, 0);
        if (androidId != null) {
            queryStringBuilder.appendQueryParameter("nan_aid", androidId);
        }
        if (deviceId != null) {
            queryStringBuilder.appendQueryParameter("nan_did", deviceId);
        }
        if (timezone != null) {
            queryStringBuilder.appendQueryParameter("nan_tz", timezone);
        }
        HashMap<String, String> extraParams = unflattenParams(extras);
        if (type.equalsIgnoreCase("install") || type.equalsIgnoreCase("visit")) {
            String attributionId = getFacebookAttributionId();
            if (attributionId != null) {
                queryStringBuilder.appendQueryParameter("fb_attr_id", attributionId);
            }
            String uniqueParam = AndroidUtil.getRandomUUID().toString().replaceAll(Constants.FILENAME_SEQUENCE_SEPARATOR, "");
            if (extraParams.containsKey("unique")) {
                String uniqueParam2 = extraParams.get("unique");
                uniqueParam = uniqueParam2;
            }
            queryStringBuilder.appendQueryParameter("unique", uniqueParam);
        }
        Iterator<Map.Entry<String, String>> it = extraParams.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> next = it.next();
            String value = next.getValue();
            if (value != null && value.length() > 0) {
                queryStringBuilder.appendQueryParameter(next.getKey(), value);
            }
            it.remove();
        }
        Uri queryString = queryStringBuilder.build();
        String url = "http://api.nanigans.com/mobile.php?" + queryString.getEncodedQuery();
        Log.v(TAG, "Nanigans URL: " + url);
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inStream = entity.getContent();
                inStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        client.getConnectionManager().shutdown();
    }

    private String getFacebookAttributionId() {
        String attributionId = null;
        Cursor cursor = null;
        try {
            try {
                cursor = this.mContext.getContentResolver().query(Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider"), new String[]{Facebook.ATTRIBUTION_ID_COLUMN_NAME}, null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    Log.e(TAG, "Attribution ID not found/empty");
                } else {
                    attributionId = cursor.getString(cursor.getColumnIndex(Facebook.ATTRIBUTION_ID_COLUMN_NAME)).replaceAll(Constants.FILENAME_SEQUENCE_SEPARATOR, "");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching atribution ID");
                if (cursor != null) {
                    cursor.close();
                }
            }
            return attributionId;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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
        return unflattened;
    }
}
