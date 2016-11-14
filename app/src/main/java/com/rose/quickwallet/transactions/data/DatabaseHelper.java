package com.rose.quickwallet.transactions.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.rose.quickwallet.transactions.DetailsRecyclerViewItem;
import com.rose.quickwallet.transactions.RecyclerViewItem;

import java.util.ArrayList;

import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_BALANCE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_HISTORY_DETAIL;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_HISTORY_ID;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_HISTORY_TIME;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_HISTORY_TYPE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_HISTORY_VALUE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_IMAGE_URI;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_LAST_UPDATE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_NAME;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_PHONE;
import static com.rose.quickwallet.transactions.data.QuickWalletContract.QuickWalletEntries.COLUMN_QUICKBLOX_ID;

/**
 *
 * Created by rose on 24/7/15.
 */

public class DatabaseHelper {

    private final String TABLE_NAME = "QuickWallet";


    private SQLiteDatabase database;
    private String mCurrency;
    private ContentResolver mContentResolver;

    public DatabaseHelper(Context context){
        mCurrency = PreferenceManager.getDefaultSharedPreferences(context).getString("prefCurrency", "");
        mContentResolver = context.getContentResolver();
        database = new DatabaseOpenHelper(context).getWritableDatabase();
    }



    public void saveData(String name, String image_uri, Float amount,String type, String details, String phone, int userID){
        //database = openHelper.getWritableDatabase();
        Cursor cursor =getItem(name);
        if(cursor.moveToFirst()){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_BALANCE, cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE)) + amount);
            cv.put(COLUMN_IMAGE_URI, image_uri);
            cv.put(COLUMN_LAST_UPDATE, System.currentTimeMillis());
            cv.put(COLUMN_QUICKBLOX_ID,userID);
            cv.put(COLUMN_PHONE,phone);
            String selectionArgs[] = {name};
            mContentResolver.update(QuickWalletContract.QuickWalletEntries.CONTENT_URI,cv,COLUMN_NAME + " LIKE ?", selectionArgs);
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, name);
            cv.put(COLUMN_IMAGE_URI, image_uri);
            float newBalance = getBalance(name) + amount;
            cv.put(COLUMN_LAST_UPDATE,System.currentTimeMillis());
            cv.put(COLUMN_BALANCE, newBalance);
            cv.put(COLUMN_QUICKBLOX_ID,userID);
            cv.put(COLUMN_PHONE,phone);
            //cv.put(COLUMN_HISTORY, history);
            mContentResolver.insert(QuickWalletContract.QuickWalletEntries.CONTENT_URI,cv);
        }
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + removeIllegalCharacters(name) + "(" + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1," +COLUMN_HISTORY_TYPE + " TEXT," + COLUMN_HISTORY_VALUE + " REAL," + COLUMN_HISTORY_TIME + " INTEGER," + COLUMN_HISTORY_DETAIL + " TEXT)";
        database.execSQL(query);
        ContentValues cvHistory = new ContentValues();
        cvHistory.put(COLUMN_HISTORY_TYPE,type);
        cvHistory.put(COLUMN_HISTORY_VALUE, amount);
        cvHistory.put(COLUMN_HISTORY_DETAIL,details);
        cvHistory.put(COLUMN_HISTORY_TIME,System.currentTimeMillis()/1000);
        mContentResolver.insert(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon()
                .appendPath(removeIllegalCharacters(name)).build(),cvHistory);
        //database.close();
    }

    public float getBalance(String name){
        //database = openHelper.getReadableDatabase();
        String[] selectionArgs = {name};
        float balance;
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI, null, COLUMN_NAME + " LIKE ?", selectionArgs, null);
        if(cursor.moveToFirst()) {
            balance = cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE));
            cursor.close();
        }
        else
            balance = 0;
        //database.close();
        return balance;
    }

    public ArrayList<RecyclerViewItem> getData(){
        //String query = "SELECT * FROM "+ TABLE_NAME + " ORDER BY " + COLUMN_LAST_UPDATE + " DESC";
        ArrayList<RecyclerViewItem> dataSet = new ArrayList<>();
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI,null,null,null, COLUMN_LAST_UPDATE + " DESC");
        // This item is used to compensate for the list header so that the position of adapter and array list remain same
        dataSet.add(new RecyclerViewItem());
        if(cursor.moveToFirst()){
            do{
                RecyclerViewItem viewItem = new RecyclerViewItem();
                viewItem.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                viewItem.setImageUri(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI)));
                viewItem.setBalance(cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE)));
                viewItem.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_UPDATE)));
                viewItem.setLastTransaction(getLastTransaction(viewItem.getName()));
                dataSet.add(viewItem);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return dataSet;
    }

    public ArrayList<DetailsRecyclerViewItem> getHistoryData(String name){
        ArrayList<DetailsRecyclerViewItem> dataList = new ArrayList<>();
        //String query = "SELECT * FROM " + TABLE_NAME + removeIllegalCharacters(name) + " ORDER BY " + COLUMN_HISTORY_TIME + " DESC";
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon()
                .appendPath(removeIllegalCharacters(name)).build(),null,null,null,COLUMN_HISTORY_TIME + " DESC");
        if(cursor.moveToFirst()){
            do{
                DetailsRecyclerViewItem viewItem = new DetailsRecyclerViewItem();
                viewItem.setType(cursor.getString(cursor.getColumnIndex(COLUMN_HISTORY_TYPE)));
                viewItem.setAmount(cursor.getFloat(cursor.getColumnIndex(COLUMN_HISTORY_VALUE)));
                viewItem.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_HISTORY_TIME)));
                viewItem.setDetail(cursor.getString(cursor.getColumnIndex(COLUMN_HISTORY_DETAIL)));
                dataList.add(viewItem);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return dataList;
    }

    public ArrayList<RecyclerViewItem> search(String searchQuery){
        //String query = "SELECT * FROM  " + TABLE_NAME +" WHERE " + COLUMN_NAME + " LIKE ? ORDER BY " + COLUMN_LAST_UPDATE + " DESC";

        ArrayList<RecyclerViewItem> dataSet = new ArrayList<>();
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI,null,COLUMN_NAME + " LIKE ?"
                ,new String[] {searchQuery+"%"}, COLUMN_LAST_UPDATE + " DESC");
        dataSet.add(new RecyclerViewItem());
        if(cursor.moveToFirst()){
            do{
                RecyclerViewItem viewItem = new RecyclerViewItem();
                viewItem.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                viewItem.setImageUri(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI)));
                viewItem.setBalance(cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE)));
                viewItem.setLastTransaction(getLastTransaction(viewItem.getName()));
                dataSet.add(viewItem);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return dataSet;
    }

    public Cursor getItem(String name){
        String[] selectionArgs = {name};
        return mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI, null,COLUMN_NAME + " LIKE ?",selectionArgs,null);
    }

    public void updateItemOnSwipe(String name){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAST_UPDATE, Integer.MIN_VALUE);
        cv.put(COLUMN_BALANCE, 0);
        String selectionArgs[] = {name};
        mContentResolver.update(QuickWalletContract.QuickWalletEntries.CONTENT_URI, cv, COLUMN_NAME + " LIKE ?", selectionArgs);
        //Cursor cursor = getHistoryItem(name);
        ContentValues cvHistory = new ContentValues();
        cvHistory.put(COLUMN_HISTORY_TYPE, "Clear Balance");
        cvHistory.put(COLUMN_HISTORY_VALUE, 0);
        cvHistory.put(COLUMN_HISTORY_TIME,System.currentTimeMillis()/1000);
        /*if(cursor.moveToLast() && cursor.getString(cursor.getColumnIndex(COLUMN_HISTORY_TYPE)).equals("Clear Balance")) {
            return;
            //database.update(TABLE_NAME + name.replaceAll(" ",""),cv,null,null);
        }
        else{*/
            mContentResolver.insert(QuickWalletContract.QuickWalletEntries.CONTENT_URI
                    .buildUpon().appendPath(removeIllegalCharacters(name)).build(), cvHistory);
        //}
    }

    public void onUndoSwipe(RecyclerViewItem item){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAST_UPDATE, item.getTime());
        cv.put(COLUMN_BALANCE, item.getBalance());
        String selectionArgs[] = {item.getName()};
        mContentResolver.update(QuickWalletContract.QuickWalletEntries.CONTENT_URI, cv, COLUMN_NAME + " LIKE ?", selectionArgs);
        String whereClause = COLUMN_HISTORY_ID + "=" + "(SELECT MAX(" + COLUMN_HISTORY_ID + ") FROM " + TABLE_NAME +removeIllegalCharacters(item.getName()) + ")";
        mContentResolver.delete(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon()
                .appendPath(removeIllegalCharacters(item.getName())).build(), whereClause, null);

    }

    /*public Cursor getHistoryItem(String name){
        String query = "SELECT * FROM " + TABLE_NAME + removeIllegalCharacters(name);
        return database.rawQuery(query,null);
    }*/

    public void clearHistory(String name){
        //String query = "SELECT * FROM " + TABLE_NAME + name.replaceAll(" ","");
        mContentResolver.delete(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon()
                .appendPath(removeIllegalCharacters(name)).build(),null,null);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAST_UPDATE, Integer.MIN_VALUE);
        cv.put(COLUMN_BALANCE, 0);
        String selectionArgs[] = {name};
        mContentResolver.update(QuickWalletContract.QuickWalletEntries.CONTENT_URI, cv, COLUMN_NAME + " LIKE ?", selectionArgs);
    }

    public void deleteContact(String name){
        mContentResolver.delete(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon()
                .appendPath(removeIllegalCharacters(name)).build(), null, null);
        mContentResolver.delete(QuickWalletContract.QuickWalletEntries.CONTENT_URI, COLUMN_NAME + " LIKE ?", new String[]{name});
    }

    public String getLastTransaction(String name){
        //String query = "SELECT * FROM "  + TABLE_NAME + removeIllegalCharacters(name) + " ORDER BY " + COLUMN_HISTORY_TIME + " DESC LIMIT 1";
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon()
                .appendPath(removeIllegalCharacters(name)).build(),null,null,null, COLUMN_HISTORY_TIME + " DESC LIMIT 1");
        String lastTransaction = "Last Transaction:  ";
        if(cursor.moveToFirst()) {
            lastTransaction += cursor.getString(cursor.getColumnIndex(COLUMN_HISTORY_TYPE)) + " ";
            float balance = cursor.getFloat(cursor.getColumnIndex(COLUMN_HISTORY_VALUE));
            if(balance < 0)
                balance = -1 *balance;
            if(balance!=0)
                lastTransaction += mCurrency + balance;
        }
        cursor.close();
        return lastTransaction;
    }

    /**public ArrayList<RecyclerViewItem> getPendingBalances(){
        ArrayList<RecyclerViewItem> dataList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME,null);
        if(cursor.moveToFirst()){
            do{
                RecyclerViewItem item = new RecyclerViewItem();
                item.setBalance(cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE)));
            }while (cursor.moveToNext());
        }
        return dataList;
    }*/
    public void close(){
        if( database.isOpen())
            database.close();
    }

    public String removeIllegalCharacters(String name){
        //return name.replaceAll("[\\p{Punct}\\p{Blank}]","");
        return name.replaceAll("[\\W]","");
    }

    public float totalLent(){
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI,null, COLUMN_BALANCE + " > 0", null,null);
        float lent = 0;
        if(cursor.moveToFirst()){
            do{
                lent += cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return lent;
    }

    public float totalBorrowed(){
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI,null, COLUMN_BALANCE + " < 0", null,null);
        float lent = 0;
        if(cursor.moveToFirst()){
            do{
                lent += cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE));
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (lent == 0)
                return 0;
        else return -1 * lent;
    }

    public void onEditDetails(String type,String details,float amount, Long time, String name){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_HISTORY_DETAIL,details);
        contentValues.put(COLUMN_HISTORY_TYPE,type);
        contentValues.put(COLUMN_HISTORY_VALUE, amount);
        mContentResolver.update(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon().appendPath(removeIllegalCharacters(name)).build(),
                contentValues, COLUMN_HISTORY_TIME + " LIKE ?", new String[]{Long.toString(time)});
        updateBalance(name);
    }

    public void onDeleteTransactionDetails(Long time, String name){
        database.delete(TABLE_NAME + removeIllegalCharacters(name),COLUMN_HISTORY_TIME + " = " + time,null);
        updateBalance(name);
    }

    public float calculateBalance(String name){
        Cursor cursor = mContentResolver.query(QuickWalletContract.QuickWalletEntries.CONTENT_URI.buildUpon()
                .appendPath(removeIllegalCharacters(name)).build(),null,null,null,null);
        float balance = 0;
        if (cursor.moveToFirst()){
            do {
                float itemBalance =cursor.getFloat(cursor.getColumnIndex(COLUMN_HISTORY_VALUE));
                if(itemBalance==0)
                    balance = 0;
                else
                    balance += itemBalance;
            }while (cursor.moveToNext());
        }
        cursor.close();
        return balance;
    }

    public void updateBalance(String name){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BALANCE,calculateBalance(name));
        mContentResolver.update(QuickWalletContract.QuickWalletEntries.CONTENT_URI,cv,COLUMN_NAME + " LIKE ?", new String[]{name});
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }
}
