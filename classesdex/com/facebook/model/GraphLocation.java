package com.facebook.model;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface GraphLocation extends GraphObject {
    String getCity();

    String getCountry();

    double getLatitude();

    double getLongitude();

    String getState();

    String getStreet();

    String getZip();

    void setCity(String str);

    void setCountry(String str);

    void setLatitude(double d);

    void setLongitude(double d);

    void setState(String str);

    void setStreet(String str);

    void setZip(String str);
}
