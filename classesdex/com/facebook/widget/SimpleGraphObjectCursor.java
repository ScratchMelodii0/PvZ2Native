package com.facebook.widget;

import android.database.CursorIndexOutOfBoundsException;
import com.facebook.model.GraphObject;
import java.util.ArrayList;
import java.util.Collection;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class SimpleGraphObjectCursor<T extends GraphObject> implements GraphObjectCursor<T> {
    private boolean closed;
    private boolean fromCache;
    private ArrayList<T> graphObjects;
    private boolean moreObjectsAvailable;
    private int pos;

    SimpleGraphObjectCursor() {
        this.pos = -1;
        this.closed = false;
        this.graphObjects = new ArrayList<>();
        this.moreObjectsAvailable = false;
        this.fromCache = false;
    }

    SimpleGraphObjectCursor(SimpleGraphObjectCursor<T> other) {
        this.pos = -1;
        this.closed = false;
        this.graphObjects = new ArrayList<>();
        this.moreObjectsAvailable = false;
        this.fromCache = false;
        this.pos = other.pos;
        this.closed = other.closed;
        this.graphObjects = new ArrayList<>();
        this.graphObjects.addAll(other.graphObjects);
        this.fromCache = other.fromCache;
    }

    public void addGraphObjects(Collection<T> graphObjects, boolean fromCache) {
        this.graphObjects.addAll(graphObjects);
        this.fromCache |= fromCache;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean isFromCache() {
        return this.fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean areMoreObjectsAvailable() {
        return this.moreObjectsAvailable;
    }

    public void setMoreObjectsAvailable(boolean moreObjectsAvailable) {
        this.moreObjectsAvailable = moreObjectsAvailable;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public int getCount() {
        return this.graphObjects.size();
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public int getPosition() {
        return this.pos;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean move(int offset) {
        return moveToPosition(this.pos + offset);
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean moveToPosition(int position) {
        int count = getCount();
        if (position >= count) {
            this.pos = count;
            return false;
        }
        if (position < 0) {
            this.pos = -1;
            return false;
        }
        this.pos = position;
        return true;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean moveToNext() {
        return moveToPosition(this.pos + 1);
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean moveToPrevious() {
        return moveToPosition(this.pos - 1);
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean isFirst() {
        return this.pos == 0 && getCount() != 0;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean isLast() {
        int count = getCount();
        return this.pos == count + (-1) && count != 0;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean isBeforeFirst() {
        return getCount() == 0 || this.pos == -1;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean isAfterLast() {
        int count = getCount();
        return count == 0 || this.pos == count;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public T getGraphObject() {
        if (this.pos < 0) {
            throw new CursorIndexOutOfBoundsException("Before first object.");
        }
        if (this.pos >= this.graphObjects.size()) {
            throw new CursorIndexOutOfBoundsException("After last object.");
        }
        return this.graphObjects.get(this.pos);
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public void close() {
        this.closed = true;
    }

    @Override // com.facebook.widget.GraphObjectCursor
    public boolean isClosed() {
        return this.closed;
    }
}
