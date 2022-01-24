package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mPhoneEditText, mPasswordEditText;
    private Button mLoginButton, mCreateAccount;
    private TextView mPasswordShowHideText, mForgottenPasswordText;
    private ImageView mCoverImage;
    private ProgressBar mLoginLoader;
    private TextInputLayout mPhoneEditTextHolder, mPasswordEditTextHolder;
    private Thread loginThread2 = null;
    private Dialog.OnCancelListener cancelListenerActive1;
    private String activityFinished = "no", loginResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPhoneEditText = findViewById(R.id.activity_login_phone_edit_text);
        mPasswordEditText = findViewById(R.id.activity_login_password);
        mLoginButton = findViewById(R.id.activity_login_login_button);
        mCreateAccount = findViewById(R.id.activity_login_create_account_button);
        mPasswordShowHideText = findViewById(R.id.activity_login_password_show_hide_text);
        mForgottenPasswordText = findViewById(R.id.activity_login_forgot_password_text);
        mCoverImage = findViewById(R.id.activity_login_cover_image);
        mLoginLoader = findViewById(R.id.activity_login_loader);
        mPhoneEditTextHolder = findViewById(R.id.activity_login_phone_edit_text_layout_holder);
        mPasswordEditTextHolder = findViewById(R.id.activity_login_password_layout_holder);

        if(getIntent().getExtras() !=null) {activityFinished =(String) getIntent().getExtras().get(Config.KEY_ACTIVITY_FINISHED);} else {activityFinished = "no";}

        mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(mPasswordEditText.getText().toString().length() > 1){
                    mPasswordShowHideText.setVisibility(View.VISIBLE);
                } else {
                    mPasswordShowHideText.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                loginThread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loginAndGetUserCredentials(mPhoneEditText.getText().toString().trim(), mPasswordEditText.getText().toString().trim(), LocaleHelper.getLanguage(getApplicationContext()));
                    }
                });
                loginThread2.start();
            }
        };

        mPasswordShowHideText.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mCreateAccount.setOnClickListener(this);
        mForgottenPasswordText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.activity_login_password_show_hide_text){
            if(mPasswordShowHideText.getText().toString().equalsIgnoreCase(getResources().getString(R.string.login_activity_password_show))){
                mPasswordShowHideText.setText(getResources().getString(R.string.login_activity_password_hide));
                mPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                mPasswordEditText.setSelection(mPasswordEditText.length());
            } else {
                mPasswordShowHideText.setText(getResources().getString(R.string.login_activity_password_show));
                mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mPasswordEditText.setSelection(mPasswordEditText.length());
            }
        } else if(view.getId() == R.id.activity_login_login_button){
            if(!mPhoneEditText.getText().toString().trim().equalsIgnoreCase("") && !mPasswordEditText.getText().toString().trim().equalsIgnoreCase("")){
                if(Connectivity.isConnected(getApplicationContext())){
                    cancelListenerActive1 = Config.showDialogType2(LoginActivity.this, cancelListenerActive1, true);
                } else {
                    Config.showToastType1(LoginActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                }
            }
        } else if(view.getId() == R.id.activity_login_create_account_button){
            Config.openActivity(LoginActivity.this, SignUpActivity.class, 1, 0, 0, "", "");
            return;

        } else if(view.getId() == R.id.activity_login_forgot_password_text){
            Config.openActivity(LoginActivity.this, ForgotPasswordActivity.class, 1, 0, 0, "", "");
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPhoneEditText = findViewById(R.id.activity_login_phone_edit_text);
        mPasswordEditText = findViewById(R.id.activity_login_password);
        mLoginButton = findViewById(R.id.activity_login_login_button);
        mCreateAccount = findViewById(R.id.activity_login_create_account_button);
        mPasswordShowHideText = findViewById(R.id.activity_login_password_show_hide_text);
        mForgottenPasswordText = findViewById(R.id.activity_login_forgot_password_text);
        mCoverImage = findViewById(R.id.activity_login_cover_image);
        mLoginLoader = findViewById(R.id.activity_login_loader);
        mPhoneEditTextHolder = findViewById(R.id.activity_login_phone_edit_text_layout_holder);
        mPasswordEditTextHolder = findViewById(R.id.activity_login_password_layout_holder);
        if(getIntent().getExtras() !=null) {
            activityFinished = (String) getIntent().getExtras().get(Config.KEY_ACTIVITY_FINISHED);
        } else {
            activityFinished = "no";
        }

        if(!loginResponse.trim().equalsIgnoreCase("")){
            Config.showDialogType1(LoginActivity.this, getString(R.string.login_activity_error), loginResponse, "", null, false, "", "");
            mLoginLoader.setVisibility(View.INVISIBLE);
            mCoverImage.setVisibility(View.VISIBLE);
            mPhoneEditText.setVisibility(View.VISIBLE);
            mPasswordEditText.setVisibility(View.VISIBLE);
            mPhoneEditTextHolder.setVisibility(View.VISIBLE);
            mPasswordShowHideText.setVisibility(View.VISIBLE);
            mPasswordEditTextHolder.setVisibility(View.VISIBLE);
            mLoginButton.setVisibility(View.VISIBLE);
            mForgottenPasswordText.setVisibility(View.VISIBLE);
            mCreateAccount.setVisibility(View.VISIBLE);
            loginResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED LOGIN-ACTIVITY");
        mPhoneEditText = null;
        mPasswordEditText = null;
        mLoginButton = null;
        mCreateAccount = null;
        mPasswordShowHideText = null;
        mForgottenPasswordText = null;
        mCoverImage = null;
        mLoginLoader = null;
        mPhoneEditTextHolder = null;
        mPasswordEditTextHolder = null;
        if(loginThread2 != null){
            loginThread2.interrupt();
            loginThread2 = null;
        }
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED LOGIN-ACTIVITY");

        if (activityFinished.trim().equalsIgnoreCase("no")) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "finish STARTED LOGIN-ACTIVITY");
        cancelListenerActive1 = null;
        activityFinished = null;
        loginResponse = null;
        if(loginThread2 != null){
            loginThread2.interrupt();
            loginThread2 = null;
        }
        Config.unbindDrawables(findViewById(R.id.login_activity_root_view_constraintlayout));
        Config.freeMemory();
    }


    public void loginAndGetUserCredentials(final String phone, final String password, final String language){

                loginResponse = "";

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // UI ACTIONS COME HERE
                        mCoverImage.setVisibility(View.INVISIBLE);
                        mPhoneEditText.setVisibility(View.INVISIBLE);
                        mPasswordEditText.setVisibility(View.INVISIBLE);
                        mPhoneEditTextHolder.setVisibility(View.INVISIBLE);
                        mPasswordShowHideText.setVisibility(View.INVISIBLE);
                        mPasswordEditTextHolder.setVisibility(View.INVISIBLE);
                        mLoginButton.setVisibility(View.INVISIBLE);
                        mForgottenPasswordText.setVisibility(View.INVISIBLE);
                        mCreateAccount.setVisibility(View.INVISIBLE);
                        mLoginLoader.setVisibility(View.VISIBLE);
                    }
                });

                AndroidNetworking.post(Config.LINK_LOGIN)
                        .addBodyParameter("user_phone_number", phone)
                        .addBodyParameter("password", password)
                        .addBodyParameter("user_language", language)
                        .addBodyParameter("app_type", "ANDROID")
                        .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(getApplicationContext())))
                        .setTag("login_activity_login")
                        .setPriority(Priority.MEDIUM)
                        .build().getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        if(MyLifecycleHandler.isApplicationVisible()){
                            try {
                                Log.e("PSignup", response);
                                JSONObject o = new JSONObject(response);
                                String myStatus = o.getString("status");
                                final String myStatusMessage = o.getString("message");

                                if (myStatus.equalsIgnoreCase("yes")) {

                                    //STORING THE USER DATA
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, o.getString("user_phone"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID, o.getString("user_id"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN, o.getString("access_token"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME, o.getString("user_pott_name"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_FULL_NAME, o.getString("user_full_name"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PROFILE_PICTURE, o.getString("user_profile_picture"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_COUNTRY, o.getString("user_country"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VERIFIED_STATUS, String.valueOf(o.getInt("user_verified_status")));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_TYPE, o.getString("user_type"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_GENDER, o.getString("user_gender"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DATE_OF_BIRTH, o.getString("user_date_of_birth"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY, o.getString("user_currency"));
                                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN, o.getString("mtn_momo_number"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE, o.getString("vodafone_momo_number"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO, o.getString("airteltigo_momo_number"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN_NAME, o.getString("mtn_momo_acc_name"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE_NAME, o.getString("vodafone_momo_acc_name"));
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO_NAME, o.getString("airteltigo_momo_acc_name"));
                                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));
                                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_MEDIA_POSTING_ALLOWED, o.getInt("media_allowed"));

                                    if(o.getBoolean("phone_verification_is_on")){
                                        Config.openActivity(LoginActivity.this, ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, phone);
                                        return;
                                    }

                                    if(o.getBoolean("id_verification_is_on")){
                                        Config.openActivity(LoginActivity.this, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
                                        return;
                                    }

                                    Config.checkUpdateAndForwardToUpdateActivity(LoginActivity.this, o.getInt("user_android_app_max_vc"), o.getBoolean("user_android_app_force_update"));

                                    Config.openActivity(LoginActivity.this, MainActivity.class, 1, 2, 0, "", "");
                                    return;

                                    /*
                                    if (Config.userProfilePictureIsSet(LoginActivity.this)) {
                                        Config.openActivity(LoginActivity.this, MainActivity.class, 1, 2, 0, "", "");
                                    } else {
                                        Config.openActivity(LoginActivity.this, SetProfilePictureActivity.class, 1, 2, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
                                    }
                                    */

                                } else if(myStatus.equalsIgnoreCase("0")){
                                    Config.openActivity(LoginActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                                    return;
                                } else if(myStatus.equalsIgnoreCase("9")){
                                    Config.openActivity(LoginActivity.this, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
                                    return;
                                } else {
                                    if(MyLifecycleHandler.isApplicationInForeground()){
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Config.showDialogType1(LoginActivity.this, getString(R.string.login_activity_login_failed), myStatusMessage, "", null, true, "", "");
                                                mLoginLoader.setVisibility(View.INVISIBLE);
                                                mCoverImage.setVisibility(View.VISIBLE);
                                                mPhoneEditText.setVisibility(View.VISIBLE);
                                                mPasswordEditText.setVisibility(View.VISIBLE);
                                                mPhoneEditTextHolder.setVisibility(View.VISIBLE);
                                                mPasswordShowHideText.setVisibility(View.VISIBLE);
                                                mPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                                mLoginButton.setVisibility(View.VISIBLE);
                                                mForgottenPasswordText.setVisibility(View.VISIBLE);
                                                mCreateAccount.setVisibility(View.VISIBLE);

                                            }
                                        });
                                    } else {
                                        loginResponse = myStatusMessage;
                                    }

                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                                if(MyLifecycleHandler.isApplicationInForeground()){
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Config.showDialogType1(LoginActivity.this, getString(R.string.login_activity_error), getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                                            mLoginLoader.setVisibility(View.INVISIBLE);
                                            mCoverImage.setVisibility(View.VISIBLE);
                                            mPhoneEditText.setVisibility(View.VISIBLE);
                                            mPasswordEditText.setVisibility(View.VISIBLE);
                                            mPhoneEditTextHolder.setVisibility(View.VISIBLE);
                                            mPasswordShowHideText.setVisibility(View.VISIBLE);
                                            mPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                            mLoginButton.setVisibility(View.VISIBLE);
                                            mForgottenPasswordText.setVisibility(View.VISIBLE);
                                            mCreateAccount.setVisibility(View.VISIBLE);

                                        }
                                    });
                                } else {
                                    loginResponse = getString(R.string.login_activity_an_unexpected_error_occured);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (MyLifecycleHandler.isApplicationInForeground()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(LoginActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                    mLoginLoader.setVisibility(View.INVISIBLE);
                                    mCoverImage.setVisibility(View.VISIBLE);
                                    mPhoneEditText.setVisibility(View.VISIBLE);
                                    mPasswordEditText.setVisibility(View.VISIBLE);
                                    mPhoneEditTextHolder.setVisibility(View.VISIBLE);
                                    mPasswordShowHideText.setVisibility(View.VISIBLE);
                                    mPasswordEditTextHolder.setVisibility(View.VISIBLE);
                                    mLoginButton.setVisibility(View.VISIBLE);
                                    mForgottenPasswordText.setVisibility(View.VISIBLE);
                                    mCreateAccount.setVisibility(View.VISIBLE);

                                }
                            });
                        } else {
                            loginResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                        }
                    }
                });
    }

}
