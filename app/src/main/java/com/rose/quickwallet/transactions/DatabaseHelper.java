package com.rose.quickwallet.transactions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

/**
 *
 * Created by rose on 24/7/15.
 */
public class DatabaseHelper {
    private final String DATABASE_NAME = "QuickWallet.db";
    private final String TABLE_NAME = "QuickWallet";
    private final String COLUMN_NAME = "Name";
    private final String COLUMN_IMAGE_URI = "ImageUri";
    private final String COLUMN_BALANCE = "Balance";
    private final String COLUMN_LAST_UPDATE = "Updated";
    private final String COLUMN_PHONE = "PhoneNo";
    private final String COLUMN_QUICKBLOX_ID = "QuickbloxID";
    private final String COLUMN_HISTORY_TYPE = "Type";
    private final String COLUMN_HISTORY_VALUE = "Value";
    private final String COLUMN_HISTORY_TIME = "Time";
    private final String COLUMN_HISTORY_DETAIL = "Details";
    private final String COLUMN_HISTORY_ID = "ID";
    private int VERSION = 2;
    private SQLiteDatabase database;
    private String mCurrency;

    public DatabaseHelper(Context context){
        DatabaseOpenHelper openHelper = new DatabaseOpenHelper(context);
        mCurrency = PreferenceManager.getDefaultSharedPreferences(context).getString("prefCurrency", "");
    }

    public class DatabaseOpenHelper extends SQLiteOpenHelper{

