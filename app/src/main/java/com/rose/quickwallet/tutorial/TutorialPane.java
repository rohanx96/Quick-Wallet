package com.rose.quickwallet.tutorial;

/**
 * Created by rose on 11/11/15.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rose.quickwallet.R;

public class TutorialPane extends Fragment {

    final static String POSITION = "position";
    int position;
    public static TutorialPane newInstance(int position) {
        TutorialPane pane = new TutorialPane();
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        pane.setArguments(args);
        return pane;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        position = getArguments().getInt(POSITION);
        int layoutResId;
        switch(position){
            case 0:
                layoutResId = R.layout.fragment_tutorial_one;
                break;
            case 1:
                layoutResId = R.layout.fragment_tutorial_two;
                break;
            case 2:
                layoutResId = R.layout.fragment_tutorial_three;
                break;
            case 3:
                layoutResId = R.layout.fragment_tutorial_four;
                break;
            case 4:
                layoutResId = R.layout.fragment_tutorial_five;
                break;
            case 5:
                layoutResId = R.layout.fragment_tutorial_transparent;
                break;
            default:
                layoutResId = -1;
        }
        ViewGroup rootView = (ViewGroup) inflater.inflate(layoutResId, container, false);
        rootView.setTag(position);
        return rootView;
    }
}
