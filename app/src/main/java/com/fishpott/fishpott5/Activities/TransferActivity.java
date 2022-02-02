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



public class TransferActivity extends AppCompatActivity implements View.OnClickListener{

    private SwipeRefreshLayout mReloadSharesSwipeRefreshLayout;
    private ConstraintLayout mLoaderHolderConstraintLayout, mTransferFormHolderConstraintLayout;
    private TextView mLoaderTextView, mSharesListTextView, mFeeInfoTextView, mTransferInfoTextView;
    private ImageView mLoaderImageView, mBackImageView;
    private Boolean networkRequestStarted = false;
    private Thread transferThread = null;
    private EditText mQuantityEditText, mReceiverPottNameEditText, mPasswordEditText;
    private Button mTransferButton;
    private TextInputLayout mQuantityEditTextTextInputLayout;
    private int selectedSharesIndex = 0, transferfeeInt = 0;
    private String transferFee = "", selectedSharesName = "", selectedSharesId = "", selectedSharesAvailableQuantity = "", selectedSharesCostPrice = "", selectedSharesMaxPrice = "", networkResponse = "";
    private String[] chosenSharesInfo = {"sId", "sName", "sSellQuantity", "sSellPrice", ""};
    private String[] sharesNamesStringArraySet;
    private List<String> sharesNamesStringArrayList = new ArrayList<>();
    private List<String> sharesIdStringArrayList = new ArrayList<>();
    private List<String> sharesAvailableQuantityStringArrayList = new ArrayList<>();
    private List<String> shareCostPricesStringArrayList = new ArrayList<>();
    private List<String> sharesMaxPriceStringArrayList = new ArrayList<>();
    private NumberPicker.OnValueChangeListener mSharesSetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

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
                    mQuantityEditTextTextInputLayout.setHint(getString(R.string.quantity_to_transfer) + " (" + selectedSharesAvailableQuantity + " " + getString(R.string.available) +  ")");
                    selectedSharesMaxPrice = sharesMaxPriceStringArrayList.get(newVal);
                    setSharesInfo();
                } else {
                    mSharesListTextView.setText(getString(R.string.choose_shares));
                    mQuantityEditText.setHint(getString(R.string.quantity_to_transfer));
                    selectedSharesName = "";
                    selectedSharesId = "";
                    selectedSharesAvailableQuantity = "";
                    selectedSharesCostPrice = "";
                    selectedSharesMaxPrice = "";
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
            mSharesSetListener = Config.openNumberPickerForCountries(TransferActivity.this, mSharesSetListener, 0, sharesNamesStringArrayList.size()-1, true, sharesNamesStringArraySet, selectedSharesIndex);
        } else if(v.getId() == mBackImageView.getId()){
            onBackPressed();
        } else if(v.getId() == mTransferButton.getId()){
            transferShares(getApplicationContext(), mReceiverPottNameEditText.getText().toString().trim(),  mPasswordEditText.getText().toString().trim(), mQuantityEditText.getText().toString().toString().trim());
        }
    }


    private Boolean setSharesInfo(){
        Log.e("setSharesInfo", "selectedSharesName: " + selectedSharesName);
        Log.e("setSharesInfo", "selectedSharesId: " + selectedSharesId);
        Log.e("setSharesInfo", "selectedSharesAvailableQuantity: " + selectedSharesAvailableQuantity);
        Log.e("setSharesInfo", "selectedSharesCostPrice: " + selectedSharesCostPrice);
        Log.e("setSharesInfo", "selectedSharesMaxPrice: " + selectedSharesMaxPrice);

        if(!selectedSharesName.equalsIgnoreCase("") && !selectedSharesId.equalsIgnoreCase("")
                && !selectedSharesAvailableQuantity.equalsIgnoreCase("") && !selectedSharesCostPrice.equalsIgnoreCase("")
                && !selectedSharesMaxPrice.equalsIgnoreCase("")){
            if(!mQuantityEditText.getText().toString().equalsIgnoreCase("")){

                String mySellQuantityString = mQuantityEditText.getText().toString().toString().trim();

                int mySellQuantityInt = Integer.valueOf(mySellQuantityString);
                //float myTotalCostPriceFloat = mySellQuantityInt * Float.valueOf(selectedSharesCostPrice);

                if(mySellQuantityInt > 0 && mySellQuantityInt <= Integer.valueOf(selectedSharesAvailableQuantity)){
                    if(transferFee.trim().equalsIgnoreCase("")){
                        mFeeInfoTextView.setText("Transfer Center is currently not ready. Please swipe down to refresh. If this continues, please try again later.");
                    } else {
                        mFeeInfoTextView.setText("Transfer fee:" + transferFee + ". You will be directed to make payment.");
                    }
                    mTransferInfoTextView.setText("You are transferring " + mySellQuantityString + " " + selectedSharesName);
                    return true;
                } else if(mySellQuantityInt > Integer.valueOf(selectedSharesAvailableQuantity)){
                    Config.showToastType1(TransferActivity.this, getString(R.string.reduce_the_quantity_of_shares_to_transfer_it_cannot_be_more_than_what_you_have));
                }
            } else {
                Config.showToastType1(TransferActivity.this, getString(R.string.fill_in_the_quantity_to_transfer));
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
                mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(TransferActivity.this, R.anim.suggestion_loading_anim));
                mLoaderTextView.setText("Getting your shares...");
            }
        });

        AndroidNetworking.post(Config.LINK_GET_MY_SHARES)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(TransferActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(TransferActivity.this.getApplicationContext())))
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
                            sharesMaxPriceStringArrayList.add("");

                            for (int i = 0; i < linkupsSuggestionsArray.length(); i++){
                                final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                sharesNamesStringArrayList.add(k.getString("business_name"));
                                sharesIdStringArrayList.add(k.getString("stock_id"));
                                sharesAvailableQuantityStringArrayList.add(k.getString("quantity_of_stocks"));
                                shareCostPricesStringArrayList.add(k.getString("cost_per_share_usd"));
                                sharesMaxPriceStringArrayList.add(k.getString("value_per_share_usd"));

                                if(i == linkupsSuggestionsArray.length()-1){
                                    sharesNamesStringArraySet = new String[sharesNamesStringArrayList.size()];
                                    sharesNamesStringArraySet = sharesNamesStringArrayList.toArray(sharesNamesStringArraySet);
                                }
                            }

                        } else {
                            Config.showDialogType1(TransferActivity.this, "", "You have no shares to transfer", "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
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
                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mLoaderImageView.clearAnimation();
                        mLoaderTextView.setText("Failed. Click to try again");
                        Config.showToastType1(TransferActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(TransferActivity.this, myStatusMessage);
                        Config.signOutUser(getApplicationContext(), true, TransferActivity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(TransferActivity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!isFinishing()){
                        mLoaderImageView.clearAnimation();
                        mLoaderTextView.setText("Failed. Click to try again");
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(TransferActivity.this, getString(R.string.failed_check_your_internet_and_try_again));
                if(!isFinishing()){
                    mTransferFormHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                    mLoaderImageView.clearAnimation();
                    mLoaderTextView.setText("Failed. Click the icon to load your shares");
                }
            }
        });
    }


    private void transferShares(Context context, String receiverPottName, String userPassword, String transferQuantity){
        Log.e("selectedSharesId", selectedSharesId);
        Log.e("transferQuantity", transferQuantity);
        Log.e("receiverPottName", receiverPottName);

        if(     selectedSharesId.equalsIgnoreCase("")
                || transferQuantity.trim().equalsIgnoreCase("")
                || receiverPottName.trim().length() < 4) {
            Config.showToastType1(TransferActivity.this, "The form is incomplete");
            return;
        }

        if(Integer.valueOf(transferQuantity) < 1) {
            Config.showToastType1(TransferActivity.this, "The form is incomplete.");
            return;
        }

        networkRequestStarted = true;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mTransferFormHolderConstraintLayout.setVisibility(View.INVISIBLE);
                mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                mLoaderImageView.startAnimation(AnimationUtils.loadAnimation(TransferActivity.this, R.anim.suggestion_loading_anim));
                mLoaderTextView.setText("Setting payment portal...");
            }
        });

        AndroidNetworking.post(Config.LINK_TRANSFER_STOCKS)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(TransferActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(TransferActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(TransferActivity.this.getApplicationContext())))
                .addBodyParameter("user_password", userPassword)
                .addBodyParameter("stockownership_id", selectedSharesId)
                .addBodyParameter("transfer_quantity", transferQuantity)
                .addBodyParameter("receiver_pottname", receiverPottName)
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

                        final int paymentGatewayAmtInt = new JSONObject(response).getJSONObject("data").getInt("transfer_fee_cedis_no_sign");
                        //String paymentGatewayAmtString = new JSONObject(response).getJSONObject("data").getString("transfer_fee_cedis_with_sign");
                        final String transanctionId = new JSONObject(response).getJSONObject("data").getString("transanction_id");
                        // LIST RESULTS SETTING COMES HERE

                        if(!isFinishing()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    //mLoaderImageView.clearAnimation();
                                    //mLoaderTextView.setText("...");
                                    //mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                                    //mTransferFormHolderConstraintLayout.setVisibility(View.VISIBLE);
                                    //Config.showToastType1(TransferActivity.this, "Go to payment gateway");
                                    Config.openActivity(TransferActivity.this, ProcessPaymentActvity.class, 1, 0, 0, "ORDERID", transanctionId);

                                    /*
                                    new thetellerManager(TransferActivity.this).setAmount(Long.parseLong(String.valueOf(paymentGatewayAmtInt)))
                                            .setEmail("annodankyikwaku@gmail.com")
                                            .setfName("fName")
                                            .setlName("lName")
                                            .setMerchant_id("merchantId")
                                            .setNarration("narration")
                                            .setApiUser("apiUser")
                                            .setApiKey("apiKey")
                                            .setTxRef(transanctionId)
                                            .set3dUrl("dUrl")
                                            .acceptCardPayments(true)
                                            .acceptGHMobileMoneyPayments(true)
                                            .onStagingEnv(false)
                                            .initialize();
                                     */
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
                        Config.showToastType1(TransferActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(TransferActivity.this, myStatusMessage);
                        Config.signOutUser(getApplicationContext(), true, TransferActivity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(TransferActivity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
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
                Config.showToastType1(TransferActivity.this, getString(R.string.failed_check_your_internet_and_try_again));
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