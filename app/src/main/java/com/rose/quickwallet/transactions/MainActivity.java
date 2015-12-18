package com.rose.quickwallet.transactions;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.rose.quickwallet.AlarmReceiver;
import com.rose.quickwallet.BaseActivity;
import com.rose.quickwallet.EnterPinActivity;
import com.rose.quickwallet.R;
import com.rose.quickwallet.SettingsActivity;
import com.rose.quickwallet.callbackhepers.ItemTouchHelperCallback;
import com.rose.quickwallet.callbackhepers.RecyclerViewCallback;
import com.rose.quickwallet.myWallet.WalletActivity;
import com.rose.quickwallet.quickblox.Consts;
import com.rose.quickwallet.quickblox.DialogUtils;
import com.rose.quickwallet.quickblox.GoCloudActivity;
import com.rose.quickwallet.quickblox.RetreiveUsersService;
import com.rose.quickwallet.quickblox.pushnotifications.GCMClientHelper;
import com.rose.quickwallet.tutorial.TutorialActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements RecyclerViewCallback {
    //ArrayList<Map<String,String>> contactsData = new ArrayList<>();
    //SimpleCursorAdapter simpleCursorAdapter;
    //AutoCompleteTextView searchText;
    //Uri uri = ContactsContract.Contacts.CONTENT_URI;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    //private CoordinatorLayout coordinatorLayout;
    private boolean isSignedIn =false;
    private GCMClientHelper pushClientManager;
    private SharedPreferences preferences;
    boolean shouldAnimate = false;
    //private DrawerLayout drawerLayout;
    //private NavigationView navigationView;
    //private String searchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //TraceCompat.beginSection("start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isSignedIn = preferences.getBoolean(Consts.IS_SIGNED_UP, false);
        //DialogUtils.showLong(getApplicationContext(),"isSignedIn: " + isSignedIn);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isFirstStartAfterGCMUpdate = preferences.getBoolean("isFirstStartAfterGCMUpdate", true);
                if(isFirstStartAfterGCMUpdate){
                    Intent goCloud = new Intent(getApplicationContext(), GoCloudActivity.class);
                    startActivity(goCloud);
                    Intent tutorial = new Intent(MainActivity.this,TutorialActivity.class);
                    startActivity(tutorial);
                    finish();
                    preferences.edit().putBoolean("isFirstStartAfterGCMUpdate",false).apply();
                }
                else if(preferences.getBoolean("securitySwitch",false) && !getIntent().getAction().equals("enter")){
                    Intent password = new Intent(MainActivity.this, EnterPinActivity.class);
                    password.setAction("ENTER_PASSWORD");
                    startActivity(password);
                    finish();
                }
            }
        });
        thread.start();

        /*if(Build.VERSION.SDK_INT < 21) {
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main_activity));
            setTitle("");
            setupSearchEditText();
        }
        else
            setupSearchView();*/

        shouldAnimate = true;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);
        requestPermissions();
        //loadAd();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        //coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_coordinator_layout);
        adapter = new RecyclerAdapter(new ArrayList<RecyclerViewItem>(), MainActivity.this);
        recyclerView.setAdapter(adapter);
        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback((RecyclerAdapter) recyclerView.getAdapter());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        /*DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback((RecyclerAdapter) recyclerView.getAdapter());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        databaseHelper.close();*/

        if(isSignedIn){
            QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
            final QBUser user = new QBUser();
            user.setEmail(preferences.getString(Consts.USER_LOGIN, null));
            user.setPassword(preferences.getString(Consts.USER_PASSWORD, null));
            //DialogUtils.showLong(getApplicationContext(), "creating session for user: " + user);
            QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
                @Override
                public void onSuccess(QBSession session, Bundle params) {
                    //DialogUtils.showLong(getApplicationContext(),"Registering GCM");
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent retrieveUsers = new Intent(getApplicationContext(), RetreiveUsersService.class);
                            retrieveUsers.putExtra("createSession",false);
                            startService(retrieveUsers);
                        }
                    });
                    th.start();

                    pushClientManager = new GCMClientHelper(MainActivity.this, Consts.PROJECT_NUMBER);
                    pushClientManager.registerIfNeeded(new GCMClientHelper.RegistrationCompletedHandler() {
                        @Override
                        public void onSuccess(String registrationId, boolean isNewRegistration) {
                            //Toast.makeText(MainActivity.this, registrationId,Toast.LENGTH_SHORT).show();
                            // SEND async device registration to your back-end server
                            // linking user with device registration id
                            // POST https://my-back-end.com/devices/register?user_id=123&device_id=abc
                        }

                        @Override
                        public void onFailure(String ex) {
                            super.onFailure(ex);
                            //Toast.makeText(MainActivity.this,"unable to create session",Toast.LENGTH_LONG).show();
                            // If there is an error registering, don't just keep trying to register.
                            // Require the user to click a button again, or perform
                            // exponential back-off when retrying.
                        }
                    });
                }

                @Override
                public void onError(List<String> errors) {
                    // errors
                }
            });
        }
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                // we will not get a value  at first start, so true will be returned
                boolean isFirstStart = preferences.getBoolean("isFirstStart",true);
                // if it was the first app start
                if(isFirstStart) {
                    drawerLayout.openDrawer(Gravity.LEFT);
                    SharedPreferences.Editor e = preferences.edit();
                    // we save the value "false", indicating that it is no longer the first appstart
                    e.putBoolean("isFirstStart", false);
                    e.apply();
                }
                setupNavigationHeader();
            }
        });
        t.start();

        /*searchText = (AutoCompleteTextView) findViewById(R.id.search_text_view);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (simpleCursorAdapter != null) {
                    simpleCursorAdapter.getFilter().filter(charSequence);
                    searchText.setAdapter(simpleCursorAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView)view.findViewById(R.id.contact_name);
                searchText.setText(textView.getText());
            }
        });*/
        /*recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        RecyclerAdapter adapter = new RecyclerAdapter(databaseHelper.getData(),this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        databaseHelper.close();*/
        /*final ContentResolver cr = getContentResolver();
        //Cursor cur = cr.query(uri, null, null, null, null);
        String from[] = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        int to[] = {R.id.contact_name, R.id.contact_image};
        simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.search_contacts_list_item, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        simpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE '" + constraint + "%'";
                String[] selectionArgs = null;
                Cursor cur = cr.query(uri, null, selection, selectionArgs, null);
                return cur;
            }
        });
        /*if(getIntent().getAction().equals(Intent.ACTION_SEARCH))
            search(getIntent().getStringExtra(SearchManager.QUERY));*/
        //searchText.setAdapter(simpleCursorAdapter);
        //cur.close();
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
        if(getIntent().getAction() != null) {
            if (getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
                search(getIntent().getStringExtra(SearchManager.QUERY));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().findItem(R.id.nav_transactions).setChecked(true);
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        //adapter.refreshDataList(databaseHelper.getData(),false);
        adapter.setDataList(databaseHelper.getData());
        ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getAdapter().notifyDataSetChanged();
        shouldAnimate = false;
        /*ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback((RecyclerAdapter) recyclerView.getAdapter());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);*/

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                /*DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //recyclerView.getAdapter().notifyDataSetChanged();
                        adapter.refreshDataList(databaseHelper.getData(),true);

                    }
                });*/
                //recyclerView.getAdapter().notifyDataSetChanged();
                //databaseHelper.close();
                setServiceAlarm();
                /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                if(preferences.getBoolean("notificationPersistent",false))
                    createPersistentNotification();
                else
                    cancelPersistentNotification();*/
            }
        });
        thread.start();
        if(Build.VERSION.SDK_INT < 21)
            setupSearchEditText();
        else
            setupSearchView();
        //((TextView)recyclerView.findViewById(R.id.lent_header)).setText("Lent: " + databaseHelper.totalLent());

        //TraceCompat.endSection();
        /*DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        RecyclerAdapter adapter = new RecyclerAdapter(databaseHelper.getData(),this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback((RecyclerAdapter)recyclerView.getAdapter());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        databaseHelper.close();*/
        //displayTutorial();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    /**public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (simpleCursorAdapter != null) {
            simpleCursorAdapter.getFilter().filter(s);
            searchText.setAdapter(simpleCursorAdapter);
     }
     }*/

    public void start(View view) {
        Intent intent = new Intent(this, AddNewTransactionActivity.class);
        intent.putExtra("action", "generic");
        intent.setAction("generic");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivity(intent);
        overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);

    }

    public void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        SearchView searchView = (SearchView) findViewById(R.id.search_view_home);
        searchView.setSearchableInfo(searchableInfo);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return false;
            }
        });
    }

    public void setupSearchEditText(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.search_edit_text);
            actionBar.setDisplayShowTitleEnabled(false);
            setupSearchView();
        }
    }

    public void search(String s) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        adapter.setDataList(databaseHelper.search(s));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onItemSwiped(final RecyclerViewItem item) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.updateItemOnSwipe(item.getName());
        RelativeLayout header = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.recycler_view_header_layout, recyclerView,false);
        TextView totalLent = (TextView) header.findViewById(R.id.lent_header);
        //TextView totalLent = (TextView) recyclerView.findViewById(R.id.lent_header);
        TextView totalBorrowed = (TextView) header.findViewById(R.id.borrowed_header);
        //TextView totalBorrowed = (TextView) recyclerView.findViewById(R.id.borrowed_header);
        totalLent.setText("Lent: " + databaseHelper.totalLent());
        totalBorrowed.setText("Borrowed: " + databaseHelper.totalBorrowed());
        //recyclerView.getAdapter().notifyDataSetChanged();
        databaseHelper.close();
        Snackbar.make(findViewById(R.id.activity_coordinator_layout), "Any pending balance with " + item.getName() + " is now cleared", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
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

    public void onAdd(View v) {
        Intent intent = new Intent(getApplicationContext(), AddNewTransactionActivity.class);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra("action", "generic");
        startActivity(intent);
        //overridePendingTransition(R.anim.add_activity_enter_animation,R.anim.no_animation);
    }

    /**public void createPersistentNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent addActivity = new Intent(this,AddNewTransactionActivity.class);
        addActivity.putExtra("action","generic");
        addActivity.setAction("notification");
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("QuickWallet")
                .setContentText("Tap to add a transaction")
                .setContentIntent(PendingIntent.getActivity(this, 456, addActivity,PendingIntent.FLAG_CANCEL_CURRENT))
                .setOngoing(true);
        notificationManager.notify(5672, builder.build());
    }*/

    /**public void cancelPersistentNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(5672);
    }*/

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
        String interval = preferences.getString("notificationInterval","12");
        AlarmManager alarm = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        if(preferences.getBoolean("notificationSwitch",true)) {
            switch (interval) {
                case "30":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15 * 60 * 1000, AlarmManager.INTERVAL_HALF_HOUR, pIntent);
                    break;
                case "60":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 60 * 1000, AlarmManager.INTERVAL_HOUR, pIntent);
                    break;
                case "3":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 60 * 1000, 3 * 60 * 60 * 1000, pIntent);
                    break;
                case "6":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3 * 60 * 60 * 1000, 6 * 60 * 60 * 1000, pIntent);
                    break;
                case "12":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 6 * 60 * 60 * 1000, AlarmManager.INTERVAL_HALF_DAY, pIntent);
                    break;
                case "24":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 12 * 60 * 60 * 1000, AlarmManager.INTERVAL_DAY, pIntent);
                    break;
            }
        }
    }

    public void requestPermissions(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){
                android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                dialogBuilder.setMessage("The read contact permission is required to provide you with results from your contacts when adding transactions and to display the contact's name and image. No other detail from the contacts are read or used. Please grant the permission when prompted for proper functionality.")
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.show();
            }
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},2);
        }
    }

    private void loadAd(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
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
        });
    }

    public void onOpenDrawer(View v){
        drawerLayout.openDrawer(Gravity.LEFT);
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

    private void setupNavigationHeader(){
        final TextView navViewHeaderText = (TextView)LayoutInflater.from(this).inflate(R.layout.nav_header_layout, navigationView).findViewById(R.id.nav_header_text);
        if(isSignedIn){
            navViewHeaderText.setText("Sign Out");
            navViewHeaderText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Are you sure you want to sign out?")
                            .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    final ProgressDialog progressDialog = DialogUtils.getProgressDialog(MainActivity.this);
                                    if(pushClientManager!= null) {
                                        progressDialog.show();
                                        pushClientManager.unsubscribeFromPushNotifications();
                                        QBUsers.signOut(new QBEntityCallbackImpl() {
                                            @Override
                                            public void onSuccess() {
                                                preferences.edit().putBoolean(Consts.IS_SIGNED_UP, false).apply();
                                                progressDialog.hide();
                                                isSignedIn = false;
                                                navViewHeaderText.setText("Sign In");
                                                drawerLayout.closeDrawer(Gravity.LEFT);
                                                dialog.dismiss();
                                                setupNavigationHeader();
                                            }

                                            @Override
                                            public void onError(List errors) {
                                                Toast.makeText(MainActivity.this, "Error: " + errors, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, "Unable to sign out. Please check your internet connection", Toast.LENGTH_LONG).show();
                                        drawerLayout.closeDrawer(Gravity.LEFT);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    drawerLayout.closeDrawer(Gravity.LEFT);
                                }
                            })
                            .show();
                }
            });
        }
        else {
            navViewHeaderText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent goCloud = new Intent(getApplicationContext(), GoCloudActivity.class);
                    startActivity(goCloud);
                }
            });
        }
    }

    /*public void displayTutorial(){
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.fab_add, this))
                .setContentText("Here's how to highlight items on a toolbar")
                .build()
                .show();
    }*/
    /*private void displayDemoIfNeeded() {

        boolean neverShowDemoAgain = RoboDemo.isNeverShowAgain(this, DEMO_ACTIVITY_ID);

        if ( !neverShowDemoAgain && showDemo ) {
            showDemo = false;
            ArrayList<LabeledPoint> arrayListPoints = new ArrayList< LabeledPoint >();

            // create a list of LabeledPoints
            LabeledPoint p = new LabeledPoint( findViewById(R.id.fab_add), "Add transactions by clicking on this button");
            arrayListPoints.add( p );


            // start DemoActivity.
            Intent intent = new Intent( this, MainActivityDemoActivity.class );
            RoboDemo.prepareDemoActivityIntent( intent, DEMO_ACTIVITY_ID, arrayListPoints );
            startActivity(intent);
        }
    }*/
}