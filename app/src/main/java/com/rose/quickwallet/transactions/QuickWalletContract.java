package com.rose.quickwallet.transactions;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rohanx96 on 11/5/16.
 */

public class QuickWalletContract {
    public static final String CONTENT_AUTHORITY = "com.rose.quickwallet";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_QUICK_WALLET = "QuickWallet";

    public static final class QuickWalletEntries implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUICK_WALLET).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUICK_WALLET;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUICK_WALLET;

        public static final String TABLE_NAME = "QuickWallet";
        public final static String COLUMN_NAME = "Name";
        public final static String COLUMN_IMAGE_URI = "ImageUri";
        public static final String COLUMN_BALANCE = "Balance";
        public static final String COLUMN_LAST_UPDATE = "Updated";
        public static final String COLUMN_PHONE = "PhoneNo";
        public static final String COLUMN_QUICKBLOX_ID = "QuickbloxID";
        public static final String COLUMN_HISTORY_TYPE = "Type";
        public static final String COLUMN_HISTORY_VALUE = "Value";
        public static final String COLUMN_HISTORY_TIME = "Time";
        public static final String COLUMN_HISTORY_DETAIL = "Details";
        public static final String COLUMN_HISTORY_ID = "ID";
    }

    public static String getNameFromUri(Uri uri){
        return uri.getLastPathSegment();
    }
}
