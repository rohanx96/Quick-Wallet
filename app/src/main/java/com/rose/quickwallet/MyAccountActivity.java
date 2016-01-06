package com.rose.quickwallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.rose.quickwallet.quickblox.Consts;
import com.rose.quickwallet.quickblox.pushnotifications.GCMClientHelper;
import com.rose.quickwallet.transactions.MainActivity;

import java.util.List;
//import android.support.v7.app.ActionBarActivity;

/**
 * Created by rose on 20/12/15.
 *
 */
public class MyAccountActivity extends Activity {
    SharedPreferences preferences;
    TextView nameText;
    TextView phoneText;
    TextView emailText;
    int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        nameText = (TextView) findViewById(R.id.account_name);
        phoneText = (TextView) findViewById(R.id.account_phone);
        emailText = (TextView) findViewById(R.id.account_emailId);
        emailText.setText(preferences.getString(Consts.USER_LOGIN,null));
        phoneText.setText(preferences.getString(Consts.USER_PHONE,null));
        nameText.setText(preferences.getString(Consts.USER_NAME,null));
        userId = preferences.getInt(Consts.USER_ID,-1);
        if(userId==-1){
            QBUsers.getUserByEmail(emailText.getText().toString(),new QBEntityCallbackImpl<QBUser>(){
                @Override
                public void onSuccess(QBUser result, Bundle params) {
                    userId = result.getId();
                    preferences.edit().putInt(Consts.USER_ID,userId).apply();
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in_short, R.anim.slide_out_from_top); // enter animation for the previous activity that is brought up from stack
    }

    public void onEditName(View view){
        final EditText nameInput = new EditText(this);
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parameters.setMargins(20,0,20,0);
        nameInput.setLayoutParams(parameters);
        layout.addView(nameInput);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.enter_name));
        builder.setView(layout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                final ProgressDialog pDialog = new ProgressDialog(MyAccountActivity.this);
                pDialog.setMessage(getString(R.string.editing_profile));
                pDialog.show();
                dialog.dismiss();
                QBUser user = new QBUser(userId);
                //user.setFullName(((EditText) LayoutInflater.from(MyAccountActivity.this).inflate(R.layout.edit_text,null,false).findViewById(R.id.edit_account_info)).getText().toString());
                user.setFullName(nameInput.getText().toString());
                QBUsers.updateUser(user, new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {
                        nameText.setText(user.getFullName());
                        preferences.edit().putString(Consts.USER_NAME,user.getFullName()).apply();
                        pDialog.dismiss();
                    }

                    @Override
                    public void onError(List<String> errors) {
                        Toast.makeText(MyAccountActivity.this, "Error: " + errors, Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                    }
                });
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void onEditContact(View view){
        final EditText contactInput = new EditText(this);
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parameters.setMargins(20,0,20,0);
        contactInput.setLayoutParams(parameters);
        contactInput.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(contactInput);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.enter_phone));
        builder.setView(layout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                final ProgressDialog pDialog = new ProgressDialog(MyAccountActivity.this);
                pDialog.setMessage(getString(R.string.editing_profile));
                pDialog.show();
                QBUser user = new QBUser(userId);
                //user.setPhone(((EditText) LayoutInflater.from(MyAccountActivity.this).inflate(R.layout.edit_text,null,false).findViewById(R.id.edit_account_info)).getText().toString());
                user.setPhone(contactInput.getText().toString());
                QBUsers.updateUser(user, new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {
                        phoneText.setText(user.getPhone());
                        preferences.edit().putString(Consts.USER_PHONE, user.getPhone()).apply();
                        pDialog.dismiss();
                    }

                    @Override
                    public void onError(List<String> errors) {
                        Toast.makeText(MyAccountActivity.this, "Error: " + errors, Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                    }
                });
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void onSignOut(View view){
        GCMClientHelper pushClientManager = new GCMClientHelper(this, Consts.PROJECT_NUMBER);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.signing_out));
        progressDialog.show();
        pushClientManager.unsubscribeFromPushNotifications();
        QBUsers.signOut(new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                PreferenceManager.getDefaultSharedPreferences(MyAccountActivity.this).edit().putBoolean(Consts.IS_SIGNED_UP, false).apply();
                progressDialog.hide();
                finish();
                Intent main = new Intent(MyAccountActivity.this, MainActivity.class);
                main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(main);
            }

            @Override
            public void onError(List errors) {
                Toast.makeText(MyAccountActivity.this, "Error: " + errors, Toast.LENGTH_LONG).show();
            }
        });
    }
}
