package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuySharesForSaleActivity extends AppCompatActivity implements View.OnClickListener {

    private String shareID = "", shareNewsID = "", shareParentID = "", shareName = "", shareLogo = "", shareQuantity = "",
            receiverPottName = "", finalRiskType = "", finalQuantity = "", finalPassword = "", networkResponse = "";
    private int shareQuantityInt = 0, selectedRiskIndex = 0, finalPurchaseStatus = 0;
    private ImageView mBackImageView;
    private ScrollView mItemHolderScrollView, mFinalHolderScrollView;
    private Button mBuyButton, mGetPriceButton, mResetButton;
    private TextInputLayout mQuantityTextInputLayout;
    private CircleImageView mSharesLogoCircleImageView;
    private ProgressBar mLoadingProgressBar;
    private String[] riskNames;
    private TextView mShareNameTextView, mFinalPricePerShareTextView, mFinalQuantityTextView, mInsuranceFeeTextView, mProcessingFeeTextView,
            mFinalRateTextView, mFinalTotalTextView, mFinalYieldInfoTextView, mRiskTextView, mFinalRiskTextView, mFinalItemNameTextView,
            mTermAndConditionsTextView;
    private List<String> sharesNamesStringArrayList = new ArrayList<>();
    private EditText mQuantityEditText, mReceiverPottNameEditText, mPasswordEditText;
    private Thread imageLoaderThread = null, networkThread = null;
    private NumberPicker.OnValueChangeListener mSharesSetListener;
    private Dialog.OnCancelListener cancelListenerActive1;
    Boolean THIS_IS_A_TREASURY_BILL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_shares_for_sale);

        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle !=null) {
            String[] buyInfo =(String[]) intentBundle.get("BUY_INFO");
            shareID = buyInfo[0];
            shareParentID = buyInfo[1];
            shareName = buyInfo[2].trim();
            shareQuantity = buyInfo[3];
            shareLogo = buyInfo[4];
            if(buyInfo.length >=6){
                shareNewsID = buyInfo[5];
            }
            Log.e("BuySharesForSaleActivit", "shareID : " + shareID);
            Log.e("BuySharesForSaleActivit", "shareParentID : " + shareParentID);
            Log.e("BuySharesForSaleActivit", "shareName : " + shareName);
            Log.e("BuySharesForSaleActivit", "shareQuantity : " + shareQuantity);
            Log.e("BuySharesForSaleActivit", "shareLogo : " + shareLogo);
            if(shareID.trim().equalsIgnoreCase("") || shareParentID.trim().equalsIgnoreCase("")
                    || shareName.trim().equalsIgnoreCase("") || shareQuantity.trim().equalsIgnoreCase("")
                    || shareLogo.trim().equalsIgnoreCase("")){
                Config.showToastType1(BuySharesForSaleActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
                finish();
            }
            shareQuantityInt = Integer.valueOf(shareQuantity).intValue();

            if(shareQuantityInt <= 0){
                Config.showToastType1(BuySharesForSaleActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
                finish();
            }
        } else {
            Config.showToastType1(BuySharesForSaleActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
            finish();
        }

        sharesNamesStringArrayList.add(getResources().getString(R.string.choose_risk_protection));
        sharesNamesStringArrayList.add(getString(R.string._100_risk_protection));
        sharesNamesStringArrayList.add(getString(R.string._50_risk_protection));
        sharesNamesStringArrayList.add(getString(R.string._no_risk_protection));
        riskNames = new String[]{
                getResources().getString(R.string.choose_risk_protection),
                getResources().getString(R.string._100_risk_protection),
                getResources().getString(R.string._50_risk_protection),
                getResources().getString(R.string._no_risk_protection)
        };

        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mSharesLogoCircleImageView = findViewById(R.id.shares_for_sale_logo_circleimageview);
        mShareNameTextView = findViewById(R.id.share_name_textview);

        mItemHolderScrollView = findViewById(R.id.contents_holder);
        mQuantityTextInputLayout = findViewById(R.id.amount_sent_textinputlayout);
        mQuantityEditText = findViewById(R.id.amount_sent_edittext);
        mReceiverPottNameEditText = findViewById(R.id.sender_name_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);
        mRiskTextView = findViewById(R.id.risk_input_textview);
        mGetPriceButton = findViewById(R.id.price_button);
        mFinalHolderScrollView = findViewById(R.id.contents_holder2);
        mFinalItemNameTextView = findViewById(R.id.share_name_textview2);
        mFinalPricePerShareTextView = findViewById(R.id.price_per_share_textview);
        mFinalQuantityTextView = findViewById(R.id.quantity_textview);
        mFinalRateTextView = findViewById(R.id.rate_textview);
        mFinalTotalTextView = findViewById(R.id.total_textview);
        mFinalYieldInfoTextView = findViewById(R.id.agree_checkbox);
        mInsuranceFeeTextView = findViewById(R.id.risk_fee_textview);
        mProcessingFeeTextView = findViewById(R.id.processing_fee_textview);
        mTermAndConditionsTextView = findViewById(R.id.t_and_c_textview);
        mFinalRiskTextView = findViewById(R.id.risk_textview);
        mLoadingProgressBar = findViewById(R.id.loader);
        mBuyButton = findViewById(R.id.request_button);
        mResetButton = findViewById(R.id.reset_button);

        if(shareName.length() > 4){
            if(shareName.trim().substring(shareName.length() - 4).equalsIgnoreCase("bill")){
                mRiskTextView.setClickable(false);
                mRiskTextView.setText(getString(R.string._100_risk_protection));
                selectedRiskIndex = 1;
                finalRiskType = String.valueOf(selectedRiskIndex);
                THIS_IS_A_TREASURY_BILL = true;
            }
        } else {
            Config.showToastType1(BuySharesForSaleActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
            finish();
        }


        mQuantityTextInputLayout.setHint(getResources().getString(R.string.how_many_shares)  + "(" + getResources().getString(R.string.max) + ": " + shareQuantity + ")" );
        mShareNameTextView.setText(shareName);
        mFinalItemNameTextView.setText(getResources().getString(R.string.item) + " " + shareName);
        imageLoaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(shareLogo.length() > 15){
                    Config.loadUrlImage(BuySharesForSaleActivity.this, true, shareLogo, mSharesLogoCircleImageView, 0, 15, 15);
                } else {
                    Config.loadErrorImageView(BuySharesForSaleActivity.this, R.drawable.setprofilepicture_activity_imageholder_default_image, mSharesLogoCircleImageView, 15, 15);
                }
            }
        });
        imageLoaderThread.start();

        mSharesLogoCircleImageView.setOnClickListener(this);
        mShareNameTextView.setOnClickListener(this);
        mRiskTextView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mGetPriceButton.setOnClickListener(this);
        mBuyButton.setOnClickListener(this);
        mResetButton.setOnClickListener(this);
        mTermAndConditionsTextView.setOnClickListener(this);


        mSharesSetListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                selectedRiskIndex = newVal;
                finalRiskType = String.valueOf(newVal);
                if(selectedRiskIndex > 0){
                    mRiskTextView.setText(sharesNamesStringArrayList.get(newVal));
                } else {
                    mRiskTextView.setText(sharesNamesStringArrayList.get(0));
                }
            }
        };

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(finalPurchaseStatus == 6){
                    Config.openActivity(BuySharesForSaleActivity.this, CreditWalletActivity.class, 1, 0, 0, "", "");
                    //Config.openActivity(BuySharesForSaleActivity.this, MtnMobileMoneyActivity.class, 1, 0, 0, "", "");
                } else {
                    finish();
                }
            }
        };
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

        if(networkThread != null){
            networkThread.interrupt();
            networkThread = null;
        }

        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }

        Config.unbindDrawables(findViewById(R.id.root_buysharesforsale_activity));
        Config.freeMemory();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == mBackImageView.getId()){
            onBackPressed();
        } else if(view.getId() == mTermAndConditionsTextView.getId()){
            String url = Config.CURRENT_HTTP_IN_USE + "www.fishpott.com/t_c.html";
            Config.openActivity(BuySharesForSaleActivity.this, WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, url);
            return;
        } else if(view.getId() == mSharesLogoCircleImageView.getId() || view.getId() == mShareNameTextView.getId()){
            Config.openActivity(BuySharesForSaleActivity.this, StockProfileActivity.class, 1, 0, 1, "shareparentid",  shareParentID);
        } else if(view.getId() == mGetPriceButton.getId()){
            if(mQuantityEditText.getText().toString().trim().length() > 0 && selectedRiskIndex > 0){
                finalQuantity = mQuantityEditText.getText().toString().trim();
                finalPassword = mPasswordEditText.getText().toString().trim();
                int buyQuantityInt = Integer.valueOf(finalQuantity).intValue();
                receiverPottName = mReceiverPottNameEditText.getText().toString().trim();
                if(buyQuantityInt > 0 && buyQuantityInt <= shareQuantityInt){
                    if(Connectivity.isConnected(BuySharesForSaleActivity.this)){
                        networkThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getFinalPrice(shareID, String.valueOf(selectedRiskIndex), finalQuantity, LocaleHelper.getLanguage(BuySharesForSaleActivity.this));
                            }
                        });
                        networkThread.start();
                } else {
                        Config.showToastType1(BuySharesForSaleActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                    }
                }
                //sendComment(newsId, NewsStatsListDataGenerator.getAllData().size(), message, LocaleHelper.getLanguage(getApplicationContext()));
            } else if(mQuantityEditText.getText().toString().trim().length() < 0){
                Config.showToastType1(BuySharesForSaleActivity.this, getString(R.string.the_form_is_incomplete));
            } else if(selectedRiskIndex <= 0){
                Config.showToastType1(BuySharesForSaleActivity.this, getString(R.string.choose_a_risk_protection));
            }
        } else if(view.getId() == mBuyButton.getId()){
            if(
                    (finalRiskType.trim().equalsIgnoreCase("1") || finalRiskType.trim().equalsIgnoreCase("2") || finalRiskType.trim().equalsIgnoreCase("3"))
                    && Integer.valueOf(finalQuantity).intValue() > 0 && !finalPassword.trim().equalsIgnoreCase("")
                    ){
                CompletePurchase(shareID, shareNewsID, finalPassword, finalRiskType, finalQuantity, receiverPottName, LocaleHelper.getLanguage(BuySharesForSaleActivity.this));
            } else {

                if(!finalRiskType.trim().equalsIgnoreCase("1") && !finalRiskType.trim().equalsIgnoreCase("2") && !finalRiskType.trim().equalsIgnoreCase("3")){

                } else if(Integer.valueOf(finalQuantity).intValue() < 1){
                    Config.showToastType1(BuySharesForSaleActivity.this, getResources().getString(R.string.submission_invalid_please_restart_process));
                }
            }
        } else if(view.getId() == mRiskTextView.getId()){
            if(!THIS_IS_A_TREASURY_BILL) {
                mSharesSetListener = Config.openNumberPickerForCountries(BuySharesForSaleActivity.this, mSharesSetListener, 0, sharesNamesStringArrayList.size() - 1, true, riskNames, selectedRiskIndex);
            }
        } else if(view.getId() == mResetButton.getId()){
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            mFinalHolderScrollView.setVisibility(View.INVISIBLE);
            mItemHolderScrollView.setVisibility(View.VISIBLE);
        }
    }

    private void getFinalPrice(String theStockID, String riskType, String buyQuantity, String language){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mItemHolderScrollView.setVisibility(View.INVISIBLE);
                mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                mLoadingProgressBar.setVisibility(View.VISIBLE);
            }
        });
            AndroidNetworking.post(Config.LINK_GET_FINAL_PRICE)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("share_id", theStockID)
                    .addBodyParameter("risk_type", riskType)
                    .addBodyParameter("buy_quantity", buyQuantity)
                    .addBodyParameter("language", language)
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("get_final_price")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    Log.e("BuyShares4", "response : " + response);
                    if (MyLifecycleHandler.isApplicationVisible() && !BuySharesForSaleActivity.this.isFinishing()){
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("data_returned");

                            final JSONObject o = array.getJSONObject(0);
                            final int myStatus = o.getInt("1");
                            final String statusMsg = o.getString("2");
                            networkResponse = statusMsg;

                            if (myStatus != 1) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                        mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                                        mItemHolderScrollView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }

                            // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                            if (myStatus == 2) {
                                Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                                Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                                return;
                            }

                            // GENERAL ERROR
                            if (myStatus == 3) {
                                Config.showDialogType1(BuySharesForSaleActivity.this, "", statusMsg, "", null, false, "", "");
                                return;
                            }

                            // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                            if (myStatus == 4) {
                                Config.showDialogType1(BuySharesForSaleActivity.this, "", statusMsg, "", null, false, "", "");
                                Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                            }

                            if (myStatus == 5) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        shareQuantity = statusMsg;
                                        shareQuantityInt = Integer.valueOf(shareQuantity).intValue();
                                        mQuantityTextInputLayout.setHint(getResources().getString(R.string.how_many_shares) + "(" + getResources().getString(R.string.max) + ": " + statusMsg + ")");
                                    }
                                });
                                Config.showDialogType1(BuySharesForSaleActivity.this, "1", getString(R.string.the_available_quantity_has_changed_please_update_the_quantity_you_want_to_buy), "", null, false, "", "");
                                return;
                            }

                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));
                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                            Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                            if (myStatus == 1) {
                                final String currPricePerItem = o.getString("8");
                                final String currQuantity = o.getString("9");
                                final String currRate = o.getString("10");
                                final String currTotal = o.getString("11");
                                final String currYieldInfo = o.getString("12");
                                final String currRisk = o.getString("13");
                                final String riskInsuranceFee = o.getString("15");
                                final String processingFee = o.getString("14");
                                finalQuantity = currQuantity;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFinalPricePerShareTextView.setText(getResources().getString(R.string.price_per_item) + " " + currPricePerItem);
                                        mFinalQuantityTextView.setText(getResources().getString(R.string.quantity_colon) + " " + currQuantity);
                                        mFinalRateTextView.setText(getResources().getString(R.string.rate) + " " + currRate);
                                        mFinalTotalTextView.setText(getResources().getString(R.string.total) + " " + currTotal);
                                        mFinalYieldInfoTextView.setText(currYieldInfo);
                                        mFinalRiskTextView.setText(getResources().getString(R.string.risk) + " " + currRisk);
                                        mInsuranceFeeTextView.setText(getResources().getString(R.string.risk_insurance_fee) + " " + riskInsuranceFee);
                                        mProcessingFeeTextView.setText(getResources().getString(R.string.processing_fee) + " " + processingFee);
                                        mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                        mItemHolderScrollView.setVisibility(View.INVISIBLE);
                                        mFinalHolderScrollView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                                    mItemHolderScrollView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onError(ANError anError) {
                    if(MyLifecycleHandler.isApplicationVisible()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                                mItemHolderScrollView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
    }

    private void CompletePurchase(String theStockID, String newsId, String currFinalPassword, String riskType, String buyQuantity, String receiverPName, String language){
        finalPurchaseStatus = 0;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mItemHolderScrollView.setVisibility(View.INVISIBLE);
                mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                mLoadingProgressBar.setVisibility(View.VISIBLE);
            }
        });

        AndroidNetworking.post(Config.LINK_COMPLETE_PURCHASE)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("share_id", theStockID)
                .addBodyParameter("risk_type", riskType)
                .addBodyParameter("buy_quantity", buyQuantity)
                .addBodyParameter("myrawpass", currFinalPassword)
                .addBodyParameter("receiver_pottname", receiverPName)
                .addBodyParameter("news_id", newsId)
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("get_final_price")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("CompleteSharesBuy", "response : " + response);
                if (MyLifecycleHandler.isApplicationVisible()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        final JSONObject o = array.getJSONObject(0);
                        final int myStatus = o.getInt("1");
                        final String statusMsg = o.getString("2");

                        if (myStatus != 1) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                                    mItemHolderScrollView.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if (myStatus == 2) {
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if (myStatus == 3) {
                            Config.showDialogType1(BuySharesForSaleActivity.this, "", statusMsg, "", null, false, "", "");
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if (myStatus == 4) {
                            Config.showDialogType1(BuySharesForSaleActivity.this, "", statusMsg, "", null, false, "", "");
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        if (myStatus == 5) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    shareQuantity = statusMsg;
                                    shareQuantityInt = Integer.valueOf(shareQuantity).intValue();
                                    mQuantityTextInputLayout.setHint(getResources().getString(R.string.how_many_shares) + "(" + getResources().getString(R.string.max) + ": " + statusMsg + ")");
                                }
                            });
                            Config.showDialogType1(BuySharesForSaleActivity.this, "1", getString(R.string.the_available_quantity_has_changed_please_update_the_quantity_you_want_to_buy), "", null, false, "", "");
                            return;
                        }

                        if (myStatus == 6) {
                            finalPurchaseStatus = 6;
                            Config.showDialogType1(BuySharesForSaleActivity.this, "", getString(R.string.you_do_not_have_enough_money_in_your_account_to_make_this_purchase), "", cancelListenerActive1, true, getString(R.string.credit_wallet), getResources().getString(R.string.setprofilepicture_activity_okay));
                        }


                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if (myStatus == 1) {
                            final String currPricePerItem = o.getString("8");
                            final String currQuantity = o.getString("9");
                            final String currRate = o.getString("10");
                            final String currTotal = o.getString("11");
                            final String currYieldInfo = o.getString("12");
                            final String currRisk = o.getString("13");
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mFinalPricePerShareTextView.setText(getResources().getString(R.string.price_per_item) + " " + currPricePerItem);
                                    mFinalQuantityTextView.setText(getResources().getString(R.string.quantity_colon) + " " + currQuantity);
                                    mFinalRateTextView.setText(getResources().getString(R.string.rate) + " " + currRate);
                                    mFinalTotalTextView.setText(getResources().getString(R.string.total) + " " + currTotal);
                                    mFinalYieldInfoTextView.setText(getResources().getString(R.string.total) + " " + currYieldInfo);
                                    mFinalRiskTextView.setText(getResources().getString(R.string.total) + " " + currRisk);
                                    Config.showDialogType1(BuySharesForSaleActivity.this, "1", getString(R.string.transaction_successful), "", cancelListenerActive1, true, getString(R.string.setprofilepicture_activity_okay), "");
                                    //mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    //mItemHolderConstraintLayout.setVisibility(View.INVISIBLE);
                                    //mFinalHolderConstraintLayout.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                                mItemHolderScrollView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                if(MyLifecycleHandler.isApplicationVisible()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingProgressBar.setVisibility(View.INVISIBLE);
                            mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                            mItemHolderScrollView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

}