        public DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
            database = getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String query = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_NAME + " TEXT UNIQUE NOT NULL," + COLUMN_IMAGE_URI + " TEXT," + COLUMN_BALANCE + " REAL," + COLUMN_LAST_UPDATE + " INTEGER," + COLUMN_PHONE + " TEXT," + COLUMN_QUICKBLOX_ID +" INTEGER DEFAULT -1)";
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
            database.update(TABLE_NAME,cv,COLUMN_NAME + " LIKE ?",selectionArgs);
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
            database.insert(TABLE_NAME, null, cv);
        }
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + removeIllegalCharacters(name) + "(" + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1," +COLUMN_HISTORY_TYPE + " TEXT," + COLUMN_HISTORY_VALUE + " REAL," + COLUMN_HISTORY_TIME + " INTEGER," + COLUMN_HISTORY_DETAIL + " TEXT)";
        database.execSQL(query);
        ContentValues cvHistory = new ContentValues();
        cvHistory.put(COLUMN_HISTORY_TYPE,type);
        cvHistory.put(COLUMN_HISTORY_VALUE, amount);
        cvHistory.put(COLUMN_HISTORY_DETAIL,details);
        cvHistory.put(COLUMN_HISTORY_TIME,System.currentTimeMillis()/1000);
        database.insert(TABLE_NAME + removeIllegalCharacters(name), null, cvHistory);
        //database.close();
    }

    public float getBalance(String name){
        //database = openHelper.getReadableDatabase();
        String[] selectionArgs = {name};
        float balance;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + " LIKE ?", selectionArgs);
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
        String query = "SELECT * FROM "+ TABLE_NAME + " ORDER BY " + COLUMN_LAST_UPDATE + " DESC";
        ArrayList<RecyclerViewItem> dataSet = new ArrayList<>();
        Cursor cursor = database.rawQuery(query, null);
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
        String query = "SELECT * FROM " + TABLE_NAME + removeIllegalCharacters(name) + " ORDER BY " + COLUMN_HISTORY_TIME + " DESC";
        Cursor cursor = database.rawQuery(query, null);
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
        String query = "SELECT * FROM " + TABLE_NAME +" WHERE " + COLUMN_NAME + " LIKE ? ORDER BY " + COLUMN_LAST_UPDATE + " DESC";

        ArrayList<RecyclerViewItem> dataSet = new ArrayList<>();
        Cursor cursor = database.rawQuery(query,new String[] {searchQuery+"%"});
        if(cursor.moveToFirst()){
            do{
                RecyclerViewItem viewItem = new RecyclerViewItem();
                viewItem.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                viewItem.setImageUri(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI)));
                viewItem.setBalance(cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE)));
                dataSet.add(viewItem);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return dataSet;
    }

    public Cursor getItem(String name){
        String[] selectionArgs = {name};
        return database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + " LIKE ?", selectionArgs);
    }

    public void updateItemOnSwipe(String name){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAST_UPDATE, Integer.MIN_VALUE);
        cv.put(COLUMN_BALANCE, 0);
        String selectionArgs[] = {name};
        database.update(TABLE_NAME, cv, COLUMN_NAME + " LIKE ?", selectionArgs);
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
            database.insert(TABLE_NAME + removeIllegalCharacters(name), null, cvHistory);
        //}
    }

    public void onUndoSwipe(RecyclerViewItem item){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAST_UPDATE, item.getTime());
        cv.put(COLUMN_BALANCE, item.getBalance());
        String selectionArgs[] = {item.getName()};
        database.update(TABLE_NAME, cv, COLUMN_NAME + " LIKE ?", selectionArgs);
        String whereClause = COLUMN_HISTORY_ID + "=" + "(SELECT MAX(" + COLUMN_HISTORY_ID + ") FROM " + TABLE_NAME +removeIllegalCharacters(item.getName()) + ")";
        database.delete(TABLE_NAME + removeIllegalCharacters(item.getName()), whereClause, null);

    }

    /*public Cursor getHistoryItem(String name){
        String query = "SELECT * FROM " + TABLE_NAME + removeIllegalCharacters(name);
        return database.rawQuery(query,null);
    }*/

    public void clearHistory(String name){
        //String query = "SELECT * FROM " + TABLE_NAME + name.replaceAll(" ","");
        database.delete(TABLE_NAME + removeIllegalCharacters(name),null,null);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAST_UPDATE, Integer.MIN_VALUE);
        cv.put(COLUMN_BALANCE, 0);
        String selectionArgs[] = {name};
        database.update(TABLE_NAME, cv, COLUMN_NAME + " LIKE ?", selectionArgs);
    }

    public void deleteContact(String name){
        database.delete(TABLE_NAME + removeIllegalCharacters(name), null, null);
        database.delete(TABLE_NAME, COLUMN_NAME + " LIKE ?", new String[]{name});
    }

    public String getLastTransaction(String name){
        String query = "SELECT * FROM "  + TABLE_NAME + removeIllegalCharacters(name) + " ORDER BY " + COLUMN_HISTORY_TIME + " DESC LIMIT 1";
        Cursor cursor = database.rawQuery(query, null);
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
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_BALANCE + " > 0", null);
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
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_BALANCE + " < 0", null);
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
        database.update(TABLE_NAME + removeIllegalCharacters(name), contentValues, COLUMN_HISTORY_TIME + " LIKE ?", new String[]{Long.toString(time)});
        updateBalance(name);
    }

    public void onDeleteTransactionDetails(Long time, String name){
        database.delete(TABLE_NAME + removeIllegalCharacters(name),COLUMN_HISTORY_TIME + " = " + time,null);
        updateBalance(name);
    }

    public float calculateBalance(String name){
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + removeIllegalCharacters(name),null);
        float balance = 0;
        if (cursor.moveToFirst()){
            do {
                float itemBalance =cursor.getFloat(cursor.getColumnIndex(COLUMN_HISTORY_VALUE));
                if(itemBalance==0)
                    balance = 0;
                else
                    balance +=itemBalance;
            }while (cursor.moveToNext());
        }
        cursor.close();
        return balance;
    }

    public void updateBalance(String name){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BALANCE,calculateBalance(name));
        database.update(TABLE_NAME,cv,COLUMN_NAME + " LIKE ?", new String[]{name});
    }
}
