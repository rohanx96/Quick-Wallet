package com.rose.quickwallet.transactions;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rose.quickwallet.R;
import com.rose.quickwallet.transactions.data.DatabaseHelper;

import java.util.Currency;
import java.util.Locale;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by rohanx96 on 11/27/16.
 */

public class AddNewTransactionFragment extends Fragment {
    // Variables to hold transaction and user data
    private String image_uri;
    private String type = "Lent";
    private String name;
    private String detail;
    private float amount = 0;
    private String contact;
    //    private String senderName;
//    private String senderPhone;
    private float balance = 0; // The total balance with the selected opponent

    private DatabaseHelper databaseHelper;
    //    private QBChatService chatService;
    private Context context;
    //    private boolean sendNotification = true;
//    private boolean isSignedUp = false;
    private int opponentUserID = -1;
    //    private String[] phoneNos;
    //private LoadingView loadingView;
    private SimpleCursorAdapter simpleCursorAdapter;
    AutoCompleteTextView searchText;

    // Variables to store calculator states
    private TextView txtResult; // Reference to EditText of result
    private float result = 0;     // Result of computation
    private String inStr = "0"; // Current input string
    // Previous operator: '+', '-', '*', '/', '=' or ' ' (no operator)
    private char lastOperator = ' ';

    private String mCurrency;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_transaction,container,false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //loadingView = (LoadingView) findViewById(R.id.add_transaction_loading_view);
        databaseHelper = new DatabaseHelper(context);
        setupCalc();
         /* Remove code for resizing screen. Refer commit for version 7.0.qb.3 */
        setupSearchView();
//        isSignedUp = preferences.getBoolean(Consts.IS_SIGNED_UP, false);
//        if (isSignedUp) {
//            ((CheckBox) findViewById(R.id.checkbox_send_notification)).setChecked(true);
//            senderName = preferences.getString(Consts.USER_NAME, "");
//            senderPhone = preferences.getString(Consts.USER_PHONE,"");
//            if (!QBChatService.isInitialized()) {
//                QBChatService.init(this);
//            }
//            chatService = QBChatService.getInstance();
//            /*final QBUser user = new QBUser();
//            user.setEmail(preferences.getString(Consts.USER_LOGIN, null));
//            user.setPassword(preferences.getString(Consts.USER_PASSWORD, null));
//            QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
//            Previously included code to generate session for user so as to send chat message but not required since it can be done
//            without creating session. Refer commit for version 7.0.qb.3
//            */
//
//        }
        mCurrency = preferences.getString("prefCurrency","");

        // Initialization when activity started through particular contact add action
        if (getArguments().getString("action").equals("add")) {
            name = getArguments().getString(SearchManager.QUERY);
            RelativeLayout relativeLayout =(RelativeLayout) rootView.findViewById(R.id.no_name_details);
            relativeLayout.setVisibility(View.GONE);
            RelativeLayout linearLayout = (RelativeLayout) rootView.findViewById(R.id.contact_details);
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

        // Initialization when activity started from a received notification. No quickblox notifications now so not needed
//        if (getArguments().getString("action").equals("addNotification")){
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    // Search for contact name using contact number
//                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(getArguments().getString("contact")));
//                    Cursor phoneLookup = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
//                    if(phoneLookup!=null && phoneLookup.moveToFirst()){
//                        name = phoneLookup.getString(phoneLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
//                        phoneLookup.close();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                //Following code copied from getContactImageAndNumbers
//                                RelativeLayout relativeLayout =(RelativeLayout) rootView.findViewById(R.id.no_name_details);
//                                relativeLayout.setVisibility(View.GONE);
//                                RelativeLayout contactDetails = (RelativeLayout) rootView.findViewById(R.id.contact_details);
//                                TextView contactName = (TextView) contactDetails.findViewById(R.id.contact_detail_name);
//                                contactName.setText(name);
//                                Cursor cursor = databaseHelper.getItem(name);
//                                if (cursor.moveToFirst()) {
//                                    image_uri = cursor.getString(cursor.getColumnIndex("ImageUri"));
//                                    contact = cursor.getString(cursor.getColumnIndex("PhoneNo"));
//                                }
//                                getContactImageAndNumbers();
//                                ImageView imageView = (ImageView) contactDetails.findViewById(R.id.contact_detail_image);
//                                if (image_uri != null)
//                                    imageView.setImageURI(Uri.parse(image_uri));
//                                else
//                                    imageView.setImageResource(R.drawable.contact_no_image);
//                                balance = databaseHelper.getBalance(name);
//                                setBalanceText(balance);
//                                contactDetails.setVisibility(View.VISIBLE);
//                            }
//                        });
//                    }
//                }
//            });
//            thread.start();
//            amount = getArguments().getFloat("amount", 0);
//            //Following code copied from onActivityResult
//            if (amount > 0) {
//                type = "Borrowed";
//            }
//            else
//                amount = -1* amount;
//            TextView textView = (TextView) rootView.findViewById(R.id.money_detail_balance);
//            textView.setText(String.valueOf(amount));
//            textView = (TextView) rootView.findViewById(R.id.money_detail_name);
//            textView.setText(type);
//            if (type.equals("Lent")) {
//                textView.setTextColor(setColorGreen());
//            } else {
//                textView.setTextColor(setColorRed());
//            }
//            /* Removed code for resizing screen. Refer commit for version 7.0.qb.3 */
//            //Set details in detail edit text
//            detail = getArguments().getString("details");
//            EditText detailsText = (EditText) rootView.findViewById(R.id.details_edit_text);
//            detailsText.setText(detail);
////            // No need to send notification for this transaction because opponent already knows of the transaction
////            ((CheckBox) findViewById(R.id.checkbox_send_notification)).setChecked(false);
////            sendNotification = false;
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startReveal();
        databaseHelper = new DatabaseHelper(context);
//        if (!QBChatService.isInitialized()) {
//            QBChatService.init(this);
//            chatService = QBChatService.getInstance();
//            databaseHelper = new DatabaseHelper(this);
//        }
    }

