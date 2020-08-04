package com.fishpott.fishpott5.Activities;

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

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mPhoneNumberEditText;
    private Button mSendResetCodeButton;
    private TextInputLayout mPhoneInputLayoutHolder;
    private ImageView mBackArrowImageView, coverImageView;
    private ProgressBar mGettingCodeLoader;
    private Thread sendResetCodeThread2 = null;
    private TextView introTextTextView;
    private String networkResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mPhoneNumberEditText = findViewById(R.id.activity_forgotpassword_phone_edit_text);
        mSendResetCodeButton = findViewById(R.id.activity_forgotpassword_send_reset_code);
        mPhoneInputLayoutHolder = findViewById(R.id.activity_forgotpassword_phone_edit_text_layout_holder);
        mBackArrowImageView = findViewById(R.id.activity_forgotpassword_back_imageview);
        mGettingCodeLoader = findViewById(R.id.activity_forgotpassword_loader);
        coverImageView = findViewById(R.id.activity_forgotpassword_coverimage_imageView);
        introTextTextView = findViewById(R.id.activity_forgotpassword_intotext_textView);

        mSendResetCodeButton.setOnClickListener(this);
        mBackArrowImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.activity_forgotpassword_send_reset_code){
            if(!mPhoneNumberEditText.getText().toString().trim().equalsIgnoreCase("")){
                if(Connectivity.isConnected(getApplicationContext())){
                    sendResetCodeThread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getResetCode(mPhoneNumberEditText.getText().toString().trim(), LocaleHelper.getLanguage(getApplicationContext()));
                        }
                    });
                    sendResetCodeThread2.start();
                } else {

                    Config.showToastType1(ForgotPasswordActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));

                }

            }
        } else if(view.getId() == R.id.activity_forgotpassword_back_imageview){
            onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED FORGOTPASSWORD-ACTIVITY");
        mPhoneNumberEditText = findViewById(R.id.activity_forgotpassword_phone_edit_text);
        mSendResetCodeButton = findViewById(R.id.activity_forgotpassword_send_reset_code);
        mPhoneInputLayoutHolder = findViewById(R.id.activity_forgotpassword_phone_edit_text_layout_holder);
        mBackArrowImageView = findViewById(R.id.activity_forgotpassword_back_imageview);
        mGettingCodeLoader = findViewById(R.id.activity_forgotpassword_loader);
        coverImageView = findViewById(R.id.activity_forgotpassword_coverimage_imageView);
        introTextTextView = findViewById(R.id.activity_forgotpassword_intotext_textView);

        if(!networkResult.trim().equalsIgnoreCase("")){
            Config.showDialogType1(ForgotPasswordActivity.this, getString(R.string.login_activity_error), networkResult, "", null, false, "", "");
            mGettingCodeLoader.setVisibility(View.INVISIBLE);
            mSendResetCodeButton.setVisibility(View.VISIBLE);
            mPhoneInputLayoutHolder.setVisibility(View.VISIBLE);
            mPhoneNumberEditText.setVisibility(View.VISIBLE);
            introTextTextView.setVisibility(View.VISIBLE);
            coverImageView.setVisibility(View.VISIBLE);
            networkResult = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED FORGOTPASSWORD-ACTIVITY");
        mPhoneNumberEditText = null;
        mSendResetCodeButton = null;
        mPhoneInputLayoutHolder = null;
        mBackArrowImageView = null;
        coverImageView = null;
        mGettingCodeLoader = null;
        introTextTextView = null;
        if(sendResetCodeThread2 != null){
            sendResetCodeThread2.interrupt();
            sendResetCodeThread2 = null;
        }

        Config.freeMemory();
    }


    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED FORGOTPASSWORD-ACTIVITY");
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED FORGOTPASSWORD-ACTIVITY");
        if(sendResetCodeThread2 != null){
            sendResetCodeThread2.interrupt();
            sendResetCodeThread2 = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_forgotpasword_activity));
        Config.freeMemory();
    }

    public void getResetCode(final String phone, final String language){
        networkResult = "";
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mPhoneNumberEditText.setVisibility(View.INVISIBLE);
                mSendResetCodeButton.setVisibility(View.INVISIBLE);
                mPhoneInputLayoutHolder.setVisibility(View.INVISIBLE);
                introTextTextView.setVisibility(View.INVISIBLE);
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                    coverImageView.setVisibility(View.INVISIBLE);
                }
                mGettingCodeLoader.setVisibility(View.VISIBLE);
            }
        });

        Log.e("resetPass", "FORGOT phone: " + phone);


        AndroidNetworking.post(Config.LINK_GET_RESET_CODE)
                .addBodyParameter("phone", phone)
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("forgotpassword_activity_sendresetcode")
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
                        if (myStatus.equalsIgnoreCase("yes")) {
                            Config.openActivity(ForgotPasswordActivity.this, ChangePasswordWithResetCodeActivity.class, 1, 1, 1, "RESET_PASSWORD_PHONE_NUMBER", phone);
                        } else if(myStatus.equalsIgnoreCase("0")){
                            Config.openActivity(ForgotPasswordActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                            return;
                        } else {
                            if(MyLifecycleHandler.isApplicationInForeground()){
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Config.showDialogType1(ForgotPasswordActivity.this, getResources().getString(R.string.login_activity_login_failed), myStatusMessage, "", null, true, "", "");
                                        mGettingCodeLoader.setVisibility(View.INVISIBLE);
                                        mSendResetCodeButton.setVisibility(View.VISIBLE);
                                        mPhoneInputLayoutHolder.setVisibility(View.VISIBLE);
                                        mPhoneNumberEditText.setVisibility(View.VISIBLE);
                                        introTextTextView.setVisibility(View.VISIBLE);
                                        coverImageView.setVisibility(View.VISIBLE);
                                        networkResult = "";
                                    }
                                });
                            } else {
                                networkResult = myStatusMessage;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(MyLifecycleHandler.isApplicationInForeground()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showDialogType1(ForgotPasswordActivity.this, getResources().getString(R.string.login_activity_error), getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                                    mGettingCodeLoader.setVisibility(View.INVISIBLE);
                                    mPhoneInputLayoutHolder.setVisibility(View.VISIBLE);
                                    mSendResetCodeButton.setVisibility(View.VISIBLE);
                                    mPhoneNumberEditText.setVisibility(View.VISIBLE);
                                    introTextTextView.setVisibility(View.VISIBLE);
                                    coverImageView.setVisibility(View.VISIBLE);
                                    networkResult = "";
                                }
                            });
                        } else {
                            networkResult = getResources().getString(R.string.login_activity_an_unexpected_error_occured);

                        }
                    }
            }
            @Override
            public void onError(ANError anError) {
                if(MyLifecycleHandler.isApplicationInForeground()){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Config.showToastType1(ForgotPasswordActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                            mGettingCodeLoader.setVisibility(View.INVISIBLE);
                            mSendResetCodeButton.setVisibility(View.VISIBLE);
                            mPhoneInputLayoutHolder.setVisibility(View.VISIBLE);
                            mPhoneNumberEditText.setVisibility(View.VISIBLE);
                            introTextTextView.setVisibility(View.VISIBLE);
                            coverImageView.setVisibility(View.VISIBLE);
                            networkResult = "";
                        }
                    });
                } else {
                    networkResult = getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                }
            }
        });

    }
}
