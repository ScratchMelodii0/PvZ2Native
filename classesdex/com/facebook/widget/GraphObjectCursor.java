package com.facebook.widget;

import com.facebook.model.GraphObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
interface GraphObjectCursor<T extends GraphObject> {
    boolean areMoreObjectsAvailable();

    void close();

    int getCount();

    T getGraphObject();

    int getPosition();

    boolean isAfterLast();

    boolean isBeforeFirst();

    boolean isClosed();

    boolean isFirst();

    boolean isFromCache();

    boolean isLast();

    boolean move(int i);

    boolean moveToFirst();

    boolean moveToLast();

    boolean moveToNext();

    boolean moveToPosition(int i);

    boolean moveToPrevious();
}
