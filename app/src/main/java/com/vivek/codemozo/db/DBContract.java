package com.vivek.codemozo.db;

import android.net.Uri;


public class DBContract {

    public static final String CONTENT_AUTHORITY = "com.vivek.codemozo";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_RESOURCE = "resource";
    public static final String PATH_CONTEST = "contests";
    public static final String PATH_NOTIFICATION = "notifications";
    public static final String PATH_CALENDAR = "calendar";
    public static final String PATH_LIVE = "live";
    public static final String PATH_FUTURE = "future";
    public static final String PATH_PAST = "past";


}
