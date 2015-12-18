package com.rose.quickwallet.transactions;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.rose.quickwallet.R;
import com.rose.quickwallet.callbackhepers.DetailsRecyclerViewCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * Created by rose on 3/8/15.
 *
 */
public class DetailsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<DetailsRecyclerViewItem> dataList;
    private DetailsRecyclerViewCallback callback;
    private Context context;
    private String name;

    public DetailsRecyclerViewAdapter(ArrayList<DetailsRecyclerViewItem> dataList, Context context, String name){
        super();
        this.dataList = dataList;
        this.context = context;
        this.name = name;
        callback = (DetailsRecyclerViewCallback) context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DetailsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.details_recycler_view_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        DetailsViewHolder viewHolder = (DetailsViewHolder) holder;
        if(dataList.get(position).getDetail() == null || dataList.get(position).getDetail().equals(""))
            viewHolder.detailsDescription.setText("No details specified");
        else viewHolder.detailsDescription.setText(dataList.get(position).getDetail());
        if(dataList.get(position).getType().equals("Lent")) {
            bindLent(viewHolder);
            viewHolder.detailsAmount.setText(Float.toString(dataList.get(position).getAmount()));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClicked(position);
                }
            });
        }
        if(dataList.get(position).getType().equals("Borrowed")) {
            bindBorrowed(viewHolder);
            viewHolder.detailsAmount.setText(Float.toString(-1 * dataList.get(position).getAmount()));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClicked(position);
                }
            });
        }
        if(dataList.get(position).getType().equals("Clear Balance")) {
            bindClearBalance(viewHolder);
            viewHolder.detailsAmount.setText("");
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                    builder.setItems(R.array.transaction_clear_balance_click_dialog_options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                                databaseHelper.onDeleteTransactionDetails(dataList.get(position).getTime(), name);
                                dataList.remove(position);
                                notifyItemRemoved(position);
                                databaseHelper.close();
                                callback.onDeleteTransaction();
                            }
                        }
                    });
                    builder.show();
                }
            });
        }
        //viewHolder.detailsAmount.setText(Float.toString(dataList.get(position).getAmount()));
        String date = new SimpleDateFormat("d MMM, HH:mm").format(new Date((dataList.get(position).getTime() * 1000)));
        viewHolder.detailsDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void bindLent(DetailsViewHolder viewHolder){
        //DetailsViewHolder viewHolder = (DetailsViewHolder) holder;
        viewHolder.detailsType.setText("Lent");
        viewHolder.detailsTypeIcon.setImageResource(R.drawable.lent_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }

    public void bindBorrowed(DetailsViewHolder viewHolder){
        //DetailsViewHolder viewHolder = (DetailsViewHolder) holder;
        viewHolder.detailsType.setText("Borrowed");
        viewHolder.detailsTypeIcon.setImageResource(R.drawable.borrowed_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }
    public void bindClearBalance(DetailsViewHolder viewHolder){
        //DetailsViewHolder viewHolder = (DetailsViewHolder) holder;
        viewHolder.detailsType.setText("Clear Balance");
        viewHolder.detailsTypeIcon.setImageResource(R.drawable.paid);
        //viewHolder.viewHolderContainer.setBackgroundResource(R.drawable.oval_background);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
        viewHolder.detailsDescription.setText("");
    }

    public void onItemClicked(final int position){
        android.support.v7.app.AlertDialog.Builder dialogBuilder= new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper(context,R.style.myDialog));
        dialogBuilder.setItems(R.array.transaction_click_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    DetailsRecyclerViewItem item = dataList.get(position);
                    Intent editItem = new Intent(context, EditTransactionDetails.class);
                    editItem.putExtra("NAME", name);
                    editItem.putExtra("TIME", item.getTime());
                    editItem.putExtra("DETAILS",item.getDetail());
                    editItem.putExtra("TYPE",item.getType());
                    editItem.putExtra("AMOUNT",item.getAmount());
                    editItem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(editItem);
                    dialog.dismiss();
                }
                else if (which==1){
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    databaseHelper.onDeleteTransactionDetails(dataList.get(position).getTime(), name);
                    dataList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(0,dataList.size());
                    dialog.dismiss();
                    databaseHelper.close();
                    callback.onDeleteTransaction();
                }
            }
        });
        dialogBuilder.show();

    }

    public class DetailsViewHolder extends RecyclerView.ViewHolder{
        private TextView detailsType;
        private TextView detailsDate;
        private TextView detailsAmount;
        private ImageView detailsTypeIcon;
        private TextView detailsDescription;
        private RelativeLayout viewHolderContainer;
        public DetailsViewHolder(View itemView) {
            super(itemView);
            viewHolderContainer= (RelativeLayout) itemView;
            detailsType = (TextView) itemView.findViewById(R.id.details_type_text);
            detailsAmount = (TextView) itemView.findViewById(R.id.details_amount_text);
            detailsDate =(TextView) itemView.findViewById(R.id.details_date_text);
            detailsDescription = (TextView) itemView.findViewById(R.id.details_description_text);
            detailsTypeIcon = (ImageView) itemView.findViewById(R.id.details_type_icon);
        }
    }

    public ArrayList<DetailsRecyclerViewItem> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<DetailsRecyclerViewItem> dataList) {
        this.dataList = dataList;
    }
}
