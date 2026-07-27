package com.mobileapptracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class MATProvider extends ContentProvider {
    public static final String PUBLISHER_PACKAGE_NAME = "publisher_package_name";
    public static final String TRACKING_ID = "tracking_id";
    public static final String _ID = "_id";
    private static final UriMatcher a;
    private SQLiteDatabase b;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        a = uriMatcher;
        uriMatcher.addURI("*", "referrer_apps", 1);
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (a.match(uri)) {
            case 1:
                int iDelete = this.b.delete("referrer_apps", selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return iDelete;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        switch (a.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/vnd.mobileapptracker.referrer_apps ";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues values) {
        long jInsert = this.b.insert("referrer_apps", "", values);
        if (jInsert <= 0) {
            throw new SQLException("Failed to insert row into " + uri);
        }
        Uri uriWithAppendedId = ContentUris.withAppendedId(uri, jInsert);
        getContext().getContentResolver().notifyChange(uriWithAppendedId, null);
        return uriWithAppendedId;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.b = new a(getContext()).getWritableDatabase();
        return this.b != null;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables("referrer_apps");
        if (sortOrder == null || sortOrder == "") {
            sortOrder = PUBLISHER_PACKAGE_NAME;
        }
        Cursor cursorQuery = sQLiteQueryBuilder.query(this.b, projection, selection, selectionArgs, null, null, sortOrder);
        cursorQuery.setNotificationUri(getContext().getContentResolver(), uri);
        return cursorQuery;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (a.match(uri)) {
            case 1:
                int iUpdate = this.b.update("referrer_apps", values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return iUpdate;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
