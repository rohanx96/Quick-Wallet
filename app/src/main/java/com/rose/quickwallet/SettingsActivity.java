package com.rose.quickwallet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.Toolbar;

/**
 * Created by rose on 16/8/15.
 */
public class SettingsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setSupportActionBar( (android.support.v7.widget.Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, null, false));
        setTitle(getString(R.string.settings));
        getFragmentManager().beginTransaction().replace(android.R.id.content,new SettingsFragment()).commit();
    }
}
