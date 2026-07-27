package com.swrve.sdk.messaging;

import com.swrve.sdk.ISwrveEventListener;
import com.swrve.sdk.SwrveTalk;
import java.lang.ref.WeakReference;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveEventListener implements ISwrveEventListener {
    private ISwrveMessageListener messageListener;
    private WeakReference<SwrveTalk> talk;

    public SwrveEventListener(SwrveTalk talk, ISwrveMessageListener messageListener) {
        this.talk = new WeakReference<>(talk);
        this.messageListener = messageListener;
    }

    @Override // com.swrve.sdk.ISwrveEventListener
    public void onEvent(String eventName) {
        SwrveMessage message;
        SwrveTalk talkRef = this.talk.get();
        if (talkRef != null && this.messageListener != null && (message = talkRef.getMessageForEvent(eventName)) != null) {
            this.messageListener.onMessage(message);
        }
    }
}
