package com.facebook.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.R;
import com.facebook.internal.SessionTracker;
import com.facebook.model.GraphObject;
import com.facebook.widget.GraphObjectAdapter;
import com.facebook.widget.GraphObjectPagingLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public abstract class PickerFragment<T extends GraphObject> extends Fragment {
    private static final String ACTIVITY_CIRCLE_SHOW_KEY = "com.facebook.android.PickerFragment.ActivityCircleShown";
    public static final String DONE_BUTTON_TEXT_BUNDLE_KEY = "com.facebook.widget.PickerFragment.DoneButtonText";
    public static final String EXTRA_FIELDS_BUNDLE_KEY = "com.facebook.widget.PickerFragment.ExtraFields";
    private static final int PROFILE_PICTURE_PREFETCH_BUFFER = 5;
    private static final String SELECTION_BUNDLE_KEY = "com.facebook.android.PickerFragment.Selection";
    public static final String SHOW_PICTURES_BUNDLE_KEY = "com.facebook.widget.PickerFragment.ShowPictures";
    public static final String SHOW_TITLE_BAR_BUNDLE_KEY = "com.facebook.widget.PickerFragment.ShowTitleBar";
    public static final String TITLE_TEXT_BUNDLE_KEY = "com.facebook.widget.PickerFragment.TitleText";
    private ProgressBar activityCircle;
    GraphObjectAdapter<T> adapter;
    private Button doneButton;
    private Drawable doneButtonBackground;
    private String doneButtonText;
    private GraphObjectFilter<T> filter;
    private final Class<T> graphObjectClass;
    private final int layout;
    private ListView listView;
    private PickerFragment<T>.LoadingStrategy loadingStrategy;
    private OnDataChangedListener onDataChangedListener;
    private OnDoneButtonClickedListener onDoneButtonClickedListener;
    private OnErrorListener onErrorListener;
    private OnSelectionChangedListener onSelectionChangedListener;
    private PickerFragment<T>.SelectionStrategy selectionStrategy;
    private SessionTracker sessionTracker;
    private Drawable titleBarBackground;
    private String titleText;
    private TextView titleTextView;
    private boolean showPictures = true;
    private boolean showTitleBar = true;
    HashSet<String> extraFields = new HashSet<>();
    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() { // from class: com.facebook.widget.PickerFragment.6
        @Override // android.widget.AbsListView.OnScrollListener
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            PickerFragment.this.reprioritizeDownloads();
        }
    };

    public interface GraphObjectFilter<T> {
        boolean includeItem(T t);
    }

    public interface OnDataChangedListener {
        void onDataChanged(PickerFragment<?> pickerFragment);
    }

    public interface OnDoneButtonClickedListener {
        void onDoneButtonClicked(PickerFragment<?> pickerFragment);
    }

    public interface OnErrorListener {
        void onError(PickerFragment<?> pickerFragment, FacebookException facebookException);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(PickerFragment<?> pickerFragment);
    }

    abstract PickerFragment<T>.PickerFragmentAdapter<T> createAdapter();

    abstract PickerFragment<T>.LoadingStrategy createLoadingStrategy();

    abstract PickerFragment<T>.SelectionStrategy createSelectionStrategy();

    abstract Request getRequestForLoadData(Session session);

    PickerFragment(Class<T> graphObjectClass, int layout, Bundle args) {
        this.graphObjectClass = graphObjectClass;
        this.layout = layout;
        setPickerFragmentSettingsFromBundle(args);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$UnknownArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:593)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    @Override // android.support.v4.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.adapter = createAdapter();
        this.adapter.setFilter(new GraphObjectAdapter.Filter<T>() { // from class: com.facebook.widget.PickerFragment.1
            @Override // com.facebook.widget.GraphObjectAdapter.Filter
            public boolean includeItem(T graphObject) {
                return PickerFragment.this.filterIncludesItem(graphObject);
            }
        });
    }

    @Override // android.support.v4.app.Fragment
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.com_facebook_picker_fragment);
        setShowPictures(a.getBoolean(0, this.showPictures));
        String extraFieldsString = a.getString(1);
        if (extraFieldsString != null) {
            String[] strings = extraFieldsString.split(",");
            setExtraFields(Arrays.asList(strings));
        }
        this.showTitleBar = a.getBoolean(2, this.showTitleBar);
        this.titleText = a.getString(3);
        this.doneButtonText = a.getString(4);
        this.titleBarBackground = a.getDrawable(5);
        this.doneButtonBackground = a.getDrawable(6);
        a.recycle();
    }

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(this.layout, container, false);
        this.listView = (ListView) view.findViewById(R.id.com_facebook_picker_list_view);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.facebook.widget.PickerFragment.2
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PickerFragment.this.onListItemClick((ListView) parent, v, position);
            }
        });
        this.listView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.facebook.widget.PickerFragment.3
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View v) {
                return false;
            }
        });
        this.listView.setOnScrollListener(this.onScrollListener);
        this.listView.setAdapter((ListAdapter) this.adapter);
        this.activityCircle = (ProgressBar) view.findViewById(R.id.com_facebook_picker_activity_circle);
        return view;
    }

    @Override // android.support.v4.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.sessionTracker = new SessionTracker(getActivity(), new Session.StatusCallback() { // from class: com.facebook.widget.PickerFragment.4
            @Override // com.facebook.Session.StatusCallback
            public void call(Session session, SessionState state, Exception exception) {
                if (!session.isOpened()) {
                    PickerFragment.this.clearResults();
                }
            }
        });
        setSettingsFromBundle(savedInstanceState);
        this.loadingStrategy = createLoadingStrategy();
        this.loadingStrategy.attach(this.adapter);
        this.selectionStrategy = createSelectionStrategy();
        this.selectionStrategy.readSelectionFromBundle(savedInstanceState, SELECTION_BUNDLE_KEY);
        if (this.showTitleBar) {
            inflateTitleBar((ViewGroup) getView());
        }
        if (this.activityCircle != null && savedInstanceState != null) {
            boolean shown = savedInstanceState.getBoolean(ACTIVITY_CIRCLE_SHOW_KEY, false);
            if (shown) {
                displayActivityCircle();
            } else {
                hideActivityCircle();
            }
        }
    }

    @Override // android.support.v4.app.Fragment
    public void onDetach() {
        super.onDetach();
        this.listView.setOnScrollListener(null);
        this.listView.setAdapter((ListAdapter) null);
        this.loadingStrategy.detach();
        this.sessionTracker.stopTracking();
    }

    @Override // android.support.v4.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveSettingsToBundle(outState);
        this.selectionStrategy.saveSelectionToBundle(outState, SELECTION_BUNDLE_KEY);
        if (this.activityCircle != null) {
            outState.putBoolean(ACTIVITY_CIRCLE_SHOW_KEY, this.activityCircle.getVisibility() == 0);
        }
    }

    @Override // android.support.v4.app.Fragment
    public void setArguments(Bundle args) {
        super.setArguments(args);
        setSettingsFromBundle(args);
    }

    public OnDataChangedListener getOnDataChangedListener() {
        return this.onDataChangedListener;
    }

    public void setOnDataChangedListener(OnDataChangedListener onDataChangedListener) {
        this.onDataChangedListener = onDataChangedListener;
    }

    public OnSelectionChangedListener getOnSelectionChangedListener() {
        return this.onSelectionChangedListener;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener onSelectionChangedListener) {
        this.onSelectionChangedListener = onSelectionChangedListener;
    }

    public OnDoneButtonClickedListener getOnDoneButtonClickedListener() {
        return this.onDoneButtonClickedListener;
    }

    public void setOnDoneButtonClickedListener(OnDoneButtonClickedListener onDoneButtonClickedListener) {
        this.onDoneButtonClickedListener = onDoneButtonClickedListener;
    }

    public OnErrorListener getOnErrorListener() {
        return this.onErrorListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public GraphObjectFilter<T> getFilter() {
        return this.filter;
    }

    public void setFilter(GraphObjectFilter<T> filter) {
        this.filter = filter;
    }

    public Session getSession() {
        return this.sessionTracker.getSession();
    }

    public void setSession(Session session) {
        this.sessionTracker.setSession(session);
    }

    public boolean getShowPictures() {
        return this.showPictures;
    }

    public void setShowPictures(boolean showPictures) {
        this.showPictures = showPictures;
    }

    public Set<String> getExtraFields() {
        return new HashSet(this.extraFields);
    }

    public void setExtraFields(Collection<String> fields) {
        this.extraFields = new HashSet<>();
        if (fields != null) {
            this.extraFields.addAll(fields);
        }
    }

    public void setShowTitleBar(boolean showTitleBar) {
        this.showTitleBar = showTitleBar;
    }

    public boolean getShowTitleBar() {
        return this.showTitleBar;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getTitleText() {
        if (this.titleText == null) {
            this.titleText = getDefaultTitleText();
        }
        return this.titleText;
    }

    public void setDoneButtonText(String doneButtonText) {
        this.doneButtonText = doneButtonText;
    }

    public String getDoneButtonText() {
        if (this.doneButtonText == null) {
            this.doneButtonText = getDefaultDoneButtonText();
        }
        return this.doneButtonText;
    }

    public void loadData(boolean forceReload) {
        if (forceReload || !this.loadingStrategy.isDataPresentOrLoading()) {
            loadDataSkippingRoundTripIfCached();
        }
    }

    public void setSettingsFromBundle(Bundle inState) {
        setPickerFragmentSettingsFromBundle(inState);
    }

    boolean filterIncludesItem(T graphObject) {
        if (this.filter != null) {
            return this.filter.includeItem(graphObject);
        }
        return true;
    }

    List<T> getSelectedGraphObjects() {
        return this.adapter.getGraphObjectsById(this.selectionStrategy.getSelectedIds());
    }

    void saveSettingsToBundle(Bundle outState) {
        outState.putBoolean(SHOW_PICTURES_BUNDLE_KEY, this.showPictures);
        if (!this.extraFields.isEmpty()) {
            outState.putString(EXTRA_FIELDS_BUNDLE_KEY, TextUtils.join(",", this.extraFields));
        }
        outState.putBoolean(SHOW_TITLE_BAR_BUNDLE_KEY, this.showTitleBar);
        outState.putString(TITLE_TEXT_BUNDLE_KEY, this.titleText);
        outState.putString(DONE_BUTTON_TEXT_BUNDLE_KEY, this.doneButtonText);
    }

    void onLoadingData() {
    }

    String getDefaultTitleText() {
        return null;
    }

    String getDefaultDoneButtonText() {
        return getString(R.string.com_facebook_picker_done_button_text);
    }

    void displayActivityCircle() {
        if (this.activityCircle != null) {
            layoutActivityCircle();
            this.activityCircle.setVisibility(0);
        }
    }

    void layoutActivityCircle() {
        float alpha = !this.adapter.isEmpty() ? 0.25f : 1.0f;
        setAlpha(this.activityCircle, alpha);
    }

    void hideActivityCircle() {
        if (this.activityCircle != null) {
            this.activityCircle.clearAnimation();
            this.activityCircle.setVisibility(4);
        }
    }

    void setSelectionStrategy(PickerFragment<T>.SelectionStrategy selectionStrategy) {
        if (selectionStrategy != this.selectionStrategy) {
            this.selectionStrategy = selectionStrategy;
            if (this.adapter != null) {
                this.adapter.notifyDataSetChanged();
            }
        }
    }

    private static void setAlpha(View view, float alpha) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
        alphaAnimation.setDuration(0L);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }

    private void setPickerFragmentSettingsFromBundle(Bundle inState) {
        if (inState != null) {
            this.showPictures = inState.getBoolean(SHOW_PICTURES_BUNDLE_KEY, this.showPictures);
            String extraFieldsString = inState.getString(EXTRA_FIELDS_BUNDLE_KEY);
            if (extraFieldsString != null) {
                String[] strings = extraFieldsString.split(",");
                setExtraFields(Arrays.asList(strings));
            }
            this.showTitleBar = inState.getBoolean(SHOW_TITLE_BAR_BUNDLE_KEY, this.showTitleBar);
            String titleTextString = inState.getString(TITLE_TEXT_BUNDLE_KEY);
            if (titleTextString != null) {
                this.titleText = titleTextString;
                if (this.titleTextView != null) {
                    this.titleTextView.setText(this.titleText);
                }
            }
            String doneButtonTextString = inState.getString(DONE_BUTTON_TEXT_BUNDLE_KEY);
            if (doneButtonTextString != null) {
                this.doneButtonText = doneButtonTextString;
                if (this.doneButton != null) {
                    this.doneButton.setText(this.doneButtonText);
                }
            }
        }
    }

    private void inflateTitleBar(ViewGroup view) {
        ViewStub stub = (ViewStub) view.findViewById(R.id.com_facebook_picker_title_bar_stub);
        if (stub != null) {
            View titleBar = stub.inflate();
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
            layoutParams.addRule(3, R.id.com_facebook_picker_title_bar);
            this.listView.setLayoutParams(layoutParams);
            if (this.titleBarBackground != null) {
                titleBar.setBackgroundDrawable(this.titleBarBackground);
            }
            this.doneButton = (Button) view.findViewById(R.id.com_facebook_picker_done_button);
            if (this.doneButton != null) {
                this.doneButton.setOnClickListener(new View.OnClickListener() { // from class: com.facebook.widget.PickerFragment.5
                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        if (PickerFragment.this.onDoneButtonClickedListener != null) {
                            PickerFragment.this.onDoneButtonClickedListener.onDoneButtonClicked(PickerFragment.this);
                        }
                    }
                });
                if (getDoneButtonText() != null) {
                    this.doneButton.setText(getDoneButtonText());
                }
                if (this.doneButtonBackground != null) {
                    this.doneButton.setBackgroundDrawable(this.doneButtonBackground);
                }
            }
            this.titleTextView = (TextView) view.findViewById(R.id.com_facebook_picker_title);
            if (this.titleTextView != null && getTitleText() != null) {
                this.titleTextView.setText(getTitleText());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onListItemClick(ListView listView, View v, int position) {
        String id = this.adapter.getIdOfGraphObject((GraphObject) listView.getItemAtPosition(position));
        this.selectionStrategy.toggleSelection(id);
        this.adapter.notifyDataSetChanged();
        if (this.onSelectionChangedListener != null) {
            this.onSelectionChangedListener.onSelectionChanged(this);
        }
    }

    private void loadDataSkippingRoundTripIfCached() {
        clearResults();
        Request request = getRequestForLoadData(getSession());
        if (request != null) {
            onLoadingData();
            this.loadingStrategy.startLoading(request);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearResults() {
        if (this.adapter != null) {
            boolean wasSelection = !this.selectionStrategy.isEmpty();
            boolean wasData = !this.adapter.isEmpty();
            this.loadingStrategy.clearResults();
            this.selectionStrategy.clear();
            this.adapter.notifyDataSetChanged();
            if (wasData && this.onDataChangedListener != null) {
                this.onDataChangedListener.onDataChanged(this);
            }
            if (wasSelection && this.onSelectionChangedListener != null) {
                this.onSelectionChangedListener.onSelectionChanged(this);
            }
        }
    }

    void updateAdapter(SimpleGraphObjectCursor<T> data) {
        int newPositionOfItem;
        if (this.adapter != null) {
            View view = this.listView.getChildAt(1);
            int anchorPosition = this.listView.getFirstVisiblePosition();
            if (anchorPosition > 0) {
                anchorPosition++;
            }
            GraphObjectAdapter.SectionAndItem<T> anchorItem = this.adapter.getSectionAndItem(anchorPosition);
            int top = (view == null || anchorItem.getType() == GraphObjectAdapter.SectionAndItem.Type.ACTIVITY_CIRCLE) ? 0 : view.getTop();
            boolean dataChanged = this.adapter.changeCursor(data);
            if (view != null && anchorItem != null && (newPositionOfItem = this.adapter.getPosition(anchorItem.sectionKey, anchorItem.graphObject)) != -1) {
                this.listView.setSelectionFromTop(newPositionOfItem, top);
            }
            if (dataChanged && this.onDataChangedListener != null) {
                this.onDataChangedListener.onDataChanged(this);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reprioritizeDownloads() {
        int lastVisibleItem = this.listView.getLastVisiblePosition();
        if (lastVisibleItem >= 0) {
            int firstVisibleItem = this.listView.getFirstVisiblePosition();
            this.adapter.prioritizeViewRange(firstVisibleItem, lastVisibleItem, 5);
        }
    }

    abstract class LoadingStrategy {
        protected static final int CACHED_RESULT_REFRESH_DELAY = 2000;
        protected GraphObjectAdapter<T> adapter;
        protected GraphObjectPagingLoader<T> loader;

        LoadingStrategy() {
        }

        public void attach(GraphObjectAdapter<T> adapter) {
            this.loader = (GraphObjectPagingLoader) PickerFragment.this.getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<SimpleGraphObjectCursor<T>>() { // from class: com.facebook.widget.PickerFragment.LoadingStrategy.1
                @Override // android.support.v4.app.LoaderManager.LoaderCallbacks
                public Loader<SimpleGraphObjectCursor<T>> onCreateLoader(int id, Bundle args) {
                    return LoadingStrategy.this.onCreateLoader();
                }

                @Override // android.support.v4.app.LoaderManager.LoaderCallbacks
                public void onLoadFinished(Loader<SimpleGraphObjectCursor<T>> loader, SimpleGraphObjectCursor<T> data) {
                    if (loader != LoadingStrategy.this.loader) {
                        throw new FacebookException("Received callback for unknown loader.");
                    }
                    LoadingStrategy.this.onLoadFinished((GraphObjectPagingLoader) loader, data);
                }

                @Override // android.support.v4.app.LoaderManager.LoaderCallbacks
                public void onLoaderReset(Loader<SimpleGraphObjectCursor<T>> loader) {
                    if (loader != LoadingStrategy.this.loader) {
                        throw new FacebookException("Received callback for unknown loader.");
                    }
                    LoadingStrategy.this.onLoadReset((GraphObjectPagingLoader) loader);
                }
            });
            this.loader.setOnErrorListener(new GraphObjectPagingLoader.OnErrorListener() { // from class: com.facebook.widget.PickerFragment.LoadingStrategy.2
                @Override // com.facebook.widget.GraphObjectPagingLoader.OnErrorListener
                public void onError(FacebookException error, GraphObjectPagingLoader<?> loader) {
                    PickerFragment.this.hideActivityCircle();
                    if (PickerFragment.this.onErrorListener != null) {
                        PickerFragment.this.onErrorListener.onError(PickerFragment.this, error);
                    }
                }
            });
            this.adapter = adapter;
            this.adapter.changeCursor(this.loader.getCursor());
            this.adapter.setOnErrorListener(new GraphObjectAdapter.OnErrorListener() { // from class: com.facebook.widget.PickerFragment.LoadingStrategy.3
                @Override // com.facebook.widget.GraphObjectAdapter.OnErrorListener
                public void onError(GraphObjectAdapter<?> adapter2, FacebookException error) {
                    if (PickerFragment.this.onErrorListener != null) {
                        PickerFragment.this.onErrorListener.onError(PickerFragment.this, error);
                    }
                }
            });
        }

        public void detach() {
            this.adapter.setDataNeededListener(null);
            this.adapter.setOnErrorListener(null);
            this.loader.setOnErrorListener(null);
            this.loader = null;
            this.adapter = null;
        }

        public void clearResults() {
            if (this.loader != null) {
                this.loader.clearResults();
            }
        }

        public void startLoading(Request request) {
            if (this.loader != null) {
                this.loader.startLoading(request, true);
                onStartLoading(this.loader, request);
            }
        }

        public boolean isDataPresentOrLoading() {
            return !this.adapter.isEmpty() || this.loader.isLoading();
        }

        protected GraphObjectPagingLoader<T> onCreateLoader() {
            return new GraphObjectPagingLoader<>(PickerFragment.this.getActivity(), PickerFragment.this.graphObjectClass);
        }

        protected void onStartLoading(GraphObjectPagingLoader<T> loader, Request request) {
            PickerFragment.this.displayActivityCircle();
        }

        protected void onLoadReset(GraphObjectPagingLoader<T> loader) {
            this.adapter.changeCursor(null);
        }

        protected void onLoadFinished(GraphObjectPagingLoader<T> loader, SimpleGraphObjectCursor<T> data) {
            PickerFragment.this.updateAdapter(data);
        }
    }

    abstract class SelectionStrategy {
        abstract void clear();

        abstract Collection<String> getSelectedIds();

        abstract boolean isEmpty();

        abstract boolean isSelected(String str);

        abstract void readSelectionFromBundle(Bundle bundle, String str);

        abstract void saveSelectionToBundle(Bundle bundle, String str);

        abstract boolean shouldShowCheckBoxIfUnselected();

        abstract void toggleSelection(String str);

        SelectionStrategy() {
        }
    }

    class SingleSelectionStrategy extends SelectionStrategy {
        private String selectedId;

        SingleSelectionStrategy() {
            super();
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        public Collection<String> getSelectedIds() {
            return Arrays.asList(this.selectedId);
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        boolean isSelected(String id) {
            return (this.selectedId == null || id == null || !this.selectedId.equals(id)) ? false : true;
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        void toggleSelection(String id) {
            if (this.selectedId != null && this.selectedId.equals(id)) {
                this.selectedId = null;
            } else {
                this.selectedId = id;
            }
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        void saveSelectionToBundle(Bundle outBundle, String key) {
            if (!TextUtils.isEmpty(this.selectedId)) {
                outBundle.putString(key, this.selectedId);
            }
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        void readSelectionFromBundle(Bundle inBundle, String key) {
            if (inBundle != null) {
                this.selectedId = inBundle.getString(key);
            }
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        public void clear() {
            this.selectedId = null;
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        boolean isEmpty() {
            return this.selectedId == null;
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        boolean shouldShowCheckBoxIfUnselected() {
            return false;
        }
    }

    class MultiSelectionStrategy extends SelectionStrategy {
        private Set<String> selectedIds;

        MultiSelectionStrategy() {
            super();
            this.selectedIds = new HashSet();
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        public Collection<String> getSelectedIds() {
            return this.selectedIds;
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        boolean isSelected(String id) {
            return id != null && this.selectedIds.contains(id);
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        void toggleSelection(String id) {
            if (id != null) {
                if (this.selectedIds.contains(id)) {
                    this.selectedIds.remove(id);
                } else {
                    this.selectedIds.add(id);
                }
            }
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        void saveSelectionToBundle(Bundle outBundle, String key) {
            if (!this.selectedIds.isEmpty()) {
                String ids = TextUtils.join(",", this.selectedIds);
                outBundle.putString(key, ids);
            }
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        void readSelectionFromBundle(Bundle inBundle, String key) {
            String ids;
            if (inBundle != null && (ids = inBundle.getString(key)) != null) {
                String[] splitIds = TextUtils.split(ids, ",");
                this.selectedIds.clear();
                Collections.addAll(this.selectedIds, splitIds);
            }
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        public void clear() {
            this.selectedIds.clear();
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        boolean isEmpty() {
            return this.selectedIds.isEmpty();
        }

        @Override // com.facebook.widget.PickerFragment.SelectionStrategy
        boolean shouldShowCheckBoxIfUnselected() {
            return true;
        }
    }

    abstract class PickerFragmentAdapter<U extends GraphObject> extends GraphObjectAdapter<T> {
        public PickerFragmentAdapter(Context context) {
            super(context);
        }

        @Override // com.facebook.widget.GraphObjectAdapter
        boolean isGraphObjectSelected(String graphObjectId) {
            return PickerFragment.this.selectionStrategy.isSelected(graphObjectId);
        }

        @Override // com.facebook.widget.GraphObjectAdapter
        void updateCheckboxState(CheckBox checkBox, boolean graphObjectSelected) {
            checkBox.setChecked(graphObjectSelected);
            int visible = (graphObjectSelected || PickerFragment.this.selectionStrategy.shouldShowCheckBoxIfUnselected()) ? 0 : 8;
            checkBox.setVisibility(visible);
        }
    }
}
