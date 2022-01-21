package com.fishpott.fishpott5.Fragments.Signup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;

public class SignupBusinessStage1Fragment extends Fragment implements View.OnClickListener {

    private EditText mBusinessName, mBranchLocation, mBusinessEmail;
    private ImageView mBackImageView;
    private Button mContinueButton;
    private View view = null;

    public static SignupBusinessStage1Fragment newInstance() {
        return new SignupBusinessStage1Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_signup_business_stage1, container, false);

        mBackImageView = view.findViewById(R.id.fragment_signup_businessstage1_back_imageview);
        mBusinessName = view.findViewById(R.id.fragment_signup_businessstage1_businessname_edit_text);
        mBusinessEmail = view.findViewById(R.id.fragment_signup_businessstage1_businessemail_edit_text);
        mBranchLocation = view.findViewById(R.id.fragment_signup_businessstage1_businesslocation_edit_text);
        mContinueButton = view.findViewById(R.id.fragment_signup_businessstage1_continue_button);

        mContinueButton.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fragment_signup_businessstage1_continue_button){
            if(!mBusinessName.getText().toString().trim().equalsIgnoreCase("") && !mBranchLocation.getText().toString().trim().equalsIgnoreCase("") && !mBusinessEmail.getText().toString().trim().equalsIgnoreCase("")){
                Log.e("1mBusinessEmail", mBusinessEmail.getText().toString().trim());
                Config.openFragment(getActivity().getSupportFragmentManager(),R.id.activity_signup_fragment_holder, SignupBusinessStage2Fragment.newInstance(mBusinessName.getText().toString().trim(), mBranchLocation.getText().toString().trim(), mBusinessEmail.getText().toString().trim()), "SignupPersonalStage2Fragment", 1);
            }
        } else if(view.getId() == R.id.fragment_signup_businessstage1_back_imageview){
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUP-BUSINESS-STAGE_1-FRAGMENT");
        mBackImageView = view.findViewById(R.id.fragment_signup_businessstage1_back_imageview);
        mBusinessName = view.findViewById(R.id.fragment_signup_businessstage1_businessname_edit_text);
        mBusinessEmail = view.findViewById(R.id.fragment_signup_businessstage1_businessemail_edit_text);
        mBranchLocation = view.findViewById(R.id.fragment_signup_businessstage1_businesslocation_edit_text);
        mContinueButton = view.findViewById(R.id.fragment_signup_businessstage1_continue_button);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED SIGNUP-BUSINESS-STAGE_1-FRAGMENT");
        mBusinessName = null;
        mBranchLocation = null;
        mBackImageView = null;
        mContinueButton = null;
        mBusinessEmail =  null;
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED SIGNUP-BUSINESS-STAGE_1-FRAGMENT");
        mBusinessName = null;
        mBranchLocation = null;
        mBackImageView = null;
        mBusinessEmail =  null;
        mContinueButton = null;
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_signupbusinessstage1_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
