package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Views.CircleImageView;

import com.fishpott.fishpott5.R;
import java.util.ArrayList;
import java.util.List;

public class BuyBusinessStockSuggestedActivity extends AppCompatActivity {

    private String suggestionBusinessID = "", shareLogo = "", shareParentID = "", shareName = "",  = "", shareQuantity = "",
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

    }
}