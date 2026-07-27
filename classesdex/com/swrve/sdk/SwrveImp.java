package com.swrve.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import com.swrve.sdk.config.SwrveConfig;
import com.swrve.sdk.localstorage.ILocalStorage;
import com.swrve.sdk.localstorage.MemoryCachedLocalStorage;
import com.swrve.sdk.localstorage.MemoryLocalStorage;
import com.swrve.sdk.localstorage.SQLiteLocalStorage;
import com.swrve.sdk.rest.IRESTClient;
import com.swrve.sdk.rest.IRESTResponseListener;
import com.swrve.sdk.rest.RESTClient;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
abstract class SwrveImp {
    protected static final String APP_LAUNCH_ACTION = "/1/app_launch";
    private static final String ATTRIBUTION_ID_COLUMN_NAME = "aid";
    protected static final String BATCH_EVENTS_ACTION = "/1/batch";
    protected static final String CLICK_THRU_ACTION = "/1/click_thru";
    protected static final String EMPTY_JSON_ARRAY = "[]";
    protected static final String LOG_TAG = "SwrveSDK";
    protected static final String PLATFORM = "Android ";
    protected static final String RESOURCES_CACHE_CATEGORY = "Resources";
    protected static final String RESOURCES_DIFF_CACHE_CATEGORY = "ResourcesDiff";
    protected static final String SEND_IDENTIFIERS_ACTION = "/1/send_identifiers";
    protected static final String SWRVE_ANDROID_DEVICE_XDPI = "swrve.android_device_xdpi";
    protected static final String SWRVE_ANDROID_DEVICE_YDPI = "swrve.android_device_ydpi";
    protected static final String SWRVE_APP_STORE = "swrve.app_store";
    protected static final String SWRVE_DEVICE_DPI = "swrve.device_dpi";
    protected static final String SWRVE_DEVICE_HEIGHT = "swrve.device_height";
    protected static final String SWRVE_DEVICE_NAME = "swrve.device_name";
    protected static final String SWRVE_DEVICE_WIDTH = "swrve.device_width";
    protected static final String SWRVE_LANGUAGE = "swrve.language";
    protected static final String SWRVE_OS = "swrve.os";
    protected static final String SWRVE_OS_VERSION = "swrve.os_version";
    protected static final String SWRVE_SDK_VERSION = "swrve.sdk_version";
    protected static final String USER_RESOURCES_ACTION = "/api/1/user_resources";
    protected static final String USER_RESOURCES_DIFF_ACTION = "/api/1/user_resources_diff";
    protected String apiKey;
    protected String appVersion;
    protected MemoryCachedLocalStorage cachedLocalStorage;
    protected SwrveConfig config;
    protected WeakReference<Context> context;
    protected ISwrveEventListener eventListener;
    protected int gameId;
    protected long installTime;
    protected String language;
    protected long lastSessionTick;
    protected String linkToken;
    protected long newSessionInterval;
    protected IRESTClient restClient;
    protected ExecutorService restClientExecutor;
    protected long sendQueuedEventsInterval;
    protected String sessionToken;
    protected ExecutorService storageExecutor;
    protected String userId;
    protected static String version = "2.1";
    private static final Uri ATTRIBUTION_ID_CONTENT_URI = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
    private static String INSTALL_TIME_CATEGORY = "SwrveSDK.installTime";
    protected ConcurrentHashMap<String, String> linkData = new ConcurrentHashMap<>();
    protected PendingState appLaunchPendingState = new PendingState(false, false);
    protected PendingState linkPendingState = new PendingState(false, false);
    protected PendingState clickThruPendingState = new PendingState(false, false);
    protected boolean destroyed = false;

    protected class PendingState {
        public boolean pending;
        public boolean sending;

        public PendingState(boolean pending, boolean sending) {
            this.pending = pending;
            this.sending = sending;
        }

        public void setPending(boolean pending) {
            this.pending = pending;
        }

        public void setSending(boolean sending) {
            this.sending = sending;
        }

