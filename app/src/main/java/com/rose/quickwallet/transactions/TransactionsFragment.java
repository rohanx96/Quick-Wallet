package com.rose.quickwallet.transactions;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.SearchView;
import android.widget.TextView;

import com.rose.quickwallet.R;
import com.rose.quickwallet.callbackhepers.ItemTouchHelperCallback;
import com.rose.quickwallet.callbackhepers.RecyclerViewCallback;
import com.rose.quickwallet.transactions.data.DatabaseHelper;

import java.util.ArrayList;

import static android.content.Context.SEARCH_SERVICE;

//import com.quickblox.auth.QBAuth;
//import com.quickblox.auth.model.QBSession;
//import com.quickblox.core.QBEntityCallbackImpl;
//import com.quickblox.core.QBSettings;
//import com.quickblox.users.model.QBUser;
//import com.rose.quickwallet.MyAccountActivity;
//import com.rose.quickwallet.quickblox.Consts;
//import com.rose.quickwallet.quickblox.GoCloudActivity;
//import com.rose.quickwallet.quickblox.RetreiveUsersService;
//import com.rose.quickwallet.quickblox.pushnotifications.GCMClientHelper;
//import java.util.List;


public class TransactionsFragment extends Fragment implements RecyclerViewCallback {
    View rootView;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private SharedPreferences preferences;
    boolean shouldAnimate = false;
    Context mContext;
    private boolean isTablet;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_transactions,container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isTablet = getActivity().findViewById(R.id.details_fragment_disabled_text)!=null;
        mContext = getActivity();
        shouldAnimate = true;
        //loadAd();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RecyclerAdapter(new ArrayList<RecyclerViewItem>(), TransactionsFragment.this, mContext, isTablet);
        recyclerView.setAdapter(adapter);
        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback((RecyclerAdapter) recyclerView.getAdapter());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();

        DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
        //adapter.refreshDataList(databaseHelper.getData(),false);
        adapter.setDataList(databaseHelper.getData());
        adapter.setmCurrency(preferences.getString("prefCurrency", ""));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getAdapter().notifyDataSetChanged();
        shouldAnimate = false;
        resetFabScale();
        FloatingActionButton fab = ((FloatingActionButton) rootView.findViewById(R.id.fab_add));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start(view);
            }
        });
        if (Build.VERSION.SDK_INT < 21)
           setupSearchEditText();
        else
            setupSearchView();
    }

    /* OnClick method for fab button to start AddNewTransaction Activity */
    public void start(View view) {
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add);
        //fab.setImageResource(R.color.colorAccent);
        startRippleAnimation(fab);
        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, AddNewTransactionActivity.class);
                intent.putExtra("action", "generic");
                intent.setAction("generic");
                startActivity(intent);
                overridePendingTransition(0, R.anim.stay);
            }
        },250);*/
    }

    public void setupSearchView() {
        SearchManager searchManager = (SearchManager) mContext.getSystemService(SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getActivity().getComponentName());
        SearchView searchView = (SearchView) rootView.findViewById(R.id.search_view_home);
        searchView.setSearchableInfo(searchableInfo);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s); //Search for the string s. Look at the method definition below for execution details
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s); //Search for the string s. Look at the method definition for execution details
                return false;
            }
        });
    }

    public void setupSearchEditText() {
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar_main_activity);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.search_edit_text);
            actionBar.setDisplayShowTitleEnabled(false);
            setupSearchView();
        }
    }

    /**
     * Searches for string s in names stored in database and updates the recycler view with the datalist obtained from database
     */
    public void search(String s) {
        DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
        adapter.setDataList(databaseHelper.search(s));
        recyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onItemSwiped(final RecyclerViewItem item) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
        databaseHelper.updateItemOnSwipe(item.getName());
        String currency = preferences.getString("prefCurrency", "");
        View header = recyclerView.getChildAt(0);
        //RelativeLayout header = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.recycler_view_header_layout, recyclerView,false);
        TextView totalLent = (TextView) header.findViewById(R.id.lent_header);
        //TextView totalLent = (TextView) recyclerView.findViewById(R.id.lent_header);
        TextView totalBorrowed = (TextView) header.findViewById(R.id.borrowed_header);
        //TextView totalBorrowed = (TextView) recyclerView.findViewById(R.id.borrowed_header);
        //TODO: Find a solution to update the balance for null views
        // These views are returned null when the header image is not visible on screen
        // (When user has scrolled down and the view at position 0 has been recycled
        if (totalLent != null) // prevent force close due to null
            totalLent.setText(getResources().getString(R.string.lent_colon) + currency + databaseHelper.totalLent());
        if (totalBorrowed != null) // prevent force close due to null
            totalBorrowed.setText(getResources().getString(R.string.borrowed_colon) + currency + databaseHelper.totalBorrowed());
        //recyclerView.getAdapter().notifyDataSetChanged();
        databaseHelper.close();
        Snackbar.make(rootView, getString(R.string.snackbar_balance_cleared_beg) + item.getName() + getString(R.string.snackbar_balance_cleared_end), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
                databaseHelper.onUndoSwipe(item);
                adapter.setDataList(databaseHelper.getData());
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }).show();
        if(isTablet)
            getFragmentManager().findFragmentById(R.id.container_details_fragment_main).onResume();
    }

    @Override
    public int clickedItemPosition(View v) {
        return recyclerView.indexOfChild(v);
    }

    @Override
    public void removeItem(int position) {
        recyclerView.removeViewAt(position);
    }


    public void refreshDataList(DatabaseHelper databaseHelper){
        adapter.setDataList(databaseHelper.getData());
        recyclerView.getAdapter().notifyDataSetChanged();
    }
    /**
     * Increases the fab button size to fill the screen creating a ripple effect
     */
    public void startRippleAnimation(final FloatingActionButton view) {
        /* Due to setting margin in our fab button when we scale it up it can not take up the entire screen.
        To overcome this we make another fab button at the same position as our original fab button without setting margin to this fab
        button. We then scale up this new fab button
         */
        CoordinatorLayout frame = (CoordinatorLayout) rootView;
        // Check if it has been previously added. If not then we create a new fab button
        FloatingActionButton imageView = (FloatingActionButton) frame.findViewById(R.id.fab_expand_menu_button);
        if (imageView == null) {
            imageView = new FloatingActionButton(mContext);
            imageView.setId(R.id.fab_expand_menu_button); // Set an id so that it can be found later in order to scale it down

            //imageView.setImageResource(R.drawable.circle_selected);
            //imageView.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            /* Set the position of this new fab button same as that of our existing fab button */
            imageView.setX(view.getX());
            imageView.setY(view.getY());

            frame.addView(imageView);
        }
        imageView.setVisibility(View.VISIBLE);
        // Start animation
        imageView.animate().scaleX(35.0f).scaleY(35.0f).setDuration(500).setInterpolator(new AccelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if(isTablet){
                            AddNewTransactionFragment fragment = new AddNewTransactionFragment();
                            Bundle args = new Bundle();
                            args.putString("action","generic");
                            fragment.setArguments(args);
                            getFragmentManager().beginTransaction().add(R.id.container_left_fragment_main, fragment).
                                    addToBackStack(null).commit();
                        }
                        else {
                            Intent intent = new Intent(mContext, AddNewTransactionActivity.class);
                            intent.putExtra("action", "generic");
                            intent.setAction("generic");
                            ((MainActivity) mContext).overridePendingTransition(0, 0);
                            startActivity(intent);
                        }
                    }
                });
        /*Intent intent = new Intent(MainActivity.this, AddNewTransactionActivity.class);
        intent.putExtra("action", "generic");
        intent.setAction("generic");
        Bundle bundle = ActivityOptionsCompat.makeScaleUpAnimation(view,0,0,view.getWidth(),view.getHeight()).toBundle();
        startActivity(intent, bundle);*/
    }

    /**
     * Resets the size of the dynamically added fab. Used in OnResume
     */
    public void resetFabScale() {
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_expand_menu_button);
        if (fab != null) {
            // Visibility is set to gone or else it comes up over our actual fab hiding its + image
            fab.setVisibility(View.GONE);
            fab.setScaleX(1.0f);
            fab.setScaleY(1.0f);
        }
    }
}