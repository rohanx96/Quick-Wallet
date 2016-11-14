package com.rose.quickwallet.transactions.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_BALANCE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_IMAGE_URI;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_LAST_UPDATE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_NAME;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_PHONE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_QUICKBLOX_ID;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.TABLE_NAME;

/**
 * Helper class to get a readable or writable database
 * Created by rohanx96 on 11/5/16.
 */


public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "QuickWallet.db";
    private static int VERSION = 2;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
//        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_NAME + " TEXT UNIQUE NOT NULL," + COLUMN_IMAGE_URI + " TEXT," + COLUMN_BALANCE + " REAL," + COLUMN_LAST_UPDATE + " INTEGER," + COLUMN_PHONE + " TEXT," + COLUMN_QUICKBLOX_ID + " INTEGER DEFAULT -1)";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            /*String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
            sqLiteDatabase.execSQL(query);
            onCreate(sqLiteDatabase);*/
        String upgradeQuery1 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_PHONE + " TEXT";
        String upgradeQuery2 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_QUICKBLOX_ID + " INTEGER DEFAULT -1";
        if (oldVersion == 1 && newVersion == 2) {
            sqLiteDatabase.execSQL(upgradeQuery1);
            sqLiteDatabase.execSQL(upgradeQuery2);
        }
    }
}
