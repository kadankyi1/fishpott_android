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
import com.fishpott.fishpott5.Fragments.SettingsFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RedeemWalletCreditCouponActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackImageView;
    private EditText mCouponCodeEditText;
    private TextInputLayout mCouponCodeEditTextInputLayout;
    private Button mRedeemButton;
    private ProgressBar mLoadingProgressBar;
    private Thread networkRequestThread = null;
    private Boolean REQUEST_HAS_STARTED = false;
    private String networkResponse = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_wallet_credit_coupon);

        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mCouponCodeEditText = findViewById(R.id.coupon_code_redeemwalletcreditcoupon_activity_edittext);
        mCouponCodeEditTextInputLayout = findViewById(R.id.coupon_code_redeemwalletcreditcoupon_activity_edittext_textinputlayout);
        mRedeemButton = findViewById(R.id.request_button);
        mLoadingProgressBar = findViewById(R.id.loader);


        mBackImageView.setOnClickListener(this);
        mRedeemButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.title_bar_back_icon_imageview){
            onBackPressed();
        } else if(view.getId() == R.id.request_button){

            networkRequestThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(Connectivity.isConnected(getApplicationContext())) {
                        if(mCouponCodeEditText.getText().toString().trim().length() > 1){
                            makeRequest(mCouponCodeEditText.getText().toString().trim());
                        }
                    } else {
                        Config.showToastType1(RedeemWalletCreditCouponActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                    }
                }
            });
            networkRequestThread.start();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mCouponCodeEditText = findViewById(R.id.coupon_code_redeemwalletcreditcoupon_activity_edittext);
        mCouponCodeEditTextInputLayout = findViewById(R.id.coupon_code_redeemwalletcreditcoupon_activity_edittext_textinputlayout);
        mRedeemButton = findViewById(R.id.request_button);
        mLoadingProgressBar = findViewById(R.id.loader);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            Config.showDialogType1(RedeemWalletCreditCouponActivity.this, "1", networkResponse, "", null, false, "", "");
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            mCouponCodeEditText.setVisibility(View.VISIBLE);
            mCouponCodeEditTextInputLayout.setVisibility(View.VISIBLE);
            mRedeemButton.setVisibility(View.VISIBLE);
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mCouponCodeEditText = null;
        mCouponCodeEditTextInputLayout = null;
        mRedeemButton = null;
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
        mCouponCodeEditText = null;
        mCouponCodeEditTextInputLayout = null;
        mRedeemButton = null;
        mLoadingProgressBar = null;
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_redeemwalletcreditcoupon_activity));
        Config.freeMemory();
    }

    private void makeRequest(String code){
        if(!REQUEST_HAS_STARTED){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mCouponCodeEditText.setVisibility(View.INVISIBLE);
                    mCouponCodeEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mRedeemButton.setVisibility(View.INVISIBLE);
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                }
            });

            AndroidNetworking.post(Config.LINK_REDEEM_WALLET_CREDIT_CODE)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("code", code)
                    .addBodyParameter("language", LocaleHelper.getLanguage(getApplicationContext()))
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("redeem_code")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    if(MyLifecycleHandler.isApplicationInForeground()){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mCouponCodeEditText.setVisibility(View.VISIBLE);
                                mCouponCodeEditTextInputLayout.setVisibility(View.VISIBLE);
                                mRedeemButton.setVisibility(View.VISIBLE);
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
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
                        if(myStatus == 3 || myStatus == 5){
                            Config.showToastType1(RedeemWalletCreditCouponActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(RedeemWalletCreditCouponActivity.this, statusMsg);
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
                                        try {
                                            Config.showDialogType1(RedeemWalletCreditCouponActivity.this, "1", statusMsg, "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                                            mCouponCodeEditText.setText("");
                                            SettingsFragment.mDebitWalletBalanceTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + o.getString("8"));
                                            SettingsFragment.mWithdrawalWalletBalanceTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + o.getString("9"));
                                            SettingsFragment.mPottPearlsBalanceTextView.setText(o.getString("10"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET, o.getString("9"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEBIT_WALLET, o.getString("8"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_PEARLS, o.getString("10"));

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Config.showToastType1(RedeemWalletCreditCouponActivity.this, getString(R.string.an_error_occurred));
                                            mCouponCodeEditText.setVisibility(View.VISIBLE);
                                            mCouponCodeEditTextInputLayout.setVisibility(View.VISIBLE);
                                            mRedeemButton.setVisibility(View.VISIBLE);
                                            mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(MyLifecycleHandler.isApplicationInForeground()) {
                            networkResponse = "";
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(RedeemWalletCreditCouponActivity.this, getString(R.string.an_error_occurred));
                                    mCouponCodeEditText.setVisibility(View.VISIBLE);
                                    mCouponCodeEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mRedeemButton.setVisibility(View.VISIBLE);
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
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
                                Config.showToastType1(RedeemWalletCreditCouponActivity.this, getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                mCouponCodeEditText.setVisibility(View.VISIBLE);
                                mCouponCodeEditTextInputLayout.setVisibility(View.VISIBLE);
                                mRedeemButton.setVisibility(View.VISIBLE);
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
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
