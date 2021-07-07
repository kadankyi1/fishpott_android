package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.fishpott.fishpott5.Adapters.MyShares_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;

import java.util.ArrayList;
import java.util.List;

public class LoadSharesForPostingFragment extends Fragment implements View.OnClickListener {

    private static final String LOADING_CONTEXT = "param1";
    private static final String LOADING_CONTEXT_TYPE = "param2";
    private TextView mSelectedSharesTextView, mSharesInfoTextView;
    private EditText mPricePerShareEditText, mQuantityToSellEditText, mPasswordEditText;
    private TextInputLayout mPricePerShareTextInputLayout, mQuantityToSellTextInputLayout;
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

    private OnFragmentInteractionListener mListener;

    public LoadSharesForPostingFragment() {
        // Required empty public constructor
    }

    public static LoadSharesForPostingFragment newInstance(String param1, String param2) {
        LoadSharesForPostingFragment fragment = new LoadSharesForPostingFragment();
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
        View view = inflater.inflate(R.layout.fragment_load_shares_for_posting, container, false);
        fetchMySharesInfo();
        mReloadSharesImageView = (ImageView) view.findViewById(R.id.loadsharesforpostingfragment_reloadicon_imageview);
        mCloseSharesAddingFragmentImageView = (ImageView) view.findViewById(R.id.loadsharesforpostingfragment_closeicon_imageview);
        mSharesInfoTextView = (TextView) view.findViewById(R.id.fragment_loadsharesforposting_sharesinfo_textview);
        mSelectedSharesTextView = (TextView) view.findViewById(R.id.fragment_loadsharesforposting_chosen_textview);
        mPricePerShareEditText = (EditText) view.findViewById(R.id.fragment_loadsharesforposting_sharesprice_edit_text);
        mQuantityToSellEditText = (EditText) view.findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_loadsharesforposting_password_edittext);
        mAddToPostButton = (Button) view.findViewById(R.id.fragment_loadsharesforposting_addsharestopost_button);
        mPricePerShareTextInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_loadsharesforposting_sharesprice_edit_text_layout_holder);
        mQuantityToSellTextInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_loadsharesforposting_sharesquantity_edit_text_layout_holder);


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

        mReloadSharesImageView.setOnClickListener(this);
        mCloseSharesAddingFragmentImageView.setOnClickListener(this);
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
                    mPricePerShareTextInputLayout.setHint(getString(R.string.price_per_share) + " " + getString(R.string.cost_price_per_share) + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + selectedSharesCostPrice  + ")");
                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_sell) + " " + getString(R.string.available_to_sell) + selectedSharesAvailableQuantity  + ")");
                    selectedSharesMaxPrice = sharesMaxPriceStringArrayList.get(newVal);
                } else {
                    mPricePerShareTextInputLayout.setHint(getString(R.string.price_per_share));
                    mQuantityToSellTextInputLayout.setHint(getString(R.string.quantity_to_sell));
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
        } else if(v.getId() == R.id.loadsharesforpostingfragment_reloadicon_imageview){
            //reloadShares
            Config.showToastType1(getActivity(), getString(R.string.loading_shares));
            NewsFetcherAndPreparerService.fetchMyShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchMySharesInfo();
                }
            }, 6000);
        } else if(v.getId() == R.id.loadsharesforpostingfragment_closeicon_imageview){
            getActivity().onBackPressed();
        } else if(v.getId() == R.id.fragment_loadsharesforposting_addsharestopost_button){
            if(setSharesInfo() && !mPasswordEditText.getText().toString().trim().equalsIgnoreCase("")){
                chosenSharesInfo[4] = mPasswordEditText.getText().toString().trim();
                sendBackSharesInfo(chosenSharesInfo);
                getActivity().onBackPressed();
            } else if(mPasswordEditText.getText().toString().trim().equalsIgnoreCase("")){
                Config.showToastType1(getActivity(), getString(R.string.enter_your_password));
            }
        }
    }

    private Boolean setSharesInfo(){
        if(!selectedSharesName.equalsIgnoreCase("") && !selectedSharesId.equalsIgnoreCase("")
                && !selectedSharesAvailableQuantity.equalsIgnoreCase("") && !selectedSharesCostPrice.equalsIgnoreCase("")
                && !selectedSharesMaxPrice.equalsIgnoreCase("")){
            if(!mPricePerShareEditText.getText().toString().equalsIgnoreCase("") && !mQuantityToSellEditText.getText().toString().equalsIgnoreCase("")){

                String mySellQuantityString = mQuantityToSellEditText.getText().toString().toString().trim();
                String mMySellPricePerShareString = mPricePerShareEditText.getText().toString().toString().trim();

                int mySellQuantityInt = Integer.valueOf(mySellQuantityString);
                float mySellPricePerShareFloat = Float.valueOf(mMySellPricePerShareString);
                float myTotalSellingPriceFloat = mySellQuantityInt * mySellPricePerShareFloat;
                float myTotalCostPriceFloat= mySellQuantityInt * Float.valueOf(selectedSharesCostPrice);
                float myTotalProfitOrLossFloat = Math.abs(myTotalSellingPriceFloat - myTotalCostPriceFloat);

                if(mySellQuantityInt > 0 && mySellQuantityInt <= Integer.valueOf(selectedSharesAvailableQuantity)
                        && mySellPricePerShareFloat > 0 && mySellPricePerShareFloat <= Float.valueOf(selectedSharesMaxPrice)){
                    chosenSharesInfo[0] = selectedSharesId;
                    chosenSharesInfo[1] = selectedSharesName;
                    chosenSharesInfo[2] = mySellQuantityString;
                    chosenSharesInfo[3] = mMySellPricePerShareString;
                    if(myTotalSellingPriceFloat > myTotalCostPriceFloat){
                        mSharesInfoTextView.setText(getString(R.string.you_make_a_profit_of) + " " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + String.valueOf(myTotalProfitOrLossFloat) + " " + getString(R.string.after_selling) + " " + mySellQuantityString + " " + selectedSharesName + " " + getString(R.string.for_) + " " +  Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + mMySellPricePerShareString + " " + getString(R.string.per_share));
                    } else if(myTotalSellingPriceFloat < myTotalCostPriceFloat){
                        mSharesInfoTextView.setText(getString(R.string.you_make_a_loss_of) + " " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + String.valueOf(myTotalProfitOrLossFloat) + " " + getString(R.string.after_selling) + " " + mySellQuantityString + " " + selectedSharesName + " " + getString(R.string.for_) + " " +  Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + mMySellPricePerShareString + " " + getString(R.string.per_share) + " " + getString(R.string.consider_increading_your_price_per_share));
                    } else {
                        mSharesInfoTextView.setText(getString(R.string.you_make_no_profit_or_loss) + " " + getString(R.string.after_selling) + " " + mySellQuantityString + " " + selectedSharesName + " " + getString(R.string.for_) + " " +  Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + mMySellPricePerShareString + " " + getString(R.string.per_share)  + " " + getString(R.string.consider_increading_your_price_per_share));
                    }
                    return true;
                } else if(mySellQuantityInt > Integer.valueOf(selectedSharesAvailableQuantity)){
                    Config.showToastType1(getActivity(), getString(R.string.reduce_the_quantity_of_shares_to_sell_it_cannot_be_more_than_what_you_have));
                } else if(mySellPricePerShareFloat <= 0){
                    Config.showToastType1(getActivity(), getString(R.string.change_your_selling_price_it_must_be_more_than_zero));
                } else if(mySellPricePerShareFloat > Float.valueOf(selectedSharesMaxPrice)){
                    Config.showToastType1(getActivity(), getString(R.string.change_your_selling_price_it_cannot_be_more_than) + " " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + selectedSharesMaxPrice);
                }
            } else {
                Config.showToastType1(getActivity(), getString(R.string.fill_in_the_price_per_share_and_quantity_to_sell));
            }
        } else {
            Config.showToastType1(getActivity(), getString(R.string.choose_shares));
        }

        return false;
    }

        public void fetchMySharesInfo(){
        // populate the message from the cursor
        if(getActivity() != null){
            int totalCount = 1;
            sharesNamesStringArrayList.add(getString(R.string.choose_shares));
            sharesIdStringArrayList.add("");
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
                Config.showToastType1(getActivity(), getString(R.string.shares_loaded));
            } else {
                Config.showToastType1(getActivity(), getString(R.string.no_shares_found_click_the_reload_button_to_refresh));
            }
            cursor.close();

        }
    }

    public void sendBackSharesInfo(String[] sharesInfo) {
        if (mListener != null) {
            mListener.onFragmentInteraction(sharesInfo);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String[] sharesInfo);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
