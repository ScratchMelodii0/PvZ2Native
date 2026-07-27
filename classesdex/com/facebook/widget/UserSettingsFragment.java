package com.facebook.widget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.android.R;
import com.facebook.model.GraphUser;
import com.facebook.widget.ImageRequest;
import com.facebook.widget.LoginButton;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class UserSettingsFragment extends FacebookFragment {
    private static final String FIELDS = "fields";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PICTURE = "picture";
    private static final String REQUEST_FIELDS = TextUtils.join(",", new String[]{ID, NAME, PICTURE});
    private TextView connectedStateLabel;
    private LoginButton loginButton;
    private LoginButton.LoginButtonProperties loginButtonProperties = new LoginButton.LoginButtonProperties();
    private Session.StatusCallback sessionStatusCallback;
    private GraphUser user;
    private Session userInfoSession;
    private Drawable userProfilePic;
    private String userProfilePicID;

    @Override // com.facebook.widget.FacebookFragment, android.support.v4.app.Fragment
    public /* bridge */ /* synthetic */ void onActivityCreated(Bundle x0) {
        super.onActivityCreated(x0);
    }

    @Override // com.facebook.widget.FacebookFragment, android.support.v4.app.Fragment
    public /* bridge */ /* synthetic */ void onActivityResult(int x0, int x1, Intent x2) {
        super.onActivityResult(x0, x1, x2);
    }

    @Override // com.facebook.widget.FacebookFragment, android.support.v4.app.Fragment
    public /* bridge */ /* synthetic */ void onDestroy() {
        super.onDestroy();
    }

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.com_facebook_usersettingsfragment, container, false);
        this.loginButton = (LoginButton) view.findViewById(R.id.com_facebook_usersettingsfragment_login_button);
        this.loginButton.setProperties(this.loginButtonProperties);
        this.loginButton.setFragment(this);
        Session session = getSession();
        if (session != null && !session.equals(Session.getActiveSession())) {
            this.loginButton.setSession(session);
        }
        this.connectedStateLabel = (TextView) view.findViewById(R.id.com_facebook_usersettingsfragment_profile_name);
        if (view.getBackground() == null) {
            view.setBackgroundColor(getResources().getColor(R.color.com_facebook_blue));
        } else {
            view.getBackground().setDither(true);
        }
        return view;
    }

    @Override // android.support.v4.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override // android.support.v4.app.Fragment
    public void onResume() {
        super.onResume();
        fetchUserInfo();
        updateUI();
    }

    @Override // com.facebook.widget.FacebookFragment
    public void setSession(Session newSession) {
        super.setSession(newSession);
        if (this.loginButton != null) {
            this.loginButton.setSession(newSession);
        }
        fetchUserInfo();
        updateUI();
    }

    public void setDefaultAudience(SessionDefaultAudience defaultAudience) {
        this.loginButtonProperties.setDefaultAudience(defaultAudience);
    }

    public SessionDefaultAudience getDefaultAudience() {
        return this.loginButtonProperties.getDefaultAudience();
    }

    public void setReadPermissions(List<String> permissions) {
        this.loginButtonProperties.setReadPermissions(permissions, getSession());
    }

    public void setPublishPermissions(List<String> permissions) {
        this.loginButtonProperties.setPublishPermissions(permissions, getSession());
    }

    public void clearPermissions() {
        this.loginButtonProperties.clearPermissions();
    }

    public void setLoginBehavior(SessionLoginBehavior loginBehavior) {
        this.loginButtonProperties.setLoginBehavior(loginBehavior);
    }

    public SessionLoginBehavior getLoginBehavior() {
        return this.loginButtonProperties.getLoginBehavior();
    }

    public void setOnErrorListener(LoginButton.OnErrorListener onErrorListener) {
        this.loginButtonProperties.setOnErrorListener(onErrorListener);
    }

    public LoginButton.OnErrorListener getOnErrorListener() {
        return this.loginButtonProperties.getOnErrorListener();
    }

    public void setSessionStatusCallback(Session.StatusCallback callback) {
        this.sessionStatusCallback = callback;
    }

    public Session.StatusCallback getSessionStatusCallback() {
        return this.sessionStatusCallback;
    }

    @Override // com.facebook.widget.FacebookFragment
    protected void onSessionStateChange(SessionState state, Exception exception) {
        fetchUserInfo();
        updateUI();
        if (this.sessionStatusCallback != null) {
            this.sessionStatusCallback.call(getSession(), state, exception);
        }
    }

    List<String> getPermissions() {
        return this.loginButtonProperties.getPermissions();
    }

    private void fetchUserInfo() {
        final Session currentSession = getSession();
        if (currentSession != null && currentSession.isOpened()) {
            if (currentSession != this.userInfoSession) {
                Request request = Request.newMeRequest(currentSession, new Request.GraphUserCallback() { // from class: com.facebook.widget.UserSettingsFragment.1
                    @Override // com.facebook.Request.GraphUserCallback
                    public void onCompleted(GraphUser me, Response response) {
                        if (currentSession == UserSettingsFragment.this.getSession()) {
                            UserSettingsFragment.this.user = me;
                            UserSettingsFragment.this.updateUI();
                        }
                        if (response.getError() != null) {
                            UserSettingsFragment.this.loginButton.handleError(response.getError().getException());
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString(FIELDS, REQUEST_FIELDS);
                request.setParameters(parameters);
                Request.executeBatchAsync(request);
                this.userInfoSession = currentSession;
                return;
            }
            return;
        }
        this.user = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUI() {
        if (isAdded()) {
            if (isSessionOpen()) {
                this.connectedStateLabel.setTextColor(getResources().getColor(R.color.com_facebook_usersettingsfragment_connected_text_color));
                this.connectedStateLabel.setShadowLayer(1.0f, BitmapDescriptorFactory.HUE_RED, -1.0f, getResources().getColor(R.color.com_facebook_usersettingsfragment_connected_shadow_color));
                if (this.user != null) {
                    ImageRequest request = getImageRequest();
                    if (request != null) {
                        URL requestUrl = request.getImageUrl();
                        if (!requestUrl.equals(this.connectedStateLabel.getTag())) {
                            if (this.user.getId().equals(this.userProfilePicID)) {
                                this.connectedStateLabel.setCompoundDrawables(null, this.userProfilePic, null, null);
                                this.connectedStateLabel.setTag(requestUrl);
                            } else {
                                ImageDownloader.downloadAsync(request);
                            }
                        }
                    }
                    this.connectedStateLabel.setText(this.user.getName());
                    return;
                }
                this.connectedStateLabel.setText(getResources().getString(R.string.com_facebook_usersettingsfragment_logged_in));
                Drawable noProfilePic = getResources().getDrawable(R.drawable.com_facebook_profile_default_icon);
                noProfilePic.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen.com_facebook_usersettingsfragment_profile_picture_width), getResources().getDimensionPixelSize(R.dimen.com_facebook_usersettingsfragment_profile_picture_height));
                this.connectedStateLabel.setCompoundDrawables(null, noProfilePic, null, null);
                return;
            }
            int textColor = getResources().getColor(R.color.com_facebook_usersettingsfragment_not_connected_text_color);
            this.connectedStateLabel.setTextColor(textColor);
            this.connectedStateLabel.setShadowLayer(BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_RED, textColor);
            this.connectedStateLabel.setText(getResources().getString(R.string.com_facebook_usersettingsfragment_not_logged_in));
            this.connectedStateLabel.setCompoundDrawables(null, null, null, null);
            this.connectedStateLabel.setTag(null);
        }
    }

    private ImageRequest getImageRequest() {
        try {
            ImageRequest.Builder requestBuilder = new ImageRequest.Builder(getActivity(), ImageRequest.getProfilePictureUrl(this.user.getId(), getResources().getDimensionPixelSize(R.dimen.com_facebook_usersettingsfragment_profile_picture_width), getResources().getDimensionPixelSize(R.dimen.com_facebook_usersettingsfragment_profile_picture_height)));
            ImageRequest request = requestBuilder.setCallerTag(this).setCallback(new ImageRequest.Callback() { // from class: com.facebook.widget.UserSettingsFragment.2
                @Override // com.facebook.widget.ImageRequest.Callback
                public void onCompleted(ImageResponse response) {
                    UserSettingsFragment.this.processImageResponse(UserSettingsFragment.this.user.getId(), response);
                }
            }).build();
            return request;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processImageResponse(String id, ImageResponse response) {
        Bitmap bitmap;
        if (response != null && (bitmap = response.getBitmap()) != null) {
            BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
            drawable.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen.com_facebook_usersettingsfragment_profile_picture_width), getResources().getDimensionPixelSize(R.dimen.com_facebook_usersettingsfragment_profile_picture_height));
            this.userProfilePic = drawable;
            this.userProfilePicID = id;
            this.connectedStateLabel.setCompoundDrawables(null, drawable, null, null);
            this.connectedStateLabel.setTag(response.getRequest().getImageUrl());
        }
    }
}
