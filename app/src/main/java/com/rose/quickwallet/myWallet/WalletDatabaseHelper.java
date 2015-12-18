package com.rose.quickwallet.myWallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by rose on 19/8/15.
 */
public class WalletDatabaseHelper {

    private final String DATABASE_NAME = "MyWallet.db";
    private final String TABLE_NAME = "MyWallet";
    private final String COLUMN_TYPE = "Type";
    private final String COLUMN_DATE = "Date";
    private final String COLUMN_DETAILS = "Details";
    private final String COLUMN_AMOUNT = "Amount";
    private final String COLUMN_BALANCE = "Balance";
    private final int VERSION = 1;
    private SQLiteDatabase database;
    private final long  MILLIS_IN_DAY = 86400;

    public WalletDatabaseHelper(Context context){
        WalletDatabaseOpenHelper openHelper = new WalletDatabaseOpenHelper(context);
    }

    public class WalletDatabaseOpenHelper extends SQLiteOpenHelper{

        public WalletDatabaseOpenHelper(Context context){
            super(context,DATABASE_NAME,null,VERSION);
            database = getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(" + COLUMN_TYPE + " TEXT," + COLUMN_DETAILS + " TEXT," + COLUMN_AMOUNT + " REAL," + COLUMN_BALANCE + " REAL," + COLUMN_DATE + " INTEGER)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    public void saveItemToDatabase(String type,String details,float amount){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TYPE,type);
        contentValues.put(COLUMN_DETAILS,details);
        contentValues.put(COLUMN_AMOUNT,amount);
        contentValues.put(COLUMN_DATE,Calendar.getInstance().getTimeInMillis()/1000);
        contentValues.put(COLUMN_BALANCE,(getBalance() + amount));
        database.insert(TABLE_NAME,null,contentValues);
    }

    public float getBalance(){
        //database = openHelper.getReadableDatabase();
        float balance;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DATE + " DESC LIMIT 1",null);
        if(cursor.moveToFirst()) {
            balance = cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE));
            cursor.close();
        }
        else
            balance = 0;
        //database.close();
        return balance;
    }

    public ArrayList<WalletItem> getData(){
        ArrayList<WalletItem> dataList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DATE + " DESC",null);
        long time = 0;
        if(cursor.moveToFirst()){
            //time = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
            do{
                if(!isSameDay(time,cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)))){
                    time = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
                    WalletItem walletItem = new WalletItem();
                    walletItem.setType("Date");
                    walletItem.setDate(time);
                    dataList.add(walletItem);
                }
                WalletItem walletItem = new WalletItem();
                walletItem.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                walletItem.setDetails(cursor.getString(cursor.getColumnIndex(COLUMN_DETAILS)));
                walletItem.setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)));
                //time = walletItem.getDate();
                walletItem.setAmount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                walletItem.setBalance(cursor.getFloat(cursor.getColumnIndex(COLUMN_BALANCE)));
                dataList.add(walletItem);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return dataList;
    }

    public float getTodaysExpense(){
        float expense = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DATE + " DESC",null);
        if(cursor.moveToFirst()){
            do{
                if(isSameDay(Calendar.getInstance().getTimeInMillis()/1000,cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)))){
                    if(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)).equals("Expense"))
                        expense += -1 * cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT));
                }
                else
                    break;
            }while (cursor.moveToNext());
        }
        cursor.close();
        return expense;
    }
    public float getTodaysIncome(){
        float expense = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DATE + " DESC",null);
        if(cursor.moveToFirst()){
            do{
                if(isSameDay(Calendar.getInstance().getTimeInMillis()/1000,cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)))){
                    if(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)).equals("Income"))
                        expense += cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT));
                }
                else
                    break;
            }while (cursor.moveToNext());
        }
        cursor.close();
        return expense;
    }
    public boolean isSameDay(long time1,long time2){
        Date date1 = new Date(time1*1000);
        Date date2 = new Date(time2*1000);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
        //return (time1/MILLIS_IN_DAY == time2/MILLIS_IN_DAY);

    }

    public void clearHistory(){
        database.delete(TABLE_NAME,null,null);
    }
    public void close(){
        if(database.isOpen())
            database.close();
    }

    public void onEditTransactionDetails(Long time, String type, String details, Float amount){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_AMOUNT,amount);
        cv.put(COLUMN_DETAILS,details);
        cv.put(COLUMN_TYPE,type);
        database.update(TABLE_NAME, cv, COLUMN_DATE + " LIKE ?", new String[]{Long.toString(time)});
    }

    public float calculateBalance(){
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME,null);
        float balance = 0;
        if (cursor.moveToFirst()){
            do {
                float itemBalance =cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT));
                if(itemBalance==0)
                    balance = 0;
                else
                    balance +=itemBalance;
            }while (cursor.moveToNext());
        }
        cursor.close();
        return balance;
    }

    public void onDeleteTransactionDetails(Long time){
        database.delete(TABLE_NAME, COLUMN_DATE + " = " + time, null);
    }
}
