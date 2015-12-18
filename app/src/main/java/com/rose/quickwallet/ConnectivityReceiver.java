package com.rose.quickwallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.rose.quickwallet.quickblox.RetreiveUsersService;

/**
 * Created by rose on 19/7/15.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        //boolean connecting = true;
        while(true) {
            if(netInfo!=null && netInfo.isConnectedOrConnecting()){
                if(netInfo.isConnected()) {
                    Log.v("Service", "Refreshing started");
                    Intent retrieveUsers = new Intent(context, RetreiveUsersService.class);
                    retrieveUsers.putExtra("createSession", true);
                    context.startService(retrieveUsers);
                    /*Intent sendNotifications = new Intent(context, SendNotificationsService.class);
                    sendNotifications.putExtra("createSession", true);
                    context.startService(sendNotifications);*/
                    break;
                }
            }
            else break;
        }
    }
}
