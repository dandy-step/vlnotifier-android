package com.dandystep.vlnotifier;

import android.provider.BaseColumns;

final class DatabaseClass {
    private DatabaseClass() {}

    private static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "casters";
        static final String CASTERNAME_COLUMN = "casterName";
        static final String NOTIFICATIONSTATE_COLUMN = "notificationState";
        static final String LOGO_COLUMN = "logoBitmap";
    }

    static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + FeedEntry.TABLE_NAME + " (" + FeedEntry._ID + " INTEGER PRIMARY KEY, " + FeedEntry.CASTERNAME_COLUMN + " TEXT NOT NULL, " + FeedEntry.NOTIFICATIONSTATE_COLUMN + " INTEGER NOT NULL, " + FeedEntry.LOGO_COLUMN + " BLOB)";
}
