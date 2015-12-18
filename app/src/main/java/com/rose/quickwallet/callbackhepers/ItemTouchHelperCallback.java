package com.rose.quickwallet.callbackhepers;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.rose.quickwallet.transactions.RecyclerAdapter;

/**
 * Created by rose on 28/7/15.
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback{
    private final ItemTouchHelperAdapter mAdapter;
    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter)
    {
        super(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
        mAdapter =adapter;

    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if(viewHolder instanceof RecyclerAdapter.ViewHolderHeader)
            return 0;
        else return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemSwiped(viewHolder.getAdapterPosition());
    }
}
