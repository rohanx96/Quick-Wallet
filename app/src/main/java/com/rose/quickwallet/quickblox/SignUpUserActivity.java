package com.rose.quickwallet.quickblox;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import com.rose.quickwallet.R;
import com.rose.quickwallet.transactions.MainActivity;

import java.util.List;

/**
 *
 * Created by rose on 6/10/15.
 *
 */
public class SignUpUserActivity extends ActionBarActivity {

    private EditText loginEditText;
    private EditText passwordEditText;
    private EditText phoneEditText;
    private EditText nameEditText;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        progressDialog = DialogUtils.getProgressDialog(this);
        initUI();
        QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
            }

            @Override
            public void onError(List<String> errors) {
            }
        });
        requestPermissions();
    }

    private void initUI() {
        //actionBar.setDisplayHomeAsUpEnabled(true);
        loginEditText = (EditText) findViewById(R.id.login_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
        phoneEditText = (EditText) findViewById(R.id.phone_edittext);
        nameEditText = (EditText) findViewById(R.id.name_edittext);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up_button:
                if (isValidEmail(loginEditText.getText().toString())) {
                    if (!nameEditText.getText().toString().equals("")) {
                        if (!(phoneEditText.getText().toString().length() < 7) || phoneEditText.getText().toString().length() > 15) {
                            progressDialog.show();

                            // Sign Up user
                            //
                            QBUser qbUser = new QBUser();
                            qbUser.setFullName(nameEditText.getText().toString());
                            qbUser.setEmail(loginEditText.getText().toString());
                            qbUser.setPassword(passwordEditText.getText().toString());
                            qbUser.setPhone(phoneEditText.getText().toString());
                            QBUsers.signUpSignInTask(qbUser, new QBEntityCallbackImpl<QBUser>() {
                                @Override
                                public void onSuccess(QBUser qbUser, Bundle bundle) {
                                    progressDialog.hide();
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    preferences.edit().putString(Consts.USER_NAME, nameEditText.getText().toString()).apply();
                                    preferences.edit().putInt(Consts.USER_ID,qbUser.getId()).apply();
                                    preferences.edit().putString(Consts.USER_LOGIN, loginEditText.getText().toString()).apply();
                                    preferences.edit().putString(Consts.USER_PASSWORD, passwordEditText.getText().toString()).apply();
                                    preferences.edit().putString(Consts.USER_PHONE, phoneEditText.getText().toString()).apply();
                                    preferences.edit().putBoolean(Consts.IS_SIGNED_UP, true).apply();
                                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                    mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(mainActivity);
                        /*DataHolder.getDataHolder().addQbUserToList(qbUser);
                        DataHolder.getDataHolder().setSignInQbUser(qbUser);
                        DataHolder.getDataHolder().setSignInUserPassword(passwordEditText.getText().toString());*/

                                    finish();
                                }

                                @Override
                                public void onError(List<String> strings) {
                                    // create session if already not created due to internet connectivity changed (unavailable in OnCreate)
                                    if(strings.get(0).equals("Token is required")){
                                        QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
                                        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
                                            @Override
                                            public void onSuccess(QBSession qbSession, Bundle bundle) {
                                                progressDialog.hide();
                                                Toast.makeText(SignUpUserActivity.this, getString(R.string.toast_retry), Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onError(List<String> errors) {
                                                progressDialog.hide();
                                                Toast.makeText(SignUpUserActivity.this, getString(R.string.toast_check_internet), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        progressDialog.hide();
                                        DialogUtils.showLong(getApplicationContext(), strings.get(0));
                                    }
                                }
                            });
                        } else
                            phoneEditText.setError(getString(R.string.wrong_number));
                    }
                    else
                        nameEditText.setError(getString(R.string.toast_help_enter_name));
                }
                else
                loginEditText.setError(getString(R.string.wrong_email));
                break;
        }
    }
    public final boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void requestPermissions(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
                android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                dialogBuilder.setMessage(getString(R.string.request_permission_telephone))
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(SignUpUserActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
                            }
                        });
                dialogBuilder.show();
            }
            else
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},2);
        }
    }
}