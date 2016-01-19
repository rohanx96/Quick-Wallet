package com.rose.quickwallet.transactions;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.rose.quickwallet.CalcActivity;
import com.rose.quickwallet.EnterPinActivity;
import com.rose.quickwallet.R;
import com.rose.quickwallet.quickblox.Consts;
import com.rose.quickwallet.quickblox.RetreiveUsersService;
import com.rose.quickwallet.quickblox.pushnotifications.PendingNotificationsDatabaseHelper;
import com.rose.quickwallet.quickblox.QuickbloxUsersDatabaseHelper;


import java.util.ArrayList;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Adds transaction to local database and send chat message to corresponding user
 * Created by rose on 23/7/15.
 */

public class AddNewTransactionActivity extends Activity {
    private String image_uri;
    private String type = "Lent";
    private String name;
    private String detail;
    private float amount = 0;
    private String contact;
    private String senderName;
    private String senderPhone;
    private DatabaseHelper databaseHelper;
    private QBChatService chatService;
    //private Spinner typeSpinner;
    private float balance = 0;
    private Context context;
    private boolean sendNotification = true;
    //static int userNumber = 1;
    private boolean isSignedUp = false;
    private int opponentUserID = -1;
    private String[] phoneNos;
    //private LoadingView loadingView;
    private SimpleCursorAdapter simpleCursorAdapter;
    AutoCompleteTextView searchText;
    private TextView txtResult; // Reference to EditText of result
    private float result = 0;     // Result of computation
    private String inStr = "0"; // Current input string
    // Previous operator: '+', '-', '*', '/', '=' or ' ' (no operator)
    private char lastOperator = ' ';

    //private int READ_CONTACT_PERMISSION_CODE = 2;
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
        //loadingView = (LoadingView) findViewById(R.id.add_transaction_loading_view);
        context = getApplicationContext();
        databaseHelper = new DatabaseHelper(this);
        setupCalc();
        //getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 305)); //440px -294dp
        //getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 345)); //440px -294dp
        //getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 365)); //440px -294dp
        setupSearchView();
        isSignedUp = preferences.getBoolean(Consts.IS_SIGNED_UP, false);
        if (isSignedUp) {
            ((CheckBox) findViewById(R.id.checkbox_send_notification)).setChecked(true);
            senderName = preferences.getString(Consts.USER_NAME, "");
            senderPhone = preferences.getString(Consts.USER_PHONE,"");
            if (!QBChatService.isInitialized()) {
                QBChatService.init(this);
            }
            chatService = QBChatService.getInstance();
            /*final QBUser user = new QBUser();
            user.setEmail(preferences.getString(Consts.USER_LOGIN, null));
            user.setPassword(preferences.getString(Consts.USER_PASSWORD, null));
            QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
                @Override
                public void onSuccess(QBSession session, Bundle params) {
                    // success, login to chat
                    user.setId(session.getUserId());

                    chatService.login(user, new QBEntityCallbackImpl() {
                        @Override
                        public void onSuccess() {
                            // success
                            try {
                                chatService.startAutoSendPresence(60);
                            } catch (SmackException.NotLoggedInException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(List errors) {
                            // error
                        }
                    });
                }

                @Override
                public void onError(List<String> errors) {
                    // errors
                }
            });*/
        }

