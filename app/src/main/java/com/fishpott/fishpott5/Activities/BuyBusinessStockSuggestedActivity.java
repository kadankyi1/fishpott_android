package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.content.Context;
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
    private EditText mHowMuchToInvestEditText, mPasswordEditText;
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

        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mSharesLogoCircleImageView = findViewById(R.id.shares_for_sale_logo_circleimageview);
        mShareNameTextView = findViewById(R.id.share_name_textview);

        mItemHolderScrollView = findViewById(R.id.contents_holder);
        mHowMuchToInvestEditText = findViewById(R.id.amount_sent_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);
        mRiskTextView = findViewById(R.id.risk_input_textview);

        // LOADING THE LOGO IMAGE
        Config.loadUrlImage(BuyBusinessStockSuggestedActivity.this, true, shareLogo, mSharesLogoCircleImageView, 0, 60, 60);

        // SETTING THE BUSINESS NAME
        mShareNameTextView.setText(shareName);

        // SETTING ONCLICK LISTENERS
        mRiskTextView.setOnClickListener(this);

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
        }
    }



    private void getFinalInvestmentPriceSummary(Context context, String theDrillID, String theAnswerNumber){
        networkRequestStarted = true;

        AndroidNetworking.post(Config.LINK_SAVE_DRILL_AND_GET_ANSWERS_COUNT)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(BuyBusinessStockSuggestedActivity.this.getApplicationContext())))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(BuyBusinessStockSuggestedActivity.this))
                .addBodyParameter("drill_id", theDrillID)
                .addBodyParameter("drill_answer", theAnswerNumber)
                .setTag("save_drill_answer")
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
                    final String drill_next_one_time = o.getJSONObject("data").getString("drill_next_one_time");
                    final String answer1 = o.getJSONObject("data").getString("answer_1");
                    final String answer2 = o.getJSONObject("data").getString("answer_2");
                    final String answer3 = o.getJSONObject("data").getString("answer_3");
                    final String answer4 = o.getJSONObject("data").getString("answer_4");
                    final String answer1Count = o.getJSONObject("data").getString("answer_1_count");
                    final String answer2Count = o.getJSONObject("data").getString("answer_2_count");
                    final String answer3Count = o.getJSONObject("data").getString("answer_3_count");
                    final String answer4Count = o.getJSONObject("data").getString("answer_4_count");

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(BuyBusinessStockSuggestedActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){

                        Config.showToastType1(BuyBusinessStockSuggestedActivity.this, myStatusMessage);
                        if(MyLifecycleHandler.isApplicationInForeground()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    /*
                                    mSuggestionLoaderImageView.clearAnimation();
                                    mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                                    mSuggestionLoaderTextTextView.setVisibility(View.INVISIBLE);
                                    mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                                    mNextDrillTextView.setText(drill_next_one_time);
                                    mAnswer1CountTextView.setText(answer1Count);
                                    mAnswer1TextView.setText(answer1);
                                    mAnswer2CountTextView.setText(answer2Count);
                                    mAnswer2TextView.setText(answer2);
                                    mAnswer3CountTextView.setText(answer3Count);
                                    mAnswer3TextView.setText(answer3);
                                    mAnswer4CountTextView.setText(answer4Count);
                                    mAnswer4TextView.setText(answer4);
                                    mAnswersCountScrollView.setVisibility(View.VISIBLE);
                                     */
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
                        //mSuggestionLoaderImageView.clearAnimation();
                        //mSuggestionLoaderTextTextView.setText("Click the icon to get your next suggestion");
                        //Config.showToastType1(getActivity(), myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        //Config.showToastType1(getActivity(), myStatusMessage);
                        //Config.signOutUser(getActivity().getApplicationContext(), false, null, null, 0, 2);
                    } else if(myStatus == 5){
                        //Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        //Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(MyLifecycleHandler.isApplicationInForeground()){
                            /*
                            ADD XML TO FRONT SO USER CAN CLICK TO TRY AGAIN IF IT FAILS
                             */
                    } else {
                        //networkResponse = getString(R.string.login_activity_an_unexpected_error_occured);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                if(MyLifecycleHandler.isApplicationInForeground()){
                            /*
                            ADD XML TO FRONT SO USER CAN CLICK TO TRY AGAIN IF IT FAILS
                             */
                } else {
                    //networkResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                }
            }
        });
    }
}