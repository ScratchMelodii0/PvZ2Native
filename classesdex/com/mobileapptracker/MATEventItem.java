package com.mobileapptracker;

import java.util.HashMap;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class MATEventItem {
    public String attribute_sub1;
    public String attribute_sub2;
    public String attribute_sub3;
    public String attribute_sub4;
    public String attribute_sub5;
    public String itemname;
    public int quantity;
    public double revenue;
    public double unitPrice;

    public MATEventItem(String itemname, int quantity, double unitPrice, double revenue) {
        this.itemname = null;
        this.quantity = 0;
        this.unitPrice = 0.0d;
        this.revenue = 0.0d;
        this.attribute_sub1 = null;
        this.attribute_sub2 = null;
        this.attribute_sub3 = null;
        this.attribute_sub4 = null;
        this.attribute_sub5 = null;
        this.itemname = itemname;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.revenue = revenue;
    }

    public MATEventItem(String itemname, int quantity, double unitPrice, double revenue, String att1, String att2, String att3, String att4, String att5) {
        this.itemname = null;
        this.quantity = 0;
        this.unitPrice = 0.0d;
        this.revenue = 0.0d;
        this.attribute_sub1 = null;
        this.attribute_sub2 = null;
        this.attribute_sub3 = null;
        this.attribute_sub4 = null;
        this.attribute_sub5 = null;
        this.itemname = itemname;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.revenue = revenue;
        this.attribute_sub1 = att1;
        this.attribute_sub2 = att2;
        this.attribute_sub3 = att3;
        this.attribute_sub4 = att4;
        this.attribute_sub5 = att5;
    }

    public MATEventItem(String itemname, String att1, String att2, String att3, String att4, String att5) {
        this.itemname = null;
        this.quantity = 0;
        this.unitPrice = 0.0d;
        this.revenue = 0.0d;
        this.attribute_sub1 = null;
        this.attribute_sub2 = null;
        this.attribute_sub3 = null;
        this.attribute_sub4 = null;
        this.attribute_sub5 = null;
        this.itemname = itemname;
        this.attribute_sub1 = att1;
        this.attribute_sub2 = att2;
        this.attribute_sub3 = att3;
        this.attribute_sub4 = att4;
        this.attribute_sub5 = att5;
    }

    public JSONObject toJSON() {
        HashMap map = new HashMap();
        if (this.itemname != null) {
            map.put("item", this.itemname);
        }
        map.put("quantity", Integer.toString(this.quantity));
        map.put("unit_price", Double.toString(this.unitPrice));
        map.put("revenue", Double.toString(this.revenue));
        if (this.attribute_sub1 != null) {
            map.put("attribute_sub1", this.attribute_sub1);
        }
        if (this.attribute_sub2 != null) {
            map.put("attribute_sub2", this.attribute_sub2);
        }
        if (this.attribute_sub3 != null) {
            map.put("attribute_sub3", this.attribute_sub3);
        }
        if (this.attribute_sub4 != null) {
            map.put("attribute_sub4", this.attribute_sub4);
        }
        if (this.attribute_sub5 != null) {
            map.put("attribute_sub5", this.attribute_sub5);
        }
        return new JSONObject(map);
    }
}
