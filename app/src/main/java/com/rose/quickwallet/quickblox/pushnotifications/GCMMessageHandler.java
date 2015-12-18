package com.rose.quickwallet.quickblox.pushnotifications;

/**
 *
 * Created by rose on 9/10/15.
 */
import com.google.android.gms.gcm.GcmListenerService;
import com.rose.quickwallet.R;
import com.rose.quickwallet.transactions.AddNewTransactionActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GCMMessageHandler extends GcmListenerService {
    int MESSAGE_NOTIFICATION_ID;
    //int previousNotificationID;
    int count =0;

    public GCMMessageHandler(){
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MESSAGE_NOTIFICATION_ID = PreferenceManager.getDefaultSharedPreferences(this).getInt("GCMNotificationID",1);
        //previousNotificationID = PreferenceManager.getDefaultSharedPreferences(this).getInt("GCMNotificationID",1);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("GCMNotification","Message received");
        //MESSAGE_NOTIFICATION_ID = count;
        count++;
        int ID = MESSAGE_NOTIFICATION_ID + count;
        preferences.edit().putInt("GCMNotificationID", ID).commit();
        Log.i("GCMNotification", "Count: " + count);
        processNotification(from, data, ID);
    }

    // Creates notification based on title and body received
    /*private void createNotification(String title, String body) {
        Context context = getBaseContext();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title)
                .setContentText(body);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
    }*/

    private void processNotification(String type, Bundle extras, int ID) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final String messageValue = extras.getString("message");

        /*Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Consts.EXTRA_MESSAGE, messageValue);*/
        Intent addActivity = new Intent(this,AddNewTransactionActivity.class);
        addActivity.putExtra("action","generic");
        addActivity.setAction("notificationID " + System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this,ID, addActivity, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(messageValue)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageValue).setSummaryText("Tap to add transaction"))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentIntent(contentIntent);
        //Toast.makeText(this,"ID: " + MESSAGE_NOTIFICATION_ID,Toast.LENGTH_LONG).show();

        /*if(previousNotificationID >= MESSAGE_NOTIFICATION_ID)
            MESSAGE_NOTIFICATION_ID = previousNotificationID + count;
        previousNotificationID = MESSAGE_NOTIFICATION_ID;*/

        Log.i("GCMNotification", " ID: " + ID);
        notificationManager.notify(ID, mBuilder.build());


        // notify about new push
        //
        //Intent intentNewPush = new Intent(Consts.NEW_PUSH_EVENT);
       /* intentNewPush.putExtra(Consts.EXTRA_MESSAGE, messageValue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewPush);*/

        //Log.i(TAG, "Broadcasting event " + Consts.NEW_PUSH_EVENT + " with data: " + messageValue);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
