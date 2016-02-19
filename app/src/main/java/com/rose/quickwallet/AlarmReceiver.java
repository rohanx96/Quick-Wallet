package com.rose.quickwallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receives alarm set for notifications
 * Created by rose on 20/8/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 235632;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("alarm receiver", " received alarm");
        Intent createNotification = new Intent(context,NotificationService.class);
        context.startService(createNotification);
    }
}
