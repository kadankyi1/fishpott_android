package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfirmPhoneNumberActivity extends AppCompatActivity {

    private TextInputLayout mVerificationCodeEditTextHolderTextInputLayout;
    private EditText mVerificationCodeEditText;
    private Button mVerifyButton, mResentCodeButton;
    private ProgressBar mVerifyingLoader;
    private Thread verifyPhoneNumberThread2 = null, resendResetCodeThread2 = null;
    private String phone = "", networkResponse = "";
    private Boolean CLOSE_APP_COMPLETELY = true;
    private ImageView coverImageView;
    private TextView introTextTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_phone_number);

        if(getIntent().getExtras() != null) {
            phone =(String) getIntent().getExtras().get(Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE);
        } else {
            finish();
        }

        mVerificationCodeEditTextHolderTextInputLayout = findViewById(R.id.activity_confirmphonenumber_verificationcode_edit_text_layout_holder);
        mVerificationCodeEditText = findViewById(R.id.activity_confirmphonenumber_verificationcode_edit_text);
        mVerifyButton = findViewById(R.id.activity_confirmphonenumber_send_reset_code_button);
        mResentCodeButton = findViewById(R.id.activity_confirmphonenumber_resend_reset_code_button);
        mVerifyingLoader = findViewById(R.id.activity_confirmphonenumber_loader);
        coverImageView = findViewById(R.id.activity_confirmphonenumber_coverimage_imageView);
        introTextTextView = findViewById(R.id.activity_confirmphonenumber_intotext_textView);

        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVerificationCodeEditText.getText().toString().trim().length() > 4 && mVerificationCodeEditText.getText().toString().trim().length() < 11){
                    if(Connectivity.isConnected(ConfirmPhoneNumberActivity.this)){
                        verifyPhoneNumberThread2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                verifyPhoneNumber(phone, mVerificationCodeEditText.getText().toString().trim(), LocaleHelper.getLanguage(ConfirmPhoneNumberActivity.this));
                            }
                        });
                        verifyPhoneNumberThread2.start();
                    } else {

                        Config.showToastType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));

                    }

                }
            }
        });

        mResentCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Connectivity.isConnected(ConfirmPhoneNumberActivity.this)){
                    resendResetCodeThread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            resendVerificationCode(phone, LocaleHelper.getLanguage(ConfirmPhoneNumberActivity.this));
                        }
                    });
                    resendResetCodeThread2.start();
                } else {

                    Config.showToastType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED CONFIRMPHONNUMBER-ACTIVITY");
        mVerificationCodeEditTextHolderTextInputLayout = findViewById(R.id.activity_confirmphonenumber_verificationcode_edit_text_layout_holder);
        mVerificationCodeEditText = findViewById(R.id.activity_confirmphonenumber_verificationcode_edit_text);
        mVerifyButton = findViewById(R.id.activity_confirmphonenumber_send_reset_code_button);
        mResentCodeButton = findViewById(R.id.activity_confirmphonenumber_resend_reset_code_button);
        mVerifyingLoader = findViewById(R.id.activity_confirmphonenumber_loader);
        coverImageView = findViewById(R.id.activity_confirmphonenumber_coverimage_imageView);
        introTextTextView = findViewById(R.id.activity_confirmphonenumber_intotext_textView);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            mVerifyingLoader.setVisibility(View.INVISIBLE);
            mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
            mVerificationCodeEditText.setVisibility(View.VISIBLE);
            mVerifyButton.setVisibility(View.VISIBLE);
            mResentCodeButton.setVisibility(View.VISIBLE);
            coverImageView.setVisibility(View.VISIBLE);
            introTextTextView.setVisibility(View.VISIBLE);
            Config.showDialogType1(ConfirmPhoneNumberActivity.this, getString(R.string.login_activity_error), networkResponse, "", null, false, "", "");
            networkResponse = "";
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED CONFIRMPHONNUMBER-ACTIVITY");
        mVerificationCodeEditTextHolderTextInputLayout = null;
        mVerificationCodeEditText = null;
        mVerifyButton = null;
        mResentCodeButton = null;
        mVerifyingLoader = null;
        coverImageView = null;
        introTextTextView = null;
        if(verifyPhoneNumberThread2 != null){
            verifyPhoneNumberThread2.interrupt();
            verifyPhoneNumberThread2 = null;
        }
        if(resendResetCodeThread2 != null){
            resendResetCodeThread2.interrupt();
            resendResetCodeThread2 = null;
        }
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED CONFIRMPHONNUMBER-ACTIVITY");
        if(CLOSE_APP_COMPLETELY){
            ExitActivity.exitApplicationAndRemoveFromRecent(ConfirmPhoneNumberActivity.this);
        } else {
            Config.freeMemory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED CONFIRMPHONNUMBER-ACTIVITY");
        mVerificationCodeEditTextHolderTextInputLayout = null;
        mVerificationCodeEditText = null;
        mVerifyButton = null; mResentCodeButton = null;
        mVerifyingLoader = null;
        phone  = null;
        networkResponse = null;
        CLOSE_APP_COMPLETELY = null;
        coverImageView = null;
        introTextTextView = null;
        if(verifyPhoneNumberThread2 != null){
            verifyPhoneNumberThread2.interrupt();
            verifyPhoneNumberThread2 = null;
        }
        if(resendResetCodeThread2 != null){
            resendResetCodeThread2.interrupt();
            resendResetCodeThread2 = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_confirmphonenumber_activity));
        Config.freeMemory();
    }

    public void verifyPhoneNumber(final String phone,final String verificationCode, final String language){
        networkResponse = "";
        new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.INVISIBLE);
                        mVerificationCodeEditText.setVisibility(View.INVISIBLE);
                        mVerifyButton.setVisibility(View.INVISIBLE);
                        mResentCodeButton.setVisibility(View.INVISIBLE);
                        coverImageView.setVisibility(View.INVISIBLE);
                        introTextTextView.setVisibility(View.INVISIBLE);
                        mVerifyingLoader.setVisibility(View.VISIBLE);
                    }
                });

                        AndroidNetworking.post(Config.VERIFY_PHONE_NUMBER_WITH_CODE)
                                .addBodyParameter("phone", phone)
                                .addBodyParameter("verification_code", verificationCode)
                                .addBodyParameter("language", language)
                                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                .setTag("confirmphonenumber_activity_verify_phone")
                                .setPriority(Priority.MEDIUM)
                                .build().getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        JSONArray array = jsonObject.getJSONArray("data_returned");
                                        JSONObject o = array.getJSONObject(0);
                                        String myStatus = o.getString("status");
                                        final String myStatusMessage = o.getString("message");

                                        // IF THE STATUS IS "YES", THEN WE SEND THE USER TO MAIN ACTIVITY OR PROFILE PICTURE AND CLEAR ALL ACTIVITY HISTORY. IF NOT, WE SHOW THEM THE ERROR MESSAGE
                                        if (myStatus.equalsIgnoreCase("yes")) {
                                            CLOSE_APP_COMPLETELY = false;
                                            Config.setSharedPreferenceBoolean(ConfirmPhoneNumberActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, false);

                                            //REDIRECTING IF USER HAS SET PROFILE PICTURE OR NOT
                                            if(!Config.userProfilePictureIsSet(ConfirmPhoneNumberActivity.this)){
                                                Config.openActivity(ConfirmPhoneNumberActivity.this, SetProfilePictureActivity.class, 1, 2, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
                                                return;
                                            } else {
                                                Config.openActivity(ConfirmPhoneNumberActivity.this, MainActivity.class, 1, 2, 0, "", "");
                                                return;
                                            }

                                        } else if(myStatus.equalsIgnoreCase("0")){
                                            // ACCOUNT HAS BEEN FLAGGED
                                            Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS, 1);
                                            Config.openActivity(ConfirmPhoneNumberActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                                            return;
                                        } else {
                                            if(MyLifecycleHandler.isApplicationInForeground()) {
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Config.showDialogType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_login_failed), myStatusMessage, "", null, false, "", "");
                                                        mVerifyingLoader.setVisibility(View.INVISIBLE);
                                                        mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
                                                        mVerificationCodeEditText.setVisibility(View.VISIBLE);
                                                        mVerifyButton.setVisibility(View.VISIBLE);
                                                        mResentCodeButton.setVisibility(View.VISIBLE);
                                                        coverImageView.setVisibility(View.VISIBLE);
                                                        introTextTextView.setVisibility(View.VISIBLE);
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
                                                    Config.showDialogType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_error), getResources().getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                                                    mVerifyingLoader.setVisibility(View.INVISIBLE);
                                                    mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
                                                    mVerificationCodeEditText.setVisibility(View.VISIBLE);
                                                    mVerifyButton.setVisibility(View.VISIBLE);
                                                    mResentCodeButton.setVisibility(View.VISIBLE);
                                                    coverImageView.setVisibility(View.VISIBLE);
                                                    introTextTextView.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        } else {
                                            networkResponse = getResources().getString(R.string.login_activity_an_unexpected_error_occured);
                                        }

                                    }

                            }
                            @Override
                            public void onError(ANError anError) {
                                if(MyLifecycleHandler.isApplicationInForeground()){
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Config.showToastType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                            mVerifyingLoader.setVisibility(View.INVISIBLE);
                                            mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
                                            mVerificationCodeEditText.setVisibility(View.VISIBLE);
                                            mVerifyButton.setVisibility(View.VISIBLE);
                                            mResentCodeButton.setVisibility(View.VISIBLE);
                                            coverImageView.setVisibility(View.VISIBLE);
                                            introTextTextView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else {
                                    networkResponse = getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                                }

                            }
                        });

    }


    public void resendVerificationCode(final String phone, final String language){
        networkResponse = "";
        new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.INVISIBLE);
                        mVerificationCodeEditText.setVisibility(View.INVISIBLE);
                        mVerifyButton.setVisibility(View.INVISIBLE);
                        mResentCodeButton.setVisibility(View.INVISIBLE);
                        coverImageView.setVisibility(View.INVISIBLE);
                        introTextTextView.setVisibility(View.INVISIBLE);
                        mVerifyingLoader.setVisibility(View.VISIBLE);
                    }
                });

                AndroidNetworking.post(Config.RESEND_PHONE_VERIFICATION_CODE)
                        .addBodyParameter("phone", phone)
                        .addBodyParameter("language", language)
                        .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                        .setTag("confirmphonenumber_activity_resend_code")
                        .setPriority(Priority.MEDIUM)
                        .build().getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray array = jsonObject.getJSONArray("data_returned");
                                JSONObject o = array.getJSONObject(0);
                                String myStatus = o.getString("status");
                                final String myStatusMessage = o.getString("message");

                                // IF THE STATUS IS "YES", THEN IT'S SUCCESSFUL
                                if(myStatus.equalsIgnoreCase("yes")){
                                    Config.showDialogType1(ConfirmPhoneNumberActivity.this, "1", myStatusMessage, "", null, false, "", "");
                                    mVerifyingLoader.setVisibility(View.INVISIBLE);
                                    mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
                                    mVerificationCodeEditText.setVisibility(View.VISIBLE);
                                    mVerifyButton.setVisibility(View.VISIBLE);
                                    mResentCodeButton.setVisibility(View.VISIBLE);
                                    coverImageView.setVisibility(View.VISIBLE);
                                    introTextTextView.setVisibility(View.VISIBLE);
                                } else if(myStatus.equalsIgnoreCase("0")){
                                    // ACCOUNT HAS BEEN FLAGGED
                                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS, 1);
                                    Config.openActivity(ConfirmPhoneNumberActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                                    return;
                                } else {
                                    if(MyLifecycleHandler.isApplicationInForeground()) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Config.showDialogType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_login_failed), myStatusMessage, "", null, false, "", "");
                                                mVerifyingLoader.setVisibility(View.INVISIBLE);
                                                mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
                                                mVerificationCodeEditText.setVisibility(View.VISIBLE);
                                                mVerifyButton.setVisibility(View.VISIBLE);
                                                mResentCodeButton.setVisibility(View.VISIBLE);
                                                coverImageView.setVisibility(View.VISIBLE);
                                                introTextTextView.setVisibility(View.VISIBLE);
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
                                            Config.showDialogType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_error), getResources().getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                                            mVerifyingLoader.setVisibility(View.INVISIBLE);
                                            mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
                                            mVerificationCodeEditText.setVisibility(View.VISIBLE);
                                            mVerifyButton.setVisibility(View.VISIBLE);
                                            mResentCodeButton.setVisibility(View.VISIBLE);
                                            coverImageView.setVisibility(View.VISIBLE);
                                            introTextTextView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else {
                                    networkResponse = getResources().getString(R.string.login_activity_an_unexpected_error_occured);
                                }

                            }

                    }
                    @Override
                    public void onError(ANError anError) {
                        if(MyLifecycleHandler.isApplicationInForeground()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(ConfirmPhoneNumberActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                    mVerifyingLoader.setVisibility(View.INVISIBLE);
                                    mVerificationCodeEditTextHolderTextInputLayout.setVisibility(View.VISIBLE);
                                    mVerificationCodeEditText.setVisibility(View.VISIBLE);
                                    mVerifyButton.setVisibility(View.VISIBLE);
                                    mResentCodeButton.setVisibility(View.VISIBLE);
                                    coverImageView.setVisibility(View.VISIBLE);
                                    introTextTextView.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            networkResponse = getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                        }

                    }
                });
    }
}
