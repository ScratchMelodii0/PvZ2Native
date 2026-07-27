package com.facebook.widget;

import android.content.Context;
import com.facebook.internal.FileLruCache;
import com.facebook.internal.Utility;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class UrlRedirectCache {
    private static volatile FileLruCache urlRedirectCache;
    static final String TAG = UrlRedirectCache.class.getSimpleName();
    private static final String REDIRECT_CONTENT_TAG = TAG + "_Redirect";

    UrlRedirectCache() {
    }

    static synchronized FileLruCache getCache(Context context) throws IOException {
        if (urlRedirectCache == null) {
            urlRedirectCache = new FileLruCache(context.getApplicationContext(), TAG, new FileLruCache.Limits());
        }
        return urlRedirectCache;
    }

    static URL getRedirectedUrl(Context context, URL url) throws Throwable {
        FileLruCache cache;
        boolean redirectExists;
        InputStreamReader reader;
        if (url == null) {
            return null;
        }
        String urlString = url.toString();
        URL finalUrl = null;
        InputStreamReader reader2 = null;
        try {
            cache = getCache(context);
            redirectExists = false;
        } catch (MalformedURLException e) {
        } catch (IOException e2) {
        } catch (Throwable th) {
            th = th;
        }
        while (true) {
            try {
                reader = reader2;
                InputStream stream = cache.get(urlString, REDIRECT_CONTENT_TAG);
                if (stream == null) {
                    break;
                }
                redirectExists = true;
                reader2 = new InputStreamReader(stream);
                char[] buffer = new char[128];
                StringBuilder urlBuilder = new StringBuilder();
                while (true) {
                    int bufferLength = reader2.read(buffer, 0, buffer.length);
                    if (bufferLength <= 0) {
                        break;
                    }
                    urlBuilder.append(buffer, 0, bufferLength);
                }
                Utility.closeQuietly(reader2);
                urlString = urlBuilder.toString();
            } catch (MalformedURLException e3) {
                reader2 = reader;
            } catch (IOException e4) {
                reader2 = reader;
                Utility.closeQuietly(reader2);
                return null;
            } catch (Throwable th2) {
                th = th2;
                reader2 = reader;
                Utility.closeQuietly(reader2);
                throw th;
            }
            Utility.closeQuietly(reader2);
            return null;
        }
        if (redirectExists) {
            URL finalUrl2 = new URL(urlString);
            finalUrl = finalUrl2;
        }
        Utility.closeQuietly(reader);
        return finalUrl;
    }

    static void cacheUrlRedirect(Context context, URL fromUrl, URL toUrl) {
        if (fromUrl != null && toUrl != null) {
            OutputStream redirectStream = null;
            try {
                FileLruCache cache = getCache(context);
                redirectStream = cache.openPutStream(fromUrl.toString(), REDIRECT_CONTENT_TAG);
                redirectStream.write(toUrl.toString().getBytes());
            } catch (IOException e) {
            } finally {
                Utility.closeQuietly(redirectStream);
            }
        }
    }
}
