package com.rose.quickwallet.quickblox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rose.quickwallet.R;
import com.rose.quickwallet.transactions.MainActivity;

/**
 *
 * Created by rose on 11/10/15.
 *
 */
public class GoCloudActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_cloud);
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.sign_up_button_go_cloud:
                Intent signUp = new Intent(getApplicationContext(),SignUpUserActivity.class);
                startActivity(signUp);
                break;
            case R.id.sign_in_button_go_cloud:
                Intent signIn = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(signIn);
                break;
            case R.id.continue_go_cloud:
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(main);
                finish();
                break;
        }
    }
}
