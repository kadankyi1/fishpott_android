package com.fishpott.fishpott5.Activities;

import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.Arrays;
import java.util.List;

public class WithdrawFundsActivity extends AppCompatActivity  implements View.OnClickListener {

    private ImageView mBackImageView;
    private TextView mCountryTextView, mCountryTextViewLabel, mBankInfoTextView;
    private EditText mAmountEditText, mBankMomoNetworkNameEditText, mAccNameEditText,
            mAccNumberEditText, mRoutingNumberEditText, mPasswordEditText;
    private TextInputLayout mAmountEditTextEditTextInputLayout, mBankMomoNetworkNameEditTextInputLayout,
            mAccNameEditTextInputLayout, mAccNumberEditTextInputLayout, mRoutingNumberEditTextInputLayout,
            mPasswordEditTextInputLayout;
    private Button mRedeemButton;
    private NumberPicker.OnValueChangeListener mNumberSetListener;
    private int defaultCountry = 0;
    private String selectedCountry = "", networkResponse = "";
    private ProgressBar mLoadingProgressBar;
    private Thread networkRequestThread = null;
    private Boolean REQUEST_HAS_STARTED = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_funds);

        final String[] countriesStringArraySet = getResources().getStringArray(R.array.countries_array_starting_with_choose_country);
        final List<String> countriesStringArrayList = Arrays.asList(countriesStringArraySet);

        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mCountryTextViewLabel = findViewById(R.id.country_label_withdrawfunds_activity_textview);
        mCountryTextView = findViewById(R.id.country_withdrawfunds_activity_textview);
        mAmountEditText = findViewById(R.id.amount_edittext);
        mBankMomoNetworkNameEditText = findViewById(R.id.network_name_edittext);
        mAccNameEditText = findViewById(R.id.account_name_edittext);
        mAccNumberEditText = findViewById(R.id.account_number_edittext);
        mBankInfoTextView = findViewById(R.id.activity_withdrawfunds_activity_bankinfo_textview);
        mRoutingNumberEditText = findViewById(R.id.routing_number_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);
        mAmountEditTextEditTextInputLayout = findViewById(R.id.amount_withdrawfunds_activity_edittext_textinputlayout);
        mBankMomoNetworkNameEditTextInputLayout = findViewById(R.id.bank_momo_networkk_withdrawfunds_activity_edittext_textinputlayout);
        mAccNameEditTextInputLayout = findViewById(R.id.account_name_withdrawfunds_activity_edittext_textinputlayout);
        mAccNumberEditTextInputLayout = findViewById(R.id.account_mobile_number_withdrawfunds_activity_edittext_textinputlayout);
        mRoutingNumberEditTextInputLayout = findViewById(R.id.routing_number_withdrawfunds_activity_edittext_textinputlayout);
        mPasswordEditTextInputLayout = findViewById(R.id.password_withdrawfunds_activity_edittext_textinputlayout);
        mRedeemButton = findViewById(R.id.request_button);
        mLoadingProgressBar = findViewById(R.id.loader);

        mAmountEditTextEditTextInputLayout.setHint(getResources().getString(R.string.amount) + "(" + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET)  + " " + getResources().getString(R.string.available) + ")");


        mCountryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNumberSetListener = Config.openNumberPickerForCountries(WithdrawFundsActivity.this, mNumberSetListener, 0, countriesStringArraySet.length-1, true, getResources().getStringArray(R.array.countries_array_starting_with_choose_country), defaultCountry);
            }
        });

        mNumberSetListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                defaultCountry = newVal;
                mCountryTextView.setText(countriesStringArrayList.get(newVal));
                selectedCountry = countriesStringArrayList.get(newVal);
            }
        };


        mBackImageView.setOnClickListener(this);
        mRedeemButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.title_bar_back_icon_imageview){
            onBackPressed();
        } else if(view.getId() == R.id.request_button){

                    if(Connectivity.isConnected(getApplicationContext())) {
                        if(     !selectedCountry.trim().equalsIgnoreCase("")
                                && !selectedCountry.trim().equalsIgnoreCase(getString(R.string.choose_country))
                                && mAmountEditText.getText().toString().trim().length() > 1
                                && mBankMomoNetworkNameEditText.getText().toString().trim().length() > 1
                                && mAccNameEditText.getText().toString().trim().length() > 1
                                && mAccNumberEditText.getText().toString().trim().length() > 1
                                && mPasswordEditText.getText().toString().trim().length() > 1 ){

                            String bankMomoName = "Mobile Money";
                            if(!mBankMomoNetworkNameEditText.getText().toString().trim().equalsIgnoreCase("")){
                                bankMomoName = mBankMomoNetworkNameEditText.getText().toString().trim();
                            }

                            final String amount = mAmountEditText.getText().toString().trim();
                            final String bankMomoNetworkName = bankMomoName;
                            final String accName = mAccNameEditText.getText().toString().trim();
                            final String accNumber = mAccNumberEditText.getText().toString().trim();
                            final String routingNumber = mRoutingNumberEditText.getText().toString().trim();
                            final String password = mPasswordEditText.getText().toString().trim();



                            networkRequestThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    makeRequest(selectedCountry, amount, bankMomoNetworkName,accName, accNumber, routingNumber, password);
                                }
                            });
                            networkRequestThread.start();
                        } else {
                            Config.showToastType1(WithdrawFundsActivity.this, getResources().getString(R.string.the_form_is_incomplete));
                        }
                    } else {
                        Config.showToastType1(WithdrawFundsActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                    }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mCountryTextViewLabel = findViewById(R.id.country_label_withdrawfunds_activity_textview);
        mCountryTextView = findViewById(R.id.country_withdrawfunds_activity_textview);
        mAmountEditText = findViewById(R.id.amount_edittext);
        mBankMomoNetworkNameEditText = findViewById(R.id.network_name_edittext);
        mAccNameEditText = findViewById(R.id.account_name_edittext);
        mAccNumberEditText = findViewById(R.id.account_number_edittext);
        mBankInfoTextView = findViewById(R.id.activity_withdrawfunds_activity_bankinfo_textview);
        mRoutingNumberEditText = findViewById(R.id.routing_number_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);
        mAmountEditTextEditTextInputLayout = findViewById(R.id.amount_withdrawfunds_activity_edittext_textinputlayout);
        mBankMomoNetworkNameEditTextInputLayout = findViewById(R.id.bank_momo_networkk_withdrawfunds_activity_edittext_textinputlayout);
        mAccNameEditTextInputLayout = findViewById(R.id.account_name_withdrawfunds_activity_edittext_textinputlayout);
        mAccNumberEditTextInputLayout = findViewById(R.id.account_mobile_number_withdrawfunds_activity_edittext_textinputlayout);
        mRoutingNumberEditTextInputLayout = findViewById(R.id.routing_number_withdrawfunds_activity_edittext_textinputlayout);
        mPasswordEditTextInputLayout = findViewById(R.id.password_withdrawfunds_activity_edittext_textinputlayout);
        mRedeemButton = findViewById(R.id.request_button);
        mLoadingProgressBar = findViewById(R.id.loader);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            Config.showDialogType1(WithdrawFundsActivity.this, "1", networkResponse, "", null, false, "", "");
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            mAmountEditTextEditTextInputLayout.setVisibility(View.VISIBLE);
            mBankMomoNetworkNameEditTextInputLayout.setVisibility(View.VISIBLE);
            mAccNameEditTextInputLayout.setVisibility(View.VISIBLE);
            mAccNumberEditTextInputLayout.setVisibility(View.VISIBLE);
            mRoutingNumberEditTextInputLayout.setVisibility(View.VISIBLE);
            mPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
            mRedeemButton.setVisibility(View.VISIBLE);
            mBankInfoTextView.setVisibility(View.VISIBLE);
            mCountryTextViewLabel.setVisibility(View.VISIBLE);
            mCountryTextView.setVisibility(View.VISIBLE);
            networkResponse = "";
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mCountryTextView = null;
        mCountryTextViewLabel = null;
        mBankInfoTextView = null;
        mAmountEditText = null;
        mBankMomoNetworkNameEditText = null;
        mAccNameEditText = null;
        mAccNumberEditText = null;
        mRoutingNumberEditText = null;
        mPasswordEditText = null;
        mAmountEditTextEditTextInputLayout = null;
        mBankMomoNetworkNameEditTextInputLayout = null;
        mAccNameEditTextInputLayout = null;
        mAccNumberEditTextInputLayout = null;
        mRoutingNumberEditTextInputLayout = null;
        mPasswordEditTextInputLayout = null;
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
        mCountryTextView = null;
        mCountryTextViewLabel = null;
        mBankInfoTextView = null;
        mAmountEditText = null;
        mBankMomoNetworkNameEditText = null;
        mAccNameEditText = null;
        mAccNumberEditText = null;
        mRoutingNumberEditText = null;
        mPasswordEditText = null;
        mAmountEditTextEditTextInputLayout = null;
        mBankMomoNetworkNameEditTextInputLayout = null;
        mAccNameEditTextInputLayout = null;
        mAccNumberEditTextInputLayout = null;
        mRoutingNumberEditTextInputLayout = null;
        mPasswordEditTextInputLayout = null;
        mRedeemButton = null;
        mNumberSetListener = null;
        selectedCountry  = null;
        mLoadingProgressBar = null;
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_withdrawfunds_activity));
        Config.freeMemory();
    }


    private void makeRequest(String country, String amount, String bankorNetworkName, String accName, String accNumber, String routingNumber, String rawPass){
        if(!REQUEST_HAS_STARTED){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mAmountEditTextEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mBankMomoNetworkNameEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mAccNameEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mAccNumberEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mRoutingNumberEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mPasswordEditTextInputLayout.setVisibility(View.INVISIBLE);
                    mRedeemButton.setVisibility(View.INVISIBLE);
                    mCountryTextViewLabel.setVisibility(View.INVISIBLE);
                    mCountryTextView.setVisibility(View.INVISIBLE);
                    mBankInfoTextView.setVisibility(View.INVISIBLE);
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                }
            });

            AndroidNetworking.post(Config.LINK_WITHDRAW_FUNDS)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("settle_type", "")
                    .addBodyParameter("country", country)
                    .addBodyParameter("withdrawal_amount", amount)
                    .addBodyParameter("bank_or_network_name", bankorNetworkName)
                    .addBodyParameter("acc_name", accName)
                    .addBodyParameter("acc_number", accNumber)
                    .addBodyParameter("routing_number", routingNumber)
                    .addBodyParameter("raw_pass", rawPass)
                    .addBodyParameter("language", LocaleHelper.getLanguage(getApplicationContext()))
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("withdraw_funds")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    if(MyLifecycleHandler.isApplicationInForeground()){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mAmountEditTextEditTextInputLayout.setVisibility(View.VISIBLE);
                                mBankMomoNetworkNameEditTextInputLayout.setVisibility(View.VISIBLE);
                                mAccNameEditTextInputLayout.setVisibility(View.VISIBLE);
                                mAccNumberEditTextInputLayout.setVisibility(View.VISIBLE);
                                mRoutingNumberEditTextInputLayout.setVisibility(View.VISIBLE);
                                mPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mRedeemButton.setVisibility(View.VISIBLE);
                                mBankInfoTextView.setVisibility(View.VISIBLE);
                                mCountryTextViewLabel.setVisibility(View.VISIBLE);
                                mCountryTextView.setVisibility(View.VISIBLE);
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
                            Config.showToastType1(WithdrawFundsActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(WithdrawFundsActivity.this, statusMsg);
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
                                            Config.showDialogType1(WithdrawFundsActivity.this, "1", statusMsg, "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                                            mAmountEditText.setText("");
                                            mBankMomoNetworkNameEditText.setText("");
                                            mAccNameEditText.setText("");
                                            mAccNumberEditText.setText("");
                                            mRoutingNumberEditText.setText("");
                                            mPasswordEditText.setText("");

                                            SettingsFragment.mDebitWalletBalanceTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + o.getString("8"));
                                            SettingsFragment.mWithdrawalWalletBalanceTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + o.getString("9"));
                                            SettingsFragment.mPottPearlsBalanceTextView.setText(o.getString("10"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET, o.getString("9"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEBIT_WALLET, o.getString("8"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_PEARLS, o.getString("10"));

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Config.showToastType1(WithdrawFundsActivity.this, getString(R.string.an_error_occurred));
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
                                    Config.showToastType1(WithdrawFundsActivity.this, getString(R.string.an_error_occurred));
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    mAmountEditTextEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mBankMomoNetworkNameEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mAccNameEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mAccNumberEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mRoutingNumberEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                    mRedeemButton.setVisibility(View.VISIBLE);
                                    mCountryTextViewLabel.setVisibility(View.VISIBLE);
                                    mCountryTextView.setVisibility(View.VISIBLE);
                                    mBankInfoTextView.setVisibility(View.VISIBLE);
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
                                Config.showToastType1(WithdrawFundsActivity.this, getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mAmountEditTextEditTextInputLayout.setVisibility(View.VISIBLE);
                                mBankMomoNetworkNameEditTextInputLayout.setVisibility(View.VISIBLE);
                                mAccNameEditTextInputLayout.setVisibility(View.VISIBLE);
                                mAccNumberEditTextInputLayout.setVisibility(View.VISIBLE);
                                mRoutingNumberEditTextInputLayout.setVisibility(View.VISIBLE);
                                mPasswordEditTextInputLayout.setVisibility(View.VISIBLE);
                                mRedeemButton.setVisibility(View.VISIBLE);
                                mCountryTextViewLabel.setVisibility(View.VISIBLE);
                                mCountryTextView.setVisibility(View.VISIBLE);
                                mBankInfoTextView.setVisibility(View.VISIBLE);
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
