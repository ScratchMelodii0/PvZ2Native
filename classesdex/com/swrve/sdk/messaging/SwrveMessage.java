package com.swrve.sdk.messaging;

import com.swrve.sdk.SwrveTalk;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveMessage {
    protected File cacheDir;
    protected SwrveCampaign campaign;
    protected List<SwrveMessageFormat> formats;
    protected int id;
    protected SwrveTalk messageController;
    protected String name;

    public SwrveMessage(SwrveTalk messageController, SwrveCampaign campaign) {
        this.messageController = messageController;
        this.campaign = campaign;
        this.formats = new ArrayList();
    }

    public int getId() {
        return this.id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public List<SwrveMessageFormat> getFormats() {
        return this.formats;
    }

    protected void setFormats(List<SwrveMessageFormat> formats) {
        this.formats = formats;
    }

    public File getCacheDir() {
        return this.cacheDir;
    }

    protected void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public SwrveCampaign getCampaign() {
        return this.campaign;
    }

    protected void setCampaign(SwrveCampaign campaign) {
        this.campaign = campaign;
    }

    public SwrveMessage(SwrveTalk controller, SwrveCampaign campaign, JSONObject messageData) throws JSONException {
        this(controller, campaign);
        setId(messageData.getInt("id"));
        setName(messageData.getString("name"));
        setCacheDir(controller.getCacheDir());
        JSONObject template = messageData.getJSONObject("template");
        JSONArray jsonFormats = template.getJSONArray("formats");
        int j = jsonFormats.length();
        for (int i = 0; i < j; i++) {
            JSONObject messageFormatData = jsonFormats.getJSONObject(i);
            SwrveMessageFormat messageFormat = createMessageFormat(this, messageFormatData);
            getFormats().add(messageFormat);
        }
    }

    protected SwrveMessageFormat createMessageFormat(SwrveMessage swrveMessage, JSONObject messageFormatData) throws JSONException {
        return new SwrveMessageFormat(swrveMessage, messageFormatData);
    }

    public SwrveTalk getMessageController() {
        return this.messageController;
    }

    public SwrveMessageFormat getFormat(SwrveOrientation orientation) {
        if (this.formats != null && this.formats.size() > 0) {
            if (orientation == SwrveOrientation.Both) {
                return this.formats.get(0);
            }
            for (SwrveMessageFormat proposedFormat : this.formats) {
                if (proposedFormat.getOrientation() == orientation) {
                    return proposedFormat;
                }
            }
        }
        return null;
    }
}
