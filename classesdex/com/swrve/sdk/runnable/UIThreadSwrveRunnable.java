package com.swrve.sdk.runnable;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public abstract class UIThreadSwrveRunnable implements Runnable {
    protected Exception exception;

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
