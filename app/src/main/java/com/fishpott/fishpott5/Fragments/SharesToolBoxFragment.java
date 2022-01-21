package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fishpott.fishpott5.Fragments.SharesToolBox.CancelSharesFragment;
import com.fishpott.fishpott5.Fragments.SharesToolBox.TransferCenterFragment;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class SharesToolBoxFragment extends Fragment implements TabLayout.OnTabSelectedListener {

    ViewPager vp;
    TabLayout tabLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SharesToolBoxFragment() {
        // Required empty public constructor
    }

    public static SharesToolBoxFragment newInstance(String param1, String param2) {
        SharesToolBoxFragment fragment = new SharesToolBoxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shares_toolbox, container, false);


        vp = (ViewPager) view.findViewById(R.id.mViewpager_ID);
        this.addPages();
        tabLayout = (TabLayout) view.findViewById(R.id.mTab_ID);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(vp);
        tabLayout.addOnTabSelectedListener(this);
        return view;
    }

    private void addPages() {
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getActivity().getSupportFragmentManager());
        pagerAdapter.addFragment(new TransferCenterFragment());
        pagerAdapter.addFragment(new CancelSharesFragment());
        vp.setAdapter(pagerAdapter);
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> fragments=new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment f) {
            fragments.add(f);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            if(position == 0){
                title = "Transfer Shares";
            } else if(position == 1){
                title = "Cancel Shares";
            }
            return title.toString();
        }
    }

    public void onTabSelected(TabLayout.Tab tab) {
        vp.setCurrentItem(tab.getPosition());
    }



    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }
    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
