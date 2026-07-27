package com.facebook.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.android.R;
import com.facebook.internal.SessionAuthorizationType;
import com.facebook.internal.SessionTracker;
import com.facebook.internal.Utility;
import com.facebook.model.GraphUser;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class LoginButton extends Button {
    private static final String TAG = LoginButton.class.getName();
    private String applicationId;
    private boolean confirmLogout;
    private boolean fetchUserInfo;
    private String loginText;
    private String logoutText;
    private Fragment parentFragment;
    private LoginButtonProperties properties;
    private SessionTracker sessionTracker;
    private GraphUser user;
    private UserInfoChangedCallback userInfoChangedCallback;
    private Session userInfoSession;

    public interface OnErrorListener {
        void onError(FacebookException facebookException);
    }

    public interface UserInfoChangedCallback {
        void onUserInfoFetched(GraphUser graphUser);
    }

    static class LoginButtonProperties {
        private OnErrorListener onErrorListener;
        private Session.StatusCallback sessionStatusCallback;
        private SessionDefaultAudience defaultAudience = SessionDefaultAudience.FRIENDS;
        private List<String> permissions = Collections.emptyList();
        private SessionAuthorizationType authorizationType = null;
        private SessionLoginBehavior loginBehavior = SessionLoginBehavior.SSO_WITH_FALLBACK;

        LoginButtonProperties() {
        }

        public void setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
        }

        public OnErrorListener getOnErrorListener() {
            return this.onErrorListener;
        }

        public void setDefaultAudience(SessionDefaultAudience defaultAudience) {
            this.defaultAudience = defaultAudience;
        }

        public SessionDefaultAudience getDefaultAudience() {
            return this.defaultAudience;
        }

        public void setReadPermissions(List<String> permissions, Session session) {
            if (SessionAuthorizationType.PUBLISH.equals(this.authorizationType)) {
                throw new UnsupportedOperationException("Cannot call setReadPermissions after setPublishPermissions has been called.");
            }
            if (validatePermissions(permissions, SessionAuthorizationType.READ, session)) {
                this.permissions = permissions;
                this.authorizationType = SessionAuthorizationType.READ;
            }
        }

        public void setPublishPermissions(List<String> permissions, Session session) {
            if (SessionAuthorizationType.READ.equals(this.authorizationType)) {
                throw new UnsupportedOperationException("Cannot call setPublishPermissions after setReadPermissions has been called.");
            }
            if (validatePermissions(permissions, SessionAuthorizationType.PUBLISH, session)) {
                this.permissions = permissions;
                this.authorizationType = SessionAuthorizationType.PUBLISH;
            }
        }

        private boolean validatePermissions(List<String> permissions, SessionAuthorizationType authType, Session currentSession) {
            if (SessionAuthorizationType.PUBLISH.equals(authType) && Utility.isNullOrEmpty(permissions)) {
                throw new IllegalArgumentException("Permissions for publish actions cannot be null or empty.");
            }
            if (currentSession == null || !currentSession.isOpened() || Utility.isSubset(permissions, currentSession.getPermissions())) {
                return true;
            }
            Log.e(LoginButton.TAG, "Cannot set additional permissions when session is already open.");
            return false;
        }

        List<String> getPermissions() {
            return this.permissions;
        }

        public void clearPermissions() {
            this.permissions = null;
            this.authorizationType = null;
        }

        public void setLoginBehavior(SessionLoginBehavior loginBehavior) {
            this.loginBehavior = loginBehavior;
        }

        public SessionLoginBehavior getLoginBehavior() {
            return this.loginBehavior;
        }

        public void setSessionStatusCallback(Session.StatusCallback callback) {
            this.sessionStatusCallback = callback;
        }

        public Session.StatusCallback getSessionStatusCallback() {
            return this.sessionStatusCallback;
        }
    }

    public LoginButton(Context context) {
        super(context);
        this.applicationId = null;
        this.user = null;
        this.userInfoSession = null;
        this.properties = new LoginButtonProperties();
        initializeActiveSessionWithCachedToken(context);
        finishInit();
    }

    public LoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.applicationId = null;
        this.user = null;
        this.userInfoSession = null;
        this.properties = new LoginButtonProperties();
        if (attrs.getStyleAttribute() == 0) {
            setTextColor(getResources().getColor(R.color.com_facebook_loginview_text_color));
            setTextSize(0, getResources().getDimension(R.dimen.com_facebook_loginview_text_size));
            setPadding(getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_left), getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_top), getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_right), getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_bottom));
            setWidth(getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_width));
            setHeight(getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_height));
            setGravity(17);
            if (isInEditMode()) {
                setBackgroundColor(getResources().getColor(R.color.com_facebook_blue));
                this.loginText = "Log in";
            } else {
                setBackgroundResource(R.drawable.com_facebook_loginbutton_blue);
            }
        }
        parseAttributes(attrs);
        if (!isInEditMode()) {
            initializeActiveSessionWithCachedToken(context);
        }
    }

    public LoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.applicationId = null;
        this.user = null;
        this.userInfoSession = null;
        this.properties = new LoginButtonProperties();
        parseAttributes(attrs);
        initializeActiveSessionWithCachedToken(context);
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.properties.setOnErrorListener(onErrorListener);
    }

    public OnErrorListener getOnErrorListener() {
        return this.properties.getOnErrorListener();
    }

    public void setDefaultAudience(SessionDefaultAudience defaultAudience) {
        this.properties.setDefaultAudience(defaultAudience);
    }

    public SessionDefaultAudience getDefaultAudience() {
        return this.properties.getDefaultAudience();
    }

    public void setReadPermissions(List<String> permissions) {
        this.properties.setReadPermissions(permissions, this.sessionTracker.getSession());
    }

    public void setPublishPermissions(List<String> permissions) {
        this.properties.setPublishPermissions(permissions, this.sessionTracker.getSession());
    }

    public void clearPermissions() {
        this.properties.clearPermissions();
    }

    public void setLoginBehavior(SessionLoginBehavior loginBehavior) {
        this.properties.setLoginBehavior(loginBehavior);
    }

    public SessionLoginBehavior getLoginBehavior() {
        return this.properties.getLoginBehavior();
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public UserInfoChangedCallback getUserInfoChangedCallback() {
        return this.userInfoChangedCallback;
    }

    public void setUserInfoChangedCallback(UserInfoChangedCallback userInfoChangedCallback) {
        this.userInfoChangedCallback = userInfoChangedCallback;
    }

    public void setSessionStatusCallback(Session.StatusCallback callback) {
        this.properties.setSessionStatusCallback(callback);
    }

    public Session.StatusCallback getSessionStatusCallback() {
        return this.properties.getSessionStatusCallback();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Session session = this.sessionTracker.getSession();
        if (session != null) {
            return session.onActivityResult((Activity) getContext(), requestCode, resultCode, data);
        }
        return false;
    }

    public void setSession(Session newSession) {
        this.sessionTracker.setSession(newSession);
        fetchUserInfo();
        setButtonText();
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        finishInit();
    }

    private void finishInit() {
        setOnClickListener(new LoginClickListener());
        setButtonText();
        if (!isInEditMode()) {
            this.sessionTracker = new SessionTracker(getContext(), new LoginButtonCallback(), null, false);
            fetchUserInfo();
        }
    }

    public void setFragment(Fragment fragment) {
        this.parentFragment = fragment;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.sessionTracker != null && !this.sessionTracker.isTracking()) {
            this.sessionTracker.startTracking();
            fetchUserInfo();
            setButtonText();
        }
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.sessionTracker != null) {
            this.sessionTracker.stopTracking();
        }
    }

    List<String> getPermissions() {
        return this.properties.getPermissions();
    }

    void setProperties(LoginButtonProperties properties) {
        this.properties = properties;
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.com_facebook_login_view);
        this.confirmLogout = a.getBoolean(0, true);
        this.fetchUserInfo = a.getBoolean(1, true);
        this.loginText = a.getString(2);
        this.logoutText = a.getString(3);
        a.recycle();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setButtonText() {
        if (this.sessionTracker != null && this.sessionTracker.getOpenSession() != null) {
            setText(this.logoutText != null ? this.logoutText : getResources().getString(R.string.com_facebook_loginview_log_out_button));
        } else {
            setText(this.loginText != null ? this.loginText : getResources().getString(R.string.com_facebook_loginview_log_in_button));
        }
    }

    private boolean initializeActiveSessionWithCachedToken(Context context) {
        if (context == null) {
            return false;
        }
        Session session = Session.getActiveSession();
        if (session != null) {
            return session.isOpened();
        }
        String applicationId = Utility.getMetadataApplicationId(context);
        return (applicationId == null || Session.openActiveSessionFromCache(context) == null) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fetchUserInfo() {
        if (this.fetchUserInfo) {
            final Session currentSession = this.sessionTracker.getOpenSession();
            if (currentSession != null) {
                if (currentSession != this.userInfoSession) {
                    Request request = Request.newMeRequest(currentSession, new Request.GraphUserCallback() { // from class: com.facebook.widget.LoginButton.1
                        @Override // com.facebook.Request.GraphUserCallback
                        public void onCompleted(GraphUser me, Response response) {
                            if (currentSession == LoginButton.this.sessionTracker.getOpenSession()) {
                                LoginButton.this.user = me;
                                if (LoginButton.this.userInfoChangedCallback != null) {
                                    LoginButton.this.userInfoChangedCallback.onUserInfoFetched(LoginButton.this.user);
                                }
                            }
                            if (response.getError() != null) {
                                LoginButton.this.handleError(response.getError().getException());
                            }
                        }
                    });
                    Request.executeBatchAsync(request);
                    this.userInfoSession = currentSession;
                    return;
                }
                return;
            }
            this.user = null;
            if (this.userInfoChangedCallback != null) {
                this.userInfoChangedCallback.onUserInfoFetched(this.user);
            }
        }
    }

    private class LoginClickListener implements View.OnClickListener {
        private LoginClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            String message;
            Context context = LoginButton.this.getContext();
            final Session openSession = LoginButton.this.sessionTracker.getOpenSession();
            if (openSession != null) {
                if (LoginButton.this.confirmLogout) {
                    String logout = LoginButton.this.getResources().getString(R.string.com_facebook_loginview_log_out_action);
                    String cancel = LoginButton.this.getResources().getString(R.string.com_facebook_loginview_cancel_action);
                    if (LoginButton.this.user != null && LoginButton.this.user.getName() != null) {
                        message = String.format(LoginButton.this.getResources().getString(R.string.com_facebook_loginview_logged_in_as), LoginButton.this.user.getName());
                    } else {
                        message = LoginButton.this.getResources().getString(R.string.com_facebook_loginview_logged_in_using_facebook);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(message).setCancelable(true).setPositiveButton(logout, new DialogInterface.OnClickListener() { // from class: com.facebook.widget.LoginButton.LoginClickListener.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                            openSession.closeAndClearTokenInformation();
                        }
                    }).setNegativeButton(cancel, (DialogInterface.OnClickListener) null);
                    builder.create().show();
                    return;
                }
                openSession.closeAndClearTokenInformation();
                return;
            }
            Session currentSession = LoginButton.this.sessionTracker.getSession();
            if (currentSession == null || currentSession.getState().isClosed()) {
                LoginButton.this.sessionTracker.setSession(null);
                Session session = new Session.Builder(context).setApplicationId(LoginButton.this.applicationId).build();
                Session.setActiveSession(session);
                currentSession = session;
            }
            if (!currentSession.isOpened()) {
                Session.OpenRequest openRequest = null;
                if (LoginButton.this.parentFragment != null) {
                    openRequest = new Session.OpenRequest(LoginButton.this.parentFragment);
                } else if (context instanceof Activity) {
                    openRequest = new Session.OpenRequest((Activity) context);
                }
                if (openRequest != null) {
                    openRequest.setDefaultAudience(LoginButton.this.properties.defaultAudience);
                    openRequest.setPermissions(LoginButton.this.properties.permissions);
                    openRequest.setLoginBehavior(LoginButton.this.properties.loginBehavior);
                    if (SessionAuthorizationType.PUBLISH.equals(LoginButton.this.properties.authorizationType)) {
                        currentSession.openForPublish(openRequest);
                    } else {
                        currentSession.openForRead(openRequest);
                    }
                }
            }
        }
    }

    private class LoginButtonCallback implements Session.StatusCallback {
        private LoginButtonCallback() {
        }

        @Override // com.facebook.Session.StatusCallback
        public void call(Session session, SessionState state, Exception exception) {
            LoginButton.this.fetchUserInfo();
            LoginButton.this.setButtonText();
            if (exception != null) {
                LoginButton.this.handleError(exception);
            }
            if (LoginButton.this.properties.sessionStatusCallback != null) {
                LoginButton.this.properties.sessionStatusCallback.call(session, state, exception);
            }
        }
    }

    void handleError(Exception exception) {
        if (this.properties.onErrorListener != null) {
            if (exception instanceof FacebookException) {
                this.properties.onErrorListener.onError((FacebookException) exception);
            } else {
                this.properties.onErrorListener.onError(new FacebookException(exception));
            }
        }
    }
}
