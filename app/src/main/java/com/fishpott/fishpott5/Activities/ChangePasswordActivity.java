package com.fishpott.fishpott5.Activities;

import android.os.Handler;
import android.os.Looper;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

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

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackImageView, mPosterImageView;
    private EditText mCurrentPasswordEditText, mNewPasswordEditText, mNewRepeatedPasswordEditText;
    private TextInputLayout mCurrentPasswordEditTextInputLayout, mNewPasswordEditTextInputLayout, mNewRepeatedPasswordEditTextInputLayout;
    private Button mNetworkRequestButton1;
    private ProgressBar mLoadingProgressBar;
    private Thread networkRequestThread = null;
    private Boolean REQUEST_HAS_STARTED = false;
    private String networkResponse = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mBackImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mPosterImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mCurrentPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text);
        mNewPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text);
        mNewRepeatedPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text);
        mCurrentPasswordEditTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text_layout_holder);
        mNewPasswordEditTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text_layout_holder);
        mNewRepeatedPasswordEditTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text_layout_holder);
        mNetworkRequestButton1 = findViewById(R.id.activity_changepasswordwithresetcode_reset_password);
        mLoadingProgressBar = findViewById(R.id.activity_changepasswordwithresetcode_loader);


        mBackImageView.setOnClickListener(this);
        mNetworkRequestButton1.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.activity_changepasswordwithresetcode_back_imageview){
            onBackPressed();
        } else if(view.getId() == R.id.activity_changepasswordwithresetcode_reset_password){

                    if(mNewPasswordEditText.getText().toString().trim().equals(mNewRepeatedPasswordEditText.getText().toString().trim())){
                        if(Connectivity.isConnected(getApplicationContext())) {
                            final String currPassword = mCurrentPasswordEditText.getText().toString().trim();
                            final String newPassword = mNewPasswordEditText.getText().toString().trim();
                            networkRequestThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    makeRequest(currPassword, newPassword);
                                }
                            });
                            networkRequestThread.start();
                            } else {
                            Config.showToastType1(ChangePasswordActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                        }
                    } else {
                        Config.showToastType1(ChangePasswordActivity.this, getString(R.string.new_password_does_not_match_repeated_password));
                    }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mPosterImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mCurrentPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text);
        mNewPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text);
        mNewRepeatedPasswordEditText = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text);
        mCurrentPasswordEditTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text_layout_holder);
        mNewPasswordEditTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text_layout_holder);
        mNewRepeatedPasswordEditTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_repeatpassword_edit_text_layout_holder);
        mNetworkRequestButton1 = findViewById(R.id.activity_changepasswordwithresetcode_reset_password);
        mLoadingProgressBar = findViewById(R.id.activity_changepasswordwithresetcode_loader);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            Config.showDialogType1(ChangePasswordActivity.this, "1", networkResponse, "", null, false, "", "");
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            mPosterImageView.setVisibility(View.VISIBLE);
            mCurrentPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
            mNewPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
            mNewRepeatedPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
            mNetworkRequestButton1.setVisibility(View.VISIBLE);
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mPosterImageView = null;
        mCurrentPasswordEditText = null;
        mNewPasswordEditText = null;
        mNewRepeatedPasswordEditText = null;
        mCurrentPasswordEditTextInputLayout = null;
        mNewPasswordEditTextInputLayout = null;
        mNewRepeatedPasswordEditTextInputLayout = null;
        mNetworkRequestButton1 = null;
        mLoadingProgressBar = null;
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackImageView = null;
        mPosterImageView = null;
        mCurrentPasswordEditText = null;
        mNewPasswordEditText = null;
        mNewRepeatedPasswordEditText = null;
        mCurrentPasswordEditTextInputLayout = null;
        mNewPasswordEditTextInputLayout = null;
        mNewRepeatedPasswordEditTextInputLayout = null;
        mNetworkRequestButton1 = null;
        mLoadingProgressBar = null;
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_change_password_loggedin_activity));
        Config.freeMemory();
    }

    private void makeRequest(String currentPassword, String newPassword){
        if(!REQUEST_HAS_STARTED){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mPosterImageView.setVisibility(View.INVISIBLE);
                    mCurrentPasswordEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mNewPasswordEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mNewRepeatedPasswordEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mNetworkRequestButton1.setVisibility(View.INVISIBLE);
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                }
            });

            AndroidNetworking.post(Config.LINK_CHANGE_PASSWORD)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("current_password", currentPassword)
                    .addBodyParameter("new_password", newPassword)
                    .addBodyParameter("language", LocaleHelper.getLanguage(getApplicationContext()))
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("redeem_code")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    if(MyLifecycleHandler.isApplicationInForeground()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mPosterImageView.setVisibility(View.VISIBLE);
                                mCurrentPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mNewPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mNewRepeatedPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mNetworkRequestButton1.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        final JSONObject o = array.getJSONObject(0);
                        int myStatus = o.getInt("1");
                        final String statusMsg = o.getString("2");
                        networkResponse = statusMsg;

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(ChangePasswordActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(ChangePasswordActivity.this, statusMsg);
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if(myStatus == 1){
                            if(MyLifecycleHandler.isApplicationInForeground()) {
                                networkResponse = "";
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Config.showDialogType1(ChangePasswordActivity.this, "1", statusMsg, "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                                        try {
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN, o.getString("8"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        mCurrentPasswordEditText.setText("");
                                        mNewPasswordEditText.setText("");
                                        mNewRepeatedPasswordEditText.setText("");
                                    }
                                });
                            } else {
                                networkResponse = statusMsg;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(MyLifecycleHandler.isApplicationInForeground()) {
                            networkResponse = "";
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(ChangePasswordActivity.this, getString(R.string.an_error_occurred));
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    mPosterImageView.setVisibility(View.VISIBLE);
                                    mCurrentPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mNewPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mNewRepeatedPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mNetworkRequestButton1.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            networkResponse = getString(R.string.an_error_occurred);
                        }
                    }
                }

                @Override
                public void onError(ANError anError) {
                    if(MyLifecycleHandler.isApplicationInForeground()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Config.showToastType1(ChangePasswordActivity.this, getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mPosterImageView.setVisibility(View.VISIBLE);
                                mCurrentPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mNewPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mNewRepeatedPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mNetworkRequestButton1.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        networkResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                    }
                }
            });
        }
    }
}