        public boolean isPending() {
            return this.pending;
        }

        public boolean isSending() {
            return this.sending;
        }
    }

    protected static String getFacebookAttributionId(ContentResolver contentResolver) {
        String[] projection = {"aid"};
        Cursor c = contentResolver.query(ATTRIBUTION_ID_CONTENT_URI, projection, null, null, null);
        if (c == null || !c.moveToFirst()) {
            return null;
        }
        String string = c.getString(c.getColumnIndex("aid"));
        c.close();
        return string;
    }

    protected void setAndroidIdMd5() {
        Context contextRef = this.context.get();
        if (contextRef != null) {
            String androidId = Settings.Secure.getString(contextRef.getContentResolver(), "android_id");
            String androidIdMd5 = SwrveHelper.md5(androidId);
            queueLinkId("android_id_md5", androidIdMd5);
        }
    }

    protected void openLocalStorageConnection() {
        ILocalStorage newlocalStorage = createLocalStorage();
        this.cachedLocalStorage.setSecondaryStorage(newlocalStorage);
    }

    protected ILocalStorage createLocalStorage() {
        return new SQLiteLocalStorage(this.context.get(), this.config.getDbName(), this.config.getMaxSqliteDbSize());
    }

    protected IRESTClient createRESTClient() {
        return new RESTClient();
    }

    protected MemoryCachedLocalStorage createCachedLocalStorage() {
        return new MemoryCachedLocalStorage(new MemoryLocalStorage(), null);
    }

