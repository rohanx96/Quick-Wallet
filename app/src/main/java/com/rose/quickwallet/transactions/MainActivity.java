package com.rose.quickwallet.transactions;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.TextView;

//import com.quickblox.auth.QBAuth;
//import com.quickblox.auth.model.QBSession;
//import com.quickblox.core.QBEntityCallbackImpl;
//import com.quickblox.core.QBSettings;
//import com.quickblox.users.model.QBUser;
import com.rose.quickwallet.AlarmReceiver;
import com.rose.quickwallet.BaseActivity;
import com.rose.quickwallet.EnterPinActivity;
//import com.rose.quickwallet.MyAccountActivity;

import com.rose.quickwallet.R;
import com.rose.quickwallet.SettingsActivity;
import com.rose.quickwallet.callbackhepers.ItemTouchHelperCallback;
import com.rose.quickwallet.callbackhepers.RecyclerViewCallback;
import com.rose.quickwallet.myWallet.WalletActivity;
//import com.rose.quickwallet.quickblox.Consts;
//import com.rose.quickwallet.quickblox.GoCloudActivity;
//import com.rose.quickwallet.quickblox.RetreiveUsersService;
//import com.rose.quickwallet.quickblox.pushnotifications.GCMClientHelper;
import com.rose.quickwallet.tutorial.TutorialActivity;

import java.util.ArrayList;
//import java.util.List;


public class MainActivity extends BaseActivity implements RecyclerViewCallback {
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
//    private boolean isSignedIn = false;
//    private GCMClientHelper pushClientManager;
    private SharedPreferences preferences;
    boolean shouldAnimate = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //TraceCompat.beginSection("start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        isSignedIn = preferences.getBoolean(Consts.IS_SIGNED_UP, false);
        //DialogUtils.showLong(getApplicationContext(),"isSignedIn: " + isSignedIn);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                boolean isFirstStartAfterGCMUpdate = preferences.getBoolean("isFirstStartAfterGCMUpdate", true);
//                if (isFirstStartAfterGCMUpdate) {
//                    Intent goCloud = new Intent(getApplicationContext(), GoCloudActivity.class);
//                    startActivity(goCloud);
//                    Intent tutorial = new Intent(MainActivity.this, TutorialActivity.class);
//                    startActivity(tutorial);
//                    finish();
//                    preferences.edit().putBoolean("isFirstStartAfterGCMUpdate", false).apply();
                if (preferences.getBoolean("securitySwitch", false) && !getIntent().getAction().equals("enter")) {
                    Intent password = new Intent(MainActivity.this, EnterPinActivity.class);
                    password.setAction("ENTER_PASSWORD");
                    startActivity(password);
                    finish();
                }
            }
        });
        thread.start();

        shouldAnimate = true;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);
        requestPermissions();
        //loadAd();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new RecyclerAdapter(new ArrayList<RecyclerViewItem>(), MainActivity.this);
        recyclerView.setAdapter(adapter);
        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback((RecyclerAdapter) recyclerView.getAdapter());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
