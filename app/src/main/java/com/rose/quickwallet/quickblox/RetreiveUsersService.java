package com.rose.quickwallet.quickblox;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.Consts;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.rose.quickwallet.quickblox.pushnotifications.PendingNotificationsDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by rose on 17/10/15.
 *
 */
public class RetreiveUsersService extends IntentService {

    static int userNumber = 1;
    SharedPreferences preferences;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RetreiveUsersService(String name) {
        super(name);
    }
    public RetreiveUsersService(){
        super("RetreiveUsersService");
    }
    @Override
    protected void onHandleIntent(final Intent intent) {
        // Start
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSignedIn = preferences.getBoolean(com.rose.quickwallet.quickblox.Consts.IS_SIGNED_UP, false);
        if (isSignedIn) {
            //Log.i("retrieveUsersService", "Inside retreive users service");
            if(intent.getBooleanExtra("createSession",false)) {
                QBSettings.getInstance().fastConfigInit(com.rose.quickwallet.quickblox.Consts.APP_ID, com.rose.quickwallet.quickblox.Consts.AUTH_KEY, com.rose.quickwallet.quickblox.Consts.AUTH_SECRET);
                final QBUser user = new QBUser();
                user.setEmail(preferences.getString(com.rose.quickwallet.quickblox.Consts.USER_LOGIN, null));
                user.setPassword(preferences.getString(com.rose.quickwallet.quickblox.Consts.USER_PASSWORD, null));
                //DialogUtils.showLong(getApplicationContext(), "creating session for user: " + user);
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
                        QBAuth.createSession(user,new QBEntityCallbackImpl<QBSession>() {
                        @Override
                        public void onSuccess(QBSession session, Bundle params) {
                            //Log.i("retrieveUsersService", "Success creating session");
                            retrieveAllUsersFromPage(preferences.getInt(com.rose.quickwallet.quickblox.Consts.SHARED_PREFERENCES_USERS_CURRENT_PAGE, 1));
                            sendNotifications();
                        }

                        @Override
                        public void onError(List<String> errors) {
                            // errors
                        }
                    });
                    }
                });
            }
            else {
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
                        if(intent.getBooleanExtra("sendNotifications",false)){
                            sendNotifications();
                        }
                        else
                            retrieveAllUsersFromPage(preferences.getInt(com.rose.quickwallet.quickblox.Consts.SHARED_PREFERENCES_USERS_CURRENT_PAGE, 1));
                        //sendNotifications();
                    }
                });
            }
        }
    }

    //TODO: Find a better solution to retreiving users. Also update users entry instead of ignoring for existing IDs
    private void retrieveAllUsersFromPage(int page){
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(100);
        /*Bundle params = new Bundle();
        ArrayList<QBUser> users = null;
        try {
            users = QBUsers.getUsers(pagedRequestBuilder, params);
        } catch (QBResponseException e) {
        }
        if(users != null) {
            Toast.makeText(RetreiveUsersService.this, users.size() + "users returned", Toast.LENGTH_LONG).show();
            Log.i("retrieveUsersService", users.size() + "users returned");
            for (QBUser user : users) {
                QuickbloxUsersDatabaseHelper databaseHelper = new QuickbloxUsersDatabaseHelper(RetreiveUsersService.this);
                databaseHelper.insertUser(user.getPhone(), user.getId());
                ++userNumber;
            }

            int currentPage = params.getInt(Consts.CURR_PAGE);
            int totalEntries = params.getInt(Consts.TOTAL_ENTRIES);

            if (userNumber < totalEntries) {
                retrieveAllUsersFromPage(currentPage + 1);
            } else {
                preferences.edit().putInt(com.rose.quickwallet.quickblox.Consts.SHARED_PREFERENCES_USERS_CURRENT_PAGE, currentPage).apply();
            }
        }
        else {
            Toast.makeText(RetreiveUsersService.this, "No users returned", Toast.LENGTH_LONG).show();
            Log.i("retrieveUsersService", "No users returned");
        }*/
        QBUsers.getUsers(pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                //Log.i("retrieveUsersService", users.size() + "users returned");
                QuickbloxUsersDatabaseHelper databaseHelper = new QuickbloxUsersDatabaseHelper(RetreiveUsersService.this);
                for(QBUser user : users){
                    databaseHelper.insertUser(user.getPhone(),user.getId());
                    ++userNumber;
                }

                int currentPage = params.getInt(Consts.CURR_PAGE);
                int totalEntries = params.getInt(Consts.TOTAL_ENTRIES);

                if(userNumber < totalEntries){
                    retrieveAllUsersFromPage(currentPage+1);
                }
                else {
                    preferences.edit().putInt(com.rose.quickwallet.quickblox.Consts.SHARED_PREFERENCES_USERS_CURRENT_PAGE,currentPage).apply();
                    databaseHelper.closeDatabase();
                }

            }

            @Override
            public void onError(List<String> errors) {
                Log.i("retrieveUsersService", "Error: " + errors);
            }
        });
    }

    public void sendNotifications(){
        final PendingNotificationsDatabaseHelper databaseHelper = new PendingNotificationsDatabaseHelper(RetreiveUsersService.this);
        ArrayList<QBEvent> pendingEvents = databaseHelper.getPendingNotifications();
        for (final QBEvent event : pendingEvents) {
            QBMessages.createEvent(event, new QBEntityCallback<QBEvent>() {
                @Override
                public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                    databaseHelper.deleteEvent(Long.valueOf(event.getName()));
                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(List<String> list) {
                    Log.i("SendPendingNotification", "Errors: " + list);
                }
            });
        }
    }
}
