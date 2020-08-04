package com.fishpott.fishpott5.Fragments.Signup;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.ConfirmPhoneNumberActivity;
import com.fishpott.fishpott5.Activities.SetProfilePictureActivity;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SignupBusinessStage3Fragment extends Fragment implements View.OnClickListener {

    private String businessName = "", branchLocation = "", dob = "", country = "", countryCode = "", networkResponse = "";
    private ImageView mBackImageView;
    private EditText mPottNameEditText, mPhoneNumberEditText, mPasswordEditText, mRetypedPasswordEditText, mReferrerPottNameEditText;
    private TextInputLayout mPottNameEditTextHolder, mPhoneNumberEditTextHolder, mPasswordEditTextHolder, mRetypedPasswordEditTextHolder, mReferrerPottNameEditTextHolder;
    private Button mContinueButton;
    private ProgressBar mSigningUpLoaderProgressBar;
    private TextView mFinalTextTextView, mAtTextView;
    private Thread signUpThread2 = null;
    private Dialog.OnCancelListener cancelListenerActive1;
    private View view = null;

    public static SignupBusinessStage3Fragment newInstance(String businessName, String branchLocation, String dob, String country, String countryCode) {
        SignupBusinessStage3Fragment fragment = new SignupBusinessStage3Fragment();
        Bundle args = new Bundle();
        args.putString("businessName", businessName);
        args.putString("branchLocation", branchLocation);
        args.putString("dob", dob);
        args.putString("country", country);
        args.putString("countryCode", countryCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            businessName = getArguments().getString("businessName");
            branchLocation = getArguments().getString("branchLocation");
            dob = getArguments().getString("dob");
            country = getArguments().getString("country");
            countryCode = getArguments().getString("countryCode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_signup_business_stage3, container, false);

        mBackImageView = view.findViewById(R.id.fragment_signup_businessstage3_back_imageview);
        mFinalTextTextView = view.findViewById(R.id.fragment_signup_businessstage3_final_step_textView);
        mAtTextView = view.findViewById(R.id.fragment_signup_businessstage3_at_textView);
        mPottNameEditText = view.findViewById(R.id.fragment_signup_businessstage3_pottname_edit_text);
        mPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_pottname_edit_text_layout_holder);
        mPhoneNumberEditText = view.findViewById(R.id.fragment_signup_businessstage3_phonenumber_edit_text);
        mPhoneNumberEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_phonenumber_edit_text_layout_holder);
        mPasswordEditText = view.findViewById(R.id.fragment_signup_businessstage3_password_edit_text);
        mPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_password_edit_text_layout_holder);
        mRetypedPasswordEditText = view.findViewById(R.id.fragment_signup_businessstage3_retypepassword_edit_text);
        mRetypedPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_retypepassword_edit_text_layout_holder);
        mReferrerPottNameEditText = view.findViewById(R.id.fragment_signup_businessstage3_referrerpottname_edit_text);
        mReferrerPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_referrerpottname_edit_text_layout_holder);
        mContinueButton = view.findViewById(R.id.fragment_signup_businessstage3_finish_button);
        mSigningUpLoaderProgressBar = view.findViewById(R.id.fragment_signup_businessstage3_signup_loader);

        mBackImageView.setOnClickListener(this);
        mContinueButton.setOnClickListener(this);

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(Connectivity.isConnected(getActivity())){
                    signUpThread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            signUpAndGetUserCredentials(businessName, branchLocation, "business", dob, country, mPottNameEditText.getText().toString().trim(), mPhoneNumberEditText.getText().toString().trim(), mPasswordEditText.getText().toString().trim(), mReferrerPottNameEditText.getText().toString().trim(), LocaleHelper.getLanguage(getActivity()));
                        }
                    });
                    signUpThread2.start();
                } else {
                    Config.showToastType1(getActivity(), getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                }
            }
        };
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fragment_signup_businessstage3_back_imageview){
            getActivity().onBackPressed();
        } else if(view.getId() == R.id.fragment_signup_businessstage3_finish_button){
            if(!mPottNameEditText.getText().toString().trim().equalsIgnoreCase("") && !mPhoneNumberEditText.getText().toString().trim().equalsIgnoreCase("") &&
                    !mPasswordEditText.getText().toString().trim().equalsIgnoreCase("") && !mRetypedPasswordEditText.getText().toString().trim().equalsIgnoreCase("") &&
                    !businessName.trim().equalsIgnoreCase("") && !branchLocation.trim().equalsIgnoreCase("") &&
                    !dob.trim().equalsIgnoreCase("") && !country.trim().equalsIgnoreCase("") && !countryCode.trim().equalsIgnoreCase("") &&
                    mPottNameEditText.getText().toString().trim().length() > 4 && !mPottNameEditText.getText().toString().trim().equalsIgnoreCase("linkups") &&
                    mPhoneNumberEditText.getText().toString().trim().length() > 10 &&
                    mPhoneNumberEditText.getText().toString().trim().substring(0, countryCode.length()).equalsIgnoreCase(countryCode) &&
                    !mPottNameEditText.getText().toString().trim().equalsIgnoreCase("linkups") &&
                    !mReferrerPottNameEditText.getText().toString().trim().equalsIgnoreCase("linkups") &&
                    mPasswordEditText.getText().toString().trim().equals(mRetypedPasswordEditText.getText().toString().trim())
                    ){
                cancelListenerActive1 = Config.showDialogType2(getActivity(), cancelListenerActive1, true);
            } else if(mPottNameEditText.getText().toString().trim().equalsIgnoreCase("") || mPottNameEditText.getText().toString().trim().length() < 5){
                Config.showDialogType1(getActivity(), "1", getString(R.string.fragment_signup_personalstage3_pottname_must_be_5_letters_or_more), "", null, true, "", "");
            } else if(!mPottNameEditText.getText().toString().trim().equalsIgnoreCase("") && mPottNameEditText.getText().toString().trim().equalsIgnoreCase("linkups")){
                Config.showToastType1(getActivity(), getActivity().getString(R.string.fragment_signup_personalstage3_your_pottname_cannot_be_linkups_linkups_is_a_reserved_word));
            } else if(!mReferrerPottNameEditText.getText().toString().trim().equalsIgnoreCase("") && mReferrerPottNameEditText.getText().toString().trim().length() < 5){
                Config.showToastType1(getActivity(), getString(R.string.fragment_signup_personalstage3_your_referrer_pott_name_must_be_5_letters_or_more));
            } else if(!mReferrerPottNameEditText.getText().toString().trim().equalsIgnoreCase("") && mReferrerPottNameEditText.getText().toString().trim().equalsIgnoreCase("linkups")){
                Config.showToastType1(getActivity(), getString(R.string.fragment_signup_personalstage3_your_referrer_pott_name_cannot_be_linkups_linkups_is_a_reserved_word));
            } else if(mPhoneNumberEditText.getText().toString().trim().length() < 10 || !mPhoneNumberEditText.getText().toString().trim().substring(0, countryCode.length()).equalsIgnoreCase(countryCode) || mPhoneNumberEditText.getText().toString().trim().length() < 11){
                Config.showDialogType1(getActivity(), "1", getString(R.string.fragment_signup_personalstage3_type_a_correct_phone_number_and_it_must_contain_your_country_code_like_this) + " " +  countryCode + "207393447", "", null, false, "", "");
            } else if(!mPasswordEditText.getText().toString().trim().equals(mRetypedPasswordEditText.getText().toString().trim())){
                Config.showToastType1(getActivity(), getActivity().getResources().getString(R.string.fragment_signup_personalstage3_passwords_do_not_match));
            } else if(businessName.trim().equalsIgnoreCase("") || branchLocation.trim().equalsIgnoreCase("")){
                Config.showDialogType1(getActivity(), "1", getString(R.string.fragment_signup_personalstage3_something_went_wrong_please_return_to_the_first_stage_to_fill_your_first_name_last_name_and_gender),"", null, true, "", "");
            } else if(dob.trim().equalsIgnoreCase("") || country.trim().equalsIgnoreCase("") || countryCode.trim().equalsIgnoreCase("")){
                Config.showDialogType1(getActivity(), "1", getString(R.string.fragment_signup_personalstage3_something_went_wrong_please_return_to_the_first_stage_to_fill_your_first_name_last_name_and_gender),"", null, true, "", "");
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUP-BUSINESS-STAGE_FINAL-FRAGMENT");
        mBackImageView = view.findViewById(R.id.fragment_signup_businessstage3_back_imageview);
        mFinalTextTextView = view.findViewById(R.id.fragment_signup_businessstage3_final_step_textView);
        mAtTextView = view.findViewById(R.id.fragment_signup_businessstage3_at_textView);
        mPottNameEditText = view.findViewById(R.id.fragment_signup_businessstage3_pottname_edit_text);
        mPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_pottname_edit_text_layout_holder);
        mPhoneNumberEditText = view.findViewById(R.id.fragment_signup_businessstage3_phonenumber_edit_text);
        mPhoneNumberEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_phonenumber_edit_text_layout_holder);
        mPasswordEditText = view.findViewById(R.id.fragment_signup_businessstage3_password_edit_text);
        mPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_password_edit_text_layout_holder);
        mRetypedPasswordEditText = view.findViewById(R.id.fragment_signup_businessstage3_retypepassword_edit_text);
        mRetypedPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_retypepassword_edit_text_layout_holder);
        mReferrerPottNameEditText = view.findViewById(R.id.fragment_signup_businessstage3_referrerpottname_edit_text);
        mReferrerPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_businessstage3_referrerpottname_edit_text_layout_holder);
        mContinueButton = view.findViewById(R.id.fragment_signup_businessstage3_finish_button);
        mSigningUpLoaderProgressBar = view.findViewById(R.id.fragment_signup_businessstage3_signup_loader);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            mFinalTextTextView.setVisibility(View.VISIBLE);
            mAtTextView.setVisibility(View.VISIBLE);
            mPottNameEditTextHolder.setVisibility(View.VISIBLE);
            mPhoneNumberEditTextHolder.setVisibility(View.VISIBLE);
            mPasswordEditTextHolder.setVisibility(View.VISIBLE);
            mRetypedPasswordEditTextHolder.setVisibility(View.VISIBLE);
            mReferrerPottNameEditTextHolder.setVisibility(View.VISIBLE);
            mContinueButton.setVisibility(View.VISIBLE);
            mSigningUpLoaderProgressBar.setVisibility(View.INVISIBLE);
            Config.showDialogType1(getActivity(), getString(R.string.login_activity_error), networkResponse, "", null, false, "", "");
            networkResponse = "";
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED SIGNUP-BUSINESS-STAGE_FINAL-FRAGMENT");
        mBackImageView = null;
        mPottNameEditText = null;
        mPhoneNumberEditText = null;
        mPasswordEditText = null;
        mRetypedPasswordEditText = null;
        mReferrerPottNameEditText = null;
        mPottNameEditTextHolder = null;
        mPhoneNumberEditTextHolder = null;
        mPasswordEditTextHolder = null;
        mRetypedPasswordEditTextHolder = null;
        mReferrerPottNameEditTextHolder = null;
        mContinueButton = null;
        mSigningUpLoaderProgressBar = null;
        mFinalTextTextView = null;
        mAtTextView = null;
        if(signUpThread2 != null){
            signUpThread2.interrupt();
            signUpThread2 = null;
        }
        Config.freeMemory();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED SIGNUP-BUSINESS-STAGE_FINAL-FRAGMENT");
        businessName = null;
        branchLocation = null;
        dob = null;
        country = null;
        countryCode = null;
        mBackImageView = null;
        mPottNameEditText = null;
        mPhoneNumberEditText = null;
        mPasswordEditText = null;
        mRetypedPasswordEditText = null;
        mReferrerPottNameEditText = null;
        mPottNameEditTextHolder = null;
        mPhoneNumberEditTextHolder = null;
        mPasswordEditTextHolder = null;
        mRetypedPasswordEditTextHolder = null;
        mReferrerPottNameEditTextHolder = null;
        mContinueButton = null;
        mSigningUpLoaderProgressBar = null;
        mFinalTextTextView = null;
        mAtTextView = null;
        networkResponse = null;
        cancelListenerActive1 = null;
        if(signUpThread2 != null){
            signUpThread2.interrupt();
            signUpThread2 = null;
        }
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_signupbusinessstage3_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }

    public void signUpAndGetUserCredentials(final String businessName, final String branchLocation, final String gender, final String dob, final String country, final String pottname, final String phoneNumber, final String password, final String referrerPottName, final String language){
                networkResponse = "";
        new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mFinalTextTextView.setVisibility(View.INVISIBLE);
                        mAtTextView.setVisibility(View.INVISIBLE);
                        mPottNameEditTextHolder.setVisibility(View.INVISIBLE);
                        mPhoneNumberEditTextHolder.setVisibility(View.INVISIBLE);
                        mPasswordEditTextHolder.setVisibility(View.INVISIBLE);
                        mRetypedPasswordEditTextHolder.setVisibility(View.INVISIBLE);
                        mReferrerPottNameEditTextHolder.setVisibility(View.INVISIBLE);
                        mContinueButton.setVisibility(View.INVISIBLE);
                        mSigningUpLoaderProgressBar.setVisibility(View.VISIBLE);
                    }
                });

                        AndroidNetworking.post(Config.LINK_SIGNUP_BUSINESS)
                                .addBodyParameter("business_name", businessName)
                                .addBodyParameter("branch_location", branchLocation)
                                .addBodyParameter("gender", gender)
                                .addBodyParameter("dob", dob)
                                .addBodyParameter("country", country)
                                .addBodyParameter("pott_name", pottname)
                                .addBodyParameter("referrer_pott_name", referrerPottName)
                                .addBodyParameter("phone_number", phoneNumber)
                                .addBodyParameter("password", password)
                                .addBodyParameter("language", language)
                                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                .setTag("signup_fragment_signup_personalstage3")
                                .setPriority(Priority.MEDIUM)
                                .build().getAsString(new StringRequestListener() {
                            @Override public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        JSONArray array = jsonObject.getJSONArray("data_returned");
                                        JSONObject o = array.getJSONObject(0);

                                        String myStatus = o.getString("status");
                                        final String myStatusMessage = o.getString("message");

                                        if (myStatus.equalsIgnoreCase("yes")) {
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, phoneNumber);
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID, o.getString("user_id"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD, o.getString("user_pass"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME, o.getString("user_pott_name"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_FULL_NAME, o.getString("user_full_name"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_COUNTRY, o.getString("user_country"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VERIFIED_STATUS, "0");
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_TYPE, o.getString("user_type"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_GENDER, o.getString("user_gender"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DATE_OF_BIRTH, o.getString("user_date_of_birth"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY, o.getString("user_currency"));
                                            Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN, o.getString("8"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE, o.getString("9"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO, o.getString("10"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN_NAME, o.getString("11"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE_NAME, o.getString("12"));
                                            Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO_NAME, o.getString("13"));

                                            if(o.getBoolean("phone_verification_is_on")){
                                                Config.openActivity(getActivity(), ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, phoneNumber);
                                                return;
                                            }

                                            if (Config.checkUpdateAndForwardToUpdateActivity(getActivity(), o.getInt("highest_version_code"), o.getBoolean("force_update_status"), o.getString("update_date"), false)) {
                                                Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("update_date"));
                                                Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("force_update_status"));
                                                Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("highest_version_code"));
                                                return;
                                            }

                                            Config.openActivity(getActivity(), SetProfilePictureActivity.class, 1, 2, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
                                            return;

                                        } else {
                                            if(MyLifecycleHandler.isApplicationInForeground()){
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mFinalTextTextView.setVisibility(View.VISIBLE);
                                                        mAtTextView.setVisibility(View.VISIBLE);
                                                        mPottNameEditTextHolder.setVisibility(View.VISIBLE);
                                                        mPhoneNumberEditTextHolder.setVisibility(View.VISIBLE);
                                                        mPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                                        mRetypedPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                                        mReferrerPottNameEditTextHolder.setVisibility(View.VISIBLE);
                                                        mContinueButton.setVisibility(View.VISIBLE);
                                                        mSigningUpLoaderProgressBar.setVisibility(View.INVISIBLE);
                                                        Config.showDialogType1(getActivity(), getString(R.string.login_activity_login_failed), myStatusMessage, "", null, true, "", "");
                                                        networkResponse = "";
                                                    }
                                                });
                                            } else {
                                                networkResponse = myStatusMessage;
                                            }

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        if(MyLifecycleHandler.isApplicationInForeground()) {
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mFinalTextTextView.setVisibility(View.VISIBLE);
                                                    mAtTextView.setVisibility(View.VISIBLE);
                                                    mPottNameEditTextHolder.setVisibility(View.VISIBLE);
                                                    mPhoneNumberEditTextHolder.setVisibility(View.VISIBLE);
                                                    mPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                                    mRetypedPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                                    mReferrerPottNameEditTextHolder.setVisibility(View.VISIBLE);
                                                    mContinueButton.setVisibility(View.VISIBLE);
                                                    mSigningUpLoaderProgressBar.setVisibility(View.INVISIBLE);
                                                    Config.showDialogType1(getActivity(), getResources().getString(R.string.login_activity_error), getResources().getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                                                    networkResponse = "";
                                                }
                                            });
                                        } else {
                                            networkResponse = getResources().getString(R.string.login_activity_an_unexpected_error_occured);
                                        }
                                    }

                            }

                            @Override
                            public void onError(ANError anError) {
                                if(MyLifecycleHandler.isApplicationInForeground()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //mTitleBarConstraintLayout.setVisibility(View.VISIBLE);
                                            mFinalTextTextView.setVisibility(View.VISIBLE);
                                            mAtTextView.setVisibility(View.VISIBLE);
                                            mPottNameEditTextHolder.setVisibility(View.VISIBLE);
                                            mPhoneNumberEditTextHolder.setVisibility(View.VISIBLE);
                                            mPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                            mRetypedPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                            mReferrerPottNameEditTextHolder.setVisibility(View.VISIBLE);
                                            mContinueButton.setVisibility(View.VISIBLE);
                                            mSigningUpLoaderProgressBar.setVisibility(View.INVISIBLE);
                                            Config.showToastType1(getActivity(), getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                            networkResponse = "";
                                        }
                                    });
                                } else {
                                    networkResponse = getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                                }
                            }
                        });
    }


}