    protected ExecutorService createStorageExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    protected ExecutorService createRESTClientExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    protected String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return model.startsWith(manufacturer) ? model : manufacturer + " " + model;
    }

    protected void processUserResourcesData(String resourcesAsJSON, ISwrveUserResourcesListener listener) {
        try {
            JSONArray jsonResources = new JSONArray(resourcesAsJSON);
            Map<String, Map<String, String>> mapResources = new HashMap<>();
            int j = jsonResources.length();
            for (int i = 0; i < j; i++) {
                JSONObject resourceJSON = jsonResources.getJSONObject(i);
                String uid = resourceJSON.getString("uid");
                Map<String, String> resourceMap = SwrveHelper.JSONToMap(resourceJSON);
                mapResources.put(uid, resourceMap);
            }
            listener.onUserResourcesSuccess(mapResources, resourcesAsJSON);
        } catch (Exception exp) {
            listener.onUserResourcesError(exp);
        }
    }

    protected void processUserResourcesDiffData(String resourcesAsJSON, ISwrveUserResourcesDiffListener listener) {
        try {
            JSONArray jsonResourcesDiff = new JSONArray(resourcesAsJSON);
            Map<String, Map<String, String>> mapOldResources = new HashMap<>();
            Map<String, Map<String, String>> mapNewResources = new HashMap<>();
            int j = jsonResourcesDiff.length();
            for (int i = 0; i < j; i++) {
                Map<String, String> mapOldResourceValues = new HashMap<>();
                Map<String, String> mapNewResourceValues = new HashMap<>();
                JSONObject resourceJSON = jsonResourcesDiff.getJSONObject(i);
                String uid = resourceJSON.getString("uid");
                JSONObject resourceDiffsJSON = resourceJSON.getJSONObject("diff");
                Iterator<String> it = resourceDiffsJSON.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    mapOldResourceValues.put(key, resourceDiffsJSON.getJSONObject(key).getString("old"));
                    mapNewResourceValues.put(key, resourceDiffsJSON.getJSONObject(key).getString("new"));
                }
                mapOldResources.put(uid, mapOldResourceValues);
                mapNewResources.put(uid, mapNewResourceValues);
            }
            listener.onUserResourcesDiffSuccess(mapOldResources, mapNewResources, resourcesAsJSON);
        } catch (Exception exp) {
            listener.onUserResourcesDiffError(exp);
        }
    }

    protected void queueLinkId(String custom_id, String id) {
        try {
            synchronized (this.linkData) {
                this.linkData.put(custom_id, id);
                synchronized (this.linkPendingState) {
                    this.linkPendingState.setPending(true);
                }
            }
        } catch (Exception e) {
        }
    }

    protected void notifySentToLinkServer() {
        synchronized (this.linkPendingState) {
            this.linkPendingState.setPending(false);
            this.linkPendingState.setSending(false);
        }
    }

    protected void notifySentToLinkServerError() {
        synchronized (this.linkPendingState) {
            this.linkPendingState.setPending(true);
            this.linkPendingState.setSending(false);
        }
    }

    protected void notifySentAppLaunch() {
        synchronized (this.appLaunchPendingState) {
            this.appLaunchPendingState.setPending(false);
            this.appLaunchPendingState.setSending(false);
        }
    }

    protected void notifySentAppLaunchError() {
        synchronized (this.appLaunchPendingState) {
            this.appLaunchPendingState.setPending(true);
            this.appLaunchPendingState.setSending(false);
        }
    }

    protected void notifySentClickThru() {
        synchronized (this.clickThruPendingState) {
            this.clickThruPendingState.setPending(false);
            this.clickThruPendingState.setSending(false);
        }
    }

    protected void notifySentClickThruError() {
        synchronized (this.clickThruPendingState) {
            this.clickThruPendingState.setPending(true);
            this.clickThruPendingState.setSending(false);
        }
    }

    protected long getInstallTime() {
        long installTime = new Date().getTime();
        try {
            String installTimeRaw = this.cachedLocalStorage.getCacheEntryForUser(INSTALL_TIME_CATEGORY, INSTALL_TIME_CATEGORY);
            if (installTimeRaw != null) {
                installTime = Long.parseLong(installTimeRaw);
            } else {
                this.cachedLocalStorage.setCacheEntryForUser(INSTALL_TIME_CATEGORY, INSTALL_TIME_CATEGORY, String.valueOf(installTime));
                ILocalStorage secondaryStorage = this.cachedLocalStorage.getSecondaryStorage();
                if (secondaryStorage != null) {
                    secondaryStorage.setCacheEntryForUser(INSTALL_TIME_CATEGORY, INSTALL_TIME_CATEGORY, String.valueOf(installTime));
                }
            }
        } catch (Exception exp) {
            Log.e(LOG_TAG, "Could not get or save install time", exp);
        }
        return installTime;
    }

    protected String getLinkRequestBody() {
        String result = null;
        synchronized (this.linkPendingState) {
            if (this.linkPendingState.isPending() && !this.linkPendingState.isSending()) {
                try {
                    JSONObject request = SwrveHelper.mapToJSONObject(this.linkData);
                    request.put("api_key", this.apiKey);
                    request.put("user", this.userId);
                    result = request.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private class QueueEventRunnable implements Runnable {
        private String eventName;
        private Map<String, Object> parameters;
        private Map<String, String> payload;

        public QueueEventRunnable(String eventName, Map<String, Object> parameters, Map<String, String> payload) {
            this.eventName = eventName;
            this.parameters = parameters;
            this.payload = payload;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                String eventString = Event.eventAsJSON(this.eventName, this.parameters, this.payload);
                this.parameters = null;
                this.payload = null;
                SwrveImp.this.cachedLocalStorage.addEvent(eventString);
                Log.i(SwrveImp.LOG_TAG, this.eventName + " event queued");
            } catch (JSONException je) {
                Log.e(SwrveImp.LOG_TAG, "Parameter or payload data not encodable as JSON", je);
            } catch (Exception se) {
                Log.e(SwrveImp.LOG_TAG, "Unable to insert into local storage", se);
            }
            SwrveImp.this.taskCompleted();
        }
    }

    protected void queueEvent(String eventName, Map<String, Object> parameters, Map<String, String> payload) {
        try {
            storageExecutorExecute(new QueueEventRunnable(eventName, parameters, payload));
        } catch (Exception exp) {
            Log.e(LOG_TAG, "Unable to queue event", exp);
        }
    }

    protected void postBatchRequest(SwrveConfig config, String postData, final IPostBatchRequestListener listener) {
        this.restClient.post(config.getEventsUrl() + BATCH_EVENTS_ACTION, postData, new IRESTResponseListener() { // from class: com.swrve.sdk.SwrveImp.1
            @Override // com.swrve.sdk.rest.IRESTResponseListener
            public void onResponse(int responseCode, String responseBody) {
                listener.onResponse(responseCode != 503);
                if (responseCode != 200) {
                    Log.e(SwrveImp.LOG_TAG, "postBatchRequest failed with code: " + responseCode + " and body: " + responseBody);
                }
            }

            @Override // com.swrve.sdk.rest.IRESTResponseListener
            public void onException(Exception exp) {
            }
        });
    }

    protected void trySendAppLaunch() {
        boolean sendAppLaunch;
        if (this.userId != null) {
            try {
                synchronized (this.appLaunchPendingState) {
                    sendAppLaunch = this.appLaunchPendingState.isPending() && !this.appLaunchPendingState.isSending();
                    if (sendAppLaunch) {
                        this.appLaunchPendingState.setSending(true);
                    }
                }
                if (sendAppLaunch) {
                    Log.i(LOG_TAG, "Sending app launch");
                    restClientExecutorExecute(new Runnable() { // from class: com.swrve.sdk.SwrveImp.2
                        @Override // java.lang.Runnable
                        public void run() {
                            Map<String, String> params = new HashMap<>();
                            params.put("api_key", SwrveImp.this.apiKey);
                            params.put("user", SwrveImp.this.userId);
                            params.put("app_version", SwrveImp.this.appVersion);
                            params.put("link_token", SwrveImp.this.linkToken);
                            try {
                                SwrveImp.this.restClient.get(SwrveImp.this.config.getLinkUrl() + SwrveImp.APP_LAUNCH_ACTION, params, new IRESTResponseListener() { // from class: com.swrve.sdk.SwrveImp.2.1
                                    @Override // com.swrve.sdk.rest.IRESTResponseListener
                                    public void onResponse(int responseStatus, String responseBody) {
                                        if (responseStatus == 200) {
                                            Log.i(SwrveImp.LOG_TAG, "Click thru succesfully sent");
                                        }
                                        if (responseStatus == 200 || responseStatus == 400 || responseStatus == 401 || responseStatus == 403 || responseStatus == 410) {
                                            SwrveImp.this.notifySentAppLaunch();
                                            Map<String, Object> parameters = new HashMap<>();
                                            parameters.put("name", "Swrve.Messages.app_launch");
                                            SwrveImp.this.queueEvent(SQLiteLocalStorage.COLUMN_EVENT, parameters, null);
                                            return;
                                        }
                                        SwrveImp.this.notifySentAppLaunchError();
                                    }

                                    @Override // com.swrve.sdk.rest.IRESTResponseListener
                                    public void onException(Exception exp) {
                                        SwrveImp.this.notifySentAppLaunchError();
                                    }
                                });
                                SwrveImp.this.appLaunchPendingState.setSending(false);
                            } catch (Exception e) {
                                SwrveImp.this.notifySentAppLaunchError();
                            }
                            SwrveImp.this.taskCompleted();
                        }
                    });
                }
            } catch (Exception exp) {
                Log.e(LOG_TAG, "Could not send app launch", exp);
            }
        }
    }

    protected void trySendClickThru() {
        boolean sendClickThru;
        if (this.userId != null) {
            try {
                synchronized (this.clickThruPendingState) {
                    sendClickThru = this.clickThruPendingState.isPending() && !this.clickThruPendingState.isSending();
                    if (sendClickThru) {
                        this.clickThruPendingState.setSending(true);
                    }
                }
                if (sendClickThru) {
                    final Map<ILocalStorage, Map<Long, Map.Entry<Integer, String>>> combinedClickThrus = this.cachedLocalStorage.getCombinedFirstNClickThrus(Integer.valueOf(this.config.getMaxClickThrusPerFlush()));
                    if (!combinedClickThrus.isEmpty()) {
                        Log.i(LOG_TAG, "Sending click thru");
                        restClientExecutorExecute(new Runnable() { // from class: com.swrve.sdk.SwrveImp.3
                            @Override // java.lang.Runnable
                            public void run() {
                                for (final ILocalStorage storage : combinedClickThrus.keySet()) {
                                    Map<Long, Map.Entry<Integer, String>> clickThrus = (Map) combinedClickThrus.get(storage);
                                    for (final Long clickThruId : clickThrus.keySet()) {
                                        final Map.Entry<Integer, String> clickThru = clickThrus.get(clickThruId);
                                        Map<String, String> params = new HashMap<>();
                                        params.put("api_key", SwrveImp.this.apiKey);
                                        params.put("user", SwrveImp.this.userId);
                                        params.put("link_token", SwrveImp.this.linkToken);
                                        params.put("destination", clickThru.getKey().toString());
                                        params.put(SQLiteLocalStorage.COLUMN_SOURCE, clickThru.getValue());
                                        try {
                                            SwrveImp.this.restClient.get(SwrveImp.this.config.getLinkUrl() + SwrveImp.CLICK_THRU_ACTION, params, new IRESTResponseListener() { // from class: com.swrve.sdk.SwrveImp.3.1
                                                @Override // com.swrve.sdk.rest.IRESTResponseListener
                                                public void onResponse(int responseStatus, String responseBody) {
                                                    if (responseStatus == 200) {
                                                        Log.i(SwrveImp.LOG_TAG, "Click thru succesfully sent");
                                                    }
                                                    if (responseStatus == 200 || responseStatus == 400 || responseStatus == 401 || responseStatus == 403 || responseStatus == 410) {
                                                        SwrveImp.this.notifySentClickThru();
                                                        storage.removeClickThrusById(clickThruId.longValue());
                                                        HashMap map = new HashMap();
                                                        map.put("destination", ((Integer) clickThru.getKey()).toString());
                                                        map.put(SQLiteLocalStorage.COLUMN_SOURCE, clickThru.getValue());
                                                        Map<String, Object> parameters = new HashMap<>();
                                                        parameters.put("name", "Swrve.Messages.click_thru");
                                                        SwrveImp.this.queueEvent(SQLiteLocalStorage.COLUMN_EVENT, parameters, map);
                                                        return;
                                                    }
                                                    SwrveImp.this.notifySentClickThruError();
                                                }

                                                @Override // com.swrve.sdk.rest.IRESTResponseListener
                                                public void onException(Exception exp) {
                                                    SwrveImp.this.notifySentClickThruError();
                                                }
                                            });
                                            SwrveImp.this.clickThruPendingState.setSending(false);
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                                SwrveImp.this.taskCompleted();
                            }
                        });
                    }
                }
            } catch (Exception exp) {
                Log.e(LOG_TAG, "Could not send click thru", exp);
            }
        }
    }

    protected boolean restClientExecutorExecute(Runnable runnable) {
        try {
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while scheduling a rest execution", e);
        }
        if (this.restClientExecutor.isShutdown()) {
            Log.i(LOG_TAG, "Trying to schedule a rest execution while shutdown");
            return false;
        }
        this.restClientExecutor.execute(runnable);
        return true;
    }

    protected boolean storageExecutorExecute(Runnable runnable) {
        try {
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while scheduling a storage execution", e);
        }
        if (this.storageExecutor.isShutdown()) {
            Log.i(LOG_TAG, "Trying to schedule a storage execution while shutdown");
            return false;
        }
        this.storageExecutor.execute(runnable);
        return true;
    }

    protected long getSessionTime() {
        return new Date().getTime();
    }

    protected void generateNewSessionInterval() {
        this.lastSessionTick = getSessionTime() + this.newSessionInterval;
    }

    protected void taskCompleted() {
    }
}
