package com.facebook.widget;

import android.content.Context;
import android.net.Uri;
import com.facebook.internal.Validate;
import java.net.MalformedURLException;
import java.net.URL;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class ImageRequest {
    private static final String HEIGHT_PARAM = "height";
    private static final String MIGRATION_PARAM = "migration_overrides";
    private static final String MIGRATION_VALUE = "{october_2012:true}";
    private static final String PROFILEPIC_URL_FORMAT = "https://graph.facebook.com/%s/picture";
    static final int UNSPECIFIED_DIMENSION = 0;
    private static final String WIDTH_PARAM = "width";
    private boolean allowCachedRedirects;
    private Callback callback;
    private Object callerTag;
    private Context context;
    private URL imageUrl;

    interface Callback {
        void onCompleted(ImageResponse imageResponse);
    }

    static URL getProfilePictureUrl(String userId, int width, int height) throws MalformedURLException {
        Validate.notNullOrEmpty(userId, "userId");
        int width2 = Math.max(width, 0);
        int height2 = Math.max(height, 0);
        if (width2 == 0 && height2 == 0) {
            throw new IllegalArgumentException("Either width or height must be greater than 0");
        }
        Uri.Builder builder = new Uri.Builder().encodedPath(String.format(PROFILEPIC_URL_FORMAT, userId));
        if (height2 != 0) {
            builder.appendQueryParameter(HEIGHT_PARAM, String.valueOf(height2));
        }
        if (width2 != 0) {
            builder.appendQueryParameter(WIDTH_PARAM, String.valueOf(width2));
        }
        builder.appendQueryParameter(MIGRATION_PARAM, MIGRATION_VALUE);
        return new URL(builder.toString());
    }

    private ImageRequest(Builder builder) {
        this.context = builder.context;
        this.imageUrl = builder.imageUrl;
        this.callback = builder.callback;
        this.allowCachedRedirects = builder.allowCachedRedirects;
        this.callerTag = builder.callerTag == null ? new Object() : builder.callerTag;
    }

    Context getContext() {
        return this.context;
    }

    URL getImageUrl() {
        return this.imageUrl;
    }

    Callback getCallback() {
        return this.callback;
    }

    boolean isCachedRedirectAllowed() {
        return this.allowCachedRedirects;
    }

    Object getCallerTag() {
        return this.callerTag;
    }

    static class Builder {
        private boolean allowCachedRedirects;
        private Callback callback;
        private Object callerTag;
        private Context context;
        private URL imageUrl;

        Builder(Context context, URL imageUrl) {
            Validate.notNull(imageUrl, "imageUrl");
            this.context = context;
            this.imageUrl = imageUrl;
        }

        Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        Builder setCallerTag(Object callerTag) {
            this.callerTag = callerTag;
            return this;
        }

        Builder setAllowCachedRedirects(boolean allowCachedRedirects) {
            this.allowCachedRedirects = allowCachedRedirects;
            return this;
        }

        ImageRequest build() {
            return new ImageRequest(this);
        }
    }
}
