package com.fishpott.fishpott5.Fragments.SharesToolBox;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.MainActivity;
import com.fishpott.fishpott5.Activities.UpdateActivity;
import com.fishpott.fishpott5.Adapters.MyShares_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.Notifications_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Notifications_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.Notification_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.fishpott.fishpott5.Activities.MainActivity.mNotificationMenuIconUpdateIconConstraintLayout;
import static com.fishpott.fishpott5.Fragments.NotificationsFragment.mNotificationsRecyclerView;

public class TransferCenterFragment extends Fragment implements View.OnClickListener {

    private ConstraintLayout mStage1ConstraintLayout, mStage2ConstraintLayout;
    private TextView mSelectedSharesTextView, mSharesInfoTextView, mTransferFeeTextView;
    private SwipeRefreshLayout mReloadableSwipeRefreshLayout;
    private EditText mPricePerShareEditText, mQuantityToSellEditText, mPasswordEditText;
    private TextInputLayout mPricePerShareTextInputLayout, mQuantityToSellTextInputLayout;
    private Button mAddToPostButton;
    private int selectedSharesIndex = 0;
    private String selectedSharesName = "", selectedSharesId = "", selectedSharesAvailableQuantity = "", selectedSharesCostPrice = "", selectedSharesMaxPrice = "", networkResponse = "";
    private Thread transferThread = null;
    private String[] chosenSharesInfo = {"sId", "sName", "sSellQuantity", "sSellPrice", ""};
    private String[] sharesNamesStringArraySet;
    private List<String> sharesNamesStringArrayList = new ArrayList<>();
    private List<String> sharesIdStringArrayList = new ArrayList<>();
    private List<String> sharesAvailableQuantityStringArrayList = new ArrayList<>();
    private List<String> shareCostPricesStringArrayList = new ArrayList<>();
    private List<String> sharesMaxPriceStringArrayList = new ArrayList<>();
    private NumberPicker.OnValueChangeListener mSharesSetListener;
    private final Handler handler = new Handler();
    private View view;


    public TransferCenterFragment() {}

    public static TransferCenterFragment newInstance() {
        TransferCenterFragment fragment = new TransferCenterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_transfer_center, container, false);
        fetchMySharesInfo(false);

