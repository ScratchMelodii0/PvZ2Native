package com.swrve.sdk.messaging;

import android.graphics.Point;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveImage extends SwrveWidget {
    protected String file;

    @Override // com.swrve.sdk.messaging.SwrveWidget
    public /* bridge */ /* synthetic */ Point getPosition() {
        return super.getPosition();
    }

    @Override // com.swrve.sdk.messaging.SwrveWidget
    public /* bridge */ /* synthetic */ Point getSize() {
        return super.getSize();
    }

    public SwrveImage() {
    }

    public SwrveImage(SwrveMessage message, JSONObject imageData) throws JSONException {
        setPosition(getCenterFrom(imageData));
        setSize(getSizeFrom(imageData));
        setFile(imageData.getJSONObject("image").getString("value"));
    }

    public String getFile() {
        return this.file;
    }

    protected void setFile(String file) {
        this.file = file;
    }
}
