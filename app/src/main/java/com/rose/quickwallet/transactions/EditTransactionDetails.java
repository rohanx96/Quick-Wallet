package com.rose.quickwallet.transactions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rose.quickwallet.CalcActivity;
import com.rose.quickwallet.R;

/**
 * Created by rose on 26/9/15.
 */
public class EditTransactionDetails extends Activity {
    private String type = "Lent";
    private String details;
    private float amount=0;
    private long time;
    private String name;
    private String mCurrency;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details_transactions);
        context = getApplicationContext();
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235)); // 335px - 220dp
        TextView textView = (TextView) findViewById(R.id.edit_item_detail_name);
        textView.setText(getString(R.string.lent));
        //textView.setTextColor(Color.RED);
        name = getIntent().getStringExtra("NAME");
        time = getIntent().getLongExtra("TIME", 0);
        amount = getIntent().getFloatExtra("AMOUNT", 0);
        details = getIntent().getStringExtra("DETAILS");
        type = getIntent().getStringExtra("TYPE");
        //Log.v("Time", Long.toString(time));
        mCurrency = PreferenceManager.getDefaultSharedPreferences(this).getString("prefCurrency","");
        final EditText amountView = (EditText)findViewById(R.id.edit_item_amount);
        amountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.edit_item_details);
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
                } else {
                    String amountText = amountView.getText().toString();
                    try{
                        amount = Float.parseFloat(amountText);
                    }
                    catch (NumberFormatException e){
                        amount = Float.parseFloat("0" + amountText.substring(1,amountText.length()));
                    }

                    TextView textView = (TextView) relativeLayout.findViewById(R.id.edit_item_detail_balance);
                    textView.setText(getString(R.string.amount_colon)  + amountView.getText().toString());
                    textView = (TextView) findViewById(R.id.edit_item_detail_name);
                    textView.setText(type);
                    if (type.equals("Lent")) {
                        textView.setTextColor(getResources().getColor(R.color.lent_green));
                        //amount = amount;
                    } else {
                        textView.setTextColor(getResources().getColor(R.color.borrowed_red));
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
                    TextInputLayout amountLayout = (TextInputLayout) findViewById(R.id.edit_item_amount_wrapper);
                    amountLayout.setErrorEnabled(false);
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 330)); // 445px - 300dp
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(amount>0)
                    amountView.setText(mCurrency + Float.toString(amount));
                else
                    amountView.setText(mCurrency + Float.toString(-1*amount));
                TextView detailsText = (TextView) findViewById(R.id.edit_item_detail);
                detailsText.setText(details);
                RadioGroup radioGroup = (RadioGroup)findViewById(R.id.type_radio_options_edit);
                if (type.equals("Lent"))
                    radioGroup.check(R.id.radio_button_lent_edit);
                else
                    radioGroup.check(R.id.radio_button_borrowed_edit);
            }
        });
        thread.run();
    }

    public void onTypeRadioButtonSelected(View v){
        TextView textView = (TextView) findViewById(R.id.edit_item_detail_name);
        switch (v.getId()){
            case (R.id.radio_button_borrowed_edit):
                textView.setText(getString(R.string.borrowed));
                textView.setTextColor(Color.RED);
                type = "Borrowed";
                break;
            case R.id.radio_button_lent_edit:
                textView.setText(getString(R.string.lent));
                textView.setTextColor(Color.GREEN);
                type = "Lent";
        }
    }

    public void onEditWalletItem(View v){
        TextInputLayout amountLayout = (TextInputLayout) findViewById(R.id.edit_item_amount_wrapper);
        if(amount==0) {
            amountLayout.setError(getString(R.string.enter_amount));
            amountLayout.setErrorEnabled(true);
            return;
        }
        TextView detailsText = (TextView) findViewById(R.id.edit_item_detail);
        details = detailsText.getText().toString();
        //Snackbar.make(findViewById(R.id.edit_transaction), "Added Transaction: " + type + ": " + amount, Snackbar.LENGTH_SHORT).show();
        if(type.equals("Borrowed"))
            amount = -1 * amount;
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        //databaseHelper.saveItemToDatabase(type,details,amount);
        databaseHelper.onEditDetails(type,details,amount,time,name);
        databaseHelper.close();
        /*amountLayout.setErrorEnabled(false);
        amount = 0;
        EditText amountView = (EditText) findViewById(R.id.edit_item_amount);
        amountView.setText("");
        detailsText.setText(null);*/
        finish();
    }

    public int convertDPToPx(Context context, float dp){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public void startCalCActivity(View v){
        Intent calcActivity = new Intent(this, CalcActivity.class);
        startActivityForResult(calcActivity,234);
    }
}
