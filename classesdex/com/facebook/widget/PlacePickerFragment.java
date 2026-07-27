package com.facebook.widget;

import android.app.Activity;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.facebook.FacebookException;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.android.R;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;
import com.facebook.model.GraphPlace;
import com.facebook.widget.GraphObjectAdapter;
import com.facebook.widget.PickerFragment;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class PlacePickerFragment extends PickerFragment<GraphPlace> {
    private static final String CATEGORY = "category";
    public static final int DEFAULT_RADIUS_IN_METERS = 1000;
    public static final int DEFAULT_RESULTS_LIMIT = 100;
    private static final String ID = "id";
    private static final String LOCATION = "location";
    public static final String LOCATION_BUNDLE_KEY = "com.facebook.widget.PlacePickerFragment.Location";
    private static final String NAME = "name";
    public static final String RADIUS_IN_METERS_BUNDLE_KEY = "com.facebook.widget.PlacePickerFragment.RadiusInMeters";
    public static final String RESULTS_LIMIT_BUNDLE_KEY = "com.facebook.widget.PlacePickerFragment.ResultsLimit";
    public static final String SEARCH_TEXT_BUNDLE_KEY = "com.facebook.widget.PlacePickerFragment.SearchText";
    public static final String SHOW_SEARCH_BOX_BUNDLE_KEY = "com.facebook.widget.PlacePickerFragment.ShowSearchBox";
    private static final String TAG = "PlacePickerFragment";
    private static final String WERE_HERE_COUNT = "were_here_count";
    private static final int searchTextTimerDelayInMilliseconds = 2000;
    private boolean hasSearchTextChangedSinceLastQuery;
    private Location location;
    private int radiusInMeters;
    private int resultsLimit;
    private EditText searchBox;
    private String searchText;
    private Timer searchTextTimer;
    private boolean showSearchBox;

    public PlacePickerFragment() {
        this(null);
    }

    public PlacePickerFragment(Bundle args) {
        super(GraphPlace.class, R.layout.com_facebook_placepickerfragment, args);
        this.radiusInMeters = 1000;
        this.resultsLimit = 100;
        this.showSearchBox = true;
        setPlacePickerSettingsFromBundle(args);
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getRadiusInMeters() {
        return this.radiusInMeters;
    }

    public void setRadiusInMeters(int radiusInMeters) {
        this.radiusInMeters = radiusInMeters;
    }

    public int getResultsLimit() {
        return this.resultsLimit;
    }

    public void setResultsLimit(int resultsLimit) {
        this.resultsLimit = resultsLimit;
    }

    public String getSearchText() {
        return this.searchText;
    }

    public void setSearchText(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            searchText = null;
        }
        this.searchText = searchText;
        if (this.searchBox != null) {
            this.searchBox.setText(searchText);
        }
    }

    public void onSearchBoxTextChanged(String searchText, boolean forceReloadEventIfSameText) {
        if (forceReloadEventIfSameText || !Utility.stringsEqualOrEmpty(this.searchText, searchText)) {
            if (TextUtils.isEmpty(searchText)) {
                searchText = null;
            }
            this.searchText = searchText;
            this.hasSearchTextChangedSinceLastQuery = true;
            if (this.searchTextTimer == null) {
                this.searchTextTimer = createSearchTextTimer();
            }
        }
    }

    public GraphPlace getSelection() {
        Collection<GraphPlace> selection = getSelectedGraphObjects();
        if (selection == null || selection.size() <= 0) {
            return null;
        }
        return selection.iterator().next();
    }

    @Override // com.facebook.widget.PickerFragment
    public void setSettingsFromBundle(Bundle inState) {
        super.setSettingsFromBundle(inState);
        setPlacePickerSettingsFromBundle(inState);
    }

    @Override // com.facebook.widget.PickerFragment, android.support.v4.app.Fragment
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.com_facebook_place_picker_fragment);
        setRadiusInMeters(a.getInt(0, this.radiusInMeters));
        setResultsLimit(a.getInt(1, this.resultsLimit));
        if (a.hasValue(1)) {
            setSearchText(a.getString(2));
        }
        this.showSearchBox = a.getBoolean(3, this.showSearchBox);
        a.recycle();
    }

    @Override // com.facebook.widget.PickerFragment, android.support.v4.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        ViewStub stub;
        super.onActivityCreated(savedInstanceState);
        ViewGroup view = (ViewGroup) getView();
        if (this.showSearchBox && (stub = (ViewStub) view.findViewById(R.id.com_facebook_placepickerfragment_search_box_stub)) != null) {
            this.searchBox = (EditText) stub.inflate();
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
            layoutParams.addRule(3, R.id.search_box);
            ListView listView = (ListView) view.findViewById(R.id.com_facebook_picker_list_view);
            listView.setLayoutParams(layoutParams);
            if (view.findViewById(R.id.com_facebook_picker_title_bar) != null) {
                RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-1, -2);
                layoutParams2.addRule(3, R.id.com_facebook_picker_title_bar);
                this.searchBox.setLayoutParams(layoutParams2);
            }
            this.searchBox.addTextChangedListener(new SearchTextWatcher());
            if (!TextUtils.isEmpty(this.searchText)) {
                this.searchBox.setText(this.searchText);
            }
        }
    }

    @Override // android.support.v4.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (this.searchBox != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService("input_method");
            imm.showSoftInput(this.searchBox, 1);
        }
    }

    @Override // com.facebook.widget.PickerFragment, android.support.v4.app.Fragment
    public void onDetach() {
        super.onDetach();
        if (this.searchBox != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService("input_method");
            imm.hideSoftInputFromWindow(this.searchBox.getWindowToken(), 0);
        }
    }

    @Override // com.facebook.widget.PickerFragment
    void saveSettingsToBundle(Bundle outState) {
        super.saveSettingsToBundle(outState);
        outState.putInt(RADIUS_IN_METERS_BUNDLE_KEY, this.radiusInMeters);
        outState.putInt(RESULTS_LIMIT_BUNDLE_KEY, this.resultsLimit);
        outState.putString(SEARCH_TEXT_BUNDLE_KEY, this.searchText);
        outState.putParcelable(LOCATION_BUNDLE_KEY, this.location);
        outState.putBoolean(SHOW_SEARCH_BOX_BUNDLE_KEY, this.showSearchBox);
    }

    @Override // com.facebook.widget.PickerFragment
    void onLoadingData() {
        this.hasSearchTextChangedSinceLastQuery = false;
    }

    @Override // com.facebook.widget.PickerFragment
    Request getRequestForLoadData(Session session) {
        return createRequest(this.location, this.radiusInMeters, this.resultsLimit, this.searchText, this.extraFields, session);
    }

    @Override // com.facebook.widget.PickerFragment
    String getDefaultTitleText() {
        return getString(R.string.com_facebook_nearby);
    }

    @Override // com.facebook.widget.PickerFragment
    PickerFragment<GraphPlace>.PickerFragmentAdapter<GraphPlace> createAdapter() {
        PickerFragment<GraphPlace>.PickerFragmentAdapter<GraphPlace> adapter = new PickerFragment<GraphPlace>.PickerFragmentAdapter<GraphPlace>(getActivity()) { // from class: com.facebook.widget.PlacePickerFragment.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.facebook.widget.GraphObjectAdapter
            public CharSequence getSubTitleOfGraphObject(GraphPlace graphObject) {
                String category = graphObject.getCategory();
                Integer wereHereCount = (Integer) graphObject.getProperty(PlacePickerFragment.WERE_HERE_COUNT);
                if (category != null && wereHereCount != null) {
                    String result = PlacePickerFragment.this.getString(R.string.com_facebook_placepicker_subtitle_format, category, wereHereCount);
                    return result;
                }
                if (category == null && wereHereCount != null) {
                    String result2 = PlacePickerFragment.this.getString(R.string.com_facebook_placepicker_subtitle_were_here_only_format, wereHereCount);
                    return result2;
                }
                if (category == null || wereHereCount != null) {
                    return null;
                }
                String result3 = PlacePickerFragment.this.getString(R.string.com_facebook_placepicker_subtitle_catetory_only_format, category);
                return result3;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.facebook.widget.GraphObjectAdapter
            public int getGraphObjectRowLayoutId(GraphPlace graphObject) {
                return R.layout.com_facebook_placepickerfragment_list_row;
            }

            @Override // com.facebook.widget.GraphObjectAdapter
            protected int getDefaultPicture() {
                return R.drawable.com_facebook_place_default_icon;
            }
        };
        adapter.setShowCheckbox(false);
        adapter.setShowPicture(getShowPictures());
        return adapter;
    }

    @Override // com.facebook.widget.PickerFragment
    PickerFragment<GraphPlace>.LoadingStrategy createLoadingStrategy() {
        return new AsNeededLoadingStrategy();
    }

    @Override // com.facebook.widget.PickerFragment
    PickerFragment<GraphPlace>.SelectionStrategy createSelectionStrategy() {
        return new PickerFragment.SingleSelectionStrategy();
    }

    private Request createRequest(Location location, int radiusInMeters, int resultsLimit, String searchText, Set<String> extraFields, Session session) {
        Request request = Request.newPlacesSearchRequest(session, location, radiusInMeters, resultsLimit, searchText, null);
        Set<String> fields = new HashSet<>(extraFields);
        String[] requiredFields = {ID, NAME, LOCATION, "category", WERE_HERE_COUNT};
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

    private void setPlacePickerSettingsFromBundle(Bundle inState) {
        if (inState != null) {
            setRadiusInMeters(inState.getInt(RADIUS_IN_METERS_BUNDLE_KEY, this.radiusInMeters));
            setResultsLimit(inState.getInt(RESULTS_LIMIT_BUNDLE_KEY, this.resultsLimit));
            if (inState.containsKey(SEARCH_TEXT_BUNDLE_KEY)) {
                setSearchText(inState.getString(SEARCH_TEXT_BUNDLE_KEY));
            }
            if (inState.containsKey(LOCATION_BUNDLE_KEY)) {
                Location location = (Location) inState.getParcelable(LOCATION_BUNDLE_KEY);
                setLocation(location);
            }
            this.showSearchBox = inState.getBoolean(SHOW_SEARCH_BOX_BUNDLE_KEY, this.showSearchBox);
        }
    }

    private Timer createSearchTextTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() { // from class: com.facebook.widget.PlacePickerFragment.2
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                PlacePickerFragment.this.onSearchTextTimerTriggered();
            }
        }, 0L, 2000L);
        return timer;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSearchTextTimerTriggered() {
        if (this.hasSearchTextChangedSinceLastQuery) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() { // from class: com.facebook.widget.PlacePickerFragment.3
                @Override // java.lang.Runnable
                public void run() {
                    FacebookException error = null;
                    try {
                        try {
                            PlacePickerFragment.this.loadData(true);
                            if (0 != 0) {
                                PickerFragment.OnErrorListener onErrorListener = PlacePickerFragment.this.getOnErrorListener();
                                if (onErrorListener != null) {
                                    onErrorListener.onError(PlacePickerFragment.this, null);
                                } else {
                                    Logger.log(LoggingBehavior.REQUESTS, PlacePickerFragment.TAG, "Error loading data : %s", null);
                                }
                            }
                        } catch (FacebookException fe) {
                            if (fe != null) {
                                PickerFragment.OnErrorListener onErrorListener2 = PlacePickerFragment.this.getOnErrorListener();
                                if (onErrorListener2 != null) {
                                    onErrorListener2.onError(PlacePickerFragment.this, fe);
                                } else {
                                    Logger.log(LoggingBehavior.REQUESTS, PlacePickerFragment.TAG, "Error loading data : %s", fe);
                                }
                            }
                        } catch (Exception e) {
                            FacebookException error2 = new FacebookException(e);
                            if (error2 != null) {
                                PickerFragment.OnErrorListener onErrorListener3 = PlacePickerFragment.this.getOnErrorListener();
                                if (onErrorListener3 != null) {
                                    onErrorListener3.onError(PlacePickerFragment.this, error2);
                                } else {
                                    Logger.log(LoggingBehavior.REQUESTS, PlacePickerFragment.TAG, "Error loading data : %s", error2);
                                }
                                error = error2;
                            } else {
                                error = error2;
                            }
                        }
                    } catch (Throwable th) {
                        if (error != null) {
                            PickerFragment.OnErrorListener onErrorListener4 = PlacePickerFragment.this.getOnErrorListener();
                            if (onErrorListener4 != null) {
                                onErrorListener4.onError(PlacePickerFragment.this, error);
                            } else {
                                Logger.log(LoggingBehavior.REQUESTS, PlacePickerFragment.TAG, "Error loading data : %s", error);
                            }
                        }
                        throw th;
                    }
                }
            });
        } else {
            this.searchTextTimer.cancel();
            this.searchTextTimer = null;
        }
    }

    private class AsNeededLoadingStrategy extends PickerFragment.LoadingStrategy {
        private AsNeededLoadingStrategy() {
            super();
        }

        @Override // com.facebook.widget.PickerFragment.LoadingStrategy
        public void attach(GraphObjectAdapter<GraphPlace> adapter) {
            super.attach(adapter);
            this.adapter.setDataNeededListener(new GraphObjectAdapter.DataNeededListener() { // from class: com.facebook.widget.PlacePickerFragment.AsNeededLoadingStrategy.1
                @Override // com.facebook.widget.GraphObjectAdapter.DataNeededListener
                public void onDataNeeded() {
                    if (!AsNeededLoadingStrategy.this.loader.isLoading()) {
                        AsNeededLoadingStrategy.this.loader.followNextLink();
                    }
                }
            });
        }

        @Override // com.facebook.widget.PickerFragment.LoadingStrategy
        protected void onLoadFinished(GraphObjectPagingLoader<GraphPlace> loader, SimpleGraphObjectCursor<GraphPlace> data) {
            super.onLoadFinished(loader, data);
            if (data != null && !loader.isLoading()) {
                PlacePickerFragment.this.hideActivityCircle();
                if (data.isFromCache()) {
                    loader.refreshOriginalRequest(data.areMoreObjectsAvailable() ? 2000L : 0L);
                }
            }
        }
    }

    private class SearchTextWatcher implements TextWatcher {
        private SearchTextWatcher() {
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            PlacePickerFragment.this.onSearchBoxTextChanged(s.toString(), false);
        }

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable s) {
        }
    }
}
