package com.facebook.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.facebook.FacebookException;
import com.facebook.android.R;
import com.facebook.model.GraphObject;
import com.facebook.widget.ImageRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class GraphObjectAdapter<T extends GraphObject> extends BaseAdapter implements SectionIndexer {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final int ACTIVITY_CIRCLE_VIEW_TYPE = 2;
    private static final int DISPLAY_SECTIONS_THRESHOLD = 1;
    private static final int GRAPH_OBJECT_VIEW_TYPE = 1;
    private static final int HEADER_VIEW_TYPE = 0;
    private static final String ID = "id";
    private static final int MAX_PREFETCHED_PICTURES = 20;
    private static final String NAME = "name";
    private static final String PICTURE = "picture";
    private Context context;
    private GraphObjectCursor<T> cursor;
    private DataNeededListener dataNeededListener;
    private boolean displaySections;
    private Filter<T> filter;
    private String groupByField;
    private final LayoutInflater inflater;
    private OnErrorListener onErrorListener;
    private boolean showCheckbox;
    private boolean showPicture;
    private List<String> sortFields;
    private final Map<String, ImageRequest> pendingRequests = new HashMap();
    private List<String> sectionKeys = new ArrayList();
    private Map<String, ArrayList<T>> graphObjectsBySection = new HashMap();
    private Map<String, T> graphObjectsById = new HashMap();
    private Map<String, ImageResponse> prefetchedPictureCache = new HashMap();
    private ArrayList<String> prefetchedProfilePictureIds = new ArrayList<>();

    public interface DataNeededListener {
        void onDataNeeded();
    }

    interface Filter<T> {
        boolean includeItem(T t);
    }

    private interface ItemPicture extends GraphObject {
        ItemPictureData getData();
    }

    private interface ItemPictureData extends GraphObject {
        String getUrl();
    }

    public interface OnErrorListener {
        void onError(GraphObjectAdapter<?> graphObjectAdapter, FacebookException facebookException);
    }

    static {
        $assertionsDisabled = !GraphObjectAdapter.class.desiredAssertionStatus();
    }

    public static class SectionAndItem<T extends GraphObject> {
        public T graphObject;
        public String sectionKey;

        public enum Type {
            GRAPH_OBJECT,
            SECTION_HEADER,
            ACTIVITY_CIRCLE
        }

        public SectionAndItem(String sectionKey, T graphObject) {
            this.sectionKey = sectionKey;
            this.graphObject = graphObject;
        }

        public Type getType() {
            if (this.sectionKey == null) {
                return Type.ACTIVITY_CIRCLE;
            }
            if (this.graphObject == null) {
                return Type.SECTION_HEADER;
            }
            return Type.GRAPH_OBJECT;
        }
    }

    public GraphObjectAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public List<String> getSortFields() {
        return this.sortFields;
    }

    public void setSortFields(List<String> sortFields) {
        this.sortFields = sortFields;
    }

    public String getGroupByField() {
        return this.groupByField;
    }

    public void setGroupByField(String groupByField) {
        this.groupByField = groupByField;
    }

    public boolean getShowPicture() {
        return this.showPicture;
    }

    public void setShowPicture(boolean showPicture) {
        this.showPicture = showPicture;
    }

    public boolean getShowCheckbox() {
        return this.showCheckbox;
    }

    public void setShowCheckbox(boolean showCheckbox) {
        this.showCheckbox = showCheckbox;
    }

    public DataNeededListener getDataNeededListener() {
        return this.dataNeededListener;
    }

    public void setDataNeededListener(DataNeededListener dataNeededListener) {
        this.dataNeededListener = dataNeededListener;
    }

    public OnErrorListener getOnErrorListener() {
        return this.onErrorListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public GraphObjectCursor<T> getCursor() {
        return this.cursor;
    }

    public boolean changeCursor(GraphObjectCursor<T> cursor) {
        if (this.cursor == cursor) {
            return false;
        }
        if (this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = cursor;
        rebuildAndNotify();
        return true;
    }

    public void rebuildAndNotify() {
        rebuildSections();
        notifyDataSetChanged();
    }

    public void prioritizeViewRange(int firstVisibleItem, int lastVisibleItem, int prefetchBuffer) {
        if (lastVisibleItem >= firstVisibleItem) {
            for (int i = lastVisibleItem; i >= 0; i--) {
                SectionAndItem<T> sectionAndItem = getSectionAndItem(i);
                if (sectionAndItem.graphObject != null) {
                    ImageRequest request = this.pendingRequests.get(getIdOfGraphObject(sectionAndItem.graphObject));
                    if (request != null) {
                        ImageDownloader.prioritizeRequest(request);
                    }
                }
            }
            int start = Math.max(0, firstVisibleItem - prefetchBuffer);
            int end = Math.min(lastVisibleItem + prefetchBuffer, getCount() - 1);
            ArrayList<T> graphObjectsToPrefetchPicturesFor = new ArrayList<>();
            for (int i2 = start; i2 < firstVisibleItem; i2++) {
                SectionAndItem<T> sectionAndItem2 = getSectionAndItem(i2);
                if (sectionAndItem2.graphObject != null) {
                    graphObjectsToPrefetchPicturesFor.add(sectionAndItem2.graphObject);
                }
            }
            for (int i3 = lastVisibleItem + 1; i3 <= end; i3++) {
                SectionAndItem<T> sectionAndItem3 = getSectionAndItem(i3);
                if (sectionAndItem3.graphObject != null) {
                    graphObjectsToPrefetchPicturesFor.add(sectionAndItem3.graphObject);
                }
            }
            for (T graphObject : graphObjectsToPrefetchPicturesFor) {
                URL url = getPictureUrlOfGraphObject(graphObject);
                String id = getIdOfGraphObject(graphObject);
                boolean alreadyPrefetching = this.prefetchedProfilePictureIds.remove(id);
                this.prefetchedProfilePictureIds.add(id);
                if (!alreadyPrefetching) {
                    downloadProfilePicture(id, url, null);
                }
            }
        }
    }

    protected String getSectionKeyOfGraphObject(T graphObject) {
        String result = null;
        if (this.groupByField != null && (result = (String) graphObject.getProperty(this.groupByField)) != null && result.length() > 0) {
            result = result.substring(0, 1).toUpperCase();
        }
        return result != null ? result : "";
    }

    protected CharSequence getTitleOfGraphObject(T graphObject) {
        return (String) graphObject.getProperty(NAME);
    }

    protected CharSequence getSubTitleOfGraphObject(T graphObject) {
        return null;
    }

    protected URL getPictureUrlOfGraphObject(T graphObject) {
        String url = null;
        Object o = graphObject.getProperty(PICTURE);
        if (o instanceof String) {
            url = (String) o;
        } else if (o instanceof JSONObject) {
            ItemPicture itemPicture = (ItemPicture) GraphObject.Factory.create((JSONObject) o).cast(ItemPicture.class);
            ItemPictureData data = itemPicture.getData();
            if (data != null) {
                url = data.getUrl();
            }
        }
        if (url != null) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
            }
        }
        return null;
    }

    protected View getSectionHeaderView(String sectionHeader, View convertView, ViewGroup parent) {
        TextView result = (TextView) convertView;
        if (result == null) {
            result = (TextView) this.inflater.inflate(R.layout.com_facebook_picker_list_section_header, (ViewGroup) null);
        }
        result.setText(sectionHeader);
        return result;
    }

    protected View getGraphObjectView(T graphObject, View convertView, ViewGroup parent) {
        View result = convertView;
        if (result == null) {
            result = createGraphObjectView(graphObject, convertView);
        }
        populateGraphObjectView(result, graphObject);
        return result;
    }

    private View getActivityCircleView(View convertView, ViewGroup parent) {
        View result = convertView;
        if (result == null) {
            result = this.inflater.inflate(R.layout.com_facebook_picker_activity_circle_row, (ViewGroup) null);
        }
        ProgressBar activityCircle = (ProgressBar) result.findViewById(R.id.com_facebook_picker_row_activity_circle);
        activityCircle.setVisibility(0);
        return result;
    }

    protected int getGraphObjectRowLayoutId(T graphObject) {
        return R.layout.com_facebook_picker_list_row;
    }

    protected int getDefaultPicture() {
        return R.drawable.com_facebook_profile_default_icon;
    }

    protected View createGraphObjectView(T graphObject, View convertView) {
        View result = this.inflater.inflate(getGraphObjectRowLayoutId(graphObject), (ViewGroup) null);
        ViewStub checkboxStub = (ViewStub) result.findViewById(R.id.com_facebook_picker_checkbox_stub);
        if (checkboxStub != null) {
            if (!getShowCheckbox()) {
                checkboxStub.setVisibility(8);
            } else {
                CheckBox checkBox = (CheckBox) checkboxStub.inflate();
                updateCheckboxState(checkBox, false);
            }
        }
        ViewStub profilePicStub = (ViewStub) result.findViewById(R.id.com_facebook_picker_profile_pic_stub);
        if (!getShowPicture()) {
            profilePicStub.setVisibility(8);
        } else {
            ImageView imageView = (ImageView) profilePicStub.inflate();
            imageView.setVisibility(0);
        }
        return result;
    }

    protected void populateGraphObjectView(View view, T graphObject) {
        URL pictureURL;
        String id = getIdOfGraphObject(graphObject);
        view.setTag(id);
        CharSequence title = getTitleOfGraphObject(graphObject);
        TextView titleView = (TextView) view.findViewById(R.id.com_facebook_picker_title);
        if (titleView != null) {
            titleView.setText(title, TextView.BufferType.SPANNABLE);
        }
        CharSequence subtitle = getSubTitleOfGraphObject(graphObject);
        TextView subtitleView = (TextView) view.findViewById(R.id.picker_subtitle);
        if (subtitleView != null) {
            if (subtitle != null) {
                subtitleView.setText(subtitle, TextView.BufferType.SPANNABLE);
                subtitleView.setVisibility(0);
            } else {
                subtitleView.setVisibility(8);
            }
        }
        if (getShowCheckbox()) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.com_facebook_picker_checkbox);
            updateCheckboxState(checkBox, isGraphObjectSelected(id));
        }
        if (getShowPicture() && (pictureURL = getPictureUrlOfGraphObject(graphObject)) != null) {
            ImageView profilePic = (ImageView) view.findViewById(R.id.com_facebook_picker_image);
            if (this.prefetchedPictureCache.containsKey(id)) {
                ImageResponse response = this.prefetchedPictureCache.get(id);
                profilePic.setImageBitmap(response.getBitmap());
                profilePic.setTag(response.getRequest().getImageUrl());
                return;
            }
            downloadProfilePicture(id, pictureURL, profilePic);
        }
    }

    String getIdOfGraphObject(T graphObject) {
        if (graphObject.asMap().containsKey(ID)) {
            Object obj = graphObject.getProperty(ID);
            if (obj instanceof String) {
                return (String) obj;
            }
        }
        throw new FacebookException("Received an object without an ID.");
    }

    boolean filterIncludesItem(T graphObject) {
        return this.filter == null || this.filter.includeItem(graphObject);
    }

    Filter<T> getFilter() {
        return this.filter;
    }

    void setFilter(Filter<T> filter) {
        this.filter = filter;
    }

    boolean isGraphObjectSelected(String graphObjectId) {
        return false;
    }

    void updateCheckboxState(CheckBox checkBox, boolean graphObjectSelected) {
    }

    String getPictureFieldSpecifier() {
        View view = createGraphObjectView(null, null);
        ImageView picture = (ImageView) view.findViewById(R.id.com_facebook_picker_image);
        if (picture == null) {
            return null;
        }
        ViewGroup.LayoutParams layoutParams = picture.getLayoutParams();
        return String.format("picture.height(%d).width(%d)", Integer.valueOf(layoutParams.height), Integer.valueOf(layoutParams.width));
    }

    private boolean shouldShowActivityCircleCell() {
        return (this.cursor == null || !this.cursor.areMoreObjectsAvailable() || this.dataNeededListener == null || isEmpty()) ? false : true;
    }

    private void rebuildSections() {
        this.sectionKeys = new ArrayList();
        this.graphObjectsBySection = new HashMap();
        this.graphObjectsById = new HashMap();
        this.displaySections = false;
        if (this.cursor != null && this.cursor.getCount() != 0) {
            int i = 0;
            this.cursor.moveToFirst();
            do {
                GraphObject graphObject = this.cursor.getGraphObject();
                if (filterIncludesItem(graphObject)) {
                    i++;
                    String sectionKeyOfGraphObject = getSectionKeyOfGraphObject(graphObject);
                    if (!this.graphObjectsBySection.containsKey(sectionKeyOfGraphObject)) {
                        this.sectionKeys.add(sectionKeyOfGraphObject);
                        this.graphObjectsBySection.put(sectionKeyOfGraphObject, new ArrayList<>());
                    }
                    this.graphObjectsBySection.get(sectionKeyOfGraphObject).add(graphObject);
                    this.graphObjectsById.put(getIdOfGraphObject(graphObject), (T) graphObject);
                }
            } while (this.cursor.moveToNext());
            if (this.sortFields != null) {
                final Collator collator = Collator.getInstance();
                Iterator<ArrayList<T>> it = this.graphObjectsBySection.values().iterator();
                while (it.hasNext()) {
                    Collections.sort(it.next(), new Comparator<GraphObject>() { // from class: com.facebook.widget.GraphObjectAdapter.1
                        @Override // java.util.Comparator
                        public int compare(GraphObject a, GraphObject b) {
                            return GraphObjectAdapter.compareGraphObjects(a, b, GraphObjectAdapter.this.sortFields, collator);
                        }
                    });
                }
            }
            Collections.sort(this.sectionKeys, Collator.getInstance());
            this.displaySections = this.sectionKeys.size() > 1 && i > 1;
        }
    }

    SectionAndItem<T> getSectionAndItem(int position) {
        if (this.sectionKeys.size() == 0) {
            return null;
        }
        String sectionKey = null;
        T graphObject = null;
        if (!this.displaySections) {
            String sectionKey2 = this.sectionKeys.get(0);
            sectionKey = sectionKey2;
            List<T> section = this.graphObjectsBySection.get(sectionKey);
            if (position >= 0 && position < section.size()) {
                graphObject = this.graphObjectsBySection.get(sectionKey).get(position);
            } else {
                if ($assertionsDisabled || (this.dataNeededListener != null && this.cursor.areMoreObjectsAvailable())) {
                    return new SectionAndItem<>(null, null);
                }
                throw new AssertionError();
            }
        } else {
            Iterator<String> it = this.sectionKeys.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String key = it.next();
                int position2 = position - 1;
                if (position == 0) {
                    sectionKey = key;
                    break;
                }
                List<T> section2 = this.graphObjectsBySection.get(key);
                if (position2 < section2.size()) {
                    sectionKey = key;
                    graphObject = section2.get(position2);
                    break;
                }
                position = position2 - section2.size();
            }
        }
        if (sectionKey != null) {
            return new SectionAndItem<>(sectionKey, graphObject);
        }
        throw new IndexOutOfBoundsException("position");
    }

    int getPosition(String sectionKey, T graphObject) {
        int position = 0;
        boolean found = false;
        Iterator<String> it = this.sectionKeys.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String key = it.next();
            if (this.displaySections) {
                position++;
            }
            if (key.equals(sectionKey)) {
                found = true;
                break;
            }
            position += this.graphObjectsBySection.get(key).size();
        }
        if (!found) {
            return -1;
        }
        if (graphObject == null) {
            return position - (this.displaySections ? 1 : 0);
        }
        for (T t : this.graphObjectsBySection.get(sectionKey)) {
            if (GraphObject.Factory.hasSameId(t, graphObject)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean isEmpty() {
        return this.sectionKeys.size() == 0;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        if (this.sectionKeys.size() == 0) {
            return 0;
        }
        int count = this.displaySections ? this.sectionKeys.size() : 0;
        for (List<T> section : this.graphObjectsBySection.values()) {
            count += section.size();
        }
        if (shouldShowActivityCircleCell()) {
            return count + 1;
        }
        return count;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return this.displaySections;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return true;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int position) {
        SectionAndItem<T> sectionAndItem = getSectionAndItem(position);
        return sectionAndItem.getType() == SectionAndItem.Type.GRAPH_OBJECT;
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        SectionAndItem<T> sectionAndItem = getSectionAndItem(position);
        if (sectionAndItem.getType() == SectionAndItem.Type.GRAPH_OBJECT) {
            return sectionAndItem.graphObject;
        }
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        String id;
        SectionAndItem<T> sectionAndItem = getSectionAndItem(position);
        if (sectionAndItem == null || sectionAndItem.graphObject == null || (id = getIdOfGraphObject(sectionAndItem.graphObject)) == null) {
            return 0L;
        }
        return Long.parseLong(id);
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getViewTypeCount() {
        return 3;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getItemViewType(int position) {
        getSectionAndItem(position);
        switch (sectionAndItem.getType()) {
            case SECTION_HEADER:
                return 0;
            case GRAPH_OBJECT:
                return 1;
            case ACTIVITY_CIRCLE:
                return 2;
            default:
                throw new FacebookException("Unexpected type of section and item.");
        }
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        SectionAndItem<T> sectionAndItem = getSectionAndItem(position);
        switch (sectionAndItem.getType()) {
            case SECTION_HEADER:
                return getSectionHeaderView(sectionAndItem.sectionKey, convertView, parent);
            case GRAPH_OBJECT:
                return getGraphObjectView(sectionAndItem.graphObject, convertView, parent);
            case ACTIVITY_CIRCLE:
                if (!$assertionsDisabled && (!this.cursor.areMoreObjectsAvailable() || this.dataNeededListener == null)) {
                    throw new AssertionError();
                }
                this.dataNeededListener.onDataNeeded();
                return getActivityCircleView(convertView, parent);
            default:
                throw new FacebookException("Unexpected type of section and item.");
        }
    }

    @Override // android.widget.SectionIndexer
    public Object[] getSections() {
        return this.displaySections ? this.sectionKeys.toArray() : new Object[0];
    }

    @Override // android.widget.SectionIndexer
    public int getPositionForSection(int section) {
        int section2;
        if (!this.displaySections || (section2 = Math.max(0, Math.min(section, this.sectionKeys.size() - 1))) >= this.sectionKeys.size()) {
            return 0;
        }
        return getPosition(this.sectionKeys.get(section2), null);
    }

    @Override // android.widget.SectionIndexer
    public int getSectionForPosition(int position) {
        SectionAndItem<T> sectionAndItem = getSectionAndItem(position);
        if (sectionAndItem == null || sectionAndItem.getType() == SectionAndItem.Type.ACTIVITY_CIRCLE) {
            return 0;
        }
        return Math.max(0, Math.min(this.sectionKeys.indexOf(sectionAndItem.sectionKey), this.sectionKeys.size() - 1));
    }

    public List<T> getGraphObjectsById(Collection<String> ids) {
        Set<String> idSet = new HashSet<>();
        idSet.addAll(ids);
        ArrayList<T> result = new ArrayList<>(idSet.size());
        for (String id : idSet) {
            T graphObject = this.graphObjectsById.get(id);
            if (graphObject != null) {
                result.add(graphObject);
            }
        }
        return result;
    }

    private void downloadProfilePicture(final String profileId, URL pictureURL, final ImageView imageView) {
        if (pictureURL != null) {
            boolean prefetching = imageView == null;
            if (prefetching || !pictureURL.equals(imageView.getTag())) {
                if (!prefetching) {
                    imageView.setTag(profileId);
                    imageView.setImageResource(getDefaultPicture());
                }
                ImageRequest.Builder builder = new ImageRequest.Builder(this.context.getApplicationContext(), pictureURL).setCallerTag(this).setCallback(new ImageRequest.Callback() { // from class: com.facebook.widget.GraphObjectAdapter.2
                    @Override // com.facebook.widget.ImageRequest.Callback
                    public void onCompleted(ImageResponse response) {
                        GraphObjectAdapter.this.processImageResponse(response, profileId, imageView);
                    }
                });
                ImageRequest newRequest = builder.build();
                this.pendingRequests.put(profileId, newRequest);
                ImageDownloader.downloadAsync(newRequest);
            }
        }
    }

    private void callOnErrorListener(Exception exception) {
        if (this.onErrorListener != null) {
            if (!(exception instanceof FacebookException)) {
                exception = new FacebookException(exception);
            }
            this.onErrorListener.onError(this, (FacebookException) exception);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processImageResponse(ImageResponse response, String graphObjectId, ImageView imageView) {
        this.pendingRequests.remove(graphObjectId);
        if (response.getError() != null) {
            callOnErrorListener(response.getError());
        }
        if (imageView == null) {
            if (response.getBitmap() != null) {
                if (this.prefetchedPictureCache.size() >= MAX_PREFETCHED_PICTURES) {
                    String oldestId = this.prefetchedProfilePictureIds.remove(0);
                    this.prefetchedPictureCache.remove(oldestId);
                }
                this.prefetchedPictureCache.put(graphObjectId, response);
                return;
            }
            return;
        }
        if (imageView != null && graphObjectId.equals(imageView.getTag())) {
            Exception error = response.getError();
            Bitmap bitmap = response.getBitmap();
            if (error == null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(response.getRequest().getImageUrl());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int compareGraphObjects(GraphObject a, GraphObject b, Collection<String> sortFields, Collator collator) {
        for (String sortField : sortFields) {
            String sa = (String) a.getProperty(sortField);
            String sb = (String) b.getProperty(sortField);
            if (sa != null && sb != null) {
                int result = collator.compare(sa, sb);
                if (result != 0) {
                    return result;
                }
            } else if (sa != null || sb != null) {
                return sa == null ? -1 : 1;
            }
        }
        return 0;
    }
}