        // Initialization when activity started through particular contact add action
        if (getIntent().getStringExtra("action").equals("add")) {
            name = getIntent().getStringExtra(SearchManager.QUERY);
            RelativeLayout relativeLayout =(RelativeLayout) findViewById(R.id.no_name_details);
            relativeLayout.setVisibility(View.GONE);
            RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.contact_details);
            TextView contactName = (TextView) linearLayout.findViewById(R.id.contact_detail_name);
            contactName.setText(name);
            Cursor cursor = databaseHelper.getItem(name);
            if (cursor.moveToFirst()) {
                image_uri = cursor.getString(cursor.getColumnIndex("ImageUri"));
                contact = cursor.getString(cursor.getColumnIndex("PhoneNo"));
            }
            getContactImageAndNumbers();
            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.contact_detail_image);
            if (image_uri != null)
                imageView.setImageURI(Uri.parse(image_uri));
            else
                imageView.setImageResource(R.drawable.contact_no_image);
            //TextView balanceDetail = (TextView)linearLayout.findViewById(R.id.contact_detail_balance);
            balance = databaseHelper.getBalance(name);
            setBalanceText(balance);
            linearLayout.setVisibility(View.VISIBLE);
        }

        // Initialization when activity started from a received notification
        if (getIntent().getStringExtra("action").equals("addNotification")){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Search for contact name using contact number
                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(getIntent().getStringExtra("contact")));
                    Cursor phoneLookup = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                    if(phoneLookup!=null && phoneLookup.moveToFirst()){
                        name = phoneLookup.getString(phoneLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                        phoneLookup.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Following code copied from getContactImageAndNumbers
                                RelativeLayout relativeLayout =(RelativeLayout) findViewById(R.id.no_name_details);
                                relativeLayout.setVisibility(View.GONE);
                                RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.contact_details);
                                TextView contactName = (TextView) linearLayout.findViewById(R.id.contact_detail_name);
                                contactName.setText(name);
                                Cursor cursor = databaseHelper.getItem(name);
                                if (cursor.moveToFirst()) {
                                    image_uri = cursor.getString(cursor.getColumnIndex("ImageUri"));
                                    contact = cursor.getString(cursor.getColumnIndex("PhoneNo"));
                                }
                                getContactImageAndNumbers();
                                ImageView imageView = (ImageView) linearLayout.findViewById(R.id.contact_detail_image);
                                if (image_uri != null)
                                    imageView.setImageURI(Uri.parse(image_uri));
                                else
                                    imageView.setImageResource(R.drawable.contact_no_image);
                                balance = databaseHelper.getBalance(name);
                                setBalanceText(balance);
                                linearLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
            thread.start();
            amount = getIntent().getFloatExtra("amount", 0);
            //Following code copied from onActivityResult
            if (amount > 0) {
                type = "Borrowed";
                //RadioGroup group = (RadioGroup) findViewById(R.id.type_options);
                //group.check(R.id.radio_button_borrowed);
            }
            else
                amount = -1* amount;
            //Button amountView = (Button) findViewById(R.id.amount_edit_text_layout);
            //RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.money_details);
            TextView textView = (TextView) findViewById(R.id.money_detail_balance);
            textView.setText(getString(R.string.amount_colon) + amount);
            textView = (TextView) findViewById(R.id.money_detail_name);
            textView.setText(type);
            //amountView.setText(getString(R.string.amount_colon) + amount);
            //amountView.setTextColor(Color.BLACK);
            if (type.equals("Lent")) {
                textView.setTextColor(setColorGreen());
            } else {
                textView.setTextColor(setColorRed());
            }
            /*if (name == null)
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 430));//560px - 374dp
            else {
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 525)); //740px - 494dp
            }*/
            //relativeLayout.setVisibility(View.VISIBLE);
            //Set details in detail edit text
            detail = getIntent().getStringExtra("details");
            EditText detailsText = (EditText) findViewById(R.id.details_edit_text);
            detailsText.setText(detail);
            // No need to send notification for this transaction
            ((CheckBox) findViewById(R.id.checkbox_send_notification)).setChecked(false);
            sendNotification = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startReveal();
        if (!QBChatService.isInitialized()) {
            QBChatService.init(this);
            chatService = QBChatService.getInstance();
            databaseHelper = new DatabaseHelper(this);
        }
    }

    /**@Override
    protected void onNewIntent(Intent intent) {
        if (ContactsContract.Intents.SEARCH_SUGGESTION_CLICKED.equals(intent.getAction())) {
            //handles suggestion clicked query
            //---------databaseHelper = new DatabaseHelper(getApplicationContext());
            getDisplayNameForContact(intent);
            RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.contact_details);
            TextView contactName = (TextView) linearLayout.findViewById(R.id.contact_detail_name);
            contactName.setText(name);
            //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //RelativeLayout relativeLayout =(RelativeLayout) findViewById(R.id.new_transaction);
            //params.addRule(RelativeLayout.BELOW,R.id.search_view_card);
            //relativeLayout.addView(contactName,params);
            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.contact_detail_image);
            if (image_uri != null)
                imageView.setImageURI(Uri.parse(image_uri));
            else
                imageView.setImageResource(R.drawable.contact_no_image);
            //TextView balanceDetail = (TextView)linearLayout.findViewById(R.id.contact_detail_balance);
            balance = databaseHelper.getBalance(name);
            setBalanceText(balance);
            linearLayout.setVisibility(View.VISIBLE);
            //---------databaseHelper.close();

        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            name = intent.getStringExtra(SearchManager.QUERY);
            //------------databaseHelper = new DatabaseHelper(getApplicationContext());
            RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.contact_details);
            TextView contactName = (TextView) linearLayout.findViewById(R.id.contact_detail_name);
            contactName.setText(name);
            Cursor cursor = databaseHelper.getItem(name);
            if (cursor.moveToFirst()) {
                image_uri = cursor.getString(cursor.getColumnIndex("ImageUri"));
                contact = cursor.getString(cursor.getColumnIndex("PhoneNo"));
            }
            getDisplayNameForContact(intent);
            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.contact_detail_image);
            if (image_uri != null)
                imageView.setImageURI(Uri.parse(image_uri));
            else
                imageView.setImageResource(R.drawable.contact_no_image);
            //TextView balanceDetail = (TextView)linearLayout.findViewById(R.id.contact_detail_balance);
            balance = databaseHelper.getBalance(name);
            setBalanceText(balance);
            linearLayout.setVisibility(View.VISIBLE);
            //---------databaseHelper.close();
        }
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 234) {
            Button amountView = (Button) findViewById(R.id.amount_edit_text_layout);
            if (resultCode == RESULT_OK) {
                float result = data.getFloatExtra("RESULT", 0);
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.money_details);
                if (result == 0) {
                    amount = 0;
                    relativeLayout.setVisibility(View.GONE);
                    amountView.setText(getString(R.string.enter_amount));
                    if (name == null)
                        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 345));//440px -294dp hdpi
                    else {
                        //if (balance == 0)
                        //    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 390));//560px - 374dp
                        //else
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 440));//640px -427dp
                    }
                } else {
                    amount = result;
                    if (amount < 0) {
                        type = "Borrowed";
                        amount = -1 * amount;
                        RadioGroup group = (RadioGroup) findViewById(R.id.type_options);
                        group.check(R.id.radio_button_borrowed);
                    }
                    TextView textView = (TextView) relativeLayout.findViewById(R.id.money_detail_balance);
                    textView.setText(getString(R.string.amount_colon) + amount);
                    textView = (TextView) findViewById(R.id.money_detail_name);
                    textView.setText(type);
                    amountView.setText(getString(R.string.amount_colon) + amount);
                    amountView.setTextColor(Color.BLACK);
                    if (type.equals("Lent")) {
                        textView.setTextColor(setColorGreen());
                        //amount = amount;
                    } else {
                        textView.setTextColor(setColorRed());
                        //amount = -1*amount;
                    }
                    if (name == null)
                        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 430));//560px - 374dp
                    else {
                        //if (balance == 0)
                         //   getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 485));//660px - 440dp
                        //else
                            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 525)); //740px - 494dp
                    }
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }*/

    private void setupSearchView() {
        /*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) findViewById(R.id.search_view);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);
        searchView.clearFocus();*/
        searchText = (AutoCompleteTextView) findViewById(R.id.search_view);
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
                name = ((TextView)view.findViewById(R.id.contact_name)).getText().toString();
                image_uri = null;
                searchText.setText("");
                getContactImageAndNumbers();
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.no_name_details);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
                alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                alphaAnimation.setDuration(500);
                relativeLayout.startAnimation(alphaAnimation);
                relativeLayout.setVisibility(View.GONE);
                relativeLayout.startAnimation(new AlphaAnimation(1.0f, 0.0f));
                RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.contact_details);
                TextView contactName = (TextView) linearLayout.findViewById(R.id.contact_detail_name);
                contactName.setText(name);
                ImageView imageView = (ImageView) linearLayout.findViewById(R.id.contact_detail_image);
                if (image_uri != null)
                    imageView.setImageURI(Uri.parse(image_uri));
                else
                    imageView.setImageResource(R.drawable.contact_no_image);
                balance = databaseHelper.getBalance(name);
                setBalanceText(balance);
                alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(500);
                alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                linearLayout.startAnimation(alphaAnimation);
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

        //final ContentResolver cr = getContentResolver();
        //Cursor cur = cr.query(uri, null, null, null, null);
        String from[] = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        int to[] = {R.id.contact_name, R.id.contact_image};
        simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.search_contacts_list_item, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        simpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + constraint + "%'";
                return getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, selection, null, null);
            }
        });
    }

    private void getContactImageAndNumbers(){
        String contactId = null;
        Cursor phoneCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
                "DISPLAY_NAME = '" + name + "'", null, null);
        if (phoneCursor.moveToFirst()) {
            int idDisplayName = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            name = phoneCursor.getString(idDisplayName);
            image_uri = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            contactId = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
        phoneCursor.close();
        if (contact == null) {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            phoneNos = new String[phones.getCount()];
            int count = 0;
            if (phones.moveToFirst()) {
                if (phones.getCount() > 1) {
                    do {
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        switch (type){
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                phoneNos[count] = "   Mobile:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                phoneNos[count] = "   Home:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                                phoneNos[count] = "   Work Mobile:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                phoneNos[count] = "   Work:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                                phoneNos[count] = "   " + phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)) + ":          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                phoneNos[count] = "   Other:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                        }
                    } while (phones.moveToNext());
                } else {
                    contact = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            }
            phones.close();
        }
    }

    public void onClickSearch(View view){
        String name_temp = searchText.getText().toString();
        if (!name_temp.replaceAll(" ", "").equals("")){
            name = name_temp;
            image_uri = null;
            RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.contact_details);
            TextView contactName = (TextView) linearLayout.findViewById(R.id.contact_detail_name);
            contactName.setText(name);
            Cursor cursor = databaseHelper.getItem(name);
            if (cursor.moveToFirst()) {
                image_uri = cursor.getString(cursor.getColumnIndex("ImageUri"));
                contact = cursor.getString(cursor.getColumnIndex("PhoneNo"));
            }
            getContactImageAndNumbers();
            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.contact_detail_image);
            if (image_uri != null)
                imageView.setImageURI(Uri.parse(image_uri));
            else
                imageView.setImageResource(R.drawable.contact_no_image);
            //TextView balanceDetail = (TextView)linearLayout.findViewById(R.id.contact_detail_balance);
            balance = databaseHelper.getBalance(name);
            setBalanceText(balance);
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.no_name_details);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
            alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnimation.setDuration(500);
            relativeLayout.startAnimation(alphaAnimation);
            relativeLayout.setVisibility(View.GONE);
            alphaAnimation = new AlphaAnimation(0f, 1f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            linearLayout.startAnimation(alphaAnimation);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onClickDeleteName(View view){
        RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.no_name_details);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.contact_details);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
        alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimation.setDuration(500);
        relativeLayout.startAnimation(alphaAnimation);
        relativeLayout.setVisibility(View.INVISIBLE);
        relativeLayout.startAnimation(new AlphaAnimation(1.0f, 0.0f));
        alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        linearLayout.startAnimation(alphaAnimation);
        linearLayout.setVisibility(View.VISIBLE);
    }

    /**private void getDisplayNameForContact(Intent intent) {
        String contactId = null;
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            if (contact == null) {
                Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
                        "DISPLAY_NAME = '" + name + "'", null, null);
                if (cursor.moveToFirst()) {
                    contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                }
                cursor.close();
            }
        } else {
            Cursor phoneCursor = getContentResolver().query(intent.getData(), null, null, null, null);
            if (phoneCursor.moveToFirst()) {
                int idDisplayName = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                name = phoneCursor.getString(idDisplayName);
                image_uri = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                contactId = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts._ID));
            }
            phoneCursor.close();
        }
        if (contact == null) {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            phoneNos = new String[phones.getCount()];
            int count = 0;
            if (phones.moveToFirst()) {
                if (phones.getCount() > 1) {
                    do {
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        switch (type){
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                phoneNos[count] = "   Mobile:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                phoneNos[count] = "   Home:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                                phoneNos[count] = "   Work Mobile:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                phoneNos[count] = "   Work:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                                phoneNos[count] = "   " + phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)) + ":          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                phoneNos[count] = "   Other:          ";
                                phoneNos[count] += number;
                                Log.i("Number", number);
                                count++;
                                break;
                        }
                        /*if (type == ContactsContract.CommonDataKinds.Phone.TYPE_HOME || type ==ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE || type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK || type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE || type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                            count ++;
                            phoneNos[count] = number;
                            Log.i("Number", number);
                            count++;
                        }
                /*int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    contact = number;
                    Log.i("contact",contact);
                    break;
                }
                    } while (phones.moveToNext());
                    //phoneNos[count] = "Please note that setting the contact correctly is essential in sending notifications";
                } else {
                    contact = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //contact = contact.replaceAll("-",""); not needed phoneNumberUtility does it
                }
            }
            phones.close();
        }
    }*/

    public void onAdd(View view) {
        //Button amountLayout = (Button) findViewById(R.id.amount_edit_text_layout);
        if (name == null || name.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_help_enter_name), Toast.LENGTH_LONG).show();
            return;
        }
        if (amount == 0) {
            //amountLayout.setTextColor(Color.RED);
            Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        //loadingView.setLoading(true);
        //CheckBox notification = (CheckBox) findViewById(R.id.checkbox_send_notification);
        //sendNotification = notification.isChecked();
        //------------databaseHelper = new DatabaseHelper(getApplicationContext());

        // Set opponentUserId and contact from database if available
        Cursor cursor = databaseHelper.getItem(name);
        if (cursor.moveToFirst()) {
            opponentUserID = cursor.getInt(cursor.getColumnIndex("QuickbloxID"));
            if (contact == null)
                contact = cursor.getString(cursor.getColumnIndex("PhoneNo"));
        }

        if (type.equals("Borrowed"))
            amount = -1 * amount;
        EditText detailText = (EditText) findViewById(R.id.details_edit_text);
        detail = detailText.getText().toString();

        // if user signed up and contact not saved make the user choose the associated contact and send chat message
        if (contact == null && isSignedUp) {
            if(sendNotification) {
                if(phoneNos.length > 0) { // no contact choser dialog shown if no number found in the contact
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.dialog_contact_choser_item);
                    arrayAdapter.addAll(phoneNos);
                    DialogPlus dialog = DialogPlus.newDialog(this)
                            .setAdapter(arrayAdapter)
                            .setOnItemClickListener(new OnItemClickListener() {
                                @Override
                                public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                                    Log.i("Position : ", Integer.toString(position));
                                    if (position == 0 || position == phoneNos.length + 1)
                                        return;
                                    else {
                                        phoneNos[position - 1] = phoneNos[position - 1].replaceAll("[^0-9+]", "");
                                        contact = phoneNos[position - 1];
                                        if (isSignedUp && opponentUserID == -1) {
                                            //userNumber = 1;
                                            //retrieveAllUsersFromPage(1);
                                            getUserIDandSendMessage();
                                        } else if (isSignedUp && opponentUserID != -1) {
                                /*Chat chat = new PrivateChatImpl(AddNewTransactionActivity.this, opponentUserID);
                                try {
                                    chat.sendMessage(createChatMessage());
                                } catch (XMPPException e) {
                                    Log.e("Mess", "failed to send a message", e);
                                } catch (SmackException sme) {
                                    Log.e("Mess", "failed to send a message", sme);
                                }*/
                                            //DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
                                            databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                                            sendChatMessage(true);
                                        }
                                        dialog.dismiss();
                                    }
                                }
                            })
                            .setFooter(R.layout.dialog_contact_choser_footer)
                            .setHeader(R.layout.dialog_contact_choser_header)
                            .setPadding(10, 10, 10, 10)
                            .create();
                    dialog.show();
                }
                else { // no number so just add transaction without sending notification
                    databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                    resetDetailsAfterTransactionAdded();
                }
            /*final android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view1 = inflater.inflate(R.layout.dialog_contact_choser_footer, null);
            dialogBuilder.setView(view1);
            dialogBuilder.setTitle("Choose contact to associate");
            ListView lv = (ListView) view1.findViewById(R.id.dialog_contacts);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
            arrayAdapter.addAll(phoneNos);
            lv.setAdapter(arrayAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    contact = phoneNos[position].substring(phoneNos[position].lastIndexOf(' ') + 1);
                    if (isSignedUp && opponentUserID == -1) {
                        //userNumber = 1;
                        //retrieveAllUsersFromPage(1);
                        getUserIDandSendMessage();
                    } else if (isSignedUp && opponentUserID != -1) {
                        DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
                        databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                        sendChatMessage(true);
                    }
                }
            });*/
                //dialogBuilder.setMessage("Please choose the number correctly else notifications will not be delivered");
                //dialogBuilder.show();
            }
            else{
                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                resetDetailsAfterTransactionAdded();
            }
        }

        // If contact is already saved or user not signed in
        else {
            // if user not signed up we can save data without contact and UserID
            if (!isSignedUp) {
                //DialogUtils.showLong(context, "Saving data without opponentID because not signed in");
                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                resetDetailsAfterTransactionAdded();
            }

            // if user is signed in userID not saved find the userID and then send chat message and then save data
            if (isSignedUp && opponentUserID == -1) {
                /*userNumber = 1;
                retrieveAllUsersFromPage(1);*/
                if(sendNotification)
                    getUserIDandSendMessage();
                else{
                    databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                    resetDetailsAfterTransactionAdded();
                }
            }
            // opponentUserId saved so directly send message and save data
            else if (isSignedUp && opponentUserID != -1) {
                /*Chat chat = new PrivateChatImpl(AddNewTransactionActivity.this, opponentUserID);
                try {
                    chat.sendMessage(createChatMessage());
                } catch (XMPPException e) {
                    Log.e("Mess", "failed to send a message", e);
                } catch (SmackException sme) {
                    Log.e("Mess", "failed to send a message", sme);
                }*/
                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                if(sendNotification)
                    sendChatMessage(true);
                else
                    resetDetailsAfterTransactionAdded();
                //DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
            }
        }
        //----------databaseHelper.close();

    }

    /*public void onClearBalance(View v) {
        //---------databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.updateItemOnSwipe(name);
        //---------databaseHelper.close();
        TextView balanceText = (TextView) findViewById(R.id.contact_detail_balance);
        balanceText.setText("No Pending Balance");
        balanceText.setTextColor(Color.BLACK);
        balance = 0;
        Button clearBalance = (Button) findViewById(R.id.clear_balance_button);
        clearBalance.setVisibility(View.GONE);
        if (amount == 0) {
            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 390)); //560
        } else
            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 485));
    }*/

    public void setBalanceText(float balance) {
        TextView balanceDetail = (TextView) findViewById(R.id.contact_detail_balance);
        if (balance < 0) {
            balanceDetail.setText(getString(R.string.pending_balance_line) + getString(R.string.borrowed_colon) + Float.toString(-1 * balance));
            balanceDetail.setTextColor(setColorRed());
            //Button clearBalance = (Button) findViewById(R.id.clear_balance_button);
            //clearBalance.setVisibility(View.VISIBLE);
            /*if (amount == 0) {
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 442));// 640px -427dp
            } else
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 525));*/
        }
        if (balance > 0) {
            balanceDetail.setText(getString(R.string.pending_balance_line) + getString(R.string.lent_colon) + Float.toString(balance));
            balanceDetail.setTextColor(setColorGreen());
            //Button clearBalance = (Button) findViewById(R.id.clear_balance_button);
            //clearBalance.setVisibility(View.VISIBLE);
            /*if (amount == 0) {
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 442)); //640
            } else
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 525));*/
        }
        if (balance == 0) {
            balanceDetail.setText(getString(R.string.no_balance));
            balanceDetail.setTextColor(Color.BLACK);
            /*Button clearBalance = (Button) findViewById(R.id.clear_balance_button);
            clearBalance.setVisibility(View.GONE);
            if (amount == 0) {
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 390)); //560
            } else
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 485));*/
            /*if (amount == 0) {
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 442)); //640
            } else
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 525));*/
        }
    }

    public void onTypeRadioButtonSelected(View v) {
        if (v.getId() == R.id.radio_button_lent) {
            type = "Lent";
            TextView textView = (TextView) findViewById(R.id.money_detail_name);
            textView.setText(type);
            textView.setTextColor(setColorGreen());
            if(amount<0) {
                amount = -1 * amount;
                txtResult.setText(String.valueOf(amount));
            }

            //amount = -1 * amount;
        } else if (v.getId() == R.id.radio_button_borrowed) {
            type = "Borrowed";
            TextView textView = (TextView) findViewById(R.id.money_detail_name);
            textView.setText(type);
            textView.setTextColor(setColorRed());
            if(amount<0){
                amount = -1*amount;
                txtResult.setText(String.valueOf(amount));
            }
        }
    }

    public void onCancel(View v) {
        if(isSignedUp)
            logoutFormChat();
        finish();
    }

    /*public int convertDPToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }*/

    public int setColorRed() {
        return Color.parseColor("#ffc94c4c");
    }

    public int setColorGreen() {
        return Color.parseColor("#ff509f4c");
    }

    public void startCalCActivity(View v) {
        Intent calcActivity = new Intent(this, CalcActivity.class);
        startActivityForResult(calcActivity, 234);
    }

    /**private void retrieveAllUsersFromPage(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(100);
        DialogUtils.showLong(context, "Finding userID");
        QBUsers.getUsers(pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {

                for (QBUser user : users) {
                    if (PhoneNumberUtils.compare(context, user.getPhone(), contact)) {
                        opponentUserID = user.getId();
                        Log.i("userId", Integer.toString(opponentUserID));
                        //--------databaseHelper = new DatabaseHelper(context);
                        databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                        //---------databaseHelper.close();
                        if (isSignedUp && opponentUserID != -1) {
                            //Chat chat = new PrivateChatImpl(AddNewTransactionActivity.this, opponentUserID);
                            try {
                                chat.sendMessage(createChatMessage());
                            } catch (XMPPException e) {
                                Log.e("Mess", "failed to send a message", e);
                            } catch (SmackException sme) {
                                Log.e("Mess", "failed to send a message", sme);
                            }//
                            DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
                            sendChatMessage(true);
                        } else
                            resetDetailsAfterTransactionAdded();
                        return;
                    }
                    Log.d("User: ", "N" + userNumber + " is: " + user);
                    ++userNumber;
                }

                //save data if no matching user found
                DialogUtils.showLong(context, "No matching userId found");
                //-----------databaseHelper = new DatabaseHelper(context);
                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                //------------databaseHelper.close();
                resetDetailsAfterTransactionAdded();
                //int currentPage = params.getInt(Consts.CURR_PAGE);
                //int totalEntries = params.getInt(Consts.TOTAL_ENTRIES);

                //if (userNumber < totalEntries) {
                    retrieveAllUsersFromPage(currentPage + 1);
                }//
            }

            @Override
            public void onError(List<String> errors) {
                DialogUtils.showLong(context, "Error retrieving users");
                //------------databaseHelper = new DatabaseHelper(context);
                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                //------------databaseHelper.close();
                resetDetailsAfterTransactionAdded();
            }
        });
    }*/

    public void getUserIDandSendMessage() {
        //Toast.makeText(AddNewTransactionActivity.this, "Finding userID", Toast.LENGTH_SHORT).show();
        final QuickbloxUsersDatabaseHelper usersDatabaseHelper = new QuickbloxUsersDatabaseHelper(context);
        ArrayList<Integer> ids = usersDatabaseHelper.getUserID(contact);
        /*AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                ArrayList<Integer> ids = usersDatabaseHelper.getUserID(contact);
                return ids;
            }

            @Override
            protected void onPostExecute(Object o) {
                ArrayList<Integer> ids = (ArrayList<Integer>) o;
                Toast.makeText(AddNewTransactionActivity.this, "Found userID sending message", Toast.LENGTH_SHORT).show();
                opponentUserID = ids.get(0);
                Log.i("userId", Integer.toString(opponentUserID));
                //--------databaseHelper = new DatabaseHelper(context);
                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
                //---------databaseHelper.close();
                if (isSignedUp && opponentUserID != -1) {
                    DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
                    sendChatMessage(true);
                } else
                    resetDetailsAfterTransactionAdded();
            }
        };
        task.execute();
    }*/
        int noOfIds = ids.size();
        if (noOfIds == 1) {
            Toast.makeText(AddNewTransactionActivity.this, getString(R.string.toast_found_user), Toast.LENGTH_SHORT).show();
            opponentUserID = ids.get(0);
            Log.i("userId", Integer.toString(opponentUserID));
            //--------databaseHelper = new DatabaseHelper(context);
            databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
            //---------databaseHelper.close();
            if (isSignedUp && opponentUserID != -1) {
                //DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
                sendChatMessage(true);
            } else
                resetDetailsAfterTransactionAdded();
        }
        else {
            opponentUserID = -1; // either contact has several IDs or no id
            databaseHelper.saveData(name,image_uri,amount,type,detail,contact,opponentUserID);
            if(noOfIds != 0) {
                for (int i = 0; i < noOfIds; i++) {
                    opponentUserID = ids.get(i);
                    if (isSignedUp && opponentUserID != -1) {
                        //DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
                        if (i == noOfIds - 1)
                            sendChatMessage(true);  // reset data oly after message to last userID has been sent
                        else
                            sendChatMessage(false);
                    }
                }
            }
            else {
                Toast.makeText(AddNewTransactionActivity.this, getString(R.string.toast_no_user), Toast.LENGTH_SHORT).show();
                resetDetailsAfterTransactionAdded(); // reset data if no user found
            }
        }
    }

    private QBChatMessage createChatMessage() {
        QBChatMessage chatMessage = new QBChatMessage();
        String messageText = senderName + ":";
        if (type.equals("Lent") ) {
            messageText += getString(R.string.gcm_noti_lent_message) + "("+amount+")";
        } else
            messageText += getString(R.string.gcm_noti_borrowed_message) + "("+ -1*amount +")" ;
        if (detail != null && !detail.equals(""))
            messageText += "[" + detail + "]";
        messageText+= getString(R.string.gcm_noti_end_message);
        messageText += "\n<" + senderPhone +">";
        chatMessage.setBody(messageText);
        chatMessage.setProperty("save_to_history", "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);
        return chatMessage;
    }

    private void sendChatMessage(final boolean shouldReset){
        final QBChatMessage msg = createChatMessage();

        msg.setRecipientId(opponentUserID);
        // msg.setDialogId("546cc8040eda8f2dd7ee449c"); Set the dialog Id or recipient Id
        msg.setProperty("send_to_chat", "1");
        //msg.setProperty("param2", "value2");
        PendingNotificationsDatabaseHelper databaseHelper = new PendingNotificationsDatabaseHelper(getApplicationContext());
        databaseHelper.insertNotification(msg.getBody(), msg.getRecipientId());
        databaseHelper.closeDatabase();
        if(shouldReset){
            Intent sendNotifications = new Intent(this, RetreiveUsersService.class);
            sendNotifications.putExtra("createSession",false);
            sendNotifications.putExtra("sendNotifications",true);
            startService(sendNotifications);
            resetDetailsAfterTransactionAdded();
        }
        /*QBChatService.createMessage(msg, new QBEntityCallbackImpl<QBChatMessage>() {
            @Override
            public void onSuccess(QBChatMessage result, Bundle params) {
                //DialogUtils.showLong(context, msg.getBody());
                sendPushNotification(msg);
                if (shouldReset)
                    resetDetailsAfterTransactionAdded();
            }

            @Override
            public void onError(List<String> errors) {
                if (errors.toString().equals("[Connection failed. Please check your internet connection.]")) {
                    PendingNotificationsDatabaseHelper databaseHelper = new PendingNotificationsDatabaseHelper(getApplicationContext());
                    databaseHelper.insertNotification(msg.getBody(), msg.getRecipientId());
                    databaseHelper.closeDatabase();
                }
                if (shouldReset)
                    resetDetailsAfterTransactionAdded();
            }
        });*/
    }

    private void sendPushNotification(final QBChatMessage msg){
        /*QBUser user = new QBUser();
        user.setPassword("zxc..098");
        user.setLogin("rOhanx96");
        QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {

            }
        });*/
        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(msg.getRecipientId());
        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.PRODUCTION);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setMessage(msg.getBody());
                /*event.setPushType(QBPushType.GCM);
                HashMap<String, String> data = new HashMap<String, String>();
                data.put("data.message", msg.getBody());
                data.put("data.type", "welcome message");
                event.setMessage(data);*/
                /*JSONObject json = new JSONObject();
                try {
                    // standart parameters
                    // read more about parameters formation http://quickblox.com/developers/Messages#Use_custom_parameters
                    json.put("message", msg.getBody());

                    // custom parameters
                    json.put("user_id", "234");
                    json.put("thread_id", "8343");

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

        QBMessages.createEvent(event, new QBEntityCallbackImpl<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle args) {
                Toast.makeText(AddNewTransactionActivity.this, getString(R.string.toast_sent_notification), Toast.LENGTH_LONG).show();
                Log.i("Push notification", "Sent");
            }

            @Override
            public void onError(List<String> errors) {
                Log.i("Push notification", "Error: " + errors);
                PendingNotificationsDatabaseHelper databaseHelper = new PendingNotificationsDatabaseHelper(getApplicationContext());
                databaseHelper.insertNotification(msg.getBody(), msg.getRecipientId());
                databaseHelper.closeDatabase();
            }
        });
    }

    private void resetDetailsAfterTransactionAdded() {
        //loadingView.setLoading(false);
        if(amount<0)
            Snackbar.make(findViewById(R.id.new_transaction), "Added Transaction: " + type + " " + name + " " + -1*amount, Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(findViewById(R.id.new_transaction), "Added Transaction: " + type + " " + name + " " + amount, Snackbar.LENGTH_SHORT).show();
        //Button amountLayout = (Button) findViewById(R.id.amount_edit_text_layout);
        // Set amounts and other details to null and resize the dialog
        //amountLayout.setText(getString(R.string.enter_amount));
        //amountLayout.setTextColor(Color.BLACK);
        balance = balance + amount;
        setBalanceText(balance);
        amount = 0;
        type = "Lent";
        TextView amountType = (TextView) findViewById(R.id.money_detail_name);
        amountType.setText(getString(R.string.amount_colon));
        amountType.setTextColor(getResources().getColor(R.color.cardview_shadow_start_color));
        txtResult.setText("0");
        contact = null;
        EditText detailText = (EditText) findViewById(R.id.details_edit_text);
        detailText.setText(null);
        //RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.money_details);
        //relativeLayout.setVisibility(View.GONE);
        /*if (name == null)
            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 305));//440px -294dp hdpi
        else {
            if (balance == 0)
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 390));//560px - 374dp
            else
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 440));//640px -427dp
        }*/
    }

    public void onCheckboxClick(View view){
        CheckBox checkBox = (CheckBox) view;
        if(isSignedUp)
            sendNotification = checkBox.isChecked();
        else {
            checkBox.setChecked(false);
            Toast.makeText(this, getString(R.string.toast_create_account), Toast.LENGTH_SHORT).show();
        }
    }

    public void setupCalc(){
        // Retrieve a reference to the EditText field for displaying the result.
        txtResult = (TextView) findViewById(R.id.money_detail_balance);
        txtResult.setText("0");

        // Register listener (this class) for all the buttons
        BtnListener listener = new BtnListener();
        ((Button) findViewById(R.id.btnNum0Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum1Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum2Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum3Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum4Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum5Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum6Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum7Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum8Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnNum9Id)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnAddId)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnSubId)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnMulId)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnDivId)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnClearId)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnEqualId)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnDecimal)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btnDelId)).setOnClickListener(listener);
    }

    private class BtnListener implements View.OnClickListener {
        // On-click event handler for all the buttons
        @Override
        public void onClick(View view) {
            TextView amountType = (TextView) findViewById(R.id.money_detail_name);
            switch (view.getId()) {
                // Number buttons: '0' to '9'
                case R.id.btnNum0Id:
                case R.id.btnNum1Id:
                case R.id.btnNum2Id:
                case R.id.btnNum3Id:
                case R.id.btnNum4Id:
                case R.id.btnNum5Id:
                case R.id.btnNum6Id:
                case R.id.btnNum7Id:
                case R.id.btnNum8Id:
                case R.id.btnNum9Id:
                case R.id.btnDecimal:
                    String inDigit = ((Button) view).getText().toString();
                    if(!inStr.contains(".")){
                        if (inStr.equals("0")) {
                            inStr = inDigit; // no leading zero
                        }
                        else {
                            inStr += inDigit; // accumulate input digit
                        }
                    }
                    else {
                        inStr += inDigit; // accumulate input digit
                    }
                    txtResult.setText(inStr);
                    amount= Float.parseFloat(inStr);
                    // Clear buffer if last operator is '='
                    if (lastOperator == '=') {
                        result = 0;
                        lastOperator = ' ';
                    }
                    break;

                // Operator buttons: '+', '-', '*', '/' and '='
                case R.id.btnAddId:
                    compute();
                    lastOperator = '+';
                    break;
                case R.id.btnSubId:
                    compute();
                    lastOperator = '-';
                    break;
                case R.id.btnMulId:
                    compute();
                    lastOperator = '*';
                    break;
                case R.id.btnDivId:
                    compute();
                    lastOperator = '/';
                    break;
                case R.id.btnEqualId:
                    compute();
                    lastOperator = '=';
                    break;

                // Clear button
                case R.id.btnClearId:
                    result = 0;
                    inStr = "0";
                    lastOperator = ' ';
                    txtResult.setText("0");
                    amountType.setText(getString(R.string.amount_colon));
                    break;
                case R.id.btnDelId:
                    if(inStr.length()>1)
                        inStr = inStr.substring(0,inStr.length()-1);
                    else if(inStr.length()==1) {
                        inStr = "0";
                        amountType.setText(getString(R.string.amount_colon));
                    }
                    txtResult.setText(inStr);
                    break;
            }
        }

        // User pushes '+', '-', '*', '/' or '=' button.
        // Perform computation on the previous result and the current input number,
        // based on the previous operator.
        private void compute() {
            float inNum = Float.parseFloat(inStr);
            inStr = "0";
            if (lastOperator == ' ') {
                result = inNum;
            } else if (lastOperator == '+') {
                result += inNum;
            } else if (lastOperator == '-') {
                result -= inNum;
            } else if (lastOperator == '*') {
                result *= inNum;
            } else if (lastOperator == '/') {
                result /= inNum;
            } else if (lastOperator == '=') {
                // Keep the result for the next operation
            }
            txtResult.setText(String.valueOf(result));
            amount = result;
            TextView amountType = (TextView) findViewById(R.id.money_detail_name);
            if(result<0) {
                //amountType.setText(getString(R.string.borrowed));
                amountType.setTextColor(setColorRed());
            }
            else {
                //amountType.setText(getString(R.string.lent));
                amountType.setTextColor(setColorGreen());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }

    @Override
    public void onBackPressed() {
        if(isSignedUp)
            logoutFormChat();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_from_top);
    }

    private void logoutFormChat(){
        boolean isLoggedIn = chatService.isLoggedIn();
        if(!isLoggedIn){
            return;
        }

        chatService.logout(new QBEntityCallbackImpl() {

            @Override
            public void onSuccess() {
                // success
                chatService.destroy();
            }

            @Override
            public void onError(final List list) {

            }
        });
    }

    public void startReveal(){
        // previously invisible view
        final View myView = findViewById(R.id.name_card);
        myView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                myView.removeOnLayoutChangeListener(this);
                myView.setVisibility(View.VISIBLE);
                // get the center for the clipping circle
                int cx = (myView.getLeft() + myView.getRight()) / 2;
                int cy = (myView.getTop() + myView.getBottom()) / 2;

                // get the final radius for the clipping circle
                int dx = Math.max(cx, myView.getWidth() - cx);
                int dy = Math.max(cy, myView.getHeight() - cy);
                float finalRadius = (float) Math.hypot(dx, dy);

                SupportAnimator animator =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(600);
                animator.start();
            }
        });
        View calc = findViewById(R.id.tableId);
        //TranslateAnimation animation = new TranslateAnimation(0,0,calc.getHeight(),0);
        //animation.setInterpolator(new AccelerateDecelerateInterpolator());
        //animation.setDuration(2000);
        //calc.startAnimation(animation);*/
        calc.setVisibility(View.VISIBLE);

        View buttons = findViewById(R.id.calc_action_buttons);
        //buttons.startAnimation(new TranslateAnimation(0,0,0,buttons.getHeight()));
        buttons.setVisibility(View.VISIBLE);
    }

    public void hideKeyboard(View v){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}