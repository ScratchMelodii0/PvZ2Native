package com.google.android.vending.expansion.downloader.impl;

import android.content.Context;
import android.net.Proxy;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import com.google.android.vending.expansion.downloader.Constants;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class DownloadThread {
    private Context mContext;
    private final DownloadsDB mDB;
    private DownloadInfo mInfo;
    private final DownloadNotification mNotification;
    private DownloaderService mService;
    private String mUserAgent;

    public DownloadThread(DownloadInfo info, DownloaderService service, DownloadNotification notification) {
        this.mContext = service;
        this.mInfo = info;
        this.mService = service;
        this.mNotification = notification;
        this.mDB = DownloadsDB.getDB(service);
        this.mUserAgent = "APKXDL (Linux; U; Android " + Build.VERSION.RELEASE + ";" + Locale.getDefault().toString() + "; " + Build.DEVICE + "/" + Build.ID + ")" + service.getPackageName();
    }

    private String userAgent() {
        return this.mUserAgent;
    }

    private static class State {
        public String mFilename;
        public String mNewUri;
        public int mRedirectCount;
        public String mRequestUri;
        public FileOutputStream mStream;
        public boolean mCountRetry = false;
        public int mRetryAfter = 0;
        public boolean mGotData = false;

        public State(DownloadInfo info, DownloaderService service) {
            this.mRedirectCount = 0;
            this.mRedirectCount = info.mRedirectCount;
            this.mRequestUri = info.mUri;
            this.mFilename = service.generateTempSaveFileName(info.mFileName);
        }
    }

    private static class InnerState {
        public int mBytesNotified;
        public int mBytesSoFar;
        public int mBytesThisSession;
        public boolean mContinuingDownload;
        public String mHeaderContentDisposition;
        public String mHeaderContentLength;
        public String mHeaderContentLocation;
        public String mHeaderETag;
        public long mTimeLastNotification;

        private InnerState() {
            this.mBytesSoFar = 0;
            this.mBytesThisSession = 0;
            this.mContinuingDownload = false;
            this.mBytesNotified = 0;
            this.mTimeLastNotification = 0L;
        }
    }

    private class StopRequest extends Throwable {
        private static final long serialVersionUID = 6338592678988347973L;
        public int mFinalStatus;

        public StopRequest(int finalStatus, String message) {
            super(message);
            this.mFinalStatus = finalStatus;
        }

        public StopRequest(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            this.mFinalStatus = finalStatus;
        }
    }

    private class RetryDownload extends Throwable {
        private static final long serialVersionUID = 6196036036517540229L;

        private RetryDownload() {
        }
    }

    public HttpHost getPreferredHttpHost(Context context, String url) {
        String proxyHost;
        if (isLocalHost(url) || this.mService.isWiFi() || (proxyHost = Proxy.getHost(context)) == null) {
            return null;
        }
        return new HttpHost(proxyHost, Proxy.getPort(context), "http");
    }

    private static final boolean isLocalHost(String url) {
        if (url == null) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            if (!host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1")) {
                if (!host.equals("[::1]")) {
                    return false;
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void run() {
        Process.setThreadPriority(10);
        State state = new State(this.mInfo, this.mService);
        AndroidHttpClient client = null;
        PowerManager.WakeLock wakeLock = null;
        try {
            try {
                try {
                    PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
                    PowerManager.WakeLock wakeLock2 = pm.newWakeLock(1, Constants.TAG);
                    wakeLock2.acquire();
                    AndroidHttpClient client2 = AndroidHttpClient.newInstance(userAgent(), this.mContext);
                    boolean finished = false;
                    while (!finished) {
                        ConnRouteParams.setDefaultProxy(client2.getParams(), getPreferredHttpHost(this.mContext, state.mRequestUri));
                        HttpGet request = new HttpGet(state.mRequestUri);
                        try {
                            executeDownload(state, client2, request);
                            finished = true;
                            request.abort();
                        } catch (RetryDownload e) {
                            request.abort();
                        } catch (Throwable th) {
                            request.abort();
                            throw th;
                        }
                    }
                    finalizeDestinationFile(state);
                    if (wakeLock2 != null) {
                        wakeLock2.release();
                    }
                    if (client2 != null) {
                        client2.close();
                    }
                    cleanupDestination(state, 200);
                    notifyDownloadCompleted(200, state.mCountRetry, state.mRetryAfter, state.mRedirectCount, state.mGotData, state.mFilename);
                } catch (StopRequest error) {
                    Log.w(Constants.TAG, "Aborting request for download " + this.mInfo.mFileName + ": " + error.getMessage());
                    error.printStackTrace();
                    int finalStatus = error.mFinalStatus;
                    if (0 != 0) {
                        wakeLock.release();
                    }
                    if (0 != 0) {
                        client.close();
                    }
                    cleanupDestination(state, finalStatus);
                    notifyDownloadCompleted(finalStatus, state.mCountRetry, state.mRetryAfter, state.mRedirectCount, state.mGotData, state.mFilename);
                }
            } catch (Throwable ex) {
                Log.w(Constants.TAG, "Exception for " + this.mInfo.mFileName + ": " + ex);
                if (0 != 0) {
                    wakeLock.release();
                }
                if (0 != 0) {
                    client.close();
                }
                cleanupDestination(state, 491);
                notifyDownloadCompleted(491, state.mCountRetry, state.mRetryAfter, state.mRedirectCount, state.mGotData, state.mFilename);
            }
        } catch (Throwable th2) {
            if (0 != 0) {
                wakeLock.release();
            }
            if (0 != 0) {
                client.close();
            }
            cleanupDestination(state, 491);
            notifyDownloadCompleted(491, state.mCountRetry, state.mRetryAfter, state.mRedirectCount, state.mGotData, state.mFilename);
            throw th2;
        }
    }

    private void executeDownload(State state, AndroidHttpClient client, HttpGet request) throws StopRequest, RetryDownload {
        InnerState innerState = new InnerState();
        byte[] data = new byte[4096];
        checkPausedOrCanceled(state);
        setupDestinationFile(state, innerState);
        addRequestHeaders(innerState, request);
        checkConnectivity(state);
        this.mNotification.onDownloadStateChanged(3);
        HttpResponse response = sendRequest(state, client, request);
        handleExceptionalStatus(state, innerState, response);
        processResponseHeaders(state, innerState, response);
        InputStream entityStream = openResponseEntity(state, response);
        this.mNotification.onDownloadStateChanged(4);
        transferData(state, innerState, data, entityStream);
    }

    private void checkConnectivity(State state) throws StopRequest {
        switch (this.mService.getNetworkAvailabilityState(this.mDB)) {
            case 1:
            case 4:
            default:
                return;
            case 2:
                throw new StopRequest(DownloaderService.STATUS_WAITING_FOR_NETWORK, "waiting for network to return");
            case 3:
                throw new StopRequest(DownloaderService.STATUS_QUEUED_FOR_WIFI, "waiting for wifi");
            case 5:
                throw new StopRequest(DownloaderService.STATUS_WAITING_FOR_NETWORK, "roaming is not allowed");
            case 6:
                throw new StopRequest(DownloaderService.STATUS_QUEUED_FOR_WIFI_OR_CELLULAR_PERMISSION, "waiting for wifi or for download over cellular to be authorized");
        }
    }

    private void transferData(State state, InnerState innerState, byte[] data, InputStream entityStream) throws StopRequest {
        while (true) {
            int bytesRead = readFromResponse(state, innerState, data, entityStream);
            if (bytesRead == -1) {
                handleEndOfStream(state, innerState);
                return;
            }
            state.mGotData = true;
            writeDataToDestination(state, data, bytesRead);
            innerState.mBytesSoFar += bytesRead;
            innerState.mBytesThisSession += bytesRead;
            reportProgress(state, innerState);
            checkPausedOrCanceled(state);
        }
    }

    private void finalizeDestinationFile(State state) throws Throwable {
        syncDestination(state);
        String tempFilename = state.mFilename;
        String finalFilename = Helpers.generateSaveFileName(this.mService, this.mInfo.mFileName);
        if (!state.mFilename.equals(finalFilename)) {
            File startFile = new File(tempFilename);
            File destFile = new File(finalFilename);
            if (this.mInfo.mTotalBytes != -1 && this.mInfo.mCurrentBytes == this.mInfo.mTotalBytes) {
                if (!startFile.renameTo(destFile)) {
                    throw new StopRequest(492, "unable to finalize destination file");
                }
                return;
            }
            throw new StopRequest(DownloaderService.STATUS_FILE_DELIVERED_INCORRECTLY, "file delivered with incorrect size. probably due to network not browser configured");
        }
    }

    private void cleanupDestination(State state, int finalStatus) {
        closeDestination(state);
        if (state.mFilename != null && DownloaderService.isStatusError(finalStatus)) {
            new File(state.mFilename).delete();
            state.mFilename = null;
        }
    }

    private void syncDestination(State state) throws Throwable {
        FileOutputStream downloadedFileStream;
        FileOutputStream downloadedFileStream2 = null;
        try {
            try {
                downloadedFileStream = new FileOutputStream(state.mFilename, true);
            } catch (Throwable th) {
                th = th;
            }
        } catch (FileNotFoundException e) {
            ex = e;
        } catch (SyncFailedException e2) {
            ex = e2;
        } catch (IOException e3) {
            ex = e3;
        } catch (RuntimeException e4) {
            ex = e4;
        }
        try {
            downloadedFileStream.getFD().sync();
            if (downloadedFileStream != null) {
                try {
                    downloadedFileStream.close();
                    downloadedFileStream2 = downloadedFileStream;
                } catch (IOException ex) {
                    Log.w(Constants.TAG, "IOException while closing synced file: ", ex);
                    downloadedFileStream2 = downloadedFileStream;
                } catch (RuntimeException ex2) {
                    Log.w(Constants.TAG, "exception while closing file: ", ex2);
                    downloadedFileStream2 = downloadedFileStream;
                }
            } else {
                downloadedFileStream2 = downloadedFileStream;
            }
        } catch (FileNotFoundException e5) {
            ex = e5;
            downloadedFileStream2 = downloadedFileStream;
            Log.w(Constants.TAG, "file " + state.mFilename + " not found: " + ex);
            if (downloadedFileStream2 != null) {
                try {
                    downloadedFileStream2.close();
                } catch (IOException ex3) {
                    Log.w(Constants.TAG, "IOException while closing synced file: ", ex3);
                } catch (RuntimeException ex4) {
                    Log.w(Constants.TAG, "exception while closing file: ", ex4);
                }
            }
        } catch (SyncFailedException e6) {
            ex = e6;
            downloadedFileStream2 = downloadedFileStream;
            Log.w(Constants.TAG, "file " + state.mFilename + " sync failed: " + ex);
            if (downloadedFileStream2 != null) {
                try {
                    downloadedFileStream2.close();
                } catch (IOException ex5) {
                    Log.w(Constants.TAG, "IOException while closing synced file: ", ex5);
                } catch (RuntimeException ex6) {
                    Log.w(Constants.TAG, "exception while closing file: ", ex6);
                }
            }
        } catch (IOException e7) {
            ex = e7;
            downloadedFileStream2 = downloadedFileStream;
            Log.w(Constants.TAG, "IOException trying to sync " + state.mFilename + ": " + ex);
            if (downloadedFileStream2 != null) {
                try {
                    downloadedFileStream2.close();
                } catch (IOException ex7) {
                    Log.w(Constants.TAG, "IOException while closing synced file: ", ex7);
                } catch (RuntimeException ex8) {
                    Log.w(Constants.TAG, "exception while closing file: ", ex8);
                }
            }
        } catch (RuntimeException e8) {
            ex = e8;
            downloadedFileStream2 = downloadedFileStream;
            Log.w(Constants.TAG, "exception while syncing file: ", ex);
            if (downloadedFileStream2 != null) {
                try {
                    downloadedFileStream2.close();
                } catch (IOException ex9) {
                    Log.w(Constants.TAG, "IOException while closing synced file: ", ex9);
                } catch (RuntimeException ex10) {
                    Log.w(Constants.TAG, "exception while closing file: ", ex10);
                }
            }
        } catch (Throwable th2) {
            th = th2;
            downloadedFileStream2 = downloadedFileStream;
            if (downloadedFileStream2 != null) {
                try {
                    downloadedFileStream2.close();
                } catch (IOException ex11) {
                    Log.w(Constants.TAG, "IOException while closing synced file: ", ex11);
                } catch (RuntimeException ex12) {
                    Log.w(Constants.TAG, "exception while closing file: ", ex12);
                }
            }
            throw th;
        }
    }

    private void closeDestination(State state) {
        try {
            if (state.mStream != null) {
                state.mStream.close();
                state.mStream = null;
            }
        } catch (IOException e) {
        }
    }

    private void checkPausedOrCanceled(State state) throws StopRequest {
        if (this.mService.getControl() == 1) {
            int status = this.mService.getStatus();
            switch (status) {
                case DownloaderService.STATUS_PAUSED_BY_APP /* 193 */:
                    throw new StopRequest(this.mService.getStatus(), "download paused");
                default:
                    return;
            }
        }
    }

    private void reportProgress(State state, InnerState innerState) {
        long now = System.currentTimeMillis();
        if (innerState.mBytesSoFar - innerState.mBytesNotified > 4096 && now - innerState.mTimeLastNotification > 1000) {
            this.mInfo.mCurrentBytes = innerState.mBytesSoFar;
            this.mDB.updateDownloadCurrentBytes(this.mInfo);
            innerState.mBytesNotified = innerState.mBytesSoFar;
            innerState.mTimeLastNotification = now;
            long totalBytesSoFar = ((long) innerState.mBytesThisSession) + this.mService.mBytesSoFar;
            this.mService.notifyUpdateBytes(totalBytesSoFar);
        }
    }

    private void writeDataToDestination(State state, byte[] data, int bytesRead) throws StopRequest {
        try {
            if (state.mStream == null) {
                state.mStream = new FileOutputStream(state.mFilename, true);
            }
            state.mStream.write(data, 0, bytesRead);
            closeDestination(state);
        } catch (IOException ex) {
            if (!Helpers.isExternalMediaMounted()) {
                throw new StopRequest(499, "external media not mounted while writing destination file");
            }
            long availableBytes = Helpers.getAvailableBytes(Helpers.getFilesystemRoot(state.mFilename));
            if (availableBytes < bytesRead) {
                throw new StopRequest(498, "insufficient space while writing destination file", ex);
            }
            throw new StopRequest(492, "while writing destination file: " + ex.toString(), ex);
        }
    }

    private void handleEndOfStream(State state, InnerState innerState) throws StopRequest {
        this.mInfo.mCurrentBytes = innerState.mBytesSoFar;
        this.mDB.updateDownload(this.mInfo);
        boolean lengthMismatched = (innerState.mHeaderContentLength == null || innerState.mBytesSoFar == Integer.parseInt(innerState.mHeaderContentLength)) ? false : true;
        if (lengthMismatched) {
            if (cannotResume(innerState)) {
                throw new StopRequest(489, "mismatched content length");
            }
            throw new StopRequest(getFinalStatusForHttpError(state), "closed socket before end of file");
        }
    }

    private boolean cannotResume(InnerState innerState) {
        return innerState.mBytesSoFar > 0 && innerState.mHeaderETag == null;
    }

    private int readFromResponse(State state, InnerState innerState, byte[] data, InputStream entityStream) throws StopRequest {
        try {
            return entityStream.read(data);
        } catch (IOException ex) {
            logNetworkState();
            this.mInfo.mCurrentBytes = innerState.mBytesSoFar;
            this.mDB.updateDownload(this.mInfo);
            if (cannotResume(innerState)) {
                String message = "while reading response: " + ex.toString() + ", can't resume interrupted download with no ETag";
                throw new StopRequest(489, message, ex);
            }
            throw new StopRequest(getFinalStatusForHttpError(state), "while reading response: " + ex.toString(), ex);
        }
    }

    private InputStream openResponseEntity(State state, HttpResponse response) throws StopRequest {
        try {
            return response.getEntity().getContent();
        } catch (IOException ex) {
            logNetworkState();
            throw new StopRequest(getFinalStatusForHttpError(state), "while getting entity: " + ex.toString(), ex);
        }
    }

    private void logNetworkState() {
        Log.i(Constants.TAG, "Net " + (this.mService.getNetworkAvailabilityState(this.mDB) == 1 ? "Up" : "Down"));
    }

    private void processResponseHeaders(State state, InnerState innerState, HttpResponse response) throws StopRequest {
        if (!innerState.mContinuingDownload) {
            readResponseHeaders(state, innerState, response);
            try {
                state.mFilename = this.mService.generateSaveFile(this.mInfo.mFileName, this.mInfo.mTotalBytes);
                try {
                    state.mStream = new FileOutputStream(state.mFilename);
                } catch (FileNotFoundException exc) {
                    File pathFile = new File(Helpers.getSaveFilePath(this.mService));
                    try {
                        if (pathFile.mkdirs()) {
                            state.mStream = new FileOutputStream(state.mFilename);
                        }
                    } catch (Exception e) {
                        throw new StopRequest(492, "while opening destination file: " + exc.toString(), exc);
                    }
                }
                updateDatabaseFromHeaders(state, innerState);
                checkConnectivity(state);
            } catch (DownloaderService.GenerateSaveFileError exc2) {
                throw new StopRequest(exc2.mStatus, exc2.mMessage);
            }
        }
    }

    private void updateDatabaseFromHeaders(State state, InnerState innerState) {
        this.mInfo.mETag = innerState.mHeaderETag;
        this.mDB.updateDownload(this.mInfo);
    }

    private void readResponseHeaders(State state, InnerState innerState, HttpResponse response) throws StopRequest {
        Header header;
        Header header2 = response.getFirstHeader("Content-Disposition");
        if (header2 != null) {
            innerState.mHeaderContentDisposition = header2.getValue();
        }
        Header header3 = response.getFirstHeader("Content-Location");
        if (header3 != null) {
            innerState.mHeaderContentLocation = header3.getValue();
        }
        Header header4 = response.getFirstHeader("ETag");
        if (header4 != null) {
            innerState.mHeaderETag = header4.getValue();
        }
        String headerTransferEncoding = null;
        Header header5 = response.getFirstHeader("Transfer-Encoding");
        if (header5 != null) {
            headerTransferEncoding = header5.getValue();
        }
        Header header6 = response.getFirstHeader("Content-Type");
        if (header6 != null) {
            String headerContentType = header6.getValue();
            if (!headerContentType.equals("application/vnd.android.obb")) {
                throw new StopRequest(DownloaderService.STATUS_FILE_DELIVERED_INCORRECTLY, "file delivered with incorrect Mime type");
            }
        }
        if (headerTransferEncoding == null && (header = response.getFirstHeader("Content-Length")) != null) {
            innerState.mHeaderContentLength = header.getValue();
            long contentLength = Long.parseLong(innerState.mHeaderContentLength);
            if (contentLength != -1 && contentLength != this.mInfo.mTotalBytes) {
                Log.e(Constants.TAG, "Incorrect file size delivered.");
            }
        }
        boolean noSizeInfo = innerState.mHeaderContentLength == null && (headerTransferEncoding == null || !headerTransferEncoding.equalsIgnoreCase("chunked"));
        if (noSizeInfo) {
            throw new StopRequest(495, "can't know size of download, giving up");
        }
    }

    private void handleExceptionalStatus(State state, InnerState innerState, HttpResponse response) throws StopRequest, RetryDownload {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 503 && this.mInfo.mNumFailed < 5) {
            handleServiceUnavailable(state, response);
        }
        if (statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307) {
            handleRedirect(state, response, statusCode);
        }
        int expectedStatus = innerState.mContinuingDownload ? 206 : 200;
        if (statusCode != expectedStatus) {
            handleOtherStatus(state, innerState, statusCode);
        } else {
            state.mRedirectCount = 0;
        }
    }

    private void handleOtherStatus(State state, InnerState innerState, int statusCode) throws StopRequest {
        int finalStatus;
        if (DownloaderService.isStatusError(statusCode)) {
            finalStatus = statusCode;
        } else if (statusCode >= 300 && statusCode < 400) {
            finalStatus = 493;
        } else if (innerState.mContinuingDownload && statusCode == 200) {
            finalStatus = 489;
        } else {
            finalStatus = 494;
        }
        throw new StopRequest(finalStatus, "http error " + statusCode);
    }

    private void handleRedirect(State state, HttpResponse response, int statusCode) throws StopRequest, RetryDownload {
        if (state.mRedirectCount >= 5) {
            throw new StopRequest(497, "too many redirects");
        }
        Header header = response.getFirstHeader("Location");
        if (header == null) {
            return;
        }
        try {
            String newUri = new URI(this.mInfo.mUri).resolve(new URI(header.getValue())).toString();
            state.mRedirectCount++;
            state.mRequestUri = newUri;
            if (statusCode == 301 || statusCode == 303) {
                state.mNewUri = newUri;
            }
            throw new RetryDownload();
        } catch (URISyntaxException e) {
            throw new StopRequest(495, "Couldn't resolve redirect URI");
        }
    }

    private void addRequestHeaders(InnerState innerState, HttpGet request) {
        if (innerState.mContinuingDownload) {
            if (innerState.mHeaderETag != null) {
                request.addHeader("If-Match", innerState.mHeaderETag);
            }
            request.addHeader("Range", "bytes=" + innerState.mBytesSoFar + Constants.FILENAME_SEQUENCE_SEPARATOR);
        }
    }

    private void handleServiceUnavailable(State state, HttpResponse response) throws StopRequest {
        state.mCountRetry = true;
        Header header = response.getFirstHeader("Retry-After");
        if (header != null) {
            try {
                state.mRetryAfter = Integer.parseInt(header.getValue());
                if (state.mRetryAfter < 0) {
                    state.mRetryAfter = 0;
                } else {
                    if (state.mRetryAfter < 30) {
                        state.mRetryAfter = 30;
                    } else if (state.mRetryAfter > 86400) {
                        state.mRetryAfter = Constants.MAX_RETRY_AFTER;
                    }
                    state.mRetryAfter += Helpers.sRandom.nextInt(31);
                    state.mRetryAfter *= 1000;
                }
            } catch (NumberFormatException e) {
            }
        }
        throw new StopRequest(DownloaderService.STATUS_WAITING_TO_RETRY, "got 503 Service Unavailable, will retry later");
    }

    private HttpResponse sendRequest(State state, AndroidHttpClient client, HttpGet request) throws StopRequest {
        try {
            return client.execute(request);
        } catch (IOException ex) {
            logNetworkState();
            throw new StopRequest(getFinalStatusForHttpError(state), "while trying to execute request: " + ex.toString(), ex);
        } catch (IllegalArgumentException ex2) {
            throw new StopRequest(495, "while trying to execute request: " + ex2.toString(), ex2);
        }
    }

    private int getFinalStatusForHttpError(State state) {
        if (this.mService.getNetworkAvailabilityState(this.mDB) != 1) {
            return DownloaderService.STATUS_WAITING_FOR_NETWORK;
        }
        if (this.mInfo.mNumFailed < 5) {
            state.mCountRetry = true;
            return DownloaderService.STATUS_WAITING_TO_RETRY;
        }
        Log.w(Constants.TAG, "reached max retries for " + this.mInfo.mNumFailed);
        return 495;
    }

    private void setupDestinationFile(State state, InnerState innerState) throws StopRequest {
        if (state.mFilename != null) {
            if (!Helpers.isFilenameValid(state.mFilename)) {
                throw new StopRequest(492, "found invalid internal destination filename");
            }
            File f = new File(state.mFilename);
            if (f.exists()) {
                long fileLength = f.length();
                if (fileLength == 0) {
                    f.delete();
                    state.mFilename = null;
                } else {
                    if (this.mInfo.mETag == null) {
                        f.delete();
                        throw new StopRequest(489, "Trying to resume a download that can't be resumed");
                    }
                    try {
                        state.mStream = new FileOutputStream(state.mFilename, true);
                        innerState.mBytesSoFar = (int) fileLength;
                        if (this.mInfo.mTotalBytes != -1) {
                            innerState.mHeaderContentLength = Long.toString(this.mInfo.mTotalBytes);
                        }
                        innerState.mHeaderETag = this.mInfo.mETag;
                        innerState.mContinuingDownload = true;
                    } catch (FileNotFoundException exc) {
                        throw new StopRequest(492, "while opening destination for resuming: " + exc.toString(), exc);
                    }
                }
            }
        }
        if (state.mStream != null) {
            closeDestination(state);
        }
    }

    private void notifyDownloadCompleted(int status, boolean countRetry, int retryAfter, int redirectCount, boolean gotData, String filename) {
        updateDownloadDatabase(status, countRetry, retryAfter, redirectCount, gotData, filename);
        if (DownloaderService.isStatusCompleted(status)) {
        }
    }

    private void updateDownloadDatabase(int status, boolean countRetry, int retryAfter, int redirectCount, boolean gotData, String filename) {
        this.mInfo.mStatus = status;
        this.mInfo.mRetryAfter = retryAfter;
        this.mInfo.mRedirectCount = redirectCount;
        this.mInfo.mLastMod = System.currentTimeMillis();
        if (!countRetry) {
            this.mInfo.mNumFailed = 0;
        } else if (gotData) {
            this.mInfo.mNumFailed = 1;
        } else {
            this.mInfo.mNumFailed++;
        }
        this.mDB.updateDownload(this.mInfo);
    }
}
