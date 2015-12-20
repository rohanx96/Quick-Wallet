package com.rose.quickwallet.quickblox;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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
 * Created by rose on 6/10/15.
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
                                    progressDialog.hide();
                                    DialogUtils.showLong(getApplicationContext(), strings.get(0));
                                }
                            });
                        } else
                            phoneEditText.setError("Please enter a valid mobile number");
                    }
                    else
                        nameEditText.setError("Please enter name");
                }
                else
                loginEditText.setError("Please enter a valid E-Mail address");
                break;
        }
    }
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}