package it.unipi.mywearapp;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This simple class is used because is mandatory when a fragment whatever is used. In this case, we use for necessity a blank fragment that
 * will be replace from the specific fragment selected by the Chart Activity
 */
public class BlankFragment extends Fragment {
    public BlankFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }
}
