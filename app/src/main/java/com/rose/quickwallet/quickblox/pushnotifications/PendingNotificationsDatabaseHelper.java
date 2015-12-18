package com.rose.quickwallet.quickblox.pushnotifications;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by rose on 23/10/15.
 */
public class PendingNotificationsDatabaseHelper {
    private final String TABLE_NAME = "PendingNotifications";
    private final String DATABASE_NAME = "jobs.db";
    private final String COLUMN_MSG = "msg";
    private final String COLUMN_OPPONENT_ID = "id";
    private final String COLUMN_TIME = "time";
    private final int DATABASE_VERSION = 1;
    private SQLiteDatabase database;

    public PendingNotificationsDatabaseHelper(Context context){
        PendingNotificationsOpenHelper openHelper = new PendingNotificationsOpenHelper(context);
        database = openHelper.getWritableDatabase();
    }

    private class PendingNotificationsOpenHelper extends SQLiteOpenHelper {
        public PendingNotificationsOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + COLUMN_OPPONENT_ID + " INTEGER," + COLUMN_MSG + " TEXT," + COLUMN_TIME + " INTEGER)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public void insertNotification(String msg, int id){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_OPPONENT_ID, id);
        cv.put(COLUMN_MSG,msg);
        cv.put(COLUMN_TIME,System.currentTimeMillis()/1000);
        database.insert(TABLE_NAME,null,cv);
    }

    public void closeDatabase(){
        if(database.isOpen())
            database.close();
    }

    public ArrayList<QBEvent> getPendingNotifications(){
        ArrayList<QBEvent> events = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()){
            do {
                QBEvent event = new QBEvent();
                StringifyArrayList<Integer> userIDs = new StringifyArrayList<>();
                userIDs.add((cursor.getInt(cursor.getColumnIndex(COLUMN_OPPONENT_ID))));
                event.setMessage(cursor.getString(cursor.getColumnIndex(COLUMN_MSG)));
                //event.setUserId(cursor.getInt(cursor.getColumnIndex(COLUMN_OPPONENT_ID)));
                event.setUserIds(userIDs);
                event.setName(Integer.toString(cursor.getInt(cursor.getColumnIndex(COLUMN_TIME))));
                event.setEnvironment(QBEnvironment.PRODUCTION);
                event.setNotificationType(QBNotificationType.PUSH);
                events.add(event);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    public void deleteEvent(long time){
        database.delete(TABLE_NAME,COLUMN_TIME + " = " + time,null);
    }
}