        mStage1ConstraintLayout = view.findViewById(R.id.stage_1_constraintlayout);
        mStage2ConstraintLayout = view.findViewById(R.id.stage_2_constraintlayout);
        mReloadableSwipeRefreshLayout = view.findViewById(R.id.swipe_reloadshares_swiperefreshrelayout);
        mSharesInfoTextView = view.findViewById(R.id.fragment_loadsharesforposting_sharesinfo_textview);
        mTransferFeeTextView = view.findViewById(R.id.fragment_loadsharesforposting_feeinfo_textview);
        mSelectedSharesTextView = view.findViewById(R.id.fragment_loadsharesforposting_chosen_textview);
        mPricePerShareEditText = view.findViewById(R.id.fragment_loadsharesforposting_sharesprice_edit_text);
        mQuantityToSellEditText = view.findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text);
        mPasswordEditText = view.findViewById(R.id.fragment_loadsharesforposting_password_edittext);
        mAddToPostButton = view.findViewById(R.id.fragment_loadsharesforposting_addsharestopost_button);
        mPricePerShareTextInputLayout = view.findViewById(R.id.fragment_loadsharesforposting_sharesprice_edit_text_layout_holder);
        mQuantityToSellTextInputLayout = view.findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text_layout_holder);

        mReloadableSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Config.showToastType1(getActivity(), getString(R.string.loading_shares));
                transferThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NewsFetcherAndPreparerService.fetchMyShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                    }
                });
                transferThread.start();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fetchMySharesInfo(true);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                if(selectedSharesIndex > 0){
                                    selectedSharesName = sharesNamesStringArrayList.get(selectedSharesIndex);
                                    mSelectedSharesTextView.setText(selectedSharesName);
                                    selectedSharesId = sharesIdStringArrayList.get(selectedSharesIndex);
                                    selectedSharesAvailableQuantity = sharesAvailableQuantityStringArrayList.get(selectedSharesIndex);
                                    selectedSharesCostPrice = shareCostPricesStringArrayList.get(selectedSharesIndex);
                                    //mPricePerShareTextInputLayout.setHint(getString(R.string.price_per_share) + " " + getString(R.string.cost_price_per_share) + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + selectedSharesCostPrice  + ")");
                                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_transfer) + " " + getString(R.string.available_to_sell) + selectedSharesAvailableQuantity  + ")");
                                    selectedSharesMaxPrice = sharesMaxPriceStringArrayList.get(selectedSharesIndex);
                                    setSharesInfo();
                                } else {
                                    //mPricePerShareTextInputLayout.setHint(getString(R.string.price_per_share));
                                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_transfer));
                                    selectedSharesName = "";
                                    selectedSharesId = "";
                                    selectedSharesAvailableQuantity = "";
                                    selectedSharesCostPrice = "";
                                    selectedSharesMaxPrice = "";
                                }
                                mReloadableSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }, 6000);
            }
        });

        mPricePerShareEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {setSharesInfo();}
            @Override public void afterTextChanged(Editable s) {}
        });
        mQuantityToSellEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {setSharesInfo();}
            @Override public void afterTextChanged(Editable s) {}
        });
        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {setSharesInfo();}
            @Override public void afterTextChanged(Editable s) {}
        });

        mSelectedSharesTextView.setOnClickListener(this);
        mAddToPostButton.setOnClickListener(this);
        mSharesSetListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                selectedSharesIndex = newVal;
                if(selectedSharesIndex > 0){
                    selectedSharesName = sharesNamesStringArrayList.get(newVal);
                    mSelectedSharesTextView.setText(selectedSharesName);
                    selectedSharesId = sharesIdStringArrayList.get(newVal);
                    selectedSharesAvailableQuantity = sharesAvailableQuantityStringArrayList.get(newVal);
                    selectedSharesCostPrice = shareCostPricesStringArrayList.get(newVal);
                    //mPricePerShareTextInputLayout.setHint(getString(R.string.price_per_share) + " " + getString(R.string.cost_price_per_share) + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + selectedSharesCostPrice  + ")");
                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_transfer) + " (" + selectedSharesAvailableQuantity + " " + getString(R.string.available) +  ")");
                    selectedSharesMaxPrice = sharesMaxPriceStringArrayList.get(newVal);
                    setSharesInfo();
                } else {
                    mSelectedSharesTextView.setText(getString(R.string.choose_shares));
                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_transfer));
                    selectedSharesName = "";
                    selectedSharesId = "";
                    selectedSharesAvailableQuantity = "";
                    selectedSharesCostPrice = "";
                    selectedSharesMaxPrice = "";
                    mSharesInfoTextView.setText("");
                    mTransferFeeTextView.setText("");
                }
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!networkResponse.trim().equalsIgnoreCase("")){
            mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
            mStage1ConstraintLayout.setVisibility(View.VISIBLE);
            Config.showDialogType1(getActivity(), "1", networkResponse, "", null, false, "", "");
            networkResponse = "";
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.freeMemory();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(transferThread != null){
            transferThread.interrupt();
            transferThread = null;
        }
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.swipe_reloadshares_swiperefreshrelayout));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.fragment_loadsharesforposting_chosen_textview){
            mSharesSetListener = Config.openNumberPickerForCountries(getActivity(), mSharesSetListener, 0, sharesNamesStringArrayList.size()-1, true, sharesNamesStringArraySet, selectedSharesIndex);
        } else if(v.getId() == R.id.fragment_loadsharesforposting_addsharestopost_button){
            if(setSharesInfo() && !mPasswordEditText.getText().toString().trim().equalsIgnoreCase("")){
                chosenSharesInfo[4] = mPasswordEditText.getText().toString().trim();
                final String pricePerShare = mPricePerShareEditText.getText().toString().trim();
                transferThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startSharesTransfer(getActivity().getApplicationContext(), pricePerShare);
                    }
                });
                transferThread.start();
            } else if(mPasswordEditText.getText().toString().trim().equalsIgnoreCase("") && mPricePerShareEditText.getText().toString().trim().equalsIgnoreCase("")){
                Config.showToastType1(getActivity(), getString(R.string.enter_your_password_and_receiver_pottname));
            }  else if(mPricePerShareEditText.getText().toString().trim().equalsIgnoreCase("")){
                Config.showToastType1(getActivity(), getString(R.string.enter_receiver_pottname));
            } else if(mPasswordEditText.getText().toString().trim().equalsIgnoreCase("")){
                Config.showToastType1(getActivity(), getString(R.string.enter_your_password));
            }
        }
    }

    private Boolean setSharesInfo(){
        if(!selectedSharesName.equalsIgnoreCase("") && !selectedSharesId.equalsIgnoreCase("")
                && !selectedSharesAvailableQuantity.equalsIgnoreCase("") && !selectedSharesCostPrice.equalsIgnoreCase("")
                && !selectedSharesMaxPrice.equalsIgnoreCase("")){
            if(!mQuantityToSellEditText.getText().toString().equalsIgnoreCase("")){

                String mySellQuantityString = mQuantityToSellEditText.getText().toString().toString().trim();

                int mySellQuantityInt = Integer.valueOf(mySellQuantityString);
                float myTotalCostPriceFloat = mySellQuantityInt * Float.valueOf(selectedSharesCostPrice);

                if(mySellQuantityInt > 0 && mySellQuantityInt <= Integer.valueOf(selectedSharesAvailableQuantity)){
                    chosenSharesInfo[0] = selectedSharesId;
                    chosenSharesInfo[1] = selectedSharesName;
                    chosenSharesInfo[2] = mySellQuantityString;
                    chosenSharesInfo[3] = selectedSharesCostPrice;
                    String transferFeeString = Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_TRANSFER_FEE);
                    if(transferFeeString.trim().equalsIgnoreCase("")){
                        mTransferFeeTextView.setText("Transfer Center is currently not ready. Please swipe down to refresh. If this continues, please try again later.");
                    } else {
                        mTransferFeeTextView.setText("Transfer fee:" + transferFeeString + ". Make sure you have enough balance in your Debit/Withdrawal Wallet to pay for the transfer.");
                    }
                    mSharesInfoTextView.setText(getString(R.string.you_lose_the) + " " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + String.valueOf(myTotalCostPriceFloat) + " " + getString(R.string.you_used_to_buy_these_shares_after_transferring) + " " + mySellQuantityString + " " + selectedSharesName);
                    return true;
                } else if(mySellQuantityInt > Integer.valueOf(selectedSharesAvailableQuantity)){
                    Config.showToastType1(getActivity(), getString(R.string.reduce_the_quantity_of_shares_to_transfer_it_cannot_be_more_than_what_you_have));
                }
            } else {
                Config.showToastType1(getActivity(), getString(R.string.fill_in_the_quantity_to_transfer));
            }
        }

        return false;
    }

    public void fetchMySharesInfo(Boolean showToast){
        // populate the message from the cursor
        if(getActivity() != null){
            int totalCount = 1;
            sharesNamesStringArrayList.clear();
            sharesIdStringArrayList.clear();
            sharesAvailableQuantityStringArrayList.clear();
            shareCostPricesStringArrayList.clear();
            sharesMaxPriceStringArrayList.clear();
            sharesNamesStringArrayList.add(getString(R.string.choose_shares));
            sharesIdStringArrayList.add("0");
            sharesAvailableQuantityStringArrayList.add("0");
            shareCostPricesStringArrayList.add("0");
            sharesMaxPriceStringArrayList.add("0");
            //CREATING THE NEWS STORIES DATABASE OBJECT
            MyShares_DatabaseAdapter myShares_databaseAdapter = new MyShares_DatabaseAdapter(getActivity().getApplication());
            // OPENING THE STORIES DATABASE
            myShares_databaseAdapter.openDatabase();
            //GETTING ALL NEWS IN LOCAL DATABASE AND POINTING ONE-BY-ONE WITH CURSOR
            Cursor cursor = myShares_databaseAdapter.getAllRows();
            if (cursor.moveToLast()) {
                sharesNamesStringArrayList.clear();
                sharesIdStringArrayList.clear();
                sharesAvailableQuantityStringArrayList.clear();
                shareCostPricesStringArrayList.clear();
                sharesMaxPriceStringArrayList.clear();

                sharesNamesStringArrayList.add(getString(R.string.choose_shares));
                sharesIdStringArrayList.add("0");
                sharesAvailableQuantityStringArrayList.add("0");
                shareCostPricesStringArrayList.add("0");
                sharesMaxPriceStringArrayList.add("0");
                do {
                    //ADDING STORY OBJECT TO LIST
                    sharesNamesStringArrayList.add(cursor.getString(myShares_databaseAdapter.COL_SHARE_NAME));
                    sharesIdStringArrayList.add(cursor.getString(myShares_databaseAdapter.COL_SHARE_ID));
                    sharesAvailableQuantityStringArrayList.add(cursor.getString(myShares_databaseAdapter.COL_SHARE_AVAILABLE_QUANTITY));
                    shareCostPricesStringArrayList.add(cursor.getString(myShares_databaseAdapter.COL_SHARE_COST_PRICE_PER_SHARE));
                    sharesMaxPriceStringArrayList.add(cursor.getString(myShares_databaseAdapter.COL_SHARE_MAX_PRICE_PER_SHARE));
                    totalCount++;
                } while(cursor.moveToPrevious());
            }
            if(totalCount > 0){
                sharesNamesStringArraySet = new String[sharesNamesStringArrayList.size()];
                sharesNamesStringArraySet = sharesNamesStringArrayList.toArray(sharesNamesStringArraySet);
                if(showToast){
                    Config.showToastType1(getActivity(), getString(R.string.shares_loaded));
                }
            } else {
                if(showToast){
                    Config.showToastType1(getActivity(), getString(R.string.no_shares_found_click_the_reload_button_to_refresh));
                }
            }
            cursor.close();
            myShares_databaseAdapter.closeNewsStoriesDatabase();
            cursor = null;
            myShares_databaseAdapter = null;

        }
    }

    private void startSharesTransfer(Context context, final String receiverPottName){

        if(     !chosenSharesInfo[0].equalsIgnoreCase("sId")
                && !chosenSharesInfo[1].equalsIgnoreCase("sName")
                && !chosenSharesInfo[2].equalsIgnoreCase("sSellQuantity")
                && !chosenSharesInfo[3].equalsIgnoreCase("sSellPrice")
                && !chosenSharesInfo[4].equalsIgnoreCase("")
                && receiverPottName.length() > 4){

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mStage1ConstraintLayout.setVisibility(View.INVISIBLE);
                    mStage2ConstraintLayout.setVisibility(View.VISIBLE);
                }
            });

            AndroidNetworking.post(Config.LINK_TRANSFER_SHARES)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("added_item_id", chosenSharesInfo[0])
                    .addBodyParameter("added_item_quantity", chosenSharesInfo[2])
                    .addBodyParameter("added_item_price", chosenSharesInfo[3])
                    .addBodyParameter("myrawpass", chosenSharesInfo[4])
                    .addBodyParameter("receiver_pottname", receiverPottName)
                    .addBodyParameter("language", LocaleHelper.getLanguage(getActivity()))
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("transfer_my_shares")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        JSONObject o = array.getJSONObject(0);
                        int myStatus = o.getInt("1");
                        String statusMsg = o.getString("2");
                        networkResponse = statusMsg;

                        if(myStatus != 1){
                            if(MyLifecycleHandler.isApplicationInForeground()){
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                                        mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(getActivity(), statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(getActivity(), statusMsg);
                            Config.signOutUser(getActivity().getApplicationContext(), false, null, null, 0, 2);
                        }

                        if(myStatus == 4){
                            Config.showToastType1(getActivity(), statusMsg);
                            Config.signOutUser(getActivity().getApplicationContext(), false, null, null, 0, 2);
                        }

                        if(myStatus == 5){
                            Config.showToastType1(getActivity(), getString(R.string.incorrect_password));
                            return;
                        }

                        if(myStatus == 6){
                            Config.showToastType1(getActivity(), getString(R.string.insufficient_shares));
                            return;
                        }

                        if(myStatus == 7){
                            Config.showToastType1(getActivity(), getString(R.string.receiver_pott_not_found));
                            return;
                        }

                        if(myStatus == 8){

                            Config.showDialogType1(getActivity(), "1", getString(R.string.process_incomplete_contact_fishpot_llc_and_report_the_issue_transfer_with_share_id) + statusMsg, "", null, false, "", "");
                            return;
                        }

                        if(myStatus == 9){
                            Config.showDialogType1(getActivity(), "1", getString(R.string.you_cannot_transfer_shares_until_after_a_week_of_ownership) + statusMsg, "", null, false, "", "");
                            return;
                        }


                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if (myStatus == 1) {
                            mSharesInfoTextView.setText("");
                            mTransferFeeTextView.setText("");
                            transferThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    NewsFetcherAndPreparerService.fetchMyShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                                }
                            });
                            transferThread.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fetchMySharesInfo(false);
                                    if(MyLifecycleHandler.isApplicationInForeground()) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSharesInfoTextView.setText("");
                                                mTransferFeeTextView.setText("");
                                                mPricePerShareEditText.setText("");
                                                mQuantityToSellEditText.setText("");
                                                mPasswordEditText.setText("");
                                                mPricePerShareTextInputLayout.setHint(getResources().getString(R.string.receiver_pott_name));
                                                mQuantityToSellTextInputLayout.setHint(getResources().getString(R.string.quantity_to_transfer));

                                            }
                                        });
                                    } else {
                                        networkResponse = getString(R.string.transfer_successful);
                                    }
                                            Config.setUserNotification(getActivity().getApplicationContext(), String.valueOf(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER), "", getString(R.string.transfer_successful), getString(R.string.your_transfer_has_completed_successfully), "", 1, R.drawable.notification_icon);
                                            Notifications_DatabaseAdapter notifications_databaseAdapter = new Notifications_DatabaseAdapter(getActivity().getApplicationContext());
                                            // OPENING THE STORIES DATABASE
                                            notifications_databaseAdapter.openDatabase();
                                            long rowId = notifications_databaseAdapter.insertRow(
                                                    Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER,
                                                    "",
                                                    Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME),
                                                    0,
                                                    "f",
                                                    getString(R.string.your_transfer_of) + " " + chosenSharesInfo[2] + " " + chosenSharesInfo[1] + " " + getString(R.string.to) + " " + getString(R.string.fragment_signup_personalstage3_at) + receiverPottName + " " + getString(R.string.has_completed_successfully),
                                                    Config.getCurrentDateTime3("MMM d, yyyy"),
                                                    ""
                                            );
                                            notifications_databaseAdapter.closeDatabase();
                                            notifications_databaseAdapter = null;

                                            Notification_Model notification_model = new Notification_Model();
                                            notification_model.setRowId(rowId);
                                            notification_model.setNotificationType(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER);
                                            notification_model.setRelevantId_1("");
                                            notification_model.setRelevantId_2(Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
                                            notification_model.setRelevantId_3("");
                                            notification_model.setReadStatus(0);
                                            notification_model.setPottPic("f");
                                            notification_model.setNotificationMessage(getString(R.string.your_transfer_of) + " " + chosenSharesInfo[2] + " " + chosenSharesInfo[1] + " " + getString(R.string.to) + " " + getString(R.string.fragment_signup_personalstage3_at) + receiverPottName + " " + getString(R.string.has_completed_successfully));
                                            notification_model.setNotificationDate(Config.getCurrentDateTime3("MMM d, yyyy"));
                                            Notifications_ListDataGenerator.addOneDataToDesiredPosition(0, notification_model);


                                            if (MyLifecycleHandler.getCurrentActivity() != null && MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())) {
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mNotificationMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                                        mNotificationMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.main_activity_onclick_icon_anim));
                                                        mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                                                    mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                                                    if (MyLifecycleHandler.isApplicationVisible()) {
                                                        Config.showDialogType1(getActivity(), "1", getString(R.string.transfer_successful), "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                                                    }
                                                }
                                            });
                                }
                            }, 6000);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(MyLifecycleHandler.isApplicationInForeground()){
                            mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                            mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                        } else {
                            networkResponse = getString(R.string.login_activity_an_unexpected_error_occured);
                        }
                    }
                }

                @Override
                public void onError(ANError anError) {
                    if(MyLifecycleHandler.isApplicationInForeground()){
                        mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                        mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                    } else {
                        networkResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                    }
                }
            });
        }
    }

}
