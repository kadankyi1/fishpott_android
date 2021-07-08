package com.fishpott.fishpott5.Activities;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.fishpott.fishpott5.ListDataGenerators.TransactionsListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.TransactionModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionsActivity extends AppCompatActivity implements View.OnClickListener {

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
        setContentView(R.layout.activity_transactions);

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
                    if(Connectivity.isConnected(TransactionsActivity.this)){
                        makeRequest();
                    } else {
                        Config.showToastType1(TransactionsActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                    }
                }
            }
        });

        mMainSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(!REQUEST_HAS_STARTED) {
                            if(Connectivity.isConnected(TransactionsActivity.this)){
                                makeRequest();
                            } else {
                                mMainSwipeRefreshLayout.setRefreshing(false);
                                Config.showToastType1(TransactionsActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                            }
                        } else {
                            mMainSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );

        if(!REQUEST_HAS_STARTED) {
            if(Connectivity.isConnected(TransactionsActivity.this)){
                makeRequest();
            } else {
                mMainSwipeRefreshLayout.setRefreshing(false);
                Config.showToastType1(TransactionsActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
            }
        }

        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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
            if(TransactionsListDataGenerator.getAllData().get(position).getType().equalsIgnoreCase("SHARES PURCHASE")){
                return 1;
            } else if(TransactionsListDataGenerator.getAllData().get(position).getType().equalsIgnoreCase("SHARES SALE")){
                return 2;
            } else if(TransactionsListDataGenerator.getAllData().get(position).getType().equalsIgnoreCase("WALLET CREDIT")){
                return 3;
            } else if(TransactionsListDataGenerator.getAllData().get(position).getType().equalsIgnoreCase("WITHDRAWAL")){
                return 4;
            } else if(TransactionsListDataGenerator.getAllData().get(position).getType().equalsIgnoreCase("SHARES TRANSFER OUT")){
                return 5;
            } else if(TransactionsListDataGenerator.getAllData().get(position).getType().equalsIgnoreCase("SHARES TRANSFER IN")){
                return 6;
            }
            return 0;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v;
            if(viewType == 1){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction_shares_purchase, parent, false);
                vh = new SharesSaleViewHolder(v);
                return vh;
            } else if(viewType == 2){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction_shares_sale, parent, false);
                vh = new WithdrawalViewHolder(v);
                return vh;
            } else if(viewType == 3){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction_credit, parent, false);
                vh = new CreditViewHolder(v);
                return vh;
            }  else if(viewType == 4){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction_withdrawal, parent, false);
                vh = new WithdrawalViewHolder(v);
                return vh;
            } else if(viewType == 5){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction_transfer_out, parent, false);
                vh = new TransferViewHolder(v);
                return vh;
            } else if(viewType == 6){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction_transfer_in, parent, false);
                vh = new TransferViewHolder(v);
                return vh;
            }

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_info_bar_rounded_corners, parent, false);
            vh = new InfoViewHolder(v);
            return vh;
        }

        public class SharesSaleViewHolder extends RecyclerView.ViewHolder {
            private TextView mDateTextView, mAmountTextView, mQuantityTextView, mStatusTextView, mNameTextView, mAddedPottName;

            public SharesSaleViewHolder(View v) {
                super(v);
                mDateTextView = itemView.findViewById(R.id.date_textview);
                mAmountTextView = itemView.findViewById(R.id.total_cost_textView);
                mQuantityTextView = itemView.findViewById(R.id.amount_textview);
                mStatusTextView = itemView.findViewById(R.id.status_textview);
                mNameTextView = itemView.findViewById(R.id.momo_type_textview);
                mAddedPottName = itemView.findViewById(R.id.info2_type_textview);
            }
        }

        public class CreditViewHolder extends RecyclerView.ViewHolder {
            private TextView mDateTextView, mAmountTextView, mStatusTextView, mNameTextView, mAddedPottName;

            public CreditViewHolder(View v) {
                super(v);
                mDateTextView = itemView.findViewById(R.id.date_textview);
                mAmountTextView = itemView.findViewById(R.id.amount_textview);
                mStatusTextView = itemView.findViewById(R.id.status_textview);
                mNameTextView = itemView.findViewById(R.id.momo_type_textview);
                mAddedPottName = itemView.findViewById(R.id.info2_type_textview);
            }
        }

        public class WithdrawalViewHolder extends RecyclerView.ViewHolder {
            private TextView mDateTextView, mAmountTextView, mStatusTextView, mQuantityTextView, mNameTextView;

            public WithdrawalViewHolder(View v) {
                super(v);
                mDateTextView = itemView.findViewById(R.id.date_textview);
                mAmountTextView = itemView.findViewById(R.id.amount_textview);
                mQuantityTextView = itemView.findViewById(R.id.info2_type_textview);
                mStatusTextView = itemView.findViewById(R.id.status_textview);
                mNameTextView = itemView.findViewById(R.id.momo_type_textview);
            }
        }


        public class TransferViewHolder extends RecyclerView.ViewHolder {
            private TextView mDateTextView, mAmountTextView, mStatusTextView, mQuantityTextView, mNameTextView, mAddedPottName ;

            public TransferViewHolder(View v) {
                super(v);
                mDateTextView = itemView.findViewById(R.id.date_textview);
                mAmountTextView = itemView.findViewById(R.id.amount_textview);
                mStatusTextView = itemView.findViewById(R.id.status_textview);
                mNameTextView = itemView.findViewById(R.id.momo_type_textview);
                mAddedPottName = itemView.findViewById(R.id.info2_type_textview);
            }
        }

        public class InfoViewHolder extends RecyclerView.ViewHolder {
            private TextView mInfoTextView;

            public InfoViewHolder(View v) {
                super(v);
                mInfoTextView = itemView.findViewById(R.id.info_textview);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof InfoViewHolder) {
                ((InfoViewHolder) holder).mInfoTextView.setText(getResources().getString(R.string.click_on_any_transaction_to_report_it));
            } else if (holder instanceof SharesSaleViewHolder) {
                ((SharesSaleViewHolder) holder).mDateTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getDate());
                ((SharesSaleViewHolder) holder).mAmountTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getQuantityOrAmount());
                ((SharesSaleViewHolder) holder).mQuantityTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getTotalCharge());
                ((SharesSaleViewHolder) holder).mStatusTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getStatusOrBuyerName());
                ((SharesSaleViewHolder) holder).mNameTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getItemNameOrReceiveNumberOrCreditType());
                ((SharesSaleViewHolder) holder).mAddedPottName.setText(TransactionsListDataGenerator.getAllData().get(position).getAddedPottName());
                setTransactionStatusColor(TransactionsListDataGenerator.getAllData().get(position).getStatusNumber(), ((SharesSaleViewHolder) holder).mStatusTextView);
                ((SharesSaleViewHolder) holder).mAddedPottName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Config.openActivity(TransactionsActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname",  TransactionsListDataGenerator.getAllData().get(position).getAddedPottName());
                    }
                });
                ((SharesSaleViewHolder) holder).mNameTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Config.openActivity(TransactionsActivity.this, StockProfileActivity.class, 1, 0, 1, "shareparentid",  TransactionsListDataGenerator.getAllData().get(position).getAddedInfo1());
                    }
                });
            } else if (holder instanceof CreditViewHolder) {
                ((CreditViewHolder) holder).mDateTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getDate());
                ((CreditViewHolder) holder).mAmountTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getTotalCharge());
                ((CreditViewHolder) holder).mStatusTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getStatusOrBuyerName());
                ((CreditViewHolder) holder).mNameTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getQuantityOrAmount());
                //((CreditViewHolder) holder).mAddedPottName.setText(TransactionsListDataGenerator.getAllData().get(position).getItemNameOrReceiveNumberOrCreditType());
                ((CreditViewHolder) holder).mAddedPottName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Config.copyToClipBoardId(getApplicationContext(), "INFO", TransactionsListDataGenerator.getAllData().get(position).getItemNameOrReceiveNumberOrCreditType());
                        Config.showToastType1(TransactionsActivity.this, "Copied");
                    }
                });
                setTransactionStatusColor(TransactionsListDataGenerator.getAllData().get(position).getStatusNumber(), ((CreditViewHolder) holder).mStatusTextView);
            } else if (holder instanceof WithdrawalViewHolder) {
                ((WithdrawalViewHolder) holder).mDateTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getDate());
                //((WithdrawalViewHolder) holder).mQuantityTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getQuantityOrAmount());
                ((WithdrawalViewHolder) holder).mQuantityTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Config.copyToClipBoardId(getApplicationContext(), "INFO", TransactionsListDataGenerator.getAllData().get(position).getQuantityOrAmount());
                        Config.showToastType1(TransactionsActivity.this, "Copied");
                    }
                });
                ((WithdrawalViewHolder) holder).mAmountTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getTotalCharge());
                ((WithdrawalViewHolder) holder).mStatusTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getStatusOrBuyerName());
                ((WithdrawalViewHolder) holder).mNameTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getItemNameOrReceiveNumberOrCreditType());
                setTransactionStatusColor(TransactionsListDataGenerator.getAllData().get(position).getStatusNumber(), ((WithdrawalViewHolder) holder).mStatusTextView);
            } else if (holder instanceof TransferViewHolder) {
                ((TransferViewHolder) holder).mDateTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getDate());
                ((TransferViewHolder) holder).mAmountTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getQuantityOrAmount());
                ((TransferViewHolder) holder).mStatusTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getStatusOrBuyerName());
                ((TransferViewHolder) holder).mNameTextView.setText(TransactionsListDataGenerator.getAllData().get(position).getItemNameOrReceiveNumberOrCreditType());
                ((TransferViewHolder) holder).mAddedPottName.setText(TransactionsListDataGenerator.getAllData().get(position).getAddedPottName());
                setTransactionStatusColor(TransactionsListDataGenerator.getAllData().get(position).getStatusNumber(), ((TransferViewHolder) holder).mStatusTextView);
                ((TransferViewHolder) holder).mAddedPottName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Config.openActivity(TransactionsActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", TransactionsListDataGenerator.getAllData().get(position).getStatusOrBuyerName());
                    }
                });
                ((TransferViewHolder) holder).mNameTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Config.openActivity(TransactionsActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "shareparentid", TransactionsListDataGenerator.getAllData().get(position).getAddedInfo1());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return TransactionsListDataGenerator.getAllData().size();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mMainSwipeRefreshLayout = findViewById(R.id.transactions_activity_swiperefreshayout);
        mReloadFreshImageView = findViewById(R.id.transactions_activity_reload_imageview);
        mLoadingProgressBar = findViewById(R.id.transactions_loader);
        if(!networkResponse.trim().equalsIgnoreCase("")){
            if(!networkResponse.trim().equalsIgnoreCase("1")){
                Config.showDialogType1(TransactionsActivity.this, "1", networkResponse, "", null, false, "", "");
            }
            mReloadFreshImageView.setVisibility(View.INVISIBLE);
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            if (mMainSwipeRefreshLayout.isRefreshing()) {
                mMainSwipeRefreshLayout.setRefreshing(false);
            }
            networkResponse = "";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mMainSwipeRefreshLayout = null;
        mReloadFreshImageView = null;
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
        networkResponse = null;
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_transactions_activity));
        Config.freeMemory();
    }

    public void setTransactionStatusColor(int status, TextView mTransactionTextView){
        if(status == 0){
            mTransactionTextView.setTextColor(getResources().getColor(R.color.colorPending));
        } else if(status == 1){
            mTransactionTextView.setTextColor(getResources().getColor(R.color.colorCompleted));
        } else if(status == 2){
            mTransactionTextView.setTextColor(getResources().getColor(R.color.colorRejected));
        } else {
            mTransactionTextView.setTextColor(getResources().getColor(R.color.colorError));
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

        AndroidNetworking.post(Config.LINK_GET_TRANSACTIONS)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", LocaleHelper.getLanguage(TransactionsActivity.this))
                .addBodyParameter("sales_lastsku", "0")
                .addBodyParameter("credit_lastsku", "0")
                .addBodyParameter("withdr_lastsku", "0")
                .addBodyParameter("cre_coup_lastsku", "0")
                .addBodyParameter("share_coup_lastsku", "0")
                .addBodyParameter("poach_last_sku", "0")
                .addBodyParameter("transfer_last_sku", "0")
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("get_transactions")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                if (MyLifecycleHandler.isApplicationInForeground()) {
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
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        final JSONObject o = array.getJSONObject(0);
                        int myStatus = o.getInt("1");
                        final String statusMsg = o.getString("2");

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3 || myStatus == 5){
                            Config.showToastType1(TransactionsActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(TransactionsActivity.this, statusMsg);
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

                                TransactionsListDataGenerator.getAllData().clear();
                                mRecyclerView.getAdapter().notifyDataSetChanged();

                                for (int i = 0; i<=linkupsSuggestionsArray.length(); i++){
                                    TransactionModel mine1 = new TransactionModel();
                                    if(i<linkupsSuggestionsArray.length()){
                                        final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                        mine1.setType(k.getString("0a"));
                                        mine1.setDate(k.getString("1"));
                                        mine1.setQuantityOrAmount(k.getString("2"));
                                        mine1.setItemNameOrReceiveNumberOrCreditType(k.getString("3"));
                                        mine1.setStatusOrBuyerName(k.getString("4"));
                                        mine1.setTotalCharge(k.getString("5"));
                                        mine1.setAddedPottName(k.getString("7"));
                                        mine1.setAddedInfo1(k.getString("8"));
                                        mine1.setStatusNumber(k.getInt("9"));

                                        TransactionsListDataGenerator.addOneData(mine1);
                                    }
                                    mRecyclerView.getAdapter().notifyItemInserted(TransactionsListDataGenerator.getAllData().size());
                                }
                            } else {
                                Config.showDialogType1(TransactionsActivity.this, "", getString(R.string.no_transactions_found), "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (MyLifecycleHandler.isApplicationInForeground()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(TransactionsActivity.this, getResources().getString(R.string.login_activity_an_unexpected_error_occured));
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
                if (MyLifecycleHandler.isApplicationInForeground()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mReloadFreshImageView.setVisibility(View.INVISIBLE);
                            mLoadingProgressBar.setVisibility(View.INVISIBLE);
                            if (mMainSwipeRefreshLayout.isRefreshing()) {
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