//        // Code to create quickblox session and register to GCM client if required
//        if (isSignedIn) {
//            QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
//            final QBUser user = new QBUser();
//            user.setEmail(preferences.getString(Consts.USER_LOGIN, null));
//            user.setPassword(preferences.getString(Consts.USER_PASSWORD, null));
//            //DialogUtils.showLong(getApplicationContext(), "creating session for user: " + user);
//            QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
//                @Override
//                public void onSuccess(QBSession session, Bundle params) {
//                    //DialogUtils.showLong(getApplicationContext(),"Registering GCM");
//                    Thread th = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent retrieveUsers = new Intent(getApplicationContext(), RetreiveUsersService.class);
//                            retrieveUsers.putExtra("createSession", false);
//                            retrieveUsers.putExtra("sendNotifications", false);
//                            startService(retrieveUsers);
//                        }
//                    });
//                    th.start();
//
//                    pushClientManager = new GCMClientHelper(MainActivity.this, Consts.PROJECT_NUMBER);
//                    pushClientManager.registerIfNeeded(new GCMClientHelper.RegistrationCompletedHandler() {
//                        @Override
//                        public void onSuccess(String registrationId, boolean isNewRegistration) {
//                            //Toast.makeText(MainActivity.this, registrationId,Toast.LENGTH_SHORT).show();
//                            // SEND async device registration to your back-end server
//                            // linking user with device registration id
//                            // POST https://my-back-end.com/devices/register?user_id=123&device_id=abc
//                        }
//
//                        @Override
//                        public void onFailure(String ex) {
//                            super.onFailure(ex);
//                            //Toast.makeText(MainActivity.this,"unable to create session",Toast.LENGTH_LONG).show();
//                            // If there is an error registering, don't just keep trying to register. Require the user to click a button again,
//                            // or perform exponential back-off when retrying.
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(List<String> errors) {
//                    // errors
//                }
//            });
//        }
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                // we will not get a value  at first start, so true will be returned
                boolean isFirstStart = preferences.getBoolean("isFirstStart", true);
                // if it was the first app start
                if (isFirstStart) {
                    drawerLayout.openDrawer(GravityCompat.START);
                    SharedPreferences.Editor e = preferences.edit();
                    // we save the value "false", indicating that it is no longer the first appstart
                    e.putBoolean("isFirstStart", false);
                    e.apply();
                }
//                setupNavigationHeader();
            }
        });
        t.start();

        if (Build.VERSION.SDK_INT >= 21) {
            TransitionInflater inflater = TransitionInflater.from(getApplication());
            getWindow().setSharedElementExitTransition(inflater.inflateTransition(R.transition.shared_contact_image_transition));
            getWindow().setSharedElementEnterTransition(inflater.inflateTransition(R.transition.details_activity_return_transition));
            getWindow().setExitTransition(inflater.inflateTransition(R.transition.main_activity_exit_transition));
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
                search(getIntent().getStringExtra(SearchManager.QUERY));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCurrencyChooserIfRequired();
        navigationView.getMenu().findItem(R.id.nav_transactions).setChecked(true);
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        //adapter.refreshDataList(databaseHelper.getData(),false);
        adapter.setDataList(databaseHelper.getData());
        adapter.setmCurrency(preferences.getString("prefCurrency", ""));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getAdapter().notifyDataSetChanged();
        shouldAnimate = false;
        resetFabScale();
        /*FloatingActionButton fab = ((FloatingActionButton) findViewById(R.id.fab_add));
        fab.setImageResource(R.drawable.plus);
        fab.setScaleX(1.0f);
        fab.setScaleY(1.0f);*/
        setServiceAlarm();
        if (Build.VERSION.SDK_INT < 21)
            setupSearchEditText();
        else
            setupSearchView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /* OnClick method for fab button to start AddNewTransaction Activity */
    public void start(View view) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        //fab.setImageResource(R.color.colorAccent);
        startRippleAnimation(fab);
        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, AddNewTransactionActivity.class);
                intent.putExtra("action", "generic");
                intent.setAction("generic");
                startActivity(intent);
                overridePendingTransition(0, R.anim.stay);
            }
        },250);*/
    }

    public void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        SearchView searchView = (SearchView) findViewById(R.id.search_view_home);
        searchView.setSearchableInfo(searchableInfo);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s); //Search for the string s. Look at the method definition below for execution details
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s); //Search for the string s. Look at the method definition for execution details
                return false;
            }
        });
    }

    public void setupSearchEditText() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.search_edit_text);
            actionBar.setDisplayShowTitleEnabled(false);
            setupSearchView();
        }
    }

    /**
     * Searches for string s in names stored in database and updates the recycler view with the datalist obtained from database
     */
    public void search(String s) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        adapter.setDataList(databaseHelper.search(s));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onItemSwiped(final RecyclerViewItem item) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.updateItemOnSwipe(item.getName());
        String currency = preferences.getString("prefCurrency", "");
        View header = recyclerView.getChildAt(0);
        //RelativeLayout header = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.recycler_view_header_layout, recyclerView,false);
        TextView totalLent = (TextView) header.findViewById(R.id.lent_header);
        //TextView totalLent = (TextView) recyclerView.findViewById(R.id.lent_header);
        TextView totalBorrowed = (TextView) header.findViewById(R.id.borrowed_header);
        //TextView totalBorrowed = (TextView) recyclerView.findViewById(R.id.borrowed_header);
        //TODO: Find a solution to update the balance for null views
        // These views are returned null when the header image is not visible on screen
        // (When user has scrolled down and the view at position 0 has been recycled
        if (totalLent != null) // prevent force close due to null
            totalLent.setText(getResources().getString(R.string.lent_colon) + currency + databaseHelper.totalLent());
        if (totalBorrowed != null) // prevent force close due to null
            totalBorrowed.setText(getResources().getString(R.string.borrowed_colon) + currency + databaseHelper.totalBorrowed());
        //recyclerView.getAdapter().notifyDataSetChanged();
        databaseHelper.close();
        Snackbar.make(findViewById(R.id.activity_coordinator_layout), getString(R.string.snackbar_balance_cleared_beg) + item.getName() + getString(R.string.snackbar_balance_cleared_end), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                databaseHelper.onUndoSwipe(item);
                adapter.setDataList(databaseHelper.getData());
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }).show();
    }

    @Override
    public int clickedItemPosition(View v) {
        return recyclerView.indexOfChild(v);
    }

    @Override
    public void removeItem(int position) {
        recyclerView.removeViewAt(position);
    }

    /**
     * Sets alarm to start the notification service to push notifications to user of pending balances
     */
    public void setServiceAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Setup periodic alarm every 5 seconds. Not required now because now times are directly set in the method call using
        // AlarmManager.INTERVAL_HALF_HOUR
        //long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
        //int intervalMillis = 5000; // 5 seconds
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String interval = preferences.getString("notificationInterval", "12");
        AlarmManager alarm = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        if (preferences.getBoolean("notificationSwitch", true)) {
            switch (interval) {
                case "30":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15 * 60 * 1000,
                            AlarmManager.INTERVAL_HALF_HOUR, pIntent);
                    break;
                case "60":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 60 * 1000,
                            AlarmManager.INTERVAL_HOUR, pIntent);
                    break;
                case "3":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 60 * 1000,
                            3 * 60 * 60 * 1000, pIntent);
                    break;
                case "6":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3 * 60 * 60 * 1000,
                            6 * 60 * 60 * 1000, pIntent);
                    break;
                case "12":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 6 * 60 * 60 * 1000,
                            AlarmManager.INTERVAL_HALF_DAY, pIntent);
                    break;
                case "24":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 12 * 60 * 60 * 1000,
                            AlarmManager.INTERVAL_DAY, pIntent);
                    break;
            }
        }
    }

    /**
     * Requests read contacts permission from user. Only for android v23 and up
     */
    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                dialogBuilder.setMessage(getString(R.string.request_permissions_contacts))
                        .setPositiveButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 2);
                            }
                        });
                dialogBuilder.show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 2);
        }
    }

    /**
     * private void loadAd(){
     * AdView mAdView = (AdView) findViewById(R.id.adView);
     * AdRequest.Builder builder = new AdRequest.Builder();
     * builder.addTestDevice("D882CD568608B87702357166E3B3E8BD");
     * AdRequest adRequest = builder.build();
     * mAdView.loadAd(adRequest);
     * mAdView.setAdListener(new AdListener() {
     *
     * @Override public void onAdFailedToLoad(int errorCode) {
     * super.onAdFailedToLoad(errorCode);
     * LinearLayout adLayout = (LinearLayout) findViewById(R.id.ad_layout);
     * adLayout.setVisibility(View.GONE);
     * }
     * @Override public void onAdLoaded() {
     * super.onAdLoaded();
     * LinearLayout adLayout = (LinearLayout) findViewById(R.id.ad_layout);
     * adLayout.setVisibility(View.VISIBLE);
     * }
     * });
     * }
     */

    public void onOpenDrawer(View v) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void selectDrawerItem(final MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.nav_wallet:
                //Toast.makeText(getApplicationContext(), "Stared Selected", Toast.LENGTH_SHORT).show();
                Intent walletActivity = new Intent(this, WalletActivity.class);
                walletActivity.setAction("generic");
                //walletActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(walletActivity);
                finish();
                menuItem.setChecked(true);
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_tutorial:
                Intent tutorial = new Intent(this, TutorialActivity.class);
                startActivity(tutorial);
                break;
            case R.id.nav_help:
                Intent helpIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "rohanx96@gmail.com", null));
                helpIntent.putExtra(Intent.EXTRA_SUBJECT, "QuickWallet App on PlayStore");
                startActivity(Intent.createChooser(helpIntent, "send Email"));
            default:
                //Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                break;
        }
        // Highlight the selected item, update the title, and close the drawer
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

