package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
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
import com.fishpott.fishpott5.ViewModels.ChangePasswordWithResetCodeViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordWithResetCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mResetCodeEditText, mPasswordEditText, mRepeatPasswordEditText;
    private TextInputLayout mResetCodeInputLayoutHolder, mPasswordInputLayoutHolder, mRepeatPasswordInputLayoutHolder;
    private Button mResetPasswordButton;
    private ImageView mBackArrowImageView, coverImageView;
    private ProgressBar mResettingPasswordButton;
    private Thread sendResetPasswordThread2 = null;
    private String phone = "", networkResponse = "";
    private Dialog.OnCancelListener cancelListenerActive1;
    private TextView introTextTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_with_reset_code);

        if(getIntent().getExtras() != null) {
            phone = (String) getIntent().getExtras().get("RESET_PASSWORD_PHONE_NUMBER");
            Log.e("resetPass", "getIntent phone: " + phone);
        }

        mResetCodeEditText = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text);
        mResetCodeInputLayoutHolder = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text_layout_holder);
        mPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text);
        mPasswordInputLayoutHolder = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text_layout_holder);
        mRepeatPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text);
        mRepeatPasswordInputLayoutHolder = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text_layout_holder);
        mResetPasswordButton = findViewById(R.id.activity_changepasswordwithresetcode_reset_password);
        mBackArrowImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mResettingPasswordButton = findViewById(R.id.activity_changepasswordwithresetcode_loader);
        introTextTextView = findViewById(R.id.activity_changepasswordwithresetcode_intotext_textView);
        coverImageView = findViewById(R.id.activity_changepasswordwithresetcode_coverimage_imageView);

        mResetPasswordButton.setOnClickListener(this);
        mBackArrowImageView.setOnClickListener(this);

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Config.openActivity(ChangePasswordWithResetCodeActivity.this, LoginActivity.class, 0, 2, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
            }
        };

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.activity_changepasswordwithresetcode_back_imageview){
            onBackPressed();
        } else if(view.getId() == R.id.activity_changepasswordwithresetcode_reset_password){
            if (!mResetCodeEditText.getText().toString().trim().equalsIgnoreCase("") &&
                    !mPasswordEditText.getText().toString().trim().equalsIgnoreCase("") &&
                    !mRepeatPasswordEditText.getText().toString().trim().equalsIgnoreCase("") &&
                    mPasswordEditText.getText().toString().trim().equals(mRepeatPasswordEditText.getText().toString().trim())) {
                if(Connectivity.isConnected(getApplicationContext())){
                    sendResetPasswordThread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            resetPassword(LocaleHelper.getLanguage(getApplicationContext()), mPasswordEditText.getText().toString().trim(), mResetCodeEditText.getText().toString().trim());
                        }
                    });
                    sendResetPasswordThread2.start();
                } else {
                    Config.showToastType1(ChangePasswordWithResetCodeActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                }
            } else if (mResetCodeEditText.getText().toString().trim().equalsIgnoreCase("") ||
                    mPasswordEditText.getText().toString().trim().equalsIgnoreCase("") ||
                    mRepeatPasswordEditText.getText().toString().trim().equalsIgnoreCase("")) {
                Config.showToastType1(ChangePasswordWithResetCodeActivity.this, getResources().getString(R.string.changepasswordwithresetcode_activity_all_fields_must_be_completed));

            } else if (!mPasswordEditText.getText().toString().trim().equals(mRepeatPasswordEditText.getText().toString().trim())) {
                Config.showToastType1(ChangePasswordWithResetCodeActivity.this, getResources().getString(R.string.changepasswordwithresetcode_activity_passwords_do_not_match));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED CHANGEPASSWORD-ACTIVITY");
        mResetCodeEditText = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text);
        mResetCodeInputLayoutHolder = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text_layout_holder);
        mPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text);
        mPasswordInputLayoutHolder = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text_layout_holder);
        mRepeatPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text);
        mRepeatPasswordInputLayoutHolder = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text_layout_holder);
        mResetPasswordButton = findViewById(R.id.activity_changepasswordwithresetcode_reset_password);
        mBackArrowImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mResettingPasswordButton = findViewById(R.id.activity_changepasswordwithresetcode_loader);
        introTextTextView = findViewById(R.id.activity_changepasswordwithresetcode_intotext_textView);
        coverImageView = findViewById(R.id.activity_changepasswordwithresetcode_coverimage_imageView);

        if(!networkResponse.trim().equalsIgnoreCase("")){
            if(networkResponse.trim().equalsIgnoreCase("1")){
                cancelListenerActive1 = new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Config.openActivity(ChangePasswordWithResetCodeActivity.this, LoginActivity.class, 0, 2, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
                    }
                };
                cancelListenerActive1 = Config.showDialogType1(ChangePasswordWithResetCodeActivity.this, "1", getResources().getString(R.string.changepasswordwithresetcode_activity_your_password_has_been_changed_successfully), "", cancelListenerActive1, true, "", "");

            } else {
                Config.showDialogType1(ChangePasswordWithResetCodeActivity.this, getString(R.string.login_activity_error), networkResponse, "", null, false, "", "");
            }
            mResettingPasswordButton.setVisibility(View.INVISIBLE);
            mResetCodeEditText.setVisibility(View.VISIBLE);
            mResetCodeInputLayoutHolder.setVisibility(View.VISIBLE);
            mPasswordEditText.setVisibility(View.VISIBLE);
            mPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
            mRepeatPasswordEditText.setVisibility(View.VISIBLE);
            mRepeatPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
            mResetPasswordButton.setVisibility(View.VISIBLE);
            coverImageView.setVisibility(View.VISIBLE);
            introTextTextView.setVisibility(View.VISIBLE);
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED CHANGEPASSWORD-ACTIVITY");
        mResetCodeEditText = null;
        mPasswordEditText = null;
        mRepeatPasswordEditText = null;
        mResetCodeInputLayoutHolder = null;
        mPasswordInputLayoutHolder = null;
        mRepeatPasswordInputLayoutHolder = null;
        mResetPasswordButton = null;
        mBackArrowImageView = null;
        coverImageView = null;
        mResettingPasswordButton = null;
        if(sendResetPasswordThread2 != null){
            sendResetPasswordThread2.interrupt();
            sendResetPasswordThread2 = null;
        }
        cancelListenerActive1 = null;
        introTextTextView = null;
        Config.freeMemory();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Log.e("memoryManage", "finish STARTED CHANGEPASSWORD-ACTIVITY");
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED CHANGEPASSWORD-ACTIVITY");
        mResetCodeEditText = null;
        mPasswordEditText = null;
        mRepeatPasswordEditText = null;
        mResetCodeInputLayoutHolder = null;
        mPasswordInputLayoutHolder = null;
        mRepeatPasswordInputLayoutHolder = null;
        mResetPasswordButton = null;
        mBackArrowImageView = null;
        coverImageView = null;
        mResettingPasswordButton = null;
        if(sendResetPasswordThread2 != null){
            sendResetPasswordThread2.interrupt();
            sendResetPasswordThread2 = null;
        }
        phone = null;
        networkResponse  = null;
        cancelListenerActive1 = null;
        introTextTextView = null;

        Config.unbindDrawables(findViewById(R.id.root_change_password_activity));
        Config.freeMemory();
    }


    public void resetPassword(final String language, final String new_password, final String reset_code){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mResetCodeEditText.setVisibility(View.INVISIBLE);
                mResetCodeInputLayoutHolder.setVisibility(View.INVISIBLE);
                mPasswordEditText.setVisibility(View.INVISIBLE);
                mPasswordInputLayoutHolder.setVisibility(View.INVISIBLE);
                mRepeatPasswordEditText.setVisibility(View.INVISIBLE);
                mRepeatPasswordInputLayoutHolder.setVisibility(View.INVISIBLE);
                mResetPasswordButton.setVisibility(View.INVISIBLE);
                coverImageView.setVisibility(View.INVISIBLE);
                introTextTextView.setVisibility(View.INVISIBLE);
                mResettingPasswordButton.setVisibility(View.VISIBLE);
            }
        });

        Log.e("resetPass", "phone: " + phone);
        Log.e("resetPass", "new_password: " + new_password);
        Log.e("resetPass", "reset_code: " + reset_code);
        Log.e("resetPass", "language: " + language);
        Log.e("resetPass", "app_version_code: " + String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)));

        AndroidNetworking.post(Config.LINK_RESET_PASSWORD_WITH_CODE)
                .addBodyParameter("user_phone_number", phone)
                .addBodyParameter("user_new_password", new_password)
                .addBodyParameter("user_password_reset_code", reset_code)
                .addBodyParameter("user_language", language)
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(getApplicationContext())))
                .setTag("changepassword_activity_resetpassword")
                .setPriority(Priority.MEDIUM)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("resetPass", "response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("data_returned");
                    JSONObject o = array.getJSONObject(0);
                    String myStatus = o.getString("status");
                    final String myStatusMessage = o.getString("message");

                    if (myStatus.equalsIgnoreCase("yes")) {
                        if(MyLifecycleHandler.isApplicationInForeground()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    cancelListenerActive1 = Config.showDialogType1(ChangePasswordWithResetCodeActivity.this, "1", myStatusMessage, "", cancelListenerActive1, true, "", "");
                                }
                            });
                        } else {
                            networkResponse = "1";
                        }
                    } else if(myStatus.equalsIgnoreCase("0")){
                        Config.openActivity(ChangePasswordWithResetCodeActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                        return;
                    } else {
                        if(MyLifecycleHandler.isApplicationInForeground()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showDialogType1(ChangePasswordWithResetCodeActivity.this, getResources().getString(R.string.login_activity_login_failed), myStatusMessage, "", null, false, "", "");
                                    mResettingPasswordButton.setVisibility(View.INVISIBLE);
                                    mResetCodeEditText.setVisibility(View.VISIBLE);
                                    mResetCodeInputLayoutHolder.setVisibility(View.VISIBLE);
                                    mPasswordEditText.setVisibility(View.VISIBLE);
                                    mPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
                                    mRepeatPasswordEditText.setVisibility(View.VISIBLE);
                                    mRepeatPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
                                    mResetPasswordButton.setVisibility(View.VISIBLE);
                                    coverImageView.setVisibility(View.VISIBLE);
                                    introTextTextView.setVisibility(View.VISIBLE);
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
                                Config.showDialogType1(ChangePasswordWithResetCodeActivity.this, getResources().getString(R.string.login_activity_error), getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                                mResettingPasswordButton.setVisibility(View.INVISIBLE);
                                mResetCodeEditText.setVisibility(View.VISIBLE);
                                mResetCodeInputLayoutHolder.setVisibility(View.VISIBLE);
                                mPasswordEditText.setVisibility(View.VISIBLE);
                                mPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
                                mRepeatPasswordEditText.setVisibility(View.VISIBLE);
                                mRepeatPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
                                mResetPasswordButton.setVisibility(View.VISIBLE);
                                coverImageView.setVisibility(View.VISIBLE);
                                introTextTextView.setVisibility(View.VISIBLE);
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
                if(MyLifecycleHandler.isApplicationInForeground()){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Config.showToastType1(ChangePasswordWithResetCodeActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                            mResettingPasswordButton.setVisibility(View.INVISIBLE);
                            mResetCodeEditText.setVisibility(View.VISIBLE);
                            mResetCodeInputLayoutHolder.setVisibility(View.VISIBLE);
                            mPasswordEditText.setVisibility(View.VISIBLE);
                            mPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
                            mRepeatPasswordEditText.setVisibility(View.VISIBLE);
                            mRepeatPasswordInputLayoutHolder.setVisibility(View.VISIBLE);
                            mResetPasswordButton.setVisibility(View.VISIBLE);
                            coverImageView.setVisibility(View.VISIBLE);
                            introTextTextView.setVisibility(View.VISIBLE);
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
