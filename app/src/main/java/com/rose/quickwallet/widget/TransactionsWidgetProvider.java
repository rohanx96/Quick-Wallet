package com.rose.quickwallet.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.rose.quickwallet.R;
import com.rose.quickwallet.transactions.AddNewTransactionActivity;
import com.rose.quickwallet.transactions.DetailsActivity;
import com.rose.quickwallet.transactions.MainActivity;

/**
 * Created by rohanx96 on 12/17/16.
 */

public class TransactionsWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_UPDATE_WIDGET = "com.rose.quickwallet.updateWidget";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_UPDATE_WIDGET)){
            ComponentName widget = new ComponentName(context.getPackageName(),TransactionsWidgetProvider.class.getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(widget),R.id.widget_transactions_list);
        }

        else super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("Widget", " Loading");
        for (int appWidgetID : appWidgetIds) {

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_collection_list);
            /* Set the click on the widget header to open the main activity */
            Intent mainActivity = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,appWidgetID,mainActivity,0);
            widget.setOnClickPendingIntent(R.id.widget_transactions_heading, pendingIntent);

            Intent addTransaction = new Intent(context, AddNewTransactionActivity.class);
            addTransaction.putExtra("action","generic");
            PendingIntent addIntent = PendingIntent.getActivity(context, appWidgetID, addTransaction, 0);
            widget.setOnClickPendingIntent(R.id.widget_add_transaction, addIntent);

            Intent remoteViewService = new Intent(context,TransactionsRemoteViewService.class);
            widget.setRemoteAdapter(R.id.widget_transactions_list, remoteViewService);
            Log.i("Widget", " Loading");
            /* Set the intent template to be used by list items in the widget */
            Intent details = new Intent(context,DetailsActivity.class);
            PendingIntent pIntent =PendingIntent.getActivity(context,0,details,PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setPendingIntentTemplate(R.id.widget_transactions_list,pIntent);

            appWidgetManager.updateAppWidget(appWidgetID,widget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetID, R.id.widget_transactions_list);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_collection_list);
        Intent remoteViewService = new Intent(context,TransactionsRemoteViewService.class);
        widget.setRemoteAdapter(R.id.widget_transactions_list, remoteViewService);
        appWidgetManager.updateAppWidget(appWidgetId,widget);
    }

    public static void sendRefreshWidgetBroadcast(Context context){
        Intent updateWidget = new Intent(context, TransactionsWidgetProvider.class);
        updateWidget.setAction(TransactionsWidgetProvider.ACTION_UPDATE_WIDGET);
        context.sendBroadcast(updateWidget);
    }
}
