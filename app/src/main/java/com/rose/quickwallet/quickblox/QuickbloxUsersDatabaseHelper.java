package com.rose.quickwallet.quickblox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;

/**
 * Created by rose on 17/10/15.
 */
public class QuickbloxUsersDatabaseHelper {
    private final String DATABASE_NAME = "QuickbloxUsers.db";
    private final String TABLE_NAME = "Users";
    private final String COLUMN_PHONE = "PhoneNo";
    private final String COLUMN_QUICKBLOX_ID = "QuickbloxID";
    private int VERSION = 1;
    private SQLiteDatabase database;
    public QuickbloxUsersDatabaseHelper(Context context){
        UsersDatabaseOpenHelper openHelper = new UsersDatabaseOpenHelper(context);
        database = openHelper.getWritableDatabase();
    }
    class UsersDatabaseOpenHelper extends SQLiteOpenHelper {
        public UsersDatabaseOpenHelper(Context context) {
            super(context,DATABASE_NAME,null,VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + COLUMN_QUICKBLOX_ID + " INTEGER," + COLUMN_PHONE + " TEXT," + "PRIMARY KEY( " + COLUMN_QUICKBLOX_ID + "," + COLUMN_PHONE +"))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

   public void insertUser(String phone, int id){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUICKBLOX_ID,id);
        contentValues.put(COLUMN_PHONE, phone);
        try {
            database.insertOrThrow(TABLE_NAME,null,contentValues);
        }
        catch (SQLException e){

        }
    }

    public ArrayList<Integer> getUserID(String phone) {
        ArrayList<Integer> userIds = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)), phone))
                    userIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_QUICKBLOX_ID)));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return userIds;
    }

    public void closeDatabase(){
        if(database.isOpen())
            database.close();
    }
}
