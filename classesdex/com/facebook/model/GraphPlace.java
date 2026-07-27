package com.facebook.model;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface GraphPlace extends GraphObject {
    String getCategory();

    String getId();

    GraphLocation getLocation();

    String getName();

    void setCategory(String str);

    void setId(String str);

    void setLocation(GraphLocation graphLocation);

    void setName(String str);
}
