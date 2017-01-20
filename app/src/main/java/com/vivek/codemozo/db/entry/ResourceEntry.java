package com.vivek.codemozo.db.entry;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.vivek.codemozo.db.DBContract;

/**
 * Created by devenv on 1/20/17.
 */

public final class ResourceEntry implements BaseColumns {
    public static final String TABLE_NAME = "resources_table";
    public static final String RESOURCE_ID_COL = "rid";
    public static final String RESOURCE_NAME_COL = "rname";
    public static final String RESOURCE_SHOW_COL = "show";

    public static final Uri CONTENT_URI_ALL =
            DBContract.BASE_CONTENT_URI.buildUpon().appendPath(DBContract.PATH_RESOURCE).build();

    public static final String CONTENT_DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DBContract.CONTENT_AUTHORITY + "/" + DBContract.PATH_RESOURCE;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DBContract.CONTENT_AUTHORITY + "/" + DBContract.PATH_RESOURCE;


    public static final String[] RESOURCE_PROJECTION = new String[]{
            _ID,
            RESOURCE_ID_COL,
            RESOURCE_NAME_COL,
            RESOURCE_SHOW_COL
    };
}