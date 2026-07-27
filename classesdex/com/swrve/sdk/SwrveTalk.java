package com.swrve.sdk;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.swrve.sdk.localstorage.SQLiteLocalStorage;
import com.swrve.sdk.messaging.ISwrveMessageListener;
import com.swrve.sdk.messaging.SwrveActionType;
import com.swrve.sdk.messaging.SwrveButton;
import com.swrve.sdk.messaging.SwrveCampaign;
import com.swrve.sdk.messaging.SwrveEventListener;
import com.swrve.sdk.messaging.SwrveMessage;
import com.swrve.sdk.messaging.SwrveMessageFormat;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveTalk extends SwrveTalkImpl {
    public SwrveTalk(Context context, Swrve swrve) throws Exception {
        if (swrve != null) {
            this.swrve = swrve;
            this.campaigns = new ArrayList();
            this.appStoreURLs = new SparseArray<>();
            this.restClient = createRESTClient();
            this.resourceDownloader = createResourceDownloader();
            findCacheFolder(context);
            getDeviceInfo(context);
            if (swrve.language == null || swrve.language.equals("")) {
                Log.e("SwrveMessagingSDK", "Invalid language specified");
                throw new Exception("Invalid language specified");
            }
            if (swrve.userId == null || swrve.userId.equals("")) {
                Log.e("SwrveMessagingSDK", "Invalid username specified");
                throw new Exception("Invalid username specified");
            }
            init();
            return;
        }
        Log.e("SwrveMessagingSDK", "Swrve Analytics SDK is null");
        throw new Exception("Swrve Analytics SDK is null");
    }

    public SwrveMessage getMessageForEvent(String event) {
        Date now;
        SwrveMessage result = null;
        try {
            now = getNow();
        } catch (Exception e) {
            Log.e("SwrveMessagingSDK", "Error while searching for a message for event " + event, e);
        }
        if (this.campaigns != null && this.campaigns.size() > 0) {
            if (isTooSoonToShowMessageAt(now)) {
                Log.i("SwrveMessagingSDK", "Not showing messages: too soon after after launch or last message. Next show at " + this.showMessagesAfter.toString());
                return null;
            }
            if (hasShowTooManyMessagesAlready()) {
                Log.i("SwrveMessagingSDK", "Not showing messages: too many messages shown");
                return null;
            }
            synchronized (this.campaigns) {
                Collections.shuffle(this.campaigns);
                Iterator<SwrveCampaign> itCampaign = this.campaigns.iterator();
                while (itCampaign.hasNext() && result == null) {
                    SwrveCampaign campaign = itCampaign.next();
                    result = campaign.getMessageForEvent(event, now);
                }
            }
            return result;
        }
        if (result == null) {
            Log.w("SwrveMessagingSDK", "Not showing messages: no candidate messages");
        } else {
            Map<String, String> payload = new HashMap<>();
            payload.put("id", String.valueOf(result.getId()));
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("name", "Swrve.Messages.message_returned");
            this.swrve.queueEvent(SQLiteLocalStorage.COLUMN_EVENT, parameters, payload);
        }
        return result;
    }

    public SwrveMessage getMessageForId(int messageId) {
        SwrveMessage result = null;
        try {
            if (this.campaigns != null && this.campaigns.size() > 0) {
                synchronized (this.campaigns) {
                    Iterator<SwrveCampaign> itCampaign = this.campaigns.iterator();
                    while (itCampaign.hasNext() && result == null) {
                        SwrveCampaign campaign = itCampaign.next();
                        result = campaign.getMessageForId(messageId);
                    }
                }
            }
            if (result == null) {
                Log.i("SwrveMessagingSDK", "Not showing messages: no candidate messages");
            }
        } catch (Exception e) {
            Log.e("SwrveMessagingSDK", "Error while searching for a message for id " + messageId, e);
        }
        return result;
    }

    public void setDownloadingEnabled(boolean enabled) {
        try {
            if (enabled) {
                this.resourceDownloader.resume();
            } else {
                this.resourceDownloader.pause();
            }
        } catch (Exception e) {
            Log.e("SwrveMessagingSDK", "Error while toggling resources downloading", e);
        }
    }

    public void buttonWasPressedByUser(SwrveButton button) {
        try {
            if (button.getActionType() != SwrveActionType.Dismiss) {
                String clickEvent = "Swrve.Messages.Message-" + button.getMessage().getId() + ".click";
                Log.i("SwrveMessagingSDK", "Sending click event: " + clickEvent + "(" + button.getName() + ")");
                Map<String, String> payload = new HashMap<>();
                payload.put("name", button.getName());
                this.swrve.event(clickEvent, payload);
            }
            if (button.getActionType() == SwrveActionType.Install) {
                Log.i("SwrveMessagingSDK", "Sending click_thru link event");
                String clickSource = "Swrve.Message-" + button.getMessage().getId();
                this.swrve.clickThru(button.getGameId(), clickSource);
            } else if (button.getActionType() == SwrveActionType.Dismiss && button.getMessage().getCampaign() != null) {
                button.getMessage().getCampaign().decrementDismissalsRemaining();
            }
            saveCampaginSettings();
        } catch (Exception e) {
            Log.e("SwrveMessagingSDK", "Error while processing button press", e);
        }
    }

    public void messageWasShownToUser(SwrveMessageFormat messageFormat) {
        if (messageFormat != null) {
            try {
                Date date = getNow();
                this.showMessagesAfter = addTimeInterval(date, this.minDelayBetweenMessage, 13);
                this.messagesLeftToShow--;
                SwrveMessage message = messageFormat.getMessage();
                SwrveCampaign campaign = message.getCampaign();
                if (campaign != null) {
                    int nextMessage = (campaign.getNext() + 1) % campaign.getMessages().size();
                    campaign.setNext(nextMessage);
                    Log.i("SwrveMessagingSDK", "Round Robin: Next message in campaign " + campaign.getId() + " is " + nextMessage);
                }
                String viewEvent = "Swrve.Messages.Message-" + message.getId() + ".impression";
                Map<String, String> payload = new HashMap<>();
                payload.put("format", messageFormat.getName());
                payload.put("orientation", messageFormat.getOrientation().name());
                payload.put("size", messageFormat.getSize().x + "x" + messageFormat.getSize().y);
                Log.i("SwrveMessagingSDK", "Sending view event: " + viewEvent);
                this.swrve.event(viewEvent, payload);
            } catch (Exception e) {
                Log.e("SwrveMessagingSDK", "Error while processing message impression", e);
            }
        }
    }

    public String getAppStoreURLForGame(int gameId) {
        try {
            return this.appStoreURLs.get(gameId);
        } catch (Exception e) {
            Log.e("SwrveMessagingSDK", "Error while obtaining app store url for game" + gameId, e);
            return null;
        }
    }

    @Override // com.swrve.sdk.SwrveTalkImpl
    protected SwrveCampaign loadCampaignFromJSON(JSONObject campaignData, Set<String> assetsQueue) throws JSONException {
        return new SwrveCampaign(this, campaignData, assetsQueue);
    }

    public void reloadCampaigns() {
        try {
            executeResourceDownloader(new Runnable() { // from class: com.swrve.sdk.SwrveTalk.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        SwrveTalk.this.downloadCampaings();
                    } catch (UnsupportedEncodingException e) {
                        Log.e("SwrveMessagingSDK", "Error downloading campaigns", e);
                    }
                    SwrveTalk.this.taskCompleted();
                }
            });
        } catch (Exception e) {
            Log.e("SwrveMessagingSDK", "Error while reloading campaigns", e);
        }
    }

    public void onDestroy() {
        try {
            Log.i("SwrveMessagingSDK", "onDestroy");
            this.resourceDownloader.shutdownNow();
            this.cacheDir = null;
            this.restClient = null;
            this.resourceDownloader = null;
            this.campaigns = null;
            this.appStoreURLs = null;
        } catch (Exception exp) {
            Log.e("SwrveMessagingSDK", "onDestroy failed", exp);
        }
    }

    public File getCacheDir() {
        return this.cacheDir;
    }

    public Swrve getAnalytics() {
        return this.swrve;
    }

    public void setMessageListener(ISwrveMessageListener messageListener) {
        if (messageListener != null) {
            this.swrve.eventListener = new SwrveEventListener(this, messageListener);
        } else {
            this.swrve.eventListener = null;
        }
    }
}
