package com.rose.quickwallet.transactions.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class specifies the table names and columns and the content uri
 * Created by rohanx96 on 11/5/16.
 */

class QuickWalletContract {
    static final String CONTENT_AUTHORITY = "com.rose.quickwallet";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_QUICK_WALLET = "QuickWallet";

    static final class QuickWalletEntries implements BaseColumns{
        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUICK_WALLET).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUICK_WALLET;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUICK_WALLET;

        static final String TABLE_NAME = "QuickWallet";
        final static String COLUMN_NAME = "Name";
        final static String COLUMN_IMAGE_URI = "ImageUri";
        static final String COLUMN_BALANCE = "Balance";
        static final String COLUMN_LAST_UPDATE = "Updated";
        static final String COLUMN_PHONE = "PhoneNo";
        static final String COLUMN_QUICKBLOX_ID = "QuickbloxID";
        static final String COLUMN_HISTORY_TYPE = "Type";
        static final String COLUMN_HISTORY_VALUE = "Value";
        static final String COLUMN_HISTORY_TIME = "Time";
        static final String COLUMN_HISTORY_DETAIL = "Details";
        static final String COLUMN_HISTORY_ID = "ID";
    }

    public static String getNameFromUri(Uri uri){
        return uri.getLastPathSegment();
    }
}
