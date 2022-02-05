package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
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
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import com.fishpott.fishpott5.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuyBusinessStockSuggestedActivity extends AppCompatActivity implements View.OnClickListener {

    private String suggestionBusinessID = "", shareLogo = "", shareParentID = "", shareName = "", shareQuantity = "",
            receiverPottName = "", finalRiskType = "", finalQuantity = "", finalPassword = "", networkResponse = "",
            orderID = "", amountCedis = "", amountDollars = "", paymentGatewayCurrency = "";
    private int shareQuantityInt = 0, selectedRiskIndex = 0, finalPurchaseStatus = 0, paymentGatewayAmount;
    private ImageView mBackImageView, mLoaderImageView;
    private ScrollView mItemHolderScrollView, mFinalHolderScrollView;
    private Button mBuyButton, mGetPriceButton, mResetButton;
    private TextInputLayout mQuantityTextInputLayout;
    private CircleImageView mSharesLogoCircleImageView;
    private ProgressBar mLoadingProgressBar;
    private String[] riskNames;
    private TextView mLoaderTextView, mShareNameTextView, mFinalPricePerShareTextView, mFinalQuantityTextView, mInsuranceFeeTextView, mProcessingFeeTextView,
            mFinalRateTextView, mFinalTotalTextView, mFinalYieldInfoTextView, mRiskTextView, mFinalRiskTextView, mFinalItemNameTextView,
            mTermAndConditionsTextView;
    private List<String> sharesNamesStringArrayList = new ArrayList<>();
    private EditText mHowMuchToInvestEditText;
    private Thread imageLoaderThread = null, networkThread = null;
    private NumberPicker.OnValueChangeListener mSharesSetListener;
    private Dialog.OnCancelListener cancelListenerActive1;
    private Boolean networkRequestStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_business_stock_suggested);

        // GETTING THE BUSINESS/STOCK INFO FROM THE PREVIOUS PAGE
        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle !=null) {
            String[] buyInfo =(String[]) intentBundle.get("BUY_INFO");
            suggestionBusinessID = buyInfo[0];
            shareLogo = buyInfo[1];
            shareName = buyInfo[2].trim();
            Log.e("BuySharesForSaleActivit", "suggestionBusinessID : " + suggestionBusinessID);
            Log.e("BuySharesForSaleActivit", "shareLogo : " + shareLogo);
            Log.e("BuySharesForSaleActivit", "shareName : " + shareName);
            if(suggestionBusinessID.trim().equalsIgnoreCase("") || shareName.trim().equalsIgnoreCase("") || shareLogo.trim().equalsIgnoreCase("")){
                Config.showToastType1(BuyBusinessStockSuggestedActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
                finish();
            }
        } else {
            Config.showToastType1(BuyBusinessStockSuggestedActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
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

        // NAVIGATION BAR
        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mSharesLogoCircleImageView = findViewById(R.id.shares_for_sale_logo_circleimageview);
        mShareNameTextView = findViewById(R.id.share_name_textview);

        // LOADER
        mLoaderImageView = findViewById(R.id.loader_imageview);
        mLoaderTextView = findViewById(R.id.loadertext_textview);

        // GET PRICE VIEW
        mItemHolderScrollView = findViewById(R.id.contents_holder);
        mHowMuchToInvestEditText = findViewById(R.id.amount_sent_edittext);
        mRiskTextView = findViewById(R.id.risk_input_textview);
        mGetPriceButton = findViewById(R.id.price_button);

        // FINAL PRICE BUY VIEW
        mFinalHolderScrollView = findViewById(R.id.contents_holder2);
        mFinalItemNameTextView = findViewById(R.id.share_name_textview2);
        mFinalPricePerShareTextView = findViewById(R.id.price_per_share_textview);
        mFinalQuantityTextView = findViewById(R.id.quantity_textview);
        mFinalRateTextView = findViewById(R.id.rate_textview);
        mFinalRiskTextView = findViewById(R.id.risk_textview);
        mInsuranceFeeTextView = findViewById(R.id.risk_fee_textview);
        mProcessingFeeTextView = findViewById(R.id.processing_fee_textview);
        mFinalTotalTextView = findViewById(R.id.total_textview);
        mFinalYieldInfoTextView = findViewById(R.id.agree_checkbox);
        mTermAndConditionsTextView = findViewById(R.id.t_and_c_textview);
        mResetButton = findViewById(R.id.reset_button);
        mBuyButton = findViewById(R.id.request_button);

        // LOADING THE LOGO IMAGE
        Config.loadUrlImage(BuyBusinessStockSuggestedActivity.this, true, shareLogo, mSharesLogoCircleImageView, 0, 60, 60);

        // SETTING THE BUSINESS NAME
        mShareNameTextView.setText(shareName);

        // SETTING ONCLICK LISTENERS
        mRiskTextView.setOnClickListener(this);
        mGetPriceButton.setOnClickListener(this);
        mTermAndConditionsTextView.setOnClickListener(this);
        mResetButton.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mBuyButton.setOnClickListener(this);

        // SETTING RISK LISTENER DATA
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
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mRiskTextView.getId()) {
            mSharesSetListener = Config.openNumberPickerForCountries(BuyBusinessStockSuggestedActivity.this, mSharesSetListener, 0, sharesNamesStringArrayList.size() - 1, true, riskNames, selectedRiskIndex);
        } else if(view.getId() ==  mGetPriceButton.getId() && !mHowMuchToInvestEditText.getText().toString().equalsIgnoreCase("") && !finalRiskType.equalsIgnoreCase("")){
            mItemHolderScrollView.setVisibility(View.INVISIBLE);
            mFinalHolderScrollView.setVisibility(View.INVISIBLE);
            mLoaderImageView.setVisibility(View.VISIBLE);
            mLoaderTextView.setVisibility(View.VISIBLE);
            mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(BuyBusinessStockSuggestedActivity.this, R.anim.suggestion_loading_anim));
            mLoaderTextView.setText("Getting final price summary...");
            // DELAYING getLatestSuggestion FOR 5S FOR ANIM TO RUN
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getFinalInvestmentPriceSummary(suggestionBusinessID, mHowMuchToInvestEditText.getText().toString(), finalRiskType);
                }
            }, 5000);
        } else if (view.getId() == mBackImageView.getId()) {
            BuyBusinessStockSuggestedActivity.this.onBackPressed();
        } else if (view.getId() == mTermAndConditionsTextView.getId()) {
            Config.openActivity(BuyBusinessStockSuggestedActivity.this, WebViewActivity.class, 0, 0, 1, Config.WEBVIEW_KEY_URL, Config.FISHPOTT_TERMS_OF_SERVICE);
        } else if (view.getId() == mResetButton.getId()) {
            mFinalHolderScrollView.setVisibility(View.INVISIBLE);
            mItemHolderScrollView.setVisibility(View.VISIBLE);
        } else if(view.getId() == mBuyButton.getId()){

            Log.e("mBuyButton", "orderID: " + orderID);
            Log.e("mBuyButton", "shareName: " + shareName);
            Log.e("mBuyButton", "shareQuantity: " + shareQuantity);
            Log.e("mBuyButton", "amountCedis: " + amountCedis);
            Log.e("mBuyButton", "amountDollars: " + amountDollars);
            Log.e("mBuyButton", "paymentGatewayCurrency: " + paymentGatewayCurrency);
            Log.e("mBuyButton", "paymentGatewayAmount: " + paymentGatewayAmount);

            if(
                    !orderID.trim().equalsIgnoreCase("")
                            && !shareName.trim().equalsIgnoreCase("")
                            && !shareQuantity.trim().equalsIgnoreCase("")
                            && !amountCedis.trim().equalsIgnoreCase("")
                            && !amountDollars.trim().equalsIgnoreCase("")
                            && !paymentGatewayCurrency.trim().equalsIgnoreCase("")
                            && paymentGatewayAmount > 0
            ){
                String[] orderDetails = {orderID, shareName, shareQuantity, "Buying", amountCedis, amountDollars, paymentGatewayCurrency, String.valueOf(paymentGatewayAmount)};
                Config.openActivity4(BuyBusinessStockSuggestedActivity.this, ProcessPaymentActvity.class, 1, 1, 1, "ORDER_DETAILS", orderDetails);
            } else {
                Config.showToastType1(BuyBusinessStockSuggestedActivity.this, "Order error. Please go back and restart the process");
            }
            //Config.openActivity(BuyBusinessStockSuggestedActivity.this, ProcessPaymentActvity.class, 1, 0, 0, "", "");
        }
    }



    private void getFinalInvestmentPriceSummary(String theBusinessID, String theBuyAmt, String theRiskInsurance){
        networkRequestStarted = true;
        Log.e("getFinalPriceSummary", "suggestionBusinessID: " + suggestionBusinessID);
        Log.e("getFinalPriceSummary", "theBuyAmt: " + theBuyAmt);
        Log.e("getFinalPriceSummary", "theRiskInsurance: " + theRiskInsurance);

        AndroidNetworking.post(Config.LINK_GET_FINAL_PRICE_SUMMARY)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(BuyBusinessStockSuggestedActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(BuyBusinessStockSuggestedActivity.this.getApplicationContext())))
                .addBodyParameter("business_id", theBusinessID)
                .addBodyParameter("investment_amt_in_dollars", theBuyAmt)
                .addBodyParameter("investment_risk_protection", theRiskInsurance)
                .setTag("get_final_price")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                networkRequestStarted = false;

                try {
                    Log.e("save_drill_answer", response);
                    final JSONObject o = new JSONObject(response);
                    int myStatus = o.getInt("status");
                    final String myStatusMessage = o.getString("message");

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){
                        final String itemName = o.getJSONObject("data").getString("item");
                        final String pricePerItem = o.getJSONObject("data").getString("price_per_item");
                        final String quantity = o.getJSONObject("data").getString("quantity");
                        shareQuantity =  quantity;
                        final String rate = o.getJSONObject("data").getString("rate");
                        final String risk = o.getJSONObject("data").getString("risk");
                        final String riskStatement = o.getJSONObject("data").getString("risk_statement");
                        final String riskInsuranceFee = o.getJSONObject("data").getString("risk_insurance_fee");
                        final String processingFee = o.getJSONObject("data").getString("processing_fee");
                        final String overallTotalUsd = o.getJSONObject("data").getString("overall_total_usd");
                        amountDollars = overallTotalUsd;
                        final String overallTotalLocalCurrency = o.getJSONObject("data").getString("overall_total_local_currency");
                        amountCedis = overallTotalLocalCurrency;
                        final String financialYieldInfo = o.getJSONObject("data").getString("financial_yield_info");
                        orderID = o.getJSONObject("data").getString("order_id");
                        paymentGatewayCurrency = o.getJSONObject("data").getString("payment_gateway_currency");
                        paymentGatewayAmount = o.getJSONObject("data").getInt("payment_gateway_amount_in_pesewas_or_cents_intval");
                        //Config.showToastType1(BuyBusinessStockSuggestedActivity.this, myStatusMessage);
                        if(!BuyBusinessStockSuggestedActivity.this.isFinishing()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mLoaderImageView.clearAnimation();
                                    mLoaderImageView.setVisibility(View.INVISIBLE);
                                    mLoaderTextView.setVisibility(View.INVISIBLE);
                                    mItemHolderScrollView.setVisibility(View.INVISIBLE);

                                    mFinalItemNameTextView.setText("Item: " + itemName);
                                    mFinalPricePerShareTextView.setText("Price Per Item: " + pricePerItem);
                                    mFinalQuantityTextView.setText("Stocks Quantity: " + quantity);
                                    mFinalRateTextView.setText("Exchange Rate: " + rate);
                                    mFinalRiskTextView.setText("Risk Statement: " + riskStatement);
                                    mInsuranceFeeTextView.setText("Risk Insurance Fee: " + riskInsuranceFee);
                                    mProcessingFeeTextView.setText("Processing Fee: " + processingFee);
                                    mFinalTotalTextView.setText("Final Charge: " + overallTotalUsd + " OR " + overallTotalLocalCurrency);
                                    mFinalYieldInfoTextView.setText(financialYieldInfo);
                                    mFinalHolderScrollView.setVisibility(View.VISIBLE);

                                }
                            });
                        }
                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(BuyBusinessStockSuggestedActivity.this, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mLoaderImageView.clearAnimation();
                        mLoaderImageView.setVisibility(View.INVISIBLE);
                        mLoaderTextView.setVisibility(View.INVISIBLE);
                        mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                        mItemHolderScrollView.setVisibility(View.VISIBLE);
                        Config.showToastType1(BuyBusinessStockSuggestedActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(BuyBusinessStockSuggestedActivity.this, myStatusMessage);
                        Config.signOutUser(BuyBusinessStockSuggestedActivity.this.getApplicationContext(), true, BuyBusinessStockSuggestedActivity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(BuyBusinessStockSuggestedActivity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!BuyBusinessStockSuggestedActivity.this.isFinishing()){
                        mLoaderImageView.clearAnimation();
                        mLoaderImageView.setVisibility(View.INVISIBLE);
                        mLoaderTextView.setVisibility(View.INVISIBLE);
                        mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                        mItemHolderScrollView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(BuyBusinessStockSuggestedActivity.this, getString(R.string.failed_check_your_internet_and_try_again));
                if(!BuyBusinessStockSuggestedActivity.this.isFinishing()){
                    mLoaderImageView.clearAnimation();
                    mLoaderImageView.setVisibility(View.INVISIBLE);
                    mLoaderTextView.setVisibility(View.INVISIBLE);
                    mFinalHolderScrollView.setVisibility(View.INVISIBLE);
                    mItemHolderScrollView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}