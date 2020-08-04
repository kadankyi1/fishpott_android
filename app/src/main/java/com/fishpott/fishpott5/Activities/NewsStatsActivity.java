package com.fishpott.fishpott5.Activities;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.NewsStatsListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.NewsStatsModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsStatsActivity extends AppCompatActivity implements View.OnClickListener {

    private SwipeRefreshLayout mMainSwipeRefreshLayout;
    private String newsId = "", statsType = "";
    private ImageView mBackImageView, mSendIconImageView,mReloadFreshImageView;
    private ProgressBar mLinkupsLoadingProgressBar;
    private EditText mMessageEditText;
    private LinearLayoutManager mLayoutManager;
    public  RecyclerView mConversationRecyclerView;
    private Thread suggestedLinkupsFetchThread2 = null, imageLoaderThread = null;
    private Boolean REQUEST_HAS_STARTED = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_stats);
        NewsStatsListDataGenerator.getAllData().clear();

        // GETTING THE INFORMATION SENT FROM THE PRIOR ACTIVITY/FRAGMENT
        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle !=null) {
            newsId =(String) intentBundle.get("id");
            statsType =(String) intentBundle.get("stats_type");
            if(newsId.trim().equalsIgnoreCase("") || statsType.trim().equalsIgnoreCase("")){
                finish();
            }
        } else {
            Config.showToastType1(NewsStatsActivity.this, getString(R.string.an_error_occurred));
            finish();
        }


        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mSendIconImageView = findViewById(R.id.sendicon_contacts_activity_imageview);
        mMessageEditText = findViewById(R.id.messagebox_contacts_activity_edittext);
        mConversationRecyclerView = findViewById(R.id.chat_messages_recyclerview);
        mMainSwipeRefreshLayout = findViewById(R.id.activity_suggestedlinkupsactivity_swiperefreshayout);
        mReloadFreshImageView = findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = findViewById(R.id.activity_suggestedlinkupsactivity_loader);

        if(statsType.trim().equalsIgnoreCase("4")){
            mMessageEditText.setVisibility(View.VISIBLE);
            mSendIconImageView.setVisibility(View.VISIBLE);
        }

        //SETTING UP RECYCLERVIEW
        mLayoutManager = new LinearLayoutManager(this);

        mConversationRecyclerView.setItemViewCacheSize(20);
        mConversationRecyclerView.setDrawingCacheEnabled(true);
        mConversationRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mConversationRecyclerView.setLayoutManager(mLayoutManager);
        mConversationRecyclerView.setAdapter(new RecyclerViewAdapter(this));

        if(Connectivity.isConnected(NewsStatsActivity.this)){
            if(!REQUEST_HAS_STARTED){
                REQUEST_HAS_STARTED =  true;
                getStats(statsType, newsId, LocaleHelper.getLanguage(NewsStatsActivity.this));
            }
        } else {
            tuningViewsOnError(0, "");
            REQUEST_HAS_STARTED =  false;
        }

        mBackImageView.setOnClickListener(this);
        mSendIconImageView.setOnClickListener(this);
        mReloadFreshImageView.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mSendIconImageView = findViewById(R.id.sendicon_contacts_activity_imageview);
        mMessageEditText = findViewById(R.id.messagebox_contacts_activity_edittext);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mSendIconImageView = null;
        mMessageEditText = null;
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
        newsId = null;
        statsType = null;
        mBackImageView = null;
        mSendIconImageView = null;
        mReloadFreshImageView = null;
        mLinkupsLoadingProgressBar = null;
        mMessageEditText = null;
        mLayoutManager = null;
        mConversationRecyclerView = null;
        if(suggestedLinkupsFetchThread2 != null){
            suggestedLinkupsFetchThread2.interrupt();
            suggestedLinkupsFetchThread2 = null;
        }
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_newstats_activity));
        Config.freeMemory();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == mBackImageView.getId()){
            onBackPressed();
        } else if(view.getId() == mSendIconImageView.getId()){
            if(mMessageEditText.getText().toString().trim().length() > 0){
                view.startAnimation(AnimationUtils.loadAnimation(NewsStatsActivity.this, R.anim.main_activity_onclick_icon_anim));
                String message = mMessageEditText.getText().toString().trim();
                sendComment(newsId, NewsStatsListDataGenerator.getAllData().size(), message, LocaleHelper.getLanguage(getApplicationContext()));
                mSendIconImageView.startAnimation(AnimationUtils.loadAnimation(NewsStatsActivity.this, R.anim.main_activity_onclick_icon_anim));
            }
        } else if(view.getId() == mReloadFreshImageView.getId()){
            if(Connectivity.isConnected(NewsStatsActivity.this)){
                suggestedLinkupsFetchThread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getStats(statsType, newsId, LocaleHelper.getLanguage(NewsStatsActivity.this));
                    }
                });
                suggestedLinkupsFetchThread2.start();
            } else {
                tuningViewsOnError(0, "");
                REQUEST_HAS_STARTED =  false;
            }
        }
    }


    public void getStats(String statsType, String newsId, final String language){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                REQUEST_HAS_STARTED = true;
                mReloadFreshImageView.setVisibility(View.INVISIBLE);
                mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
            }
        });

        Log.e("NewsStatsG", "log_phone : " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
        Log.e("NewsStatsG", "log_pass_token : " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD));
        Log.e("NewsStatsG", "mypottname : " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
        Log.e("NewsStatsG", "news_id : " + newsId);
        Log.e("NewsStatsG", "stats_type : " + statsType);
        Log.e("NewsStatsG", "my_currency : " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY));
        Log.e("NewsStatsG", "language : " + language);
        Log.e("NewsStatsG", "app_version_code : " + String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)));

        AndroidNetworking.post(Config.LINK_GET_STATS)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("news_id", newsId)
                .addBodyParameter("stats_type", statsType)
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("get_stats")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("NewsStatsG", "response : " + response);
                if (MyLifecycleHandler.isApplicationVisible() && mConversationRecyclerView != null) {
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
                            Config.showToastType1(NewsStatsActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(NewsStatsActivity.this, statusMsg);
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

                                NewsStatsListDataGenerator.getAllData().clear();
                                mConversationRecyclerView.getAdapter().notifyDataSetChanged();

                                for (int i = 0; i<=linkupsSuggestionsArray.length(); i++){
                                    NewsStatsModel mine1 = new NewsStatsModel();
                                    if(i<linkupsSuggestionsArray.length()){
                                        final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                        mine1.setStatsType(k.getInt("0a"));
                                        mine1.setNewsId(k.getString("1"));
                                        mine1.setReactionOrComment(k.getString("2"));
                                        mine1.setDate(k.getString("3"));
                                        mine1.setSenderPottName(k.getString("4"));
                                        mine1.setSenderPottPic(k.getString("5"));
                                        mine1.setSendStatus(1);
                                        mine1.setSentLocally(false);

                                        if(!k.getString("5").trim().equalsIgnoreCase("")){
                                            final String thisImageurl = k.getString("5").trim();
                                            imageLoaderThread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Config.loadUrlImage(NewsStatsActivity.this, false, thisImageurl, null, 0, 60, 60);
                                                }
                                            });
                                            imageLoaderThread.start();


                                        }
                                        NewsStatsListDataGenerator.addOneData(mine1);
                                    }
                                    mConversationRecyclerView.getAdapter().notifyItemInserted(NewsStatsListDataGenerator.getAllData().size());
                                }

                            }

                            if(MyLifecycleHandler.isApplicationVisible()) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mMainSwipeRefreshLayout.isRefreshing()) {
                                            mMainSwipeRefreshLayout.setRefreshing(false);
                                        }
                                        mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                                        mReloadFreshImageView.setVisibility(View.INVISIBLE);
                                        mConversationRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                }); //END OF HANDLER-TO-MAIN-THREAD
                            }
                        } else if(myStatus == 0){
                            // ACCOUNT HAS BEEN FLAGGED
                            Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS, 1);
                            Config.openActivity(NewsStatsActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                            return;
                        } else {
                            tuningViewsOnError(1, statusMsg);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        tuningViewsOnError(0, "");
                    }
                }
                REQUEST_HAS_STARTED =  false;
            }

            @Override
            public void onError(ANError anError) {
                tuningViewsOnError(0, "");
                REQUEST_HAS_STARTED =  false;
            }
        });
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
            return NewsStatsListDataGenerator.getAllData().get(position).getStatsType();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            // 0 = Likes, 1 = Dislikes, 2 = Views, 3 = Purchases, 4= Comments
            if (viewType == 0) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_like, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == 1) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_dislike, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == 2) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_view, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == 3) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_purchases, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == 4) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_comment, parent, false);
                vh = new CommentViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_view, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            }

            return vh;
        }

        public class LikeDislikeViewPurchasesViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mDateTextView;
            private CircleImageView mPottPicCircleImageView;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public LikeDislikeViewPurchasesViewHolder(View v) {
                super(v);

                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.chat_pott_picture_circleimageview);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.chat_full_name_textview);
                mDateTextView = (TextView) itemView.findViewById(R.id.date_textview);

                mPottNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
            }
        }

        public class CommentViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mDateTextView, mCommentTextView;
            private ImageView mErrorIconImageView;
            private ConstraintLayout mRootViewConstraintLayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public CommentViewHolder(View v) {
                super(v);

                mRootViewConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.comment_rootview);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.chat_full_name_textview);
                mDateTextView = (TextView) itemView.findViewById(R.id.date_textview);
                mCommentTextView = (TextView) itemView.findViewById(R.id.comment_textview);
                mErrorIconImageView = (ImageView) itemView.findViewById(R.id.unsent_icon);

                mRootViewConstraintLayout.setOnClickListener(innerClickListener);
                mPottNameTextView.setOnClickListener(innerClickListener);
                mErrorIconImageView.setOnClickListener(innerClickListener);
            }
        }

        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof CommentViewHolder) {
                //SETTING ON-CLICK LISTENER
                ((CommentViewHolder) holder).mPottNameTextView.setText(NewsStatsListDataGenerator.getAllData().get(position).getSenderPottName());
                ((CommentViewHolder) holder).mDateTextView.setText(NewsStatsListDataGenerator.getAllData().get(position).getDate());
                ((CommentViewHolder) holder).mCommentTextView.setText(NewsStatsListDataGenerator.getAllData().get(position).getReactionOrComment());

                if(NewsStatsListDataGenerator.getAllData().get(position).getStatsType() == Config.UNSENT){
                    ((CommentViewHolder) holder).mErrorIconImageView.setVisibility(View.VISIBLE);
                } else {
                    ((CommentViewHolder) holder).mErrorIconImageView.setVisibility(View.GONE);
                }
                Config.linkifyAllMentions(getApplicationContext(), ((CommentViewHolder) holder).mCommentTextView);
            } else {
                //SETTING ON-CLICK LISTENER
                ((LikeDislikeViewPurchasesViewHolder) holder).mPottNameTextView.setText(NewsStatsListDataGenerator.getAllData().get(position).getSenderPottName());
                ((LikeDislikeViewPurchasesViewHolder) holder).mDateTextView.setText(NewsStatsListDataGenerator.getAllData().get(position).getDate());

                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(NewsStatsListDataGenerator.getAllData().get(position).getSenderPottPic().trim().equalsIgnoreCase("")){
                            Config.loadUrlImage(NewsStatsActivity.this, true, NewsStatsListDataGenerator.getAllData().get(position).getSenderPottPic().trim(), ((LikeDislikeViewPurchasesViewHolder) holder).mPottPicCircleImageView, 0, 60, 60);
                        } else {
                            Config.loadUrlImage(NewsStatsActivity.this, true, NewsStatsListDataGenerator.getAllData().get(position).getSenderPottPic().trim(), ((LikeDislikeViewPurchasesViewHolder) holder).mPottPicCircleImageView, 0, 60, 60);
                        }
                    }
                });
                imageLoaderThread.start();
            }
        }

        @Override
        public int getItemCount() {
            return NewsStatsListDataGenerator.getAllData().size();
        }
    }


    private void tuningViewsOnError(final int showToast, final String statusMsg){
        if (MyLifecycleHandler.isApplicationVisible()) {
            REQUEST_HAS_STARTED =  false;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                    mReloadFreshImageView.setVisibility(View.VISIBLE);
                    if(mMainSwipeRefreshLayout.isRefreshing()){
                        mMainSwipeRefreshLayout.setRefreshing(false);
                    }
                    if(showToast == 1){
                        Config.showToastType1(NewsStatsActivity.this, statusMsg);
                    }
                }
            }); //END OF HANDLER-TO-MAIN-THREAD
        }
    }

    private void sendComment(String newsId, final int listPosition, final String comment, String language){
        final NewsStatsModel newsStatsModel = new NewsStatsModel();
        newsStatsModel.setStatsType(4);
        newsStatsModel.setNewsId(newsId);
        newsStatsModel.setReactionOrComment(comment);
        newsStatsModel.setDate(Config.getCurrentDateTime3("MMM d"));
        newsStatsModel.setSenderPottName(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));

        if(Connectivity.isConnected(getApplicationContext())){
            newsStatsModel.setSendStatus(Config.SENT);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mMessageEditText.setText("");
                    NewsStatsListDataGenerator.addOneData(newsStatsModel);
                    mConversationRecyclerView.getAdapter().notifyItemInserted(NewsStatsListDataGenerator.getAllData().size());
                    mConversationRecyclerView.getLayoutManager().scrollToPosition(NewsStatsListDataGenerator.getAllData().size()-1);

                }
            });

            AndroidNetworking.post(Config.LINK_SEND_COMMENT)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("news_id", newsId)
                    .addBodyParameter("comment_message", comment)
                    .addBodyParameter("language", language)
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("send_comment")
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
                            resetUiForUnsentMessage(listPosition);
                        }

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(NewsStatsActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(NewsStatsActivity.this, statusMsg);
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        resetUiForUnsentMessage(listPosition);
                    }
                }

                @Override
                public void onError(ANError anError) {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            resetUiForUnsentMessage(listPosition);
                        }
                    });
                }
            });

        } else {
            newsStatsModel.setSendStatus(Config.UNSENT);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    NewsStatsListDataGenerator.addOneData(newsStatsModel);
                    mConversationRecyclerView.getAdapter().notifyItemInserted(NewsStatsListDataGenerator.getAllData().size());
                    mConversationRecyclerView.getLayoutManager().scrollToPosition(NewsStatsListDataGenerator.getAllData().size()-1);

                }
            });
        }

    }


    private void allOnClickHandlers(View v, int position) {
        if(     v.getId() == R.id.chat_pott_picture_circleimageview || v.getId() == R.id.chat_full_name_textview ){
            Config.openActivity(NewsStatsActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", NewsStatsListDataGenerator.getAllData().get(position).getSenderPottName());
        } else if(v.getId() == R.id.comment_rootview){
            if(Connectivity.isConnected(getApplicationContext()) && NewsStatsListDataGenerator.getAllData().get(position).getSentLocally() && NewsStatsListDataGenerator.getAllData().get(position).getSendStatus() == Config.UNSENT) {
                sendComment(newsId, position, NewsStatsListDataGenerator.getAllData().get(position).getReactionOrComment(), LocaleHelper.getLanguage(getApplicationContext()));
                NewsStatsListDataGenerator.getAllData().remove(position);
                mConversationRecyclerView.getAdapter().notifyItemRemoved(position);
                mConversationRecyclerView.getAdapter().notifyItemRangeChanged(position, NewsStatsListDataGenerator.getAllData().size());
            }
        }
    }


    public void resetUiForUnsentMessage(final int listPosition){
        final NewsStatsModel newsStatsModel = new NewsStatsModel();
        newsStatsModel.setStatsType(4);
        newsStatsModel.setNewsId(newsId);
        newsStatsModel.setReactionOrComment(NewsStatsListDataGenerator.getAllData().get(listPosition).getReactionOrComment());
        newsStatsModel.setDate(Config.getCurrentDateTime3("MMM d"));
        newsStatsModel.setSenderPottName(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
        newsStatsModel.setSendStatus(Config.UNSENT);
        NewsStatsListDataGenerator.getAllData().remove(listPosition);
        NewsStatsListDataGenerator.addOneDataToDesiredPosition(listPosition, newsStatsModel);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mConversationRecyclerView.getAdapter().notifyItemChanged(listPosition);
                mConversationRecyclerView.getLayoutManager().scrollToPosition(listPosition);
            }
        });
    }


}
