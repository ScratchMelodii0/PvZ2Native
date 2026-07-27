package com.swrve.sdk.messaging;

import android.graphics.Point;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
abstract class SwrveWidget {
    protected Point position;
    protected Point size;

    SwrveWidget() {
    }

    public Point getPosition() {
        return this.position;
    }

    protected void setPosition(Point position) {
        this.position = position;
    }

    public Point getSize() {
        return this.size;
    }

    protected void setSize(Point size) {
        this.size = size;
    }

    protected static Point getSizeFrom(JSONObject data) throws JSONException {
        return new Point(data.getJSONObject("w").getInt("value"), data.getJSONObject("h").getInt("value"));
    }

    protected static Point getCenterFrom(JSONObject data) throws JSONException {
        return new Point(data.getJSONObject("x").getInt("value"), data.getJSONObject("y").getInt("value"));
    }
}
