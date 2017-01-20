package com.vivek.codemozo.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import com.vivek.codemozo.R;
import com.vivek.codemozo.db.entry.ResourceEntry;

import java.util.HashMap;


public class AppUtils {

    public static final String BASE_URL = "http://clist.by:80/";

    public static final long SIX_HOURS = 6 * 60 * 60;
    public static final long ONE_DAY = SIX_HOURS * 4;
    public static final long ONE_WEEK = ONE_DAY * 7;
    public static final long ONE_MONTH = ONE_WEEK * 4;

    public static final String MAX_CONTEST_DURATION = "MAX_CONTEST_DURATION";
    public static final long MAX_DEFAULT_CONTEST_DURATION = 3 * ONE_MONTH;
    public static final String LAST_SYNC_PERFORMED = "LAST_SYNC_PERFORMED";
    public static final long LAST_SYNC_PERFORMED_DEFAULT_VALUE = -1;


    public static final int STATUS_CONTEST_ENDED = -1;
    public static final int STATUS_CONTEST_LIVE = 0;
    public static final int STATUS_CONTEST_FUTURE = 1;

    public static final int STATUS_CALENDAR_EVENT_SUCCESS = 0;
    public static final int STATUS_CALENDAR_PERMISSION_ERROR = -1;
    public static final int STATUS_CALENDAR_NO_ACCOUNT = -2;
    public static final int STATUS_CALENDAR_EVENT_ALREADY_ADDED = -3;
    public static final int STATUS_CALENDAR_EVENT_NOT_PRESENT = -4;

    public static final int STATUS_CALENDAR_EVENT_NOT_PRESENT_SO_ADDED = 101;
    public static final int STATUS_CALENDAR_EVENT_PRESENT_SO_REMOVED = 102;

    public static final String BROADCAST_DATA_UPDATED = "com.vivek.codemozo" + ".BROADCAST_SYNC_PERFORMED";
    public static final String CONTEST_KEY = "CONTEST_KEY";
    public static final String UNKNOWN_RESOURCE = "Unknown Website";
    public static final String GIT_URL = "https://github.com/vivekkiran/codemozo";
    public static final String EMAIL_ID = "thevivekkiran@gmail.com";

    static HashMap<Integer, String> resourceHashMap;

    public static void cacheResources(Context context) {
        if (resourceHashMap == null) {
            resourceHashMap = new HashMap<>();
        }
        Debug.c();
        Cursor cursor = context.getContentResolver().query(
                ResourceEntry.CONTENT_URI_ALL,
                ResourceEntry.RESOURCE_PROJECTION,
                null,
                null,
                null
        );
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(ResourceEntry
                            .RESOURCE_ID_COL));
                    String name = cursor.getString(cursor.getColumnIndex(ResourceEntry
                            .RESOURCE_NAME_COL));
                    resourceHashMap.put(id, name);
                }
            } finally {
                cursor.close();
            }
        }
    }

    public static String getResourceName(Context context, int key) {
        if (resourceHashMap == null
                || resourceHashMap.size() == 0
                || (!resourceHashMap.containsKey(key))) {
            cacheResources(context);
        }
        if (resourceHashMap.containsKey(key)) {
            return resourceHashMap.get(key);
        }
        return UNKNOWN_RESOURCE;
    }

    public static String getGoodResourceName(String resource) {
        String ret = resource;
        int slash = resource.indexOf("/");
        if (slash != -1) {
            ret = resource.substring(slash + 1);
        } else {
            int lastDot = resource.lastIndexOf(".");
            if (lastDot != -1) {
                ret = resource.substring(0, lastDot);
            }
        }
        ret = ret.replaceAll("\\.", " ").toUpperCase();
        return ret;
    }

    public static int getImageForResource(String resource) {
        if (resource != null) {
            String res = resource.toLowerCase();
            if (res.contains("codechef")) {
                return R.drawable.codechef;
            } else if (res.contains("hackerrank")) {
                return R.drawable.hackerrank;
            } else if (res.contains("hackerearth")) {
                return R.drawable.hackerearth;
            } else if (res.contains("codeforces")) {
                return R.drawable.codeforces;
            } else if (res.contains("topcoder")) {
                return R.drawable.topcoder;
            } else if (res.contains("spoj")) {
                return R.drawable.spoj;
            } else if (res.contains("kaggle")) {
                return R.drawable.kaggle;
            }
        }
        return R.mipmap.ic_launcher;
    }

    public static void openWebsite(Context context, CoordinatorLayout mCoordinatorLayout, String url) {
        if (url != null && url.trim().length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                showSnackBarMessage(context, mCoordinatorLayout, context.getString(R.string.website_open_error));
            }
        }

    }

    public static void showSnackBarMessage(Context context, CoordinatorLayout mCoordinatorLayout, String msg) {
        if (context != null && mCoordinatorLayout != null) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, HtmlUtils.fromHtml(msg), Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            snackbar.show();
        }
    }
}
