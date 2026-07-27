package com.swrve.sdk.messaging;

import android.graphics.Point;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveMessageFormat {
    protected static final String LOG_TAG = "SwrveMessagingSDK";
    protected List<SwrveButton> buttons;
    protected List<SwrveImage> images;
    protected String language;
    protected SwrveMessage message;
    protected String name;
    protected SwrveOrientation orientation;
    protected float scale;
    protected Point size;

    public SwrveMessageFormat(SwrveMessage message) {
        this.message = message;
        this.buttons = new ArrayList();
        this.images = new ArrayList();
        this.scale = 1.0f;
    }

    public SwrveMessage getMessage() {
        return this.message;
    }

    public List<SwrveButton> getButtons() {
        return this.buttons;
    }

    protected void setButtons(List<SwrveButton> buttons) {
        this.buttons = buttons;
    }

    public List<SwrveImage> getImages() {
        return this.images;
    }

    protected void setImages(List<SwrveImage> images) {
        this.images = images;
    }

    public String getName() {
        return this.name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return this.language;
    }

    protected void setLanguage(String language) {
        this.language = language;
    }

    public Point getSize() {
        return this.size;
    }

    protected void setSize(Point size) {
        this.size = size;
    }

    public SwrveOrientation getOrientation() {
        return this.orientation;
    }

    protected void setOrientation(SwrveOrientation orientation) {
        this.orientation = orientation;
    }

    public float getScale() {
        return this.scale;
    }

    protected void setScale(float scale) {
        this.scale = scale;
    }

    public SwrveMessageFormat(SwrveMessage message, JSONObject messageFormatData) throws JSONException {
        this(message);
        setName(messageFormatData.getString("name"));
        setLanguage(messageFormatData.getString("language"));
        if (messageFormatData.has("orientation")) {
            setOrientation(SwrveOrientation.parse(messageFormatData.getString("orientation")));
        }
        if (messageFormatData.has("scale")) {
            setScale(Float.parseFloat(messageFormatData.getString("scale")));
        }
        setSize(getSizeFrom(messageFormatData.getJSONObject("size")));
        Log.i(LOG_TAG, "Format " + getName() + " Size: " + this.size.x + "x" + this.size.y + " scale " + this.scale);
        JSONArray jsonButtons = messageFormatData.getJSONArray("buttons");
        int j = jsonButtons.length();
        for (int i = 0; i < j; i++) {
            SwrveButton button = new SwrveButton(message, jsonButtons.getJSONObject(i));
            getButtons().add(button);
        }
        JSONArray jsonImages = messageFormatData.getJSONArray("images");
        int ji = jsonImages.length();
        for (int ii = 0; ii < ji; ii++) {
            SwrveImage image = new SwrveImage(message, jsonImages.getJSONObject(ii));
            getImages().add(image);
        }
    }

    protected static Point getSizeFrom(JSONObject data) throws JSONException {
        return new Point(data.getJSONObject("w").getInt("value"), data.getJSONObject("h").getInt("value"));
    }
}