    /**
     * Implementation for search view. Defines what happens when suggestion clicked or a new search string passed.
     * Not required because application uses edit text instead of search view now. Refer commit for version 7.0.qb.3
     * @Override
    protected void onNewIntent(Intent intent) {
    if (ContactsContract.Intents.SEARCH_SUGGESTION_CLICKED.equals(intent.getAction())) {
    } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    }
    }*/

    /*
    Managing results when returning from calculator activity.
    Removed as not required. Refer commit for version 7.0.qb.3
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 234) {
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }*/

    /** Implementation for edit text that works as search view for searching contacts */
    private void setupSearchView() {
        searchText = (AutoCompleteTextView) rootView.findViewById(R.id.search_view);
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
                RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.no_name_details);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
                alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                alphaAnimation.setDuration(500);
                relativeLayout.startAnimation(alphaAnimation);
                relativeLayout.setVisibility(View.GONE);
                relativeLayout.startAnimation(new AlphaAnimation(1.0f, 0.0f));
                RelativeLayout linearLayout = (RelativeLayout) rootView.findViewById(R.id.contact_details);
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
        String from[] = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        int to[] = {R.id.contact_name, R.id.contact_image};
        simpleCursorAdapter = new SimpleCursorAdapter(context, R.layout.search_contacts_list_item, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        simpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + constraint + "%'";
                return context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, selection, null, null);
            }
        });
    }

    private void getContactImageAndNumbers(){
//        String contactId = null;
        Cursor phoneCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
                "DISPLAY_NAME = '" + name + "'", null, null);
        if (phoneCursor.moveToFirst()) {
            int idDisplayName = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            name = phoneCursor.getString(idDisplayName);
            image_uri = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
//            contactId = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
        phoneCursor.close();
//        Do not require the contact phone numbers
//        if (contact == null) {
//            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
//            phoneNos = new String[phones.getCount()];
//            int count = 0;
//            if (phones.moveToFirst()) {
//                if (phones.getCount() > 1) {
//                    do {
//                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
//                        switch (type){
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
//                                phoneNos[count] = "   Mobile:          ";
//                                phoneNos[count] += number;
//                                Log.i("Number", number);
//                                count++;
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
//                                phoneNos[count] = "   Home:          ";
//                                phoneNos[count] += number;
//                                Log.i("Number", number);
//                                count++;
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
//                                phoneNos[count] = "   Work Mobile:          ";
//                                phoneNos[count] += number;
//                                Log.i("Number", number);
//                                count++;
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
//                                phoneNos[count] = "   Work:          ";
//                                phoneNos[count] += number;
//                                Log.i("Number", number);
//                                count++;
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
//                                phoneNos[count] = "   " + phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)) + ":          ";
//                                phoneNos[count] += number;
//                                Log.i("Number", number);
//                                count++;
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
//                                phoneNos[count] = "   Other:          ";
//                                phoneNos[count] += number;
//                                Log.i("Number", number);
//                                count++;
//                                break;
//                        }
//                    } while (phones.moveToNext());
//                } else {
//                    contact = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                }
//            }
//            phones.close();
//        }

    }

    public void onClickSearch(View view){
        String name_temp = searchText.getText().toString();
        if (!name_temp.replaceAll(" ", "").equals("")){
            name = name_temp;
            image_uri = null;
            RelativeLayout linearLayout = (RelativeLayout) rootView.findViewById(R.id.contact_details);
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
            RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.no_name_details);
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
        RelativeLayout linearLayout = (RelativeLayout) rootView.findViewById(R.id.no_name_details);
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.contact_details);
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
     Method used to get contact image and numbers when using search view. Now the same is done in getContactImageAndNumbers above.
     Refer commit for version 7.0.qb.3
     }*/

    public void onAdd(View view) {
        if (name == null || name.equals("")) {
            Toast.makeText(context, getString(R.string.toast_help_enter_name), Toast.LENGTH_LONG).show();
            return;
        }
        if (amount == 0) {
            //amountLayout.setTextColor(Color.RED);
            Toast.makeText(context, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        //loadingView.setLoading(true);
        // Set opponentUserId and contact from database if available
        Cursor cursor = databaseHelper.getItem(name);
        if (cursor.moveToFirst()) {
            opponentUserID = cursor.getInt(cursor.getColumnIndex("QuickbloxID"));
            if (contact == null)
                contact = cursor.getString(cursor.getColumnIndex("PhoneNo"));
        }

        if (type.equals("Borrowed"))
            amount = -1 * amount;
        EditText detailText = (EditText) rootView.findViewById(R.id.details_edit_text);
        detail = detailText.getText().toString();
        //Add transaction to database and reset UI
        databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
        resetDetailsAfterTransactionAdded();

        // if user signed up and contact not saved make the user choose the associated contact and send chat message
//        if (contact == null && isSignedUp) {
//            if(sendNotification) {
//                if(phoneNos.length > 0) { // no contact chooser dialog shown if no number found in the contact
//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.dialog_contact_choser_item);
//                    arrayAdapter.addAll(phoneNos);
//                    DialogPlus dialog = DialogPlus.newDialog(this)
//                            .setAdapter(arrayAdapter)
//                            .setOnItemClickListener(new OnItemClickListener() {
//                                @Override
//                                public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
//                                    Log.i("Position : ", Integer.toString(position));
//                                    if (position == 0 || position == phoneNos.length + 1)
//                                        return;
//                                    else {
//                                        phoneNos[position - 1] = phoneNos[position - 1].replaceAll("[^0-9+]", "");
//                                        contact = phoneNos[position - 1];
//                                        if (isSignedUp && opponentUserID == -1) {
//                                            //userNumber = 1;
//                                            //retrieveAllUsersFromPage(1);
//                                            getUserIDandSendMessage();
//                                        } else if (isSignedUp && opponentUserID != -1) {
//                                            databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
//                                            sendChatMessage(true);
//                                        }
//                                        dialog.dismiss();
//                                    }
//                                }
//                            })
//                            .setFooter(R.layout.dialog_contact_choser_footer)
//                            .setHeader(R.layout.dialog_contact_choser_header)
//                            .setPadding(10, 10, 10, 10)
//                            .create();
//                    dialog.show();
//                }
//                else { // no number so just add transaction without sending notification
//                    databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
//                    resetDetailsAfterTransactionAdded();
//                }
//            }
//            else{
//                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
//                resetDetailsAfterTransactionAdded();
//            }
//        }
//
//        // If contact is already saved or user not signed in
//        else {
//            // if user not signed up we can save data without contact and UserID
//            if (!isSignedUp) {
//                //DialogUtils.showLong(context, "Saving data without opponentID because not signed in");
//                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
//                resetDetailsAfterTransactionAdded();
//            }
//
//            // if user is signed in userID not saved find the userID and then send chat message and then save data
//            if (isSignedUp && opponentUserID == -1) {
//                /*userNumber = 1;
//                retrieveAllUsersFromPage(1);*/
//                if(sendNotification)
//                    getUserIDandSendMessage();
//                else{
//                    databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
//                    resetDetailsAfterTransactionAdded();
//                }
//            }
//            // opponentUserId saved so directly send message and save data
//            else if (isSignedUp && opponentUserID != -1) {
//                databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
//                if(sendNotification)
//                    sendChatMessage(true);
//                else
//                    resetDetailsAfterTransactionAdded();
//                //DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
//            }
//        }
    }

    /*public void onClearBalance(View v) {
        Method implementation for clear balance button(now not shown).  Refer commit for version 7.0.qb.3
    }*/

    public void setBalanceText(float balance) {
        TextView balanceDetail = (TextView) rootView.findViewById(R.id.contact_detail_balance);
        if (balance < 0) {
            balanceDetail.setText(getString(R.string.pending_balance_line) + getString(R.string.borrowed_colon) + mCurrency + Float.toString(-1 * balance));
            balanceDetail.setTextColor(setColorRed());
            /* Remove screen resizing code.  Refer commit for version 7.0.qb.3 */
        }
        if (balance > 0) {
            balanceDetail.setText(getString(R.string.pending_balance_line) + getString(R.string.lent_colon) + mCurrency + Float.toString(balance));
            balanceDetail.setTextColor(setColorGreen());
            /* Remove screen resizing code.  Refer commit for version 7.0.qb.3 */
        }
        if (balance == 0) {
            balanceDetail.setText(getString(R.string.no_balance));
            balanceDetail.setTextColor(Color.BLACK);
           /* Remove screen resizing code.  Refer commit for version 7.0.qb.3 */
        }
    }

    public void onTypeRadioButtonSelected(View v) {
        if (v.getId() == R.id.radio_button_lent) {
            changeButton(v, true, true);
            changeButton(rootView.findViewById(R.id.radio_button_borrowed),false, false);
            type = "Lent";
            TextView textView = (TextView) rootView.findViewById(R.id.money_detail_name);
            textView.setText(type);
            textView.setTextColor(setColorGreen());
            if(amount<0) {
                amount = -1 * amount;
                txtResult.setText(String.valueOf(amount));
            }

            //amount = -1 * amount;
        } else if (v.getId() == R.id.radio_button_borrowed) {
            changeButton(v, true, false);
            changeButton(rootView.findViewById(R.id.radio_button_lent),false, true);
            type = "Borrowed";
            TextView textView = (TextView) rootView.findViewById(R.id.money_detail_name);
            textView.setText(type);
            textView.setTextColor(setColorRed());
            if(amount<0){
                amount = -1*amount;
                txtResult.setText(String.valueOf(amount));
            }
        }
    }

    /** Changes the button colors and text to the specified type*/
    public void changeButton(View view, boolean selected, boolean lent){
        Button button = (Button) view;
        if(selected){
            if(lent)
                button.setBackground(getResources().getDrawable(R.drawable.oval_button_pressed));
            else
                button.setBackground(getResources().getDrawable(R.drawable.oval_button_pressed_red));
            button.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            if(lent) {
                button.setBackground(getResources().getDrawable(R.drawable.oval_button));
                button.setTextColor(getResources().getColor(R.color.teal));
            }
            else {
                button.setBackground(getResources().getDrawable(R.drawable.oval_button_red));
                button.setTextColor(getResources().getColor(R.color.borrowed_red));
            }
        }
    }

    public void onCancel(View v) {
//        if(isSignedUp)
//            logoutFormChat();
        getActivity().finish();
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

//    public void startCalCActivity(View v) {
//        Intent calcActivity = new Intent(this, CalcActivity.class);
//        startActivityForResult(calcActivity, 234);
//    }

//    public void getUserIDandSendMessage() {
//        //Toast.makeText(AddNewTransactionActivity.this, "Finding userID", Toast.LENGTH_SHORT).show();
//        final QuickbloxUsersDatabaseHelper usersDatabaseHelper = new QuickbloxUsersDatabaseHelper(context);
//        ArrayList<Integer> ids = usersDatabaseHelper.getUserID(contact);
//        /*AsyncTask task = new AsyncTask() {
//            Initially users were searched in real time. Now a database is maintained for users. Refer commit for version 7.0.qb.3
//        task.execute();
//    }*/
//        int noOfIds = ids.size();
//        if (noOfIds == 1) {
//            Toast.makeText(AddNewTransactionActivity.this, getString(R.string.toast_found_user), Toast.LENGTH_SHORT).show();
//            opponentUserID = ids.get(0);
//            Log.i("userId", Integer.toString(opponentUserID));
//            databaseHelper.saveData(name, image_uri, amount, type, detail, contact, opponentUserID);
//            if (isSignedUp && opponentUserID != -1) {
//                //DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
//                sendChatMessage(true);
//            } else
//                resetDetailsAfterTransactionAdded();
//        }
//        else {
//            opponentUserID = -1; // either contact has several IDs or no id
//            databaseHelper.saveData(name,image_uri,amount,type,detail,contact,opponentUserID);
//            if(noOfIds != 0) {
//                for (int i = 0; i < noOfIds; i++) {
//                    opponentUserID = ids.get(i);
//                    if (isSignedUp && opponentUserID != -1) {
//                        //DialogUtils.showLong(context, "Sending chat message to " + opponentUserID);
//                        if (i == noOfIds - 1)
//                            sendChatMessage(true);  // reset data oly after message to last userID has been sent
//                        else
//                            sendChatMessage(false);
//                    }
//                }
//            }
//            else {
//                Toast.makeText(AddNewTransactionActivity.this, getString(R.string.toast_no_user), Toast.LENGTH_SHORT).show();
//                resetDetailsAfterTransactionAdded(); // reset data if no user found
//            }
//        }
//    }

//    /** The format created for the chat message. The same text is used as notification body*/
//    private QBChatMessage createChatMessage() {
//        QBChatMessage chatMessage = new QBChatMessage();
//        // Special characters are added to identify change in data and make the string easy to parse when getting details from it
//        String messageText = senderName + ":";
//        if (type.equals("Lent") ) {
//            messageText += getString(R.string.gcm_noti_lent_message) + "("+amount+")";
//        } else
//            messageText += getString(R.string.gcm_noti_borrowed_message) + "("+ -1*amount +")" ;
//        if (detail != null && !detail.equals(""))
//            messageText += "[" + detail + "]";
//        messageText+= getString(R.string.gcm_noti_end_message);
//        messageText += "\n<" + senderPhone +">";
//        chatMessage.setBody(messageText);
//        chatMessage.setProperty("save_to_history", "1");
//        chatMessage.setDateSent(System.currentTimeMillis() / 1000);
//        return chatMessage;
//    }

    /** Sends the transaction as a chat to the quickblox server. This is not essentially required for sending notifications but helps
     * in maintaining logs of transactions
     * @param shouldReset Defines if the details should be reset after sending chat
     */
//    private void sendChatMessage(final boolean shouldReset){
//        final QBChatMessage msg = createChatMessage();
//        msg.setRecipientId(opponentUserID);
//        // msg.setDialogId("546cc8040eda8f2dd7ee449c"); Set the dialog Id or recipient Id
//        msg.setProperty("send_to_chat", "1");
//        //msg.setProperty("param2", "value2");
//        PendingNotificationsDatabaseHelper databaseHelper = new PendingNotificationsDatabaseHelper(getApplicationContext());
//        databaseHelper.insertNotification(msg.getBody(), msg.getRecipientId());
//        databaseHelper.closeDatabase();
//        if(shouldReset){
//            Intent sendNotifications = new Intent(this, RetreiveUsersService.class);
//            sendNotifications.putExtra("createSession",false);
//            sendNotifications.putExtra("sendNotifications",true);
//            startService(sendNotifications);
//            resetDetailsAfterTransactionAdded();
//        }
//        /*QBChatService.createMessage(msg, new QBEntityCallbackImpl<QBChatMessage>() {
//            @Override
//            public void onSuccess(QBChatMessage result, Bundle params) {
//            Not creating chats for faster addition of transactions. Refer commit for version 7.0.qb.3
//        });*/
//    }

    /** Sends push notification to the specified opponent ID */
    /* Not required since notifications are now sent in background  in a service. Refer commit for version 7.0.qb.3
    private void sendPushNotification(final QBChatMessage msg){
        QBEvent event = new QBEvent();
        QBMessages.createEvent(event, new QBEntityCallbackImpl<QBEvent>() {
        });
    }*/

    /**
     * Resets user interface elements so that user can add a new transaction. The name of person in previous transaction is not reset to
     * facilitate continuous addition of transaction with same person.
     */
    private void resetDetailsAfterTransactionAdded() {
        //loadingView.setLoading(false);
        if(amount<0)
            Snackbar.make(rootView, "Added Transaction: " + type + " " + name + " "
                    + Currency.getInstance(Locale.getDefault()).getSymbol() + -1*amount, Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(rootView, "Added Transaction: " + type + " " + name + " "
                    + Currency.getInstance(Locale.getDefault()).getSymbol() + amount, Snackbar.LENGTH_SHORT).show();
        balance = balance + amount;
        setBalanceText(balance);
        amount = 0;
        result = 0; // set the calculator result to zero so that it may not be included in further calculations if user adds more transactions
        type = "Lent";
        TextView amountType = (TextView) rootView.findViewById(R.id.money_detail_name);
        amountType.setText(getString(R.string.amount_colon));
        amountType.setTextColor(getResources().getColor(R.color.cardview_shadow_start_color));
        txtResult.setText("0");
        // Set the inStr to 0 so that if user add another transaction the previous result does not show up
        inStr = "0";
        contact = null;
        EditText detailText = (EditText) rootView.findViewById(R.id.details_edit_text);
        detailText.setText(null);
         /* Remove code for resizing screen. Refer commit for version 7.0.qb.3 */
    }

//    /** OnClick method for checkbox that allows user to choose whether or not send notification */
//    public void onCheckboxClick(View view){
//        CheckBox checkBox = (CheckBox) view;
//        if(isSignedUp)
//            sendNotification = checkBox.isChecked();
//        else {
//            checkBox.setChecked(false);
//            Toast.makeText(this, getString(R.string.toast_create_account), Toast.LENGTH_SHORT).show();
//        }
//    }

    /** Sets up the calculator button click listners */
    public void setupCalc(){
        // Retrieve a reference to the EditText field for displaying the result.
        txtResult = (TextView) rootView.findViewById(R.id.money_detail_balance);
        txtResult.setText("0");

        // Register listener (this class) for all the buttons
        AddNewTransactionFragment.BtnListener listener = new AddNewTransactionFragment.BtnListener();
        rootView.findViewById(R.id.btnNum0Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum1Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum2Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum3Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum4Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum5Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum6Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum7Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum8Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnNum9Id).setOnClickListener(listener);
        rootView.findViewById(R.id.btnAddId).setOnClickListener(listener);
        rootView.findViewById(R.id.btnSubId).setOnClickListener(listener);
        rootView.findViewById(R.id.btnMulId).setOnClickListener(listener);
        rootView.findViewById(R.id.btnDivId).setOnClickListener(listener);
        rootView.findViewById(R.id.btnClearId).setOnClickListener(listener);
        rootView.findViewById(R.id.btnEqualId).setOnClickListener(listener);
        rootView.findViewById(R.id.btnDecimal).setOnClickListener(listener);
        rootView.findViewById(R.id.btnDelId).setOnClickListener(listener);
    }

    /**
     * This class contains the implementation for the calculator. It provides methods for various actions based on button clicked and
     * calculates the result
     */
    private class BtnListener implements View.OnClickListener {
        // On-click event handler for all the buttons
        @Override
        public void onClick(View view) {
            TextView amountType = (TextView) rootView.findViewById(R.id.money_detail_name);
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
                case R.id.btnDecimal:
                    if(!inStr.contains(".")){
                        inStr += ".";
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
            TextView amountType = (TextView) rootView.findViewById(R.id.money_detail_name);
            if(result<0) {
                amountType.setTextColor(setColorRed());
            }
            else {
                amountType.setTextColor(setColorGreen());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }

//    private void logoutFormChat(){
//        boolean isLoggedIn = chatService.isLoggedIn();
//        if(!isLoggedIn){
//            return;
//        }
//
//        chatService.logout(new QBEntityCallbackImpl() {
//
//            @Override
//            public void onSuccess() {
//                // success
//                chatService.destroy();
//            }
//
//            @Override
//            public void onError(final List list) {
//
//            }
//        });
//    }

    /** Reveal animation shown at activity startup. Uses external library */
    public void startReveal(){
        // previously invisible view
        final View myView = rootView.findViewById(R.id.name_card);
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

                SupportAnimator animator = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(600);
                animator.start();
            }
        });

        /* The calculator and buttons are hidden on startup and shown after reveal to improve effect */
        View calc = rootView.findViewById(R.id.tableId);
        //TranslateAnimation animation = new TranslateAnimation(0,0,calc.getHeight(),0);
        //animation.setInterpolator(new AccelerateDecelerateInterpolator());
        //animation.setDuration(2000);
        //calc.startAnimation(animation);*/
        calc.setVisibility(View.VISIBLE);

        View buttons = rootView.findViewById(R.id.calc_action_buttons);
        //buttons.startAnimation(new TranslateAnimation(0,0,0,buttons.getHeight()));
        buttons.setVisibility(View.VISIBLE);
    }

    /** Hides keyboard if it is currently visible. Used to hide keyboard when user presses on amount */
    public void hideKeyboard(View v){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
