package com.swrve.sdk;

import android.content.Context;
import com.swrve.sdk.config.SwrveConfig;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveInstance {
    private static Swrve instance;

    public static synchronized Swrve getInstance() {
        if (instance == null) {
            instance = new Swrve();
        }
        return instance;
    }

    public static void init(Context context, int gameId, String apiKey, String userId, SwrveConfig config) {
        getInstance().init(context, gameId, apiKey, userId, config);
    }

    public static void sessionStart() {
        getInstance().sessionStart();
    }

    public static void sessionEnd() {
        getInstance().sessionEnd();
    }

    public static void event(String name, Map<String, String> payload) {
        getInstance().event(name, payload);
    }

    public static void purchase(String item, String currency, int cost, int quantity) {
        getInstance().purchase(item, currency, cost, quantity);
    }

    public static void currencyGiven(String givenCurrency, double givenAmount) {
        getInstance().currencyGiven(givenCurrency, givenAmount);
    }

    public static void userUpdate(Map<String, String> attributes) {
        getInstance().userUpdate(attributes);
    }

    public static void buyIn(String rewardCurrency, int rewardAmount, double localCost, String localCurrency, String paymentProvider) {
        getInstance().buyIn(rewardCurrency, rewardAmount, localCost, localCurrency, paymentProvider);
    }

    public static void getUserResources(ISwrveUserResourcesListener listener) {
        getInstance().getUserResources(listener);
    }

    public static void getUserResourcesDiff(ISwrveUserResourcesDiffListener listener) {
        getInstance().getUserResourcesDiff(listener);
    }

    public static void sendQueuedEvents() {
        getInstance().sendQueuedEvents();
    }

    public static void flushToDisk() {
        getInstance().flushToDisk();
    }

    public static void clickThru(int targetGameId, String source) {
        getInstance().clickThru(targetGameId, source);
    }

    public static void sendAndroidId() {
        getInstance().sendAndroidId();
    }

    public static void setTpid(String tpid) {
        getInstance().setTpid(tpid);
    }

    public static void setCustomID(String customID) {
        getInstance().setCustomID(customID);
    }

    public static void setOdin1(String odin1) {
        getInstance().setOdin1(odin1);
    }

    public static void setMacAddress(String mac_address) {
        getInstance().setMacAddress(mac_address);
    }

    public static void setFacebookAppAttribution(String fbAppAttribution) {
        getInstance().setFacebookAppAttribution(fbAppAttribution);
    }

    public static void setIMEIMD5(String imei_md5) {
        getInstance().setIMEIMD5(imei_md5);
    }

    public static void onPause() {
        getInstance().onPause();
    }

    public static void onResume() {
        getInstance().onResume();
    }

    public static void onDestroy() {
        getInstance().onDestroy();
        instance = null;
    }

    public static String getVersion() {
        return Swrve.getVersion();
    }

    public static String getLanguage() {
        return getInstance().getLanguage();
    }

    public static void setLanguage(String language) {
        getInstance().setLanguage(language);
    }
}
