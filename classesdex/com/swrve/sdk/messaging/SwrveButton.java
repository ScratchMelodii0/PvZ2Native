package com.swrve.sdk.messaging;

import android.graphics.Point;
import com.facebook.internal.ServerProtocol;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveButton extends SwrveWidget {
    protected String action;
    protected SwrveActionType actionType;
    protected int gameId;
    protected String image;
    protected SwrveMessage message;
    protected String name;

    @Override // com.swrve.sdk.messaging.SwrveWidget
    public /* bridge */ /* synthetic */ Point getPosition() {
        return super.getPosition();
    }

    @Override // com.swrve.sdk.messaging.SwrveWidget
    public /* bridge */ /* synthetic */ Point getSize() {
        return super.getSize();
    }

    public SwrveButton() {
    }

    public SwrveButton(SwrveMessage message, JSONObject buttonData) throws JSONException {
        String gameIdStr;
        if (buttonData.has("name")) {
            setName(buttonData.getString("name"));
        }
        setPosition(getCenterFrom(buttonData));
        setSize(getSizeFrom(buttonData));
        setImage(buttonData.getJSONObject("image_up").getString("value"));
        setMessage(message);
        if (buttonData.has("game_id") && (gameIdStr = buttonData.getJSONObject("game_id").getString("value")) != null && !gameIdStr.equals("")) {
            int gameId = Integer.parseInt(gameIdStr);
            setGameId(gameId);
        }
        setAction(buttonData.getJSONObject("action").getString("value"));
        setActionType(SwrveActionType.parse(buttonData.getJSONObject(ServerProtocol.DIALOG_PARAM_TYPE).getString("value")));
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return this.image;
    }

    protected void setImage(String image) {
        this.image = image;
    }

    public String getAction() {
        return this.action;
    }

    protected void setAction(String action) {
        this.action = action;
    }

    public SwrveMessage getMessage() {
        return this.message;
    }

    protected void setMessage(SwrveMessage message) {
        this.message = message;
    }

    public int getGameId() {
        return this.gameId;
    }

    protected void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public SwrveActionType getActionType() {
        return this.actionType;
    }

    protected void setActionType(SwrveActionType actionType) {
        this.actionType = actionType;
    }
}
