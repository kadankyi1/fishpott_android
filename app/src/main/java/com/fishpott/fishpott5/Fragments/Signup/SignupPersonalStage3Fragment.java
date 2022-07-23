package com.fishpott.fishpott5.Fragments.Signup;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.fishpott.fishpott5.Activities.MainActivity;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
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
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupPersonalStage3Fragment extends Fragment implements View.OnClickListener {

    private String firstName = "", lastName = "", gender = "", dob = "", country = "", countryCode = "", networkResponse = "", emailAddress = "";
    private ImageView mBackImageView;
    private EditText mPottNameEditText, mPhoneNumberEditText, mPasswordEditText, mRetypedPasswordEditText, mReferrerPottNameEditText;
    private TextInputLayout mPottNameEditTextHolder, mPhoneNumberEditTextHolder, mPasswordEditTextHolder, mRetypedPasswordEditTextHolder, mReferrerPottNameEditTextHolder;
    private Button mContinueButton;
    private ProgressBar mSigningUpLoaderProgressBar;
    private TextView mFinalTextTextView, mAtTextView;
    private Thread signUpThread2 = null;
    private Dialog.OnCancelListener cancelListenerActive1;
    private View view = null;

    public static SignupPersonalStage3Fragment newInstance(String firstName, String lastName, String gender, String dob, String emailAddress, String country, String countryCode) {
        SignupPersonalStage3Fragment fragment = new SignupPersonalStage3Fragment();
        Bundle args = new Bundle();
        args.putString("firstName", firstName);
        args.putString("lastName", lastName);
        args.putString("gender", gender);
        args.putString("dob", dob);
        args.putString("emailAddress", emailAddress);
        args.putString("country", country);
        args.putString("countryCode", countryCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            firstName = getArguments().getString("firstName");
            lastName = getArguments().getString("lastName");
            gender = getArguments().getString("gender");
            dob = getArguments().getString("dob");
            emailAddress = getArguments().getString("emailAddress");
            country = getArguments().getString("country");
            countryCode = getArguments().getString("countryCode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup_personal_stage3, container, false);

        mBackImageView = view.findViewById(R.id.fragment_signup_personalstage3_back_imageview);
        mFinalTextTextView = view.findViewById(R.id.fragment_signup_personalstage3_final_step_textView);
        mAtTextView = view.findViewById(R.id.fragment_signup_personalstage3_at_textView);
        mPottNameEditText = view.findViewById(R.id.fragment_signup_personalstage3_pottname_edit_text);
        mPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_pottname_edit_text_layout_holder);
        mPhoneNumberEditText = view.findViewById(R.id.fragment_signup_personalstage3_phonenumber_edit_text);
        mPhoneNumberEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_phonenumber_edit_text_layout_holder);
        mPasswordEditText = view.findViewById(R.id.fragment_signup_personalstage3_password_edit_text);
        mPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_password_edit_text_layout_holder);
        mRetypedPasswordEditText = view.findViewById(R.id.fragment_signup_personalstage3_retypepassword_edit_text);
        mRetypedPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_retypepassword_edit_text_layout_holder);
        mReferrerPottNameEditText = view.findViewById(R.id.fragment_signup_personalstage3_referrerpottname_edit_text);
        mReferrerPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_referrerpottname_edit_text_layout_holder);
        mContinueButton = view.findViewById(R.id.fragment_signup_personalstage3_finish_button);
        mSigningUpLoaderProgressBar = view.findViewById(R.id.fragment_signup_personalstage3_signup_loader);

        mContinueButton.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(Connectivity.isConnected(getActivity())){
                    signUpThread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            signUpAndGetUserCredentials(firstName, lastName, gender, dob, emailAddress, country, mPottNameEditText.getText().toString().trim(), mPhoneNumberEditText.getText().toString().trim(), mPasswordEditText.getText().toString().trim(), mReferrerPottNameEditText.getText().toString().trim(), LocaleHelper.getLanguage(getActivity()));
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
        if(view.getId() == R.id.fragment_signup_personalstage3_back_imageview){
            getActivity().onBackPressed();
        } else if(view.getId() == R.id.fragment_signup_personalstage3_finish_button){
            if(!mPottNameEditText.getText().toString().trim().equalsIgnoreCase("") && !mPhoneNumberEditText.getText().toString().trim().equalsIgnoreCase("") &&
                    !mPasswordEditText.getText().toString().trim().equalsIgnoreCase("") && !mRetypedPasswordEditText.getText().toString().trim().equalsIgnoreCase("") &&
                    !firstName.trim().equalsIgnoreCase("") && !lastName.trim().equalsIgnoreCase("") && !gender.trim().equalsIgnoreCase("") &&
                    !dob.trim().equalsIgnoreCase("") && !country.trim().equalsIgnoreCase("") && !countryCode.trim().equalsIgnoreCase("") &&
                    mPottNameEditText.getText().toString().trim().length() > 4 && !mPottNameEditText.getText().toString().trim().equalsIgnoreCase("linkups") &&
                    mPhoneNumberEditText.getText().toString().trim().substring(0, countryCode.length()).equalsIgnoreCase(countryCode) &&
                    mPhoneNumberEditText.getText().toString().trim().length() > 10 &&
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
            } else if(!mPhoneNumberEditText.getText().toString().trim().substring(0, countryCode.length()).equalsIgnoreCase(countryCode) || mPhoneNumberEditText.getText().toString().trim().length() < 11){
                Config.showDialogType1(getActivity(), "1", getString(R.string.fragment_signup_personalstage3_type_a_correct_phone_number_and_it_must_contain_your_country_code_like_this) + " " + countryCode + "207393447", "", null, false, "", "");
            } else if(!mPasswordEditText.getText().toString().trim().equals(mRetypedPasswordEditText.getText().toString().trim())){
                Config.showToastType1(getActivity(), getActivity().getResources().getString(R.string.fragment_signup_personalstage3_passwords_do_not_match));
            } else if(firstName.trim().equalsIgnoreCase("") || lastName.trim().equalsIgnoreCase("") || gender.trim().equalsIgnoreCase("")){
                Config.showDialogType1(getActivity(), "1", getString(R.string.fragment_signup_personalstage3_something_went_wrong_please_return_to_the_first_stage_to_fill_your_first_name_last_name_and_gender),"", null, true, "", "");
            } else if(dob.trim().equalsIgnoreCase("") || country.trim().equalsIgnoreCase("") || countryCode.trim().equalsIgnoreCase("")){
                Config.showDialogType1(getActivity(), "1", getString(R.string.fragment_signup_personalstage3_something_went_wrong_please_return_to_the_first_stage_to_fill_your_first_name_last_name_and_gender),"", null, true, "", "");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUP-PERSONAL-STAGE_FINAL-FRAGMENT");
        mBackImageView = view.findViewById(R.id.fragment_signup_personalstage3_back_imageview);
        mFinalTextTextView = view.findViewById(R.id.fragment_signup_personalstage3_final_step_textView);
        mAtTextView = view.findViewById(R.id.fragment_signup_personalstage3_at_textView);
        mPottNameEditText = view.findViewById(R.id.fragment_signup_personalstage3_pottname_edit_text);
        mPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_pottname_edit_text_layout_holder);
        mPhoneNumberEditText = view.findViewById(R.id.fragment_signup_personalstage3_phonenumber_edit_text);
        mPhoneNumberEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_phonenumber_edit_text_layout_holder);
        mPasswordEditText = view.findViewById(R.id.fragment_signup_personalstage3_password_edit_text);
        mPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_password_edit_text_layout_holder);
        mRetypedPasswordEditText = view.findViewById(R.id.fragment_signup_personalstage3_retypepassword_edit_text);
        mRetypedPasswordEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_retypepassword_edit_text_layout_holder);
        mReferrerPottNameEditText = view.findViewById(R.id.fragment_signup_personalstage3_referrerpottname_edit_text);
        mReferrerPottNameEditTextHolder = view.findViewById(R.id.fragment_signup_personalstage3_referrerpottname_edit_text_layout_holder);
        mContinueButton = view.findViewById(R.id.fragment_signup_personalstage3_finish_button);
        mSigningUpLoaderProgressBar = view.findViewById(R.id.fragment_signup_personalstage3_signup_loader);
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
        Log.e("memoryManage", "onStop STARTED SIGNUP-PERSONAL-STAGE_FINAL-FRAGMENT");
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
        Log.e("memoryManage", "onDestroy STARTED SIGNUP-PERSONAL-STAGE_FINAL-FRAGMENT");
        firstName = null;
        lastName = null;
        gender = null;
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
        cancelListenerActive1 = null;
        networkResponse = null;
        if(signUpThread2 != null){
            signUpThread2.interrupt();
            signUpThread2 = null;
        }
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_signuppersonalstage3_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }

    public void signUpAndGetUserCredentials(final String firstName, final String lastName, final String gender, final String dob, final String emailAddress, final String country, final String pottname, final String phoneNumber, final String password, final String referrerPottName, final String language){
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


        Log.e("user_firstname", firstName);
        Log.e("user_surname", lastName);
        Log.e("user_gender", gender);
        Log.e("user_dob", dob);
        Log.e("user_email", emailAddress);
        Log.e("user_country", country);
        Log.e("user_pottname", pottname);
        Log.e("user_referred_by", referrerPottName);
        Log.e("user_phone_number", phoneNumber);
        Log.e("password", password);
        Log.e("user_language", language);
        Log.e("user_firstname", firstName);
        Log.e("user_firstname", firstName);
        Log.e("user_firstname", firstName);
        Log.e("app_version_code", String.valueOf(Config.getAppVersionCode(getActivity().getApplicationContext())));



        /*
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.LINK_SIGNUP_PERSONAL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("SignupActivity", "response: " +  response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SignupActivity", "error: " + error.toString());

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Config.showToastType1(getActivity(), "Check your internet connection and try again");
                        } else if (error instanceof AuthFailureError) {
                            Config.showToastType1(getActivity(), "We failed to recognize your account. Please re-sign-in and try again");
                        } else if (error instanceof ServerError) {
                            Config.showToastType1(getActivity(), "Registration failed. Account may already exist or incorrect form information. Try again later");
                        } else if (error instanceof NetworkError) {
                            Config.showToastType1(getActivity(), "Check your internet connection and try again");
                        } else if (error instanceof ParseError) {
                            Config.showToastType1(getActivity(), "An unexpected error occurred.");
                        }
                    }
                }) {


                //@Override
                //public Map<String, String> getHeaders() throws AuthFailureError {
                    //Map<String, String> headers = new HashMap<>();
                    //headers.put("Accept", "application/json");
                    //headers.put("ContentType","multipart/form-data");
                    //headers.put("ContentType", "application/json");
                    //return headers;
                //}


            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_firstname", firstName);
                map.put("user_surname", lastName);
                map.put("user_gender", gender);
                map.put("user_dob", dob);
                map.put("user_country", country);
                map.put("user_pottname", pottname);
                map.put("user_referred_by", referrerPottName);
                map.put("user_phone_number", phoneNumber);
                map.put("password", password);
                map.put("app_type", "ANDROID");
                map.put("user_language", language);
                map.put("app_version_code", String.valueOf(Config.getAppVersionCode(getActivity().getApplicationContext())));

                Log.e("SignupActivity", "Map: " +  map.toString());
                return map;
            }
        };
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(stringRequest);
        */


        AndroidNetworking.post(Config.LINK_SIGNUP_PERSONAL)
                //.addHeaders("Accept", "application/json")
                .addBodyParameter("user_firstname", firstName)
                .addBodyParameter("user_surname", lastName)
                .addBodyParameter("user_gender", gender)
                .addBodyParameter("user_dob", dob)
                .addBodyParameter("user_email", emailAddress)
                .addBodyParameter("user_country", country)
                .addBodyParameter("user_pottname", pottname)
                .addBodyParameter("user_referred_by", referrerPottName)
                .addBodyParameter("user_phone_number", phoneNumber)
                .addBodyParameter("password", password)
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("user_language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(getActivity().getApplicationContext())))
                .setTag("signup_fragment_signup_personalstage3")
                .setPriority(Priority.MEDIUM)
                .build().getAsString(new StringRequestListener() {
            @Override public void onResponse(String response) {
                Log.e("PSignup", response);
                try {
                    JSONObject o = new JSONObject(response);
                    String myStatus = o.getString("status");
                    final String myStatusMessage = o.getString("message");

                    if (myStatus.equalsIgnoreCase("yes")) {

                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, o.getString("user_phone"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_EMAIL, o.getString("user_email"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID, o.getString("user_id"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN, o.getString("access_token"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME, o.getString("user_pott_name"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_FULL_NAME, o.getString("user_full_name"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_COUNTRY, o.getString("user_country"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VERIFIED_STATUS, String.valueOf(o.getInt("user_verified_status")));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_TYPE, o.getString("user_type"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_GENDER, o.getString("user_gender"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DATE_OF_BIRTH, o.getString("user_date_of_birth"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY, o.getString("user_currency"));
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN, o.getString("mtn_momo_number"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE, o.getString("vodafone_momo_number"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO, o.getString("airteltigo_momo_number"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN_NAME, o.getString("mtn_momo_acc_name"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE_NAME, o.getString("vodafone_momo_acc_name"));
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO_NAME, o.getString("airteltigo_momo_acc_name"));
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_MEDIA_POSTING_ALLOWED, o.getInt("media_allowed"));

                        if(o.getBoolean("phone_verification_is_on")){
                            Config.openActivity(getActivity(), ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, phoneNumber);
                            return;
                        }

                        if(Config.checkUpdateAndForwardToUpdateActivity(getActivity(), o.getInt("user_android_app_max_vc"), o.getBoolean("user_android_app_force_update"))){
                            return;
                        }

                        Config.openActivity(getActivity(), MainActivity.class, 1, 2, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
                        //Config.openActivity(getActivity(), SetProfilePictureActivity.class, 1, 2, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
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
                                    Config.showDialogType1(getActivity(), getString(R.string.signup_failed), myStatusMessage, "", null, true, "", "");

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
                                Config.showDialogType1(getActivity(), getString(R.string.signup_failed), getResources().getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                            }
                        });
                    } else {
                        networkResponse = getResources().getString(R.string.login_activity_an_unexpected_error_occured);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                Log.e("PSignup", anError.getErrorBody());

                if (MyLifecycleHandler.isApplicationInForeground()) {
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
                            Config.showToastType1(getActivity(), getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));

                        }
                    });
                } else {
                    networkResponse = getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                }

            }
        });


    }
}
