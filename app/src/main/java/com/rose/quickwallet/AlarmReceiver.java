package com.rose.quickwallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by rose on 20/8/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 235632;
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent createNotification = new Intent(context,NotificationService.class);
        context.startService(createNotification);
    }
}
