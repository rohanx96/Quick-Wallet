package com.rose.quickwallet.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rose.quickwallet.R;
import com.rose.quickwallet.transactions.DetailsActivity;
import com.rose.quickwallet.transactions.RecyclerViewItem;
import com.rose.quickwallet.transactions.data.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by rohanx96 on 12/17/16.
 * This class loads the remote views for the collection widget
 */

public class TransactionsRemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TransactionsRemoteViewsFactory(this);
    }

    class TransactionsRemoteViewsFactory implements RemoteViewsFactory {
        private Context mContext;
        private DatabaseHelper mDatabaseHelper;
        private ArrayList<RecyclerViewItem> mDataList;
        private String mCurrency;

        TransactionsRemoteViewsFactory(Context context){
            super();
            this.mContext = context;
        }

        @Override
        public void onCreate() {
            mDatabaseHelper = new DatabaseHelper(mContext);
            mDataList = mDatabaseHelper.getData();
            mCurrency = PreferenceManager.getDefaultSharedPreferences(mContext).getString("prefCurrency","");
        }

        @Override
        public void onDataSetChanged() {
            onCreate();
        }

        @Override
        public void onDestroy() {
            mDatabaseHelper.close();
        }

        @Override
        public int getCount() {
            //Log.i("Widget ", mDataList.size() + "");
            return mDataList.size()-1;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RecyclerViewItem item = mDataList.get(i+1);
            //Log.i("Widget", item.getName());
            RemoteViews holder = new RemoteViews(mContext.getPackageName(), R.layout.widget_transactions_list_item);
            holder.setTextViewText(R.id.widget_item_name, item.getName());
            if(item.getBalance()<0){
                holder.setTextColor(R.id.widget_item_balance, Color.parseColor("#ffc94c4c"));
                holder.setTextViewText(R.id.widget_item_balance, "- " + mCurrency +  Float.toString(-1 * item.getBalance()));
            }
            else {
                holder.setTextColor(R.id.widget_item_balance, Color.parseColor("#ff509f4c"));
                holder.setTextViewText(R.id.widget_item_balance, "  "  + mCurrency + Float.toString(item.getBalance()));
            }
            Intent intent = new Intent(mContext, DetailsActivity.class);
            intent.putExtra("Name", item.getName());
            intent.putExtra("imageUri", item.getImageUri());
            intent.putExtra("position", i);
            holder.setOnClickFillInIntent(R.id.widget_list_item, intent);
            return holder;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
