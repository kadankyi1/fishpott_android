package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class MtnMobileMoneyActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackImageView;
    private EditText mAmountEditText, mPhoneEditText;
    private TextInputLayout mAmountTextInputLayout, mPhoneTextInputLayout;
    private Button mSendRequestButton;
    private ProgressBar mLoadingProgressBar;
    private Thread networkRequestThread = null;
    private Boolean REQUEST_HAS_STARTED = false;
    private Dialog.OnCancelListener cancelListenerActive1;
    private String networkResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mtn_mobile_money);

        mBackImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mAmountEditText = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text);
        mAmountTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text_layout_holder);
        mPhoneEditText = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text);
        mPhoneTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text_layout_holder);
        mSendRequestButton = findViewById(R.id.activity_changepasswordwithresetcode_reset_password);
        mLoadingProgressBar = findViewById(R.id.activity_changepasswordwithresetcode_loader);

        mAmountTextInputLayout.setHint(getResources().getString(R.string.amount) + " (" + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + ")");

        mBackImageView.setOnClickListener(this);
        mSendRequestButton.setOnClickListener(this);


        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        };
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.activity_changepasswordwithresetcode_back_imageview){
            onBackPressed();
        } else if(view.getId() == R.id.activity_changepasswordwithresetcode_reset_password){
                    if(!mAmountEditText.getText().toString().trim().equalsIgnoreCase("") && !mPhoneEditText.getText().toString().trim().equalsIgnoreCase("")){
                        if(mPhoneEditText.getText().toString().trim().length() == 10 && Integer.valueOf(mAmountEditText.getText().toString().trim()).intValue() > 0){

                            //if(mPhoneEditText.getText().toString().trim().substring(0, 3).equalsIgnoreCase("024") || mPhoneEditText.getText().toString().trim().substring(0, 3).equalsIgnoreCase("054")){
                                if(Connectivity.isConnected(getApplicationContext())) {
                                    final String phone = mPhoneEditText.getText().toString().trim();
                                    final String amount = mAmountEditText.getText().toString().trim();
                                    networkRequestThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendMobilMoneyCollectionRequest(phone, amount);
                                        }
                                    });
                                    networkRequestThread.start();
                                } else {
                                    Config.showToastType1(MtnMobileMoneyActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                }
                            /*
                            } else {
                                Config.showToastType1(MtnMobileMoneyActivity.this, "Please enter an MTN Number");
                            }
                             */
                        } else if(mPhoneEditText.getText().toString().trim().length() != 10){
                            Config.showToastType1(MtnMobileMoneyActivity.this, getString(R.string.phone_number_should_be_10_number_with_no_country_code_eg_0241234567));
                        } else if(Integer.valueOf(mAmountEditText.getText().toString().trim()).intValue() <= 0){
                            Config.showToastType1(MtnMobileMoneyActivity.this, getString(R.string.the_amount_be_less_than_zero));
                        } else {
                            Config.showToastType1(MtnMobileMoneyActivity.this, getString(R.string.please_fill_out_the_form_correctly));
                        }
                    } else {
                        Config.showToastType1(MtnMobileMoneyActivity.this, getString(R.string.please_fill_out_the_form_correctly));
                    }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.activity_changepasswordwithresetcode_back_imageview);
        mAmountEditText = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text);
        mAmountTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_resetcode_edit_text_layout_holder);
        mPhoneEditText = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text);
        mPhoneTextInputLayout = findViewById(R.id.activity_changepasswordwithresetcode_password_edit_text_layout_holder);
        mSendRequestButton = findViewById(R.id.activity_changepasswordwithresetcode_reset_password);
        mLoadingProgressBar = findViewById(R.id.activity_changepasswordwithresetcode_loader);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            Config.showDialogType1(MtnMobileMoneyActivity.this, "1", networkResponse, "", null, false, "", "");
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            mBackImageView.setVisibility(View.VISIBLE);
            mAmountTextInputLayout.setVisibility(View.VISIBLE);
            mPhoneTextInputLayout.setVisibility(View.VISIBLE);
            mSendRequestButton.setVisibility(View.VISIBLE);
            networkResponse = "";
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mAmountEditText = null;
        mPhoneEditText = null;
        mAmountTextInputLayout = null;
        mPhoneTextInputLayout = null;
        mSendRequestButton = null;
        mLoadingProgressBar = null;
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackImageView = null;
        mAmountEditText = null;
        mPhoneEditText = null;
        mAmountTextInputLayout = null;
        mPhoneTextInputLayout = null;
        mSendRequestButton = null;
        mLoadingProgressBar = null;
        cancelListenerActive1 = null;
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_mtnmobilemoney_activity));
        Config.freeMemory();
    }

    private void sendMobilMoneyCollectionRequest(String phone, String amount){
        if(!REQUEST_HAS_STARTED){
            //REQUEST_HAS_STARTED = true;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mAmountTextInputLayout.setVisibility(View.INVISIBLE);
                    mPhoneTextInputLayout.setVisibility(View.INVISIBLE);
                    mSendRequestButton.setVisibility(View.INVISIBLE);
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                }
            });


            AndroidNetworking.post(Config.LINK_MTN_MOBILE_MONEY_CREDIT_AUTOMATIC_REQUEST_MAkER)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("mtn_phone_number", phone)
                    .addBodyParameter("amount", amount)
                    .addBodyParameter("language", LocaleHelper.getLanguage(getApplicationContext()))
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("redeem_code")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    Log.e("MtnMoMo", "response : " + response);
                    if(MyLifecycleHandler.isApplicationInForeground()){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mBackImageView.setVisibility(View.VISIBLE);
                                mAmountTextInputLayout.setVisibility(View.VISIBLE);
                                mPhoneTextInputLayout.setVisibility(View.VISIBLE);
                                mSendRequestButton.setVisibility(View.VISIBLE);
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
                            Config.showToastType1(MtnMobileMoneyActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(MtnMobileMoneyActivity.this, statusMsg);
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if(myStatus == 1) {
                            if (MyLifecycleHandler.isApplicationInForeground()) {
                                cancelListenerActive1 = Config.showDialogType1(MtnMobileMoneyActivity.this, "1", statusMsg, "", cancelListenerActive1, true, "Complete", "");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (MyLifecycleHandler.isApplicationInForeground()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(MtnMobileMoneyActivity.this, getString(R.string.an_error_occurred));
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    mBackImageView.setVisibility(View.VISIBLE);
                                    mAmountTextInputLayout.setVisibility(View.VISIBLE);
                                    mPhoneTextInputLayout.setVisibility(View.VISIBLE);
                                    mSendRequestButton.setVisibility(View.VISIBLE);
                                }
                            });
                        }  else {
                            networkResponse = getString(R.string.an_error_occurred);
                        }
                    }
                }

                @Override
                public void onError(ANError anError) {
                    if (MyLifecycleHandler.isApplicationInForeground()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Config.showToastType1(MtnMobileMoneyActivity.this, getString(R.string.an_error_occurred));
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mBackImageView.setVisibility(View.VISIBLE);
                                mAmountTextInputLayout.setVisibility(View.VISIBLE);
                                mPhoneTextInputLayout.setVisibility(View.VISIBLE);
                                mSendRequestButton.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        networkResponse = getString(R.string.an_error_occurred);
                    }
                }
            });

        }


    }

}
