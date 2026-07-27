package com.swrve.sdk.config;

import com.swrve.sdk.SwrveAppStore;
import com.swrve.sdk.messaging.SwrveOrientation;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveConfig {
    public static final int DO_NOT_SEND_ANDROID_ID_MD5 = 4;
    public static final int DO_NOT_SEND_ANY = 7;
    public static final int DO_NOT_SEND_FB_APP_ATTRIBUTION = 2;
    public static final int DO_NOT_SEND_MAC_ADDRESS_MD5 = 1;
    private String appVersion;
    private String language;
    private String linkToken;
    private long maxSqliteDbSize = 1048576;
    private int maxEventsPerFlush = 50;
    private int maxClickThrusPerFlush = 10;
    private int maxConcurrentDownloads = 10;
    private String dbName = "swrve.db";
    private String eventsUrl = null;
    private String defaultEventsUrl = null;
    private String linkUrl = null;
    private String defaultLinkUrl = null;
    private String contentUrl = null;
    private String defaultContentUrl = null;
    private long newSessionInterval = 30000;
    private long sendQueuedEventsInterval = 30000;
    private SwrveAppStore appStore = SwrveAppStore.GooglePlay;
    private int identifierRules = 0;
    private SwrveOrientation orientation = SwrveOrientation.Both;
    private boolean talkAutoDownload = true;

    public void setSendQueuedEventsInterval(long sendQueuedEventsInterval) {
        this.sendQueuedEventsInterval = sendQueuedEventsInterval;
    }

    public void disableSendQueuedEventsInterval() {
        this.sendQueuedEventsInterval = 0L;
    }

    public long getSendQueuedEventsInterval() {
        return this.sendQueuedEventsInterval;
    }

    public boolean isTalkAutoDownload() {
        return this.talkAutoDownload;
    }

    public void setTalkAutoDownload(boolean talkAutoDownload) {
        this.talkAutoDownload = talkAutoDownload;
    }

    public SwrveOrientation getOrientation() {
        return this.orientation;
    }

    public void setOrientation(SwrveOrientation orientation) {
        this.orientation = orientation;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getIdentifierRules() {
        return this.identifierRules;
    }

    public void setIdentifierRules(int identifierRules) {
        this.identifierRules = identifierRules;
    }

    public long getMaxSqliteDbSize() {
        return this.maxSqliteDbSize;
    }

    public SwrveConfig setMaxSqliteDbSize(long maxSqliteDbSize) {
        this.maxSqliteDbSize = maxSqliteDbSize;
        return this;
    }

    public int getMaxEventsPerFlush() {
        return this.maxEventsPerFlush;
    }

    public SwrveConfig setMaxEventsPerFlush(int maxEventsPerFlush) {
        this.maxEventsPerFlush = maxEventsPerFlush;
        return this;
    }

    public int getMaxConcurrentDownloads() {
        return this.maxConcurrentDownloads;
    }

    public SwrveConfig setMaxConcurrentDownloads(int maxConcurrentDownloads) {
        this.maxConcurrentDownloads = maxConcurrentDownloads;
        return this;
    }

    public int getMaxClickThrusPerFlush() {
        return this.maxClickThrusPerFlush;
    }

    public SwrveConfig setMaxClickThrusPerFlush(int maxClickThrusPerFlush) {
        this.maxClickThrusPerFlush = maxClickThrusPerFlush;
        return this;
    }

    public String getDbName() {
        return this.dbName;
    }

    public SwrveConfig setDbName(String db_name) {
        this.dbName = db_name;
        return this;
    }

    public String getEventsUrl() {
        return this.eventsUrl == null ? this.defaultEventsUrl : this.eventsUrl;
    }

    public SwrveConfig setEventsUrl(String eventsUrl) {
        this.eventsUrl = eventsUrl;
        return this;
    }

    public String getLinkUrl() {
        return this.linkUrl == null ? this.defaultLinkUrl : this.linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getContentUrl() {
        return this.contentUrl == null ? this.defaultContentUrl : this.contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getLinkToken() {
        return this.linkToken;
    }

    public void setLinkToken(String linkToken) {
        this.linkToken = linkToken;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public long getNewSessionInterval() {
        return this.newSessionInterval;
    }

    public void setNewSessionInterval(long newSessionInterval) {
        this.newSessionInterval = newSessionInterval;
    }

    public SwrveAppStore getAppStore() {
        return this.appStore;
    }

    public void setAppStore(SwrveAppStore appStore) {
        this.appStore = appStore;
    }

    public void generateUrls(int gameId) {
        this.defaultEventsUrl = "http://" + gameId + ".api.swrve.com";
        this.defaultLinkUrl = "https://" + gameId + ".link.swrve.com";
        this.defaultContentUrl = "https://" + gameId + ".content.swrve.com";
    }
}
