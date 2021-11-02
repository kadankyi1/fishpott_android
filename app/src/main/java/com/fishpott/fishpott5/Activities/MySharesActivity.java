package com.fishpott.fishpott5.Activities;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.MySharesViewingListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.SharesModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MySharesActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackImageView, mReloadFreshImageView;
    private SwipeRefreshLayout mMainSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingProgressBar;
    private LinearLayoutManager mLayoutManager;
    private Boolean REQUEST_HAS_STARTED = false;
    private Thread imageLoaderThread = null, networkRequestThread = null;
    private String networkResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_shares);

        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mMainSwipeRefreshLayout = findViewById(R.id.transactions_activity_swiperefreshayout);
        mRecyclerView = findViewById(R.id.transactions_activity_recyclerview);
        mReloadFreshImageView = findViewById(R.id.transactions_activity_reload_imageview);
        mLoadingProgressBar = findViewById(R.id.transactions_loader);

        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new RecyclerViewAdapter(this));

        mReloadFreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!REQUEST_HAS_STARTED) {
                    if(Connectivity.isConnected(MySharesActivity.this)){
                        networkRequestThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                makeRequest();
                            }
                        });
                        networkRequestThread.start();
                    } else {
                        Config.showToastType1(MySharesActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                    }
                }
            }
        });

        mMainSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(!REQUEST_HAS_STARTED) {
                            if(Connectivity.isConnected(MySharesActivity.this)){
                                networkRequestThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        makeRequest();
                                    }
                                });
                                networkRequestThread.start();
                            } else {
                                mMainSwipeRefreshLayout.setRefreshing(false);
                                Config.showToastType1(MySharesActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                            }
                        } else {
                            mMainSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );

        if(!REQUEST_HAS_STARTED) {
            if(Connectivity.isConnected(MySharesActivity.this)){
                networkRequestThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        makeRequest();
                    }
                });
                networkRequestThread.start();
            } else {
                mMainSwipeRefreshLayout.setRefreshing(false);
                Config.showToastType1(MySharesActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
            }
        }

        mBackImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.title_bar_back_icon_imageview){
            onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mReloadFreshImageView = findViewById(R.id.transactions_activity_reload_imageview);
        mMainSwipeRefreshLayout = findViewById(R.id.transactions_activity_swiperefreshayout);
        mLoadingProgressBar = findViewById(R.id.transactions_loader);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            if(!networkResponse.trim().equalsIgnoreCase("1")){
                Config.showDialogType1(MySharesActivity.this, "1", networkResponse, "", null, false, "", "");
            }
            mReloadFreshImageView.setVisibility(View.INVISIBLE);
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            if(mMainSwipeRefreshLayout.isRefreshing()){
                mMainSwipeRefreshLayout.setRefreshing(false);
            }
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mReloadFreshImageView = null;
        mMainSwipeRefreshLayout = null;
        mLoadingProgressBar = null;
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

        mBackImageView = null;
        mReloadFreshImageView = null;
        mMainSwipeRefreshLayout = null;
        mRecyclerView = null;
        mLoadingProgressBar = null;
        mLayoutManager = null;
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_myshares_activity));
        Config.freeMemory();
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
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_my_shares, parent, false);
            vh = new ViewHolder(v);
            return vh;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView mProfitLossValueInfoTextView, mTotalCostTextView, mValuePerShareTextView, mQuantityTextView
                    , mShareNameTextView, mInfoTextView;

            public ViewHolder(View v) {
                super(v);
                mProfitLossValueInfoTextView = itemView.findViewById(R.id.type_textview);
                mTotalCostTextView = itemView.findViewById(R.id.total_cost_textView);
                mValuePerShareTextView = itemView.findViewById(R.id.amount_textview);
                mQuantityTextView = itemView.findViewById(R.id.status_textview);
                mShareNameTextView = itemView.findViewById(R.id.momo_type_textview);
                mInfoTextView = itemView.findViewById(R.id.info_textview);
            }
        }


        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if(MySharesViewingListDataGenerator.getAllData().get(position).getProfitOrLoss().equalsIgnoreCase("Value Profit")){
                ((ViewHolder) holder).mProfitLossValueInfoTextView.setText("Value Profit");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((ViewHolder) holder).mProfitLossValueInfoTextView.setTextColor(getColor(R.color.colorYardsale));
                }
            } else if(MySharesViewingListDataGenerator.getAllData().get(position).getProfitOrLoss().equalsIgnoreCase("Value Loss")){
                ((ViewHolder) holder).mProfitLossValueInfoTextView.setText("Value Loss");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((ViewHolder) holder).mProfitLossValueInfoTextView.setTextColor(getColor(R.color.newsBackgroundDeepRed2));
                }
            } else {
                ((ViewHolder) holder).mProfitLossValueInfoTextView.setText("Value Unchanged");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((ViewHolder) holder).mProfitLossValueInfoTextView.setTextColor(getColor(R.color.colorEventsDark));
                }
            }

            ((ViewHolder) holder).mTotalCostTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) +  MySharesViewingListDataGenerator.getAllData().get(position).getSharesCostPricePerShare());
            ((ViewHolder) holder).mValuePerShareTextView.setText(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + MySharesViewingListDataGenerator.getAllData().get(position).getSharesMaxPricePerShare());
            ((ViewHolder) holder).mQuantityTextView.setText(MySharesViewingListDataGenerator.getAllData().get(position).getSharesAvailableQuantity());
            ((ViewHolder) holder).mShareNameTextView.setText(MySharesViewingListDataGenerator.getAllData().get(position).getSharesName());
            ((ViewHolder) holder).mInfoTextView.setText(MySharesViewingListDataGenerator.getAllData().get(position).getSharesDividendPerShare());
        }


        @Override
        public int getItemCount() {
            return MySharesViewingListDataGenerator.getAllData().size();
        }


    }



    public void makeRequest(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                REQUEST_HAS_STARTED =  true;
                mReloadFreshImageView.setVisibility(View.INVISIBLE);
                mLoadingProgressBar.setVisibility(View.VISIBLE);
            }
        }); //END OF HANDLER-1-TO-MAIN-THREAD

        AndroidNetworking.post(Config.LINK_GET_MY_SHARES)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(MySharesActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(MySharesActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(MySharesActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(MySharesActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(MySharesActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(MySharesActivity.this.getApplicationContext())))
                .setTag("get_my_shares")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("get_my_shares", response);
                if (!MySharesActivity.this.isFinishing()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mReloadFreshImageView.setVisibility(View.INVISIBLE);
                            mLoadingProgressBar.setVisibility(View.INVISIBLE);
                            if (mMainSwipeRefreshLayout.isRefreshing()) {
                                mMainSwipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }); //END OF HANDLER-TO-MAIN-THREAD
                } else {
                    networkResponse = "1";
                }
                    try {

                        final JSONObject o = new JSONObject(response);
                        int myStatus = o.getInt("status");
                        final String statusMsg = o.getString("message");

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3 || myStatus == 5){
                            Config.showToastType1(MySharesActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(MySharesActivity.this, statusMsg);
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(MySharesActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceBoolean(MySharesActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                        Config.setSharedPreferenceInt(MySharesActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                        if (myStatus == 1) {
                            JSONArray linkupsSuggestionsArray = new JSONObject(response).getJSONArray("data");
                            // LIST RESULTS SETTING COMES HERE
                            if(linkupsSuggestionsArray.length() > 0){

                                MySharesViewingListDataGenerator.getAllData().clear();
                                mRecyclerView.getAdapter().notifyDataSetChanged();

                                for (int i = 0; i<=linkupsSuggestionsArray.length(); i++){
                                    SharesModel mine1 = new SharesModel();
                                    if(i<linkupsSuggestionsArray.length()){
                                        final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                        mine1.setSharesName(k.getString("business_name"));
                                        mine1.setSharesId(k.getString("business_id"));
                                        mine1.setSharesParentId("");
                                        mine1.setSharesAvailableQuantity(k.getString("quantity_of_stocks"));
                                        mine1.setSharesCostPricePerShare(k.getString("cost_per_share_usd"));
                                        mine1.setSharesMaxPricePerShare(k.getString("value_per_share_usd"));
                                        mine1.setSharesDividendPerShare(k.getString("ai_info"));
                                        mine1.setProfitOrLoss(k.getString("value_phrase"));
                                        MySharesViewingListDataGenerator.addOneData(mine1);
                                    }
                                    mRecyclerView.getAdapter().notifyItemInserted(MySharesViewingListDataGenerator.getAllData().size());
                                }
                            } else {
                                Config.showDialogType1(MySharesActivity.this, "", getString(R.string.no_transactions_found), "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (!MySharesActivity.this.isFinishing()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(MySharesActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
                                    mReloadFreshImageView.setVisibility(View.INVISIBLE);
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    if (mMainSwipeRefreshLayout.isRefreshing()) {
                                        mMainSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }
                            }); //END OF HANDLER-TO-MAIN-THREAD
                        } else {
                            networkResponse = getResources().getString(R.string.login_activity_an_unexpected_error_occured);
                        }
                    }
                    REQUEST_HAS_STARTED =  false;
            }

            @Override
            public void onError(ANError anError) {
                if (!MySharesActivity.this.isFinishing()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mReloadFreshImageView.setVisibility(View.INVISIBLE);
                            mLoadingProgressBar.setVisibility(View.INVISIBLE);
                            if(mMainSwipeRefreshLayout.isRefreshing()){
                                mMainSwipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    });
                } else {
                    networkResponse = "1";
                }
            }
        });
    }
}
