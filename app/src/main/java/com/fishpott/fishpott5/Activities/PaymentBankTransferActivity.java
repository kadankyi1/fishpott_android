package com.fishpott.fishpott5.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentBankTransferActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackImageView;
    private ProgressBar mLoadingProgressBar;
    private ConstraintLayout mContentHolderConstraintLayout;
    private EditText mTransactionIdEditText, mAmountSentEditText, mSenderNameEditText;
    private TextView mBankNameTextView, mBankAddressTextView, mBankSwiftIbanCodeTextView,
            mBankBranchTextView, mBankAccountNameTextView, mBankAccNumberTextView, mReferenceCodeTextView;
    private TextView mDateMonthTextView, mDateDayTextView, mDateYearTextView, mTitleTextView;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextInputLayout mAmountSentTextInputLayout;
    private Dialog.OnCancelListener cancelListenerActive1;
    private ImageView mLoaderImageView;
    private TextView mLoaderTextView;
    private Button mSendButton;
    private Thread networkRequestThread = null;
    private Boolean REQUEST_HAS_STARTED = false, networkRequestStarted = false;
    private int presetDateDay = 19, presetDateMonth = 6, presetDateYear = 2019;
    private String momoType = "", bankName = "", bankAddress = "", bankSwiftIban = "", bankBranch = "", bankAccountName = "", bankAccountNumber = "",
            dob = "", pay_type = "", networkResponse = "";
    private String orderId = "", itemName = "", itemQuantity = "", preText = "", amountCedis = "", amountDollars = "", paymentGatewayCurrency = "", paymentType = "";
    private int paymentGatewayPriceInCentsOrPesewas = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_bank_transfer);

        if(getIntent().getExtras() != null) {
            String[] info = (String[]) getIntent().getExtras().get("ORDER_DETAILS");
            orderId = info[0];
            itemName = info[1];
            itemQuantity = info[2];
            preText = info[3];
            amountCedis = info[4];
            amountDollars = info[5];
            paymentGatewayCurrency = info[6];
            paymentGatewayPriceInCentsOrPesewas = Integer.parseInt(info[7]);
            paymentType = info[8];
            bankName = info[9];
            bankAddress = info[10];
            bankSwiftIban = info[11];
            bankBranch = info[12];
            bankAccountName = info[13];
            bankAccountNumber = info[14];

            if(
                    orderId.trim().equalsIgnoreCase("")
                            || itemName.trim().equalsIgnoreCase("")
                            || itemQuantity.trim().equalsIgnoreCase("")
                            || preText.trim().equalsIgnoreCase("")
                            || amountCedis.trim().equalsIgnoreCase("")
                            || amountDollars.trim().equalsIgnoreCase("")
                            || paymentGatewayCurrency.trim().equalsIgnoreCase("")
                            || paymentType.trim().equalsIgnoreCase("")
                            || paymentGatewayPriceInCentsOrPesewas < 1
            ){
                finish();
            }
        } else {
            finish();
        }

        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mLoadingProgressBar = findViewById(R.id.loader);
        mContentHolderConstraintLayout = findViewById(R.id.contents_holder);
        mTitleTextView = findViewById(R.id.country_withdrawfunds_activity_textview);

        mBankNameTextView = findViewById(R.id.bank_name_text);
        mBankAddressTextView = findViewById(R.id.bank_address_text);
        mBankSwiftIbanCodeTextView = findViewById(R.id.bank_swift_iban_code_text);
        mBankBranchTextView = findViewById(R.id.bank_branch_text);
        mBankAccountNameTextView = findViewById(R.id.bank_account_name_text);
        mBankAccNumberTextView = findViewById(R.id.bank_account_number_text);
        mReferenceCodeTextView = findViewById(R.id.reference_code_text);

        mTransactionIdEditText = findViewById(R.id.transaction_id_edittext);
        mAmountSentEditText = findViewById(R.id.amount_sent_edittext);
        mAmountSentTextInputLayout = findViewById(R.id.amount_sent_textinputlayout);
        mSenderNameEditText = findViewById(R.id.sender_name_edittext);
        mDateYearTextView = findViewById(R.id.date_year_textview);
        mDateMonthTextView = findViewById(R.id.date_month_textview);
        mDateDayTextView = findViewById(R.id.date_day_textview);
        mSendButton = findViewById(R.id.request_button);

        // LOADER
        mLoaderImageView = findViewById(R.id.loader_imageview);
        mLoaderTextView = findViewById(R.id.loadertext_textview);

        mAmountSentTextInputLayout.setHint(getResources().getString(R.string.amount) + "(" + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + ")");

        presetDateYear = Integer.valueOf(Config.getCurrentDateTime3("yyyy")).intValue();
        presetDateMonth = (Integer.valueOf(Config.getCurrentDateTime3("MM")).intValue()) - 1;
        presetDateDay = Integer.valueOf(Config.getCurrentDateTime3("dd")).intValue();

        mTitleTextView.setText("Send a wire transfer of " + amountDollars + " or " + amountCedis + " to the Account below. Payment must come from an account bearing your name.");
        mBankNameTextView.setText(bankName);
        mBankAddressTextView.setText(bankAddress);
        mBankSwiftIbanCodeTextView.setText(bankSwiftIban);
        mBankBranchTextView.setText(bankBranch);
        mBankAccountNameTextView.setText(bankAccountName);
        mBankAccNumberTextView.setText(bankAccountNumber);
        mReferenceCodeTextView.setText(orderId);

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Config.openActivity(PaymentBankTransferActivity.this, TransactionsActivity.class, 1, 1, 0, "", "");
            }
        };

        // LISTENING FOR WHEN THE DATE IS SET AND SETTING THE DATE TEXT
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mDateYearTextView.setText(String.valueOf(year));
                mDateMonthTextView.setText(Config.getMonthNameFromMonthNumber(PaymentBankTransferActivity.this, month, 2));
                mDateDayTextView.setText(String.valueOf(dayOfMonth));
                presetDateDay = dayOfMonth;
                presetDateMonth = month;
                presetDateYear = year;
                dob = String.valueOf(year) + "-" + String.valueOf(month+1) + "-" + String.valueOf(dayOfMonth);
            }
        };

        mBackImageView.setOnClickListener(this);
        mDateYearTextView.setOnClickListener(this);
        mDateMonthTextView.setOnClickListener(this);
        mDateDayTextView.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_bar_back_icon_imageview) {
            onBackPressed();
        } else if (view.getId() == R.id.date_year_textview
                || view.getId() == R.id.date_month_textview
                || view.getId() == R.id.date_day_textview) {
            mDateSetListener = Config.openDatePickerDialog(PaymentBankTransferActivity.this, mDateSetListener, true, presetDateDay, presetDateMonth, presetDateYear);
        } else if(view.getId() == R.id.request_button){
            if(!dob.trim().equalsIgnoreCase("")
                    && !mTransactionIdEditText.getText().toString().trim().equalsIgnoreCase("")){
                final String transID = mTransactionIdEditText.getText().toString().trim();
                final String amount = mAmountSentEditText.getText().toString().trim();
                final String senderName = mSenderNameEditText.getText().toString().trim();
                networkRequestThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateOrderStatus(orderId, paymentType, transID, dob);
                        //makeRequest(transID, amount, senderName, dob, pay_type);
                    }
                });
                networkRequestThread.start();
            } else {
                Config.showToastType1(PaymentBankTransferActivity.this, getString(R.string.the_form_is_incomplete));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mLoadingProgressBar = findViewById(R.id.loader);
        mContentHolderConstraintLayout = findViewById(R.id.contents_holder);
        //mReceivingPhoneNumberTextView = findViewById(R.id.receiving_phone_number_textview);
        //mReceivingAccNameTextView = findViewById(R.id.receiving_phone_number_acc_name_textview);
        mTransactionIdEditText = findViewById(R.id.transaction_id_edittext);
        mAmountSentEditText = findViewById(R.id.amount_sent_edittext);
        mSenderNameEditText = findViewById(R.id.sender_name_edittext);
        mDateYearTextView = findViewById(R.id.date_year_textview);
        mDateMonthTextView = findViewById(R.id.date_month_textview);
        mDateDayTextView = findViewById(R.id.date_day_textview);
        mSendButton = findViewById(R.id.request_button);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            Config.showDialogType1(PaymentBankTransferActivity.this, "1", networkResponse, "", null, false, "", "");
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            mContentHolderConstraintLayout.setVisibility(View.VISIBLE);
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mLoadingProgressBar = null;
        mContentHolderConstraintLayout = null;
        mTransactionIdEditText = null;
        mAmountSentEditText = null;
        mSenderNameEditText = null;
        //mReceivingPhoneNumberTextView = null;
        //mReceivingAccNameTextView = null;
        mDateMonthTextView = null;
        mDateDayTextView = null;
        mDateYearTextView = null;
        mSendButton = null;
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
        mLoadingProgressBar = null;
        mContentHolderConstraintLayout = null;
        mTransactionIdEditText = null;
        mAmountSentEditText = null;
        mSenderNameEditText = null;
        //mReceivingPhoneNumberTextView = null;
        //mReceivingAccNameTextView = null;
        mDateMonthTextView = null;
        mDateDayTextView = null;
        mDateYearTextView = null;
        mDateSetListener = null;
        mSendButton = null;
        momoType = null;
        //momoNumber = null;
        //momoName = null;
        dob = null;
        pay_type = null;
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_manual_momo_activity));
        Config.freeMemory();
    }


    private void updateOrderStatus(final String thisOrderID, String thisPaymentType, String transactionID, String dateSent){
        networkRequestStarted = true;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                mLoaderTextView.setText("Recording payment...");
                mContentHolderConstraintLayout.setVisibility(View.INVISIBLE);
                mLoaderImageView.setVisibility(View.VISIBLE);
                mLoaderTextView.setVisibility(View.VISIBLE);
                mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(PaymentBankTransferActivity.this, R.anim.suggestion_loading_anim));
            }
        });
        Log.e("getFinalPriceSummary", "thisOrderID: " + thisOrderID);
        Log.e("getFinalPriceSummary", "thisPaymentType: " + thisPaymentType);

        AndroidNetworking.post(Config.LINK_UPDATE_ORDER_STATUS)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(PaymentBankTransferActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(PaymentBankTransferActivity.this.getApplicationContext())))
                .addBodyParameter("item_type", thisPaymentType)
                .addBodyParameter("item_id", thisOrderID)
                .addBodyParameter("payment_gateway_status", "1")
                .addBodyParameter("payment_gateway_info", "Bank Wire Transfer From Account Name: " + transactionID + " - Date Sent: " + dateSent)
                .setTag("update_order_status")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                networkRequestStarted = false;

                try {
                    Log.e("payresponse", response);
                    final JSONObject o = new JSONObject(response);
                    int myStatus = o.getInt("status");
                    final String myStatusMessage = o.getString("message");

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){

                        if(!PaymentBankTransferActivity.this.isFinishing()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    cancelListenerActive1 = Config.showDialogType1(PaymentBankTransferActivity.this, "", "Payment successful. Your order is under review. Please record this transaction ID : " + thisOrderID, "show-positive-image", cancelListenerActive1, true, getString(R.string.setprofilepicture_activity_okay), "");


                                    /*
                                    mLoaderImageView.clearAnimation();
                                    mLoaderImageView.setVisibility(View.INVISIBLE);
                                    mLoaderTextView.setVisibility(View.INVISIBLE);
                                    mItemHolderScrollView.setVisibility(View.INVISIBLE);
                                     */

                                }
                            });
                        }
                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(PaymentBankTransferActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(PaymentBankTransferActivity.this, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mLoaderImageView.clearAnimation();
                        mLoaderImageView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setText("Click icon to retry payment recording...");
                        mContentHolderConstraintLayout.setVisibility(View.INVISIBLE);
                        Config.showToastType1(PaymentBankTransferActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(PaymentBankTransferActivity.this, myStatusMessage);
                        Config.signOutUser(PaymentBankTransferActivity.this.getApplicationContext(), true, PaymentBankTransferActivity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(PaymentBankTransferActivity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!PaymentBankTransferActivity.this.isFinishing()){
                        mLoaderImageView.clearAnimation();
                        mLoaderImageView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setText("Click icon to retry payment recording...");
                        mContentHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(PaymentBankTransferActivity.this, getString(R.string.failed_check_your_internet_and_try_again));
                if(!PaymentBankTransferActivity.this.isFinishing()){
                    mLoaderImageView.clearAnimation();
                    mLoaderImageView.setVisibility(View.VISIBLE);
                    mLoaderTextView.setVisibility(View.VISIBLE);
                    mLoaderTextView.setText("Click icon to retry payment recording...");
                    mContentHolderConstraintLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    /*
    private void makeRequest(String transactionID, String amount, String senderName, String dob, String pay_type){
        if(!REQUEST_HAS_STARTED){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mContentHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                }
            });

            AndroidNetworking.post(Config.LINK_MOBILE_MONEY_CREDIT)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("transaction_id", transactionID)
                    .addBodyParameter("pay_type", pay_type)
                    .addBodyParameter("amount_sent", amount)
                    .addBodyParameter("sender_name", senderName)
                    .addBodyParameter("send_date", dob)
                    .addBodyParameter("language", LocaleHelper.getLanguage(getApplicationContext()))
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("credit_via_momo")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    if(MyLifecycleHandler.isApplicationInForeground()){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mContentHolderConstraintLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        networkResponse = getString(R.string.an_error_occurred);
                    }

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        final JSONObject o = array.getJSONObject(0);
                        int myStatus = o.getInt("1");
                        final String statusMsg = o.getString("2");

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3 || myStatus == 5){
                            Config.showToastType1(PaymentBankTransferActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(PaymentBankTransferActivity.this, statusMsg);
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
                                            Config.showDialogType1(PaymentBankTransferActivity.this, "1", statusMsg, "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                                            mTransactionIdEditText.setText("");
                                            mSenderNameEditText.setText("");
                                            mAmountSentEditText.setText("");

                                            SettingsFragment.mDebitWalletBalanceTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + o.getString("8"));
                                            SettingsFragment.mWithdrawalWalletBalanceTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + o.getString("9"));
                                            SettingsFragment.mPottPearlsBalanceTextView.setText(o.getString("10"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET, o.getString("9"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEBIT_WALLET, o.getString("8"));
                                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_PEARLS, o.getString("10"));

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Config.showToastType1(PaymentBankTransferActivity.this, getString(R.string.an_error_occurred));
                                        }
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
                                    Config.showToastType1(PaymentBankTransferActivity.this, getString(R.string.an_error_occurred));
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    mContentHolderConstraintLayout.setVisibility(View.VISIBLE);
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
                                Config.showToastType1(PaymentBankTransferActivity.this, getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mContentHolderConstraintLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        networkResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                    }
                }
            });
        }
    }
    */

}
