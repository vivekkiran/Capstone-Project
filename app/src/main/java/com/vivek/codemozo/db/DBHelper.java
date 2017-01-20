package com.vivek.codemozo.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vivek.codemozo.db.entry.ContestEntry;
import com.vivek.codemozo.db.entry.ResourceEntry;
import com.vivek.codemozo.utils.Debug;


public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "codemozo.db";

    public static final String SQL_CREATE_TABLE_RESOURCE =
            "CREATE TABLE " + ResourceEntry.TABLE_NAME + " ("
                    + ResourceEntry._ID + " INTEGER PRIMARY KEY,"
                    + ResourceEntry.RESOURCE_ID_COL + " INTEGER NOT NULL,"
                    + ResourceEntry.RESOURCE_NAME_COL + " TEXT NOT NULL,"
                    + ResourceEntry.RESOURCE_SHOW_COL + " INTEGER NOT NULL,"
                    + " UNIQUE (" + ResourceEntry.RESOURCE_ID_COL + ") ON CONFLICT IGNORE"
                    + " );";

    public static final String SQL_CREATE_TABLE_CONTESTS =
            "CREATE TABLE " + ContestEntry.TABLE_NAME + " ("
                    + ContestEntry._ID + " INTEGER PRIMARY KEY,"
                    + ContestEntry.CONTEST_ID_COL + " INTEGER NOT NULL,"
                    + ContestEntry.CONTEST_NAME_COL + " TEXT NOT NULL,"
                    + ContestEntry.CONTEST_URL_COL + " TEXT NOT NULL,"
                    + ContestEntry.CONTEST_RESOURCE_ID_COL + " TEXT NOT NULL,"
                    + ContestEntry.CONTEST_START_COL + " INTEGER NOT NULL,"
                    + ContestEntry.CONTEST_END_COL + " INTEGER NOT NULL,"
                    + ContestEntry.CONTEST_DURATION_COL + " INTEGER NOT NULL,"
                    + " UNIQUE (" + ContestEntry.CONTEST_ID_COL + ") ON CONFLICT REPLACE"
                    + " );";

//    public static final String SQL_CREATE_TABLE_NOTIFICATIONS =
//            "CREATE TABLE " + NotificationEntry.TABLE_NAME + " ("
//                    + NotificationEntry._ID + " INTEGER PRIMARY KEY,"
//                    + NotificationEntry.NOTIFICATION_CONTEST_ID_COL + " INTEGER NOT NULL,"
//                    + NotificationEntry.NOTIFICATION_TIME_COL + " INTEGER NOT NULL,"
//                    + NotificationEntry.NOTIFICATION_CONTEST_START_TIME_COL + " INTEGER NOT NULL,"
//                    + " UNIQUE (" + NotificationEntry._ID + ") ON CONFLICT REPLACE"
//                    + " );";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Debug.c();
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Debug.c();
        db.execSQL(SQL_CREATE_TABLE_RESOURCE);
        db.execSQL(SQL_CREATE_TABLE_CONTESTS);
//        db.execSQL(SQL_CREATE_TABLE_NOTIFICATIONS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Debug.c();
        db.execSQL("DROP TABLE IF EXISTS " + ResourceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ContestEntry.TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        onCreate(db);
    }
}
