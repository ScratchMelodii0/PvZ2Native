package com.facebook.widget;

import android.graphics.Bitmap;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class ImageResponse {
    private Bitmap bitmap;
    private Exception error;
    private boolean isCachedRedirect;
    private ImageRequest request;

    ImageResponse(ImageRequest request, Exception error, boolean isCachedRedirect, Bitmap bitmap) {
        this.request = request;
        this.error = error;
        this.bitmap = bitmap;
        this.isCachedRedirect = isCachedRedirect;
    }

    ImageRequest getRequest() {
        return this.request;
    }

    Exception getError() {
        return this.error;
    }

    Bitmap getBitmap() {
        return this.bitmap;
    }

    boolean isCachedRedirect() {
        return this.isCachedRedirect;
    }
}
