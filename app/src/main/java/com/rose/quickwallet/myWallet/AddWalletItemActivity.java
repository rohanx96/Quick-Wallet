package com.rose.quickwallet.myWallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rose.quickwallet.CalcActivity;
import com.rose.quickwallet.R;

/**
 * Created by rose on 19/8/15.
 *
 */

public class AddWalletItemActivity extends Activity {
    private String type = "Expense";
    private long time;
    private String details;
    private float amount=0;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_wallet_item_activity);
        context = getApplicationContext();
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235)); // 335px - 220dp
        TextView textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
        textView.setText(getString(R.string.expense));
        textView.setTextColor(Color.RED);
        final Button amountView = (Button)findViewById(R.id.add_wallet_item_amount_wrapper);
        /*amountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_wallet_item_details);
                if (amountView.getText().toString().equals("")) {
                    amount = 0;
                    relativeLayout.setVisibility(View.GONE);
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235)); // 335px - 220dp
                    /*if (name == null)
                        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 440);
                    else {
                        if (balance == 0)
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 560);
                        else
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 640);
                    }*/
                /*} else {
                    amount = Float.parseFloat(amountView.getText().toString());
                    TextView textView = (TextView) relativeLayout.findViewById(R.id.add_wallet_item_detail_balance);
                    textView.setText("Amount: " + amountView.getText().toString());
                    textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
                    textView.setText(type);
                    if (type.equals("Income")) {
                        textView.setTextColor(Color.GREEN);
                        //amount = amount;
                    } else {
                        textView.setTextColor(Color.RED);
                        //amount = -1*amount;
                    }
                    /*if (name == null)
                        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 560);
                    else {
                        if (balance == 0)
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 660);
                        else
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 740);
                    }*/
                    /*TextInputLayout amountLayout = (TextInputLayout) findViewById(R.id.add_wallet_item_amount_wrapper);
                    amountLayout.setErrorEnabled(false);
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context,330)); // 445px - 300dp
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });*/
        if(getIntent().getBooleanExtra("EDIT",false)){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    time = getIntent().getLongExtra("TIME", 0);
                    amount = getIntent().getFloatExtra("AMOUNT", 0);
                    details = getIntent().getStringExtra("DETAILS");
                    type = getIntent().getStringExtra("TYPE");
                    if(amount<0)
                        amount = -1 * amount;
                    amountView.setText(getString(R.string.amount_colon) + Float.toString(amount));
                    TextView detailsText = (TextView) findViewById(R.id.add_wallet_item_detail);
                    detailsText.setText(details);
                    RadioGroup radioGroup = (RadioGroup)findViewById(R.id.type_radio_options);
                    if (type.equals("Income"))
                        radioGroup.check(R.id.radio_button_income);
                    else
                        radioGroup.check(R.id.radio_button_expenditure);
                    Button doneButton = (Button) findViewById(R.id.done_button_add_wallet_item);
                    doneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onEditWalletItem();
                        }
                    });
                    doneButton.setText(getString(R.string.edit));
                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_wallet_item_details);
                    TextView textView = (TextView) relativeLayout.findViewById(R.id.add_wallet_item_detail_balance);
                    textView.setText(getString(R.string.amount_colon) + amount);
                    textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
                    textView.setText(type);
                    amountView.setTextColor(Color.BLACK);
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 330)); // 445px - 300dp
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            });
            thread.run();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.add_activity_exit_animation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 222){
            if (resultCode == RESULT_OK){
                float result = data.getFloatExtra("RESULT",0);
                Button amountView = (Button) findViewById(R.id.add_wallet_item_amount_wrapper);
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_wallet_item_details);
                if (result == 0) {
                    amount = 0;
                    relativeLayout.setVisibility(View.GONE);
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235)); // 335px - 220dp
                    amountView.setText(getString(R.string.enter_amount));
                } else {
                    amount = result;
                    if(amount<0){
                        type = "Expense";
                        amount = -1*amount;
                        RadioGroup group = (RadioGroup) findViewById(R.id.type_radio_options);
                        group.check(R.id.radio_button_expenditure);
                    }
                    TextView textView = (TextView) relativeLayout.findViewById(R.id.add_wallet_item_detail_balance);
                    textView.setText(getString(R.string.amount_colon) + amount);
                    textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
                    textView.setText(type);
                    amountView.setText(getString(R.string.amount_colon) + amount);
                    amountView.setTextColor(Color.BLACK);
                    if (type.equals("Income")) {
                        textView.setTextColor(setColorGreen());
                        //amount = amount;
                    } else {
                        textView.setTextColor(setColorRed());
                        //amount = -1*amount;
                    }
                    /*if (name == null)
                        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 560);
                    else {
                        if (balance == 0)
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 660);
                        else
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 740);
                    }*/
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context,330)); // 445px - 300dp
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void onTypeRadioButtonSelected(View v){
        TextView textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
        switch (v.getId()){
            case (R.id.radio_button_expenditure):
                textView.setText(getString(R.string.expense));
                textView.setTextColor(setColorRed());
                type = "Expense";
                break;
            case R.id.radio_button_income:
                textView.setText(getString(R.string.income));
                textView.setTextColor(setColorGreen());
                type = "Income";
        }
    }

    public void onAddWalletItem(View v){
        Button amountLayout = (Button) findViewById(R.id.add_wallet_item_amount_wrapper);
        if(amount==0) {
            amountLayout.setTextColor(Color.RED);
            Toast.makeText(this,getString(R.string.enter_amount),Toast.LENGTH_SHORT).show();
            return;
        }
        TextView detailsText = (TextView) findViewById(R.id.add_wallet_item_detail);
        details = detailsText.getText().toString();
        Snackbar.make(findViewById(R.id.new_wallet_transaction), "Added Transaction: " + type + ": " + amount, Snackbar.LENGTH_SHORT).show();
        if(type.equals("Expense"))
            amount = -1 * amount;
        WalletDatabaseHelper databaseHelper = new WalletDatabaseHelper(this);
        databaseHelper.saveItemToDatabase(type,details,amount);
        databaseHelper.close();
        amount = 0;
        amountLayout.setText(getString(R.string.enter_amount));
        amountLayout.setTextColor(Color.BLACK);
        detailsText.setText(null);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_wallet_item_details);
        relativeLayout.setVisibility(View.GONE);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235));
    }

    public void onEditWalletItem(){
        Button amountLayout = (Button) findViewById(R.id.add_wallet_item_amount_wrapper);
        if(amount==0) {
            amountLayout.setTextColor(Color.RED);
            Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        TextView detailsText = (TextView) findViewById(R.id.add_wallet_item_detail);
        details = detailsText.getText().toString();
        //Snackbar.make(findViewById(R.id.new_wallet_transaction), "Added Transaction: " + type + ": " + amount, Snackbar.LENGTH_SHORT).show();
        if(type.equals("Expense"))
            amount = -1 * amount;
        WalletDatabaseHelper databaseHelper = new WalletDatabaseHelper(this);
        databaseHelper.onEditTransactionDetails(time,type,details,amount);
        databaseHelper.close();
        finish();
    }

    public int convertDPToPx(Context context, float dp){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,metrics);
    }

    public void startCalCActivity(View v){
        Intent calcActivity = new Intent(this, CalcActivity.class);
        startActivityForResult(calcActivity, 222);
    }

    public int setColorRed(){
        return Color.parseColor("#ffc94c4c");
    }

    public int setColorGreen(){
        return Color.parseColor("#ff509f4c");
    }
}