//    private void setupNavigationHeader() {
//        final TextView navViewHeaderText = (TextView) LayoutInflater.from(this).inflate(R.layout.nav_header_layout, navigationView).findViewById(R.id.nav_header_text);
//        if (isSignedIn) {
//            navViewHeaderText.setText(getResources().getString(R.string.my_account));
//            navViewHeaderText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent account = new Intent(MainActivity.this, MyAccountActivity.class);
//                    startActivity(account);
//                    overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.fade_out); // this method should be called just after startActivity
//                }
//            });
//        } else {
//            navViewHeaderText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent goCloud = new Intent(getApplicationContext(), GoCloudActivity.class);
//                    startActivity(goCloud);
//                }
//            });
//        }
//    }

    /**
     * Displays the currency chooser dialog if not previously shown
     */
    public void showCurrencyChooserIfRequired() {
        if (preferences.getString("prefCurrencyIsShown", "F").equals("F")) {
            // Set the dialog shown preference to true
            preferences.edit().putString("prefCurrencyIsShown", "T").apply();
            //Show the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCustomTitle(LayoutInflater.from(this).inflate(R.layout.dialog_currency_choser_header, null, false));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.dialog_currency_choser_item);
            adapter.addAll(getResources().getStringArray(R.array.currency_locales));
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            preferences.edit().putString("prefCurrency", "$").apply();
                            break;
                        case 1:
                            preferences.edit().putString("prefCurrency", "€").apply();
                            break;
                        case 2:
                            preferences.edit().putString("prefCurrency", "£").apply();
                            break;
                        case 3:
                            preferences.edit().putString("prefCurrency", "₹").apply();
                            break;
                        case 4:
                            preferences.edit().putString("prefCurrency", "₣").apply();
                            break;
                        case 5:
                            preferences.edit().putString("prefCurrency", "¥").apply();
                            break;
                        case 6:
                            preferences.edit().putString("prefCurrency", "RM").apply();
                            break;
                        case 7:
                            preferences.edit().putString("prefCurrency", "¥").apply();
                            break;
                        case 8:
                            preferences.edit().putString("prefCurrency", "₩").apply();
                            break;
                        default:
                            preferences.edit().putString("prefCurrency", "").apply();
                            break;
                    }
                    dialog.dismiss();
                    onResume();
                }
            })
                    .create()
                    .show();
        }
    }

    /**
     * Increases the fab button size to fill the screen creating a ripple effect
     */
    public void startRippleAnimation(final FloatingActionButton view) {
        /* Due to setting margin in our fab button when we scale it up it can not take up the entire screen.
        To overcome this we make another fab button at the same position as our original fab button without setting margin to this fab
        button. We then scale up this new fab button
         */
        CoordinatorLayout frame = (CoordinatorLayout) findViewById(R.id.activity_coordinator_layout);
        // Check if it has been previously added. If not then we create a new fab button
        FloatingActionButton imageView = (FloatingActionButton) frame.findViewById(R.id.fab_expand_menu_button);
        if (imageView == null) {
            imageView = new FloatingActionButton(this);
            imageView.setId(R.id.fab_expand_menu_button); // Set an id so that it can be found later in order to scale it down

            //imageView.setImageResource(R.drawable.circle_selected);
            //imageView.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            /* Set the position of this new fab button same as that of our existing fab button */
            imageView.setX(view.getX());
            imageView.setY(view.getY());

            frame.addView(imageView);
        }
        imageView.setVisibility(View.VISIBLE);
        // Start animation
        imageView.animate().scaleX(35.0f).scaleY(35.0f).setDuration(500).setInterpolator(new AccelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, AddNewTransactionActivity.class);
                        intent.putExtra("action", "generic");
                        intent.setAction("generic");
                        overridePendingTransition(0, 0);
                        startActivity(intent);
                    }
                });
        /*Intent intent = new Intent(MainActivity.this, AddNewTransactionActivity.class);
        intent.putExtra("action", "generic");
        intent.setAction("generic");
        Bundle bundle = ActivityOptionsCompat.makeScaleUpAnimation(view,0,0,view.getWidth(),view.getHeight()).toBundle();
        startActivity(intent, bundle);*/
    }

    /**
     * Resets the size of the dynamically added fab. Used in OnResume
     */
    public void resetFabScale() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_expand_menu_button);
        if (fab != null) {
            // Visibility is set to gone or else it comes up over our actual fab hiding its + image
            fab.setVisibility(View.GONE);
            fab.setScaleX(1.0f);
            fab.setScaleY(1.0f);
        }
    }
}