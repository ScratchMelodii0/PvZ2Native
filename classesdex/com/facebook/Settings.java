package com.facebook;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import com.facebook.Request;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.facebook.model.GraphObject;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class Settings {
    private static final String ANALYTICS_EVENT = "event";
    private static final String ATTRIBUTION_ID_COLUMN_NAME = "aid";
    private static final String ATTRIBUTION_KEY = "attribution";
    private static final String ATTRIBUTION_PREFERENCES = "com.facebook.sdk.attributionTracking";
    private static final int DEFAULT_CORE_POOL_SIZE = 5;
    private static final int DEFAULT_KEEP_ALIVE = 1;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 128;
    private static final String MOBILE_INSTALL_EVENT = "MOBILE_APP_INSTALL";
    private static final String PUBLISH_ACTIVITY_PATH = "%s/activities";
    private static volatile Executor executor;
    private static volatile boolean shouldAutoPublishInstall;
    private static final HashSet<LoggingBehavior> loggingBehaviors = new HashSet<>(Arrays.asList(LoggingBehavior.DEVELOPER_ERRORS));
    private static final Object LOCK = new Object();
    private static final Uri ATTRIBUTION_ID_CONTENT_URI = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
    private static final BlockingQueue<Runnable> DEFAULT_WORK_QUEUE = new LinkedBlockingQueue(10);
    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new ThreadFactory() { // from class: com.facebook.Settings.1
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "FacebookSdk #" + this.counter.incrementAndGet());
        }
    };

    public static final Set<LoggingBehavior> getLoggingBehaviors() {
        Set<LoggingBehavior> setUnmodifiableSet;
        synchronized (loggingBehaviors) {
            setUnmodifiableSet = Collections.unmodifiableSet(new HashSet(loggingBehaviors));
        }
        return setUnmodifiableSet;
    }

    public static final void addLoggingBehavior(LoggingBehavior behavior) {
        synchronized (loggingBehaviors) {
            loggingBehaviors.add(behavior);
        }
    }

    public static final void removeLoggingBehavior(LoggingBehavior behavior) {
        synchronized (loggingBehaviors) {
            loggingBehaviors.remove(behavior);
        }
    }

    public static final void clearLoggingBehaviors() {
        synchronized (loggingBehaviors) {
            loggingBehaviors.clear();
        }
    }

    public static final boolean isLoggingBehaviorEnabled(LoggingBehavior behavior) {
        synchronized (loggingBehaviors) {
        }
        return false;
    }

    public static Executor getExecutor() {
        synchronized (LOCK) {
            if (executor == null) {
                Executor executor2 = getAsyncTaskExecutor();
                if (executor2 == null) {
                    executor2 = new ThreadPoolExecutor(5, 128, 1L, TimeUnit.SECONDS, DEFAULT_WORK_QUEUE, DEFAULT_THREAD_FACTORY);
                }
                executor = executor2;
            }
        }
        return executor;
    }

    public static void setExecutor(Executor executor2) {
        Validate.notNull(executor2, "executor");
        synchronized (LOCK) {
            executor = executor2;
        }
    }

    private static Executor getAsyncTaskExecutor() {
        try {
            Field executorField = AsyncTask.class.getField("THREAD_POOL_EXECUTOR");
            if (executorField == null) {
                return null;
            }
            try {
                Object executorObject = executorField.get(null);
                if (executorObject != null && (executorObject instanceof Executor)) {
                    return (Executor) executorObject;
                }
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        } catch (NoSuchFieldException e2) {
            return null;
        }
    }

    public static void publishInstallAsync(Context context, String applicationId) {
        publishInstallAsync(context, applicationId, null);
    }

    public static void publishInstallAsync(Context context, final String applicationId, final Request.Callback callback) {
        final Context applicationContext = context.getApplicationContext();
        getExecutor().execute(new Runnable() { // from class: com.facebook.Settings.2
            @Override // java.lang.Runnable
            public void run() {
                final Response response = Settings.publishInstallAndWaitForResponse(applicationContext, applicationId);
                if (callback != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() { // from class: com.facebook.Settings.2.1
                        @Override // java.lang.Runnable
                        public void run() {
                            callback.onCompleted(response);
                        }
                    });
                }
            }
        });
    }

    public static void setShouldAutoPublishInstall(boolean shouldAutoPublishInstall2) {
        shouldAutoPublishInstall = shouldAutoPublishInstall2;
    }

    public static boolean getShouldAutoPublishInstall() {
        return shouldAutoPublishInstall;
    }

    public static boolean publishInstallAndWait(Context context, String applicationId) {
        Response response = publishInstallAndWaitForResponse(context, applicationId);
        return response != null && response.getError() == null;
    }

    public static Response publishInstallAndWaitForResponse(Context context, String applicationId) {
        try {
            if (context == null || applicationId == null) {
                throw new IllegalArgumentException("Both context and applicationId must be non-null");
            }
            String attributionId = getAttributionId(context.getContentResolver());
            SharedPreferences preferences = context.getSharedPreferences(ATTRIBUTION_PREFERENCES, 0);
            String pingKey = applicationId + "ping";
            String jsonKey = applicationId + "json";
            long lastPing = preferences.getLong(pingKey, 0L);
            String lastResponseJSON = preferences.getString(jsonKey, null);
            GraphObject publishParams = GraphObject.Factory.create();
            publishParams.setProperty("event", MOBILE_INSTALL_EVENT);
            publishParams.setProperty(ATTRIBUTION_KEY, attributionId);
            String publishUrl = String.format(PUBLISH_ACTIVITY_PATH, applicationId);
            Request publishRequest = Request.newPostRequest(null, publishUrl, publishParams, null);
            if (lastPing != 0) {
                GraphObject graphObject = null;
                if (lastResponseJSON != null) {
                    try {
                        graphObject = GraphObject.Factory.create(new JSONObject(lastResponseJSON));
                    } catch (JSONException e) {
                    }
                }
                if (graphObject == null) {
                    return Response.createResponsesFromString("true", null, new RequestBatch(publishRequest), true).get(0);
                }
                return new Response((Request) null, (HttpURLConnection) null, graphObject, true);
            }
            if (attributionId == null) {
                throw new FacebookException("No attribution id returned from the Facebook application");
            }
            if (!Utility.queryAppAttributionSupportAndWait(applicationId)) {
                throw new FacebookException("Install attribution has been disabled on the server.");
            }
            Response publishResponse = publishRequest.executeAndWait();
            SharedPreferences.Editor editor = preferences.edit();
            long lastPing2 = System.currentTimeMillis();
            editor.putLong(pingKey, lastPing2);
            if (publishResponse.getGraphObject() != null && publishResponse.getGraphObject().getInnerJSONObject() != null) {
                editor.putString(jsonKey, publishResponse.getGraphObject().getInnerJSONObject().toString());
            }
            editor.commit();
            return publishResponse;
        } catch (Exception e2) {
            Utility.logd("Facebook-publish", e2);
            return new Response(null, null, new FacebookRequestError(null, e2));
        }
    }

    public static String getAttributionId(ContentResolver contentResolver) {
        String[] projection = {"aid"};
        Cursor c = contentResolver.query(ATTRIBUTION_ID_CONTENT_URI, projection, null, null, null);
        if (c == null || !c.moveToFirst()) {
            return null;
        }
        String string = c.getString(c.getColumnIndex("aid"));
        c.close();
        return string;
    }

    public static String getSdkVersion() {
        return FacebookSdkVersion.BUILD;
    }

    public static String getMigrationBundle() {
        return FacebookSdkVersion.MIGRATION_BUNDLE;
    }
}
