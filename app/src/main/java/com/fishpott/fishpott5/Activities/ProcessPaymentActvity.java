package com.fishpott.fishpott5.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fevziomurtekin.payview.Payview;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

//

public class ProcessPaymentActvity extends AppCompatActivity {

    private String orderId = "", itemName = "", itemQuantity = "", preText = "", amountCedis = "", amountDollars = "", paymentGatewayCurrency = "";
    private int paymentGatewayPriceInCentsOrPesewas = 0;
    private TextView mTitleTextView, mItemDescriptioTextView, mLoaderTextView;
    private TextInputEditText mCardOwnerNameTextView, mCardNumberTextView, mCardMonthTextView, mCardYearTextView, mCardCVVTextView;
    private LinearLayout mCardHolderLinearLayout;
    private ImageView mLoaderImageView;
    private Button mPayButton;
    private Dialog.OnCancelListener cancelListenerActive1;
    private Thread networkThread = null;
    private Boolean networkRequestStarted = false, paymentSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_payment);

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

            if(
                    orderId.trim().equalsIgnoreCase("")
                            || itemName.trim().equalsIgnoreCase("")
                            || itemQuantity.trim().equalsIgnoreCase("")
                            || preText.trim().equalsIgnoreCase("")
                            || amountCedis.trim().equalsIgnoreCase("")
                            || amountDollars.trim().equalsIgnoreCase("")
                            || paymentGatewayCurrency.trim().equalsIgnoreCase("")
                            || paymentGatewayPriceInCentsOrPesewas < 1
            ){
                finish();
            }
        } else {
            finish();
        }

        mTitleTextView = findViewById(R.id.title_bar_title_textview);
        mItemDescriptioTextView = findViewById(R.id.item_name_textview);
        mCardHolderLinearLayout = findViewById(R.id.ll_editviews);
        mCardOwnerNameTextView = findViewById(R.id.tev_card_name);
        mCardNumberTextView = findViewById(R.id.tev_card_no);
        mCardMonthTextView = findViewById(R.id.tev_card_month);
        mCardYearTextView = findViewById(R.id.tev_card_year);
        mCardCVVTextView = findViewById(R.id.tev_card_cv);
        mPayButton = findViewById(R.id.btn_pay);

        // LOADER
        mLoaderImageView = findViewById(R.id.loader_imageview);
        mLoaderTextView = findViewById(R.id.loadertext_textview);

        mTitleTextView.setText("Payment: " + amountCedis + " or " + amountDollars);
        mItemDescriptioTextView.setText(preText.trim() + " " + itemQuantity + " " + itemName + " Stock(s)");


        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Config.openActivity(ProcessPaymentActvity.this, TransactionsActivity.class, 1, 1, 0, "", "");
            }
        };

        mLoaderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!networkRequestStarted && paymentSuccessful){
                    mLoaderTextView.setText("Recording success payment...");
                    mCardHolderLinearLayout.setVisibility(View.INVISIBLE);
                    mLoaderImageView.setVisibility(View.VISIBLE);
                    mLoaderTextView.setVisibility(View.VISIBLE);
                    mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(ProcessPaymentActvity.this, R.anim.suggestion_loading_anim));
                    updateOrderStatus(orderId);
                }
            }
        });
        mLoaderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!networkRequestStarted && paymentSuccessful){
                    mLoaderTextView.setText("Recording success payment...");
                    mCardHolderLinearLayout.setVisibility(View.INVISIBLE);
                    mLoaderImageView.setVisibility(View.VISIBLE);
                    mLoaderTextView.setVisibility(View.VISIBLE);
                    mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(ProcessPaymentActvity.this, R.anim.suggestion_loading_anim));
                    updateOrderStatus(orderId);
                }
            }
        });
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(
                                !mCardOwnerNameTextView.getText().toString().trim().equalsIgnoreCase("")
                                && !mCardNumberTextView.getText().toString().trim().equalsIgnoreCase("")
                                && !mCardMonthTextView.getText().toString().trim().equalsIgnoreCase("")
                                && !mCardYearTextView.getText().toString().trim().equalsIgnoreCase("")
                                && !mCardCVVTextView.getText().toString().trim().equalsIgnoreCase("")
                                && !mPayButton.getText().toString().trim().equalsIgnoreCase("")
                ) {
                    // This sets up the card and check for validity
                    // This is a test card from paystack
                    String cardNumber = mCardNumberTextView.getText().toString().trim();
                    int expiryMonth = Integer.parseInt(mCardMonthTextView.getText().toString().trim()); //any month in the future
                    int expiryYear = Integer.parseInt(mCardYearTextView.getText().toString().trim()); // any year in the future. '2018' would work also!
                    String cvv = mCardCVVTextView.getText().toString().trim();  // cvv of the test card

                    Card card = new Card(cardNumber, expiryMonth, expiryYear, cvv);
                    if (card.isValid()) {
                        mCardHolderLinearLayout.setVisibility(View.INVISIBLE);
                        mLoaderImageView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setVisibility(View.VISIBLE);
                        mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(ProcessPaymentActvity.this, R.anim.suggestion_loading_anim));
                        mLoaderTextView.setText("Verifying card...");

                        //PaystackSdk.chargeCard chargeCard = new PaystackSdk.chargeCard();
                        Charge charge = new Charge();
                        charge.setCurrency(paymentGatewayCurrency);
                        //charge.setPlan();
                        //charge.setSubaccount();
                        //charge.setTransactionCharge();
                        charge.setAmount(paymentGatewayPriceInCentsOrPesewas);
                        charge.setEmail(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_EMAIL));
                        charge.setReference(orderId);
                        charge.setCard(card);
                        //charge.setBearer();
                        //charge.putMetadata();

                        PaystackSdk.chargeCard(ProcessPaymentActvity.this, charge, new Paystack.TransactionCallback() {
                            @Override
                            public void onSuccess(Transaction transaction) {
                                // DELAYING getLatestSuggestion FOR 5S FOR ANIM TO RUN
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        paymentSuccessful = true;
                                        if(!networkRequestStarted){
                                            mLoaderTextView.setText("Recording success payment...");
                                            updateOrderStatus(orderId);
                                        }
                                    }
                                }, 5000);
                                Config.showToastType1(ProcessPaymentActvity.this, "SUCCESS PAYMENT");

                            }

                            @Override
                            public void beforeValidate(Transaction transaction) {
                                Config.showToastType1(ProcessPaymentActvity.this, "beforeValidate PAYMENT");
                            }

                            @Override
                            public void onError(Throwable error, Transaction transaction) {
                                Config.showToastType1(ProcessPaymentActvity.this, "Payment failed. Please use a different card");
                                /*
                                paymentSuccessful = true;
                                if(!networkRequestStarted){
                                    mLoaderTextView.setText("Recording failed payment...");
                                    updateOrderStatus(orderId);
                                }
                                */
                            }
                        });

                        //Config.showToastType1(ProcessPaymentActvity.this, "CARD READY FOR PAY");
                    } else {
                        Config.showToastType1(ProcessPaymentActvity.this, "Card is invalid. Please check the details.");
                    }
                } else {
                    Config.showToastType1(ProcessPaymentActvity.this, "Please complete the form");
                }
            }
        });

    }



    private void updateOrderStatus(String thisOrderID){
        networkRequestStarted = true;
        Log.e("getFinalPriceSummary", "thisOrderID: " + thisOrderID);

        AndroidNetworking.post(Config.LINK_UPDATE_ORDER_STATUS)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(ProcessPaymentActvity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(ProcessPaymentActvity.this.getApplicationContext())))
                .addBodyParameter("item_type", "stockpurchase")
                .addBodyParameter("item_id", thisOrderID)
                .addBodyParameter("payment_gateway_status", "1")
                .addBodyParameter("payment_gateway_info", "pai")
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
                    Config.setSharedPreferenceBoolean(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){

                        if(!ProcessPaymentActvity.this.isFinishing()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    cancelListenerActive1 = Config.showDialogType1(ProcessPaymentActvity.this, "", "Payment successful. Your order is under review. Please record this transaction ID : " + thisOrderID, "show-positive-image", cancelListenerActive1, true, getString(R.string.setprofilepicture_activity_okay), "");


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
                        Config.setSharedPreferenceBoolean(ProcessPaymentActvity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(ProcessPaymentActvity.this, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mLoaderImageView.clearAnimation();
                        mLoaderImageView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setText("Click icon to retry payment recording...");
                        mCardHolderLinearLayout.setVisibility(View.INVISIBLE);
                        Config.showToastType1(ProcessPaymentActvity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(ProcessPaymentActvity.this, myStatusMessage);
                        Config.signOutUser(ProcessPaymentActvity.this.getApplicationContext(), true, ProcessPaymentActvity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(ProcessPaymentActvity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!ProcessPaymentActvity.this.isFinishing()){
                        mLoaderImageView.clearAnimation();
                        mLoaderImageView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setVisibility(View.VISIBLE);
                        mLoaderTextView.setText("Click icon to retry payment recording...");
                        mCardHolderLinearLayout.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(ProcessPaymentActvity.this, getString(R.string.failed_check_your_internet_and_try_again));
                if(!ProcessPaymentActvity.this.isFinishing()){
                    mLoaderImageView.clearAnimation();
                    mLoaderImageView.setVisibility(View.VISIBLE);
                    mLoaderTextView.setVisibility(View.VISIBLE);
                    mLoaderTextView.setText("Click icon to retry payment recording...");
                    mCardHolderLinearLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
