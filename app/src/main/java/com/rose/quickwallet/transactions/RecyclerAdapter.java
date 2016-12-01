package com.rose.quickwallet.transactions;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rose.quickwallet.R;
import com.rose.quickwallet.callbackhepers.ItemTouchHelperAdapter;
import com.rose.quickwallet.callbackhepers.RecyclerViewCallback;

import java.util.ArrayList;

/**
 *
 * Created by rose on 26/7/15.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter,OnClickListener {
    private RecyclerViewCallback recyclerViewCallback;
    private ArrayList<RecyclerViewItem> dataList;
    private Context context;
    private MainActivity activity;
    private int expandedPosition;
    private int lastPosition = -1;
    private String mCurrency;
    private boolean isTabletUI;

    public RecyclerAdapter(ArrayList<RecyclerViewItem> dataList,RecyclerViewCallback callback, Context context, boolean isTabletUI){
        this.dataList = dataList;
        this.context = context;
        this.recyclerViewCallback = callback;
        this.expandedPosition = Integer.MAX_VALUE;
        activity = (MainActivity) context;
        this.mCurrency = PreferenceManager.getDefaultSharedPreferences(this.context).getString("prefCurrency","");
        this.isTabletUI = isTabletUI;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType != 0)
            return new ViewHolderItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.recylcer_view_item_layout,parent,false));
        else
            return new ViewHolderHeader(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_header_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.v("RecyclerView","Got called for position " + position);/////////////////////////
        if(position!=0){
            RecyclerViewItem viewItem = dataList.get((position));///////////////////////////
            ViewHolderItem itemHolder = (ViewHolderItem) holder;
            setAnimation(itemHolder.container,position);
            itemHolder.nameView.setText(viewItem.getName());
            if (viewItem.getBalance() < 0) {
                itemHolder.balanceView.setText(context.getString(R.string.borrowed_colon)+ mCurrency +
                        Float.toString(-1 * viewItem.getBalance()));
                itemHolder.balanceView.setTextColor(Color.parseColor("#ffc94c4c"));
                itemHolder.paidView.setVisibility(View.GONE);
            }
            else if (viewItem.getBalance() > 0) {
                itemHolder.balanceView.setText(context.getString(R.string.lent_colon)+ mCurrency +
                        Float.toString(viewItem.getBalance()));
                itemHolder.balanceView.setTextColor(Color.parseColor("#ff509f4c"));
                itemHolder.paidView.setVisibility(View.GONE);
            }
            else if(viewItem.getBalance() == 0){
                itemHolder.balanceView.setText(context.getString(R.string.no_balance));
                itemHolder.paidView.setVisibility(View.VISIBLE);
                itemHolder.balanceView.setTextColor(Color.parseColor("#ff454545"));
            }
            if(viewItem.isExpanded()){
                itemHolder.buttonBar.setVisibility(View.VISIBLE);
                if(Build.VERSION.SDK_INT>=21)
                    itemHolder.imageView.setTransitionName("contactImage");
                //itemHolder.addButton.setVisibility(View.VISIBLE);
                //itemHolder.viewHistoryButton.setVisibility(View.VISIBLE);
            }
            else {
                itemHolder.buttonBar.setVisibility(View.GONE);
                if(Build.VERSION.SDK_INT>=21)
                    itemHolder.imageView.setTransitionName("null");
                //itemHolder.addButton.setVisibility(View.GONE);
                //itemHolder.viewHistoryButton.setVisibility(View.GONE);
            }
            if (viewItem.getImageUri() != null) {
            /*Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr, Uri.parse(viewItem.getImageUri()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(null, bitmap);
            drawable.setCornerRadius(5);
            drawable.setGravity(Gravity.CENTER);
            drawable.setFilterBitmap(true);
            holder.imageView.setImageDrawable(drawable);*/
                try{
                    itemHolder.imageView.setImageURI(Uri.parse(viewItem.getImageUri()));
                }
                catch (Exception e){
                    itemHolder.imageView.setImageResource(R.drawable.contact_no_image);
                }
            } else
                itemHolder.imageView.setImageResource(R.drawable.contact_no_image);
            itemHolder.lastTransactionView.setText(viewItem.getLastTransaction());
        }
        else {
            ViewHolderHeader header = (ViewHolderHeader) holder;
            header.headerImage.setImageResource(R.drawable.toolbar_background);
            setAnimation(header.container, position);
            header.totalLent.setText(context.getString(R.string.lent_colon)+ mCurrency + getTotalLent());
            header.totalBorrowed.setText(context.getString(R.string.borrowed_colon) + mCurrency + getTotalBorrowed());
            //Log.v("RecyclerView","Setting header image");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0)
            return 0;
        else return 1;
    }

    @Override
    public int getItemCount() {
        int itemCount = dataList.size();////////////////////////////////////
        Log.v("RecyclerView","getCount got called : returning " + itemCount);
        return itemCount;
    }

    @Override
    public void onItemSwiped(int position) {
        //Log.v("position",Integer.toString(position));
        if(position==0)
            return;
        else {
            RecyclerViewItem item = dataList.get((position));//////////////////
            RecyclerViewItem viewItem = new RecyclerViewItem();
            viewItem.setBalance(item.getBalance());
            viewItem.setType(item.getType());
            viewItem.setName(item.getName());
            viewItem.setImageUri(item.getImageUri());
            viewItem.setTime(item.getTime());
            viewItem.setAmount(item.getAmount());
            item.setIsExpanded(false);
            item.setLastTransaction(context.getString(R.string.last_trans_clear_balance));
            dataList.remove(position);///////////////////////
            //recyclerViewCallback.removeItem(position);
            item.setBalance(0);
            notifyItemRemoved(position);
            //notifyItemRangeChanged(position, dataList.size());
            /*if(expandedPosition>position && expandedPosition!=Integer.MAX_VALUE )
                expandedPosition--;**/
            //Log.i("RecyclerView", "Removed item at" + position);
            //Log.i("RecyclerView", "Item Name" + item.getName());
            dataList.add(item);
            notifyItemInserted(dataList.size() - 1);/////////////////////////////
            //notifyItemMoved(position,dataList.size());
            /*DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.updateItemOnSwipe(item.getName());
            databaseHelper.close();*/
            //item.setBalance(balance);
            //viewItem.setBalance(balance);
            recyclerViewCallback.onItemSwiped(viewItem);

        }
    }

    @Override
    public void onItemMoved(int position) {

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(context,DetailsActivity.class);
        context.startActivity(intent);
    }

    public class ViewHolderItem extends RecyclerView.ViewHolder implements OnClickListener{
        private TextView nameView;
        private TextView balanceView;
        private ImageView imageView;
        private ImageView paidView;
        private TextView lastTransactionView;
        private Button addButton;
        private Button viewHistoryButton;
        private LinearLayout buttonBar;
        private android.support.v7.widget.CardView container;
        public ViewHolderItem(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameView = (TextView) itemView.findViewById(R.id.recycler_name);
            balanceView = (TextView) itemView.findViewById(R.id.recycler_balance);
            imageView = (ImageView) itemView.findViewById(R.id.recycler_image);
            paidView = (ImageView) itemView.findViewById(R.id.recycler_paid);
            addButton = (Button) itemView.findViewById(R.id.add_button_recycler);
            container = (android.support.v7.widget.CardView)itemView.findViewById(R.id.recycler_view_item_container);
            addButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent addIntent = new Intent(context,AddNewTransactionActivity.class);
                    addIntent.setAction(Intent.ACTION_SEARCH);
                    addIntent.putExtra("action","add");
                    addIntent.putExtra(SearchManager.QUERY,getDataList().get(getAdapterPosition()).getName());//////////////////////
                    context.startActivity(addIntent);
                    activity.overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.stay);
                }
            });

            lastTransactionView = (TextView) itemView.findViewById(R.id.recycler_last_transaction);
            buttonBar = (LinearLayout) itemView.findViewById(R.id.button_bar);
        }

        @Override
        public void onClick(View view) {
            Log.i("Recycler View", "Clicked on position " + getAdapterPosition());
            if(!isTabletUI) {
                if (expandedPosition != getAdapterPosition() && expandedPosition != Integer.MAX_VALUE) {
                    getDataList().get(expandedPosition).setIsExpanded(false);/////////////////////
                    notifyItemChanged(expandedPosition);
                }
                getDataList().get(getAdapterPosition()).toggleIsExpanded();/////////////////////////////////
                expandedPosition = getAdapterPosition();
                //notifyDataSetChanged();
                notifyItemChanged(getAdapterPosition());
                final ImageView sharedImageView = (ImageView) view.findViewById(R.id.recycler_image);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sharedImageView.setTransitionName("contactImage" + getAdapterPosition());
                }
                viewHistoryButton = (Button) view.findViewById(R.id.view_history_button_recycler);
                viewHistoryButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(context, DetailsActivity.class);
                        String name = getDataList().get((getAdapterPosition())).getName();///////////////////////////
                        intent.putExtra("Name", name);
                        intent.putExtra("imageUri", getDataList().get((getAdapterPosition())).getImageUri());/////////////////////////
                        intent.putExtra("position", getAdapterPosition());
                        Log.i("imageTAG", " position" + getAdapterPosition());
                        if (Build.VERSION.SDK_INT >= 21)
                            context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedImageView, "contactImage" + getAdapterPosition()).toBundle());
                        else
                            context.startActivity(intent);
                        buttonBar.setVisibility(View.GONE);

                    }
                });
            }
            else {
                DetailsFragment fragment = new DetailsFragment();
                Bundle args = new Bundle();
                args.putString("Name",getDataList().get((getAdapterPosition())).getName());
                args.putString("imageUri", getDataList().get((getAdapterPosition())).getImageUri());
                args.putInt("position", getAdapterPosition());
                fragment.setArguments(args);
                activity.findViewById(R.id.details_fragment_disabled_text).setVisibility(View.GONE);
                activity.findViewById(R.id.container_details_fragment_main).setVisibility(View.VISIBLE);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.container_details_fragment_main,fragment).commit();
            }
            /*Intent intent = new Intent(context,DetailsActivity.class);
            String name = getDataList().get((getAdapterPosition()-1)).getName();
            intent.putExtra("Name",name);
            intent.putExtra("imageUri",getDataList().get((getAdapterPosition()-1)).getImageUri());
            context.startActivity(intent);*/
        }
    }
    public class ViewHolderHeader extends RecyclerView.ViewHolder {
        private ImageView headerImage;
        private TextView totalLent;
        private TextView totalBorrowed;
        private RelativeLayout container;
        public ViewHolderHeader(View itemView) {
            super(itemView);
            headerImage = (ImageView) itemView.findViewById(R.id.header_image);
            totalLent = (TextView) itemView.findViewById(R.id.lent_header);
            totalBorrowed = (TextView) itemView.findViewById(R.id.borrowed_header);
            container = (RelativeLayout) itemView.findViewById(R.id.recycler_view_header_container);
            headerImage.setFocusable(false);
            headerImage.setFocusableInTouchMode(false);
            headerImage.setEnabled(false);
        }
    }

    private void setAnimation(View viewToAnimate, final int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            if (position == 0){
                //viewToAnimate.setVisibility(View.VISIBLE);
                //viewToAnimate.animate().translationY(viewToAnimate.getHeight()).alpha(1.0f).setDuration(1000);
                /*Animation a = new ScaleAnimation(1, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float) 0.5,    Animation.RELATIVE_TO_SELF, (float) 0);
                a.setFillAfter(true);
                a.setInterpolator(new AccelerateDecelerateInterpolator());
                viewToAnimate.setAnimation(a);
                a.setDuration(400);
                viewToAnimate.startAnimation(a);*/
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
                return;
            }
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            animation.setDuration(400);
            animation.setInterpolator(context, android.R.interpolator.accelerate_decelerate);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public ArrayList<RecyclerViewItem> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<RecyclerViewItem> dataList) {
        this.dataList = dataList;
    }

    /*public void refreshDataList(ArrayList<RecyclerViewItem> dataList, boolean shouldAnimate){
        if(shouldAnimate){
            this.dataList.clear();
            for( int i=1;i<dataList.size();i++) {////////////////////
                this.dataList.add(dataList.get(i));
                notifyItemInserted(i);
            }
        }
        else {
            this.dataList = dataList;
            //notifyDataSetChanged();
        }
    }*/

    public void setmCurrency(String mCurrency) {
        this.mCurrency = mCurrency;
    }

    public float getTotalLent(){
        float lent=0;
        for (RecyclerViewItem item:dataList)
        {
            if(item.getBalance()>0){
                lent+=item.getBalance();
            }
        }
        return lent;
    }

    public float getTotalBorrowed(){
        float borrowed=0;
        for (RecyclerViewItem item:dataList)
        {
            if(item.getBalance()<0){
                borrowed+=item.getBalance();
            }
        }
        if (borrowed == 0)
            return borrowed;
        else
            return -1*borrowed;
    }

}
