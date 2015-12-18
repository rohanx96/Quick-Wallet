package com.rose.quickwallet;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.rose.quickwallet.transactions.DatabaseHelper;
import com.rose.quickwallet.transactions.MainActivity;
import com.rose.quickwallet.transactions.RecyclerViewItem;

import java.util.ArrayList;

/**
 * Created by rose on 15/8/15.
 */
public class NotificationService extends IntentService {
    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        ArrayList<RecyclerViewItem> dataList = databaseHelper.getData();
        Boolean pendingBalance = true;
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        int count = 0;
        float lentBalance=0;
        float borrowedBalance = 0;
        while(pendingBalance){
            if(count==dataList.size())
                break;
            float balance = dataList.get(count).getBalance();
            if(balance == 0)
                pendingBalance =false;
            else if(balance<0){
                borrowedBalance+=balance;
                String line = "Borrowed         " + dataList.get(count).getName();
                for(int i = 0 ; i<2*(19 - dataList.get(count).getName().length());i++)
                    line+=" ";
                line+=-1*balance;
                //inboxStyle.addLine("Borrowed      " + dataList.get(count).getName() + "           " + -1 * balance);
                inboxStyle.addLine(line);
            }
            else{
                lentBalance+=balance;
                String line = "Lent                  " + dataList.get(count).getName();
                for(int i = 0 ; i<2*(19 - dataList.get(count).getName().length());i++)
                    line+=" ";
                line+=balance;
                //inboxStyle.addLine("Lent                 " + dataList.get(count).getName() + "           " + balance);
                inboxStyle.addLine(line);
            }
            count++;
        }
        if(lentBalance!=0 || borrowedBalance!=0 ){
            Intent startApplication = new Intent(this,MainActivity.class);
            intent.putExtra("action","generic");
            inboxStyle.setSummaryText("Lent: " + lentBalance + "         Borrowed: " + -1 * borrowedBalance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setAutoCancel(true)
                    .setStyle(inboxStyle)
                    .setContentTitle("You have pending transactions")
                    .setContentText("Lent: " + lentBalance + "        Borrowed: " + -1*borrowedBalance)
                    .setContentIntent(PendingIntent.getActivity(this,0,startApplication,0))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setDefaults(Notification.DEFAULT_ALL);
            notificationManager.notify(23,notificationBuilder.build());
        }
    }
}
