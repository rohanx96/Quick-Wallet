package com.rose.quickwallet.myWallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rose.quickwallet.R;
import com.rose.quickwallet.BaseActivity;
import com.rose.quickwallet.SettingsActivity;
import com.rose.quickwallet.callbackhepers.DetailsRecyclerViewCallback;
import com.rose.quickwallet.transactions.MainActivity;
import com.rose.quickwallet.tutorial.TutorialActivity;

/**
 *
 * Created by rose on 16/8/15.
 */
public class WalletActivity extends BaseActivity implements DetailsRecyclerViewCallback {
    private RecyclerView recyclerView;
    private WalletDatabaseHelper databaseHelper;
    private WalletRecyclerAdapter recyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_wallet_activity));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);
        setTitle("My Wallet");
        /*AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        builder.addTestDevice("D882CD568608B87702357166E3B3E8BD");
        AdRequest adRequest = builder.build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                LinearLayout adLayout = (LinearLayout) findViewById(R.id.ad_layout);
                adLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                LinearLayout adLayout = (LinearLayout) findViewById(R.id.ad_layout);
                adLayout.setVisibility(View.VISIBLE);
            }
        });*/
        recyclerView = (RecyclerView) findViewById(R.id.wallet_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new WalletDatabaseHelper(this);
        recyclerAdapter = new WalletRecyclerAdapter(databaseHelper.getData(),this);
        recyclerView.setAdapter(recyclerAdapter);
        TextView fundsText = (TextView) findViewById(R.id.wallet_funds_text);
        fundsText.setText("Funds in Wallet: " + databaseHelper.getBalance());
        TextView expensesToday = (TextView) findViewById(R.id.wallet_expenses_today);
        expensesToday.setText("Total Expenses Today: " + databaseHelper.getTodaysExpense());
        TextView incomeToday = (TextView) findViewById(R.id.wallet_income_today);
        incomeToday.setText("Total Income Today: " + databaseHelper.getTodaysIncome());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle("My Wallet");
        navigationView.getMenu().findItem(R.id.nav_wallet).setChecked(true);
        final TextView navViewHeaderText = (TextView) LayoutInflater.from(this).inflate(R.layout.nav_header_layout,navigationView).findViewById(R.id.nav_header_text);
        navViewHeaderText.setText("");
        databaseHelper = new WalletDatabaseHelper(getApplicationContext());
        recyclerAdapter.setDataList(databaseHelper.getData());
        recyclerView.getAdapter().notifyDataSetChanged();
        TextView fundsText = (TextView) findViewById(R.id.wallet_funds_text);
        fundsText.setText("Funds in Wallet: " + databaseHelper.calculateBalance());
        TextView expensesToday = (TextView) findViewById(R.id.wallet_expenses_today);
        expensesToday.setText("Total Expenses Today: " + databaseHelper.getTodaysExpense());
        TextView incomeToday = (TextView) findViewById(R.id.wallet_income_today);
        incomeToday.setText("Total Income Today: " + databaseHelper.getTodaysIncome());
        databaseHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wallet_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.wallet_menu_clear_history:
                databaseHelper = new WalletDatabaseHelper(this);
                databaseHelper.clearHistory();

                recyclerAdapter.setDataList(databaseHelper.getData());
                recyclerView.getAdapter().notifyDataSetChanged();
                TextView fundsText = (TextView) findViewById(R.id.wallet_funds_text);
                fundsText.setText("Funds in Wallet: " + databaseHelper.getBalance());
                TextView expensesToday = (TextView) findViewById(R.id.wallet_expenses_today);
                expensesToday.setText("Total Expenses Today: " + databaseHelper.getTodaysExpense());
                TextView incomeToday = (TextView) findViewById(R.id.wallet_income_today);
                incomeToday.setText("Total Income Today: " + databaseHelper.getTodaysIncome());
                databaseHelper.close();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAddFabButtonClick(View v){
        Intent intent = new Intent(this,AddWalletItemActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
    }

    @Override
    public void onDeleteTransaction() {
        databaseHelper = new WalletDatabaseHelper(this);
        TextView fundsText = (TextView) findViewById(R.id.wallet_funds_text);
        fundsText.setText("Funds in Wallet: " + databaseHelper.calculateBalance());
        TextView expensesToday = (TextView) findViewById(R.id.wallet_expenses_today);
        expensesToday.setText("Total Expenses Today: " + databaseHelper.getTodaysExpense());
        TextView incomeToday = (TextView) findViewById(R.id.wallet_income_today);
        incomeToday.setText("Total Income Today: " + databaseHelper.getTodaysIncome());
        databaseHelper.close();
    }

    @Override
    public void selectDrawerItem(final MenuItem menuItem) {

        switch(menuItem.getItemId()) {

            case R.id.nav_transactions:
                //Toast.makeText(getApplicationContext(), "Send Selected", Toast.LENGTH_SHORT).show();
                Intent transactionActivity = new Intent(this,MainActivity.class);
                transactionActivity.setAction("enter");
                //transactionActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(transactionActivity);
                finish();
                menuItem.setChecked(true);
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_tutorial:
                Intent tutorial = new Intent(this, TutorialActivity.class);
                startActivity(tutorial);
                break;
            case R.id.nav_help:
                Intent helpIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "rohanx96@gmail.com", null));
                //intent.setType("text/html");
                helpIntent.putExtra(Intent.EXTRA_SUBJECT, "QuickWallet App on PlayStore");
                startActivity(Intent.createChooser(helpIntent,"send Email"));
            default:
                //Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                break;
        }
        // Highlight the selected item, update the title, and close the drawer
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }
}
