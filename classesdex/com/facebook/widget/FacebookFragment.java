package com.facebook.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.internal.SessionAuthorizationType;
import com.facebook.internal.SessionTracker;
import java.util.Date;
import java.util.List;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class FacebookFragment extends Fragment {
    private SessionTracker sessionTracker;

    FacebookFragment() {
    }

    @Override // android.support.v4.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.sessionTracker = new SessionTracker(getActivity(), new DefaultSessionStatusCallback());
    }

    @Override // android.support.v4.app.Fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.sessionTracker.getSession().onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    @Override // android.support.v4.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.sessionTracker.stopTracking();
    }

    public void setSession(Session newSession) {
        if (this.sessionTracker != null) {
            this.sessionTracker.setSession(newSession);
        }
    }

    protected void onSessionStateChange(SessionState state, Exception exception) {
    }

    protected final Session getSession() {
        if (this.sessionTracker != null) {
            return this.sessionTracker.getSession();
        }
        return null;
    }

    protected final boolean isSessionOpen() {
        return (this.sessionTracker == null || this.sessionTracker.getOpenSession() == null) ? false : true;
    }

    protected final SessionState getSessionState() {
        Session currentSession;
        if (this.sessionTracker == null || (currentSession = this.sessionTracker.getSession()) == null) {
            return null;
        }
        return currentSession.getState();
    }

    protected final String getAccessToken() {
        Session currentSession;
        if (this.sessionTracker == null || (currentSession = this.sessionTracker.getOpenSession()) == null) {
            return null;
        }
        return currentSession.getAccessToken();
    }

    protected final Date getExpirationDate() {
        Session currentSession;
        if (this.sessionTracker == null || (currentSession = this.sessionTracker.getOpenSession()) == null) {
            return null;
        }
        return currentSession.getExpirationDate();
    }

    protected final void closeSession() {
        Session currentSession;
        if (this.sessionTracker != null && (currentSession = this.sessionTracker.getOpenSession()) != null) {
            currentSession.close();
        }
    }

    protected final void closeSessionAndClearTokenInformation() {
        Session currentSession;
        if (this.sessionTracker != null && (currentSession = this.sessionTracker.getOpenSession()) != null) {
            currentSession.closeAndClearTokenInformation();
        }
    }

    protected final List<String> getSessionPermissions() {
        Session currentSession;
        if (this.sessionTracker == null || (currentSession = this.sessionTracker.getSession()) == null) {
            return null;
        }
        return currentSession.getPermissions();
    }

    protected final void openSession() {
        openSessionForRead(null, null);
    }

    protected final void openSessionForRead(String applicationId, List<String> permissions) {
        openSessionForRead(applicationId, permissions, SessionLoginBehavior.SSO_WITH_FALLBACK, Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
    }

    protected final void openSessionForRead(String applicationId, List<String> permissions, SessionLoginBehavior behavior, int activityCode) {
        openSession(applicationId, permissions, behavior, activityCode, SessionAuthorizationType.READ);
    }

    protected final void openSessionForPublish(String applicationId, List<String> permissions) {
        openSessionForPublish(applicationId, permissions, SessionLoginBehavior.SSO_WITH_FALLBACK, Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
    }

    protected final void openSessionForPublish(String applicationId, List<String> permissions, SessionLoginBehavior behavior, int activityCode) {
        openSession(applicationId, permissions, behavior, activityCode, SessionAuthorizationType.PUBLISH);
    }

    private void openSession(String applicationId, List<String> permissions, SessionLoginBehavior behavior, int activityCode, SessionAuthorizationType authType) {
        if (this.sessionTracker != null) {
            Session currentSession = this.sessionTracker.getSession();
            if (currentSession == null || currentSession.getState().isClosed()) {
                Session session = new Session.Builder(getActivity()).setApplicationId(applicationId).build();
                Session.setActiveSession(session);
                currentSession = session;
            }
            if (!currentSession.isOpened()) {
                Session.OpenRequest openRequest = new Session.OpenRequest(this).setPermissions(permissions).setLoginBehavior(behavior).setRequestCode(activityCode);
                if (SessionAuthorizationType.PUBLISH.equals(authType)) {
                    currentSession.openForPublish(openRequest);
                } else {
                    currentSession.openForRead(openRequest);
                }
            }
        }
    }

    private class DefaultSessionStatusCallback implements Session.StatusCallback {
        private DefaultSessionStatusCallback() {
        }

        @Override // com.facebook.Session.StatusCallback
        public void call(Session session, SessionState state, Exception exception) {
            FacebookFragment.this.onSessionStateChange(state, exception);
        }
    }
}
