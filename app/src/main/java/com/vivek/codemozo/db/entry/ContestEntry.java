package com.vivek.codemozo.db.entry;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.vivek.codemozo.db.DBContract;

/**
 * Created by devenv on 1/20/17.
 */

public final class ContestEntry implements BaseColumns {
    public static final String TABLE_NAME = "contests_table";
    public static final String CONTEST_ID_COL = "cid";
    public static final String CONTEST_NAME_COL = "name";
    public static final String CONTEST_URL_COL = "url";
    public static final String CONTEST_RESOURCE_ID_COL = "resource_id";
    public static final String CONTEST_START_COL = "start";
    public static final String CONTEST_END_COL = "end";
    public static final String CONTEST_DURATION_COL = "duration";
    public static final Uri CONTENT_URI_ALL =
            DBContract.BASE_CONTENT_URI.buildUpon().appendPath(DBContract.PATH_CONTEST).build();

    public static Uri buildContestUriWithId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI_ALL, id);
    }

    public static long getIdFromContestUri(Uri uri) {
        return ContentUris.parseId(uri);
    }

    public static final Uri CONTENT_URI_LIVE =
            CONTENT_URI_ALL.buildUpon().appendPath(DBContract.PATH_LIVE).build();

    public static Uri buildLiveContestUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI_LIVE, id);
    }

    public static final Uri CONTENT_URI_FUTURE =
            CONTENT_URI_ALL.buildUpon().appendPath(DBContract.PATH_FUTURE).build();

    public static Uri buildFutureContestUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI_FUTURE, id);
    }

    public static final Uri CONTENT_URI_PAST =
            CONTENT_URI_ALL.buildUpon().appendPath(DBContract.PATH_PAST).build();

    public static Uri buildPastContestUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI_PAST, id);
    }

    public static long getTimeFromContestUri(Uri uri) {
        return ContentUris.parseId(uri);
    }

    public static final String CONTENT_DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DBContract.CONTENT_AUTHORITY + "/" + DBContract.PATH_CONTEST;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DBContract.CONTENT_AUTHORITY + "/" + DBContract.PATH_CONTEST;


    public static final String[] CONTEST_PROJECTION = new String[]{
            _ID,
            CONTEST_ID_COL,
            CONTEST_NAME_COL,
            CONTEST_URL_COL,
            CONTEST_RESOURCE_ID_COL,
            CONTEST_START_COL,
            CONTEST_END_COL,
            CONTEST_DURATION_COL
    };

}
