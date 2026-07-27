package com.facebook.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.android.R;
import com.facebook.model.GraphUser;
import com.facebook.widget.PickerFragment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class FriendPickerFragment extends PickerFragment<GraphUser> {
    private static final String ID = "id";
    public static final String MULTI_SELECT_BUNDLE_KEY = "com.facebook.widget.FriendPickerFragment.MultiSelect";
    private static final String NAME = "name";
    public static final String USER_ID_BUNDLE_KEY = "com.facebook.widget.FriendPickerFragment.UserId";
    private boolean multiSelect;
    private String userId;

    public FriendPickerFragment() {
        this(null);
    }

    @SuppressLint({"ValidFragment"})
    public FriendPickerFragment(Bundle args) {
        super(GraphUser.class, R.layout.com_facebook_friendpickerfragment, args);
        this.multiSelect = true;
        setFriendPickerSettingsFromBundle(args);
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean getMultiSelect() {
        return this.multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        if (this.multiSelect != multiSelect) {
            this.multiSelect = multiSelect;
            setSelectionStrategy(createSelectionStrategy());
        }
    }

    public List<GraphUser> getSelection() {
        return getSelectedGraphObjects();
    }

    @Override // com.facebook.widget.PickerFragment, android.support.v4.app.Fragment
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.com_facebook_friend_picker_fragment);
        setMultiSelect(a.getBoolean(0, this.multiSelect));
        a.recycle();
    }

    @Override // com.facebook.widget.PickerFragment
    public void setSettingsFromBundle(Bundle inState) {
        super.setSettingsFromBundle(inState);
        setFriendPickerSettingsFromBundle(inState);
    }

    @Override // com.facebook.widget.PickerFragment
    void saveSettingsToBundle(Bundle outState) {
        super.saveSettingsToBundle(outState);
        outState.putString(USER_ID_BUNDLE_KEY, this.userId);
        outState.putBoolean(MULTI_SELECT_BUNDLE_KEY, this.multiSelect);
    }

    @Override // com.facebook.widget.PickerFragment
    PickerFragment<GraphUser>.PickerFragmentAdapter<GraphUser> createAdapter() {
        PickerFragment<GraphUser>.PickerFragmentAdapter<GraphUser> adapter = new PickerFragment<GraphUser>.PickerFragmentAdapter<GraphUser>(getActivity()) { // from class: com.facebook.widget.FriendPickerFragment.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.facebook.widget.GraphObjectAdapter
            public int getGraphObjectRowLayoutId(GraphUser graphObject) {
                return R.layout.com_facebook_picker_list_row;
            }

            @Override // com.facebook.widget.GraphObjectAdapter
            protected int getDefaultPicture() {
                return R.drawable.com_facebook_profile_default_icon;
            }
        };
        adapter.setShowCheckbox(true);
        adapter.setShowPicture(getShowPictures());
        adapter.setSortFields(Arrays.asList(NAME));
        adapter.setGroupByField(NAME);
        return adapter;
    }

    @Override // com.facebook.widget.PickerFragment
    PickerFragment<GraphUser>.LoadingStrategy createLoadingStrategy() {
        return new ImmediateLoadingStrategy();
    }

    @Override // com.facebook.widget.PickerFragment
    PickerFragment<GraphUser>.SelectionStrategy createSelectionStrategy() {
        return this.multiSelect ? new PickerFragment.MultiSelectionStrategy() : new PickerFragment.SingleSelectionStrategy();
    }

    @Override // com.facebook.widget.PickerFragment
    Request getRequestForLoadData(Session session) {
        if (this.adapter == null) {
            throw new FacebookException("Can't issue requests until Fragment has been created.");
        }
        String userToFetch = this.userId != null ? this.userId : "me";
        return createRequest(userToFetch, this.extraFields, session);
    }

    @Override // com.facebook.widget.PickerFragment
    String getDefaultTitleText() {
        return getString(R.string.com_facebook_choose_friends);
    }

    private Request createRequest(String userID, Set<String> extraFields, Session session) {
        Request request = Request.newGraphPathRequest(session, userID + "/friends", null);
        Set<String> fields = new HashSet<>(extraFields);
        String[] requiredFields = {ID, NAME};
        fields.addAll(Arrays.asList(requiredFields));
        String pictureField = this.adapter.getPictureFieldSpecifier();
        if (pictureField != null) {
            fields.add(pictureField);
        }
        Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);
        return request;
    }

    private void setFriendPickerSettingsFromBundle(Bundle inState) {
        if (inState != null) {
            if (inState.containsKey(USER_ID_BUNDLE_KEY)) {
                setUserId(inState.getString(USER_ID_BUNDLE_KEY));
            }
            setMultiSelect(inState.getBoolean(MULTI_SELECT_BUNDLE_KEY, this.multiSelect));
        }
    }

    private class ImmediateLoadingStrategy extends PickerFragment.LoadingStrategy {
        private ImmediateLoadingStrategy() {
            super();
        }

        @Override // com.facebook.widget.PickerFragment.LoadingStrategy
        protected void onLoadFinished(GraphObjectPagingLoader<GraphUser> loader, SimpleGraphObjectCursor<GraphUser> data) {
            super.onLoadFinished(loader, data);
            if (data != null && !loader.isLoading()) {
                if (data.areMoreObjectsAvailable()) {
                    followNextLink();
                    return;
                }
                FriendPickerFragment.this.hideActivityCircle();
                if (data.isFromCache()) {
                    loader.refreshOriginalRequest(data.getCount() == 0 ? 2000L : 0L);
                }
            }
        }

        private void followNextLink() {
            FriendPickerFragment.this.displayActivityCircle();
            this.loader.followNextLink();
        }
    }
}
