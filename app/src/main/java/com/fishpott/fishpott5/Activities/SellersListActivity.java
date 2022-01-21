package com.fishpott.fishpott5.Activities;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
import com.fishpott.fishpott5.ListDataGenerators.SellersListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.sellerModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SellersListActivity extends AppCompatActivity implements View.OnClickListener{

    private SwipeRefreshLayout mMainSwipeRefreshLayout;
    private RecyclerView mSuggestedLinkupsRecyclerView;
    private ImageView mBackImageView,mReloadFreshImageView;
    private ProgressBar mLinkupsLoadingProgressBar;
    private LinearLayoutManager mLayoutManager;
    private Boolean REQUEST_HAS_STARTED = false;
    private Thread suggestedLinkupsFetchThread2 = null, imageLoaderThread = null;
    private Boolean someLinkupshaveBeenFetched = false, contactsSavedToServer = false, contactsPicked = false;
    private int requestMakerStyle = 0; // 0 = middle, 1= bottom, 2 = swipe
    private String parentSharesId = "", networkResponse = "";
    private String[] buyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers_list);

        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle !=null) {
            buyInfo = (String[]) intentBundle.get("BUY_INFO");
            parentSharesId = buyInfo[1];
            Log.e("BuySharesForSaleActivit", "shareID : " + buyInfo[0]);
            Log.e("BuySharesForSaleActivit", "shareParentID : " + buyInfo[1]);
            Log.e("BuySharesForSaleActivit", "shareName : " + buyInfo[2]);
            Log.e("BuySharesForSaleActivit", "shareQuantity : " + buyInfo[3]);
            Log.e("BuySharesForSaleActivit", "shareLogo : " + buyInfo[4]);
            if(buyInfo[1].trim().equalsIgnoreCase("")
                    || buyInfo[2].trim().equalsIgnoreCase("")
                    || buyInfo[4].trim().equalsIgnoreCase("")){
                Config.showToastType1(SellersListActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
                finish();
            }
        } else {
            Config.showToastType1(SellersListActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
            finish();
        }
        SellersListDataGenerator.getAllData().clear();
        mMainSwipeRefreshLayout = findViewById(R.id.activity_suggestedlinkupsactivity_swiperefreshayout);
        mSuggestedLinkupsRecyclerView = findViewById(R.id.activity_suggestedlinkupsactivity_recyclerview);
        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mReloadFreshImageView = findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = findViewById(R.id.activity_suggestedlinkupsactivity_loader);

        mLayoutManager = new LinearLayoutManager(SellersListActivity.this);

        mSuggestedLinkupsRecyclerView.setItemViewCacheSize(20);
        mSuggestedLinkupsRecyclerView.setDrawingCacheEnabled(true);
        mSuggestedLinkupsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mSuggestedLinkupsRecyclerView.setLayoutManager(mLayoutManager);
        mSuggestedLinkupsRecyclerView.setAdapter(new RecyclerViewAdapter(this));
        mSuggestedLinkupsRecyclerView.getAdapter().notifyDataSetChanged();

        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mReloadFreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!REQUEST_HAS_STARTED && Connectivity.isConnected(SellersListActivity.this)) {
                    mReloadFreshImageView.setVisibility(View.INVISIBLE);
                    mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
                    requestMakerStyle = 0;
                    makeRequest();
                }
            }
        });

        mMainSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(!REQUEST_HAS_STARTED && Connectivity.isConnected(SellersListActivity.this)){
                            requestMakerStyle = 2;
                            makeRequest();
                        } else {
                            mMainSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );

        // FETCHING FIRST BATCH OF LINKUPS BEFORE GETTING CONTACTS
        if(Connectivity.isConnected(SellersListActivity.this)){
            REQUEST_HAS_STARTED =  true;

            suggestedLinkupsFetchThread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    getSuggestedLinkUps(LocaleHelper.getLanguage(SellersListActivity.this));
                }
            });
            suggestedLinkupsFetchThread2.start();
        } else {
            tuningViewsOnError(0, "");
            REQUEST_HAS_STARTED =  false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        mMainSwipeRefreshLayout = findViewById(R.id.activity_suggestedlinkupsactivity_swiperefreshayout);
        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mReloadFreshImageView = findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = findViewById(R.id.activity_suggestedlinkupsactivity_loader);

        if(!networkResponse.trim().equalsIgnoreCase("")){
            if(!networkResponse.trim().equalsIgnoreCase("1")) {
                Config.showDialogType1(SellersListActivity.this, "1", networkResponse, "", null, false, "", "");
                tuningViewsOnError(0,"");
            } else {
                mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                mReloadFreshImageView.setVisibility(View.INVISIBLE);
                if(mMainSwipeRefreshLayout.isRefreshing()){
                    mMainSwipeRefreshLayout.setRefreshing(false);
                }
                mSuggestedLinkupsRecyclerView.setVisibility(View.VISIBLE);
            }
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMainSwipeRefreshLayout = null;
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

        mMainSwipeRefreshLayout = null;
        mSuggestedLinkupsRecyclerView = null;
        mBackImageView = null;
        mReloadFreshImageView = null;
        mLinkupsLoadingProgressBar = null;
        mLayoutManager = null;
        parentSharesId = null;
        networkResponse = null;

        if(suggestedLinkupsFetchThread2 != null){
            suggestedLinkupsFetchThread2.interrupt();
            suggestedLinkupsFetchThread2 = null;
        }

        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_sellerslist_activity));
        Config.freeMemory();
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
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_seller_offer, parent, false);
            vh = new SuggestedLinkupViewHolder(v);

            return vh;
        }

        public class SuggestedLinkupViewHolder extends RecyclerView.ViewHolder {
            private TextView mSellerPotNameTextView, mPricePerShareTextView, mQuantityForSaleTextView;
            private CircleImageView mSellerPotPicImageview;
            private Button mBuyButton;
            private ImageView mBlueVerifiedIconImageView, mGreenVerifiedIconImageView, mBusinessAccountIconImageView;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public SuggestedLinkupViewHolder(View v) {
                super(v);
                mSellerPotPicImageview = itemView.findViewById(R.id.chat_pott_picture_circleimageview);
                mBlueVerifiedIconImageView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_verfiedblue_imageview);
                mGreenVerifiedIconImageView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_verfiedgreen_imageview);
                mBusinessAccountIconImageView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_businessicon_imageview);
                mSellerPotNameTextView = itemView.findViewById(R.id.chat_full_name_textview);
                mPricePerShareTextView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_fullname_textView);
                mQuantityForSaleTextView = itemView.findViewById(R.id.list_item_sellerlistactivity_quantity_textView);
                mBuyButton = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_linkup_button);

                // ALL ON-CLICK LISTENERS
                mSellerPotPicImageview.setOnClickListener(innerClickListener);
                mBlueVerifiedIconImageView.setOnClickListener(innerClickListener);
                mGreenVerifiedIconImageView.setOnClickListener(innerClickListener);
                mSellerPotNameTextView.setOnClickListener(innerClickListener);
                mBuyButton.setOnClickListener(innerClickListener);
            }
        }


        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((SuggestedLinkupViewHolder) holder).mSellerPotNameTextView.setText("@"+SellersListDataGenerator.getAllData().get(position).getSellerPottName());
            ((SuggestedLinkupViewHolder) holder).mPricePerShareTextView.setText(SellersListDataGenerator.getAllData().get(position).getOfferPricePerShare());
            ((SuggestedLinkupViewHolder) holder).mQuantityForSaleTextView.setText(SellersListDataGenerator.getAllData().get(position).getOfferQuantity());
            if (SellersListDataGenerator.getAllData().get(position).getSellerVerifiedStatus() == 1) {
                ((SuggestedLinkupViewHolder) holder).mBlueVerifiedIconImageView.setVisibility(View.INVISIBLE);
                ((SuggestedLinkupViewHolder) holder).mGreenVerifiedIconImageView.setVisibility(View.VISIBLE);
            } else if (SellersListDataGenerator.getAllData().get(position).getSellerVerifiedStatus() == 2) {
                ((SuggestedLinkupViewHolder) holder).mGreenVerifiedIconImageView.setVisibility(View.INVISIBLE);
                ((SuggestedLinkupViewHolder) holder).mBlueVerifiedIconImageView.setVisibility(View.VISIBLE);
            } else {
                ((SuggestedLinkupViewHolder) holder).mGreenVerifiedIconImageView.setVisibility(View.INVISIBLE);
                ((SuggestedLinkupViewHolder) holder).mBlueVerifiedIconImageView.setVisibility(View.INVISIBLE);
            }
            if(SellersListDataGenerator.getAllData().get(position).getSellerAccountType() == 1){
                ((SuggestedLinkupViewHolder) holder).mBusinessAccountIconImageView.setVisibility(View.VISIBLE);
            } else {
                ((SuggestedLinkupViewHolder) holder).mBusinessAccountIconImageView.setVisibility(View.GONE);
            }
            if(SellersListDataGenerator.getAllData().get(position).getSellerProfilePicture().trim().length() > 1){

                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadUrlImage(SellersListActivity.this, true, SellersListDataGenerator.getAllData().get(position).getSellerProfilePicture().trim(), ((SuggestedLinkupViewHolder) holder).mSellerPotPicImageview, 0, 60, 60);
                    }
                });
                imageLoaderThread.start();

            } else {
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadErrorImageView(SellersListActivity.this, R.drawable.setprofilepicture_activity_imageholder_default_image, ((SuggestedLinkupViewHolder) holder).mSellerPotPicImageview, 60, 60);
                    }
                });
                imageLoaderThread.start();
            }

        }

        @Override
        public void onViewRecycled(@NonNull final RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder instanceof SellersListActivity.RecyclerViewAdapter.SuggestedLinkupViewHolder) {
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Config.loadErrorImageView(SellersListActivity.this, R.drawable.setprofilepicture_activity_imageholder_default_image, ((SellersListActivity.RecyclerViewAdapter.SuggestedLinkupViewHolder) holder).mLinkupProfilePicImageView, 60, 60);
                        Log.e("onViewRecycled", "View Recycled. Error Image Loaded");
                    }
                });
                imageLoaderThread.start();

            }
        }

        @Override
        public int getItemCount() {
            return SellersListDataGenerator.getAllData().size();
        }


    }


    private void allOnClickHandlers(View v, int position) {
        if(     v.getId() == R.id.chat_pott_picture_circleimageview
                || v.getId() == R.id.list_item_suggestedlinkupsactivity_activity_verfiedgreen_imageview
                || v.getId() == R.id.list_item_suggestedlinkupsactivity_activity_verfiedblue_imageview
                || v.getId() == R.id.chat_full_name_textview ){
            Config.openActivity(SellersListActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", SellersListDataGenerator.getAllData().get(position).getSellerPottName());
        } else if(v.getId() == R.id.list_item_suggestedlinkupsactivity_activity_linkup_button){

            buyInfo[0] = SellersListDataGenerator.getAllData().get(position).getShareId();
            buyInfo[3] = SellersListDataGenerator.getAllData().get(position).getOfferQuantity();
            buyInfo[5] = SellersListDataGenerator.getAllData().get(position).getNewsId();
            Config.openActivity4(SellersListActivity.this, BuySharesForSaleActivity.class, 1, 0, 1, "BUY_INFO", buyInfo);
        }
    }

    public void getSuggestedLinkUps(final String language){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                REQUEST_HAS_STARTED =  true;
                // UI ACTIONS COME HERE
                if(requestMakerStyle == 0){ // made by the middle loader
                    mReloadFreshImageView.setVisibility(View.INVISIBLE);
                    //mSuggestedLinkupsRecyclerView.setVisibility(View.INVISIBLE);
                    mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
                }
            }
        }); //END OF HANDLER-1-TO-MAIN-THREAD

        AndroidNetworking.post(Config.LINK_GET_SELLERS_OF_SHARES)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("parentsharesid", parentSharesId)
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("get_sellers")
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

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(SellersListActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(SellersListActivity.this, statusMsg);
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

                                SellersListDataGenerator.getAllData().clear();
                                mSuggestedLinkupsRecyclerView.getAdapter().notifyDataSetChanged();

                                for (int i = 0; i<=linkupsSuggestionsArray.length(); i++){
                                    sellerModel mine1 = new sellerModel();
                                    if(i<linkupsSuggestionsArray.length()){
                                        final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                        mine1.setSellerPottName(k.getString("0a"));
                                        mine1.setSellerVerifiedStatus(k.getInt("1"));
                                        mine1.setSellerProfilePicture(k.getString("2"));
                                        mine1.setOfferQuantity(k.getString("3"));
                                        mine1.setOfferPricePerShare(k.getString("4"));
                                        mine1.setSellerAccountType(k.getInt("5"));
                                        mine1.setShareId(k.getString("6"));
                                        mine1.setNewsId(k.getString("7"));

                                        if(!k.getString("2").trim().equalsIgnoreCase("")){
                                            final String thisImageurl = k.getString("2").trim();
                                            imageLoaderThread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Config.loadUrlImage(SellersListActivity.this, false, thisImageurl, null, 0, 60, 60);
                                                }
                                            });
                                            imageLoaderThread.start();


                                        }
                                        SellersListDataGenerator.addOneData(mine1);
                                    }
                                    mSuggestedLinkupsRecyclerView.getAdapter().notifyItemInserted(SellersListDataGenerator.getAllData().size());
                                }
                                someLinkupshaveBeenFetched = true;
                                if(MyLifecycleHandler.isApplicationInForeground()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // UI ACTIONS COME HERE
                                            if (requestMakerStyle == 0) { // made by the middle loader
                                                mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                                                mReloadFreshImageView.setVisibility(View.INVISIBLE);
                                            } else if (requestMakerStyle == 1) { // made by the bottom loader

                                            } else if (requestMakerStyle == 2) {  // made by the swipe loader
                                                mMainSwipeRefreshLayout.setRefreshing(false);
                                            }
                                            mSuggestedLinkupsRecyclerView.setVisibility(View.VISIBLE);
                                        }
                                    }); //END OF HANDLER-TO-MAIN-THREAD
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
                            Config.openActivity(SellersListActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
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
                            }); //END OF HANDLER-TO-MAIN-THREAD
                        } else {
                            networkResponse  = getString(R.string.login_activity_an_unexpected_error_occured);
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
                    networkResponse  = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
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
                if(Connectivity.isConnected(SellersListActivity.this)){
                    getSuggestedLinkUps(LocaleHelper.getLanguage(SellersListActivity.this));
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
                    if(requestMakerStyle == 0){ // made by the middle loader
                        mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                        mSuggestedLinkupsRecyclerView.setVisibility(View.INVISIBLE);
                        mReloadFreshImageView.setVisibility(View.VISIBLE);
                    } else if(requestMakerStyle == 1){ // made by the bottom loader
                        if(!SellersListDataGenerator.getAllData().isEmpty()){
                            mSuggestedLinkupsRecyclerView.getAdapter().notifyItemChanged(SellersListDataGenerator.getAllData().size()-1);
                        }
                    } else if(requestMakerStyle == 2){  // made by the swipe loader
                        mMainSwipeRefreshLayout.setRefreshing(false);
                        if(someLinkupshaveBeenFetched){
                        } else {
                            if(showToast == 1){
                                Config.showToastType1(SellersListActivity.this, statusMsg);
                            }
                        }
                    }
                }
            }); //END OF HANDLER-TO-MAIN-THREAD
        }
    }

}
