package com.rose.quickwallet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rose.quickwallet.transactions.AddNewTransactionActivity;
import com.rose.quickwallet.transactions.MainActivity;

/**
 *
 * Created by rose on 23/8/15.
 *
 */
public class EnterPinActivity extends Activity {
    private EditText editText1;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_pin_activity);
        if(getIntent().getAction().equals("SAVE_PASSWORD")) {
            TextView enterPin = (TextView) findViewById(R.id.enter_pin_text);
            enterPin.setText(getString(R.string.enter_new_pin));
        }
        if(getIntent().getAction().equals("CHANGE_PIN")){
            TextView enterPin = (TextView) findViewById(R.id.enter_pin_text);
            enterPin.setText(getString(R.string.enter_saved_pin));
        }
        editText1 = (EditText) findViewById(R.id.pin_text_1);
        editText2 = (EditText) findViewById(R.id.pin_text_2);
        editText3 = (EditText) findViewById(R.id.pin_text_3);
        editText4 = (EditText) findViewById(R.id.pin_text_4);
        final Button enterButton = (Button) findViewById(R.id.pin_enter_button);
        /*editText4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == R.id.password_enter_ime_action){
                    onEnter(enterButton);
                    return true;
                }
                return false;
            }
        });*/
        editText1.requestFocus();
        editText1.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>= 1) {
                    editText1.clearFocus();
                    editText2.requestFocus();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
        });

        editText2.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Integer textlength2 = editText1.getText().length();

                if (s.length() >= 1) {
                    editText2.clearFocus();
                    editText3.requestFocus();
                }
                if(s.length() ==0){
                    editText2.clearFocus();
                    editText1.requestFocus();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }
        });
        editText3.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {


                if (s.length() >= 1) {
                    editText3.clearFocus();
                    editText4.requestFocus();
                }
                if(s.length()==0){
                    editText3.clearFocus();
                    editText2.requestFocus();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }
        });

        editText4.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //Integer textlength2 = editText4.getText().length();

                if (s.length() >= 1) {
                    editText4.clearFocus();
                    if(getIntent().getAction().equals("ENTER_PASSWORD") || getIntent().getAction().equals("ENTER_PASSWORD_NOTIFICATION"))
                        onEnter(enterButton);
                    enterButton.requestFocus();
                }
                if(s.length() == 0){
                    editText4.clearFocus();
                    editText3.requestFocus();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }
        });

    }

    public void onEnter(View v){
        /*final EditText editText1 = (EditText) findViewById(R.id.pin_text_1);
        final EditText editText2 = (EditText) findViewById(R.id.pin_text_2);
        final EditText editText3 = (EditText) findViewById(R.id.pin_text_3);
        final EditText editText4 = (EditText) findViewById(R.id.pin_text_4);*/
        String enteredPassword = editText1.getText().toString();
        enteredPassword+= editText2.getText().toString();
        enteredPassword+= editText3.getText().toString();
        enteredPassword+= editText4.getText().toString();
        if(getIntent().getAction().equals("SAVE_PASSWORD")){
            if(enteredPassword.length()<4){
                TextView errorText = (TextView)findViewById(R.id.pin_error_text);
                errorText.setText(getString(R.string.wrong_pin_length));
                errorText.setVisibility(View.VISIBLE);
            }
            else {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                preferences.edit().putString("password",(enteredPassword)).apply();
                finish();
            }
        }
        else if(getIntent().getAction().equals("ENTER_PASSWORD")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if((enteredPassword).equals(preferences.getString("password", "abcd"))){
                Intent mainActivity = new Intent(this, MainActivity.class);
                mainActivity.setAction("enter");
                startActivity(mainActivity);
                finish();
            }
            else {
                displayErrorText();
            }
        }
        else if(getIntent().getAction().equals("ENTER_PASSWORD_NOTIFICATION")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if((enteredPassword).equals(preferences.getString("password", "abcd"))){
                Intent addTransactionActivity = new Intent(this, AddNewTransactionActivity.class);
                addTransactionActivity.setAction("generic");
                addTransactionActivity.putExtra("action","generic");
                startActivity(addTransactionActivity);
                finish();
            }
            else {
               displayErrorText();
            }
        }
        else if(getIntent().getAction().equals("REMOVE_PASSWORD")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if((enteredPassword).equals(preferences.getString("password", "abcd"))) {
                preferences.edit().putString("password", "abcd").apply();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().putBoolean("securitySwitch", false).apply();
                finish();
            }
            else displayErrorText();

        }
        else if (getIntent().getAction().equals("CHANGE_PASSWORD")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if((enteredPassword).equals(preferences.getString("password", "abcd"))){
                Intent savePassword = new Intent(this,EnterPinActivity.class);
                savePassword.setAction("SAVE_PASSWORD");
                startActivity(savePassword);
                finish();
            }
            else displayErrorText();
        }
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getAction().equals("SAVE_PASSWORD")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putString("password","abcd").apply();
            //
            preferences.edit().putBoolean("securitySwitch",false).apply();
        }
        if(getIntent().getAction().equals("REMOVE_PASSWORD")){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().putBoolean("securitySwitch",true).apply();
        }
        super.onBackPressed();
    }

    public void onCancel(View v){
        onBackPressed();
    }

    public void displayErrorText(){
        TextView errorText = (TextView)findViewById(R.id.pin_error_text);
        errorText.setVisibility(View.VISIBLE);
        editText1.setText(null);
        editText2.setText(null);
        editText3.setText(null);
        editText4.setText(null);
        editText1.requestFocus();
    }
    /*
    //Following two methods are to create a SHA Hash mapping of password.

    private static String convertToHex(byte[] data) throws java.io.IOException
    {
        StringBuffer sb = new StringBuffer();
        String hex=null;

        hex= Base64.encodeToString(data,0,data.length,0);

        sb.append(hex);

        return sb.toString();
    }


    public String computeSHAHash(String password) {
    // The MessageDigest class uses a one-way hash function to turn an arbitrary number of bytes into a fixed-length byte sequence
    // The original arbitrary-length sequence is the message, and the fixed-length byte sequence is the digest or message digest.
        MessageDigest mdSha1 = null;
        try
        {
            mdSha1 = MessageDigest.getInstance("SHA-1"); //SHA-1: a type of hashing : is used here
        } catch (NoSuchAlgorithmException e1) {
            //Log.e("myapp", "Error initializing SHA1 message digest");
        }
        try {
        // After creating or resetting a MessageDigest you should call update(byte[], int, int) for each block of input data, and then call digest() to get the final digest.
        // Note that calling digest resets the MessageDigest
            mdSha1.update(password.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] data = mdSha1.digest();
        try {
            return convertToHex(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
