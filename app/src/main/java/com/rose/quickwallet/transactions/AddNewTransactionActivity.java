/*
 * Copyright (c) 2016. Rohan Agarwal (rOhanX96)
 */

package com.rose.quickwallet.transactions;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.rose.quickwallet.EnterPinActivity;
import com.rose.quickwallet.R;

/**
 * Adds transaction to local database and send chat message to corresponding user
 * Created by rose on 23/7/15.
 */

public class AddNewTransactionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("securitySwitch", false) && getIntent().getAction().equals("notification")) {
            Intent enterPassword = new Intent(this, EnterPinActivity.class);
            enterPassword.setAction("ENTER_PASSWORD_NOTIFICATION");
            startActivity(enterPassword);
            finish();
        }
        AddNewTransactionFragment fragment = new AddNewTransactionFragment();
        Bundle args = new Bundle();
        args.putString("action",getIntent().getStringExtra("action"));
        args.putString(SearchManager.QUERY, getIntent().getStringExtra(SearchManager.QUERY));
        args.putString("contact", getIntent().getStringExtra("contact"));
        args.putFloat("amount", getIntent().getFloatExtra("amount",0));
        args.putString("details",getIntent().getStringExtra("details"));
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().add(R.id.add_transaction_fragment_container,fragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
//        if(isSignedUp)
//            logoutFormChat();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_from_top);
    }
}