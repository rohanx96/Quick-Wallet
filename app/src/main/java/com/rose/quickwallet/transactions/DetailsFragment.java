package com.rose.quickwallet.transactions;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.rose.quickwallet.R;
import com.rose.quickwallet.callbackhepers.DetailsRecyclerViewCallback;
import com.rose.quickwallet.transactions.data.DatabaseHelper;

/**
 * Created by rohanx96 on 11/18/16.
 */

public class DetailsFragment extends Fragment implements DetailsRecyclerViewCallback {
    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;
    private String name;
    private String imageUri;
    private Context mContext;
    private View rootView;
    private DetailsRecyclerViewAdapter recyclerViewAdapter;
    private String mCurrency;
    private boolean isTabletUI = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_details, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        if (getActivity() instanceof MainActivity) {
            isTabletUI = true;
        }
        name = getArguments().getString("Name");
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle(name);
        if (!isTabletUI) {
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.details_toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        imageUri = getArguments().getString("imageUri");
        ImageView contactImage = (ImageView) rootView.findViewById(R.id.contact_image_toolbar);
        if (imageUri != null)
            contactImage.setImageURI(Uri.parse(imageUri));
        else
            contactImage.setImageResource(R.drawable.contact_no_image);
        mCurrency = PreferenceManager.getDefaultSharedPreferences(mContext).getString("prefCurrency", "");
        recyclerView = (RecyclerView) rootView.findViewById(R.id.details_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        databaseHelper = new DatabaseHelper(mContext);
        recyclerViewAdapter = new DetailsRecyclerViewAdapter(databaseHelper.getHistoryData(name), this, mContext, name, mCurrency);
        recyclerView.setAdapter(recyclerViewAdapter);
        /*ImageButton delete = (ImageButton) findViewById(R.id.delete_icon);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDelete(view);
            }
        });*/
        if (Build.VERSION.SDK_INT >= 21)
            contactImage.setTransitionName("contactImage" + getArguments().getInt("position"));
        FloatingActionButton addButton = (FloatingActionButton) rootView.findViewById(R.id.add_floating_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAdd(view);
            }
        });
        final FloatingActionButton deleteButton = (FloatingActionButton) rootView.findViewById(R.id.delete_floating_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDelete(view);
            }
        });
        FloatingActionButton clearHistoryButton = (FloatingActionButton) rootView.findViewById(R.id.clear_history_floating_button);
        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearHistory(v);
            }
        });

    }

    public void onAdd(View view) {
        Intent intent = new Intent(mContext, AddNewTransactionActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, name);
        intent.putExtra("action", "add");
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseHelper = new DatabaseHelper(mContext);
        //ArrayList<DetailsRecyclerViewItem> detailsRecyclerViewItems = databaseHelper.getHistoryData(name);
        //int newItems = detailsRecyclerViewItems.size() - recyclerViewAdapter.getDataList().size();
        recyclerViewAdapter.setDataList(databaseHelper.getHistoryData(name));
        recyclerViewAdapter.setmCurrency(PreferenceManager.getDefaultSharedPreferences(mContext).getString("prefCurrency", ""));
        //recyclerView.getAdapter().notifyDataSetChanged();
        //for(int i =0;i<=newItems;i++){
        //    recyclerView.getAdapter().notifyItemInserted(i);
        //}
        recyclerView.getAdapter().notifyDataSetChanged();
        TextView balanceText = (TextView) rootView.findViewById(R.id.details_balance_text);
        float balance = databaseHelper.getBalance(name);
        if (balance < 0) {
            balance = -1 * balance;
            balanceText.setText(getString(R.string.current_balance_colon) + " - " + mCurrency + balance);
        } else balanceText.setText(getString(R.string.current_balance_colon) + mCurrency + balance);
        if (balance == 0)
            balanceText.setText(getString(R.string.no_balance));
        databaseHelper.close();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            startPostponedEnterTransition();
//        }
    }

    public void onClearHistory(View view) {
        databaseHelper = new DatabaseHelper(mContext);
        databaseHelper.clearHistory(name);
        DetailsRecyclerViewAdapter adapter = (DetailsRecyclerViewAdapter) recyclerView.getAdapter();
        adapter.setDataList(databaseHelper.getHistoryData(name));
        TextView balanceText = (TextView) rootView.findViewById(R.id.details_balance_text);
        balanceText.setText(R.string.no_balance);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void onDelete(View view) {
        databaseHelper = new DatabaseHelper(mContext);
        databaseHelper.deleteContact(name);
        if (isTabletUI)
            getFragmentManager().beginTransaction().remove(this).commit();
        else getActivity().finish();
    }

    @Override
    public void onDeleteTransaction() {
        TextView balanceText = (TextView) rootView.findViewById(R.id.details_balance_text);
        databaseHelper = new DatabaseHelper(mContext);
        recyclerViewAdapter.setDataList(databaseHelper.getHistoryData(name));
        recyclerView.getAdapter().notifyDataSetChanged();
        float balance = databaseHelper.getBalance(name);
        if (balance < 0)
            balance = -1 * balance;
        balanceText.setText(getString(R.string.current_balance_colon) + mCurrency + balance);
        if (balance == 0)
            balanceText.setText(getString(R.string.no_balance));
        databaseHelper.close();
    }

}
