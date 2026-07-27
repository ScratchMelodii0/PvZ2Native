package com.facebook;

import android.content.Context;
import android.os.Bundle;
import com.facebook.Session;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.facebook.model.GraphObject;
import com.swrve.sdk.localstorage.SQLiteLocalStorage;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class InsightsLogger {
    private static final String EVENT_NAME_LOG_CONVERSION_PIXEL = "fb_log_offsite_pixel";
    private static final String EVENT_NAME_LOG_MOBILE_PURCHASE = "fb_mobile_purchase";
    private static final String EVENT_PARAMETER_CURRENCY = "fb_currency";
    private static final String EVENT_PARAMETER_PIXEL_ID = "fb_offsite_pixel_id";
    private static final String EVENT_PARAMETER_PIXEL_VALUE = "fb_offsite_pixel_value";
    private static Session appAuthSession = null;
    private final String applicationId;
    private final String clientToken;
    private final Context context;
    private final Session specifiedSession;

    private InsightsLogger(Context context, String clientToken, String applicationId, Session session) {
        Validate.notNull(context, "context");
        Validate.notNullOrEmpty(clientToken, "clientToken");
        applicationId = applicationId == null ? Utility.getMetadataApplicationId(context) : applicationId;
        this.context = context;
        this.clientToken = clientToken;
        this.applicationId = applicationId;
        this.specifiedSession = session;
    }

    public static InsightsLogger newLogger(Context context, String clientToken) {
        return new InsightsLogger(context, clientToken, null, null);
    }

    public static InsightsLogger newLogger(Context context, String clientToken, String applicationId) {
        return new InsightsLogger(context, clientToken, applicationId, null);
    }

    public static InsightsLogger newLogger(Context context, String clientToken, String applicationId, Session session) {
        return new InsightsLogger(context, clientToken, applicationId, session);
    }

    public void logPurchase(BigDecimal purchaseAmount, Currency currency) {
        logPurchase(purchaseAmount, currency, null);
    }

    public void logPurchase(BigDecimal purchaseAmount, Currency currency, Bundle parameters) {
        if (purchaseAmount == null) {
            notifyDeveloperError("purchaseAmount cannot be null");
            return;
        }
        if (currency == null) {
            notifyDeveloperError("currency cannot be null");
            return;
        }
        if (parameters == null) {
            parameters = new Bundle();
        }
        parameters.putString(EVENT_PARAMETER_CURRENCY, currency.getCurrencyCode());
        logEventNow(EVENT_NAME_LOG_MOBILE_PURCHASE, purchaseAmount.doubleValue(), parameters);
    }

    public void logConversionPixel(String pixelId, double valueOfPixel) {
        if (pixelId == null) {
            notifyDeveloperError("pixelID cannot be null");
            return;
        }
        Bundle parameters = new Bundle();
        parameters.putString(EVENT_PARAMETER_PIXEL_ID, pixelId);
        parameters.putDouble(EVENT_PARAMETER_PIXEL_VALUE, valueOfPixel);
        logEventNow(EVENT_NAME_LOG_CONVERSION_PIXEL, valueOfPixel, parameters);
    }

    private void logEventNow(final String eventName, final double valueToSum, final Bundle parameters) {
        Settings.getExecutor().execute(new Runnable() { // from class: com.facebook.InsightsLogger.1
            @Override // java.lang.Runnable
            public void run() {
                String attributionId;
                String eventJSON = InsightsLogger.buildJSONForEvent(eventName, valueToSum, parameters);
                if (eventJSON != null) {
                    GraphObject publishParams = GraphObject.Factory.create();
                    publishParams.setProperty(SQLiteLocalStorage.COLUMN_EVENT, "CUSTOM_APP_EVENTS");
                    publishParams.setProperty("custom_events", eventJSON);
                    if (Utility.queryAppAttributionSupportAndWait(InsightsLogger.this.applicationId) && (attributionId = Settings.getAttributionId(InsightsLogger.this.context.getContentResolver())) != null) {
                        publishParams.setProperty("attribution", attributionId);
                    }
                    String publishUrl = String.format("%s/activities", InsightsLogger.this.applicationId);
                    try {
                        Request postRequest = Request.newPostRequest(InsightsLogger.this.sessionToLogTo(), publishUrl, publishParams, null);
                        Response response = postRequest.executeAndWait();
                        if (response.getError() != null && response.getError().getErrorCode() != -1) {
                            InsightsLogger.notifyDeveloperError(String.format("Error publishing Insights event '%s'\n  Response: %s\n  Error: %s", eventJSON, response.toString(), response.getError().toString()));
                        }
                    } catch (Exception e) {
                        Utility.logd("Insights-exception: ", e);
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String buildJSONForEvent(String eventName, double valueToSum, Bundle parameters) {
        try {
            JSONObject eventObject = new JSONObject();
            eventObject.put("_eventName", eventName);
            if (valueToSum != 1.0d) {
                eventObject.put("_valueToSum", valueToSum);
            }
            if (parameters != null) {
                Set<String> keys = parameters.keySet();
                for (String key : keys) {
                    Object value = parameters.get(key);
                    if (!(value instanceof String) && !(value instanceof Number)) {
                        notifyDeveloperError(String.format("Parameter '%s' must be a string or a numeric type.", key));
                    }
                    eventObject.put(key, value);
                }
            }
            JSONArray eventArray = new JSONArray();
            eventArray.put(eventObject);
            String result = eventArray.toString();
            return result;
        } catch (JSONException exception) {
            notifyDeveloperError(exception.toString());
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Session sessionToLogTo() {
        Session session;
        synchronized (this) {
            session = this.specifiedSession;
            if (session == null || !session.isOpened()) {
                session = Session.getActiveSession();
            }
            if (session == null || !session.isOpened() || session.getAccessToken() == null) {
                if (appAuthSession == null) {
                    String tokenString = String.format("%s|%s", this.applicationId, this.clientToken);
                    AccessToken token = AccessToken.createFromString(tokenString, null, AccessTokenSource.CLIENT_TOKEN);
                    appAuthSession = new Session(null, this.applicationId, new NonCachingTokenCachingStrategy(), false);
                    appAuthSession.open(token, (Session.StatusCallback) null);
                }
                session = appAuthSession;
            }
        }
        return session;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void notifyDeveloperError(String message) {
        Logger.log(LoggingBehavior.DEVELOPER_ERRORS, "Insights", message);
    }
}
