package com.facebook;

import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Pair;
import com.facebook.RequestBatch;
import com.facebook.internal.Logger;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.google.android.gms.plus.PlusShare;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class Request {
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String ATTACHED_FILES_PARAM = "attached_files";
    private static final String ATTACHMENT_FILENAME_PREFIX = "file";
    private static final String BATCH_APP_ID_PARAM = "batch_app_id";
    private static final String BATCH_BODY_PARAM = "body";
    private static final String BATCH_ENTRY_DEPENDS_ON_PARAM = "depends_on";
    private static final String BATCH_ENTRY_NAME_PARAM = "name";
    private static final String BATCH_ENTRY_OMIT_RESPONSE_ON_SUCCESS_PARAM = "omit_response_on_success";
    private static final String BATCH_METHOD_PARAM = "method";
    private static final String BATCH_PARAM = "batch";
    private static final String BATCH_RELATIVE_URL_PARAM = "relative_url";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_PARAM = "format";
    private static final String ISO_8601_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final int MAXIMUM_BATCH_SIZE = 50;
    private static final String ME = "me";
    private static final String MIGRATION_BUNDLE_PARAM = "migration_bundle";
    private static final String MIME_BOUNDARY = "3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f";
    private static final String MY_FEED = "me/feed";
    private static final String MY_FRIENDS = "me/friends";
    private static final String MY_PHOTOS = "me/photos";
    private static final String MY_VIDEOS = "me/videos";
    private static final String PICTURE_PARAM = "picture";
    private static final String SDK_ANDROID = "android";
    private static final String SDK_PARAM = "sdk";
    private static final String SEARCH = "search";
    private static final String USER_AGENT_BASE = "FBAndroidSDK";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static String defaultBatchApplicationId;
    private static volatile String userAgent;
    private String batchEntryDependsOn;
    private String batchEntryName;
    private boolean batchEntryOmitResultOnSuccess;
    private Callback callback;
    private GraphObject graphObject;
    private String graphPath;
    private HttpMethod httpMethod;
    private String overriddenURL;
    private Bundle parameters;
    private String restMethod;
    private Session session;

    public interface Callback {
        void onCompleted(Response response);
    }

    public interface GraphPlaceListCallback {
        void onCompleted(List<GraphPlace> list, Response response);
    }

    public interface GraphUserCallback {
        void onCompleted(GraphUser graphUser, Response response);
    }

    public interface GraphUserListCallback {
        void onCompleted(List<GraphUser> list, Response response);
    }

    private interface KeyValueSerializer {
        void writeString(String str, String str2) throws IOException;
    }

    public Request() {
        this(null, null, null, null, null);
    }

    public Request(Session session, String graphPath) {
        this(session, graphPath, null, null, null);
    }

    public Request(Session session, String graphPath, Bundle parameters, HttpMethod httpMethod) {
        this(session, graphPath, parameters, httpMethod, null);
    }

    public Request(Session session, String graphPath, Bundle parameters, HttpMethod httpMethod, Callback callback) {
        this.batchEntryOmitResultOnSuccess = true;
        this.session = session;
        this.graphPath = graphPath;
        this.callback = callback;
        setHttpMethod(httpMethod);
        if (parameters != null) {
            this.parameters = new Bundle(parameters);
        } else {
            this.parameters = new Bundle();
        }
        if (!this.parameters.containsKey(MIGRATION_BUNDLE_PARAM)) {
            this.parameters.putString(MIGRATION_BUNDLE_PARAM, FacebookSdkVersion.MIGRATION_BUNDLE);
        }
    }

    Request(Session session, URL overriddenURL) {
        this.batchEntryOmitResultOnSuccess = true;
        this.session = session;
        this.overriddenURL = overriddenURL.toString();
        setHttpMethod(HttpMethod.GET);
        this.parameters = new Bundle();
    }

    public static Request newPostRequest(Session session, String graphPath, GraphObject graphObject, Callback callback) {
        Request request = new Request(session, graphPath, null, HttpMethod.POST, callback);
        request.setGraphObject(graphObject);
        return request;
    }

    public static Request newRestRequest(Session session, String restMethod, Bundle parameters, HttpMethod httpMethod) {
        Request request = new Request(session, null, parameters, httpMethod);
        request.setRestMethod(restMethod);
        return request;
    }

    public static Request newMeRequest(Session session, final GraphUserCallback callback) {
        Callback wrapper = new Callback() { // from class: com.facebook.Request.1
            @Override // com.facebook.Request.Callback
            public void onCompleted(Response response) {
                if (callback != null) {
                    callback.onCompleted((GraphUser) response.getGraphObjectAs(GraphUser.class), response);
                }
            }
        };
        return new Request(session, ME, null, null, wrapper);
    }

    public static Request newMyFriendsRequest(Session session, final GraphUserListCallback callback) {
        Callback wrapper = new Callback() { // from class: com.facebook.Request.2
            @Override // com.facebook.Request.Callback
            public void onCompleted(Response response) {
                if (callback != null) {
                    callback.onCompleted(Request.typedListFromResponse(response, GraphUser.class), response);
                }
            }
        };
        return new Request(session, MY_FRIENDS, null, null, wrapper);
    }

    public static Request newUploadPhotoRequest(Session session, Bitmap image, Callback callback) {
        Bundle parameters = new Bundle(1);
        parameters.putParcelable(PICTURE_PARAM, image);
        return new Request(session, MY_PHOTOS, parameters, HttpMethod.POST, callback);
    }

    public static Request newUploadPhotoRequest(Session session, File file, Callback callback) throws FileNotFoundException {
        ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, 268435456);
        Bundle parameters = new Bundle(1);
        parameters.putParcelable(PICTURE_PARAM, descriptor);
        return new Request(session, MY_PHOTOS, parameters, HttpMethod.POST, callback);
    }

    public static Request newUploadVideoRequest(Session session, File file, Callback callback) throws FileNotFoundException {
        ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, 268435456);
        Bundle parameters = new Bundle(1);
        parameters.putParcelable(file.getName(), descriptor);
        return new Request(session, MY_VIDEOS, parameters, HttpMethod.POST, callback);
    }

    public static Request newGraphPathRequest(Session session, String graphPath, Callback callback) {
        return new Request(session, graphPath, null, null, callback);
    }

    public static Request newPlacesSearchRequest(Session session, Location location, int radiusInMeters, int resultsLimit, String searchText, final GraphPlaceListCallback callback) {
        if (location == null && Utility.isNullOrEmpty(searchText)) {
            throw new FacebookException("Either location or searchText must be specified.");
        }
        Bundle parameters = new Bundle(5);
        parameters.putString(ServerProtocol.DIALOG_PARAM_TYPE, "place");
        parameters.putInt("limit", resultsLimit);
        if (location != null) {
            parameters.putString("center", String.format(Locale.US, "%f,%f", Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude())));
            parameters.putInt("distance", radiusInMeters);
        }
        if (!Utility.isNullOrEmpty(searchText)) {
            parameters.putString("q", searchText);
        }
        Callback wrapper = new Callback() { // from class: com.facebook.Request.3
            @Override // com.facebook.Request.Callback
            public void onCompleted(Response response) {
                if (callback != null) {
                    callback.onCompleted(Request.typedListFromResponse(response, GraphPlace.class), response);
                }
            }
        };
        return new Request(session, SEARCH, parameters, HttpMethod.GET, wrapper);
    }

    public static Request newStatusUpdateRequest(Session session, String message, Callback callback) {
        Bundle parameters = new Bundle();
        parameters.putString("message", message);
        return new Request(session, MY_FEED, parameters, HttpMethod.POST, callback);
    }

    public final GraphObject getGraphObject() {
        return this.graphObject;
    }

    public final void setGraphObject(GraphObject graphObject) {
        this.graphObject = graphObject;
    }

    public final String getGraphPath() {
        return this.graphPath;
    }

    public final void setGraphPath(String graphPath) {
        this.graphPath = graphPath;
    }

    public final HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    public final void setHttpMethod(HttpMethod httpMethod) {
        if (this.overriddenURL != null && httpMethod != HttpMethod.GET) {
            throw new FacebookException("Can't change HTTP method on request with overridden URL.");
        }
        if (httpMethod == null) {
            httpMethod = HttpMethod.GET;
        }
        this.httpMethod = httpMethod;
    }

    public final Bundle getParameters() {
        return this.parameters;
    }

    public final void setParameters(Bundle parameters) {
        this.parameters = parameters;
    }

    public final String getRestMethod() {
        return this.restMethod;
    }

    public final void setRestMethod(String restMethod) {
        this.restMethod = restMethod;
    }

    public final Session getSession() {
        return this.session;
    }

    public final void setSession(Session session) {
        this.session = session;
    }

    public final String getBatchEntryName() {
        return this.batchEntryName;
    }

    public final void setBatchEntryName(String batchEntryName) {
        this.batchEntryName = batchEntryName;
    }

    public final String getBatchEntryDependsOn() {
        return this.batchEntryDependsOn;
    }

    public final void setBatchEntryDependsOn(String batchEntryDependsOn) {
        this.batchEntryDependsOn = batchEntryDependsOn;
    }

    public final boolean getBatchEntryOmitResultOnSuccess() {
        return this.batchEntryOmitResultOnSuccess;
    }

    public final void setBatchEntryOmitResultOnSuccess(boolean batchEntryOmitResultOnSuccess) {
        this.batchEntryOmitResultOnSuccess = batchEntryOmitResultOnSuccess;
    }

    public static final String getDefaultBatchApplicationId() {
        return defaultBatchApplicationId;
    }

    public static final void setDefaultBatchApplicationId(String applicationId) {
        defaultBatchApplicationId = applicationId;
    }

    public final Callback getCallback() {
        return this.callback;
    }

    public final void setCallback(Callback callback) {
        this.callback = callback;
    }

    public static RequestAsyncTask executePostRequestAsync(Session session, String graphPath, GraphObject graphObject, Callback callback) {
        return newPostRequest(session, graphPath, graphObject, callback).executeAsync();
    }

    public static RequestAsyncTask executeRestRequestAsync(Session session, String restMethod, Bundle parameters, HttpMethod httpMethod) {
        return newRestRequest(session, restMethod, parameters, httpMethod).executeAsync();
    }

    public static RequestAsyncTask executeMeRequestAsync(Session session, GraphUserCallback callback) {
        return newMeRequest(session, callback).executeAsync();
    }

    public static RequestAsyncTask executeMyFriendsRequestAsync(Session session, GraphUserListCallback callback) {
        return newMyFriendsRequest(session, callback).executeAsync();
    }

    public static RequestAsyncTask executeUploadPhotoRequestAsync(Session session, Bitmap image, Callback callback) {
        return newUploadPhotoRequest(session, image, callback).executeAsync();
    }

    public static RequestAsyncTask executeUploadPhotoRequestAsync(Session session, File file, Callback callback) throws FileNotFoundException {
        return newUploadPhotoRequest(session, file, callback).executeAsync();
    }

    public static RequestAsyncTask executeGraphPathRequestAsync(Session session, String graphPath, Callback callback) {
        return newGraphPathRequest(session, graphPath, callback).executeAsync();
    }

    public static RequestAsyncTask executePlacesSearchRequestAsync(Session session, Location location, int radiusInMeters, int resultsLimit, String searchText, GraphPlaceListCallback callback) {
        return newPlacesSearchRequest(session, location, radiusInMeters, resultsLimit, searchText, callback).executeAsync();
    }

    public static RequestAsyncTask executeStatusUpdateRequestAsync(Session session, String message, Callback callback) {
        return newStatusUpdateRequest(session, message, callback).executeAsync();
    }

    public final Response executeAndWait() {
        return executeAndWait(this);
    }

    public final RequestAsyncTask executeAsync() {
        return executeBatchAsync(this);
    }

    public static HttpURLConnection toHttpConnection(Request... requests) {
        return toHttpConnection(Arrays.asList(requests));
    }

    public static HttpURLConnection toHttpConnection(Collection<Request> requests) {
        Validate.notEmptyAndContainsNoNulls(requests, "requests");
        return toHttpConnection(new RequestBatch(requests));
    }

    public static HttpURLConnection toHttpConnection(RequestBatch requests) {
        URL url;
        for (Request request : requests) {
            request.validate();
        }
        try {
            if (requests.size() == 1) {
                Request request2 = requests.get(0);
                url = new URL(request2.getUrlForSingleRequest());
            } else {
                url = new URL(ServerProtocol.GRAPH_URL);
            }
            try {
                HttpURLConnection connection = createConnection(url);
                serializeToUrlConnection(requests, connection);
                return connection;
            } catch (IOException e) {
                throw new FacebookException("could not construct request body", e);
            } catch (JSONException e2) {
                throw new FacebookException("could not construct request body", e2);
            }
        } catch (MalformedURLException e3) {
            throw new FacebookException("could not construct URL for request", e3);
        }
    }

    public static Response executeAndWait(Request request) {
        List<Response> responses = executeBatchAndWait(request);
        if (responses == null || responses.size() != 1) {
            throw new FacebookException("invalid state: expected a single response");
        }
        return responses.get(0);
    }

    public static List<Response> executeBatchAndWait(Request... requests) {
        Validate.notNull(requests, "requests");
        return executeBatchAndWait(Arrays.asList(requests));
    }

    public static List<Response> executeBatchAndWait(Collection<Request> requests) {
        return executeBatchAndWait(new RequestBatch(requests));
    }

    public static List<Response> executeBatchAndWait(RequestBatch requests) {
        Validate.notEmptyAndContainsNoNulls(requests, "requests");
        try {
            HttpURLConnection connection = toHttpConnection(requests);
            return executeConnectionAndWait(connection, requests);
        } catch (Exception ex) {
            List<Response> responses = Response.constructErrorResponses(requests.getRequests(), null, new FacebookException(ex));
            runCallbacks(requests, responses);
            return responses;
        }
    }

    public static RequestAsyncTask executeBatchAsync(Request... requests) {
        Validate.notNull(requests, "requests");
        return executeBatchAsync(Arrays.asList(requests));
    }

    public static RequestAsyncTask executeBatchAsync(Collection<Request> requests) {
        return executeBatchAsync(new RequestBatch(requests));
    }

    public static RequestAsyncTask executeBatchAsync(RequestBatch requests) {
        Validate.notEmptyAndContainsNoNulls(requests, "requests");
        RequestAsyncTask asyncTask = new RequestAsyncTask(requests);
        asyncTask.executeOnSettingsExecutor();
        return asyncTask;
    }

    public static List<Response> executeConnectionAndWait(HttpURLConnection connection, Collection<Request> requests) {
        return executeConnectionAndWait(connection, new RequestBatch(requests));
    }

    public static List<Response> executeConnectionAndWait(HttpURLConnection connection, RequestBatch requests) throws Throwable {
        List<Response> responses = Response.fromHttpConnection(connection, requests);
        Utility.disconnectQuietly(connection);
        int numRequests = requests.size();
        if (numRequests != responses.size()) {
            throw new FacebookException(String.format("Received %d responses while expecting %d", Integer.valueOf(responses.size()), Integer.valueOf(numRequests)));
        }
        runCallbacks(requests, responses);
        HashSet<Session> sessions = new HashSet<>();
        for (Request request : requests) {
            if (request.session != null) {
                sessions.add(request.session);
            }
        }
        for (Session session : sessions) {
            session.extendAccessTokenIfNeeded();
        }
        return responses;
    }

    public static RequestAsyncTask executeConnectionAsync(HttpURLConnection connection, RequestBatch requests) {
        return executeConnectionAsync(null, connection, requests);
    }

    public static RequestAsyncTask executeConnectionAsync(Handler callbackHandler, HttpURLConnection connection, RequestBatch requests) {
        Validate.notNull(connection, "connection");
        RequestAsyncTask asyncTask = new RequestAsyncTask(connection, requests);
        requests.setCallbackHandler(callbackHandler);
        asyncTask.executeOnSettingsExecutor();
        return asyncTask;
    }

    public String toString() {
        return "{Request:  session: " + this.session + ", graphPath: " + this.graphPath + ", graphObject: " + this.graphObject + ", restMethod: " + this.restMethod + ", httpMethod: " + this.httpMethod + ", parameters: " + this.parameters + "}";
    }

    static void runCallbacks(final RequestBatch requests, List<Response> responses) {
        int numRequests = requests.size();
        final ArrayList<Pair<Callback, Response>> callbacks = new ArrayList<>();
        for (int i = 0; i < numRequests; i++) {
            Request request = requests.get(i);
            if (request.callback != null) {
                callbacks.add(new Pair<>(request.callback, responses.get(i)));
            }
        }
        if (callbacks.size() > 0) {
            Runnable runnable = new Runnable() { // from class: com.facebook.Request.4
                @Override // java.lang.Runnable
                public void run() {
                    for (Pair<Callback, Response> pair : callbacks) {
                        ((Callback) pair.first).onCompleted((Response) pair.second);
                    }
                    List<RequestBatch.Callback> batchCallbacks = requests.getCallbacks();
                    for (RequestBatch.Callback batchCallback : batchCallbacks) {
                        batchCallback.onBatchCompleted(requests);
                    }
                }
            };
            Handler callbackHandler = requests.getCallbackHandler();
            if (callbackHandler == null) {
                runnable.run();
            } else {
                callbackHandler.post(runnable);
            }
        }
    }

    static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty(USER_AGENT_HEADER, getUserAgent());
        connection.setRequestProperty(CONTENT_TYPE_HEADER, getMimeContentType());
        connection.setChunkedStreamingMode(0);
        return connection;
    }

    private void addCommonParameters() {
        if (this.session != null) {
            if (!this.session.isOpened()) {
                throw new FacebookException("Session provided to a Request in un-opened state.");
            }
            if (!this.parameters.containsKey("access_token")) {
                String accessToken = this.session.getAccessToken();
                Logger.registerAccessToken(accessToken);
                this.parameters.putString("access_token", accessToken);
            }
        }
        this.parameters.putString(SDK_PARAM, SDK_ANDROID);
        this.parameters.putString(FORMAT_PARAM, FORMAT_JSON);
    }

    private String appendParametersToBaseUrl(String baseUrl) {
        Uri.Builder uriBuilder = new Uri.Builder().encodedPath(baseUrl);
        Set<String> keys = this.parameters.keySet();
        for (String key : keys) {
            Object value = this.parameters.get(key);
            if (value == null) {
                value = "";
            }
            if (!isSupportedParameterType(value)) {
                if (this.httpMethod == HttpMethod.GET) {
                    throw new IllegalArgumentException(String.format("Unsupported parameter type for GET request: %s", value.getClass().getSimpleName()));
                }
            } else {
                uriBuilder.appendQueryParameter(key, parameterToString(value).toString());
            }
        }
        return uriBuilder.toString();
    }

    final String getUrlForBatchedRequest() {
        String baseUrl;
        if (this.overriddenURL != null) {
            throw new FacebookException("Can't override URL for a batch request");
        }
        if (this.restMethod != null) {
            baseUrl = ServerProtocol.BATCHED_REST_METHOD_URL_BASE + this.restMethod;
        } else {
            baseUrl = this.graphPath;
        }
        addCommonParameters();
        return appendParametersToBaseUrl(baseUrl);
    }

    final String getUrlForSingleRequest() {
        String baseUrl;
        if (this.overriddenURL != null) {
            return this.overriddenURL.toString();
        }
        if (this.restMethod != null) {
            baseUrl = ServerProtocol.REST_URL_BASE + this.restMethod;
        } else {
            baseUrl = ServerProtocol.GRAPH_URL_BASE + this.graphPath;
        }
        addCommonParameters();
        return appendParametersToBaseUrl(baseUrl);
    }

    private void serializeToBatch(JSONArray batch, Bundle attachments) throws JSONException, IOException {
        JSONObject batchEntry = new JSONObject();
        if (this.batchEntryName != null) {
            batchEntry.put(BATCH_ENTRY_NAME_PARAM, this.batchEntryName);
            batchEntry.put(BATCH_ENTRY_OMIT_RESPONSE_ON_SUCCESS_PARAM, this.batchEntryOmitResultOnSuccess);
        }
        if (this.batchEntryDependsOn != null) {
            batchEntry.put(BATCH_ENTRY_DEPENDS_ON_PARAM, this.batchEntryDependsOn);
        }
        String relativeURL = getUrlForBatchedRequest();
        batchEntry.put(BATCH_RELATIVE_URL_PARAM, relativeURL);
        batchEntry.put(BATCH_METHOD_PARAM, this.httpMethod);
        if (this.session != null) {
            String accessToken = this.session.getAccessToken();
            Logger.registerAccessToken(accessToken);
        }
        ArrayList<String> attachmentNames = new ArrayList<>();
        Set<String> keys = this.parameters.keySet();
        for (String key : keys) {
            Object value = this.parameters.get(key);
            if (isSupportedAttachmentType(value)) {
                String name = String.format("%s%d", ATTACHMENT_FILENAME_PREFIX, Integer.valueOf(attachments.size()));
                attachmentNames.add(name);
                Utility.putObjectInBundle(attachments, name, value);
            }
        }
        if (!attachmentNames.isEmpty()) {
            String attachmentNamesString = TextUtils.join(",", attachmentNames);
            batchEntry.put(ATTACHED_FILES_PARAM, attachmentNamesString);
        }
        if (this.graphObject != null) {
            final ArrayList<String> keysAndValues = new ArrayList<>();
            processGraphObject(this.graphObject, relativeURL, new KeyValueSerializer() { // from class: com.facebook.Request.5
                @Override // com.facebook.Request.KeyValueSerializer
                public void writeString(String key2, String value2) throws IOException {
                    keysAndValues.add(String.format("%s=%s", key2, URLEncoder.encode(value2, "UTF-8")));
                }
            });
            String bodyValue = TextUtils.join("&", keysAndValues);
            batchEntry.put(BATCH_BODY_PARAM, bodyValue);
        }
        batch.put(batchEntry);
    }

    private void validate() {
        if (this.graphPath != null && this.restMethod != null) {
            throw new IllegalArgumentException("Only one of a graph path or REST method may be specified per request.");
        }
    }

    static final void serializeToUrlConnection(RequestBatch requests, HttpURLConnection connection) throws JSONException, IOException {
        Logger logger = new Logger(LoggingBehavior.REQUESTS, "Request");
        int numRequests = requests.size();
        HttpMethod connectionHttpMethod = numRequests == 1 ? requests.get(0).httpMethod : HttpMethod.POST;
        connection.setRequestMethod(connectionHttpMethod.name());
        URL url = connection.getURL();
        logger.append("Request:\n");
        logger.appendKeyValue("Id", requests.getId());
        logger.appendKeyValue("URL", url);
        logger.appendKeyValue("Method", connection.getRequestMethod());
        logger.appendKeyValue(USER_AGENT_HEADER, connection.getRequestProperty(USER_AGENT_HEADER));
        logger.appendKeyValue(CONTENT_TYPE_HEADER, connection.getRequestProperty(CONTENT_TYPE_HEADER));
        connection.setConnectTimeout(requests.getTimeout());
        connection.setReadTimeout(requests.getTimeout());
        boolean isPost = connectionHttpMethod == HttpMethod.POST;
        if (!isPost) {
            logger.log();
            return;
        }
        connection.setDoOutput(true);
        BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        try {
            Serializer serializer = new Serializer(outputStream, logger);
            if (numRequests == 1) {
                Request request = requests.get(0);
                logger.append("  Parameters:\n");
                serializeParameters(request.parameters, serializer);
                logger.append("  Attachments:\n");
                serializeAttachments(request.parameters, serializer);
                if (request.graphObject != null) {
                    processGraphObject(request.graphObject, url.getPath(), serializer);
                }
            } else {
                String batchAppID = getBatchAppId(requests);
                if (Utility.isNullOrEmpty(batchAppID)) {
                    throw new FacebookException("At least one request in a batch must have an open Session, or a default app ID must be specified.");
                }
                serializer.writeString(BATCH_APP_ID_PARAM, batchAppID);
                Bundle attachments = new Bundle();
                serializeRequestsAsJSON(serializer, requests, attachments);
                logger.append("  Attachments:\n");
                serializeAttachments(attachments, serializer);
            }
            outputStream.close();
            logger.log();
        } catch (Throwable th) {
            outputStream.close();
            throw th;
        }
    }

    private static void processGraphObject(GraphObject graphObject, String path, KeyValueSerializer serializer) throws IOException {
        boolean isOGAction = false;
        if (path.startsWith("me/") || path.startsWith("/me/")) {
            int colonLocation = path.indexOf(":");
            int questionMarkLocation = path.indexOf("?");
            isOGAction = colonLocation > 3 && (questionMarkLocation == -1 || colonLocation < questionMarkLocation);
        }
        Set<Map.Entry<String, Object>> entries = graphObject.asMap().entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            boolean passByValue = isOGAction && entry.getKey().equalsIgnoreCase("image");
            processGraphObjectProperty(entry.getKey(), entry.getValue(), serializer, passByValue);
        }
    }

    private static void processGraphObjectProperty(String key, Object value, KeyValueSerializer serializer, boolean passByValue) throws IOException {
        Class<?> valueClass = value.getClass();
        if (GraphObject.class.isAssignableFrom(valueClass)) {
            value = ((GraphObject) value).getInnerJSONObject();
            valueClass = value.getClass();
        } else if (GraphObjectList.class.isAssignableFrom(valueClass)) {
            value = ((GraphObjectList) value).getInnerJSONArray();
            valueClass = value.getClass();
        }
        if (JSONObject.class.isAssignableFrom(valueClass)) {
            JSONObject jsonObject = (JSONObject) value;
            if (passByValue) {
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String propertyName = keys.next();
                    String subKey = String.format("%s[%s]", key, propertyName);
                    processGraphObjectProperty(subKey, jsonObject.opt(propertyName), serializer, passByValue);
                }
                return;
            }
            if (jsonObject.has("id")) {
                processGraphObjectProperty(key, jsonObject.optString("id"), serializer, passByValue);
                return;
            } else {
                if (jsonObject.has(PlusShare.KEY_CALL_TO_ACTION_URL)) {
                    processGraphObjectProperty(key, jsonObject.optString(PlusShare.KEY_CALL_TO_ACTION_URL), serializer, passByValue);
                    return;
                }
                return;
            }
        }
        if (JSONArray.class.isAssignableFrom(valueClass)) {
            JSONArray jsonArray = (JSONArray) value;
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                String subKey2 = String.format("%s[%d]", key, Integer.valueOf(i));
                processGraphObjectProperty(subKey2, jsonArray.opt(i), serializer, passByValue);
            }
            return;
        }
        if (String.class.isAssignableFrom(valueClass) || Number.class.isAssignableFrom(valueClass) || Boolean.class.isAssignableFrom(valueClass)) {
            serializer.writeString(key, value.toString());
        } else if (Date.class.isAssignableFrom(valueClass)) {
            Date date = (Date) value;
            SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(ISO_8601_FORMAT_STRING, Locale.US);
            serializer.writeString(key, iso8601DateFormat.format(date));
        }
    }

    private static void serializeParameters(Bundle bundle, Serializer serializer) throws Throwable {
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            Object value = bundle.get(key);
            if (isSupportedParameterType(value)) {
                serializer.writeObject(key, value);
            }
        }
    }

    private static void serializeAttachments(Bundle bundle, Serializer serializer) throws Throwable {
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            Object value = bundle.get(key);
            if (isSupportedAttachmentType(value)) {
                serializer.writeObject(key, value);
            }
        }
    }

    private static void serializeRequestsAsJSON(Serializer serializer, Collection<Request> requests, Bundle attachments) throws JSONException, IOException {
        JSONArray batch = new JSONArray();
        for (Request request : requests) {
            request.serializeToBatch(batch, attachments);
        }
        String batchAsString = batch.toString();
        serializer.writeString(BATCH_PARAM, batchAsString);
    }

    private static String getMimeContentType() {
        return String.format("multipart/form-data; boundary=%s", MIME_BOUNDARY);
    }

    private static String getUserAgent() {
        if (userAgent == null) {
            userAgent = String.format("%s.%s", USER_AGENT_BASE, FacebookSdkVersion.BUILD);
        }
        return userAgent;
    }

    private static String getBatchAppId(RequestBatch batch) {
        if (!Utility.isNullOrEmpty(batch.getBatchApplicationId())) {
            return batch.getBatchApplicationId();
        }
        for (Request request : batch) {
            Session session = request.session;
            if (session != null) {
                return session.getApplicationId();
            }
        }
        return defaultBatchApplicationId;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <T extends GraphObject> List<T> typedListFromResponse(Response response, Class<T> cls) {
        GraphObjectList<GraphObject> data;
        GraphMultiResult graphMultiResult = (GraphMultiResult) response.getGraphObjectAs(GraphMultiResult.class);
        if (graphMultiResult == null || (data = graphMultiResult.getData()) == null) {
            return null;
        }
        return data.castToListOf(cls);
    }

    private static boolean isSupportedAttachmentType(Object value) {
        return (value instanceof Bitmap) || (value instanceof byte[]) || (value instanceof ParcelFileDescriptor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isSupportedParameterType(Object value) {
        return (value instanceof String) || (value instanceof Boolean) || (value instanceof Number) || (value instanceof Date);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String parameterToString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if ((value instanceof Boolean) || (value instanceof Number)) {
            return value.toString();
        }
        if (value instanceof Date) {
            SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(ISO_8601_FORMAT_STRING, Locale.US);
            return iso8601DateFormat.format(value);
        }
        throw new IllegalArgumentException("Unsupported parameter type.");
    }

    private static class Serializer implements KeyValueSerializer {
        private boolean firstWrite = true;
        private final Logger logger;
        private final BufferedOutputStream outputStream;

        public Serializer(BufferedOutputStream outputStream, Logger logger) {
            this.outputStream = outputStream;
            this.logger = logger;
        }

        public void writeObject(String key, Object value) throws Throwable {
            if (Request.isSupportedParameterType(value)) {
                writeString(key, Request.parameterToString(value));
                return;
            }
            if (value instanceof Bitmap) {
                writeBitmap(key, (Bitmap) value);
            } else if (value instanceof byte[]) {
                writeBytes(key, (byte[]) value);
            } else {
                if (value instanceof ParcelFileDescriptor) {
                    writeFile(key, (ParcelFileDescriptor) value);
                    return;
                }
                throw new IllegalArgumentException("value is not a supported type: String, Bitmap, byte[]");
            }
        }

        @Override // com.facebook.Request.KeyValueSerializer
        public void writeString(String key, String value) throws IOException {
            writeContentDisposition(key, null, null);
            writeLine("%s", value);
            writeRecordBoundary();
            if (this.logger != null) {
                this.logger.appendKeyValue("    " + key, value);
            }
        }

        public void writeBitmap(String key, Bitmap bitmap) throws IOException {
            writeContentDisposition(key, key, "image/png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this.outputStream);
            writeLine("", new Object[0]);
            writeRecordBoundary();
            this.logger.appendKeyValue("    " + key, "<Image>");
        }

        public void writeBytes(String key, byte[] bytes) throws IOException {
            writeContentDisposition(key, key, "content/unknown");
            this.outputStream.write(bytes);
            writeLine("", new Object[0]);
            writeRecordBoundary();
            this.logger.appendKeyValue("    " + key, String.format("<Data: %d>", Integer.valueOf(bytes.length)));
        }

        public void writeFile(String key, ParcelFileDescriptor descriptor) throws Throwable {
            writeContentDisposition(key, key, "content/unknown");
            ParcelFileDescriptor.AutoCloseInputStream inputStream = null;
            BufferedInputStream bufferedInputStream = null;
            int totalBytes = 0;
            try {
                ParcelFileDescriptor.AutoCloseInputStream inputStream2 = new ParcelFileDescriptor.AutoCloseInputStream(descriptor);
                try {
                    BufferedInputStream bufferedInputStream2 = new BufferedInputStream(inputStream2);
                    try {
                        byte[] buffer = new byte[8192];
                        while (true) {
                            int bytesRead = bufferedInputStream2.read(buffer);
                            if (bytesRead == -1) {
                                break;
                            }
                            this.outputStream.write(buffer, 0, bytesRead);
                            totalBytes += bytesRead;
                        }
                        if (bufferedInputStream2 != null) {
                            bufferedInputStream2.close();
                        }
                        if (inputStream2 != null) {
                            inputStream2.close();
                        }
                        writeLine("", new Object[0]);
                        writeRecordBoundary();
                        this.logger.appendKeyValue("    " + key, String.format("<Data: %d>", Integer.valueOf(totalBytes)));
                    } catch (Throwable th) {
                        th = th;
                        bufferedInputStream = bufferedInputStream2;
                        inputStream = inputStream2;
                        if (bufferedInputStream != null) {
                            bufferedInputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = inputStream2;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }

        public void writeRecordBoundary() throws IOException {
            writeLine("--%s", Request.MIME_BOUNDARY);
        }

        public void writeContentDisposition(String name, String filename, String contentType) throws IOException {
            write("Content-Disposition: form-data; name=\"%s\"", name);
            if (filename != null) {
                write("; filename=\"%s\"", filename);
            }
            writeLine("", new Object[0]);
            if (contentType != null) {
                writeLine("%s: %s", Request.CONTENT_TYPE_HEADER, contentType);
            }
            writeLine("", new Object[0]);
        }

        public void write(String format, Object... args) throws IOException {
            if (this.firstWrite) {
                this.outputStream.write("--".getBytes());
                this.outputStream.write(Request.MIME_BOUNDARY.getBytes());
                this.outputStream.write("\r\n".getBytes());
                this.firstWrite = false;
            }
            this.outputStream.write(String.format(format, args).getBytes());
        }

        public void writeLine(String format, Object... args) throws IOException {
            write(format, args);
            write("\r\n", new Object[0]);
        }
    }
}
