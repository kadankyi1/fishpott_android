package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;


public class InfoFragment extends Fragment {

    private static final String BACKGROUND_RESOURCE = "background_resource";
    private static final String CENTER_ICON_RESOURCE = "center_icon_resource";
    private static final String CENTER_ICON_BACKGROUND_RESOURCE = "center_icon_background_resource";
    private static final String TITLE_TEXT = "title_text_resource";
    private static final String BODY_TEXT = "body_resource";
    private int centerIconResource, centerIconBackgroundResource;
    private String titleText, bodyText;
    private TextView mTitleTextView, mBodyTextView;
    private ImageView mCenterIconImageView;
    private ConstraintLayout mCenterIconBackgroundConstraintLayout;

    public InfoFragment() {}

    public static InfoFragment newInstance(int centerIconResource, int centerIconBackgroundResource, String titleText, String bodyText) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putInt(CENTER_ICON_RESOURCE, centerIconResource);
        args.putInt(CENTER_ICON_BACKGROUND_RESOURCE, centerIconBackgroundResource);
        args.putString(TITLE_TEXT, titleText);
        args.putString(BODY_TEXT, bodyText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            centerIconResource = getArguments().getInt(CENTER_ICON_RESOURCE);
            centerIconBackgroundResource = getArguments().getInt(CENTER_ICON_BACKGROUND_RESOURCE);
            titleText = getArguments().getString(TITLE_TEXT);
            bodyText = getArguments().getString(BODY_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        mTitleTextView = (TextView) view.findViewById(R.id.activity_main_info_fragment_title_textView);
        mBodyTextView = (TextView) view.findViewById(R.id.activity_main_info_fragment_body_textView);
        mCenterIconImageView = (ImageView) view.findViewById(R.id.activity_main_info_fragment_icon_imageView);
        mCenterIconBackgroundConstraintLayout = (ConstraintLayout) view.findViewById(R.id.activity_main_info_fragment_icon_holder_background_white);

        if(centerIconResource == 0){
            mCenterIconImageView.setImageResource(R.drawable.fishpott_splash_icon);
            mCenterIconBackgroundConstraintLayout.setBackgroundResource(R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background);
        } else {
            mCenterIconImageView.setImageResource(centerIconResource);
            mCenterIconBackgroundConstraintLayout.setBackgroundResource(centerIconBackgroundResource);
        }

        mTitleTextView.setText(titleText);
        mBodyTextView.setText(bodyText);



        return view;
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
