package com.swrve.sdk.messaging;

import android.util.Log;
import com.swrve.sdk.SwrveTalk;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveCampaign {
    protected static final String LOG_TAG = "SwrveMessagingSDK";
    protected static Random rnd = new Random();
    protected int dismissalsRemaining;
    protected boolean dismissalsRemainingEnabled;
    protected Date endDate;
    protected int id;
    protected List<SwrveMessage> messages;
    protected int next;
    protected boolean randomOrder;
    protected Date startDate;
    protected Set<String> triggers;

    public SwrveCampaign() {
        this.messages = new ArrayList();
        this.triggers = new HashSet();
        this.dismissalsRemainingEnabled = false;
    }

    public int getId() {
        return this.id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public List<SwrveMessage> getMessages() {
        return this.messages;
    }

    protected void setMessages(List<SwrveMessage> messages) {
        this.messages = messages;
    }

    protected void addMessage(SwrveMessage message) {
        this.messages.add(message);
    }

    public int getNext() {
        return this.next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public Set<String> getTriggers() {
        return this.triggers;
    }

    protected void setTriggers(Set<String> triggers) {
        this.triggers = triggers;
    }

    public boolean isRandomOrder() {
        return this.randomOrder;
    }

    protected void setRandomOrder(boolean randomOrder) {
        this.randomOrder = randomOrder;
    }

    public int getDismissalsRemaining() {
        return this.dismissalsRemaining;
    }

    public void setDismissalsRemaining(int dismissalsRemaining) {
        this.dismissalsRemaining = dismissalsRemaining;
        this.dismissalsRemainingEnabled = true;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    protected void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    protected void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SwrveMessage getMessageForEvent(String event, Date now) {
        int messagesCount = this.messages.size();
        if (messagesCount == 0) {
            Log.i(LOG_TAG, "No messages in campaign " + this.id);
            return null;
        }
        if (this.dismissalsRemainingEnabled && this.dismissalsRemaining <= 0) {
            Log.i(LOG_TAG, "Campaign " + this.id + " has been dismissed too many times already");
            return null;
        }
        if (this.startDate.after(now)) {
            Log.i(LOG_TAG, "Campaign " + this.id + " has not started yet");
            return null;
        }
        if (this.endDate.before(now)) {
            Log.i(LOG_TAG, "Campaign" + this.id + " has finished");
            return null;
        }
        if (!this.triggers.contains(event)) {
            Log.i(LOG_TAG, "There is no trigger in " + this.id + " that matches " + event);
            return null;
        }
        Log.i(LOG_TAG, event + " matches a trigger in " + this.id);
        return getNextMessage(messagesCount);
    }

    public SwrveMessage getMessageForId(int messageId) {
        int messagesCount = this.messages.size();
        if (messagesCount == 0) {
            Log.i(LOG_TAG, "No messages in campaign " + this.id);
            return null;
        }
        for (SwrveMessage message : this.messages) {
            if (message.getId() == messageId) {
                return message;
            }
        }
        return null;
    }

    protected SwrveMessage getNextMessage(int messagesCount) {
        int selection = this.next;
        if (this.randomOrder) {
            selection = rnd.nextInt(messagesCount);
            Log.i(LOG_TAG, "Random Message in " + this.id + " is " + this.next);
        }
        if (selection < messagesCount) {
            return this.messages.get(selection);
        }
        return null;
    }

    public SwrveCampaign(SwrveTalk controller, JSONObject campaignData, Set<String> assetsQueue) throws JSONException {
        this();
        setId(campaignData.getInt("id"));
        Log.i(LOG_TAG, "Loading campaign " + getId());
        assignCampaignTriggers(this, campaignData);
        assignCampaignRules(this, campaignData);
        assignCampaignDates(this, campaignData);
        JSONArray jsonMessages = campaignData.getJSONArray("messages");
        int t = jsonMessages.length();
        for (int k = 0; k < t; k++) {
            JSONObject messageData = jsonMessages.getJSONObject(k);
            SwrveMessage message = createMessage(controller, this, messageData);
            if (message.getFormats().size() > 0 && assetsQueue != null) {
                for (SwrveMessageFormat format : message.getFormats()) {
                    for (SwrveButton button : format.getButtons()) {
                        if (button.getImage() != null && !button.getImage().equals("")) {
                            assetsQueue.add(button.getImage());
                        }
                    }
                    for (SwrveImage image : format.getImages()) {
                        if (image.getFile() != null && !image.getFile().equals("")) {
                            assetsQueue.add(image.getFile());
                        }
                    }
                }
            }
            if (message.getFormats().size() > 0) {
                addMessage(message);
            }
        }
    }

    protected SwrveMessage createMessage(SwrveTalk controller, SwrveCampaign swrveCampaign, JSONObject messageData) throws JSONException {
        return new SwrveMessage(controller, swrveCampaign, messageData);
    }

    protected void assignCampaignTriggers(SwrveCampaign campaign, JSONObject campaignData) throws JSONException {
        JSONArray jsonTriggers = campaignData.getJSONArray("triggers");
        int j = jsonTriggers.length();
        for (int i = 0; i < j; i++) {
            String trigger = jsonTriggers.getString(i);
            campaign.getTriggers().add(trigger);
        }
    }

    protected void assignCampaignRules(SwrveCampaign campaign, JSONObject campaignData) throws JSONException {
        int dismissals;
        JSONObject rules = campaignData.getJSONObject("rules");
        campaign.setRandomOrder(rules.getString("display_order").equals("random"));
        if (rules.has("dismiss_after_views") && (dismissals = rules.getInt("dismiss_after_views")) > 0) {
            campaign.setDismissalsRemaining(dismissals);
        }
    }

    protected void assignCampaignDates(SwrveCampaign campaign, JSONObject campaignData) throws JSONException {
        campaign.setStartDate(new Date(campaignData.getLong("start_date")));
        campaign.setEndDate(new Date(campaignData.getLong("end_date")));
    }

    public void decrementDismissalsRemaining() {
        this.dismissalsRemaining--;
    }

    public JSONObject createSettings() throws JSONException {
        JSONObject settings = new JSONObject();
        settings.put("next", this.next);
        return settings;
    }

    public void loadSettings(JSONObject settings) throws JSONException {
        try {
            this.next = settings.getInt("next");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while trying to load campaign settings", e);
        }
    }
}
