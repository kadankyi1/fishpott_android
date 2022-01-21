package com.fishpott.fishpott5.Activities;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.StockProfileListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.StockProfileModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StockProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView mSuggestedLinkupsRecyclerView;
    private ImageView mBackImageView,mReloadFreshImageView;
    private ProgressBar mLinkupsLoadingProgressBar;
    private LinearLayoutManager mLayoutManager;
    private Boolean REQUEST_HAS_STARTED = false;
    private Thread suggestedLinkupsFetchThread2 = null, imageLoaderThread = null;
    private String parentSharesId = "", networkResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_profile);

        // GETTING RESET PHONE FROM OLD-TO-NEW ACTIVITY TRANSITION
        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle !=null) {
            parentSharesId =(String) intentBundle.get("shareparentid");
        } else {
            Config.showToastType1(StockProfileActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
            finish();
        }

        // BINDING VIEWS
        mSuggestedLinkupsRecyclerView = findViewById(R.id.activity_suggestedlinkupsactivity_recyclerview);
        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mReloadFreshImageView = findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = findViewById(R.id.activity_suggestedlinkupsactivity_loader);

        //SETTING UP RECYCLERVIEW
        mSuggestedLinkupsRecyclerView.setVisibility(View.INVISIBLE);
        mLayoutManager = new LinearLayoutManager(StockProfileActivity.this);

        mSuggestedLinkupsRecyclerView.setItemViewCacheSize(20);
        mSuggestedLinkupsRecyclerView.setDrawingCacheEnabled(true);
        mSuggestedLinkupsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mSuggestedLinkupsRecyclerView.setLayoutManager(mLayoutManager);
        mSuggestedLinkupsRecyclerView.setAdapter(new RecyclerViewAdapter(this));

        // SENDING THE USER TO THE MAIN-ACTIVITY WHEN THE FORWARD ICON IS CLICKED WITHOUT KILLING THIS ACTIVITY
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //
        mReloadFreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!REQUEST_HAS_STARTED && Connectivity.isConnected(StockProfileActivity.this)) {
                    mReloadFreshImageView.setVisibility(View.INVISIBLE);
                    mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
                    makeRequest();
                }
            }
        });

        // FETCHING FIRST BATCH OF LINKUPS BEFORE GETTING CONTACTS
        if(Connectivity.isConnected(StockProfileActivity.this)){
            REQUEST_HAS_STARTED =  true;
            getSuggestedLinkUps(LocaleHelper.getLanguage(StockProfileActivity.this));
        } else {
            tuningViewsOnError(0, "");
            REQUEST_HAS_STARTED =  false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mReloadFreshImageView = findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = findViewById(R.id.activity_suggestedlinkupsactivity_loader);

        if(!networkResponse.trim().equalsIgnoreCase("")){
            if(!networkResponse.trim().equalsIgnoreCase("1")) {
                Config.showDialogType1(StockProfileActivity.this, "1", networkResponse, "", null, false, "", "");
                tuningViewsOnError(1,"");
            } else {
                mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                mReloadFreshImageView.setVisibility(View.INVISIBLE);
                mSuggestedLinkupsRecyclerView.setVisibility(View.VISIBLE);
            }
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mReloadFreshImageView = null;
        mLinkupsLoadingProgressBar = null;
        Config.freeMemory();
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
        mSuggestedLinkupsRecyclerView = null;
        mBackImageView = null;
        mReloadFreshImageView = null;
        mLinkupsLoadingProgressBar = null;
        mLayoutManager = null;
        parentSharesId= null;
        networkResponse = null;
        if(suggestedLinkupsFetchThread2 != null){
            suggestedLinkupsFetchThread2.interrupt();
            suggestedLinkupsFetchThread2 = null;
        }
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_stockprofile_activity));
        Config.freeMemory();
        //CLOSE BACKGROUND THREAD END
    }

    @Override
    public void onClick(View view) {

    }


    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    private class RecyclerViewAdapter extends RecyclerView.Adapter{

        //DEFINING A CLICK LISTENER FOR THE RECYCLERVIEW
        View.OnClickListener clickListener;

        public RecyclerViewAdapter(View.OnClickListener listener) {
            this.clickListener = listener;
        }
        @Override
        public int getItemViewType(int position) {
            if(position == 0){
                return 0;
            } else if(position == 1){
                return 1;
            }
            return 2;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v;
            if(viewType == 0){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shares_profile, parent, false);
                vh = new ShareProfileViewHolder(v);
            } else if(viewType == 1){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_info_bar_rounded_corners, parent, false);
                vh = new InfoBarViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shares_value_history, parent, false);
                vh = new ValueViewHolder(v);
            }

            return vh;
        }

        public class ShareProfileViewHolder extends RecyclerView.ViewHolder {
            private CircleImageView mShareLogoCircleImageView;
            private Button mBuyButton;
            private TextView mShareNameTextView, mCompanyNameTextView, mValuePerShareTextView, mNetWorthTextView,
                    mDividendPerShareTextView, mCountryOriginTextView, mProfitPerYearTextView, mSharesHostedTextView,
                    mCeoNameTextView, mSharesDescriptionTextView;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public ShareProfileViewHolder(View v) {
                super(v);

                mShareLogoCircleImageView = itemView.findViewById(R.id.share_logo_circleimageview);
                mShareNameTextView = itemView.findViewById(R.id.share_name_textview);
                mCompanyNameTextView = itemView.findViewById(R.id.shares_offering_company_name_textview);
                mValuePerShareTextView = itemView.findViewById(R.id.value_per_share_textview);
                mNetWorthTextView = itemView.findViewById(R.id.networth_textview);
                mDividendPerShareTextView = itemView.findViewById(R.id.dividend_per_share_textview);
                mCountryOriginTextView = itemView.findViewById(R.id.location_textview);
                mProfitPerYearTextView = itemView.findViewById(R.id.income_per_month_textview);
                mSharesHostedTextView = itemView.findViewById(R.id.total_available_shares_textview);
                mCeoNameTextView = itemView.findViewById(R.id.ceo_textview);
                mSharesDescriptionTextView = itemView.findViewById(R.id.shares_description_textview);
                mBuyButton = itemView.findViewById(R.id.buy_sharesprofile_activity_button);

                // ALL ON-CLICK LISTENERS
                mCompanyNameTextView.setOnClickListener(innerClickListener);
                mBuyButton.setOnClickListener(innerClickListener);
            }
        }

        public class InfoBarViewHolder extends RecyclerView.ViewHolder {
            private TextView mInfoTextView;

            public InfoBarViewHolder(View v) {
                super(v);
                mInfoTextView = itemView.findViewById(R.id.info_textview);
            }
        }

        public class ValueViewHolder extends RecyclerView.ViewHolder {
            private TextView mValueRecordDate, mValueTextView, mDividendTextView, mInvestorsTextView;

            public ValueViewHolder(View v) {
                super(v);
                mValueRecordDate = itemView.findViewById(R.id.itemlist_sharesprofileactivity_date_textview);
                mValueTextView = itemView.findViewById(R.id.itemlist_sharesprofileactivity_valuepershare_textview);
                mDividendTextView = itemView.findViewById(R.id.itemlist_sharesprofileactivity_dividendpershare_textview);
                mInvestorsTextView = itemView.findViewById(R.id.itemlist_sharesprofileactivity_investorsnow_textview);

            }
        }


        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if(position == 0){
                ((ShareProfileViewHolder) holder).mShareNameTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getSharesName());
                ((ShareProfileViewHolder) holder).mCompanyNameTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getCompanyName());
                ((ShareProfileViewHolder) holder).mValuePerShareTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getValuePershare());
                ((ShareProfileViewHolder) holder).mNetWorthTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getCompanyNetWorth());
                ((ShareProfileViewHolder) holder).mDividendPerShareTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getYieldPerShare());
                ((ShareProfileViewHolder) holder).mCountryOriginTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getCompanyCountryOrigin());
                ((ShareProfileViewHolder) holder).mProfitPerYearTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getCompanyProfitPerYear());
                ((ShareProfileViewHolder) holder).mSharesHostedTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getTotalCompanySharesHosted());
                ((ShareProfileViewHolder) holder).mCeoNameTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getCeoName());
                ((ShareProfileViewHolder) holder).mSharesDescriptionTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getShareDescription());

                // LOADING A PROFILE PICTURE IF URL EXISTS
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (StockProfileListDataGenerator.getAllData().get(position).getSharesLogo().trim().length() > 1) {
                            Config.loadUrlImage(StockProfileActivity.this, true, StockProfileListDataGenerator.getAllData().get(position).getSharesLogo().trim(), ((ShareProfileViewHolder) holder).mShareLogoCircleImageView, 0, 60, 60);
                        } else {
                            Config.loadErrorImageView(StockProfileActivity.this, R.drawable.sharesforsale, ((ShareProfileViewHolder) holder).mShareLogoCircleImageView, 60, 60);
                        }
                    }
                });
                imageLoaderThread.start();

            } else if(position == 1){

            } else {
                ((ValueViewHolder) holder).mValueRecordDate.setText(StockProfileListDataGenerator.getAllData().get(position).getValueRecordDate());
                ((ValueViewHolder) holder).mValueTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getRecordValuerPerShare());
                ((ValueViewHolder) holder).mDividendTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getRecordDividendPerShare());
                ((ValueViewHolder) holder).mInvestorsTextView.setText(StockProfileListDataGenerator.getAllData().get(position).getRecordInvestorNumber());
            }


        }



        @Override
        public int getItemCount() {
            return StockProfileListDataGenerator.getAllData().size();
        }


    }


    private void allOnClickHandlers(View v, int position) {
        if(v.getId() == R.id.shares_offering_company_name_textview){
            Config.openActivity(StockProfileActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", "ProfileOfDifferentPottActivity - POSITION : " + String.valueOf(position) + "POTTNAME : " + StockProfileListDataGenerator.getAllData().get(position).getCompanyPottName());
        } else if(v.getId() == R.id.buy_sharesprofile_activity_button){
            String[] buyData = {
                    "",
                    StockProfileListDataGenerator.getAllData().get(position).getParentID(),
                    StockProfileListDataGenerator.getAllData().get(position).getSharesName(),
                    "",
                    StockProfileListDataGenerator.getAllData().get(position).getSharesLogo(),
                    ""
            };
            Log.e("SHAREPARENTID", "PROFILE SIDE ID : " + buyData[1]);
            Log.e("SHAREPARENTID", "PROFILE SIDE NAME : " + buyData[2]);
            Log.e("SHAREPARENTID", "PROFILE SIDE LOGO : " + buyData[4]);
            Config.openActivity4(StockProfileActivity.this, SellersListActivity.class, 1, 0, 1, "BUY_INFO", buyData);
        }
    }

    public void getSuggestedLinkUps(final String language){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                REQUEST_HAS_STARTED =  true;
                // UI ACTIONS COME HERE
                    mReloadFreshImageView.setVisibility(View.INVISIBLE);
                    mSuggestedLinkupsRecyclerView.setVisibility(View.INVISIBLE);
                    mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
            }
        }); //END OF HANDLER-1-TO-MAIN-THREAD

        AndroidNetworking.post(Config.LINK_GET_SHARE_PROFILE)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("parentsharesid", parentSharesId)
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("get_share_profile")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("StockProfileActivity", "response : " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        JSONObject o = array.getJSONObject(0);
                        int myStatus = o.getInt("1");
                        String statusMsg = o.getString("2");

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(StockProfileActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(StockProfileActivity.this, statusMsg);
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if (myStatus == 1) {
                            JSONArray linkupsSuggestionsArray = jsonObject.getJSONArray("news_returned");
                            // LIST RESULTS SETTING COMES HERE
                            if(linkupsSuggestionsArray.length() > 0){

                                StockProfileListDataGenerator.getAllData().clear();
                                mSuggestedLinkupsRecyclerView.getAdapter().notifyDataSetChanged();
                                for (int i = 0; i<linkupsSuggestionsArray.length(); i++){
                                        StockProfileModel mine1 = new StockProfileModel();
                                        final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                        if(i == 0){
                                            mine1.setParentID(k.getString("0a"));
                                            mine1.setSharesName(k.getString("1"));
                                            mine1.setSharesLogo(k.getString("2"));
                                            mine1.setValuePershare(k.getString("3"));
                                            mine1.setYieldPerShare(k.getString("4"));
                                            mine1.setCompanyName(k.getString("5"));
                                            mine1.setCompanyPottName(k.getString("6"));
                                            mine1.setCompanyNetWorth(k.getString("7"));
                                            mine1.setCompanyCountryOrigin(k.getString("8"));
                                            mine1.setCompanyProfitPerYear(k.getString("9"));
                                            mine1.setTotalCompanySharesHosted(k.getString("10"));
                                            mine1.setCeoName(k.getString("11"));
                                            mine1.setShareDescription(k.getString("12"));

                                            if(!k.getString("2").trim().equalsIgnoreCase("")){
                                                final String thisImageurl = k.getString("2").trim();
                                                imageLoaderThread = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Config.loadUrlImage(StockProfileActivity.this, false, thisImageurl, null, 0, 60, 60);
                                                    }
                                                });
                                                imageLoaderThread.start();
                                            }

                                        } else if(i == 1){
                                            StockProfileListDataGenerator.addOneData(mine1);
                                            mSuggestedLinkupsRecyclerView.getAdapter().notifyItemInserted(StockProfileListDataGenerator.getAllData().size());
                                            mine1.setValueRecordDate(k.getString("0a"));
                                            mine1.setRecordValuerPerShare(k.getString("1"));
                                            mine1.setRecordDividendPerShare(k.getString("2"));
                                            mine1.setRecordInvestorNumber(k.getString("3"));
                                        } else {
                                            mine1.setValueRecordDate(k.getString("0a"));
                                            mine1.setRecordValuerPerShare(k.getString("1"));
                                            mine1.setRecordDividendPerShare(k.getString("2"));
                                            mine1.setRecordInvestorNumber(k.getString("3"));
                                        }
                                        StockProfileListDataGenerator.addOneData(mine1);
                                        mSuggestedLinkupsRecyclerView.getAdapter().notifyItemInserted(StockProfileListDataGenerator.getAllData().size());
                                }
                                if(MyLifecycleHandler.isApplicationInForeground()){
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // UI ACTIONS COME HERE
                                            mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                                            mReloadFreshImageView.setVisibility(View.INVISIBLE);
                                            mSuggestedLinkupsRecyclerView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else {
                                    networkResponse = "1";
                                }

                            } else {
                                if(MyLifecycleHandler.isApplicationInForeground()) {
                                    tuningViewsOnError(1, statusMsg);
                                } else {
                                    networkResponse = statusMsg;
                                }
                            }
                        } else if(myStatus == 0){
                            // ACCOUNT HAS BEEN FLAGGED
                            Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS, 1);
                            Config.openActivity(StockProfileActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                            return;
                        } else {
                            if(MyLifecycleHandler.isApplicationInForeground()) {
                                tuningViewsOnError(1, statusMsg);
                            } else {
                                networkResponse = statusMsg;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(MyLifecycleHandler.isApplicationInForeground()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    tuningViewsOnError(0, "");
                                }
                            });
                        } else {
                            networkResponse = getString(R.string.login_activity_an_unexpected_error_occured);
                        }
                    }
                    REQUEST_HAS_STARTED =  false;
            }

            @Override
            public void onError(ANError anError) {
                if (MyLifecycleHandler.isApplicationInForeground()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            tuningViewsOnError(0, "");
                        }
                    }); //END OF HANDLER-4-TO-MAIN-THREAD
                    REQUEST_HAS_STARTED =  false;
                } else {
                    networkResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                }
            }
        });
    }



    private void makeRequest(){
        REQUEST_HAS_STARTED =  true;
        suggestedLinkupsFetchThread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // BACKGROUND ACTIONS COME HERE
                if(Connectivity.isConnected(StockProfileActivity.this)){
                    getSuggestedLinkUps(LocaleHelper.getLanguage(StockProfileActivity.this));
                } else {
                    tuningViewsOnError(0, "");
                    REQUEST_HAS_STARTED =  false;
                }
            }
        });
        suggestedLinkupsFetchThread2.start();
    }

    private void tuningViewsOnError(final int showToast, final String statusMsg){
        if (MyLifecycleHandler.isApplicationInForeground()) {
            REQUEST_HAS_STARTED =  false;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                        mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                        mSuggestedLinkupsRecyclerView.setVisibility(View.INVISIBLE);
                        mReloadFreshImageView.setVisibility(View.VISIBLE);
                }
            }); //END OF HANDLER-TO-MAIN-THREAD
        }
    }

}
