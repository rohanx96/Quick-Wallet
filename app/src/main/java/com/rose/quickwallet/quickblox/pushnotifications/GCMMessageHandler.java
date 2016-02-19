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

import java.util.Currency;
import java.util.Locale;

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
        //Log.i("GCMNotification","Message received");
        //MESSAGE_NOTIFICATION_ID = count;
        count++;
        int ID = MESSAGE_NOTIFICATION_ID + count;
        preferences.edit().putInt("GCMNotificationID", ID).commit();
        //Log.i("GCMNotification", "Count: " + count);
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

        Intent addActivity = new Intent(this,AddNewTransactionActivity.class);

        final String messageValue = extras.getString("message");
        String message="";

        if (messageValue != null) {
            /* This message format is when the notification is received from an application version equal or above 6.0. The text received has
             * data separated by special characters which needs to be parsed to extract data and pass to AddTransactionActivity. For message
             * format look at createChatMessage method in AddTransactionActivity */
            if (messageValue.indexOf('<') != -1) {
                String name ="";
                String contact = "";
                float amount = 0;
                String details="";
                name = messageValue.substring(0, messageValue.indexOf(':'));
                //Log.i("name ",name);
                if (messageValue.indexOf('[') != -1) //{ // checks if details are present in message
                    //amount = Float.parseFloat(messageValue.substring(messageValue.indexOf('(') + 1, messageValue.indexOf(')')));
                    details = messageValue.substring(messageValue.indexOf('[') + 1, messageValue.indexOf(']'));
                if(messageValue.contains("lent"))
                    amount = Float.parseFloat(messageValue.substring(messageValue.indexOf('(') + 1, messageValue.indexOf(')')));
                else
                    amount = -1 * Float.parseFloat(messageValue.substring(messageValue.indexOf('(') + 1, messageValue.indexOf(')')));
                //Log.i("amount notification ",Float.toString(amount));
                //Log.i("details ",details);
                contact = messageValue.substring(messageValue.indexOf('<') + 1,messageValue.indexOf('>'));
                //Log.i("contact ",contact);

                message = name + " : ";
                if (amount < 0)
                    message += getString(R.string.gcm_noti_borrowed_message)  + Currency.getInstance(Locale.getDefault()).getSymbol()+ -1 * amount;
                else message += getString(R.string.gcm_noti_lent_message) + Currency.getInstance(Locale.getDefault()).getSymbol() +  amount;
                if (!details.equals(""))
                    message += getString(R.string.gcm_noti_details_message) + details;
                message += getString(R.string.gcm_noti_end_message);
                addActivity.putExtra("action","addNotification");
                addActivity.putExtra("contact",contact);
                addActivity.putExtra("details",details);
                addActivity.putExtra("amount",amount);
            }
            // For versions below 6.0. The message text did not have contact data and special characters. So it can be directly set as
            // notification text without parsing
            else{
                message = messageValue;
                addActivity.putExtra("action","generic");
            }
        }
        /*Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Consts.EXTRA_MESSAGE, messageValue);*/
        addActivity.setAction("notificationID " + System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this,ID, addActivity, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message).setSummaryText(getString(R.string.gcm_noti_summary)))
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
