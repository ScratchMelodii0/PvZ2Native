package com.facebook.widget;

import android.content.Context;
import com.facebook.LoggingBehavior;
import com.facebook.internal.FileLruCache;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class ImageResponseCache {
    static final String TAG = ImageResponseCache.class.getSimpleName();
    private static volatile FileLruCache imageCache;

    ImageResponseCache() {
    }

    static synchronized FileLruCache getCache(Context context) throws IOException {
        if (imageCache == null) {
            imageCache = new FileLruCache(context.getApplicationContext(), TAG, new FileLruCache.Limits());
        }
        return imageCache;
    }

    static InputStream getCachedImageStream(URL url, Context context) {
        if (url == null || !isCDNURL(url)) {
            return null;
        }
        try {
            FileLruCache cache = getCache(context);
            InputStream imageStream = cache.get(url.toString());
            return imageStream;
        } catch (IOException e) {
            Logger.log(LoggingBehavior.CACHE, 5, TAG, e.toString());
            return null;
        }
    }

    static InputStream interceptAndCacheImageStream(Context context, HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() != 200) {
            return null;
        }
        URL url = connection.getURL();
        InputStream stream = connection.getInputStream();
        if (isCDNURL(url)) {
            try {
                FileLruCache cache = getCache(context);
                return cache.interceptAndPut(url.toString(), new BufferedHttpInputStream(stream, connection));
            } catch (IOException e) {
                return stream;
            }
        }
        return stream;
    }

    private static boolean isCDNURL(URL url) {
        if (url != null) {
            String uriHost = url.getHost();
            if (uriHost.endsWith("fbcdn.net")) {
                return true;
            }
            if (uriHost.startsWith("fbcdn") && uriHost.endsWith("akamaihd.net")) {
                return true;
            }
        }
        return false;
    }

    private static class BufferedHttpInputStream extends BufferedInputStream {
        HttpURLConnection connection;

        BufferedHttpInputStream(InputStream stream, HttpURLConnection connection) {
            super(stream, 8192);
            this.connection = connection;
        }

        @Override // java.io.BufferedInputStream, java.io.FilterInputStream, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            super.close();
            Utility.disconnectQuietly(this.connection);
        }
    }
}
