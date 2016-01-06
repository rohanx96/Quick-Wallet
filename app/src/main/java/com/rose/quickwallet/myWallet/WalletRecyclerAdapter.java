package com.rose.quickwallet.myWallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
 * Created by rose on 20/8/15.
 */
public class WalletRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<WalletItem> dataList;
    private Context context;
    private DetailsRecyclerViewCallback callback;

    public WalletRecyclerAdapter(ArrayList<WalletItem> dataList, Context context){
        super();
        this.dataList = dataList;
        this.context = context;
        this.callback = (DetailsRecyclerViewCallback) context;

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==0)
            return new WalletItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_recycler_item_layout,parent,false));
        else
            return new WalletDateItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.date_item_recycler_view,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if(dataList.get(position).getType().equals("Date"))
        return 1;
        else return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(dataList.get(position).getType().equals("Date")){
            WalletDateItemViewHolder viewHolder = (WalletDateItemViewHolder) holder;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            viewHolder.dateText.setText(dateFormat.format(new Date(dataList.get(position).getDate()*1000)));
        }
        else {
            WalletItemViewHolder viewHolder = (WalletItemViewHolder) holder;
            if (dataList.get(position).getDetails() == null || dataList.get(position).getDetails().equals(""))
                viewHolder.walletDescription.setText(context.getString(R.string.empty_detail_default_text));
            else viewHolder.walletDescription.setText(dataList.get(position).getDetails());
            if (dataList.get(position).getType().equals("Income")) {
                bindIncome(viewHolder);
                viewHolder.walletAmount.setText(Float.toString(dataList.get(position).getAmount()));
            }
            if (dataList.get(position).getType().equals("Expense")) {
                bindExpense(viewHolder);
                viewHolder.walletAmount.setText(Float.toString(-1 * dataList.get(position).getAmount()));
            }
        /*if(dataList.get(position).getType().equals("Clear Balance")) {
            bindClearBalance(viewHolder);
            viewHolder.detailsAmount.setText("");
        }*/
            //viewHolder.detailsAmount.setText(Float.toString(dataList.get(position).getAmount()));
            String date = new SimpleDateFormat("HH:mm").format(new Date((dataList.get(position).getDate() * 1000)));
            viewHolder.walletDate.setText(date);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void bindIncome(WalletItemViewHolder viewHolder){
        //DetailsViewHolder viewHolder = (DetailsViewHolder) holder;
        viewHolder.walletType.setText(context.getString(R.string.income));
        viewHolder.walletTypeIcon.setImageResource(R.drawable.lent_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }

    public void bindExpense(WalletItemViewHolder viewHolder){
        //DetailsViewHolder viewHolder = (DetailsViewHolder) holder;
        viewHolder.walletType.setText(context.getString(R.string.expense));
        viewHolder.walletTypeIcon.setImageResource(R.drawable.borrowed_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }
    /*public void bindClearBalance(WalletItemViewHolder viewHolder){
        //DetailsViewHolder viewHolder = (DetailsViewHolder) holder;
        viewHolder.walletType.setText("Clear Balance");
        viewHolder.walletTypeIcon.setImageResource(R.drawable.paid);
        //viewHolder.viewHolderContainer.setBackgroundResource(R.drawable.oval_background);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
        viewHolder.detailsDescription.setText("");
    }*/
    public class WalletItemViewHolder extends RecyclerView.ViewHolder{
        private TextView walletType;
        private TextView walletDate;
        private TextView walletAmount;
        private ImageView walletTypeIcon;
        private TextView walletDescription;
        private RelativeLayout viewHolderContainer;
        public WalletItemViewHolder(View itemView) {
            super(itemView);
            viewHolderContainer= (RelativeLayout) itemView;
            walletType = (TextView) itemView.findViewById(R.id.wallet_type_text);
            walletAmount = (TextView) itemView.findViewById(R.id.wallet_amount_text);
            walletDate =(TextView) itemView.findViewById(R.id.wallet_date_text);
            walletDescription = (TextView) itemView.findViewById(R.id.wallet_description_text);
            walletTypeIcon = (ImageView) itemView.findViewById(R.id.wallet_type_icon);
        }
    }

    public class WalletDateItemViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;
        public WalletDateItemViewHolder(View itemView) {
            super(itemView);
            dateText = (TextView) itemView.findViewById(R.id.date_item_text);
        }
    }

    /**public ArrayList<WalletItem> getDataList() {
        return dataList;
    }*/

    public void setDataList(ArrayList<WalletItem> dataList) {
        this.dataList = dataList;
    }

    public void onItemClick(final int position){
        android.support.v7.app.AlertDialog.Builder dialogBuilder= new android.support.v7.app.AlertDialog.Builder(context);
        dialogBuilder.setItems(R.array.transaction_click_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    WalletItem item = dataList.get(position);
                    Intent editItem = new Intent(context, AddWalletItemActivity.class);
                    editItem.putExtra("TIME", item.getDate());
                    editItem.putExtra("DETAILS",item.getDetails());
                    editItem.putExtra("TYPE",item.getType());
                    editItem.putExtra("AMOUNT",item.getAmount());
                    editItem.putExtra("EDIT",true);
                    editItem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(editItem);
                    dialog.dismiss();
                }
                else if (which==1){
                    WalletDatabaseHelper databaseHelper = new WalletDatabaseHelper(context);
                    databaseHelper.onDeleteTransactionDetails(dataList.get(position).getDate());
                    if(dataList.get(position-1).getType().equals("Date") && (dataList.size() == position+1 || dataList.get(position+1).getType().equals("Date"))){
                        dataList.remove(position);
                        dataList.remove(position-1);
                        notifyItemRemoved(position);
                        notifyItemRemoved(position-1);
                        notifyItemRangeChanged(0,dataList.size());
                    }
                    else {
                        dataList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(0,dataList.size());
                    }
                    dialog.dismiss();
                    databaseHelper.close();
                    callback.onDeleteTransaction();
                }
            }
        });
        dialogBuilder.show();
    }
}

