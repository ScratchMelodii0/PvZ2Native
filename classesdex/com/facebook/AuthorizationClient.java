package com.facebook;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieSyncManager;
import com.facebook.GetTokenClient;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.android.R;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class AuthorizationClient implements Serializable {
    private static final String TAG = "Facebook-AuthorizationClient";
    private static final String WEB_VIEW_AUTH_HANDLER_STORE = "com.facebook.AuthorizationClient.WebViewAuthHandler.TOKEN_STORE_KEY";
    private static final String WEB_VIEW_AUTH_HANDLER_TOKEN_KEY = "TOKEN";
    private static final long serialVersionUID = 1;
    transient BackgroundProcessingListener backgroundProcessingListener;
    transient boolean checkedInternetPermission;
    transient Context context;
    AuthHandler currentHandler;
    List<AuthHandler> handlersToTry;
    transient OnCompletedListener onCompletedListener;
    AuthorizationRequest pendingRequest;
    transient StartActivityDelegate startActivityDelegate;

    interface BackgroundProcessingListener {
        void onBackgroundProcessingStarted();

        void onBackgroundProcessingStopped();
    }

    interface OnCompletedListener {
        void onCompleted(Result result);
    }

    interface StartActivityDelegate {
        Activity getActivityContext();

        void startActivityForResult(Intent intent, int i);
    }

    AuthorizationClient() {
    }

    void setContext(Context context) {
        this.context = context;
        this.startActivityDelegate = null;
    }

    void setContext(final Activity activity) {
        this.context = activity;
        this.startActivityDelegate = new StartActivityDelegate() { // from class: com.facebook.AuthorizationClient.1
            @Override // com.facebook.AuthorizationClient.StartActivityDelegate
            public void startActivityForResult(Intent intent, int requestCode) {
                activity.startActivityForResult(intent, requestCode);
            }

            @Override // com.facebook.AuthorizationClient.StartActivityDelegate
            public Activity getActivityContext() {
                return activity;
            }
        };
    }

    void startOrContinueAuth(AuthorizationRequest request) {
        if (getInProgress()) {
            continueAuth();
        } else {
            authorize(request);
        }
    }

    void authorize(AuthorizationRequest request) {
        if (request != null) {
            if (this.pendingRequest != null) {
                throw new FacebookException("Attempted to authorize while a request is pending.");
            }
            if (!request.needsNewTokenValidation() || checkInternetPermission()) {
                this.pendingRequest = request;
                this.handlersToTry = getHandlerTypes(request);
                tryNextHandler();
            }
        }
    }

    void continueAuth() {
        if (this.pendingRequest == null || this.currentHandler == null) {
            throw new FacebookException("Attempted to continue authorization without a pending request.");
        }
        if (this.currentHandler.needsRestart()) {
            this.currentHandler.cancel();
            tryCurrentHandler();
        }
    }

    boolean getInProgress() {
        return (this.pendingRequest == null || this.currentHandler == null) ? false : true;
    }

    void cancelCurrentHandler() {
        if (this.currentHandler != null) {
            this.currentHandler.cancel();
        }
    }

    boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.pendingRequest.getRequestCode()) {
            return this.currentHandler.onActivityResult(requestCode, resultCode, data);
        }
        return false;
    }

    private List<AuthHandler> getHandlerTypes(AuthorizationRequest request) {
        ArrayList<AuthHandler> handlers = new ArrayList<>();
        SessionLoginBehavior behavior = request.getLoginBehavior();
        if (behavior.allowsKatanaAuth()) {
            if (!request.isLegacy()) {
                handlers.add(new GetTokenAuthHandler());
                handlers.add(new KatanaLoginDialogAuthHandler());
            }
            handlers.add(new KatanaProxyAuthHandler());
        }
        if (behavior.allowsWebViewAuth()) {
            handlers.add(new WebViewAuthHandler());
        }
        return handlers;
    }

    boolean checkInternetPermission() {
        if (this.checkedInternetPermission) {
            return true;
        }
        int permissionCheck = checkPermission("android.permission.INTERNET");
        if (permissionCheck != 0) {
            String errorType = this.context.getString(R.string.com_facebook_internet_permission_error_title);
            String errorDescription = this.context.getString(R.string.com_facebook_internet_permission_error_message);
            complete(Result.createErrorResult(errorType, errorDescription));
            return false;
        }
        this.checkedInternetPermission = true;
        return true;
    }

    void tryNextHandler() {
        while (this.handlersToTry != null && !this.handlersToTry.isEmpty()) {
            this.currentHandler = this.handlersToTry.remove(0);
            boolean started = tryCurrentHandler();
            if (started) {
                return;
            }
        }
        if (this.pendingRequest != null) {
            completeWithFailure();
        }
    }

    private void completeWithFailure() {
        complete(Result.createErrorResult("Login attempt failed.", null));
    }

    boolean tryCurrentHandler() {
        if (!this.currentHandler.needsInternetPermission() || checkInternetPermission()) {
            return this.currentHandler.tryAuthorize(this.pendingRequest);
        }
        return false;
    }

    void completeAndValidate(Result outcome) {
        if (outcome.token != null && this.pendingRequest.needsNewTokenValidation()) {
            validateSameFbidAndFinish(outcome);
        } else {
            complete(outcome);
        }
    }

    void complete(Result outcome) {
        this.handlersToTry = null;
        this.currentHandler = null;
        this.pendingRequest = null;
        notifyOnCompleteListener(outcome);
    }

    OnCompletedListener getOnCompletedListener() {
        return this.onCompletedListener;
    }

    void setOnCompletedListener(OnCompletedListener onCompletedListener) {
        this.onCompletedListener = onCompletedListener;
    }

    BackgroundProcessingListener getBackgroundProcessingListener() {
        return this.backgroundProcessingListener;
    }

    void setBackgroundProcessingListener(BackgroundProcessingListener backgroundProcessingListener) {
        this.backgroundProcessingListener = backgroundProcessingListener;
    }

    StartActivityDelegate getStartActivityDelegate() {
        if (this.startActivityDelegate != null) {
            return this.startActivityDelegate;
        }
        if (this.pendingRequest != null) {
            return new StartActivityDelegate() { // from class: com.facebook.AuthorizationClient.2
                @Override // com.facebook.AuthorizationClient.StartActivityDelegate
                public void startActivityForResult(Intent intent, int requestCode) {
                    AuthorizationClient.this.pendingRequest.getStartActivityDelegate().startActivityForResult(intent, requestCode);
                }

                @Override // com.facebook.AuthorizationClient.StartActivityDelegate
                public Activity getActivityContext() {
                    return AuthorizationClient.this.pendingRequest.getStartActivityDelegate().getActivityContext();
                }
            };
        }
        return null;
    }

    int checkPermission(String permission) {
        return this.context.checkCallingOrSelfPermission(permission);
    }

    void validateSameFbidAndFinish(Result pendingResult) {
        if (pendingResult.token == null) {
            throw new FacebookException("Can't validate without a token");
        }
        RequestBatch batch = createReauthValidationBatch(pendingResult);
        notifyBackgroundProcessingStart();
        batch.executeAsync();
    }

    RequestBatch createReauthValidationBatch(final Result pendingResult) {
        final ArrayList<String> fbids = new ArrayList<>();
        final ArrayList<String> tokenPermissions = new ArrayList<>();
        String newToken = pendingResult.token.getToken();
        Request.Callback meCallback = new Request.Callback() { // from class: com.facebook.AuthorizationClient.3
            @Override // com.facebook.Request.Callback
            public void onCompleted(Response response) {
                try {
                    GraphUser user = (GraphUser) response.getGraphObjectAs(GraphUser.class);
                    if (user != null) {
                        fbids.add(user.getId());
                    }
                } catch (Exception e) {
                }
            }
        };
        String validateSameFbidAsToken = this.pendingRequest.getPreviousAccessToken();
        Request requestCurrentTokenMe = createGetProfileIdRequest(validateSameFbidAsToken);
        requestCurrentTokenMe.setCallback(meCallback);
        Request requestNewTokenMe = createGetProfileIdRequest(newToken);
        requestNewTokenMe.setCallback(meCallback);
        Request requestCurrentTokenPermissions = createGetPermissionsRequest(validateSameFbidAsToken);
        requestCurrentTokenPermissions.setCallback(new Request.Callback() { // from class: com.facebook.AuthorizationClient.4
            @Override // com.facebook.Request.Callback
            public void onCompleted(Response response) {
                GraphObjectList<GraphObject> data;
                try {
                    GraphMultiResult result = (GraphMultiResult) response.getGraphObjectAs(GraphMultiResult.class);
                    if (result != null && (data = result.getData()) != null && data.size() == 1) {
                        GraphObject permissions = data.get(0);
                        tokenPermissions.addAll(permissions.asMap().keySet());
                    }
                } catch (Exception e) {
                }
            }
        });
        RequestBatch batch = new RequestBatch(requestCurrentTokenMe, requestNewTokenMe, requestCurrentTokenPermissions);
        batch.setBatchApplicationId(this.pendingRequest.getApplicationId());
        batch.addCallback(new RequestBatch.Callback() { // from class: com.facebook.AuthorizationClient.5
            @Override // com.facebook.RequestBatch.Callback
            public void onBatchCompleted(RequestBatch batch2) {
                Result result;
                try {
                    if (fbids.size() == 2 && fbids.get(0) != null && fbids.get(1) != null && ((String) fbids.get(0)).equals(fbids.get(1))) {
                        AccessToken tokenWithPermissions = AccessToken.createFromTokenWithRefreshedPermissions(pendingResult.token, tokenPermissions);
                        result = Result.createTokenResult(tokenWithPermissions);
                    } else {
                        result = Result.createErrorResult("User logged in as different Facebook user.", null);
                    }
                    AuthorizationClient.this.complete(result);
                } catch (Exception ex) {
                    AuthorizationClient.this.complete(Result.createErrorResult("Caught exception", ex.getMessage()));
                } finally {
                    AuthorizationClient.this.notifyBackgroundProcessingStop();
                }
            }
        });
        return batch;
    }

    Request createGetPermissionsRequest(String accessToken) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        parameters.putString("access_token", accessToken);
        return new Request(null, "me/permissions", parameters, HttpMethod.GET, null);
    }

    Request createGetProfileIdRequest(String accessToken) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        parameters.putString("access_token", accessToken);
        return new Request(null, "me", parameters, HttpMethod.GET, null);
    }

    private void notifyOnCompleteListener(Result outcome) {
        if (this.onCompletedListener != null) {
            this.onCompletedListener.onCompleted(outcome);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyBackgroundProcessingStart() {
        if (this.backgroundProcessingListener != null) {
            this.backgroundProcessingListener.onBackgroundProcessingStarted();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyBackgroundProcessingStop() {
        if (this.backgroundProcessingListener != null) {
            this.backgroundProcessingListener.onBackgroundProcessingStopped();
        }
    }

    abstract class AuthHandler implements Serializable {
        private static final long serialVersionUID = 1;

        abstract boolean tryAuthorize(AuthorizationRequest authorizationRequest);

        AuthHandler() {
        }

        boolean onActivityResult(int requestCode, int resultCode, Intent data) {
            return false;
        }

        boolean needsRestart() {
            return false;
        }

        boolean needsInternetPermission() {
            return false;
        }

        void cancel() {
        }
    }

    class WebViewAuthHandler extends AuthHandler {
        private static final long serialVersionUID = 1;
        private transient WebDialog loginDialog;

        WebViewAuthHandler() {
            super();
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean needsRestart() {
            return true;
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean needsInternetPermission() {
            return true;
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        void cancel() {
            if (this.loginDialog != null) {
                this.loginDialog.dismiss();
                this.loginDialog = null;
            }
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean tryAuthorize(final AuthorizationRequest request) {
            String applicationId = request.getApplicationId();
            Bundle parameters = new Bundle();
            if (!Utility.isNullOrEmpty(request.getPermissions())) {
                parameters.putString("scope", TextUtils.join(",", request.getPermissions()));
            }
            String previousToken = request.getPreviousAccessToken();
            if (!Utility.isNullOrEmpty(previousToken) && previousToken.equals(loadCookieToken())) {
                parameters.putString("access_token", previousToken);
            } else {
                Utility.clearFacebookCookies(AuthorizationClient.this.context);
            }
            WebDialog.OnCompleteListener listener = new WebDialog.OnCompleteListener() { // from class: com.facebook.AuthorizationClient.WebViewAuthHandler.1
                @Override // com.facebook.widget.WebDialog.OnCompleteListener
                public void onComplete(Bundle values, FacebookException error) {
                    WebViewAuthHandler.this.onWebDialogComplete(request, values, error);
                }
            };
            WebDialog.Builder builder = new AuthDialogBuilder(AuthorizationClient.this.getStartActivityDelegate().getActivityContext(), applicationId, parameters).setOnCompleteListener(listener);
            this.loginDialog = builder.build();
            this.loginDialog.show();
            return true;
        }

        void onWebDialogComplete(AuthorizationRequest request, Bundle values, FacebookException error) {
            Result outcome;
            if (values != null) {
                AccessToken token = AccessToken.createFromWebBundle(request.getPermissions(), values, AccessTokenSource.WEB_VIEW);
                outcome = Result.createTokenResult(token);
                CookieSyncManager syncManager = CookieSyncManager.createInstance(AuthorizationClient.this.context);
                syncManager.sync();
                saveCookieToken(token.getToken());
            } else if (error instanceof FacebookOperationCanceledException) {
                outcome = Result.createCancelResult("User canceled log in.");
            } else {
                outcome = Result.createErrorResult(error.getMessage(), null);
            }
            AuthorizationClient.this.completeAndValidate(outcome);
        }

        private void saveCookieToken(String token) {
            Context context = AuthorizationClient.this.getStartActivityDelegate().getActivityContext();
            SharedPreferences sharedPreferences = context.getSharedPreferences(AuthorizationClient.WEB_VIEW_AUTH_HANDLER_STORE, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(AuthorizationClient.WEB_VIEW_AUTH_HANDLER_TOKEN_KEY, token);
            if (!editor.commit()) {
                Utility.logd(AuthorizationClient.TAG, "Could not update saved web view auth handler token.");
            }
        }

        private String loadCookieToken() {
            Context context = AuthorizationClient.this.getStartActivityDelegate().getActivityContext();
            SharedPreferences sharedPreferences = context.getSharedPreferences(AuthorizationClient.WEB_VIEW_AUTH_HANDLER_STORE, 0);
            return sharedPreferences.getString(AuthorizationClient.WEB_VIEW_AUTH_HANDLER_TOKEN_KEY, "");
        }
    }

    class GetTokenAuthHandler extends AuthHandler {
        private static final long serialVersionUID = 1;
        private transient GetTokenClient getTokenClient;

        GetTokenAuthHandler() {
            super();
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        void cancel() {
            if (this.getTokenClient != null) {
                this.getTokenClient.cancel();
                this.getTokenClient = null;
            }
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean tryAuthorize(final AuthorizationRequest request) {
            this.getTokenClient = new GetTokenClient(AuthorizationClient.this.context, request.getApplicationId());
            if (this.getTokenClient.start()) {
                AuthorizationClient.this.notifyBackgroundProcessingStart();
                GetTokenClient.CompletedListener callback = new GetTokenClient.CompletedListener() { // from class: com.facebook.AuthorizationClient.GetTokenAuthHandler.1
                    @Override // com.facebook.GetTokenClient.CompletedListener
                    public void completed(Bundle result) {
                        GetTokenAuthHandler.this.getTokenCompleted(request, result);
                    }
                };
                this.getTokenClient.setCompletedListener(callback);
                return true;
            }
            return false;
        }

        void getTokenCompleted(AuthorizationRequest request, Bundle result) {
            this.getTokenClient = null;
            AuthorizationClient.this.notifyBackgroundProcessingStop();
            if (result != null) {
                ArrayList<String> currentPermissions = result.getStringArrayList("com.facebook.platform.extra.PERMISSIONS");
                List<String> permissions = request.getPermissions();
                if (currentPermissions != null && (permissions == null || currentPermissions.containsAll(permissions))) {
                    AccessToken token = AccessToken.createFromNativeLogin(result, AccessTokenSource.FACEBOOK_APPLICATION_SERVICE);
                    Result outcome = Result.createTokenResult(token);
                    AuthorizationClient.this.completeAndValidate(outcome);
                    return;
                } else {
                    ArrayList<String> newPermissions = new ArrayList<>();
                    for (String permission : permissions) {
                        if (!currentPermissions.contains(permission)) {
                            newPermissions.add(permission);
                        }
                    }
                    request.setPermissions(newPermissions);
                }
            }
            AuthorizationClient.this.tryNextHandler();
        }
    }

    abstract class KatanaAuthHandler extends AuthHandler {
        private static final long serialVersionUID = 1;

        KatanaAuthHandler() {
            super();
        }

        protected boolean tryIntent(Intent intent, int requestCode) {
            if (intent == null) {
                return false;
            }
            try {
                AuthorizationClient.this.getStartActivityDelegate().startActivityForResult(intent, requestCode);
                return true;
            } catch (ActivityNotFoundException e) {
                return false;
            }
        }
    }

    class KatanaLoginDialogAuthHandler extends KatanaAuthHandler {
        private static final long serialVersionUID = 1;

        KatanaLoginDialogAuthHandler() {
            super();
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean tryAuthorize(AuthorizationRequest request) {
            Intent intent = NativeProtocol.createLoginDialog20121101Intent(AuthorizationClient.this.context, request.getApplicationId(), new ArrayList(request.getPermissions()), request.getDefaultAudience().getNativeProtocolAudience());
            return tryIntent(intent, request.getRequestCode());
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean onActivityResult(int requestCode, int resultCode, Intent data) {
            Result outcome;
            if (data == null) {
                outcome = Result.createCancelResult("Operation canceled");
            } else if (NativeProtocol.isServiceDisabledResult20121101(data)) {
                outcome = null;
            } else if (resultCode == 0) {
                outcome = Result.createCancelResult(data.getStringExtra("com.facebook.platform.status.ERROR_DESCRIPTION"));
            } else if (resultCode != -1) {
                outcome = Result.createErrorResult("Unexpected resultCode from authorization.", null);
            } else {
                outcome = handleResultOk(data);
            }
            if (outcome != null) {
                AuthorizationClient.this.completeAndValidate(outcome);
                return true;
            }
            AuthorizationClient.this.tryNextHandler();
            return true;
        }

        private Result handleResultOk(Intent data) {
            Bundle extras = data.getExtras();
            String errorType = extras.getString("com.facebook.platform.status.ERROR_TYPE");
            if (errorType == null) {
                return Result.createTokenResult(AccessToken.createFromNativeLogin(extras, AccessTokenSource.FACEBOOK_APPLICATION_NATIVE));
            }
            if ("ServiceDisabled".equals(errorType)) {
                return null;
            }
            if ("UserCanceled".equals(errorType)) {
                return Result.createCancelResult(null);
            }
            return Result.createErrorResult(errorType, extras.getString("error_description"));
        }
    }

    class KatanaProxyAuthHandler extends KatanaAuthHandler {
        private static final long serialVersionUID = 1;

        KatanaProxyAuthHandler() {
            super();
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean tryAuthorize(AuthorizationRequest request) {
            Intent intent = NativeProtocol.createProxyAuthIntent(AuthorizationClient.this.context, request.getApplicationId(), request.getPermissions());
            return tryIntent(intent, request.getRequestCode());
        }

        @Override // com.facebook.AuthorizationClient.AuthHandler
        boolean onActivityResult(int requestCode, int resultCode, Intent data) {
            Result outcome;
            if (data == null) {
                outcome = Result.createCancelResult("Operation canceled");
            } else if (resultCode == 0) {
                outcome = Result.createCancelResult(data.getStringExtra("error"));
            } else if (resultCode != -1) {
                outcome = Result.createErrorResult("Unexpected resultCode from authorization.", null);
            } else {
                outcome = handleResultOk(data);
            }
            if (outcome != null) {
                AuthorizationClient.this.completeAndValidate(outcome);
                return true;
            }
            AuthorizationClient.this.tryNextHandler();
            return true;
        }

        private Result handleResultOk(Intent data) {
            Bundle extras = data.getExtras();
            String error = extras.getString("error");
            if (error == null) {
                error = extras.getString("error_type");
            }
            if (error == null) {
                AccessToken token = AccessToken.createFromWebBundle(AuthorizationClient.this.pendingRequest.getPermissions(), extras, AccessTokenSource.FACEBOOK_APPLICATION_WEB);
                return Result.createTokenResult(token);
            }
            if (ServerProtocol.errorsProxyAuthDisabled.contains(error)) {
                return null;
            }
            if (ServerProtocol.errorsUserCanceled.contains(error)) {
                return Result.createCancelResult(null);
            }
            return Result.createErrorResult(error, extras.getString("error_description"));
        }
    }

    static class AuthDialogBuilder extends WebDialog.Builder {
        private static final String OAUTH_DIALOG = "oauth";
        static final String REDIRECT_URI = "fbconnect://success";

        public AuthDialogBuilder(Context context, String applicationId, Bundle parameters) {
            super(context, applicationId, OAUTH_DIALOG, parameters);
        }

        @Override // com.facebook.widget.WebDialog.Builder, com.facebook.widget.WebDialog.BuilderBase
        public WebDialog build() {
            Bundle parameters = getParameters();
            parameters.putString(ServerProtocol.DIALOG_PARAM_REDIRECT_URI, "fbconnect://success");
            parameters.putString("client_id", getApplicationId());
            return new WebDialog(getContext(), OAUTH_DIALOG, parameters, getTheme(), getListener());
        }
    }

    static class AuthorizationRequest implements Serializable {
        private static final long serialVersionUID = 1;
        private String applicationId;
        private SessionDefaultAudience defaultAudience;
        private boolean isLegacy;
        private SessionLoginBehavior loginBehavior;
        private List<String> permissions;
        private String previousAccessToken;
        private int requestCode;
        private final transient StartActivityDelegate startActivityDelegate;

        AuthorizationRequest(SessionLoginBehavior loginBehavior, int requestCode, boolean isLegacy, List<String> permissions, SessionDefaultAudience defaultAudience, String applicationId, String validateSameFbidAsToken, StartActivityDelegate startActivityDelegate) {
            this.isLegacy = false;
            this.loginBehavior = loginBehavior;
            this.requestCode = requestCode;
            this.isLegacy = isLegacy;
            this.permissions = permissions;
            this.defaultAudience = defaultAudience;
            this.applicationId = applicationId;
            this.previousAccessToken = validateSameFbidAsToken;
            this.startActivityDelegate = startActivityDelegate;
        }

        StartActivityDelegate getStartActivityDelegate() {
            return this.startActivityDelegate;
        }

        List<String> getPermissions() {
            return this.permissions;
        }

        void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        SessionLoginBehavior getLoginBehavior() {
            return this.loginBehavior;
        }

        int getRequestCode() {
            return this.requestCode;
        }

        SessionDefaultAudience getDefaultAudience() {
            return this.defaultAudience;
        }

        String getApplicationId() {
            return this.applicationId;
        }

        boolean isLegacy() {
            return this.isLegacy;
        }

        void setIsLegacy(boolean isLegacy) {
            this.isLegacy = isLegacy;
        }

        String getPreviousAccessToken() {
            return this.previousAccessToken;
        }

        boolean needsNewTokenValidation() {
            return (this.previousAccessToken == null || this.isLegacy) ? false : true;
        }
    }

    static class Result implements Serializable {
        private static final long serialVersionUID = 1;
        final Code code;
        final String errorMessage;
        final AccessToken token;

        enum Code {
            SUCCESS,
            CANCEL,
            ERROR
        }

        private Result(Code code, AccessToken token, String errorMessage) {
            this.token = token;
            this.errorMessage = errorMessage;
            this.code = code;
        }

        static Result createTokenResult(AccessToken token) {
            return new Result(Code.SUCCESS, token, null);
        }

        static Result createCancelResult(String message) {
            return new Result(Code.CANCEL, null, message);
        }

        static Result createErrorResult(String errorType, String errorDescription) {
            String message = errorType;
            if (errorDescription != null) {
                message = message + ": " + errorDescription;
            }
            return new Result(Code.ERROR, null, message);
        }
    }
}
