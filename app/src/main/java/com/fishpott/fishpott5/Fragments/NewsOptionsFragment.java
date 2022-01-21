package com.fishpott.fishpott5.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fishpott.fishpott5.Activities.MessengerActivity;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;

public class NewsOptionsFragment extends Fragment {
    private static final String NEWS_ID = "NEWS_ID";

    private String newsId = "";
    private TextView  mReportNewsTextTextView, mReportNewsInfoTextTextView;
    View view;

    public NewsOptionsFragment() {}

    public static NewsOptionsFragment newInstance(String newsId) {
        NewsOptionsFragment fragment = new NewsOptionsFragment();
        Bundle args = new Bundle();
        args.putString(NEWS_ID, newsId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newsId = getArguments().getString(NEWS_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_news_options, container, false);

        //mCopyNewsLinkTextTextView = view.findViewById(R.id.copy_news_link_textview);
        //mCopyNewsLinkInfoTextTextView = view.findViewById(R.id.copy_news_link_info_textview);
        mReportNewsTextTextView = view.findViewById(R.id.report_news_textview);
        mReportNewsInfoTextTextView = view.findViewById(R.id.report_news_info_textview);

        mReportNewsTextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] chatData = {
                        "s_" + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID) + Config.FP_ID,
                        "fishpot_inc",
                        "fp",
                        newsId
                };
                Config.openActivity4(getActivity(), MessengerActivity.class, 1, 0, 1, "CHAT_INFO", chatData);
            }
        });
        mReportNewsInfoTextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] chatData = {
                        "s_" + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID) + Config.FP_ID,
                        "fishpot_inc",
                        "fp",
                        newsId
                };
                Config.openActivity4(getActivity(), MessengerActivity.class, 1, 0, 1, "CHAT_INFO", chatData);
            }
        });

        /*
        mCopyNewsLinkTextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.copyToClipBoardId(getActivity().getApplicationContext(), Config.CLIP_BOARD_FISHPOTT_NEWS_ID, newsId);
                Config.showToastType1(getActivity(), getString(R.string.news_link_copied));
                getActivity().onBackPressed();
            }
        });

        mCopyNewsLinkInfoTextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.copyToClipBoardId(getActivity().getApplicationContext(), Config.CLIP_BOARD_FISHPOTT_NEWS_ID, newsId);
                Config.showToastType1(getActivity(), getString(R.string.news_link_copied));
                getActivity().onBackPressed();
            }
        });
        */
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mReportNewsTextTextView = view.findViewById(R.id.report_news_textview);
        mReportNewsInfoTextTextView = view.findViewById(R.id.report_news_info_textview);
    }

    @Override
    public void onStop() {
        super.onStop();
        mReportNewsTextTextView = null;
        mReportNewsInfoTextTextView = null;
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String newsId = null;
        mReportNewsTextTextView = null;
        mReportNewsInfoTextTextView = null;
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_newsoptions_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
