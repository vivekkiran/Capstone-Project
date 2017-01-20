package com.vivek.codemozo.db.entry;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.vivek.codemozo.db.DBContract;

/**
 * Created by devenv on 1/20/17.
 */

public final class NotificationEntry implements BaseColumns {
    public static final String TABLE_NAME = "notifications_table";
    public static final String NOTIFICATION_CONTEST_ID_COL = "contest_id";
    public static final String NOTIFICATION_TIME_COL = "notify_time";
    public static final String NOTIFICATION_CONTEST_START_TIME_COL = "start_time";


    public static final Uri CONTENT_URI_ALL_NOTIFICATIONS =
            DBContract.BASE_CONTENT_URI.buildUpon().appendPath(DBContract.PATH_NOTIFICATION).build();

    public static Uri buildNotificationEventUriWithContestId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI_ALL_NOTIFICATIONS, id);
    }

    public static long getContestIdFromNotificationEventUri(Uri uri) {
        return ContentUris.parseId(uri);
    }

    public static final String CONTENT_DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DBContract.CONTENT_AUTHORITY + "/" + DBContract.PATH_NOTIFICATION;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DBContract.CONTENT_AUTHORITY + "/" + DBContract.PATH_NOTIFICATION;


    public static final String[] NOTIFICATION_PROJECTION = new String[]{
            _ID,
            NOTIFICATION_CONTEST_ID_COL,
            NOTIFICATION_TIME_COL,
            NOTIFICATION_CONTEST_START_TIME_COL
    };
}
