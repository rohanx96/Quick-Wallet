package com.rose.quickwallet;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
//import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

//import com.rose.quickwallet.quickblox.Consts;
//import com.rose.quickwallet.quickblox.GoCloudActivity;
import com.rose.quickwallet.transactions.AddNewTransactionActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


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

//        Preference accountSettings = findPreference("accountSettings");
//        if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Consts.IS_SIGNED_UP,false)){
//            Intent accountEdit = new Intent(getActivity(),MyAccountActivity.class);
//            accountSettings.setIntent(accountEdit);
//        }
//        else {
//            Intent goCloud = new Intent(getActivity(), GoCloudActivity.class);
//            accountSettings.setIntent(goCloud);
//        };

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

            Preference backup = findPreference("prefBackup");
            backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    exportDB();
                    return true;
                }
            });

        Preference restore = findPreference("prefRestore");
        restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestPermissions();
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.setMessage("This will replace your current application data. Are you sure you want to continue?");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        importDB();
                    }
                });
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        });
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
                //cancelPersistentNotification();
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

    /** Copies the backed up database from external stroage to data*/
    private void importDB() {

        try {
            //Log.i("Settings", " importing");
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String newDBPath = "//data//" + "com.rose.quickwallet"
                        + "//databases//" + "QuickWallet.db";
                String oldDBPath = "QuickWalletData"; // From SD directory.

                File newDB = new File(data, newDBPath);
                File oldDB = new File(sd, oldDBPath);
                if(oldDB.exists()) {
                    FileChannel src = new FileInputStream(oldDB).getChannel();
                    FileChannel dst = new FileOutputStream(newDB).getChannel();
                    src.transferTo(0, src.size(), dst);
                    src.close();
                    dst.close();
                    Snackbar.make(getView(),"Data restored successfully",Snackbar.LENGTH_LONG).show();
                }
                else{
                    Snackbar.make(getView(),"Please create a backup first",Snackbar.LENGTH_LONG).show();
                }

            }
            else {
                Snackbar.make(getView(),"Please grant write external storage permission",Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {

            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT)
                    .show();

        }
    }

    /** Copies the database from data to external storage*/
    private void exportDB() {
        requestPermissions();
        try {
            File sd = Environment.getExternalStorageDirectory();
            //File data = Environment.getDataDirectory();

            if (sd.canWrite()) {

                String backupDBPath = "QuickWalletData";
                File currentDB = getActivity().getDatabasePath("QuickWallet.db");
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Snackbar.make(getView(),"Backup Completed",Snackbar.LENGTH_LONG).show();

            }
            else {
                Snackbar.make(getView(),"Please grant write external storage permission",Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {

            Snackbar.make(getView(),"Backup Failed",Snackbar.LENGTH_LONG).show();

        }
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                dialogBuilder.setMessage("The write external storage permission is required to create a copy of the database on sd card")
                        .setPositiveButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                            }
                        });
                dialogBuilder.show();
            } else
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }
}
