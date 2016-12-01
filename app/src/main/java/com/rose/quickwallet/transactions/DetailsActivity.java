package com.rose.quickwallet.transactions;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionInflater;

import com.rose.quickwallet.R;

/**
 *
 *Created by rose on 28/7/15
 * .
 */
public class DetailsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        Bundle bundle = new Bundle();
        bundle.putString("Name",getIntent().getStringExtra("Name"));
        bundle.putString("imageUri", getIntent().getStringExtra("imageUri"));
        bundle.putInt("position",getIntent().getIntExtra("position",1));
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.container_details_fragment, fragment).commit();
        if(Build.VERSION.SDK_INT>=21){
            //getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_contact_image_transition));
            //getWindow().setSharedElementExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.details_activity_return_transition));
            getWindow().setEnterTransition(new Slide().setDuration(500));
            getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.details_activity_return_transition));
            //scheduleStartPostponedTransition(contactImage);
        }
    }
}
