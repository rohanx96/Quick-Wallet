package com.rose.quickwallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.rose.quickwallet.myWallet.WalletActivity;
import com.rose.quickwallet.transactions.MainActivity;

/**
 *
 * Created by rose on 15/8/15.
 */
public class BaseActivity extends AppCompatActivity {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.base_activity);
        /*drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);*/
    }

    protected void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(final MenuItem menuItem) {

        switch(menuItem.getItemId()) {

            case R.id.nav_wallet:
                //Toast.makeText(getApplicationContext(), "Stared Selected", Toast.LENGTH_SHORT).show();
                Intent walletActivity = new Intent(this,WalletActivity.class);
                walletActivity.setAction("generic");
                //walletActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(walletActivity);
                finish();
                menuItem.setChecked(true);
                break;
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
