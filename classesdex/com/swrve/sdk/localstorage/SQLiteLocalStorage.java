package com.swrve.sdk.localstorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SQLiteLocalStorage implements ILocalStorage, IFastInsertLocalStorage {
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_EVENT = "event";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RAW_DATA = "raw_data";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_TARGET_GAME_ID = "target_game_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final int SWRVE_DB_VERSION = 1;
    public static final String TABLE_CACHE = "server_cache";
    public static final String TABLE_CLICK_THRUS = "click_thrus";
    public static final String TABLE_EVENTS_JSON = "events";
    private SQLiteDatabase database;
    private SwrveSQLiteOpenHelper dbHelper;

    private static class SwrveSQLiteOpenHelper extends SQLiteOpenHelper {
        public SwrveSQLiteOpenHelper(Context context, String dbName) {
            super(context, dbName, (SQLiteDatabase.CursorFactory) null, 1);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE events (_id INTEGER PRIMARY KEY AUTOINCREMENT, event TEXT NOT NULL);");
            db.execSQL("CREATE TABLE server_cache (user_id TEXT NOT NULL, category TEXT NOT NULL, raw_data TEXT NOT NULL, PRIMARY KEY (user_id,category));");
            db.execSQL("CREATE TABLE click_thrus (_id INTEGER PRIMARY KEY AUTOINCREMENT, target_game_id INTEGER NOT NULL, source TEXT NOT NULL);");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public SQLiteLocalStorage(Context context, String dbName, long maxDbSize) {
        this.dbHelper = new SwrveSQLiteOpenHelper(context, dbName);
        this.database = this.dbHelper.getWritableDatabase();
        this.database.setMaximumSize(maxDbSize);
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void addEvent(String eventJSON) throws SQLException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT, eventJSON);
        this.database.insertOrThrow(TABLE_EVENTS_JSON, null, values);
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void removeEventsById(Collection<Long> ids) {
        List<String> values = new ArrayList<>(ids.size());
        Iterator<Long> it = ids.iterator();
        while (it.hasNext()) {
            long id = it.next().longValue();
            values.add(Long.toString(id));
        }
        this.database.delete(TABLE_EVENTS_JSON, "_id IN (" + TextUtils.join(",  ", values) + ")", null);
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public Map<Long, String> getFirstNEvents(Integer n) {
        Map<Long, String> events = new HashMap<>();
        Cursor cursor = this.database.query(TABLE_EVENTS_JSON, new String[]{"_id", COLUMN_EVENT}, null, null, null, null, "_id", n == null ? null : Integer.toString(n.intValue()));
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            events.put(Long.valueOf(cursor.getLong(0)), cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return events;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void setCacheEntryForUser(String userId, String category, String rawData) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_RAW_DATA, rawData);
        insertOrUpdate(TABLE_CACHE, values, "user_id= ? AND category= ?", new String[]{userId, category});
    }

    private void insertOrUpdate(String table, ContentValues values, String whereClause, String[] whereArgs) {
        int affectedRows = this.database.update(table, values, whereClause, whereArgs);
        if (affectedRows == 0) {
            this.database.insertOrThrow(table, null, values);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public String getCacheEntryForUser(String userId, String category) {
        String resultJSON = null;
        Cursor cursor = this.database.query(TABLE_CACHE, new String[]{COLUMN_RAW_DATA}, "user_id= \"" + userId + "\" AND " + COLUMN_CATEGORY + "= \"" + category + "\"", null, null, null, null, "1");
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            resultJSON = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        return resultJSON;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void reset() {
        this.database.delete(TABLE_EVENTS_JSON, null, null);
        this.database.delete(TABLE_CACHE, null, null);
        this.database.delete(TABLE_CLICK_THRUS, null, null);
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void addClickThru(int targetGameId, String source) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TARGET_GAME_ID, Integer.valueOf(targetGameId));
        values.put(COLUMN_SOURCE, source);
        this.database.insertOrThrow(TABLE_CLICK_THRUS, null, values);
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void removeClickThrusById(long id) {
        List<String> values = new ArrayList<>(1);
        values.add(Long.toString(id));
        this.database.delete(TABLE_CLICK_THRUS, "_id IN (" + TextUtils.join(",  ", values) + ")", null);
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public Map<Long, Map.Entry<Integer, String>> getFirstNClickThrus(Integer n) {
        Map<Long, Map.Entry<Integer, String>> click_thrus = new HashMap<>();
        Cursor cursor = this.database.query(TABLE_CLICK_THRUS, new String[]{"_id", COLUMN_TARGET_GAME_ID, COLUMN_SOURCE}, null, null, null, null, "_id", n == null ? null : Integer.toString(n.intValue()));
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            click_thrus.put(Long.valueOf(cursor.getLong(0)), new SimpleEntry<>(Integer.valueOf(cursor.getInt(1)), cursor.getString(2)));
            cursor.moveToNext();
        }
        cursor.close();
        return click_thrus;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public Map<Map.Entry<String, String>, String> getAllCacheEntries() {
        Map<Map.Entry<String, String>, String> allCacheEntries = new HashMap<>();
        Cursor cursor = this.database.query(TABLE_CACHE, new String[]{COLUMN_USER_ID, COLUMN_CATEGORY, COLUMN_RAW_DATA}, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            allCacheEntries.put(new SimpleEntry<>(cursor.getString(0), cursor.getString(1)), cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        return allCacheEntries;
    }

    @Override // com.swrve.sdk.localstorage.IFastInsertLocalStorage
    public void addMultipleEvent(List<String> eventsJSON) throws SQLException {
        this.database.beginTransaction();
        try {
            SQLiteStatement stmt = this.database.compileStatement("INSERT INTO events (event) VALUES (?)");
            Iterator<String> eventsIt = eventsJSON.iterator();
            while (eventsIt.hasNext()) {
                stmt.bindString(1, eventsIt.next());
                stmt.execute();
                stmt.clearBindings();
            }
            this.database.setTransactionSuccessful();
        } finally {
            this.database.endTransaction();
        }
    }

    @Override // com.swrve.sdk.localstorage.IFastInsertLocalStorage
    public void addMultipleClickThrus(List<Map.Entry<Integer, String>> clickThrus) throws SQLException {
        this.database.beginTransaction();
        try {
            SQLiteStatement stmt = this.database.compileStatement("INSERT INTO click_thrus (target_game_id, source) VALUES (?, ?)");
            for (Map.Entry<Integer, String> clickThru : clickThrus) {
                stmt.bindDouble(1, clickThru.getKey().intValue());
                stmt.bindString(2, clickThru.getValue());
                stmt.execute();
                stmt.clearBindings();
            }
            this.database.setTransactionSuccessful();
        } finally {
            this.database.endTransaction();
        }
    }

    @Override // com.swrve.sdk.localstorage.IFastInsertLocalStorage
    public void setMultipleCacheEntries(List<Map.Entry<String, Map.Entry<String, String>>> cacheEntries) throws SQLException {
        this.database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Map.Entry<String, Map.Entry<String, String>> cacheEntry : cacheEntries) {
                String userId = cacheEntry.getKey();
                String category = cacheEntry.getValue().getKey();
                values.put(COLUMN_USER_ID, userId);
                values.put(COLUMN_CATEGORY, category);
                values.put(COLUMN_RAW_DATA, cacheEntry.getValue().getValue());
                insertOrUpdate(TABLE_CACHE, values, "user_id= ? AND category= ?", new String[]{userId, category});
            }
            this.database.setTransactionSuccessful();
        } finally {
            this.database.endTransaction();
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void close() {
        this.dbHelper.close();
        this.database.close();
    }
}
