package com.swrve.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;
import com.swrve.sdk.executor.PausableExecutor;
import com.swrve.sdk.localstorage.MemoryCachedLocalStorage;
import com.swrve.sdk.localstorage.SQLiteLocalStorage;
import com.swrve.sdk.messaging.SwrveCampaign;
import com.swrve.sdk.rest.IRESTClient;
import com.swrve.sdk.rest.IRESTResponseListener;
import com.swrve.sdk.rest.RESTClient;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
abstract class SwrveTalkImpl {
    protected static final String CAMPAIGN_ACTION = "/api/1/campaigns";
    protected static final String CAMPAIGN_CATEGORY = "SwrveCampaign";
    protected static final String CAMPAIGN_SETTINGS_CATEGORY = "SwrveCampaignSettings";
    protected static int DEFAULT_DELAY_FIRST_MESSAGE = 3;
    protected static long DEFAULT_MAX_SHOWS = 10;
    protected static int DEFAULT_MIN_DELAY = 30;
    protected static final String ENDPOINT_VERSION = "3";
    protected static final String LOG_TAG = "SwrveMessagingSDK";
    protected static final String TEMPLATE_VERSION = "1";
    protected float android_device_xdpi;
    protected float android_device_ydpi;
    protected SparseArray<String> appStoreURLs;
    protected File cacheDir;
    protected List<SwrveCampaign> campaigns;
    protected String cdnRoot = "http://content-cdn.swrve.com/messaging/message_image/";
    protected float device_dpi;
    protected int device_height;
    protected int device_width;
    protected long messagesLeftToShow;
    protected int minDelayBetweenMessage;
    protected PausableExecutor resourceDownloader;
    protected IRESTClient restClient;
    protected Date showMessagesAfter;
    protected Swrve swrve;

    protected abstract SwrveCampaign loadCampaignFromJSON(JSONObject jSONObject, Set<String> set) throws JSONException;

    SwrveTalkImpl() {
    }

    protected void init() {
        boolean autoDownload = this.swrve.config.isTalkAutoDownload();
        if (autoDownload) {
            downloadCampaignsAsync();
        }
    }

    protected PausableExecutor createResourceDownloader() {
        return new PausableExecutor(this.swrve.config.getMaxConcurrentDownloads());
    }

