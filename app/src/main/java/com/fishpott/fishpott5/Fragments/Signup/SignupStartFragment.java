package com.fishpott.fishpott5.Fragments.Signup;

import android.app.Activity;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.fishpott.fishpott5.Activities.WebViewActivity;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;

public class SignupStartFragment extends Fragment implements View.OnClickListener {

    private ImageView mBackImageView;
    private Button mPersonalAccountStartButton, mBusinessAccountStartButton;
    private ConstraintLayout mPrivacyPolicyHolderConstraintLayout;
    private View view = null;

    public static SignupStartFragment newInstance() {
        return new SignupStartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup_start, container, false);

        mBackImageView = view.findViewById(R.id.fragment_of_signupactivity_signupstart_back_imageview);
        mPersonalAccountStartButton = view.findViewById(R.id.fragment_of_signupactivity_signupstart_personalstartbutton);
        mBusinessAccountStartButton = view.findViewById(R.id.fragment_of_signupactivity_signupstart_businessstartbutton);
        mPrivacyPolicyHolderConstraintLayout = view.findViewById(R.id.privacy_policy_page);

        mBackImageView.setOnClickListener(this);
        mPersonalAccountStartButton.setOnClickListener(this);
        mBusinessAccountStartButton.setOnClickListener(this);
        mPrivacyPolicyHolderConstraintLayout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fragment_of_signupactivity_signupstart_back_imageview) {
            Activity thisActivity =  getActivity();
            thisActivity.onBackPressed();
            thisActivity = null;
        } else if (view.getId() == R.id.fragment_of_signupactivity_signupstart_personalstartbutton) {
            Config.openFragment(getActivity().getSupportFragmentManager(), R.id.activity_signup_fragment_holder, SignupPersonalStage1Fragment.newInstance(), "SignupPersonalStage1Fragment", 1);
        } else if (view.getId() == R.id.fragment_of_signupactivity_signupstart_businessstartbutton) {
            Config.openFragment(getActivity().getSupportFragmentManager(),R.id.activity_signup_fragment_holder, SignupBusinessStage1Fragment.newInstance(), "SignupBusinessStage1Fragment", 1);
        } else if(view.getId() == R.id.privacy_policy_page){
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, Config.CURRENT_HTTP_IN_USE + "www.fishpott.com/pp.html");
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUPSTART-FRAGMENT");
        mBackImageView = view.findViewById(R.id.fragment_of_signupactivity_signupstart_back_imageview);
        mPersonalAccountStartButton = view.findViewById(R.id.fragment_of_signupactivity_signupstart_personalstartbutton);
        mBusinessAccountStartButton = view.findViewById(R.id.fragment_of_signupactivity_signupstart_businessstartbutton);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED SIGNUPSTART-FRAGMENT");
        mBackImageView = null;
        mPersonalAccountStartButton = null;
        mBusinessAccountStartButton = null;
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED SIGNUPSTART-FRAGMENT");
        mBackImageView = null;
        mPersonalAccountStartButton = null;
        mBusinessAccountStartButton = null;
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_signupstart_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }

}
