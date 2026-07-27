package com.popcap.SexyAppFramework;

import android.util.Base64;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidHttpTransaction implements Runnable {
    private static final String TAG = "AndroidHttpTransaction.java";
    private final int DEFAULT_TIMEOUT = 30;
    private HttpURLConnection mConnection;
    private long mNativeTransaction;
    private byte[] mRequestBody;
    private Thread mThread;

    native void HttpReceivedData(long j, byte[] bArr, int i);

    native void HttpReceivedResponse(long j);

    native void HttpTransactionCleanup(long j);

    native void HttpTransactionComplete(long j);

    native void HttpTransactionError(long j);

    public AndroidHttpTransaction(long nativeTransaction, String method, String url) throws IOException {
        Log.v(TAG, String.format("AndroidHttpTransaction( \"%s\", \"%s\" )\n", method, url));
        this.mNativeTransaction = nativeTransaction;
        String userAgent = System.getProperties().getProperty("http.agent");
        this.mConnection = (HttpURLConnection) new URL(url).openConnection();
        this.mConnection.setRequestProperty("User-Agent", userAgent);
        Log.v(TAG, "Setting user-agent to : " + userAgent);
        this.mConnection.setRequestMethod(method);
        this.mConnection.setDoInput(true);
    }

    public void SetRequestHeader(String name, String value) {
        this.mConnection.setRequestProperty(name, value);
    }

    public void SetRequestBody(byte[] body) {
        this.mRequestBody = body;
        this.mConnection.setDoOutput(true);
    }

    public byte[] GetRequestBody() {
        return this.mRequestBody;
    }

    public void SetBasicAuth(String userName, String password) {
        String auth = userName + ":" + password;
        this.mConnection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), 2));
    }

    public String GetHumanReadableUrl() {
        return this.mConnection.getURL().toExternalForm();
    }

    public void SetTimeout(int seconds) {
        this.mConnection.setReadTimeout(seconds * 1000);
        this.mConnection.setConnectTimeout(seconds * 1000);
        Log.v(TAG, "SetTimeout():" + this + ":" + Thread.currentThread().getName() + " connectTimeout = " + this.mConnection.getConnectTimeout() + ", readTimeout" + this.mConnection.getReadTimeout());
    }

    public void Start() {
        Log.v(TAG, "Start():" + this + ":" + Thread.currentThread().getName());
        this.mThread = new Thread(this);
        this.mThread.start();
    }

    @Override // java.lang.Runnable
    public void run() {
        int bytesRead;
        try {
            Log.v(TAG, "run():" + this + ":" + Thread.currentThread().getName() + " connectTimeout = " + this.mConnection.getConnectTimeout() + ", readTimeout " + this.mConnection.getReadTimeout());
            if (this.mConnection.getConnectTimeout() == 0) {
                this.mConnection.setConnectTimeout(30000);
                Log.v(TAG, "Setting connect timeout to the default of 30 seconds");
            }
            if (this.mConnection.getReadTimeout() == 0) {
                Log.v(TAG, "Setting read timeout to the default of 30 seconds");
                this.mConnection.setReadTimeout(30000);
            }
            this.mConnection.connect();
            if (this.mRequestBody != null) {
                OutputStream requestStream = this.mConnection.getOutputStream();
                requestStream.write(this.mRequestBody);
                requestStream.close();
            }
            InputStream responseStream = this.mConnection.getInputStream();
            FireReceivedResponse();
            byte[] buffer = new byte[16384];
            while (0 != this.mNativeTransaction && (bytesRead = responseStream.read(buffer)) != -1) {
                FireReceivedData(buffer, bytesRead);
            }
            responseStream.close();
            FireTransactionComplete();
        } catch (IOException ex) {
            Log.v(TAG, "IOExcp():" + ex.toString() + ":" + Thread.currentThread().getName() + ", nativePtr:" + this.mNativeTransaction);
            FireTransactionError();
        }
    }

    public void Release() {
        synchronized (this) {
            Log.v(TAG, "Release():" + this + ":" + Thread.currentThread().getName() + ", nativePtr:" + this.mNativeTransaction);
            HttpTransactionCleanup(this.mNativeTransaction);
            this.mNativeTransaction = 0L;
        }
    }

    private synchronized void FireReceivedResponse() {
        Log.v(TAG, "FireReceivedResponse():" + this + ":" + Thread.currentThread().getName() + ", nativePtr:" + this.mNativeTransaction);
        if (0 != this.mNativeTransaction) {
            HttpReceivedResponse(this.mNativeTransaction);
        }
    }

    private synchronized void FireReceivedData(byte[] data, int dataSize) {
        Log.v(TAG, "FireReceivedData():" + this + ":" + Thread.currentThread().getName() + ", nativePtr:" + this.mNativeTransaction);
        if (0 != this.mNativeTransaction) {
            HttpReceivedData(this.mNativeTransaction, data, dataSize);
        }
    }

    private synchronized void FireTransactionComplete() {
        Log.v(TAG, "FireTransactionComplete():" + this + ":" + Thread.currentThread().getName() + ", nativePtr:" + this.mNativeTransaction);
        if (0 != this.mNativeTransaction) {
            HttpTransactionComplete(this.mNativeTransaction);
        }
    }

    private synchronized void FireTransactionError() {
        Log.v(TAG, "FireTransactionError():" + this + ":" + Thread.currentThread().getName() + ", nativePtr:" + this.mNativeTransaction);
        if (0 != this.mNativeTransaction) {
            HttpTransactionError(this.mNativeTransaction);
        }
    }

    public int GetStatusCode() {
        try {
            int statusCode = this.mConnection.getResponseCode();
            Log.v(TAG, String.format("%d <- GetStatusCode()", Integer.valueOf(statusCode)));
            return statusCode;
        } catch (IOException e) {
            return 0;
        }
    }

    public String GetStatusLine() {
        try {
            String statusLine = this.mConnection.getResponseMessage();
            Log.v(TAG, String.format("\"%s\" <- GetStatusLine()", statusLine));
            return statusLine;
        } catch (IOException e) {
            return null;
        }
    }

    public int GetResponseLength() {
        return this.mConnection.getContentLength();
    }

    public String GetResponseHeader(String key) {
        String responseHeader = this.mConnection.getHeaderField(key);
        Log.v(TAG, String.format("\"%s\" <- GetResponseHeader( \"%s\" )", responseHeader, key));
        return responseHeader;
    }
}