    protected void downloadCampaignsAsync() {
        executeResourceDownloader(new Runnable() { // from class: com.swrve.sdk.SwrveTalkImpl.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    SwrveTalkImpl.this.downloadCampaings();
                } catch (UnsupportedEncodingException e) {
                    Log.e(SwrveTalkImpl.LOG_TAG, "Error downloading campaigns", e);
                }
                SwrveTalkImpl.this.taskCompleted();
            }
        });
    }

    protected void getDeviceInfo(Context context) {
        try {
            Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
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
            this.device_width = width;
            this.device_height = height;
            this.device_dpi = metrics.densityDpi;
            this.android_device_xdpi = xdpi;
            this.android_device_ydpi = ydpi;
        } catch (Exception exp) {
            Log.e(LOG_TAG, "Get device screen info failed", exp);
        }
    }

    protected void findCacheFolder(Context context) {
        this.cacheDir = context.getCacheDir();
        if (!this.cacheDir.exists()) {
            this.cacheDir.mkdirs();
        }
    }

    protected boolean executeResourceDownloader(Runnable runnable) {
        try {
            this.resourceDownloader.execute(runnable);
            return true;
        } catch (Exception exp) {
            Log.e(LOG_TAG, "Error while scheduling a download", exp);
            return false;
        }
    }

    protected void taskCompleted() {
    }

    protected IRESTClient createRESTClient() {
        return new RESTClient();
    }

    protected Date getNow() {
        return new Date();
    }

    protected boolean hasShowTooManyMessagesAlready() {
        return this.messagesLeftToShow <= 0;
    }

    protected boolean isTooSoonToShowMessageAt(Date now) {
        return now.before(this.showMessagesAfter);
    }

    @SuppressLint({"DefaultLocale"})
    protected void downloadCampaings() throws UnsupportedEncodingException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("api_key", this.swrve.apiKey);
        queryParams.put("user", this.swrve.userId);
        queryParams.put("link_token", this.swrve.linkToken);
        queryParams.put("version", ENDPOINT_VERSION);
        queryParams.put("language", this.swrve.getLanguage());
        queryParams.put("app_store", this.swrve.config.getAppStore().toArgument());
        queryParams.put("device_width", String.valueOf(this.device_width));
        queryParams.put("device_height", String.valueOf(this.device_height));
        queryParams.put("device_dpi", String.valueOf(this.device_dpi));
        queryParams.put("android_device_xdpi", String.valueOf(this.android_device_xdpi));
        queryParams.put("android_device_ydpi", String.valueOf(this.android_device_ydpi));
        queryParams.put("orientation", this.swrve.config.getOrientation().toString().toLowerCase());
        queryParams.put("device_name", this.swrve.getDeviceName());
        queryParams.put("os_version", Build.VERSION.RELEASE);
        this.restClient.get(this.swrve.config.getContentUrl() + CAMPAIGN_ACTION, queryParams, new IRESTResponseListener() { // from class: com.swrve.sdk.SwrveTalkImpl.2
            @Override // com.swrve.sdk.rest.IRESTResponseListener
            public void onResponse(int responseStatus, final String responseBody) {
                if (responseStatus == 200) {
                    SwrveTalkImpl.this.campaigns.clear();
                    List<SwrveCampaign> newCampaigns = SwrveTalkImpl.this.loadCampaignsFromJSON(responseBody);
                    SwrveTalkImpl.this.swrve.storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.SwrveTalkImpl.2.1
                        @Override // java.lang.Runnable
                        public void run() {
                            MemoryCachedLocalStorage cachedStorage = SwrveTalkImpl.this.swrve.cachedLocalStorage;
                            cachedStorage.setCacheEntryForUser(SwrveTalkImpl.this.swrve.userId, SwrveTalkImpl.CAMPAIGN_CATEGORY, responseBody);
                            if (cachedStorage.getSecondaryStorage() != null) {
                                cachedStorage.getSecondaryStorage().setCacheEntryForUser(SwrveTalkImpl.this.swrve.userId, SwrveTalkImpl.CAMPAIGN_CATEGORY, responseBody);
                            }
                            Log.i(SwrveTalkImpl.LOG_TAG, "Saved campaigns in cache.");
                            SwrveTalkImpl.this.swrve.taskCompleted();
                        }
                    });
                    SwrveTalkImpl.this.loadCampaignSettings(newCampaigns);
                    Map<String, String> payload = new HashMap<>();
                    StringBuilder campaignIds = new StringBuilder();
                    for (int i = 0; i < newCampaigns.size(); i++) {
                        if (i != 0) {
                            campaignIds.append(',');
                        }
                        campaignIds.append(newCampaigns.get(i).getId());
                    }
                    payload.put("ids", campaignIds.toString());
                    payload.put("count", String.valueOf(newCampaigns.size()));
                    Map<String, Object> parameteres = new HashMap<>();
                    parameteres.put("name", "Swrve.Messages.campaigns_downloaded");
                    SwrveTalkImpl.this.swrve.queueEvent(SQLiteLocalStorage.COLUMN_EVENT, parameteres, payload);
                    return;
                }
                String campaignsFromCache = SwrveTalkImpl.this.swrve.cachedLocalStorage.getCacheEntryForUser(SwrveTalkImpl.this.swrve.userId, SwrveTalkImpl.CAMPAIGN_CATEGORY);
                if (campaignsFromCache != null && !campaignsFromCache.equals("")) {
                    SwrveTalkImpl.this.campaigns.clear();
                    SwrveTalkImpl.this.loadCampaignSettings(SwrveTalkImpl.this.loadCampaignsFromJSON(campaignsFromCache));
                    Log.i(SwrveTalkImpl.LOG_TAG, "Loaded campaigns from cache.");
                }
            }

            @Override // com.swrve.sdk.rest.IRESTResponseListener
            public void onException(Exception exp) {
                Log.e(SwrveTalkImpl.LOG_TAG, "Error downloading campaigns", exp);
            }
        });
    }

    protected List<SwrveCampaign> loadCampaignsFromJSON(String json) {
        List<SwrveCampaign> result = new ArrayList<>();
        if (json == null) {
            Log.i(LOG_TAG, "NULL JSON for campaigns, aborting load.");
        } else {
            Log.i(LOG_TAG, "Campaign JSON data: " + json);
            try {
                JSONObject root = new JSONObject(json);
                String version = root.getString("version");
                if (!version.equals(TEMPLATE_VERSION)) {
                    Log.i(LOG_TAG, "Campaign JSON has the wrong version. No campaigns loaded.");
                } else {
                    this.cdnRoot = root.getString("cdn_root");
                    Log.i(LOG_TAG, "CDN URL " + this.cdnRoot);
                    JSONObject gamesData = root.getJSONObject("game_data");
                    if (gamesData != null) {
                        Iterator<?> gamesDataIt = gamesData.keys();
                        while (gamesDataIt.hasNext()) {
                            String gameId = gamesDataIt.next();
                            JSONObject gameData = gamesData.getJSONObject(gameId);
                            if (gameData.has("app_store_url")) {
                                String url = gameData.getString("app_store_url");
                                this.appStoreURLs.put(Integer.parseInt(gameId), url);
                                if (url == null || url.isEmpty()) {
                                    Log.e(LOG_TAG, "App store link " + gameId + " is empty!");
                                } else {
                                    Log.i(LOG_TAG, "App store Link " + gameId + ": " + url);
                                }
                            }
                        }
                    }
                    JSONObject rules = root.getJSONObject("rules");
                    int delay = rules.has("delay_first_message") ? rules.getInt("delay_first_message") : DEFAULT_DELAY_FIRST_MESSAGE;
                    long maxShows = rules.has("max_messages_per_session") ? rules.getLong("max_messages_per_session") : DEFAULT_MAX_SHOWS;
                    int minDelay = rules.has("min_delay_between_messages") ? rules.getInt("min_delay_between_messages") : DEFAULT_MIN_DELAY;
                    Date now = getNow();
                    this.showMessagesAfter = addTimeInterval(now, delay, 13);
                    this.minDelayBetweenMessage = minDelay;
                    this.messagesLeftToShow = maxShows;
                    Log.i(LOG_TAG, "Game rules OK: Delay Seconds: " + delay + " Max shows: " + maxShows);
                    Log.i(LOG_TAG, "Time is " + now.toString() + " show messages after " + this.showMessagesAfter.toString());
                    JSONArray jsonCampaigns = root.getJSONArray("campaigns");
                    int j = jsonCampaigns.length();
                    for (int i = 0; i < j; i++) {
                        JSONObject campaignData = jsonCampaigns.getJSONObject(i);
                        Set<String> campaignAssetsQueue = new HashSet<>();
                        SwrveCampaign campaign = loadCampaignFromJSON(campaignData, campaignAssetsQueue);
                        downloadCampaignAssets(campaign, campaignAssetsQueue);
                        result.add(campaign);
                    }
                }
            } catch (JSONException exp) {
                Log.e(LOG_TAG, "Error parsing campaign JSON", exp);
            }
        }
        return result;
    }

    protected void downloadCampaignAssets(final SwrveCampaign campaign, final Set<String> assetsQueue) {
        executeResourceDownloader(new Runnable() { // from class: com.swrve.sdk.SwrveTalkImpl.3
            @Override // java.lang.Runnable
            public void run() {
                for (String asset : assetsQueue) {
                    Log.i(SwrveTalkImpl.LOG_TAG, "Asset to load: " + asset);
                }
                Set<String> downloadQueue = SwrveTalkImpl.this.filterExistingFiles(assetsQueue);
                Iterator<String> itDownloadQueue = downloadQueue.iterator();
                boolean allAssetsCorrect = true;
                while (itDownloadQueue.hasNext() && allAssetsCorrect) {
                    String assetPath = itDownloadQueue.next();
                    allAssetsCorrect = SwrveTalkImpl.this.downloadAssetSynchronously(assetPath);
                }
                downloadQueue.clear();
                if (allAssetsCorrect && campaign.getMessages().size() > 0) {
                    synchronized (SwrveTalkImpl.this.campaigns) {
                        SwrveTalkImpl.this.campaigns.add(campaign);
                    }
                }
                SwrveTalkImpl.this.taskCompleted();
            }
        });
    }

    protected boolean downloadAssetSynchronously(String assetPath) {
        String url = this.cdnRoot + assetPath;
        try {
            URLConnection openConnection = new URL(url).openConnection();
            InputStream inputStream = openConnection.getInputStream();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                stream.write(buffer, 0, bytesRead);
            }
            byte[] fileContents = stream.toByteArray();
            String sha1File = SwrveHelper.sha1(stream.toByteArray());
            if (!assetPath.equals(sha1File)) {
                return false;
            }
            FileOutputStream fileStream = new FileOutputStream(new File(this.cacheDir, assetPath));
            fileStream.write(fileContents);
            fileStream.close();
            return true;
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error downloading campaigns", e);
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            Log.e(LOG_TAG, "Error downloading campaigns", e2);
            e2.printStackTrace();
            return false;
        }
    }

    protected Set<String> filterExistingFiles(Set<String> assetsQueue) {
        Iterator<String> itDownloadQueue = assetsQueue.iterator();
        while (itDownloadQueue.hasNext()) {
            String assetPath = itDownloadQueue.next();
            File file = new File(this.cacheDir, assetPath);
            if (file.exists()) {
                itDownloadQueue.remove();
            }
        }
        return assetsQueue;
    }

    protected Date addTimeInterval(Date origin, int value, int unit) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(origin);
        cal.add(unit, value);
        return cal.getTime();
    }

    protected void loadCampaignSettings(List<SwrveCampaign> campaigns) {
        JSONObject campaignSettings;
        try {
            String serializedSettings = this.swrve.cachedLocalStorage.getCacheEntryForUser(this.swrve.userId, CAMPAIGN_SETTINGS_CATEGORY);
            if (serializedSettings != null && !serializedSettings.equals("")) {
                JSONObject jsonSettings = new JSONObject(serializedSettings);
                for (SwrveCampaign campaign : campaigns) {
                    String campaignIdStr = Integer.toString(campaign.getId());
                    if (jsonSettings.has(campaignIdStr) && (campaignSettings = jsonSettings.getJSONObject(campaignIdStr)) != null) {
                        campaign.loadSettings(campaignSettings);
                    }
                }
            }
        } catch (JSONException exp) {
            Log.e(LOG_TAG, "Error loading campaigns settings", exp);
        }
    }

    protected void saveCampaginSettings() {
        try {
            JSONObject jsonSettings = new JSONObject();
            for (SwrveCampaign campaign : this.campaigns) {
                jsonSettings.put(Integer.toString(campaign.getId()), campaign.createSettings());
            }
            final String serializedSettings = jsonSettings.toString();
            this.swrve.storageExecutorExecute(new Runnable() { // from class: com.swrve.sdk.SwrveTalkImpl.4
                @Override // java.lang.Runnable
                public void run() {
                    MemoryCachedLocalStorage cachedStorage = SwrveTalkImpl.this.swrve.cachedLocalStorage;
                    cachedStorage.setCacheEntryForUser(SwrveTalkImpl.this.swrve.userId, SwrveTalkImpl.CAMPAIGN_SETTINGS_CATEGORY, serializedSettings);
                    if (cachedStorage.getSecondaryStorage() != null) {
                        cachedStorage.getSecondaryStorage().setCacheEntryForUser(SwrveTalkImpl.this.swrve.userId, SwrveTalkImpl.CAMPAIGN_SETTINGS_CATEGORY, serializedSettings);
                    }
                    Log.i(SwrveTalkImpl.LOG_TAG, "Saved campaigns in cache.");
                    SwrveTalkImpl.this.swrve.taskCompleted();
                }
            });
        } catch (JSONException exp) {
            Log.e(LOG_TAG, "Error saving campaigns settings", exp);
        }
    }
}
