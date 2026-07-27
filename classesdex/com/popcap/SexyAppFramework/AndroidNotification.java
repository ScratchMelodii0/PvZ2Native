package com.popcap.SexyAppFramework;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.swrve.sdk.localstorage.SQLiteLocalStorage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidNotification {
    private static final String PROPERTY_NOTIFICATION_LIST = "notification_list";
    private static final String TAG = "AndroidNotification";
    private Context mContext = SexyAppFrameworkActivity.instance();

    public void createBasicNotification(Context context, String activity, String action, String title, String message, String icon, String sound, int notificationId, Bundle fields) {
        Uri playSound;
        Log.i(TAG, "Creating basic notification=" + fields.toString());
        try {
            Class<?> reflectedActivity = Class.forName(activity);
            Intent gameIntent = new Intent(context, reflectedActivity);
            gameIntent.putExtra("data.fields", fields);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, gameIntent, 0);
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            if (sound != null && sound.length() != 0) {
                playSound = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + sound);
            } else {
                playSound = RingtoneManager.getDefaultUri(2);
            }
            if (icon == null) {
                icon = "notification_icon";
            }
            int iconId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
            Notification mNotification = new NotificationCompat.Builder(context).setSmallIcon(iconId).setContentTitle(title).setContentText(message).setSound(playSound).setContentIntent(contentIntent).setDefaults(6).setAutoCancel(true).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).build();
            nm.notify(notificationId, mNotification);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    private void scheduleBasicNotification(String activity, String action, String title, String message, String icon, String sound, int notificationId, int notificationGroupId, String source, int fireAt, int secondsInterval, String userInfo) {
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        Intent receiverIntent = new Intent(this.mContext, (Class<?>) ScheduledNotificationReceiver.class);
        Bundle messageBundle = new Bundle();
        messageBundle.putString("data.source", source);
        messageBundle.putString("data.title", title);
        messageBundle.putString("data.message", message);
        messageBundle.putString("data.icon", icon);
        messageBundle.putString("data.activity", activity);
        messageBundle.putString("data.action", action);
        messageBundle.putString("data.sound", sound);
        messageBundle.putInt("data.id", notificationId);
        messageBundle.putInt("data.groupid", notificationGroupId);
        messageBundle.putString("data.source", source);
        Bundle fields = new Bundle();
        fields.putString("userInfo", userInfo);
        fields.putString("notificationType", "LN");
        messageBundle.putBundle("data.fields", fields);
        receiverIntent.putExtras(messageBundle);
        addNotification(notificationId, notificationGroupId, source, userInfo);
        long currentTime = System.currentTimeMillis();
        long fireAtMillis = fireAt != 0 ? ((long) fireAt) * 1000 : currentTime;
        boolean scheduled = false;
        if (secondsInterval > 0) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, notificationId, receiverIntent, 134217728);
            long secondsIntervalMillis = ((long) secondsInterval) * 1000;
            if (currentTime > fireAtMillis) {
                long futureOffset = secondsIntervalMillis - ((currentTime - fireAtMillis) % secondsIntervalMillis);
                fireAtMillis = currentTime + futureOffset;
            }
            alarmManager.setRepeating(0, fireAtMillis, secondsIntervalMillis, pendingIntent);
            scheduled = true;
            Log.v(TAG, "Notification: repeat every " + secondsInterval + " seconds");
        } else if (fireAtMillis >= currentTime) {
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this.mContext, notificationId, receiverIntent, 134217728);
            alarmManager.set(0, fireAtMillis, pendingIntent2);
            scheduled = true;
        }
        if (scheduled) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(fireAtMillis);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Log.v(TAG, "Notification: Scheduled for: " + formatter.format(calendar.getTime()));
            return;
        }
        removeNotification(notificationId);
    }

    private void removeScheduledNotification(int notificationId) {
        Log.i(TAG, "Removing scheduled notification");
        List<Integer> listOfOne = new ArrayList<>(1);
        listOfOne.add(0, Integer.valueOf(notificationId));
        removeScheduledNotifications(listOfOne);
    }

    private void removeScheduledNotifications(List<Integer> notificationIds) {
        Log.i(TAG, "Removing multiple scheduled notifications");
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        for (int i = 0; i < notificationIds.size(); i++) {
            int notificationId = notificationIds.get(i).intValue();
            Intent receiverIntent = new Intent(this.mContext, (Class<?>) ScheduledNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, notificationId, receiverIntent, 134217728);
            alarmManager.cancel(pendingIntent);
        }
        removeNotifications(notificationIds);
    }

    private void removeScheduledNotificationsBySource(String sourceName) {
        SharedPreferences prefs = getNotificationPreferences(this.mContext);
        String notifications = prefs.getString(PROPERTY_NOTIFICATION_LIST, "[]");
        try {
            JSONArray json = new JSONArray(notifications);
            List<Integer> sourceIds = FilterIdsByStringField(json, SQLiteLocalStorage.COLUMN_SOURCE, sourceName);
            removeNotifications(sourceIds);
        } catch (Exception e) {
            Log.e(TAG, "Could not remove notification. " + e.getMessage());
        }
    }

    private void removeAllScheduledNotifications() {
        List<Integer> notificationIds = getAllNotificationIds();
        removeScheduledNotifications(notificationIds);
        try {
            removeScheduledNotifications(notificationIds);
        } catch (Exception e) {
            Log.e(TAG, "Could not remove notification. " + e.getMessage());
        }
    }

    private String getScheduledLocalNotificationsData() {
        String structuredDataJson = "[";
        SharedPreferences prefs = getNotificationPreferences(this.mContext);
        String notifications = prefs.getString(PROPERTY_NOTIFICATION_LIST, "[]");
        try {
            JSONArray json = new JSONArray(notifications);
            for (int i = 0; i < json.length(); i++) {
                JSONObject entry = json.getJSONObject(i);
                structuredDataJson = structuredDataJson + entry.getString("userInfo");
                if (i != json.length() - 1) {
                    structuredDataJson = structuredDataJson + ",";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return structuredDataJson + "]";
    }

    private SharedPreferences getNotificationPreferences(Context context) {
        return context.getSharedPreferences(SexyAppFrameworkActivity.class.getSimpleName(), 0);
    }

    private void addNotification(int notificationId, int notificationGroupId, String source, String userInfo) {
        SharedPreferences prefs = getNotificationPreferences(this.mContext);
        String notifications = prefs.getString(PROPERTY_NOTIFICATION_LIST, "[]");
        try {
            JSONArray json = new JSONArray(notifications);
            JSONObject entry = FindNotificationItemById(json, notificationId);
            if (entry == null) {
                json.put(new JSONObject().put("id", notificationId).put("groupid", notificationGroupId).put(SQLiteLocalStorage.COLUMN_SOURCE, source).put("userInfo", userInfo));
            } else {
                entry.put("groupid", notificationGroupId).put(SQLiteLocalStorage.COLUMN_SOURCE, source).put("userInfo", userInfo);
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_NOTIFICATION_LIST, json.toString());
            editor.commit();
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private JSONObject FindNotificationItemById(JSONArray json, int notificationId) {
        return FindObjectByIntField(json, "id", notificationId);
    }

    private JSONObject FindObjectByIntField(JSONArray json, String fieldName, int value) {
        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject entry = json.getJSONObject(i);
                if (entry.has(fieldName) && entry.getInt(fieldName) == value) {
                    return entry;
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    private List<Integer> FilterIdsByStringField(JSONArray json, String fieldName, String value) {
        List<Integer> outList = new Vector<>();
        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject entry = json.getJSONObject(i);
                if (entry.has(fieldName) && entry.getString(fieldName).equals(value)) {
                    outList.add(Integer.valueOf(entry.getInt("id")));
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return outList;
    }

    private void removeNotification(int notificationId) {
        List<Integer> listOfOne = new ArrayList<>(1);
        listOfOne.add(0, Integer.valueOf(notificationId));
        removeNotifications(listOfOne);
    }

    private void removeNotifications(List<Integer> notificationIds) {
        SharedPreferences prefs = getNotificationPreferences(this.mContext);
        String notifications = prefs.getString(PROPERTY_NOTIFICATION_LIST, "[]");
        JSONArray newNotificationList = new JSONArray();
        try {
            JSONArray json = new JSONArray(notifications);
            for (int i = 0; i < json.length(); i++) {
                JSONObject entry = json.getJSONObject(i);
                if (!notificationIds.contains(Integer.valueOf(entry.getInt("id")))) {
                    newNotificationList.put(entry);
                }
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_NOTIFICATION_LIST, newNotificationList.toString());
            editor.commit();
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private List<Integer> getAllNotificationIds() {
        SharedPreferences prefs = getNotificationPreferences(this.mContext);
        String notifications = prefs.getString(PROPERTY_NOTIFICATION_LIST, "[]");
        List<Integer> ids = new Vector<>();
        try {
            JSONArray json = new JSONArray(notifications);
            for (int i = 0; i < json.length(); i++) {
                JSONObject entry = json.getJSONObject(i);
                ids.add(Integer.valueOf(entry.getInt("id")));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return ids;
    }

    private String getTimezone() {
        return AndroidUtil.getTimezone();
    }
}
