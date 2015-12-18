package com.rose.quickwallet.callbackhepers;

import android.view.View;

import com.rose.quickwallet.transactions.RecyclerViewItem;

/**
 * Created by rose on 30/7/15.
 */
public interface RecyclerViewCallback {
    void onItemSwiped(RecyclerViewItem item);
    int clickedItemPosition(View v);
    void removeItem(int position);
}
