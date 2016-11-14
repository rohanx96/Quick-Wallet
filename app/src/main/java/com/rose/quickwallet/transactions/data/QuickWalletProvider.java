package com.rose.quickwallet.transactions.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.rose.quickwallet.transactions.data.DatabaseOpenHelper;
import com.rose.quickwallet.transactions.data.QuickWalletContract;

/**
 * This class specifies the supported URIs and their implementation for query, insert, update and delete methods
 * Created by rohanx96 on 11/5/16.
 */

public class QuickWalletProvider extends ContentProvider {

    static final int QUICK_WALLET = 100;
    static final int HISTORY_DETAILS = 101;
    private DatabaseOpenHelper mDatabaseHelper;
    static  UriMatcher uriMatcher = buildUriMatcher();

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(QuickWalletContract.CONTENT_AUTHORITY, QuickWalletContract.PATH_QUICK_WALLET, QUICK_WALLET);
        matcher.addURI(QuickWalletContract.CONTENT_AUTHORITY, QuickWalletContract.PATH_QUICK_WALLET + "/*", HISTORY_DETAILS );
        return matcher;
    }
    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)){
            case QUICK_WALLET:
                retCursor = mDatabaseHelper.getReadableDatabase().query(QuickWalletContract.QuickWalletEntries.TABLE_NAME,
                        projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case HISTORY_DETAILS:
                retCursor = mDatabaseHelper.getReadableDatabase().query(QuickWalletContract.QuickWalletEntries.TABLE_NAME + QuickWalletContract.getNameFromUri(uri),
                        projection,selection,selectionArgs,null,null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case QUICK_WALLET:
                return QuickWalletContract.QuickWalletEntries.CONTENT_TYPE;
            case HISTORY_DETAILS:
                return QuickWalletContract.QuickWalletEntries.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        switch (uriMatcher.match(uri)){
            case QUICK_WALLET:
                mDatabaseHelper.getWritableDatabase().insert(QuickWalletContract.QuickWalletEntries.TABLE_NAME,null,contentValues);
                break;
            case HISTORY_DETAILS:
                mDatabaseHelper.getWritableDatabase().insert(QuickWalletContract.QuickWalletEntries.TABLE_NAME +
                        QuickWalletContract.getNameFromUri(uri),null,contentValues);
                break;
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        switch (uriMatcher.match(uri)){
            case QUICK_WALLET:
                mDatabaseHelper.getWritableDatabase().delete(QuickWalletContract.QuickWalletEntries.TABLE_NAME,s,strings);
                break;
            case HISTORY_DETAILS:
                mDatabaseHelper.getWritableDatabase().delete(QuickWalletContract.QuickWalletEntries.TABLE_NAME + QuickWalletContract.getNameFromUri(uri),
                        s, strings);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        switch (uriMatcher.match(uri)){
            case QUICK_WALLET:
                mDatabaseHelper.getWritableDatabase().update(QuickWalletContract.QuickWalletEntries.TABLE_NAME,contentValues,
                        s,strings);
                break;
            case HISTORY_DETAILS:
                mDatabaseHelper.getWritableDatabase().update(QuickWalletContract.QuickWalletEntries.TABLE_NAME +
                        QuickWalletContract.getNameFromUri(uri),contentValues, s, strings);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        return 1;
    }
}
