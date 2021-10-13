package com.fishpott.fishpott5.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fishpott.fishpott5.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestoFragment extends Fragment {

    public SuggestoFragment() {
        // Required empty public constructor
    }


    public static SuggestoFragment newInstance() {
        SuggestoFragment fragment = new SuggestoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggesto, container, false);
    }
}