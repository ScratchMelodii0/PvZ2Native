package com.facebook.model;

import java.util.List;
import org.json.JSONArray;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface GraphObjectList<T> extends List<T> {
    <U extends GraphObject> GraphObjectList<U> castToListOf(Class<U> cls);

    JSONArray getInnerJSONArray();
}
