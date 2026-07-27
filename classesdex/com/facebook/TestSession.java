package com.facebook;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.Session;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class TestSession extends Session {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final String LOG_TAG = "FacebookSDK.TestSession";
    private static Map<String, TestAccount> appTestAccounts = null;
    private static final long serialVersionUID = 1;
    private static String testApplicationId;
    private static String testApplicationSecret;
    private final Mode mode;
    private final List<String> requestedPermissions;
    private final String sessionUniqueUserTag;
    private String testAccountId;
    private boolean wasAskedToExtendAccessToken;

    private interface FqlResponse extends GraphObject {
        GraphObjectList<FqlResult> getData();
    }

    private interface FqlResult extends GraphObject {
        GraphObjectList<GraphObject> getFqlResultSet();
    }

    private enum Mode {
        PRIVATE,
        SHARED
    }

    private interface TestAccount extends GraphObject {
        String getAccessToken();

        String getId();

        String getName();

        void setName(String str);
    }

    private interface UserAccount extends GraphObject {
        String getName();

        String getUid();

        void setName(String str);
    }

    static {
        $assertionsDisabled = !TestSession.class.desiredAssertionStatus();
    }

    TestSession(Activity activity, List<String> permissions, TokenCachingStrategy tokenCachingStrategy, String sessionUniqueUserTag, Mode mode) {
        super(activity, testApplicationId, tokenCachingStrategy);
        Validate.notNull(permissions, "permissions");
        Validate.notNullOrEmpty(testApplicationId, "testApplicationId");
        Validate.notNullOrEmpty(testApplicationSecret, "testApplicationSecret");
        this.sessionUniqueUserTag = sessionUniqueUserTag;
        this.mode = mode;
        this.requestedPermissions = permissions;
    }

    public static TestSession createSessionWithPrivateUser(Activity activity, List<String> permissions) {
        return createTestSession(activity, permissions, Mode.PRIVATE, null);
    }

    public static TestSession createSessionWithSharedUser(Activity activity, List<String> permissions) {
        return createSessionWithSharedUser(activity, permissions, null);
    }

    public static TestSession createSessionWithSharedUser(Activity activity, List<String> permissions, String sessionUniqueUserTag) {
        return createTestSession(activity, permissions, Mode.SHARED, sessionUniqueUserTag);
    }

    public static synchronized String getTestApplicationId() {
        return testApplicationId;
    }

    public static synchronized void setTestApplicationId(String applicationId) {
        if (testApplicationId != null && !testApplicationId.equals(applicationId)) {
            throw new FacebookException("Can't have more than one test application ID");
        }
        testApplicationId = applicationId;
    }

    public static synchronized String getTestApplicationSecret() {
        return testApplicationSecret;
    }

    public static synchronized void setTestApplicationSecret(String applicationSecret) {
        if (testApplicationSecret != null && !testApplicationSecret.equals(applicationSecret)) {
            throw new FacebookException("Can't have more than one test application secret");
        }
        testApplicationSecret = applicationSecret;
    }

    public final String getTestUserId() {
        return this.testAccountId;
    }

    private static synchronized TestSession createTestSession(Activity activity, List<String> permissions, Mode mode, String sessionUniqueUserTag) {
        if (Utility.isNullOrEmpty(testApplicationId) || Utility.isNullOrEmpty(testApplicationSecret)) {
            throw new FacebookException("Must provide app ID and secret");
        }
        if (Utility.isNullOrEmpty(permissions)) {
            permissions = Arrays.asList("email", "publish_actions");
        }
        return new TestSession(activity, permissions, new TestTokenCachingStrategy(), sessionUniqueUserTag, mode);
    }

    private static synchronized void retrieveTestAccountsForAppIfNeeded() {
        if (appTestAccounts == null) {
            appTestAccounts = new HashMap();
            String testAccountQuery = String.format("SELECT id,access_token FROM test_account WHERE app_id = %s", testApplicationId);
            Bundle parameters = new Bundle();
            try {
                JSONObject multiquery = new JSONObject();
                multiquery.put("test_accounts", testAccountQuery);
                multiquery.put("users", "SELECT uid,name FROM user WHERE uid IN (SELECT id FROM #test_accounts)");
                parameters.putString("q", multiquery.toString());
                parameters.putString("access_token", getAppAccessToken());
                Request request = new Request(null, "fql", parameters, null);
                Response response = request.executeAndWait();
                if (response.getError() != null) {
                    throw response.getError().getException();
                }
                FqlResponse fqlResponse = (FqlResponse) response.getGraphObjectAs(FqlResponse.class);
                GraphObjectList<FqlResult> fqlResults = fqlResponse.getData();
                if (fqlResults == null || fqlResults.size() != 2) {
                    throw new FacebookException("Unexpected number of results from FQL query");
                }
                Collection<TestAccount> testAccounts = fqlResults.get(0).getFqlResultSet().castToListOf(TestAccount.class);
                Collection<UserAccount> userAccounts = fqlResults.get(1).getFqlResultSet().castToListOf(UserAccount.class);
                populateTestAccounts(testAccounts, userAccounts);
            } catch (JSONException exception) {
                throw new FacebookException(exception);
            }
        }
    }

    private static synchronized void populateTestAccounts(Collection<TestAccount> testAccounts, Collection<UserAccount> userAccounts) {
        Iterator<TestAccount> it = testAccounts.iterator();
        while (it.hasNext()) {
            storeTestAccount(it.next());
        }
        for (UserAccount userAccount : userAccounts) {
            TestAccount testAccount = appTestAccounts.get(userAccount.getUid());
            if (testAccount != null) {
                testAccount.setName(userAccount.getName());
            }
        }
    }

    private static synchronized void storeTestAccount(TestAccount testAccount) {
        appTestAccounts.put(testAccount.getId(), testAccount);
    }

    private static synchronized TestAccount findTestAccountMatchingIdentifier(String identifier) {
        TestAccount testAccount;
        retrieveTestAccountsForAppIfNeeded();
        Iterator<TestAccount> it = appTestAccounts.values().iterator();
        while (true) {
            if (!it.hasNext()) {
                testAccount = null;
                break;
            }
            testAccount = it.next();
            if (testAccount.getName().contains(identifier)) {
                break;
            }
        }
        return testAccount;
    }

    @Override // com.facebook.Session
    public final String toString() {
        String superString = super.toString();
        return "{TestSession testUserId:" + this.testAccountId + " " + superString + "}";
    }

    @Override // com.facebook.Session
    void authorize(Session.AuthorizationRequest request) {
        if (this.mode == Mode.PRIVATE) {
            createTestAccountAndFinishAuth();
        } else {
            findOrCreateSharedTestAccount();
        }
    }

    @Override // com.facebook.Session
    void postStateChange(SessionState oldState, SessionState newState, Exception error) {
        String id = this.testAccountId;
        super.postStateChange(oldState, newState, error);
        if (newState.isClosed() && id != null && this.mode == Mode.PRIVATE) {
            deleteTestAccount(id, getAppAccessToken());
        }
    }

    boolean getWasAskedToExtendAccessToken() {
        return this.wasAskedToExtendAccessToken;
    }

    void forceExtendAccessToken(boolean forceExtendAccessToken) {
        AccessToken currentToken = getTokenInfo();
        setTokenInfo(new AccessToken(currentToken.getToken(), new Date(), currentToken.getPermissions(), AccessTokenSource.TEST_USER, new Date(0L)));
        setLastAttemptedTokenExtendDate(new Date(0L));
    }

    @Override // com.facebook.Session
    boolean shouldExtendAccessToken() {
        boolean result = super.shouldExtendAccessToken();
        this.wasAskedToExtendAccessToken = false;
        return result;
    }

    @Override // com.facebook.Session
    void extendAccessToken() throws Throwable {
        this.wasAskedToExtendAccessToken = true;
        super.extendAccessToken();
    }

    void fakeTokenRefreshAttempt() {
        setCurrentTokenRefreshRequest(new Session.TokenRefreshRequest());
    }

    static final String getAppAccessToken() {
        return testApplicationId + "|" + testApplicationSecret;
    }

    private void findOrCreateSharedTestAccount() {
        TestAccount testAccount = findTestAccountMatchingIdentifier(getSharedTestAccountIdentifier());
        if (testAccount != null) {
            finishAuthWithTestAccount(testAccount);
        } else {
            createTestAccountAndFinishAuth();
        }
    }

    private void finishAuthWithTestAccount(TestAccount testAccount) {
        this.testAccountId = testAccount.getId();
        AccessToken accessToken = AccessToken.createFromString(testAccount.getAccessToken(), this.requestedPermissions, AccessTokenSource.TEST_USER);
        finishAuthOrReauth(accessToken, null);
    }

    private TestAccount createTestAccountAndFinishAuth() {
        Bundle parameters = new Bundle();
        parameters.putString("installed", "true");
        parameters.putString("permissions", getPermissionsString());
        parameters.putString("access_token", getAppAccessToken());
        if (this.mode == Mode.SHARED) {
            parameters.putString("name", String.format("Shared %s Testuser", getSharedTestAccountIdentifier()));
        }
        String graphPath = String.format("%s/accounts/test-users", testApplicationId);
        Request createUserRequest = new Request(null, graphPath, parameters, HttpMethod.POST);
        Response response = createUserRequest.executeAndWait();
        FacebookRequestError error = response.getError();
        TestAccount testAccount = (TestAccount) response.getGraphObjectAs(TestAccount.class);
        if (error != null) {
            finishAuthOrReauth(null, error.getException());
            return null;
        }
        if (!$assertionsDisabled && testAccount == null) {
            throw new AssertionError();
        }
        if (this.mode == Mode.SHARED) {
            testAccount.setName(parameters.getString("name"));
            storeTestAccount(testAccount);
        }
        finishAuthWithTestAccount(testAccount);
        return testAccount;
    }

    private void deleteTestAccount(String testAccountId, String appAccessToken) {
        Bundle parameters = new Bundle();
        parameters.putString("access_token", appAccessToken);
        Request request = new Request(null, testAccountId, parameters, HttpMethod.DELETE);
        Response response = request.executeAndWait();
        FacebookRequestError error = response.getError();
        GraphObject graphObject = response.getGraphObject();
        if (error != null) {
            Log.w(LOG_TAG, String.format("Could not delete test account %s: %s", testAccountId, error.getException().toString()));
        } else if (graphObject.getProperty(Response.NON_JSON_RESPONSE_PROPERTY) == false) {
            Log.w(LOG_TAG, String.format("Could not delete test account %s: unknown reason", testAccountId));
        }
    }

    private String getPermissionsString() {
        return TextUtils.join(",", this.requestedPermissions);
    }

    private String getSharedTestAccountIdentifier() {
        long permissionsHash = ((long) getPermissionsString().hashCode()) & 4294967295L;
        long sessionTagHash = this.sessionUniqueUserTag != null ? ((long) this.sessionUniqueUserTag.hashCode()) & 4294967295L : 0L;
        long combinedHash = permissionsHash ^ sessionTagHash;
        return validNameStringFromInteger(combinedHash);
    }

    private String validNameStringFromInteger(long i) {
        String s = Long.toString(i);
        StringBuilder result = new StringBuilder("Perm");
        char lastChar = 0;
        char[] arr$ = s.toCharArray();
        for (char c : arr$) {
            if (c == lastChar) {
                c = (char) (c + '\n');
            }
            result.append((char) ((c + 'a') - 48));
            lastChar = c;
        }
        return result.toString();
    }

    private static final class TestTokenCachingStrategy extends TokenCachingStrategy {
        private Bundle bundle;

        private TestTokenCachingStrategy() {
        }

        @Override // com.facebook.TokenCachingStrategy
        public Bundle load() {
            return this.bundle;
        }

        @Override // com.facebook.TokenCachingStrategy
        public void save(Bundle value) {
            this.bundle = value;
        }

        @Override // com.facebook.TokenCachingStrategy
        public void clear() {
            this.bundle = null;
        }
    }
}
