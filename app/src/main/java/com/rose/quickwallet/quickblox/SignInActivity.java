package com.rose.quickwallet.quickblox;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.rose.quickwallet.R;
import com.rose.quickwallet.transactions.MainActivity;

import java.util.List;

public class SignInActivity extends Activity {

    private EditText loginEditText;
    private EditText passwordEditText;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_sign_in);
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
        loginEditText = (EditText) findViewById(R.id.login_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
        progressDialog = DialogUtils.getProgressDialog(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                if (isValidEmail(loginEditText.getText().toString())){
                    progressDialog.show();

                    // Sign in application with user
                    //
                    QBUser qbUser = new QBUser();
                    qbUser.setEmail(loginEditText.getText().toString());
                    qbUser.setPassword(passwordEditText.getText().toString());
                    QBUsers.signIn(qbUser, new QBEntityCallbackImpl<QBUser>() {
                        @Override
                        public void onSuccess(QBUser qbUser, Bundle bundle) {
                            setResult(RESULT_OK);
                            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            preferences.edit().putString(Consts.USER_LOGIN, loginEditText.getText().toString()).apply();
                            preferences.edit().putString(Consts.USER_PASSWORD, passwordEditText.getText().toString()).apply();
                            QBUsers.getUserByEmail(loginEditText.getText().toString(), new QBEntityCallback<QBUser>() {
                                @Override
                                public void onSuccess(QBUser qbUser, Bundle bundle) {
                                    preferences.edit().putString(Consts.USER_PHONE, qbUser.getPhone()).apply();
                                    preferences.edit().putString(Consts.USER_NAME, qbUser.getFullName()).apply();
                                    preferences.edit().putInt(Consts.USER_ID,qbUser.getId()).apply();
                                    preferences.edit().putBoolean(Consts.IS_SIGNED_UP, true).apply();
                                    progressDialog.hide();
                                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                    mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(mainActivity);
                                    finish();
                                }

                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(List<String> list) {

                                }
                            });
                        }

                        @Override
                        public void onError(List<String> errors) {
                            progressDialog.hide();
                            DialogUtils.showLong(getApplicationContext(), errors.get(0));
                        }
                    });
                }
                else{
                    loginEditText.setError(getString(R.string.wrong_email));
                }
                break;
            case R.id.forgot_password_text:
                progressDialog.show();
                QBUsers.resetPassword(loginEditText.getText().toString(), new QBEntityCallbackImpl() {
                    @Override
                    public void onSuccess() {
                        progressDialog.hide();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                        builder.setMessage(getString(R.string.reset_password_dialog_text_beg) + loginEditText.getText().toString() + getString(R.string.reset_password_dialog_text_end))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }

                    @Override
                    public void onError(List errors) {
                        progressDialog.hide();
                        loginEditText.setError(getString(R.string.wrong_email));
                        Toast.makeText(SignInActivity.this, getString(R.string.wrong_email), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.create_account_text:
                Intent signUpActivity = new Intent(getApplicationContext(),SignUpUserActivity.class);
                startActivity(signUpActivity);
                finish();
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void requestPermissions(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
                android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                dialogBuilder.setMessage(getString(R.string.request_permission_telephone))
                        .setPositiveButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(SignInActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
                            }
                        });
                dialogBuilder.show();
            }
            else
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},2);
        }
    }
}