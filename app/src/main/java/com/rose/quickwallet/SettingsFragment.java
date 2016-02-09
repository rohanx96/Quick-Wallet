package com.rose.quickwallet;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.rose.quickwallet.quickblox.Consts;
import com.rose.quickwallet.quickblox.GoCloudActivity;
import com.rose.quickwallet.transactions.AddNewTransactionActivity;


/**
 *
 * Created by rose on 16/8/15.
 *
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //final SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Preference preference  = findPreference("aboutEmail");
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "rohanx96@gmail.com", null));
        //intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, "QuickWallet App on PlayStore");
        //intent.putExtra(Intent.EXTRA_EMAIL, "rohanx96@gmail.com");
        preference.setIntent(Intent.createChooser(intent, "Send Email"));

        Preference accountSettings = findPreference("accountSettings");
        if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Consts.IS_SIGNED_UP,false)){
            Intent accountEdit = new Intent(getActivity(),MyAccountActivity.class);
            accountSettings.setIntent(accountEdit);
        }
        else {
            Intent goCloud = new Intent(getActivity(), GoCloudActivity.class);
            accountSettings.setIntent(goCloud);
        };

        Preference shareSetting = findPreference("shareApp");
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,getString(R.string.share_msg_text));
        shareSetting.setIntent(Intent.createChooser(share,"Share using"));
        /*final SwitchPreference securitySwitch = (SwitchPreference) findPreference("securitySwitch");
        securitySwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (sharedPreferences.getBoolean("securitySwitch", false)) {
                    Intent savePassword = new Intent(getActivity(), EnterPinActivity.class);
                    savePassword.setAction("SAVE_PASSWORD");
                    startActivity(savePassword);
                    return true;
                } else {
                    if (!preferences.getString("password","abcd").equals("abcd")) {
                        Intent removePassword = new Intent(getActivity(), EnterPinActivity.class);
                        removePassword.setAction("REMOVE_PASSWORD");
                        startActivity(removePassword);
                        return false;
                    }
                    return true;
                }
            }
        });*/
            Preference changePassword = findPreference("securityChangePIN");
            Intent change = new Intent(getActivity(), EnterPinActivity.class);
            change.setAction("CHANGE_PASSWORD");
            changePassword.setIntent(change);
        }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        CheckBoxPreference security = (CheckBoxPreference) findPreference("securitySwitch");
        security.setChecked(getPreferenceScreen().getSharedPreferences().getBoolean("securitySwitch",false));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("securitySwitch")){
            if(sharedPreferences.getBoolean("securitySwitch",false)){
                Intent savePassword = new Intent(getActivity(), EnterPinActivity.class);
                savePassword.setAction("SAVE_PASSWORD");
                startActivity(savePassword);
            }
            else{
                if (!sharedPreferences.getString("password","abcd").equals("abcd")) {
                    Intent removePassword = new Intent(getActivity(), EnterPinActivity.class);
                    removePassword.setAction("REMOVE_PASSWORD");
                    startActivity(removePassword);
                }
            }
        }
        if(s.equals("notificationPersistent")){
            if(sharedPreferences.getBoolean("notificationPersistent",false))
                createPersistentNotification();
            else
                cancelPersistentNotification();
        }
        if(s.equals("notificationSwitch")){
            if(!sharedPreferences.getBoolean("notificationSwitch",true)){
                cancelPersistentNotification();
                removeNotificationAlarm();
            }

        }
    }
    public void createPersistentNotification() {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
        /*Intent intent = new Intent(this,MainActivity.class);
        intent.setAction("generic");*/
        Intent addActivity = new Intent(getActivity(),AddNewTransactionActivity.class);
        addActivity.putExtra("action","generic");
        addActivity.setAction("notification");
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.gcm_noti_summary))
                .setContentIntent(PendingIntent.getActivity(getActivity(), 456, addActivity, PendingIntent.FLAG_CANCEL_CURRENT))
                .setOngoing(true);
        notificationManager.notify(5672, builder.build());
    }

    public void cancelPersistentNotification(){
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(5672);
    }

    public void removeNotificationAlarm(){
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Setup periodic alarm every 5 seconds. Not required now because now times are directly set in the method call using
        // AlarmManager.INTERVAL_HALF_HOUR
        //long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
        //int intervalMillis = 5000; // 5 seconds
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}
