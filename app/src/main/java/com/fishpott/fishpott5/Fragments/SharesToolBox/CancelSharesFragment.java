package com.fishpott.fishpott5.Fragments.SharesToolBox;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CancelSharesFragment extends Fragment implements View.OnClickListener {

    private static final String LOADING_CONTEXT = "param1";
    private static final String LOADING_CONTEXT_TYPE = "param2";
    private ConstraintLayout mStage1ConstraintLayout, mStage2ConstraintLayout;
    private TextView mSelectedSharesTextView, mSharesInfoTextView;
    private EditText mQuantityToSellEditText, mPasswordEditText;
    private TextInputLayout mQuantityToSellTextInputLayout;
    private SwipeRefreshLayout mReloadableSwipeRefreshLayout;
    private Button mAddToPostButton;
    private ImageView mReloadSharesImageView, mCloseSharesAddingFragmentImageView;
    private int selectedSharesIndex = 0;
    private String selectedSharesName = "";
    private String selectedSharesId = "";
    private String selectedSharesAvailableQuantity = "";
    private String selectedSharesCostPrice = "";
    private String selectedSharesMaxPrice = "";

    private String[] chosenSharesInfo = {"sId", "sName", "sSellQuantity", "sSellPrice", ""};

    private String[] sharesNamesStringArraySet;
    private List<String> sharesNamesStringArrayList = new ArrayList<>();
    private List<String> sharesIdStringArrayList = new ArrayList<>();
    private List<String> sharesAvailableQuantityStringArrayList = new ArrayList<>();
    private List<String> shareCostPricesStringArrayList = new ArrayList<>();
    private List<String> sharesMaxPriceStringArrayList = new ArrayList<>();


    private NumberPicker.OnValueChangeListener mSharesSetListener;
    private String mParam1;
    private String mParam2;


    public CancelSharesFragment() {
        // Required empty public constructor
    }

    public static CancelSharesFragment newInstance(String param1, String param2) {
        CancelSharesFragment fragment = new CancelSharesFragment();
        Bundle args = new Bundle();
        args.putString(LOADING_CONTEXT, param1);
        args.putString(LOADING_CONTEXT_TYPE, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(LOADING_CONTEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cancel_shares, container, false);
        fetchMySharesInfo(false);

        mStage1ConstraintLayout = (ConstraintLayout) view.findViewById(R.id.stage_1_constraintlayout);
        mStage2ConstraintLayout = (ConstraintLayout) view.findViewById(R.id.stage_2_constraintlayout);
        mReloadableSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_reloadshares_swiperefreshrelayout);
        mSharesInfoTextView = (TextView) view.findViewById(R.id.fragment_loadsharesforposting_sharesinfo_textview);
        mSelectedSharesTextView = (TextView) view.findViewById(R.id.fragment_loadsharesforposting_chosen_textview);
        mQuantityToSellEditText = (EditText) view.findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_loadsharesforposting_password_edittext);
        mAddToPostButton = (Button) view.findViewById(R.id.fragment_loadsharesforposting_addsharestopost_button);
        mQuantityToSellTextInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text_layout_holder);

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

        mReloadableSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //reloadShares
                Config.showToastType1(getActivity(), getString(R.string.loading_shares));
                NewsFetcherAndPreparerService.fetchMyShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                final Handler handler = new Handler();
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
                                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_transfer) + " " + getString(R.string.available_to_sell) + selectedSharesAvailableQuantity  + ")");
                                    selectedSharesMaxPrice = sharesMaxPriceStringArrayList.get(selectedSharesIndex);
                                    setSharesInfo();
                                } else {
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
                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_transfer) + " " + getString(R.string.available_to_sell) + selectedSharesAvailableQuantity  + ")");
                    selectedSharesMaxPrice = sharesMaxPriceStringArrayList.get(newVal);
                    setSharesInfo();
                } else {
                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_transfer));
                    selectedSharesName = "";
                    selectedSharesId = "";
                    selectedSharesAvailableQuantity = "";
                    selectedSharesCostPrice = "";
                    selectedSharesMaxPrice = "";
                }
            }
        };

        return view;
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.fragment_loadsharesforposting_chosen_textview){
            mSharesSetListener = Config.openNumberPickerForCountries(getActivity(), mSharesSetListener, 0, sharesNamesStringArrayList.size()-1, true, sharesNamesStringArraySet, selectedSharesIndex);
        } else if(v.getId() == R.id.fragment_loadsharesforposting_addsharestopost_button){
            if(setSharesInfo() && !mPasswordEditText.getText().toString().trim().equalsIgnoreCase("")){
                chosenSharesInfo[4] = mPasswordEditText.getText().toString().trim();
                startSharesTransfer(getActivity().getApplicationContext());
            } else if(mPasswordEditText.getText().toString().trim().equalsIgnoreCase("")){
                Config.showToastType1(getActivity(), getString(R.string.enter_your_password_and_receiver_pottname));
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
                float myTotalCostPriceFloat= mySellQuantityInt * Float.valueOf(selectedSharesCostPrice);

                if(mySellQuantityInt > 0 && mySellQuantityInt <= Integer.valueOf(selectedSharesAvailableQuantity)){
                    chosenSharesInfo[0] = selectedSharesId;
                    chosenSharesInfo[1] = selectedSharesName;
                    chosenSharesInfo[2] = mySellQuantityString;
                    chosenSharesInfo[3] = selectedSharesCostPrice;
                    mSharesInfoTextView.setText(getString(R.string.your_withdrawal_wallet_will_be_credited_with) + " " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + String.valueOf(myTotalCostPriceFloat) + " " + getString(R.string.after_returning_these_shares));
                    return true;
                } else if(mySellQuantityInt > Integer.valueOf(selectedSharesAvailableQuantity)){
                    Config.showToastType1(getActivity(), getString(R.string.reduce_the_quantity_of_shares_to_cancel_it_cannot_be_more_than_what_you_have));
                }
            } else {
                Config.showToastType1(getActivity(), getString(R.string.fill_in_the_quantity_to_cancel));
            }
        }

        return false;
    }

    public void fetchMySharesInfo(Boolean showToast){
        // populate the message from the cursor
        if(getActivity() != null){
            int totalCount = 0;
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

                sharesNamesStringArrayList.add("Choose Shares");
                sharesIdStringArrayList.add("");
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

        }
    }


    private void startSharesTransfer(Context context){

        if(     !chosenSharesInfo[0].equalsIgnoreCase("sId")
                && !chosenSharesInfo[1].equalsIgnoreCase("sName")
                && !chosenSharesInfo[2].equalsIgnoreCase("sSellQuantity")
                && !chosenSharesInfo[3].equalsIgnoreCase("sSellPrice")
                && !chosenSharesInfo[4].equalsIgnoreCase("")
                ){

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mStage1ConstraintLayout.setVisibility(View.INVISIBLE);
                    mStage2ConstraintLayout.setVisibility(View.VISIBLE);
                }
            });

            /*
            Config.showToastType1(getActivity(), "share ID : " + chosenSharesInfo[0]);
            Config.showToastType1(getActivity(), "share Name : " + chosenSharesInfo[1]);
            Config.showToastType1(getActivity(), "share Transfer Quantity : " + chosenSharesInfo[2]);
            Config.showToastType1(getActivity(), "share Cost Price : " + chosenSharesInfo[3]);
            Config.showToastType1(getActivity(), "Password : " + chosenSharesInfo[4]);
            Config.showToastType1(getActivity(), "Receiver Pott Name : " + receiverPottName.trim());
            */

            AndroidNetworking.post(Config.LINK_TRANSFER_SHARES)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("added_item_id", chosenSharesInfo[0])
                    .addBodyParameter("added_item_quantity", chosenSharesInfo[2])
                    .addBodyParameter("added_item_price", chosenSharesInfo[3])
                    .addBodyParameter("myrawpass", chosenSharesInfo[4])
                    .addBodyParameter("receiver_pottname", "...")
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

                        if(myStatus != 1){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                                    mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                                }
                            });
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
                            Config.showToastType1(getActivity(), getString(R.string.process_incomplete_contact_fishpot_llc_and_report_the_issue_transfer_with_share_id) + statusMsg);
                            return;
                        }

                        if(myStatus == 9){
                            Config.showToastType1(getActivity(), getString(R.string.you_cannot_cancel_shares_until_after_a_week_of_ownership));
                            return;
                        }


                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if (myStatus == 1) {

                            NewsFetcherAndPreparerService.fetchMyShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fetchMySharesInfo(false);
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSelectedSharesTextView.setText(getResources().getString(R.string.no_shares_selected));
                                            mSharesInfoTextView.setText("");
                                            mQuantityToSellEditText.setText("");
                                            mPasswordEditText.setText("");
                                            mQuantityToSellTextInputLayout.setHint(getResources().getString(R.string.quantity_to_transfer));
                                            selectedSharesId = "";
                                            selectedSharesName = "";
                                            selectedSharesId = "";
                                            selectedSharesCostPrice = "";

                                            Config.setUserNotification(getActivity().getApplicationContext(), String.valueOf(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER), "", getString(R.string.shares_ownership_cancelled), getString(R.string.shares_ownership_cancelled_and_withdrawal_wallet_credited_successfully), "", 1, R.drawable.notification_icon);
                                            Notifications_DatabaseAdapter notifications_databaseAdapter = new Notifications_DatabaseAdapter(getActivity().getApplicationContext());
                                            // OPENING THE STORIES DATABASE
                                            notifications_databaseAdapter.openDatabase();
                                            long rowId = notifications_databaseAdapter.insertRow(
                                                    Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER,
                                                    "",
                                                    Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME),
                                                    0,
                                                    "f",
                                                    getString(R.string.your_cancellation_of) +  " " + chosenSharesInfo[2] + " " + chosenSharesInfo[1] + " " +  getString(R.string.has_completed_and_your_withdrawal_wallet_credited_successfully),
                                                    Config.getCurrentDateTime3("MMM d, yyyy"),
                                                    ""
                                            );
                                            notifications_databaseAdapter.closeDatabase();

                                            Notification_Model notification_model = new Notification_Model();
                                            notification_model.setRowId(rowId);
                                            notification_model.setNotificationType(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER);
                                            notification_model.setRelevantId_1("");
                                            notification_model.setRelevantId_2(Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
                                            notification_model.setRelevantId_3("");
                                            notification_model.setReadStatus(0);
                                            notification_model.setPottPic("f");
                                            notification_model.setNotificationMessage(getString(R.string.your_cancellation_of) +  " " + chosenSharesInfo[2] + " " + chosenSharesInfo[1] + " " +  getString(R.string.has_completed_and_your_withdrawal_wallet_credited_successfully));
                                            notification_model.setNotificationDate(Config.getCurrentDateTime3("MMM d, yyyy"));

                                            //ADDING STORY OBJECT TO LIST
                                            Notifications_ListDataGenerator.addOneDataToDesiredPosition(0, notification_model);
                                            if(MyLifecycleHandler.getCurrentActivity() != null && MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())){
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mNotificationMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                                        mNotificationMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.main_activity_onclick_icon_anim));
                                                        mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                            mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                                            mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                                            if(MyLifecycleHandler.isApplicationVisible()){
                                                Config.showToastType1(getActivity(), getString(R.string.shares_ownership_cancelled_and_withdrawal_wallet_credited_successfully));
                                            }
                                        }
                                    });
                                }
                            }, 6000);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(ANError anError) {}
            });
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
