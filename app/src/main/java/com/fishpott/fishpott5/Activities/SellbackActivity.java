package com.fishpott.fishpott5.Activities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.TransactionsListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.TransactionModel;
import com.fishpott.fishpott5.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SellbackActivity extends AppCompatActivity implements View.OnClickListener{

    private SwipeRefreshLayout mReloadSharesSwipeRefreshLayout;
    private ConstraintLayout mLoaderHolderConstraintLayout, mTransferFormHolderConstraintLayout;
    private TextView mLoaderTextView, mSharesListTextView, mFeeInfoTextView, mTransferInfoTextView;
    private ImageView mLoaderImageView, mBackImageView;
    private Boolean networkRequestStarted = false;
    private Thread transferThread = null;
    private EditText mQuantityEditText, mReceiverPottNameEditText, mPasswordEditText, mBankMomoNetworkNameEditText, mAccNameEditText,
            mAccNumberEditText, mRoutingNumberEditText;
    private Button mTransferButton;
    private TextInputLayout mQuantityEditTextTextInputLayout;
    private int selectedSharesIndex = 0, transferfeeInt = 0, rateToMultiplyBy = 0;
    private Double selectedSharesMaxPrice = 0.00;
    private String localCurrencySign = "", rateFromDollar = "", transferFee = "", selectedSharesName = "", selectedSharesId = "", selectedSharesAvailableQuantity = "", selectedSharesCostPrice = "", networkResponse = "";
    private String[] chosenSharesInfo = {"sId", "sName", "sSellQuantity", "sSellPrice", ""};
    private String[] sharesNamesStringArraySet;
    private List<String> sharesNamesStringArrayList = new ArrayList<>();
    private List<String> sharesIdStringArrayList = new ArrayList<>();
    private List<String> sharesAvailableQuantityStringArrayList = new ArrayList<>();
    private List<String> shareCostPricesStringArrayList = new ArrayList<>();
    private List<Double> sharesMaxPriceStringArrayList = new ArrayList<>();
    private NumberPicker.OnValueChangeListener mSharesSetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellback);

        mReloadSharesSwipeRefreshLayout = findViewById(R.id.swipe_reloadshares_swiperefreshrelayout);
        mBackImageView =  findViewById(R.id.title_bar_back_icon_imageview);

        mLoaderHolderConstraintLayout = findViewById(R.id.stage_2_constraintlayout);
        mLoaderTextView = findViewById(R.id.fragment_suggestion_loadertext_textview);
        mLoaderImageView = findViewById(R.id.fragment_suggestion_loader_imageview);

        mTransferFormHolderConstraintLayout = findViewById(R.id.stage_1_constraintlayout);
        mSharesListTextView = findViewById(R.id.fragment_loadsharesforposting_chosen_textview);
        mQuantityEditTextTextInputLayout = findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text_layout_holder);
        mQuantityEditText = findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text);
        mReceiverPottNameEditText = findViewById(R.id.fragment_loadsharesforposting_receiverpottname_edit_text);
        mPasswordEditText = findViewById(R.id.fragment_loadsharesforposting_password_edittext);
        mFeeInfoTextView = findViewById(R.id.fragment_loadsharesforposting_sharesinfo_textview);
        mTransferInfoTextView = findViewById(R.id.fragment_loadsharesforposting_feeinfo_textview);
        mTransferButton = findViewById(R.id.fragment_loadsharesforposting_addsharestopost_button);

        mBankMomoNetworkNameEditText = findViewById(R.id.network_name_edittext);
        mAccNameEditText = findViewById(R.id.account_name_edittext);
        mAccNumberEditText = findViewById(R.id.account_number_edittext);
        mRoutingNumberEditText = findViewById(R.id.routing_number_edittext);

        mReloadSharesSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                transferThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getMyShares(getApplicationContext());
                    }
                });
                transferThread.start();
            }
        });


        mSharesSetListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                selectedSharesIndex = newVal;
                if(selectedSharesIndex > 0){
                    selectedSharesName = sharesNamesStringArrayList.get(newVal);
                    mSharesListTextView.setText(selectedSharesName);
                    selectedSharesId = sharesIdStringArrayList.get(newVal);
                    selectedSharesAvailableQuantity = sharesAvailableQuantityStringArrayList.get(newVal);
                    selectedSharesCostPrice = shareCostPricesStringArrayList.get(newVal);
                    //mPricePerShareTextInputLayout.setHint(getString(R.string.price_per_share) + " " + getString(R.string.cost_price_per_share) + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + selectedSharesCostPrice  + ")");
                    mQuantityEditTextTextInputLayout.setHint("Quantity To Sellback" + " (" + selectedSharesAvailableQuantity + " " + getString(R.string.available) +  ")");
                    selectedSharesMaxPrice = sharesMaxPriceStringArrayList.get(newVal);
                    setSharesInfo();
                } else {
                    mSharesListTextView.setText(getString(R.string.choose_shares));
                    mQuantityEditText.setHint(getString(R.string.quantity_to_transfer));
                    selectedSharesName = "";
                    selectedSharesId = "";
                    selectedSharesAvailableQuantity = "";
                    selectedSharesCostPrice = "";
                    selectedSharesMaxPrice = 0.00;
                    mFeeInfoTextView.setText("");
                    mTransferInfoTextView.setText("");
                }
            }
        };


        mQuantityEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {setSharesInfo();}
            @Override public void afterTextChanged(Editable s) {}
        });

        mBackImageView.setOnClickListener(this);
        mSharesListTextView.setOnClickListener(this);
        mLoaderTextView.setOnClickListener(this);
        mLoaderImageView.setOnClickListener(this);
        mTransferButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mLoaderTextView.getId() || v.getId() == mLoaderImageView.getId()){
            getMyShares(getApplicationContext());
        } else if(v.getId() == mSharesListTextView.getId()){
            mSharesSetListener = Config.openNumberPickerForCountries(SellbackActivity.this, mSharesSetListener, 0, sharesNamesStringArrayList.size()-1, true, sharesNamesStringArraySet, selectedSharesIndex);
        } else if(v.getId() == mBackImageView.getId()){
            onBackPressed();
        } else if(v.getId() == mTransferButton.getId()){

            if(             mBankMomoNetworkNameEditText.getText().toString().trim().length() > 1
                            && mAccNameEditText.getText().toString().trim().length() > 1
                            && mAccNumberEditText.getText().toString().trim().length() > 1
                            && mPasswordEditText.getText().toString().trim().length() > 1 ){

                final String accName = mAccNameEditText.getText().toString().trim();
                final String accNumber = mAccNumberEditText.getText().toString().trim();
                final String routingNumber = mRoutingNumberEditText.getText().toString().trim();
                final String password = mPasswordEditText.getText().toString().trim();


                Thread networkRequestThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sellBackShares(getApplicationContext(), mPasswordEditText.getText().toString().trim(), mQuantityEditText.getText().toString().trim(), mBankMomoNetworkNameEditText.getText().toString().trim(), mAccNameEditText.getText().toString().trim(), mAccNumberEditText.getText().toString().trim(), mRoutingNumberEditText.getText().toString().trim());
                    }
                });
                networkRequestThread.start();
            } else {
                Config.showToastType1(SellbackActivity.this, getResources().getString(R.string.the_form_is_incomplete));
            }
        }
    }


    private Boolean setSharesInfo(){
        Log.e("setSharesInfo", "selectedSharesName: " + selectedSharesName);
        Log.e("setSharesInfo", "selectedSharesId: " + selectedSharesId);
        Log.e("setSharesInfo", "selectedSharesAvailableQuantity: " + selectedSharesAvailableQuantity);
        Log.e("setSharesInfo", "selectedSharesCostPrice: " + selectedSharesCostPrice);
        Log.e("setSharesInfo", "selectedSharesMaxPrice: " + selectedSharesMaxPrice);

        if(!selectedSharesName.equalsIgnoreCase("") && !selectedSharesId.equalsIgnoreCase("")
                && !selectedSharesAvailableQuantity.equalsIgnoreCase("") && !selectedSharesCostPrice.equalsIgnoreCase("")){
            if(!mQuantityEditText.getText().toString().equalsIgnoreCase("")){

                String mySellQuantityString = mQuantityEditText.getText().toString().toString().trim();

                int mySellQuantityInt = Integer.valueOf(mySellQuantityString);
                //float myTotalCostPriceFloat = mySellQuantityInt * Float.valueOf(selectedSharesCostPrice);

                if(mySellQuantityInt > 0 && mySellQuantityInt <= Integer.valueOf(selectedSharesAvailableQuantity)){
                    if(selectedSharesMaxPrice > 0){
                        Double payOut = mySellQuantityInt * selectedSharesMaxPrice;
                        String payOutString = localCurrencySign + String.valueOf(payOut);

                        mTransferInfoTextView.setText("You are selling back " + mySellQuantityString + " " + selectedSharesName + ". You will be paid back " + payOutString + ". Please note that this amount is subject to change and you will be notified before processing if that happens. If you agree with the trade, simply type in your password and click sellback.");
                    } else {
                        mTransferInfoTextView.setText("");
                        Config.showToastType1(SellbackActivity.this, "This business is currently not buying back shares. You will be notified when they are ready");
                    }
                    /*
                    if(transferFee.trim().equalsIgnoreCase("")){
                        mFeeInfoTextView.setText("Transfer Center is currently not ready. Please swipe down to refresh. If this continues, please try again later.");
                    } else {
                        mFeeInfoTextView.setText("Transfer fee:" + transferFee + ". You will be directed to make payment.");
                    }
                     */
                    return true;
                } else if(mySellQuantityInt > Integer.valueOf(selectedSharesAvailableQuantity)){
                    mTransferInfoTextView.setText("");
                    Config.showToastType1(SellbackActivity.this, getString(R.string.reduce_the_quantity_of_shares_to_transfer_it_cannot_be_more_than_what_you_have));
                }
            } else {
                Config.showToastType1(SellbackActivity.this, getString(R.string.fill_in_the_quantity_to_transfer));
            }
        }

        return false;
    }

    private void getMyShares(Context context){
        networkRequestStarted = true;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mTransferFormHolderConstraintLayout.setVisibility(View.INVISIBLE);
                mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(SellbackActivity.this, R.anim.suggestion_loading_anim));
                mLoaderTextView.setText("Getting your shares...");
            }
        });

        AndroidNetworking.post(Config.LINK_GET_MY_SHARES)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(SellbackActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(SellbackActivity.this.getApplicationContext())))
                .setTag("get_my_shares")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("GetSuggestion", response);
                networkRequestStarted = false;

                try {
                    final JSONObject o = new JSONObject(response);
                    int myStatus = o.getInt("status");
                    final String myStatusMessage = o.getString("message");

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){
                        transferFee =  o.getString("transfer_fee_local_with_sign");
                        transferfeeInt =  o.getInt("transfer_fee_local");
                        rateFromDollar =  o.getString("rate");
                        rateToMultiplyBy =  o.getInt("rate_no_sign");
                        localCurrencySign =  o.getString("local_currency");
                        JSONArray linkupsSuggestionsArray = new JSONObject(response).getJSONArray("data");
                        // LIST RESULTS SETTING COMES HERE
                        if(linkupsSuggestionsArray.length() > 0){

                            sharesNamesStringArrayList.clear();
                            sharesIdStringArrayList.clear();
                            sharesAvailableQuantityStringArrayList.clear();
                            shareCostPricesStringArrayList.clear();
                            sharesMaxPriceStringArrayList.clear();
                            sharesNamesStringArrayList.add(getString(R.string.choose_shares));
                            sharesIdStringArrayList.add("");
                            sharesAvailableQuantityStringArrayList.add("");
                            shareCostPricesStringArrayList.add("");
                            sharesMaxPriceStringArrayList.add(0.00);

                            for (int i = 0; i < linkupsSuggestionsArray.length(); i++){
                                final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                sharesNamesStringArrayList.add(k.getString("business_name"));
                                sharesIdStringArrayList.add(k.getString("stock_id"));
                                sharesAvailableQuantityStringArrayList.add(k.getString("quantity_of_stocks"));
                                shareCostPricesStringArrayList.add(k.getString("cost_per_share_usd"));
                                sharesMaxPriceStringArrayList.add(k.getDouble("buyback_local"));

                                if(i == linkupsSuggestionsArray.length()-1){
                                    sharesNamesStringArraySet = new String[sharesNamesStringArrayList.size()];
                                    sharesNamesStringArraySet = sharesNamesStringArrayList.toArray(sharesNamesStringArraySet);
                                }
                            }

                            if(!isFinishing()){
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoaderImageView.clearAnimation();
                                        mLoaderTextView.setText("...");
                                        mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                                        mTransferFormHolderConstraintLayout.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        } else {
                            mLoaderImageView.clearAnimation();
                            mLoaderTextView.setText("...");
                            mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                            mTransferFormHolderConstraintLayout.setVisibility(View.INVISIBLE);
                            Config.showDialogType1(SellbackActivity.this, "", "You have no shares to sell-back", "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                        }

                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mLoaderImageView.clearAnimation();
                        mLoaderTextView.setText("Failed. Click to try again");
                        Config.showToastType1(SellbackActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(SellbackActivity.this, myStatusMessage);
                        Config.signOutUser(getApplicationContext(), true, SellbackActivity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(SellbackActivity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!isFinishing()){
                        mLoaderImageView.clearAnimation();
                        mLoaderTextView.setText("Failed. Click to try again");
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(SellbackActivity.this, getString(R.string.failed_check_your_internet_and_try_again));
                if(!isFinishing()){
                    mTransferFormHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                    mLoaderImageView.clearAnimation();
                    mLoaderTextView.setText("Failed. Click the icon to load your shares");
                }
            }
        });
    }


    private void sellBackShares(Context context, String userPassword, String transferQuantity, String bankOrNetworkName, String accName, String accNumber, String routingNumber){
        Log.e("selectedSharesId", selectedSharesId);
        Log.e("transferQuantity", transferQuantity);

        if(     selectedSharesId.equalsIgnoreCase("")
                || transferQuantity.trim().equalsIgnoreCase("")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Config.showToastType1(SellbackActivity.this, "The form is incomplete.");
                }
            });
            return;
        }

        if(Integer.valueOf(transferQuantity) < 1) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Config.showToastType1(SellbackActivity.this, "The form is incomplete.");
                }
            });
            return;
        }

        networkRequestStarted = true;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mTransferFormHolderConstraintLayout.setVisibility(View.INVISIBLE);
                mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(SellbackActivity.this, R.anim.suggestion_loading_anim));
                mLoaderTextView.setText("Setting payment portal...");
            }
        });

        AndroidNetworking.post(Config.LINK_SELLBACK_STOCKS)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(SellbackActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(SellbackActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(SellbackActivity.this.getApplicationContext())))
                .addBodyParameter("user_password", userPassword)
                .addBodyParameter("stockownership_id", selectedSharesId)
                .addBodyParameter("transfer_quantity", transferQuantity)
                .addBodyParameter("bank_or_network_name", bankOrNetworkName)
                .addBodyParameter("acc_name", accName)
                .addBodyParameter("acc_number", accNumber)
                .addBodyParameter("routing_number", routingNumber)
                .setTag("get_my_shares")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("GetSuggestion", response);
                networkRequestStarted = false;

                try {
                    final JSONObject o = new JSONObject(response);
                    int myStatus = o.getInt("status");
                    final String myStatusMessage = o.getString("message");

                    /*
                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));
                     */

                    if(myStatus == 1){

                        final String transactionId = o.getString("transaction_id");

                        if(!isFinishing()){

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showDialogType1(SellbackActivity.this, "", "Sellback under review. Please record this transaction ID : " + transactionId, "show-positive-image", null, true, getString(R.string.setprofilepicture_activity_okay), "");

                                    mTransferFormHolderConstraintLayout.setVisibility(View.INVISIBLE);
                                    mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                                    mLoaderImageView.clearAnimation();
                                    mLoaderTextView.setText("");
                                }
                            });
                        }
                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mLoaderImageView.clearAnimation();
                        mLoaderTextView.setText("Failed. Click to try again");
                        mTransferFormHolderConstraintLayout.setVisibility(View.VISIBLE);
                        mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                        Config.showToastType1(SellbackActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(SellbackActivity.this, myStatusMessage);
                        Config.signOutUser(getApplicationContext(), true, SellbackActivity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(SellbackActivity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!isFinishing()){
                        mLoaderImageView.clearAnimation();
                        mLoaderTextView.setText("Failed. Click to try again");
                        mTransferFormHolderConstraintLayout.setVisibility(View.VISIBLE);
                        mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(SellbackActivity.this, getString(R.string.failed_check_your_internet_and_try_again));
                if(!isFinishing()){
                    mTransferFormHolderConstraintLayout.setVisibility(View.VISIBLE);
                    mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mLoaderImageView.clearAnimation();
                    mLoaderTextView.setText("Failed. Click the icon to load your shares");
                }
            }
        });
    }
}