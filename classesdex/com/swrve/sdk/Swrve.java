package com.swrve.sdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.swrve.sdk.config.SwrveConfig;
import com.swrve.sdk.exceptions.NoUserIdSwrveException;
import com.swrve.sdk.localstorage.ILocalStorage;
import com.swrve.sdk.localstorage.SQLiteLocalStorage;
import com.swrve.sdk.rest.IRESTResponseListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class Swrve extends SwrveImp {
    public void init(final Context context, int gameId, String apiKey, String userId, final SwrveConfig config) {
        try {
            config.generateUrls(gameId);
            this.lastSessionTick = getSessionTime();
            this.context = new WeakReference<>(context);
            this.gameId = gameId;
            this.apiKey = apiKey;
            this.userId = userId;
            if (config.getLanguage() == null) {
                this.language = Locale.getDefault().toString();
            } else {
                this.language = config.getLanguage();
            }
            this.config = config;
            this.linkToken = config.getLinkToken();
            this.appVersion = config.getAppVersion();
            this.newSessionInterval = config.getNewSessionInterval();
            this.sendQueuedEventsInterval = config.getSendQueuedEventsInterval();
            if (this.userId != null) {
                this.sessionToken = SwrveHelper.generateSessionToken(this.apiKey, this.gameId, userId);
            }
            if (this.appVersion == null) {
                Log.i("SwrveSDK", "Getting app version automatically");
                try {
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    this.appVersion = pInfo.versionName;
                } catch (Exception exp) {
                    Log.e("SwrveSDK", "Get app version failed", exp);
                }
            }
            if (this.linkToken == null) {
                Log.i("SwrveSDK", "Generating link token");
                String androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");
                this.linkToken = SwrveHelper.generateUUID(androidId);
            }
            this.restClient = createRESTClient();
            this.cachedLocalStorage = createCachedLocalStorage();
            this.storageExecutor = createStorageExecutor();
            this.restClientExecutor = createRESTClientExecutor();
            storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.1
                @Override // java.lang.Runnable
                public void run() {
                    Swrve.this.openLocalStorageConnection();
                    Swrve.this.taskCompleted();
                }
            });
            storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.2
                @Override // java.lang.Runnable
                public void run() {
                    WifiInfo wifiInf;
                    Swrve.this.installTime = Swrve.this.getInstallTime();
                    Log.i("SwrveSDK", "Setting automatic Link IDs");
                    int rules = config.getIdentifierRules();
                    if ((rules & 4) == 0) {
                        Swrve.this.linkPendingState.setPending(true);
                        try {
                            Swrve.this.setAndroidIdMd5();
                        } catch (Exception exp2) {
                            Log.w("SwrveSDK", "Set Android Id MD5 failed", exp2);
                        }
                    }
                    if ((rules & 2) == 0) {
                        Swrve.this.linkPendingState.setPending(true);
                        try {
                            Swrve.this.setFacebookAppAttribution(SwrveImp.getFacebookAttributionId(context.getContentResolver()));
                        } catch (Exception exp3) {
                            Log.w("SwrveSDK", "Set Facebook App Attribution Id failed", exp3);
                        }
                    }
                    if ((rules & 1) == 0) {
                        Swrve.this.linkPendingState.setPending(true);
                        try {
                            WifiManager wifiMan = (WifiManager) context.getSystemService("wifi");
                            if (wifiMan != null && (wifiInf = wifiMan.getConnectionInfo()) != null) {
                                String macAddr = wifiInf.getMacAddress();
                                Swrve.this.setMacAddressMD5(SwrveHelper.md5(macAddr));
                            }
                        } catch (Exception exp4) {
                            Log.w("SwrveSDK", "Set MAC address MD5 failed", exp4);
                        }
                    }
                    if (Swrve.this.userId != null) {
                        Swrve.this.appLaunchPendingState.setPending(true);
                    }
                    Swrve.this.sendDeviceInfoNow();
                    Swrve.this.taskCompleted();
                }
            });
            startSendEventsTimer();
            Log.i("SwrveSDK", "Init finished");
        } catch (Exception exp2) {
            Log.e("SwrveSDK", "Swrve init failed", exp2);
        }
    }

    public void sessionStart() {
        queueEvent("session_start", null, null);
        storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.3
            @Override // java.lang.Runnable
            public void run() {
                Swrve.this.sendQueuedEvents();
                Swrve.this.taskCompleted();
            }
        });
    }

    public void sessionEnd() {
        queueEvent("session_end", null, null);
    }

    public void event(String name, Map<String, String> payload) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        queueEvent(SQLiteLocalStorage.COLUMN_EVENT, parameters, payload);
        if (this.eventListener != null) {
            this.eventListener.onEvent(name);
        }
    }

    public void purchase(String item, String currency, int cost, int quantity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("item", item);
        parameters.put("currency", currency);
        parameters.put("cost", Integer.toString(cost));
        parameters.put("quantity", Integer.toString(quantity));
        queueEvent("purchase", parameters, null);
    }

    public void currencyGiven(String givenCurrency, double givenAmount) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("given_currency", givenCurrency);
        parameters.put("given_amount", Double.toString(givenAmount));
        queueEvent("currency_given", parameters, null);
    }

    public void userUpdate(Map<String, String> attributes) {
        Map<String, Object> parameters = new HashMap<>();
        try {
            parameters.put("attributes", SwrveHelper.mapToJSONObject(attributes));
        } catch (JSONException ex) {
            Log.e("SwrveSDK", "JSONException when encoding user attributes", ex);
        }
        queueEvent("user", parameters, null);
    }

    public void buyIn(String rewardCurrency, int rewardAmount, double localCost, String localCurrency, String paymentProvider) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reward_currency", rewardCurrency);
        parameters.put("reward_amount", Integer.toString(rewardAmount));
        parameters.put("local_cost", Double.toString(localCost));
        parameters.put("local_currency", localCurrency);
        parameters.put("payment_provider", paymentProvider);
        queueEvent("buy_in", parameters, null);
    }

    public void getUserResources(final ISwrveUserResourcesListener listener) {
        try {
            restClientExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.4
                @Override // java.lang.Runnable
                public void run() {
                    if (Swrve.this.userId != null) {
                        Map<String, String> params = new HashMap<>();
                        params.put("api_key", Swrve.this.apiKey);
                        params.put("user", Swrve.this.userId);
                        params.put("app_version", Swrve.this.appVersion);
                        params.put("joined", String.valueOf(Swrve.this.installTime));
                        try {
                            Log.i("SwrveSDK", "Contacting AB Test server " + Swrve.this.config.getContentUrl());
                            Swrve.this.restClient.get(Swrve.this.config.getContentUrl() + "/api/1/user_resources", params, new RESTCacheResponseListener(Swrve.this.cachedLocalStorage, Swrve.this.userId, "Resources", "[]") { // from class: com.swrve.sdk.Swrve.4.1
                                @Override // com.swrve.sdk.RESTCacheResponseListener
                                public void onResponseCached(int responseStatus, String rawResponse) {
                                    Log.i("SwrveSDK", "Got AB Test response code " + responseStatus);
                                    if (rawResponse != null && !rawResponse.equals("")) {
                                        Swrve.this.processUserResourcesData(rawResponse, listener);
                                    }
                                }

                                @Override // com.swrve.sdk.rest.IRESTResponseListener
                                public void onException(Exception exp) {
                                    Log.e("SwrveSDK", "AB Test exception", exp);
                                    listener.onUserResourcesError(exp);
                                }
                            });
                        } catch (Exception exp) {
                            Log.e("SwrveSDK", "AB Test exception", exp);
                            listener.onUserResourcesError(exp);
                        }
                    } else {
                        Log.e("SwrveSDK", "Error: No user specified");
                        listener.onUserResourcesError(new NoUserIdSwrveException());
                    }
                    Swrve.this.taskCompleted();
                }
            });
        } catch (Exception exp) {
            Log.e("SwrveSDK", "Get user resources failed", exp);
        }
    }

    public void getUserResourcesDiff(final ISwrveUserResourcesDiffListener listener) {
        try {
            restClientExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.5
                @Override // java.lang.Runnable
                public void run() {
                    if (Swrve.this.userId != null) {
                        Map<String, String> params = new HashMap<>();
                        params.put("api_key", Swrve.this.apiKey);
                        params.put("user", Swrve.this.userId);
                        params.put("app_version", Swrve.this.appVersion);
                        params.put("joined", String.valueOf(Swrve.this.installTime));
                        try {
                            Log.i("SwrveSDK", "Contacting AB Test server " + Swrve.this.config.getContentUrl());
                            Swrve.this.restClient.get(Swrve.this.config.getContentUrl() + "/api/1/user_resources_diff", params, new RESTCacheResponseListener(Swrve.this.cachedLocalStorage, Swrve.this.userId, "ResourcesDiff", "[]") { // from class: com.swrve.sdk.Swrve.5.1
                                @Override // com.swrve.sdk.RESTCacheResponseListener
                                public void onResponseCached(int responseStatus, String rawResponse) {
                                    Log.i("SwrveSDK", "Got AB Test response code " + responseStatus);
                                    if (rawResponse != null && !rawResponse.equals("")) {
                                        Swrve.this.processUserResourcesDiffData(rawResponse, listener);
                                    }
                                }

                                @Override // com.swrve.sdk.rest.IRESTResponseListener
                                public void onException(Exception exp) {
                                    Log.e("SwrveSDK", "AB Test exception", exp);
                                    listener.onUserResourcesDiffError(exp);
                                }
                            });
                        } catch (Exception exp) {
                            Log.e("SwrveSDK", "AB Test exception", exp);
                            listener.onUserResourcesDiffError(exp);
                        }
                    } else {
                        Log.e("SwrveSDK", "Error: No user specified");
                        listener.onUserResourcesDiffError(new NoUserIdSwrveException());
                    }
                    Swrve.this.taskCompleted();
                }
            });
        } catch (Exception exp) {
            Log.e("SwrveSDK", "Get user resources diff failed", exp);
        }
    }

    public void sendQueuedEvents() {
        if (this.userId != null) {
            try {
                restClientExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.6
                    @Override // java.lang.Runnable
                    public void run() {
                        final Map<ILocalStorage, Map<Long, String>> combinedEvents = Swrve.this.cachedLocalStorage.getCombinedFirstNEvents(Integer.valueOf(Swrve.this.config.getMaxEventsPerFlush()));
                        final HashMap<Long, String> events = new HashMap<>();
                        if (!combinedEvents.isEmpty()) {
                            Log.i("SwrveSDK", "Sending queued events");
                            try {
                                Iterator<ILocalStorage> storageIt = combinedEvents.keySet().iterator();
                                while (storageIt.hasNext()) {
                                    events.putAll(combinedEvents.get(storageIt.next()));
                                }
                                Log.i("SwrveSDK", "Sending " + events.size() + " events to Swrve");
                                String data = Event.eventsAsBatch(Swrve.this.userId, Swrve.this.appVersion, Swrve.this.sessionToken, events.values());
                                Swrve.this.postBatchRequest(Swrve.this.config, data, new IPostBatchRequestListener() { // from class: com.swrve.sdk.Swrve.6.1
                                    @Override // com.swrve.sdk.IPostBatchRequestListener
                                    public void onResponse(boolean shouldDelete) {
                                        if (shouldDelete) {
                                            Log.i("SwrveSDK", String.valueOf(events.size()) + " events sent to Swrve");
                                            for (ILocalStorage storage : combinedEvents.keySet()) {
                                                storage.removeEventsById(((Map) combinedEvents.get(storage)).keySet());
                                            }
                                            return;
                                        }
                                        Log.e("SwrveSDK", "Batch of events could not be sent, retrying");
                                    }
                                });
                            } catch (JSONException je) {
                                Log.e("SwrveSDK", "Unable to generate event batch", je);
                            }
                        }
                        String linkRequestBody = Swrve.this.getLinkRequestBody();
                        if (linkRequestBody != null) {
                            Log.i("SwrveSDK", "Sending link ids");
                            Swrve.this.linkPendingState.setSending(true);
                            Swrve.this.restClient.post(Swrve.this.config.getLinkUrl() + "/1/send_identifiers", linkRequestBody, new IRESTResponseListener() { // from class: com.swrve.sdk.Swrve.6.2
                                @Override // com.swrve.sdk.rest.IRESTResponseListener
                                public void onResponse(int responseStatus, String responseBody) {
                                    if (responseStatus == 200) {
                                        Log.i("SwrveSDK", "Link Ids succesfully sent");
                                        Swrve.this.notifySentToLinkServer();
                                    } else if (responseStatus == 400 || responseStatus == 401 || responseStatus == 403 || responseStatus == 410) {
                                        Swrve.this.notifySentToLinkServer();
                                    }
                                }

                                @Override // com.swrve.sdk.rest.IRESTResponseListener
                                public void onException(Exception exp) {
                                    Swrve.this.notifySentToLinkServerError();
                                }
                            });
                            Swrve.this.linkPendingState.setSending(false);
                        }
                        Swrve.this.trySendAppLaunch();
                        Swrve.this.trySendClickThru();
                        Swrve.this.taskCompleted();
                    }
                });
            } catch (Exception exp) {
                Log.e("SwrveSDK", "Send queued events failed", exp);
            }
        }
    }

    public void flushToDisk() {
        try {
            storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.7
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        Log.i("SwrveSDK", "Flushing to disk");
                        Swrve.this.cachedLocalStorage.flush();
                        Swrve.this.taskCompleted();
                    } catch (Exception e) {
                        Log.e("SwrveSDK", "Flush to disk failed", e);
                    }
                }
            });
        } catch (Exception exp) {
            Log.e("SwrveSDK", "Flush to disk failed", exp);
        }
    }

    public void clickThru(final int targetGameId, final String source) {
        try {
            storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.8
                @Override // java.lang.Runnable
                public void run() {
                    synchronized (Swrve.this.clickThruPendingState) {
                        Swrve.this.clickThruPendingState.setPending(true);
                    }
                    Swrve.this.cachedLocalStorage.addClickThru(targetGameId, source);
                    Swrve.this.restClientExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.8.1
                        @Override // java.lang.Runnable
                        public void run() {
                            Swrve.this.trySendClickThru();
                            Swrve.this.taskCompleted();
                        }
                    });
                    Swrve.this.taskCompleted();
                }
            });
        } catch (Exception exp) {
            Log.e("SwrveSDK", "Click thru failed", exp);
        }
    }

    public void sendAndroidId() {
        Context contextRef = this.context.get();
        if (contextRef != null) {
            try {
                String androidId = Settings.Secure.getString(contextRef.getContentResolver(), "android_id");
                queueLinkId("android_id", androidId);
            } catch (Exception exp) {
                Log.e("SwrveSDK", "Send Android Id failed", exp);
            }
        }
    }

    public void setTpid(String tpid) {
        queueLinkId("tpid", tpid);
    }

    public void setCustomID(String customID) {
        queueLinkId("custom_id", customID);
    }

    public void setOdin1(String odin1) {
        queueLinkId("odin1", odin1);
    }

    public void setMacAddress(String mac_address) {
        queueLinkId("mac_address", mac_address);
    }

    public void setMacAddressSHA1(String mac_address_sha1) {
        queueLinkId("mac_address_sha1", mac_address_sha1);
    }

    public void setMacAddressMD5(String mac_address_md5) {
        queueLinkId("mac_address_md5", mac_address_md5);
    }

    public void setFacebookAppAttribution(String fb_app_attribution) {
        queueLinkId("fb_app_attribution", fb_app_attribution);
    }

    public void setIMEIMD5(String imei_md5) {
        queueLinkId("imei_md5", imei_md5);
    }

    public void onPause() {
        try {
            Log.i("SwrveSDK", "onPause");
            flushToDisk();
            generateNewSessionInterval();
        } catch (Exception exp) {
            Log.e("SwrveSDK", "onPause failed", exp);
        }
    }

    public void onResume() {
        try {
            Log.i("SwrveSDK", "onResume");
            long currentTime = getSessionTime();
            if (currentTime > this.lastSessionTick) {
                sessionStart();
            } else {
                sendQueuedEvents();
            }
            generateNewSessionInterval();
        } catch (Exception exp) {
            Log.e("SwrveSDK", "onResume failed", exp);
        }
    }

    public void onDestroy() {
        try {
            Log.i("SwrveSDK", "onDestroy");
            this.destroyed = true;
            this.restClientExecutor.shutdown();
            this.restClientExecutor = null;
            storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.9
                @Override // java.lang.Runnable
                public void run() {
                    Swrve.this.cachedLocalStorage.close();
                    Swrve.this.restClient = null;
                    Swrve.this.cachedLocalStorage = null;
                    Swrve.this.linkData = null;
                    Swrve.this.config = null;
                    Swrve.this.taskCompleted();
                }
            });
            this.storageExecutor.shutdown();
            this.storageExecutor = null;
        } catch (Exception exp) {
            Log.e("SwrveSDK", "onDestroy failed", exp);
        }
    }

    public static String getVersion() {
        return version;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    protected void sendDeviceInfoNow() {
        storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.10
            @Override // java.lang.Runnable
            public void run() {
                Log.i("SwrveSDK", "Sending device info");
                Map<String, String> autoUserUpdate = new HashMap<>();
                autoUserUpdate.put("swrve.device_name", Swrve.this.getDeviceName());
                autoUserUpdate.put("swrve.os", "Android");
                autoUserUpdate.put("swrve.os_version", Build.VERSION.RELEASE);
                Context contextRef = Swrve.this.context.get();
                if (contextRef != null) {
                    try {
                        Display display = ((WindowManager) contextRef.getSystemService("window")).getDefaultDisplay();
                        DisplayMetrics metrics = new DisplayMetrics();
                        int width = display.getWidth();
                        int height = display.getHeight();
                        display.getMetrics(metrics);
                        float xdpi = metrics.xdpi;
                        float ydpi = metrics.ydpi;
                        if (width > height) {
                            width = height;
                            height = width;
                            xdpi = ydpi;
                            ydpi = xdpi;
                        }
                        autoUserUpdate.put("swrve.device_width", String.valueOf(width));
                        autoUserUpdate.put("swrve.device_height", String.valueOf(height));
                        autoUserUpdate.put("swrve.device_dpi", String.valueOf(metrics.densityDpi));
                        autoUserUpdate.put("swrve.android_device_xdpi", String.valueOf(xdpi));
                        autoUserUpdate.put("swrve.android_device_ydpi", String.valueOf(ydpi));
                    } catch (Exception exp) {
                        Log.e("SwrveSDK", "Get device screen info failed", exp);
                    }
                    autoUserUpdate.put("swrve.language", Swrve.this.language);
                    autoUserUpdate.put("swrve.sdk_version", "Android " + Swrve.version);
                    autoUserUpdate.put("swrve.app_store", Swrve.this.config.getAppStore().toArgument());
                    Swrve.this.userUpdate(autoUserUpdate);
                }
                Swrve.this.taskCompleted();
            }
        });
        storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.Swrve.11
            @Override // java.lang.Runnable
            public void run() {
                Swrve.this.sendQueuedEvents();
                Swrve.this.taskCompleted();
            }
        });
    }

    protected void startSendEventsTimer() {
        if (this.sendQueuedEventsInterval != 0) {
            try {
                ExecutorService sendEventsThread = Executors.newSingleThreadExecutor();
                sendEventsThread.execute(new SwrveSendEventsRunnable(this.context.get(), this, sendEventsThread, this.sendQueuedEventsInterval));
            } catch (Exception e) {
                Log.e("SwrveSDK", "Automatic send queued events failed.", e);
            }
        }
    }
}
