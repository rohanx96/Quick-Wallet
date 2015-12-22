package com.rose.quickwallet.transactions;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.rose.quickwallet.R;
import com.rose.quickwallet.callbackhepers.DetailsRecyclerViewCallback;

/**
 *
 *Created by rose on 28/7/15
 * .
 */
public class DetailsActivity extends AppCompatActivity implements DetailsRecyclerViewCallback{
    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;
    private String name;
    private String imageUri;
    private DetailsRecyclerViewAdapter recyclerViewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        name = getIntent().getStringExtra("Name");
        toolbarLayout.setTitle(name);
        Toolbar toolbar = (Toolbar) findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        imageUri = getIntent().getStringExtra("imageUri");
        ImageView contactImage = (ImageView) findViewById(R.id.contact_image_toolbar);
        if(imageUri!=null)
            contactImage.setImageURI(Uri.parse(imageUri));
        else
            contactImage.setImageResource(R.drawable.contact_no_image);
        recyclerView = (RecyclerView)findViewById(R.id.details_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        databaseHelper = new DatabaseHelper(getApplicationContext());
        recyclerViewAdapter = new DetailsRecyclerViewAdapter(databaseHelper.getHistoryData(name),this,name);
        recyclerView.setAdapter(recyclerViewAdapter);
        /*ImageButton delete = (ImageButton) findViewById(R.id.delete_icon);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDelete(view);
            }
        });*/
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_floating_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAdd(view);
            }
        });
        final FloatingActionButton deleteButton = (FloatingActionButton) findViewById(R.id.delete_floating_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDelete(view);
            }
        });
        FloatingActionButton clearHistoryButton = (FloatingActionButton)findViewById(R.id.clear_history_floating_button);
        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearHistory(v);
            }
        });
        if(Build.VERSION.SDK_INT>=21){
            contactImage.setTransitionName("contactImage");
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_contact_image_transition));
            getWindow().setSharedElementExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.details_activity_return_transition));
            getWindow().setEnterTransition(new Slide().setDuration(500));
            getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.details_activity_return_transition));
        }
    }

    public void onAdd(View view) {
        Intent intent = new Intent(getApplicationContext(),AddNewTransactionActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY,name);
        intent.putExtra("action","add");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseHelper = new DatabaseHelper(getApplicationContext());
        //ArrayList<DetailsRecyclerViewItem> detailsRecyclerViewItems = databaseHelper.getHistoryData(name);
        //int newItems = detailsRecyclerViewItems.size() - recyclerViewAdapter.getDataList().size();
        recyclerViewAdapter.setDataList(databaseHelper.getHistoryData(name));
        //recyclerView.getAdapter().notifyDataSetChanged();
        //for(int i =0;i<=newItems;i++){
        //    recyclerView.getAdapter().notifyItemInserted(i);
        //}
        recyclerView.getAdapter().notifyDataSetChanged();
        TextView balanceText = (TextView) findViewById(R.id.details_balance_text);
        float balance = databaseHelper.getBalance(name);
        if(balance<0)
            balance = -1 * balance;
        balanceText.setText(getString(R.string.current_balance_colon) + balance);
        if (balance == 0)
            balanceText.setText(getString(R.string.no_balance));
        databaseHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void onClearHistory(View view){
        databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.clearHistory(name);
        DetailsRecyclerViewAdapter adapter = (DetailsRecyclerViewAdapter) recyclerView.getAdapter();
        adapter.setDataList(databaseHelper.getHistoryData(name));
        TextView balanceText = (TextView) findViewById(R.id.details_balance_text);
        balanceText.setText(R.string.no_balance);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void onDelete(View view){
        databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.deleteContact(name);
        finish();
    }

    @Override
    public void onDeleteTransaction() {
        TextView balanceText = (TextView) findViewById(R.id.details_balance_text);
        databaseHelper = new DatabaseHelper(this);
        recyclerViewAdapter.setDataList(databaseHelper.getHistoryData(name));
        recyclerView.getAdapter().notifyDataSetChanged();
        float balance = databaseHelper.getBalance(name);
        if(balance<0)
            balance = -1 * balance;
        balanceText.setText(getString(R.string.current_balance_colon) + balance);
        if (balance == 0)
            balanceText.setText(getString(R.string.no_balance));
        databaseHelper.close();
    }
}
