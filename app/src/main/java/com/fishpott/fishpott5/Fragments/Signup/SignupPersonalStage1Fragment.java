package com.fishpott.fishpott5.Fragments.Signup;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.ViewModels.SignupPersonalStage1ViewModel;

public class SignupPersonalStage1Fragment extends Fragment implements View.OnClickListener {

    private String gender = "";
    private EditText mFirstName, mLastName;
    private RadioButton mMaleGenderRadioButton, mFemaleGenderRadioButton;
    private ImageView mMaleGenderImageView, mFemaleGenderImageView, mBackImageView;
    private TextView mMaleGenderTextView, mFemaleGenderTextView;
    private Button mContinueButton;
    private View view = null;

    public static SignupPersonalStage1Fragment newInstance() {
        return new SignupPersonalStage1Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_signup_personal_stage1, container, false);

        mBackImageView = view.findViewById(R.id.fragment_signup_personalstage1_back_imageview);
        mFirstName = view.findViewById(R.id.fragment_signup_personalstage1_firstname_edit_text);
        mLastName = view.findViewById(R.id.fragment_signup_personalstage1_lastname_edit_text);
        mMaleGenderRadioButton = view.findViewById(R.id.fragment_signup_personalstage1_gender_male_radiobutton);
        mFemaleGenderRadioButton = view.findViewById(R.id.fragment_signup_personalstage1_gender_female_radiobutton);
        mMaleGenderImageView = view.findViewById(R.id.fragment_signup_personalstage1_gender_male_imageview);
        mFemaleGenderImageView = view.findViewById(R.id.fragment_signup_personalstage1_gender_female_imageview);
        mMaleGenderTextView = view.findViewById(R.id.fragment_signup_personalstage1_gender_male_textview);
        mFemaleGenderTextView = view.findViewById(R.id.fragment_signup_personalstage1_gender_female_textview);
        mContinueButton = view.findViewById(R.id.fragment_signup_personalstage1_continue_button);


        mMaleGenderRadioButton.setOnClickListener(this);
        mMaleGenderTextView.setOnClickListener(this);
        mMaleGenderImageView.setOnClickListener(this);
        mFemaleGenderRadioButton.setOnClickListener(this);
        mFemaleGenderTextView.setOnClickListener(this);
        mFemaleGenderImageView.setOnClickListener(this);
        mContinueButton.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);

        return view;
    }

    public void setMaleClicked(){
        gender = "male";
        mFemaleGenderRadioButton.setChecked(false);
    }

    public void setFemaleClicked(){
        gender = "female";
        mMaleGenderRadioButton.setChecked(false);
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fragment_signup_personalstage1_gender_male_radiobutton){
            setMaleClicked();
        } else if(view.getId() == R.id.fragment_signup_personalstage1_gender_male_textview){
            mMaleGenderRadioButton.performClick();
            setMaleClicked();
        } else if(view.getId() == R.id.fragment_signup_personalstage1_gender_male_imageview){
            mMaleGenderRadioButton.performClick();
            setMaleClicked();
        } else if(view.getId() == R.id.fragment_signup_personalstage1_gender_female_radiobutton){
            setFemaleClicked();
        } else if(view.getId() == R.id.fragment_signup_personalstage1_gender_female_textview){
            mFemaleGenderRadioButton.performClick();
            setFemaleClicked();
        } else if(view.getId() == R.id.fragment_signup_personalstage1_gender_female_imageview){
            mFemaleGenderRadioButton.performClick();
            setFemaleClicked();
        } else if(view.getId() == R.id.fragment_signup_personalstage1_continue_button){
            if(!mFirstName.getText().toString().trim().equalsIgnoreCase("") && !mLastName.getText().toString().trim().equalsIgnoreCase("") && !gender.trim().equalsIgnoreCase("")){
                Config.openFragment(getActivity().getSupportFragmentManager(),R.id.activity_signup_fragment_holder, SignupPersonalStage2Fragment.newInstance(mFirstName.getText().toString().trim(), mLastName.getText().toString().trim(), gender), "SignupPersonalStage2Fragment", 1);
            }
        } else if(view.getId() == R.id.fragment_signup_personalstage1_back_imageview){
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUP-PERSONAL-STAGE_1-FRAGMENT");
        mBackImageView = view.findViewById(R.id.fragment_signup_personalstage1_back_imageview);
        mFirstName = view.findViewById(R.id.fragment_signup_personalstage1_firstname_edit_text);
        mLastName = view.findViewById(R.id.fragment_signup_personalstage1_lastname_edit_text);
        mMaleGenderRadioButton = view.findViewById(R.id.fragment_signup_personalstage1_gender_male_radiobutton);
        mFemaleGenderRadioButton = view.findViewById(R.id.fragment_signup_personalstage1_gender_female_radiobutton);
        mMaleGenderImageView = view.findViewById(R.id.fragment_signup_personalstage1_gender_male_imageview);
        mFemaleGenderImageView = view.findViewById(R.id.fragment_signup_personalstage1_gender_female_imageview);
        mMaleGenderTextView = view.findViewById(R.id.fragment_signup_personalstage1_gender_male_textview);
        mFemaleGenderTextView = view.findViewById(R.id.fragment_signup_personalstage1_gender_female_textview);
        mContinueButton = view.findViewById(R.id.fragment_signup_personalstage1_continue_button);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED SIGNUP-PERSONAL-STAGE_1-FRAGMENT");
        mFirstName = null;
        mLastName = null;
        mMaleGenderRadioButton = null;
        mFemaleGenderRadioButton = null;
        mMaleGenderImageView = null;
        mFemaleGenderImageView = null;
        mBackImageView = null;
        mMaleGenderTextView = null;
        mFemaleGenderTextView = null;
        mContinueButton = null;
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED SIGNUP-PERSONAL-STAGE_1-FRAGMENT");
        gender = null;
        mFirstName = null;
        mLastName = null;
        mMaleGenderRadioButton = null;
        mFemaleGenderRadioButton = null;
        mMaleGenderImageView = null;
        mFemaleGenderImageView = null;
        mBackImageView = null;
        mMaleGenderTextView = null;
        mFemaleGenderTextView = null;
        mContinueButton = null;
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_signuppersonalstage1_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
