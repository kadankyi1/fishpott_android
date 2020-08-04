package com.fishpott.fishpott5.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Fragments.NewsOptionsFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.FullNewsListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.Vertical_NewsType_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Views.CircleImageView;
import com.fishpott.fishpott5.Views.ExoPlayerRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FullNewsActivity extends AppCompatActivity implements View.OnClickListener{

    private String newsId = "", statsType = "";
    private ExoPlayerRecyclerView mProfileNewsRecyclerView;
    private ImageView mBackImageView, mReloadFreshImageView;
    private ProgressBar mLinkupsLoadingProgressBar;
    private RecyclerView.OnScrollListener mainNewsScrollListener;
    public static LinearLayoutManager mMainNewsLayoutManager;
    private Boolean REQUEST_MAKER_IS_STILL_ACTIVE_AND_IN_VIEW = false, REQUEST_HAS_STARTED = false, INFO_FETCHED = false;
    private Thread suggestedLinkupsFetchThread = null, suggestedLinkupsFetchThread2 = null, imageLoaderThread = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_news);

        // BINDING VIEWS
        mProfileNewsRecyclerView = (ExoPlayerRecyclerView) findViewById(R.id.activity_suggestedlinkupsactivity_recyclerview);
        mBackImageView = (ImageView) findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mReloadFreshImageView = (ImageView) findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = (ProgressBar) findViewById(R.id.activity_suggestedlinkupsactivity_loader);

        //SETTING UP RECYCLERVIEW
        mProfileNewsRecyclerView.setVisibility(View.INVISIBLE);


        mProfileNewsRecyclerView.setItemViewCacheSize(20);
        mProfileNewsRecyclerView.setDrawingCacheEnabled(true);
        mProfileNewsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        //SETTING UP RECYCLERVIEW LAYOUT MANAGER
        mMainNewsLayoutManager = new LinearLayoutManager(FullNewsActivity.this);

        //SETTING UP SCROLL LISTENER FOR MAIN NEWS RECYCLERVIEW
        mainNewsScrollListener = new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e("ProfilePott", "HERE-NEW-A : 1");
                if (!REQUEST_HAS_STARTED && mMainNewsLayoutManager.getItemCount() <= (mMainNewsLayoutManager.findLastVisibleItemPosition() + 5) && FullNewsListDataGenerator.getAllData().size() > 3) {
                    // End has been reached
                    Log.e("ProfilePott", "HERE-NEW-A : 2");
                    getProfileNews(getApplicationContext(), newsId, false, statsType, "0", LocaleHelper.getLanguage(FullNewsActivity.this), (int) FullNewsListDataGenerator.getAllData().get(FullNewsListDataGenerator.getAllData().size()-1).getNewsSku()+1);
                    //Log.e("ProfilePott", "LAST SKU : " + String.valueOf(FullNewsListDataGenerator.getAllData().get(FullNewsListDataGenerator.getAllData().size()-1).getNewsSku()));

                }

                if (!recyclerView.canScrollVertically(-1)) {
                    //onScrolledToTop();

                } else if (!recyclerView.canScrollVertically(1)) {
                    //onScrolledToBottom();
                    Log.e("ProfilePott", "HERE-NEW-A : 3");

                } else if (dy < 0) {
                    //onScrolledUp();
                } else if (dy > 0) {
                    //onScrolledDown();
                }
            }
        };


        mProfileNewsRecyclerView.setLayoutManager(mMainNewsLayoutManager);
        mProfileNewsRecyclerView.addOnScrollListener(mainNewsScrollListener);
        mProfileNewsRecyclerView.setVideoInfoList(FullNewsListDataGenerator.getAllData());
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter();
        mProfileNewsRecyclerView.setAdapter(recyclerViewAdapter);


        // GETTING THE INFORMATION SENT FROM THE PRIOR ACTIVITY/FRAGMENT
        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle != null) {
            newsId =(String) intentBundle.get(Config.FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID);
            statsType =(String) intentBundle.get(Config.FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE);
            if(newsId.trim().equalsIgnoreCase("")){
                finish();
            }
            if(statsType.trim().equalsIgnoreCase("")){
                statsType = Config.NOTICATION_RELATING_COMMENT_STRING;
            }
            initiatePottFetchRequest(true, statsType, 0);
        } else {
            tuningViewsOnError(0, "", INFO_FETCHED);
        }

        mReloadFreshImageView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
    }


    public void hideOrShowViews(int mRecyclerViewStatus, int mReloadStatus, int mProgressStatus){
        mProfileNewsRecyclerView.setVisibility(mRecyclerViewStatus);
        mReloadFreshImageView.setVisibility(mReloadStatus);
        mLinkupsLoadingProgressBar.setVisibility(mProgressStatus);
    }

    public void getProfileNews(final Context context, String newsID, Boolean START_FRESH, String fetchType, final String showNews, String language, int latestSku){
        REQUEST_HAS_STARTED = true;
        if(START_FRESH){
            hideOrShowViews(View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
            FullNewsListDataGenerator.getAllData().clear();
            latestSku = 0;
        } else {
            if(latestSku != 0){
                if(FullNewsListDataGenerator.getAllData().size() > 1){
                    latestSku = (int) FullNewsListDataGenerator.getAllData().get(FullNewsListDataGenerator.getAllData().size()-2).getNewsSku();
                } else {
                    //hideOrShowViews(View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                    FullNewsListDataGenerator.getAllData().clear();
                    latestSku = 0;
                }
            } else {
                //hideOrShowViews(View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                FullNewsListDataGenerator.getAllData().clear();
            }

        }

        final int finalLatestSku = latestSku;

        Log.e("FulllNews", "newsID: " + newsID);
        Log.e("FulllNews", "stats_type: " + fetchType);
        Log.e("FulllNews", "show_news: " + showNews);
        Log.e("FulllNews", "show_news: " + String.valueOf(latestSku));

        AndroidNetworking.post(Config.LINK_GET_SINGLE_FULLNEWS_WITH_STATS)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("news_id", newsID)
                .addBodyParameter("stats_type", fetchType)
                .addBodyParameter("shows_news", showNews)
                .addBodyParameter("lastest_sku", String.valueOf(latestSku))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("config_fetch_single_news")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("FulllNews", "response : " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("data_returned");

                    JSONObject o = array.getJSONObject(0);
                    int myStatus = o.getInt("1");
                    String statusMsg = o.getString("2");

                    // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                    if(myStatus == 2){
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }

                    // GENERAL ERROR
                    if(myStatus == 3){
                        Config.showToastType1(FullNewsActivity.this, statusMsg);
                        return;
                    }

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 4){
                        Config.showToastType1(FullNewsActivity.this, statusMsg);
                        Config.signOutUser(context, false, null, null, 0, 2);
                    }

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                    Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                    if (myStatus == 1) {

                        JSONArray newsArray = jsonObject.getJSONArray("news_returned");
                        // LIST RESULTS SETTING COMES HERE
                        if(newsArray.length() <= 0 && finalLatestSku == 0){
                            Config.showToastType1(FullNewsActivity.this, getResources().getString(R.string.nothing_to_show));
                        }

                        if(finalLatestSku != 0){
                            FullNewsListDataGenerator.getAllData().remove(FullNewsListDataGenerator.getAllData().size()-1);
                            mProfileNewsRecyclerView.getAdapter().notifyItemRemoved(FullNewsListDataGenerator.getAllData().size()-1);
                            mProfileNewsRecyclerView.getAdapter().notifyItemRangeChanged(FullNewsListDataGenerator.getAllData().size()-1, FullNewsListDataGenerator.getAllData().size());
                        }


                        for (int i = 0; i < newsArray.length(); i++){
                            final JSONObject k = newsArray.getJSONObject(i);
                            Vertical_NewsType_Model verticalNewsType_model = new Vertical_NewsType_Model();
                            //verticalNewsType_model.setRowId(cursor.getLong(verticalNewsTypeDatabaseAdapter.COL_ROWID));
                            verticalNewsType_model.setNewsSku(k.getInt("0a"));
                            verticalNewsType_model.setNewsType(k.getInt("1"));
                            verticalNewsType_model.setNewsId(k.getString("2"));
                            verticalNewsType_model.setNewsText(k.getString("3"));
                            verticalNewsType_model.setNewsTime(k.getString("4"));
                            verticalNewsType_model.setNewsLikes(k.getString("5"));
                            verticalNewsType_model.setNewsDislikes(k.getString("6"));
                            verticalNewsType_model.setNewsComments(k.getString("7"));
                            verticalNewsType_model.setNewsViews(k.getString("8"));
                            verticalNewsType_model.setNewsReposts(k.getString("9"));
                            verticalNewsType_model.setNewsTransactions(k.getString("10"));
                            verticalNewsType_model.setNewsMakerId(k.getString("11"));
                            verticalNewsType_model.setNewsMakerPottName(k.getString("12"));
                            verticalNewsType_model.setNewsMakerFullName(k.getString("13"));
                            verticalNewsType_model.setNewsMakerPottPic(k.getString("14"));
                            verticalNewsType_model.setNewViewerReactionStatus(k.getInt("15"));
                            verticalNewsType_model.setNewsAddedItemId(k.getString("16"));
                            verticalNewsType_model.setNewsItemParentID(k.getString("49"));
                            verticalNewsType_model.setNewsRealItemName(k.getString("50"));
                            verticalNewsType_model.setNewsAddedItemPrice(k.getString("17"));
                            verticalNewsType_model.setNewsAddedItemIcon(k.getString("18"));
                            verticalNewsType_model.setNewsAddedItemQuantity(k.getString("19"));
                            verticalNewsType_model.setNewsMakerAccountType(k.getInt("20"));
                            verticalNewsType_model.setNewsMakerAccountVerifiedStatus(k.getInt("21"));
                            verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces(k.getString("22"));
                            verticalNewsType_model.setNewsImagesCount(k.getString("23"));
                            verticalNewsType_model.setNewsTextReadMoreToggle(k.getInt("24"));
                            verticalNewsType_model.setNewsAddedItemType(k.getInt("25"));
                            verticalNewsType_model.setNewsAddedItemStatus(k.getInt("26"));
                            verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces(k.getString("27"));
                            verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces(k.getString("28"));
                            verticalNewsType_model.setNewsVideosCount(k.getString("29"));
                            verticalNewsType_model.setNewsUrl(k.getString("30"));
                            verticalNewsType_model.setNewsUrlTitle(k.getString("31"));
                            verticalNewsType_model.setNewsItemName(k.getString("32"));
                            verticalNewsType_model.setNewsItemLocation(k.getString("33"));
                            verticalNewsType_model.setNewsItemVerifiedStatus(k.getInt("34"));
                            verticalNewsType_model.setAdvertItemIcon(k.getString("35"));
                            verticalNewsType_model.setAdvertTextTitle(k.getString("36"));
                            verticalNewsType_model.setAdvertTextTitle2(k.getString("37"));
                            verticalNewsType_model.setAdvertButtonText(k.getString("38"));
                            verticalNewsType_model.setAdvertLink(k.getString("39"));
                            verticalNewsType_model.setReposterPottName(k.getString("40"));
                            verticalNewsType_model.setRepostedText(k.getString("41"));
                            verticalNewsType_model.setRepostedIcon(k.getString("42"));
                            verticalNewsType_model.setNewRepostedViewerReactionStatus(k.getInt("43"));
                            verticalNewsType_model.setRepostedItemPrice(k.getString("44"));
                            verticalNewsType_model.setNewsBackgroundColor(k.getInt("45"));
                            verticalNewsType_model.setNewsViewsRepostOrPurchasesShowStatus(k.getInt("46"));
                            verticalNewsType_model.setRepostedItemId(k.getString("47"));
                            verticalNewsType_model.setRepostedNewsId(k.getString("48"));
                            verticalNewsType_model.setRepostedItemParentID(k.getString("49"));
                            verticalNewsType_model.setRepostedItemQuantity(k.getString("54"));
                            verticalNewsType_model.setRepostedItemName(k.getString("53"));

                            imageLoaderThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Config.loadUrlImage(context, false, k.getString("14"), null, 0, 60, 60);
                                        Config.loadUrlImage(context, false, k.getString("18"), null, 0, 70, 70);
                                        Config.loadUrlImage(context, false, k.getString("28"), null, 0, 160, 160);
                                        Config.loadUrlImage(context, false, k.getString("35"), null, 0, 100, 100);
                                        Config.loadUrlImage(context, false, k.getString("42"), null, 0, 100, 100);

                                        String[] globalSplitedNewsImagesLinksArray = k.getString("22").split("\\s+");

                                        if(globalSplitedNewsImagesLinksArray.length > 0){
                                            Config.loadUrlImage(context, false, globalSplitedNewsImagesLinksArray[0], null, 0, 160, 160);
                                        }
                                        if(globalSplitedNewsImagesLinksArray.length > 1){
                                            Config.loadUrlImage(context, false, globalSplitedNewsImagesLinksArray[1], null, 0, 160, 160);
                                        }
                                        if(globalSplitedNewsImagesLinksArray.length > 2){
                                            Config.loadUrlImage(context, false, globalSplitedNewsImagesLinksArray[2], null, 0, 160, 160);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            imageLoaderThread.start();

                            FullNewsListDataGenerator.addOneData(verticalNewsType_model);
                            mProfileNewsRecyclerView.getAdapter().notifyItemInserted(FullNewsListDataGenerator.getAllData().size()-1);
                        }

                        if(finalLatestSku == 0){
                            Log.e("ProfilePott", "HERE 1");
                            if(newsArray.length() > 0) {
                                hideOrShowViews(View.VISIBLE, View.GONE, View.GONE);
                                Log.e("ProfilePott", "HERE 2");
                            } else {
                                hideOrShowViews(View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                                Log.e("ProfilePott", "HERE 3");
                            }
                        } else {
                            Log.e("ProfilePott", "HERE 4");
                            if(newsArray.length() > 0){
                                Vertical_NewsType_Model verticalNewsType_model = new Vertical_NewsType_Model();
                                verticalNewsType_model.setNewsType(Config.NEWS_TYPE_30_NEWSLOADINGRETRY_VERTICAL_KEY);
                                FullNewsListDataGenerator.addOneData(verticalNewsType_model);
                                mProfileNewsRecyclerView.getAdapter().notifyItemInserted(FullNewsListDataGenerator.getAllData().size()-1);
                                //mProfileNewsRecyclerView.getAdapter().notifyItemRangeChanged(FullNewsListDataGenerator.getAllData().size()-1, 1);
                                Log.e("ProfilePott", "HERE 5");
                            }
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(FullNewsActivity.this, getString(R.string.an_error_occurred));
                    hideOrShowViews(View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                }
                REQUEST_HAS_STARTED = false;
            }

            @Override
            public void onError(ANError anError) {
                REQUEST_HAS_STARTED = false;
                hideOrShowViews(View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                Config.showToastType1(FullNewsActivity.this, getString(R.string.login_activity_check_your_internet_connection_and_try_again));
            }
        });
    }



    private void tuningViewsOnError(final int showToast, final String statusMsg, Boolean infoHasBeenFetched){
        if (REQUEST_MAKER_IS_STILL_ACTIVE_AND_IN_VIEW) {
            REQUEST_HAS_STARTED =  false;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                    mProfileNewsRecyclerView.setVisibility(View.INVISIBLE);
                    mReloadFreshImageView.setVisibility(View.VISIBLE);
                }
            }); //END OF HANDLER-TO-MAIN-THREAD
        }
        Config.showToastType1(FullNewsActivity.this, getResources().getString(R.string.nothing_to_show));
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == mBackImageView.getId()){
            onBackPressed();
        } else if(view.getId() == mReloadFreshImageView.getId()){
            initiatePottFetchRequest(true, statsType, 0);
        }
    }


    private void initiatePottFetchRequest(Boolean startFresh, String fetchType, int lastSku){
        if(!newsId.trim().equalsIgnoreCase("")){
            if(!REQUEST_HAS_STARTED){
                if(Connectivity.isConnected(FullNewsActivity.this)) {
                    getProfileNews(getApplicationContext(), newsId, startFresh, fetchType, "1", LocaleHelper.getLanguage(FullNewsActivity.this), lastSku);
                } else {
                    tuningViewsOnError(0, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again), INFO_FETCHED);
                }
            }
        } else {
            tuningViewsOnError(0, getResources().getString(R.string.nothing_to_show), INFO_FETCHED);
        }
    }



    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    public class RecyclerViewAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemViewType(int position) {
            return FullNewsListDataGenerator.getAllData().get(position).getNewsType();
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v;
            if (viewType == Config.NOTICATION_RELATING_LIKE) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_like, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == Config.NOTICATION_RELATING_DISLIKE) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_dislike, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == Config.NOTICATION_RELATING_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_view, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == Config.NOTICATION_RELATING_PURCHASE) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_purchases, parent, false);
                vh = new LikeDislikeViewPurchasesViewHolder(v);
            } else if (viewType == Config.NOTICATION_RELATING_COMMENT) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stats_comment, parent, false);
                vh = new CommentViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_1_justnewswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_1_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_fn_news_type_2_justnewswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_2_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_3_TO_4_JUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_3_to_4_newswithimage_newsfeed_fragment, parent, false);
                vh = new NewsType_3_to_4_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_5_TO_6_JUSTNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_5_to_6_newswithvideo_newsfeed_fragment, parent, false);
                vh = new NewsType_5_to_6_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_7_AND_9_JUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_7_and_9_newswithurlwithimage_newsfeed_fragment, parent, false);
                vh = new NewsType_7_and_9_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_8_JUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_8_newswithurlwithvideo_newsfeed_fragment, parent, false);
                vh = new NewsType_8_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_14_SHARESFORSALENEWS_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_14_sharesforsale_newsfeed_fragment, parent, false);
                vh = new NewsType_14_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_17_SHARES4SALEWITHVIDEO_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_17_sharesforsalewithvideo_newsfeed_fragment, parent, false);
                vh = new NewsType_17_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_1_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_1_reposted_justnewswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_1_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_2_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_2_reposted_justnewswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_2_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_3_TO_4_REPOSTEDJUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_3_to_4_reposted_newswithimage_newsfeed_fragment, parent, false);
                vh = new NewsType_3_to_4_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_5_TO_6_REPOSTEDNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_5_to_6_reposted_newswithvideo_newsfeed_fragment, parent, false);
                vh = new NewsType_5_to_6_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_7_AND_9_REPOSTEDJUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_7_and_9_reposted_newswithurlwithimage_newsfeed_fragment, parent, false);
                vh = new NewsType_7_and_9_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_8_REPOSTEDJUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_8_reposted_newswithurlwithvideo_newsfeed_fragment, parent, false);
                vh = new NewsType_8_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_14_REPOSTEDSHARESFORSALENEWS_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_14_reposted_sharesforsale_newsfeed_fragment, parent, false);
                vh = new NewsType_14_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_17_REPOSTEDSHARES4SALEWITHVIDEO_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_17_reposted_sharesforsalewithvideo_newsfeed_fragment, parent, false);
                vh = new NewsType_17_Reposted_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_25_POSTINGFAILEDRETRYPOST_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_25_postingfailedretrynewspost_newsfeed_fragment, parent, false);
                vh = new News_Posting_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_30_NEWSLOADINGRETRY_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_30_loading_retry_bottom_newsfeed_fragment, parent, false);
                vh = new News_Loading_More_At_Bottom_ViewHolder(v);
            }  else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_30_loading_retry_bottom_newsfeed_fragment, parent, false);
                vh = new News_Loading_More_At_Bottom_ViewHolder(v);
            }
            return vh;
        }

        // NEW TYPE 1 VIEW HOLDER
        public class NewsType_1_ViewHolder extends RecyclerView.ViewHolder  {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mNewsBackgroundConstraintLayout, mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };


            public NewsType_1_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_text_background_constraintlayout);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
            }

        }

        // NEW TYPE 2 VIEW HOLDER
        public class NewsType_2_ViewHolder extends RecyclerView.ViewHolder {

            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mNewsBackgroundConstraintLayout, mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };


            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };


            public NewsType_2_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_text_background_constraintlayout);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);

            }
        }

        // NEW TYPE 3-4 VIEW HOLDER
        public class NewsType_3_to_4_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;

            private ViewPager mNewsImagesSliderViewPager;
            private LinearLayout mNewsImagesCountDotLinearlayout;
            private TextView[] mNewsImagesCountDots;


            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };


            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };

            private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
                @Override
                public void onPageSelected(int position) {
                    addDotsIndicator(FullNewsListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
                }
                @Override
                public void onPageScrollStateChanged(int state) {}
            };

            public NewsType_3_to_4_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsImagesSliderViewPager = (ViewPager) itemView.findViewById(R.id.news_images_view_pager);
                mNewsImagesCountDotLinearlayout = (LinearLayout) itemView.findViewById(R.id.news_images_count_dots_linear_layout);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);


                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);

            }
        }

        // NEW TYPE 5-6 VIEW HOLDER
        public class NewsType_5_to_6_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mNewsVideoCoverArtImageView,
                    mPlayVideoIconImageView,
                    mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar, mLoadingVideoProgressBar;
            private View parent;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };

            public NewsType_5_to_6_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsVideoCoverArtImageView = (ImageView) itemView.findViewById(R.id.cover);
                mPlayVideoIconImageView = (ImageView) itemView.findViewById(R.id.play_icon);
                mLoadingVideoProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                parent = v;

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mPlayVideoIconImageView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);

            }
        }

        // NEW TYPE 7 VIEW HOLDER
        public class NewsType_7_and_9_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsWebsiteAddress, mNewsWebsiteTitle, mNewsTimeTextView, mNewsLikesTextView,
                    mNewsDislikesTextView, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView,mNewsImageImageView, mReloadNewsImageImageView,
                    mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar, mLoadingNewsImageProgressBar;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };

            public NewsType_7_and_9_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsImageImageView = (ImageView) itemView.findViewById(R.id.newswithurl_image_imageview);
                mReloadNewsImageImageView = (ImageView) itemView.findViewById(R.id.reload_news_image_imageview);
                mLoadingNewsImageProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_news_image_progressbar);
                mNewsWebsiteAddress = (TextView) itemView.findViewById(R.id.news_website_address);
                mNewsWebsiteTitle = (TextView) itemView.findViewById(R.id.news_title);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mNewsImageImageView.setOnClickListener(innerClickListener);
                mReloadNewsImageImageView.setOnClickListener(innerClickListener);
                mLoadingNewsImageProgressBar.setOnClickListener(innerClickListener);
                mNewsWebsiteAddress.setOnClickListener(innerClickListener);
                mNewsWebsiteTitle.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);

            }
        }

        // NEW TYPE 8 VIEW HOLDER
        public class NewsType_8_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsWebsiteAddress, mNewsWebsiteTitle, mNewsTimeTextView, mNewsLikesTextView,
                    mNewsDislikesTextView, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mNewsVideoCoverArtImageView,
                    mPlayVideoIconImageView,
                    mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar, mLoadingVideoProgressBar;
            private View parent;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };


            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };

            public NewsType_8_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsVideoCoverArtImageView = (ImageView) itemView.findViewById(R.id.cover);
                mPlayVideoIconImageView = (ImageView) itemView.findViewById(R.id.play_icon);
                mLoadingVideoProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
                mNewsWebsiteAddress = (TextView) itemView.findViewById(R.id.news_website_address);
                mNewsWebsiteTitle = (TextView) itemView.findViewById(R.id.news_title);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                parent = v;

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mPlayVideoIconImageView.setOnClickListener(innerClickListener);
                mNewsWebsiteAddress.setOnClickListener(innerClickListener);
                mNewsWebsiteTitle.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);

            }
        }

        // NEW TYPE 10 VIEW HOLDER
        public class NewsType_10_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView,
                    mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mItemForSaleNameTextView,
                    mItemForSalePriceTextView, mItemForSaleLocationTextView, mItemForSaleQuantity,
                    mNewsBuyersTextview, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView, mRepostsTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mCommentIconImageView, mRepostIconImageView,
                    mYardSaleIconHolderImageView, mItemForSaleLocationIconImageView, mBuyIconImageView;

            private ConstraintLayout mYardSaleIconHolderConstraintLayout;

            private ViewPager mNewsImagesSliderViewPager;
            private LinearLayout mNewsImagesCountDotLinearlayout;
            private TextView[] mNewsImagesCountDots;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };

            private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    addDotsIndicator(FullNewsListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
                }

                @Override
                public void onPageScrollStateChanged(int state) {}
            };


            public NewsType_10_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mYardSaleIconHolderConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.yardsale_icon_background);
                mYardSaleIconHolderImageView = (ImageView) itemView.findViewById(R.id.yardsale_icon_imageview);
                mNewsImagesSliderViewPager = (ViewPager) itemView.findViewById(R.id.news_images_view_pager);
                mNewsImagesCountDotLinearlayout = (LinearLayout) itemView.findViewById(R.id.news_images_count_dots_linear_layout);
                mItemForSalePriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mItemForSaleNameTextView = (TextView) itemView.findViewById(R.id.item_for_sale_name_textview);
                mNewsBuyersTextview = (TextView) itemView.findViewById(R.id.news_buyers_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mItemForSaleQuantity = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mItemForSaleLocationIconImageView = (ImageView) itemView.findViewById(R.id.item_for_sale_location_icon_imageview);
                mItemForSaleLocationTextView = (TextView) itemView.findViewById(R.id.item_for_sale_location_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyIconImageView = (ImageView) itemView.findViewById(R.id.buy_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mRepostsTextView = (TextView) itemView.findViewById(R.id.news_reposts_textview);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mYardSaleIconHolderImageView.setOnClickListener(innerClickListener);
                mItemForSaleNameTextView.setOnClickListener(innerClickListener);
                mItemForSalePriceTextView.setOnClickListener(innerClickListener);
                mItemForSaleLocationTextView.setOnClickListener(innerClickListener);
                mItemForSaleQuantity.setOnClickListener(innerClickListener);
                mNewsBuyersTextview.setOnClickListener(innerClickListener);
                mItemForSaleLocationIconImageView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostsTextView.setOnClickListener(innerClickListener);
                mBuyIconImageView.setOnClickListener(innerClickListener);

            }
        }

        // NEW TYPE 14 VIEW HOLDER
        public class NewsType_14_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView,
                    mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView,
                    mItemForSalePriceTextView, mItemForSaleInfo1TextView, mItemForSaleInfo2TextView, mItemForSaleQuantity,
                    mNewsBuyersTextview, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView, mRepostsTextView;
            private CircleImageView mPottPicCircleImageView, mSharesLogoCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mCommentIconImageView, mRepostIconImageView,
                    mSharesForSaleIconHolderImageView, mBuyIconImageView, mReloadSharesforsaleLogoImageView;

            private ConstraintLayout mSharesForSaleIconHolderConstraintLayout;

            private ViewPager mNewsImagesSliderViewPager;
            private LinearLayout mNewsImagesCountDotLinearlayout;
            private TextView[] mNewsImagesCountDots;
            private ProgressBar mLoadingSharesforsaleLogoImageView;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };


            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };

            private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    addDotsIndicator(FullNewsListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
                }

                @Override
                public void onPageScrollStateChanged(int state) {}
            };


            public NewsType_14_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mSharesForSaleIconHolderConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.sharesforsale_icon_background);
                mSharesForSaleIconHolderImageView = (ImageView) itemView.findViewById(R.id.sharesforsale_icon);
                mNewsImagesSliderViewPager = (ViewPager) itemView.findViewById(R.id.news_images_view_pager);
                mNewsImagesCountDotLinearlayout = (LinearLayout) itemView.findViewById(R.id.news_images_count_dots_linear_layout);
                mSharesLogoCircleImageView = (CircleImageView) itemView.findViewById(R.id.shares_for_sale_logo_circleimageview);
                mReloadSharesforsaleLogoImageView = (ImageView) itemView.findViewById(R.id.reload_shares_for_sale_logo_imageview);
                mLoadingSharesforsaleLogoImageView = (ProgressBar) itemView.findViewById(R.id.image_loading_shares_for_sale_logo_progressbar);
                mItemForSalePriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mNewsBuyersTextview = (TextView) itemView.findViewById(R.id.news_buyers_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mItemForSaleQuantity = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mItemForSaleInfo1TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info1_textview);
                mItemForSaleInfo2TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info2_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyIconImageView = (ImageView) itemView.findViewById(R.id.buy_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mRepostsTextView = (TextView) itemView.findViewById(R.id.news_reposts_textview);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mSharesForSaleIconHolderImageView.setOnClickListener(innerClickListener);
                mSharesLogoCircleImageView.setOnClickListener(innerClickListener);
                mReloadSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mLoadingSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mItemForSalePriceTextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo1TextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo2TextView.setOnClickListener(innerClickListener);
                mItemForSaleQuantity.setOnClickListener(innerClickListener);
                mNewsBuyersTextview.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostsTextView.setOnClickListener(innerClickListener);
                mBuyIconImageView.setOnClickListener(innerClickListener);
            }
        }

        // NEW TYPE 17 VIEW HOLDER
        public class NewsType_17_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView,
                    mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView,
                    mItemForSalePriceTextView, mItemForSaleInfo1TextView, mItemForSaleInfo2TextView, mItemForSaleQuantity,
                    mNewsBuyersTextview, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView, mRepostsTextView;
            private CircleImageView mPottPicCircleImageView, mSharesLogoCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mCommentIconImageView, mRepostIconImageView,
                    mSharesForSaleIconHolderImageView, mNewsVideoCoverArtImageView,
                    mPlayVideoIconImageView, mBuyIconImageView, mReloadSharesforsaleLogoImageView;
            private ConstraintLayout mSharesForSaleIconHolderConstraintLayout;
            private ProgressBar mLoadingSharesforsaleLogoImageView, mLoadingVideoProgressBar;
            private View parent;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, null, null, getAdapterPosition());
                }
            };


            public NewsType_17_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mSharesForSaleIconHolderConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.sharesforsale_icon_background);
                mSharesForSaleIconHolderImageView = (ImageView) itemView.findViewById(R.id.sharesforsale_icon);
                mNewsVideoCoverArtImageView = (ImageView) itemView.findViewById(R.id.cover);
                mPlayVideoIconImageView = (ImageView) itemView.findViewById(R.id.play_icon);
                mLoadingVideoProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
                mSharesLogoCircleImageView = (CircleImageView) itemView.findViewById(R.id.shares_for_sale_logo_circleimageview);
                mReloadSharesforsaleLogoImageView = (ImageView) itemView.findViewById(R.id.reload_shares_for_sale_logo_imageview);
                mLoadingSharesforsaleLogoImageView = (ProgressBar) itemView.findViewById(R.id.image_loading_shares_for_sale_logo_progressbar);
                mItemForSalePriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mNewsBuyersTextview = (TextView) itemView.findViewById(R.id.news_buyers_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mItemForSaleQuantity = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mItemForSaleInfo1TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info1_textview);
                mItemForSaleInfo2TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info2_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyIconImageView = (ImageView) itemView.findViewById(R.id.buy_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mRepostsTextView = (TextView) itemView.findViewById(R.id.news_reposts_textview);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mSharesForSaleIconHolderImageView.setOnClickListener(innerClickListener);
                mPlayVideoIconImageView.setOnClickListener(innerClickListener);
                mSharesLogoCircleImageView.setOnClickListener(innerClickListener);
                mReloadSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mLoadingSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mItemForSalePriceTextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo1TextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo2TextView.setOnClickListener(innerClickListener);
                mItemForSaleQuantity.setOnClickListener(innerClickListener);
                mNewsBuyersTextview.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostsTextView.setOnClickListener(innerClickListener);
                mBuyIconImageView.setOnClickListener(innerClickListener);
                parent = v;
            }
        }

        // NEW TYPE 1 SPONSORED VIEW HOLDER
        public class NewsType_1_Sponsored_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mNewsBackgroundConstraintLayout, mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;

            private ImageView mAdvertIconImageView;
            private TextView mAdvertInfo1TextView, mAdvertInfo2TextView;
            private Button mAdvertActionButton;


            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };


            public NewsType_1_Sponsored_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_text_background_constraintlayout);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);

                mAdvertIconImageView = (ImageView) itemView.findViewById(R.id.advert_icon_imageview);
                mAdvertInfo1TextView = (TextView) itemView.findViewById(R.id.advert_info1_textview);
                mAdvertInfo2TextView = (TextView) itemView.findViewById(R.id.advert_info2_textview);
                mAdvertActionButton = (Button) itemView.findViewById(R.id.advert_action_button);


                // ALL ON-CLICK LISTENERS

                mAdvertIconImageView.setOnClickListener(innerClickListener);
                mAdvertInfo1TextView.setOnClickListener(innerClickListener);
                mAdvertInfo2TextView.setOnClickListener(innerClickListener);
                mAdvertActionButton.setOnClickListener(innerClickListener);

                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                //mLikeIconImageView.setOnClickListener(innerClickListener);
                //mDislikeIconImageView.setOnClickListener(innerClickListener);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
            }

        }

        // NEW TYPE 2 VIEW HOLDER
        public class NewsType_2_Sponsored_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mNewsBackgroundConstraintLayout, mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;

            private ImageView mAdvertIconImageView;
            private TextView mAdvertInfo1TextView, mAdvertInfo2TextView;
            private Button mAdvertActionButton;


            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };


            public NewsType_2_Sponsored_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_text_background_constraintlayout);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);

                mAdvertIconImageView = (ImageView) itemView.findViewById(R.id.advert_icon_imageview);
                mAdvertInfo1TextView = (TextView) itemView.findViewById(R.id.advert_info1_textview);
                mAdvertInfo2TextView = (TextView) itemView.findViewById(R.id.advert_info2_textview);
                mAdvertActionButton = (Button) itemView.findViewById(R.id.advert_action_button);


                // ALL ON-CLICK LISTENERS

                mAdvertIconImageView.setOnClickListener(innerClickListener);
                mAdvertInfo1TextView.setOnClickListener(innerClickListener);
                mAdvertInfo2TextView.setOnClickListener(innerClickListener);
                mAdvertActionButton.setOnClickListener(innerClickListener);

                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                //mLikeIconImageView.setOnClickListener(innerClickListener);
                //mDislikeIconImageView.setOnClickListener(innerClickListener);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
            }
        }

        public class NewsType_3_to_4_Sponsored_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;

            private ViewPager mNewsImagesSliderViewPager;
            private LinearLayout mNewsImagesCountDotLinearlayout;
            private TextView[] mNewsImagesCountDots;

            private ImageView mAdvertIconImageView;
            private TextView mAdvertInfo1TextView, mAdvertInfo2TextView;
            private Button mAdvertActionButton;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    addDotsIndicator(FullNewsListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
                }

                @Override
                public void onPageScrollStateChanged(int state) {}
            };

            public NewsType_3_to_4_Sponsored_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsImagesSliderViewPager = (ViewPager) itemView.findViewById(R.id.news_images_view_pager);
                mNewsImagesCountDotLinearlayout = (LinearLayout) itemView.findViewById(R.id.news_images_count_dots_linear_layout);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);

                mAdvertIconImageView = (ImageView) itemView.findViewById(R.id.advert_icon_imageview);
                mAdvertInfo1TextView = (TextView) itemView.findViewById(R.id.advert_info1_textview);
                mAdvertInfo2TextView = (TextView) itemView.findViewById(R.id.advert_info2_textview);
                mAdvertActionButton = (Button) itemView.findViewById(R.id.advert_action_button);


                // ALL ON-CLICK LISTENERS

                mAdvertIconImageView.setOnClickListener(innerClickListener);
                mAdvertInfo1TextView.setOnClickListener(innerClickListener);
                mAdvertInfo2TextView.setOnClickListener(innerClickListener);
                mAdvertActionButton.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);


                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                //mLikeIconImageView.setOnClickListener(innerClickListener);
                //mDislikeIconImageView.setOnClickListener(innerClickListener);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);

            }
        }


        // NEW TYPE 5-6 VIEW HOLDER
        public class NewsType_5_to_6_Sponsored_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mNewsVideoCoverArtImageView,
                    mPlayVideoIconImageView,
                    mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar, mLoadingVideoProgressBar;
            private View parent;

            private ImageView mAdvertIconImageView;
            private TextView mAdvertInfo1TextView, mAdvertInfo2TextView;
            private Button mAdvertActionButton;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public NewsType_5_to_6_Sponsored_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsVideoCoverArtImageView = (ImageView) itemView.findViewById(R.id.cover);
                mPlayVideoIconImageView = (ImageView) itemView.findViewById(R.id.play_icon);
                mLoadingVideoProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                parent = v;

                mAdvertIconImageView = (ImageView) itemView.findViewById(R.id.advert_icon_imageview);
                mAdvertInfo1TextView = (TextView) itemView.findViewById(R.id.advert_info1_textview);
                mAdvertInfo2TextView = (TextView) itemView.findViewById(R.id.advert_info2_textview);
                mAdvertActionButton = (Button) itemView.findViewById(R.id.advert_action_button);


                // ALL ON-CLICK LISTENERS

                mAdvertIconImageView.setOnClickListener(innerClickListener);
                mAdvertInfo1TextView.setOnClickListener(innerClickListener);
                mAdvertInfo2TextView.setOnClickListener(innerClickListener);
                mAdvertActionButton.setOnClickListener(innerClickListener);


                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mPlayVideoIconImageView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                //mLikeIconImageView.setOnClickListener(innerClickListener);
                //mDislikeIconImageView.setOnClickListener(innerClickListener);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);

            }
        }

        public class NewsType_1_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mNewsBackgroundConstraintLayout, mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;
            private CheckBox mRepostedLikeIconImageView;
            private CheckBox mRepostedDislikeIconImageView;


            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };


            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };

            public NewsType_1_Reposted_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_text_background_constraintlayout);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);

                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);


                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);
            }

        }

        public class NewsType_2_Reposted_ViewHolder extends RecyclerView.ViewHolder {

            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mNewsBackgroundConstraintLayout, mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;
            private CheckBox mRepostedLikeIconImageView;
            private CheckBox mRepostedDislikeIconImageView;

            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };


            public NewsType_2_Reposted_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_text_background_constraintlayout);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);

                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);

            }
        }

        // NEW TYPE 3-4 VIEW HOLDER
        public class NewsType_3_to_4_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mCommentIconImageView,
                    mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar;

            private ViewPager mNewsImagesSliderViewPager;
            private LinearLayout mNewsImagesCountDotLinearlayout;
            private TextView[] mNewsImagesCountDots;
            private CheckBox mRepostedLikeIconImageView, mRepostedDislikeIconImageView;


            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;


            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };

            private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
                @Override
                public void onPageSelected(int position) {
                    addDotsIndicator(FullNewsListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
                }
                @Override
                public void onPageScrollStateChanged(int state) {}
            };

            public NewsType_3_to_4_Reposted_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsImagesSliderViewPager = (ViewPager) itemView.findViewById(R.id.news_images_view_pager);
                mNewsImagesCountDotLinearlayout = (LinearLayout) itemView.findViewById(R.id.news_images_count_dots_linear_layout);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);

                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);


                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);

            }
        }


        // NEW TYPE 5-6 VIEW HOLDER
        public class NewsType_5_to_6_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mNewsVideoCoverArtImageView,
                    mPlayVideoIconImageView,
                    mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar, mLoadingVideoProgressBar;
            private View parent;

            private CheckBox mRepostedLikeIconImageView, mRepostedDislikeIconImageView;

            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;


            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };

            public NewsType_5_to_6_Reposted_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsVideoCoverArtImageView = (ImageView) itemView.findViewById(R.id.cover);
                mPlayVideoIconImageView = (ImageView) itemView.findViewById(R.id.play_icon);
                mLoadingVideoProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                parent = v;

                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mPlayVideoIconImageView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);

            }
        }


        // NEW TYPE 7 VIEW HOLDER
        public class NewsType_7_and_9_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsWebsiteAddress, mNewsWebsiteTitle, mNewsTimeTextView, mNewsLikesTextView,
                    mNewsDislikesTextView, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView,mNewsImageImageView, mReloadNewsImageImageView,
                    mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar, mLoadingNewsImageProgressBar;

            private CheckBox mRepostedLikeIconImageView, mRepostedDislikeIconImageView;

            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };

            public NewsType_7_and_9_Reposted_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsImageImageView = (ImageView) itemView.findViewById(R.id.newswithurl_image_imageview);
                mReloadNewsImageImageView = (ImageView) itemView.findViewById(R.id.reload_news_image_imageview);
                mLoadingNewsImageProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_news_image_progressbar);
                mNewsWebsiteAddress = (TextView) itemView.findViewById(R.id.news_website_address);
                mNewsWebsiteTitle = (TextView) itemView.findViewById(R.id.news_title);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);

                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);


                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mNewsImageImageView.setOnClickListener(innerClickListener);
                mReloadNewsImageImageView.setOnClickListener(innerClickListener);
                mLoadingNewsImageProgressBar.setOnClickListener(innerClickListener);
                mNewsWebsiteAddress.setOnClickListener(innerClickListener);
                mNewsWebsiteTitle.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);

            }
        }

        // NEW TYPE 8 VIEW HOLDER
        public class NewsType_8_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsWebsiteAddress, mNewsWebsiteTitle, mNewsTimeTextView, mNewsLikesTextView,
                    mNewsDislikesTextView, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mAddedItemImageView, mNewsVideoCoverArtImageView,
                    mPlayVideoIconImageView,
                    mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private ConstraintLayout mAddedItemPriceBackgroundConstraintLayout,
                    mAddedItemIconBackgroundConstraintLayout, mAddedItemQuantityBackgroundConstraintLayout;
            private ProgressBar mLoadingAddedItemProgressBar, mLoadingVideoProgressBar;
            private View parent;

            private CheckBox mRepostedLikeIconImageView, mRepostedDislikeIconImageView;

            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;


            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };


            public NewsType_8_Reposted_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mAddedItemQuantityBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_quantity_background_constraintlayout);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.news_added_item_quantity_textview);
                mAddedItemIconBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_icon_background_constraintlayout);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.news_added_item_icon_imageview);
                mReloadAddedItemImageView = (ImageView) itemView.findViewById(R.id.reload_addeditem_imageview);
                mLoadingAddedItemProgressBar = (ProgressBar) itemView.findViewById(R.id.image_loading_addeditem_progressbar);
                mAddedItemPriceBackgroundConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.news_added_item_price_background_constraintlayout);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.news_added_item_price_textview);
                mNewsVideoCoverArtImageView = (ImageView) itemView.findViewById(R.id.cover);
                mPlayVideoIconImageView = (ImageView) itemView.findViewById(R.id.play_icon);
                mLoadingVideoProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
                mNewsWebsiteAddress = (TextView) itemView.findViewById(R.id.news_website_address);
                mNewsWebsiteTitle = (TextView) itemView.findViewById(R.id.news_title);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                parent = v;
                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mAddedItemQuantityBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mAddedItemIconBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mReloadAddedItemImageView.setOnClickListener(innerClickListener);
                mLoadingAddedItemProgressBar.setOnClickListener(innerClickListener);
                mAddedItemPriceBackgroundConstraintLayout.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mPlayVideoIconImageView.setOnClickListener(innerClickListener);
                mNewsWebsiteAddress.setOnClickListener(innerClickListener);
                mNewsWebsiteTitle.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);

            }

        }

        // NEW TYPE 10 VIEW HOLDER
        public class NewsType_10_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView,
                    mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView, mItemForSaleNameTextView,
                    mItemForSalePriceTextView, mItemForSaleLocationTextView, mItemForSaleQuantity,
                    mNewsBuyersTextview, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView, mRepostsTextView;
            private CircleImageView mPottPicCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mCommentIconImageView, mRepostIconImageView,
                    mYardSaleIconHolderImageView, mItemForSaleLocationIconImageView, mBuyIconImageView;

            private ConstraintLayout mYardSaleIconHolderConstraintLayout;

            private CheckBox mRepostedLikeIconImageView, mRepostedDislikeIconImageView;

            private ViewPager mNewsImagesSliderViewPager;
            private LinearLayout mNewsImagesCountDotLinearlayout;
            private TextView[] mNewsImagesCountDots;

            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;


            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };

            private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    addDotsIndicator(FullNewsListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
                }

                @Override
                public void onPageScrollStateChanged(int state) {}
            };


            public NewsType_10_Reposted_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mYardSaleIconHolderConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.yardsale_icon_background);
                mYardSaleIconHolderImageView = (ImageView) itemView.findViewById(R.id.yardsale_icon_imageview);
                mNewsImagesSliderViewPager = (ViewPager) itemView.findViewById(R.id.news_images_view_pager);
                mNewsImagesCountDotLinearlayout = (LinearLayout) itemView.findViewById(R.id.news_images_count_dots_linear_layout);
                mItemForSalePriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mItemForSaleNameTextView = (TextView) itemView.findViewById(R.id.item_for_sale_name_textview);
                mNewsBuyersTextview = (TextView) itemView.findViewById(R.id.news_buyers_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mItemForSaleQuantity = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mItemForSaleLocationIconImageView = (ImageView) itemView.findViewById(R.id.item_for_sale_location_icon_imageview);
                mItemForSaleLocationTextView = (TextView) itemView.findViewById(R.id.item_for_sale_location_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyIconImageView = (ImageView) itemView.findViewById(R.id.buy_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mRepostsTextView = (TextView) itemView.findViewById(R.id.news_reposts_textview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);
                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mYardSaleIconHolderImageView.setOnClickListener(innerClickListener);
                mItemForSaleNameTextView.setOnClickListener(innerClickListener);
                mItemForSalePriceTextView.setOnClickListener(innerClickListener);
                mItemForSaleLocationTextView.setOnClickListener(innerClickListener);
                mItemForSaleQuantity.setOnClickListener(innerClickListener);
                mNewsBuyersTextview.setOnClickListener(innerClickListener);
                mItemForSaleLocationIconImageView.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostsTextView.setOnClickListener(innerClickListener);
                mBuyIconImageView.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);

            }
        }

        // NEW TYPE 14 VIEW HOLDER
        public class NewsType_14_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView,
                    mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView,
                    mItemForSalePriceTextView, mItemForSaleInfo1TextView, mItemForSaleInfo2TextView, mItemForSaleQuantity,
                    mNewsBuyersTextview, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView, mRepostsTextView;
            private CircleImageView mPottPicCircleImageView, mSharesLogoCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mCommentIconImageView, mRepostIconImageView,
                    mSharesForSaleIconHolderImageView, mBuyIconImageView, mReloadSharesforsaleLogoImageView;

            private ConstraintLayout mSharesForSaleIconHolderConstraintLayout;

            private ViewPager mNewsImagesSliderViewPager;
            private LinearLayout mNewsImagesCountDotLinearlayout;
            private TextView[] mNewsImagesCountDots;
            private ProgressBar mLoadingSharesforsaleLogoImageView;

            private CheckBox mRepostedLikeIconImageView, mRepostedDislikeIconImageView;

            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };

            private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
                @Override
                public void onPageSelected(int position) {
                    addDotsIndicator(FullNewsListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
                }
                @Override
                public void onPageScrollStateChanged(int state) {}
            };


            public NewsType_14_Reposted_ViewHolder(View v) {
                super(v);

                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mSharesForSaleIconHolderConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.sharesforsale_icon_background);
                mSharesForSaleIconHolderImageView = (ImageView) itemView.findViewById(R.id.sharesforsale_icon);
                mNewsImagesSliderViewPager = (ViewPager) itemView.findViewById(R.id.news_images_view_pager);
                mNewsImagesCountDotLinearlayout = (LinearLayout) itemView.findViewById(R.id.news_images_count_dots_linear_layout);
                mSharesLogoCircleImageView = (CircleImageView) itemView.findViewById(R.id.shares_for_sale_logo_circleimageview);
                mReloadSharesforsaleLogoImageView = (ImageView) itemView.findViewById(R.id.reload_shares_for_sale_logo_imageview);
                mLoadingSharesforsaleLogoImageView = (ProgressBar) itemView.findViewById(R.id.image_loading_shares_for_sale_logo_progressbar);
                mItemForSalePriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mNewsBuyersTextview = (TextView) itemView.findViewById(R.id.news_buyers_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mItemForSaleQuantity = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mItemForSaleInfo1TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info1_textview);
                mItemForSaleInfo2TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info2_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyIconImageView = (ImageView) itemView.findViewById(R.id.buy_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mRepostsTextView = (TextView) itemView.findViewById(R.id.news_reposts_textview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);

                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mSharesForSaleIconHolderImageView.setOnClickListener(innerClickListener);
                mSharesLogoCircleImageView.setOnClickListener(innerClickListener);
                mReloadSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mLoadingSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mItemForSalePriceTextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo1TextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo2TextView.setOnClickListener(innerClickListener);
                mItemForSaleQuantity.setOnClickListener(innerClickListener);
                mNewsBuyersTextview.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostsTextView.setOnClickListener(innerClickListener);
                mBuyIconImageView.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);
            }
        }

        // NEW TYPE 17 VIEW HOLDER
        public class NewsType_17_Reposted_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPottNameTextView, mFullNameTextView, mNewsTextTextView,
                    mNewsTimeTextView, mNewsLikesTextView, mNewsDislikesTextView,
                    mItemForSalePriceTextView, mItemForSaleInfo1TextView, mItemForSaleInfo2TextView, mItemForSaleQuantity,
                    mNewsBuyersTextview, mNewsCommentsTextView, mNewsViewsTextView, mNewsReadMoreTextView, mRepostsTextView;
            private CircleImageView mPottPicCircleImageView, mSharesLogoCircleImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private ImageView mVerifiedIconImageView, mShopIconImageView,
                    mMoreOptionsImageView, mCommentIconImageView, mRepostIconImageView,
                    mSharesForSaleIconHolderImageView, mNewsVideoCoverArtImageView,
                    mPlayVideoIconImageView, mBuyIconImageView, mReloadSharesforsaleLogoImageView;
            private ConstraintLayout mSharesForSaleIconHolderConstraintLayout;
            private ProgressBar mLoadingSharesforsaleLogoImageView, mLoadingVideoProgressBar;
            private View parent;

            private CheckBox mRepostedLikeIconImageView, mRepostedDislikeIconImageView;

            private TextView mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview, mNewsRepostedAddedItemPriceTextview;
            private CircleImageView mNewsRepostedAddedItemImageCircleImageView;
            private ConstraintLayout mNewsRepostedAddedItemPriceHolderConstraintlayout, mNewsRepostedAddedItemHolderConstraintlayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            private View.OnClickListener innerClickListener2 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers2(v, mLikeIconImageView, mDislikeIconImageView, mRepostedLikeIconImageView, mRepostedDislikeIconImageView, getAdapterPosition());
                }
            };


            public NewsType_17_Reposted_ViewHolder(View v) {
                super(v);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.news_maker_pottname_textview);
                mFullNameTextView = (TextView) itemView.findViewById(R.id.news_maker_fullname_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mVerifiedIconImageView = (ImageView) itemView.findViewById(R.id.news_maker_verifiedicon_imageview);
                mShopIconImageView = (ImageView) itemView.findViewById(R.id.business_account_icon_imageview);
                mMoreOptionsImageView = (ImageView) itemView.findViewById(R.id.news_options_imageview);
                mSharesForSaleIconHolderConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.sharesforsale_icon_background);
                mSharesForSaleIconHolderImageView = (ImageView) itemView.findViewById(R.id.sharesforsale_icon);
                mNewsVideoCoverArtImageView = (ImageView) itemView.findViewById(R.id.cover);
                mPlayVideoIconImageView = (ImageView) itemView.findViewById(R.id.play_icon);
                mLoadingVideoProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
                mSharesLogoCircleImageView = (CircleImageView) itemView.findViewById(R.id.shares_for_sale_logo_circleimageview);
                mReloadSharesforsaleLogoImageView = (ImageView) itemView.findViewById(R.id.reload_shares_for_sale_logo_imageview);
                mLoadingSharesforsaleLogoImageView = (ProgressBar) itemView.findViewById(R.id.image_loading_shares_for_sale_logo_progressbar);
                mItemForSalePriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mNewsBuyersTextview = (TextView) itemView.findViewById(R.id.news_buyers_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mItemForSaleQuantity = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mItemForSaleInfo1TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info1_textview);
                mItemForSaleInfo2TextView = (TextView) itemView.findViewById(R.id.item_for_sale_info2_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsReadMoreTextView = (TextView) itemView.findViewById(R.id.news_text_readmore_textview);
                mNewsTimeTextView = (TextView) itemView.findViewById(R.id.news_time_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyIconImageView = (ImageView) itemView.findViewById(R.id.buy_icon_imageview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mRepostsTextView = (TextView) itemView.findViewById(R.id.news_reposts_textview);

                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mNewsRepostedAddedItemPriceTextview = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);
                mNewsRepostedAddedItemImageCircleImageView = (CircleImageView)itemView.findViewById(R.id.news_reposted_addeditem_image_circleimageview);
                mNewsRepostedAddedItemPriceHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_additem_price_background_constraintlayout);
                mNewsRepostedAddedItemHolderConstraintlayout = (ConstraintLayout) itemView.findViewById(R.id.news_reposted_added_item_background);
                mRepostedLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_like_icon_imageview);
                mRepostedDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_reposted_dislike_icon_imageview);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mNewsRepostedCommentTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceTextview.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemImageCircleImageView.setOnClickListener(innerClickListener);
                mNewsRepostedAddedItemPriceHolderConstraintlayout.setOnClickListener(innerClickListener);

                // ALL ON-CLICK LISTENERS
                mPottNameTextView.setOnClickListener(innerClickListener);
                mFullNameTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mVerifiedIconImageView.setOnClickListener(innerClickListener);
                mShopIconImageView.setOnClickListener(innerClickListener);
                mMoreOptionsImageView.setOnClickListener(innerClickListener);
                mSharesForSaleIconHolderImageView.setOnClickListener(innerClickListener);
                mPlayVideoIconImageView.setOnClickListener(innerClickListener);
                mSharesLogoCircleImageView.setOnClickListener(innerClickListener);
                mReloadSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mLoadingSharesforsaleLogoImageView.setOnClickListener(innerClickListener);
                mItemForSalePriceTextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo1TextView.setOnClickListener(innerClickListener);
                mItemForSaleInfo2TextView.setOnClickListener(innerClickListener);
                mItemForSaleQuantity.setOnClickListener(innerClickListener);
                mNewsBuyersTextview.setOnClickListener(innerClickListener);
                mNewsTextTextView.setOnClickListener(innerClickListener);
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                mNewsTimeTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mRepostsTextView.setOnClickListener(innerClickListener);
                mBuyIconImageView.setOnClickListener(innerClickListener);
                mRepostedLikeIconImageView.setOnClickListener(innerClickListener2);
                mRepostedDislikeIconImageView.setOnClickListener(innerClickListener2);
                parent = v;
            }
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


        public class News_Posting_ViewHolder extends RecyclerView.ViewHolder {
            private TextView mPostingTextView, mNewsTextView;
            private ProgressBar mNewsPostingProgressBar;
            private Button mRetryButton;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public News_Posting_ViewHolder(View v) {
                super(v);
                mNewsPostingProgressBar = (ProgressBar) itemView.findViewById(R.id.posting_progressbar);
                mNewsTextView = (TextView) itemView.findViewById(R.id.news_posting_newstext_textview);
                mPostingTextView = (TextView) itemView.findViewById(R.id.news_posting_text_textview);
                mRetryButton = (Button) itemView.findViewById(R.id.news_postin_retry_button);

                mRetryButton.setOnClickListener(innerClickListener);
            }
        }

        public class News_Loading_More_At_Bottom_ViewHolder extends RecyclerView.ViewHolder {
            private ImageView mRetryImageView;
            private ProgressBar mLoadingNewsprogressBar;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public News_Loading_More_At_Bottom_ViewHolder(View v) {
                super(v);
                mRetryImageView = (ImageView) itemView.findViewById(R.id.load_more_news_at_bottom_retry_imageview);
                mLoadingNewsprogressBar = (ProgressBar) itemView.findViewById(R.id.loading_more_news_at_bottom_progressbar);

                mRetryImageView.setOnClickListener(innerClickListener);
                mLoadingNewsprogressBar.setOnClickListener(innerClickListener);
            }
        }


        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (FullNewsListDataGenerator.getAllData().get(position).getNewsId() != null){
                if (!FullNewsListDataGenerator.getAllData().get(position).getNewsId().trim().equalsIgnoreCase("")){
                    Config.sendNewsViewed(getApplicationContext(), FullNewsListDataGenerator.getAllData().get(position).getNewsId(), FullNewsListDataGenerator.getAllData().get(position).getRowId(), LocaleHelper.getLanguage(getApplicationContext()));
                    //Log.e("NewfeedNewsVIEWS", "VIEWS BEING SENT");
                }
            }

            if (holder instanceof NewsType_1_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_1_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_1_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setallTextsInNews(new TextView[] {((NewsType_1_ViewHolder) holder).mPottNameTextView, ((NewsType_1_ViewHolder) holder).mFullNameTextView, ((NewsType_1_ViewHolder) holder).mNewsTimeTextView, ((NewsType_1_ViewHolder) holder).mNewsTextTextView, ((NewsType_1_ViewHolder) holder).mNewsLikesTextView, ((NewsType_1_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_1_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_1_ViewHolder) holder).mNewsViewsTextView, ((NewsType_1_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_1_ViewHolder) holder).mAddedItemQuantityTextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_1_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_1_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_1_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, ((NewsType_1_ViewHolder) holder).mNewsBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_1_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_1_ViewHolder) holder).mAddedItemImageView, ((NewsType_1_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_1_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{((NewsType_1_ViewHolder) holder).mReloadAddedItemImageView, null, null}
                        ,new ProgressBar[]{((NewsType_1_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null}
                        , position);

            } else if (holder instanceof NewsType_2_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_2_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_2_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setallTextsInNews(new TextView[] {((NewsType_2_ViewHolder) holder).mPottNameTextView, ((NewsType_2_ViewHolder) holder).mFullNameTextView, ((NewsType_2_ViewHolder) holder).mNewsTimeTextView, ((NewsType_2_ViewHolder) holder).mNewsTextTextView, ((NewsType_2_ViewHolder) holder).mNewsLikesTextView, ((NewsType_2_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_2_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_2_ViewHolder) holder).mNewsViewsTextView, ((NewsType_2_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_2_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_2_ViewHolder) holder).mNewsReadMoreTextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_2_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_2_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_2_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, ((NewsType_2_ViewHolder) holder).mNewsBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_2_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_2_ViewHolder) holder).mAddedItemImageView, ((NewsType_2_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_2_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{((NewsType_2_ViewHolder) holder).mReloadAddedItemImageView, null, null}
                        ,new ProgressBar[]{((NewsType_2_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null}
                        , position);

            } else if (holder instanceof NewsType_3_to_4_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_3_to_4_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_3_to_4_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_3_to_4_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getApplicationContext()));
                addDotsIndicator(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), 0, ((NewsType_3_to_4_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_3_to_4_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_3_to_4_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_3_to_4_ViewHolder) holder).viewPagerListener);

                setallTextsInNews(new TextView[] {((NewsType_3_to_4_ViewHolder) holder).mPottNameTextView, ((NewsType_3_to_4_ViewHolder) holder).mFullNameTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsTimeTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsTextTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsLikesTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsViewsTextView, ((NewsType_3_to_4_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_3_to_4_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsReadMoreTextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_3_to_4_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_3_to_4_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_3_to_4_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_3_to_4_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_3_to_4_ViewHolder) holder).mAddedItemImageView, ((NewsType_3_to_4_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_3_to_4_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{((NewsType_3_to_4_ViewHolder) holder).mReloadAddedItemImageView, null, null}
                        ,new ProgressBar[]{((NewsType_3_to_4_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null}
                        , position);

            } else if (holder instanceof NewsType_5_to_6_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_5_to_6_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_5_to_6_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_5_to_6_ViewHolder) holder).parent.setTag(this);
                setallTextsInNews(new TextView[] {((NewsType_5_to_6_ViewHolder) holder).mPottNameTextView, ((NewsType_5_to_6_ViewHolder) holder).mFullNameTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsTimeTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsTextTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsLikesTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsViewsTextView, ((NewsType_5_to_6_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_5_to_6_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsReadMoreTextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_5_to_6_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_5_to_6_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_5_to_6_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_5_to_6_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_5_to_6_ViewHolder) holder).mAddedItemImageView, ((NewsType_5_to_6_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_5_to_6_ViewHolder) holder).mShopIconImageView, ((NewsType_5_to_6_ViewHolder) holder).mNewsVideoCoverArtImageView}
                        ,new ImageView[]{((NewsType_5_to_6_ViewHolder) holder).mReloadAddedItemImageView, null, null, null}
                        ,new ProgressBar[]{((NewsType_5_to_6_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, ((NewsType_5_to_6_ViewHolder) holder).mLoadingVideoProgressBar}
                        , position);

            } else if (holder instanceof NewsType_7_and_9_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_7_and_9_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_7_and_9_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setallTextsInNews(new TextView[] {((NewsType_7_and_9_ViewHolder) holder).mPottNameTextView, ((NewsType_7_and_9_ViewHolder) holder).mFullNameTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsTimeTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsTextTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsLikesTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsViewsTextView, ((NewsType_7_and_9_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_7_and_9_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_7_and_9_ViewHolder) holder).mNewsWebsiteTitle}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_7_and_9_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_7_and_9_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_7_and_9_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_7_and_9_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_7_and_9_ViewHolder) holder).mAddedItemImageView, ((NewsType_7_and_9_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_7_and_9_ViewHolder) holder).mShopIconImageView, null, ((NewsType_7_and_9_ViewHolder) holder).mNewsImageImageView}
                        ,new ImageView[]{((NewsType_7_and_9_ViewHolder) holder).mReloadAddedItemImageView, null, null, null, ((NewsType_7_and_9_ViewHolder) holder).mReloadNewsImageImageView}
                        ,new ProgressBar[]{((NewsType_7_and_9_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, null, ((NewsType_7_and_9_ViewHolder) holder).mLoadingNewsImageProgressBar}
                        , position);
            } else if (holder instanceof NewsType_8_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_8_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_8_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_8_ViewHolder) holder).parent.setTag(this);
                setallTextsInNews(new TextView[] {((NewsType_8_ViewHolder) holder).mPottNameTextView, ((NewsType_8_ViewHolder) holder).mFullNameTextView, ((NewsType_8_ViewHolder) holder).mNewsTimeTextView, ((NewsType_8_ViewHolder) holder).mNewsTextTextView, ((NewsType_8_ViewHolder) holder).mNewsLikesTextView, ((NewsType_8_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_8_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_8_ViewHolder) holder).mNewsViewsTextView, ((NewsType_8_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_8_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_8_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_8_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_8_ViewHolder) holder).mNewsWebsiteTitle}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_8_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_8_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_8_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_8_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_8_ViewHolder) holder).mAddedItemImageView, ((NewsType_8_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_8_ViewHolder) holder).mShopIconImageView, ((NewsType_8_ViewHolder) holder).mNewsVideoCoverArtImageView}
                        ,new ImageView[]{((NewsType_8_ViewHolder) holder).mReloadAddedItemImageView, null, null, null}
                        ,new ProgressBar[]{((NewsType_8_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, ((NewsType_8_ViewHolder) holder).mLoadingVideoProgressBar}
                        , position);

            } else if (holder instanceof NewsType_10_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_10_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_10_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_10_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getApplicationContext()));
                addDotsIndicator(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getApplicationContext(), 0, ((NewsType_10_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_10_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_10_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_10_ViewHolder) holder).viewPagerListener);
                setallTextsInNews(new TextView[] {((NewsType_10_ViewHolder) holder).mPottNameTextView, ((NewsType_10_ViewHolder) holder).mFullNameTextView, ((NewsType_10_ViewHolder) holder).mNewsTimeTextView, ((NewsType_10_ViewHolder) holder).mNewsTextTextView, ((NewsType_10_ViewHolder) holder).mNewsLikesTextView, ((NewsType_10_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_10_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_10_ViewHolder) holder).mNewsViewsTextView, ((NewsType_10_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_10_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_10_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_10_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_10_ViewHolder) holder).mRepostsTextView, ((NewsType_10_ViewHolder) holder).mItemForSaleNameTextView, ((NewsType_10_ViewHolder) holder).mItemForSaleLocationTextView}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_10_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{null, ((NewsType_10_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_10_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);

            } else if (holder instanceof NewsType_14_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_14_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_14_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_14_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getApplicationContext()));
                addDotsIndicator(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getApplicationContext(), 0, ((NewsType_14_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_14_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_14_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_14_ViewHolder) holder).viewPagerListener);

                setallTextsInNews(new TextView[] {((NewsType_14_ViewHolder) holder).mPottNameTextView, ((NewsType_14_ViewHolder) holder).mFullNameTextView, ((NewsType_14_ViewHolder) holder).mNewsTimeTextView, ((NewsType_14_ViewHolder) holder).mNewsTextTextView, ((NewsType_14_ViewHolder) holder).mNewsLikesTextView, ((NewsType_14_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_14_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_14_ViewHolder) holder).mNewsViewsTextView, ((NewsType_14_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_14_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_14_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_14_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_14_ViewHolder) holder).mRepostsTextView, ((NewsType_14_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_14_ViewHolder) holder).mItemForSaleInfo2TextView}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_14_ViewHolder) holder).mPottPicCircleImageView, ((NewsType_14_ViewHolder) holder).mSharesLogoCircleImageView}
                        ,new ImageView[]{null,((NewsType_14_ViewHolder) holder).mReloadSharesforsaleLogoImageView}
                        ,new ProgressBar[]{null,((NewsType_14_ViewHolder) holder).mLoadingSharesforsaleLogoImageView}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{null, ((NewsType_14_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_14_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);

                ((NewsType_14_ViewHolder) holder).mReloadSharesforsaleLogoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().equalsIgnoreCase("")){
                            Config.loadUrlImageWithProgressBarAndReloadImage(getApplicationContext(), true, FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().split("\\s+")[0], ((NewsType_14_ViewHolder) holder).mSharesLogoCircleImageView, 0, 50, 50, ((NewsType_14_ViewHolder) holder).mReloadSharesforsaleLogoImageView, ((NewsType_14_ViewHolder) holder).mLoadingSharesforsaleLogoImageView);
                        }
                    }
                });
            } else if (holder instanceof NewsType_17_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_17_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_17_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_17_ViewHolder) holder).parent.setTag(this);
                setallTextsInNews(new TextView[] {((NewsType_17_ViewHolder) holder).mPottNameTextView, ((NewsType_17_ViewHolder) holder).mFullNameTextView, ((NewsType_17_ViewHolder) holder).mNewsTimeTextView, ((NewsType_17_ViewHolder) holder).mNewsTextTextView, ((NewsType_17_ViewHolder) holder).mNewsLikesTextView, ((NewsType_17_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_17_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_17_ViewHolder) holder).mNewsViewsTextView, ((NewsType_17_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_17_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_17_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_17_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_17_ViewHolder) holder).mRepostsTextView, ((NewsType_17_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_17_ViewHolder) holder).mItemForSaleInfo2TextView}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_17_ViewHolder) holder).mPottPicCircleImageView, ((NewsType_17_ViewHolder) holder).mSharesLogoCircleImageView}
                        ,new ImageView[]{null,((NewsType_17_ViewHolder) holder).mReloadSharesforsaleLogoImageView}
                        ,new ProgressBar[]{null,((NewsType_17_ViewHolder) holder).mLoadingSharesforsaleLogoImageView}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{null, ((NewsType_17_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_17_ViewHolder) holder).mShopIconImageView, ((NewsType_17_ViewHolder) holder).mNewsVideoCoverArtImageView}
                        ,new ImageView[]{null, null, null, null}
                        ,new ProgressBar[]{null, null, null, ((NewsType_17_ViewHolder) holder).mLoadingVideoProgressBar}
                        , position);

            } else if (holder instanceof NewsType_1_Sponsored_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_1_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_1_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_1_Sponsored_ViewHolder) holder).mAdvertActionButton.setText(FullNewsListDataGenerator.getAllData().get(position).getAdvertButtonText());
                setallTextsInNews(new TextView[] {((NewsType_1_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, ((NewsType_1_Sponsored_ViewHolder) holder).mAdvertInfo1TextView, ((NewsType_1_Sponsored_ViewHolder) holder).mAdvertInfo2TextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_1_Sponsored_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemImageView, ((NewsType_1_Sponsored_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_1_Sponsored_ViewHolder) holder).mShopIconImageView, null, null, ((NewsType_1_Sponsored_ViewHolder) holder).mAdvertIconImageView}
                        ,new ImageView[]{((NewsType_1_Sponsored_ViewHolder) holder).mReloadAddedItemImageView, null, null, null, null, null}
                        ,new ProgressBar[]{((NewsType_1_Sponsored_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, null, null, null}
                        , position);

            } else if (holder instanceof NewsType_2_Sponsored_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_2_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_2_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_2_Sponsored_ViewHolder) holder).mAdvertActionButton.setText(FullNewsListDataGenerator.getAllData().get(position).getAdvertButtonText());
                setallTextsInNews(new TextView[] {((NewsType_2_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, ((NewsType_2_Sponsored_ViewHolder) holder).mAdvertInfo1TextView, ((NewsType_2_Sponsored_ViewHolder) holder).mAdvertInfo2TextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_2_Sponsored_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemImageView, ((NewsType_2_Sponsored_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_2_Sponsored_ViewHolder) holder).mShopIconImageView, null, null, ((NewsType_2_Sponsored_ViewHolder) holder).mAdvertIconImageView}
                        ,new ImageView[]{((NewsType_2_Sponsored_ViewHolder) holder).mReloadAddedItemImageView, null, null, null, null, null}
                        ,new ProgressBar[]{((NewsType_2_Sponsored_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, null, null, null}
                        , position);

            } else if (holder instanceof NewsType_3_to_4_Sponsored_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getApplicationContext()));
                addDotsIndicator(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getApplicationContext(), 0, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_3_to_4_Sponsored_ViewHolder) holder).viewPagerListener);

                setallTextsInNews(new TextView[] {((NewsType_3_to_4_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsReadMoreTextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_3_to_4_Sponsored_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemImageView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mShopIconImageView, null, null, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAdvertIconImageView}
                        ,new ImageView[]{((NewsType_3_to_4_Sponsored_ViewHolder) holder).mReloadAddedItemImageView, null, null, null, null, null}
                        ,new ProgressBar[]{((NewsType_3_to_4_Sponsored_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, null, null, null}
                        , position);

            } else if (holder instanceof NewsType_5_to_6_Sponsored_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_5_to_6_Sponsored_ViewHolder) holder).parent.setTag(this);
                setallTextsInNews(new TextView[] {((NewsType_5_to_6_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsReadMoreTextView}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_5_to_6_Sponsored_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemImageView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mShopIconImageView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsVideoCoverArtImageView, null , ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAdvertIconImageView}
                        ,new ImageView[]{((NewsType_5_to_6_Sponsored_ViewHolder) holder).mReloadAddedItemImageView, null, null, null, null, null}
                        ,new ProgressBar[]{((NewsType_5_to_6_Sponsored_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mLoadingVideoProgressBar, null, null}
                        , position);
            } else if (holder instanceof NewsType_1_Reposted_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_1_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_1_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_1_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_1_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                setallTextsInNews(new TextView[] {((NewsType_1_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_1_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_1_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_1_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, null, null, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_1_Reposted_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_1_Reposted_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_1_Reposted_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, ((NewsType_1_Reposted_ViewHolder) holder).mNewsBackgroundConstraintLayout, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_1_Reposted_ViewHolder) holder).mPottPicCircleImageView,null, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null,null,null}
                        ,new ProgressBar[]{null,null,null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_1_Reposted_ViewHolder) holder).mAddedItemImageView, ((NewsType_1_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_1_Reposted_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{((NewsType_1_Reposted_ViewHolder) holder).mReloadAddedItemImageView, null, null}
                        ,new ProgressBar[]{((NewsType_1_Reposted_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null}
                        , position);


            } else if (holder instanceof NewsType_2_Reposted_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_2_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_2_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_2_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_2_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                setallTextsInNews(new TextView[] {((NewsType_2_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_2_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_2_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_2_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, null, null, null, null, null, null, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_2_Reposted_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_2_Reposted_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_2_Reposted_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, ((NewsType_2_Reposted_ViewHolder) holder).mNewsBackgroundConstraintLayout, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_2_Reposted_ViewHolder) holder).mPottPicCircleImageView, null, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_2_Reposted_ViewHolder) holder).mAddedItemImageView, ((NewsType_2_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_2_Reposted_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{((NewsType_2_Reposted_ViewHolder) holder).mReloadAddedItemImageView, null, null}
                        ,new ProgressBar[]{((NewsType_2_Reposted_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null}
                        , position);

            } else if (holder instanceof NewsType_3_to_4_Reposted_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getApplicationContext()));
                addDotsIndicator(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), 0, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_3_to_4_Reposted_ViewHolder) holder).viewPagerListener);

                setallTextsInNews(new TextView[] {((NewsType_3_to_4_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, null, null, null, null, null, null, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, null, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_3_to_4_Reposted_ViewHolder) holder).mPottPicCircleImageView, null, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemImageView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{((NewsType_3_to_4_Reposted_ViewHolder) holder).mReloadAddedItemImageView, null, null}
                        ,new ProgressBar[]{((NewsType_3_to_4_Reposted_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null}
                        , position);

            } else if (holder instanceof NewsType_5_to_6_Reposted_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                ((NewsType_5_to_6_Reposted_ViewHolder) holder).parent.setTag(this);
                setallTextsInNews(new TextView[] {((NewsType_5_to_6_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, null, null, null, null, null, null, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, null, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_5_to_6_Reposted_ViewHolder) holder).mPottPicCircleImageView, null, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemImageView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mShopIconImageView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsVideoCoverArtImageView}
                        ,new ImageView[]{((NewsType_5_to_6_Reposted_ViewHolder) holder).mReloadAddedItemImageView, null, null, null}
                        ,new ProgressBar[]{((NewsType_5_to_6_Reposted_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mLoadingVideoProgressBar}
                        , position);
            } else if (holder instanceof NewsType_7_and_9_Reposted_ViewHolder) {

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

                setallTextsInNews(new TextView[] {((NewsType_7_and_9_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsWebsiteTitle, null, null, null, null, null, null, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, null, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_7_and_9_Reposted_ViewHolder) holder).mPottPicCircleImageView, null, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemImageView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mShopIconImageView, null, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsImageImageView}
                        ,new ImageView[]{((NewsType_7_and_9_Reposted_ViewHolder) holder).mReloadAddedItemImageView, null, null, null, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mReloadNewsImageImageView}
                        ,new ProgressBar[]{((NewsType_7_and_9_Reposted_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, null, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mLoadingNewsImageProgressBar}
                        , position);
            } else if (holder instanceof NewsType_8_Reposted_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_8_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_8_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_8_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_8_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                ((NewsType_8_Reposted_ViewHolder) holder).parent.setTag(this);
                setallTextsInNews(new TextView[] {((NewsType_8_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_8_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_8_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_8_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_8_Reposted_ViewHolder) holder).mNewsWebsiteTitle, null, null, null, null, null, null, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{((NewsType_8_Reposted_ViewHolder) holder).mAddedItemPriceBackgroundConstraintLayout, ((NewsType_8_Reposted_ViewHolder) holder).mAddedItemIconBackgroundConstraintLayout, ((NewsType_8_Reposted_ViewHolder) holder).mAddedItemQuantityBackgroundConstraintLayout, null, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_8_Reposted_ViewHolder) holder).mPottPicCircleImageView, null, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{((NewsType_8_Reposted_ViewHolder) holder).mAddedItemImageView, ((NewsType_8_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_8_Reposted_ViewHolder) holder).mShopIconImageView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsVideoCoverArtImageView}
                        ,new ImageView[]{((NewsType_8_Reposted_ViewHolder) holder).mReloadAddedItemImageView, null, null, null}
                        ,new ProgressBar[]{((NewsType_8_Reposted_ViewHolder) holder).mLoadingAddedItemProgressBar, null, null, ((NewsType_8_Reposted_ViewHolder) holder).mLoadingVideoProgressBar}
                        , position);

            } else if (holder instanceof NewsType_10_Reposted_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_10_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_10_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_10_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_10_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

                ((RecyclerViewAdapter.NewsType_10_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getApplicationContext()));
                addDotsIndicator(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), 0, ((NewsType_10_Reposted_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_10_Reposted_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_10_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_10_Reposted_ViewHolder) holder).viewPagerListener);

                setallTextsInNews(new TextView[] {((NewsType_10_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_10_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_10_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_10_Reposted_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_10_Reposted_ViewHolder) holder).mRepostsTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSaleNameTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSaleLocationTextView, null, null, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{null, null, null, null, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_10_Reposted_ViewHolder) holder).mPottPicCircleImageView, null, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{null, ((NewsType_10_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_10_Reposted_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);

            } else if (holder instanceof NewsType_14_Reposted_ViewHolder) {
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_14_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_14_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_14_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_14_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

                ((RecyclerViewAdapter.NewsType_14_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getApplicationContext()));
                addDotsIndicator(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getApplicationContext(), 0, ((NewsType_14_Reposted_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_14_Reposted_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_14_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_14_Reposted_ViewHolder) holder).viewPagerListener);

                setallTextsInNews(new TextView[] {((NewsType_14_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_14_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_14_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_14_Reposted_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_14_Reposted_ViewHolder) holder).mRepostsTextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSaleInfo2TextView, null, null, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setallConstraintLayoutBackgroundsInNews(new ConstraintLayout[]{null, null, null, null, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceHolderConstraintlayout,  ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedAddedItemHolderConstraintlayout}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_14_Reposted_ViewHolder) holder).mPottPicCircleImageView, ((NewsType_14_Reposted_ViewHolder) holder).mSharesLogoCircleImageView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null,((NewsType_14_Reposted_ViewHolder) holder).mReloadSharesforsaleLogoImageView, null}
                        ,new ProgressBar[]{null,((NewsType_14_Reposted_ViewHolder) holder).mLoadingSharesforsaleLogoImageView, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{null, ((NewsType_14_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_14_Reposted_ViewHolder) holder).mShopIconImageView}
                        ,new ImageView[]{null, null, null}
                        ,new ProgressBar[]{null, null, null}
                        , position);

            } else if (holder instanceof NewsType_17_Reposted_ViewHolder) {
                ((NewsType_17_Reposted_ViewHolder) holder).parent.setTag(this);
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_17_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_17_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_17_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_17_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

                setallTextsInNews(new TextView[] {((NewsType_17_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_17_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_17_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_17_Reposted_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_17_Reposted_ViewHolder) holder).mRepostsTextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSaleInfo2TextView, null, null, ((NewsType_17_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_17_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_17_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_17_Reposted_ViewHolder) holder).mPottPicCircleImageView, ((NewsType_17_Reposted_ViewHolder) holder).mSharesLogoCircleImageView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsRepostedAddedItemImageCircleImageView}
                        ,new ImageView[]{null,((NewsType_17_Reposted_ViewHolder) holder).mReloadSharesforsaleLogoImageView, null}
                        ,new ProgressBar[]{null,((NewsType_17_Reposted_ViewHolder) holder).mLoadingSharesforsaleLogoImageView, null}
                        , position);
                setAllImageViewsInNews(
                        new ImageView[]{null, ((NewsType_17_Reposted_ViewHolder) holder).mVerifiedIconImageView, ((NewsType_17_Reposted_ViewHolder) holder).mShopIconImageView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsVideoCoverArtImageView}
                        ,new ImageView[]{null, null, null, null}
                        ,new ProgressBar[]{null, null, null, ((NewsType_17_Reposted_ViewHolder) holder).mLoadingVideoProgressBar}
                        , position);
            } else if (holder instanceof News_Posting_ViewHolder) {

                if(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim().equalsIgnoreCase("")){
                    ((News_Posting_ViewHolder) holder).mNewsTextView.setVisibility(View.GONE);
                } else {
                    ((News_Posting_ViewHolder) holder).mNewsTextView.setVisibility(View.VISIBLE);
                    ((News_Posting_ViewHolder) holder).mNewsTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim());
                    /*
                    if(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim().length() > 50){
                        ((News_Posting_ViewHolder) holder).mNewsTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim().substring(0,50) + "...");
                    } else {
                        ((News_Posting_ViewHolder) holder).mNewsTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim());
                    }
                    */
                }

                if(FullNewsListDataGenerator.getAllData().get(position).getNewsHasBeenPosted() == 2){
                    ((News_Posting_ViewHolder) holder).mPostingTextView.setText(getString(R.string.your_news_has_been_posted));
                    ((News_Posting_ViewHolder) holder).mNewsPostingProgressBar.setVisibility(View.GONE);
                    ((News_Posting_ViewHolder) holder).mRetryButton.setVisibility(View.GONE);

                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsHasBeenPosted() == 1){
                    ((News_Posting_ViewHolder) holder).mPostingTextView.setText(getString(R.string.posting));
                    ((News_Posting_ViewHolder) holder).mRetryButton.setVisibility(View.GONE);
                    ((News_Posting_ViewHolder) holder).mNewsPostingProgressBar.setVisibility(View.VISIBLE);
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsHasBeenPosted() == 0){
                    ((News_Posting_ViewHolder) holder).mPostingTextView.setText(R.string.your_post_failed_please_retry);
                    ((News_Posting_ViewHolder) holder).mNewsPostingProgressBar.setVisibility(View.GONE);
                    ((News_Posting_ViewHolder) holder).mRetryButton.setVisibility(View.VISIBLE);
                }
            } else if (holder instanceof CommentViewHolder) {
                //SETTING ON-CLICK LISTENER
                ((CommentViewHolder) holder).mPottNameTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottName());
                ((CommentViewHolder) holder).mDateTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsTime());
                ((CommentViewHolder) holder).mCommentTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText());

            } else if (holder instanceof LikeDislikeViewPurchasesViewHolder){
                //SETTING ON-CLICK LISTENER
                ((LikeDislikeViewPurchasesViewHolder) holder).mPottNameTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottName());
                ((LikeDislikeViewPurchasesViewHolder) holder).mDateTextView.setText(FullNewsListDataGenerator.getAllData().get(position).getNewsTime());

                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottPic().trim().equalsIgnoreCase("")){
                            Config.loadUrlImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottPic().trim(), ((LikeDislikeViewPurchasesViewHolder) holder).mPottPicCircleImageView, 0, 60, 60);
                        } else {
                            Config.loadUrlImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottPic().trim(), ((LikeDislikeViewPurchasesViewHolder) holder).mPottPicCircleImageView, 0, 60, 60);
                        }
                    }
                });
                imageLoaderThread.start();
            }
            else if (holder instanceof News_Loading_More_At_Bottom_ViewHolder) {

            }
        }

        @Override
        public int getItemCount() {
            return FullNewsListDataGenerator.getAllData().size();
        }

    }


    public void setLikedOrDisliked(int likeStatus, CheckBox likeIcon, CheckBox dislikeIcon, int position){
        // CHECKING FETCHER LIKE STATUS // 1 = LIKE, 0 = DISLIKE, -1 = NOTHING
        if(likeStatus == Config.LIKED){
            likeIcon.setChecked(true);
            dislikeIcon.setChecked(false);
        } else if(likeStatus == Config.DISLIKED){
            dislikeIcon.setChecked(true);
            likeIcon.setChecked(false);
        } else {
            likeIcon.setChecked(false);
            dislikeIcon.setChecked(false);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        REQUEST_MAKER_IS_STILL_ACTIVE_AND_IN_VIEW = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        REQUEST_MAKER_IS_STILL_ACTIVE_AND_IN_VIEW = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        REQUEST_MAKER_IS_STILL_ACTIVE_AND_IN_VIEW = false;
        // CLOSE BACKGROUND THREAD START
        suggestedLinkupsFetchThread = Config.closeBackgroundThread2(suggestedLinkupsFetchThread);
        suggestedLinkupsFetchThread2 = Config.closeBackgroundThread2(suggestedLinkupsFetchThread2);
        imageLoaderThread = Config.closeBackgroundThread2(imageLoaderThread);
        //CLOSE BACKGROUND THREAD END
    }

    @Override
    public void finish() {
        super.finish();
        REQUEST_MAKER_IS_STILL_ACTIVE_AND_IN_VIEW = false;
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }



    private void setallTextsInNews(TextView[] allTextViews, int position){
        // 0 = pottname, 1 = fullname, 2 = newsTime, 3 = newsText
        // 4 = Likes, 5 = Dislikes, 6 = Comments, 7 = Views
        // 8 = AddedItemPrice, 9 = ItemQuantity, 10 = ReadMore
        // 11 = UrlAddress, 12 = UrlTitle, 13 = buyers/Transactions
        // 14 = Reposts, 15 = ItemName, 16 = ItemLocation
        // 17 = AdvertInfo1, 18 = AdvertInfo2
        // 19 = ReposterPottName , 20 = RepostComment, 21 = RepostItemPrice


        for (int i = 0; i < allTextViews.length; i++){
            if(i == 0 && allTextViews[0] != null){
                allTextViews[0].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottName());
            } else if(i == 1 && allTextViews[1] != null){
                allTextViews[1].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerFullName());
            } else if(i == 2 && allTextViews[2] != null){
                allTextViews[2].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsTime());
            } else if(i == 3 && allTextViews[3] != null){
                /*
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                        && FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim().length() > 500
                        ){
                    allTextViews[3].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim().substring(0,490) + "...");
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                        && FullNewsListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                        && FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim().length() > 200){
                    allTextViews[3].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim().substring(0,190) + "...");
                } else {
                    allTextViews[3].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim());
                }
                */

                allTextViews[3].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsText().trim());

                if (
                        (
                                FullNewsListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                                        && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemPrice().trim().equalsIgnoreCase("")
                        )
                                ||
                                (
                                        FullNewsListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                                                && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemPrice().trim().equalsIgnoreCase("")
                                )

                                ||
                                (
                                        FullNewsListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_1_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                                                && FullNewsListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")
                                )
                                ||
                                (
                                        FullNewsListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_2_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                                                && FullNewsListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")
                                )
                        ){
                    allTextViews[3].setTextColor(getResources().getColor(R.color.colorAccent));
                }
                Config.linkifyAllMentions(getApplicationContext(), allTextViews[3]);
            } else if(i == 4 && allTextViews[4] != null){
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsLikes().trim().equalsIgnoreCase("")){
                    allTextViews[4].setVisibility(View.GONE);
                } else {
                    allTextViews[4].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsLikes());
                }
            } else if(i == 5 && allTextViews[5] != null){
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsDislikes().trim().equalsIgnoreCase("")){
                    allTextViews[5].setVisibility(View.GONE);
                } else {
                    allTextViews[5].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsDislikes());
                }
            } else if(i == 6 && allTextViews[6] != null){
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsComments().trim().equalsIgnoreCase("")){
                    allTextViews[6].setVisibility(View.GONE);
                } else {
                    allTextViews[6].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsComments());
                }
            } else if(i == 7 && allTextViews[7] != null){
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsViews().trim().equalsIgnoreCase("")){
                    allTextViews[7].setVisibility(View.GONE);
                } else {
                    allTextViews[7].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsViews());
                }
            } else if(i == 8 && allTextViews[8] != null && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allTextViews[8].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemPrice());
            } else if(i == 9 && allTextViews[8] != null && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allTextViews[9].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemQuantity());
            } else if(i == 10 && allTextViews[10] != null){
                allTextViews[10].setVisibility(View.GONE);
                /*if(FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY && FullNewsListDataGenerator.getAllData().get(position).getNewsText().length() > 500){
                    allTextViews[10].setVisibility(View.VISIBLE);
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY && FullNewsListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY && FullNewsListDataGenerator.getAllData().get(position).getNewsText().length() > 200){
                    allTextViews[10].setVisibility(View.VISIBLE);
                } else {
                    allTextViews[10].setVisibility(View.GONE);
                }*/
            } else if(i == 11 && allTextViews[11] != null){
                allTextViews[11].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsUrl());
            } else if(i == 12 && allTextViews[12] != null){
                allTextViews[12].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsUrlTitle());
            } else if(i == 13 && allTextViews[13] != null){
                allTextViews[13].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsTransactions());
            } else if(i == 14 && allTextViews[14] != null){
                allTextViews[14].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsReposts());
            } else if(i == 15 && allTextViews[15] != null){
                allTextViews[15].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsItemName());
            } else if(i == 16 && allTextViews[16] != null){
                allTextViews[16].setText(FullNewsListDataGenerator.getAllData().get(position).getNewsItemLocation());
            } else if(i == 17 && allTextViews[17] != null){
                allTextViews[17].setText(FullNewsListDataGenerator.getAllData().get(position).getAdvertTextTitle());
            } else if(i == 18 && allTextViews[18] != null){
                allTextViews[18].setText(FullNewsListDataGenerator.getAllData().get(position).getAdvertTextTitle2());
            } else if(i == 19 && allTextViews[19] != null){
                allTextViews[19].setText(FullNewsListDataGenerator.getAllData().get(position).getReposterPottName());
            } else if(i == 19 && allTextViews[19] != null){
                allTextViews[19].setText(FullNewsListDataGenerator.getAllData().get(position).getReposterPottName());
            } else if(i == 20 && allTextViews[20] != null){
                allTextViews[20].setText(FullNewsListDataGenerator.getAllData().get(position).getRepostedText());
            } else if(i == 21 && allTextViews[21] != null){
                allTextViews[21].setText(FullNewsListDataGenerator.getAllData().get(position).getRepostedItemPrice());
            }
        }
    }


    private void setallConstraintLayoutBackgroundsInNews(ConstraintLayout[] allConstrainLayouts, int position){
        // 0 = AddedItemPriceBackground, 1 = AddedItemIconBackground, 2 = AddedItemQuantityBackground
        // 3 = NewsTextBackground, 4 = NewsRepostedAddedItemPriceBackground
        // 5 = NewsRepostedAddedItemHolder
        for (int i = 0; i < allConstrainLayouts.length; i++){
            if(i == 0 && allConstrainLayouts[0] != null && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allConstrainLayouts[0].setVisibility(View.VISIBLE);
            } else if(i == 1 && allConstrainLayouts[1] != null && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allConstrainLayouts[1].setVisibility(View.VISIBLE);
            } else if(i == 2 && allConstrainLayouts[2] != null && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allConstrainLayouts[2].setVisibility(View.VISIBLE);
            } else if(i == 3 && allConstrainLayouts[3] != null){
                // 0 = black, 1 = red, 2 = yellow, 3 = green, 4 = orange, 5 = blue
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 0){
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 1) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 2) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 3) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 4) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 5) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.newsBackgroundBlack));
                }

            } else if(i == 4 && allConstrainLayouts[4] != null && FullNewsListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim() != null){
                if(FullNewsListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")){
                    allConstrainLayouts[4].setVisibility(View.INVISIBLE);
                } else {
                    allConstrainLayouts[4].setVisibility(View.VISIBLE);
                }
            } else if(i == 5 && allConstrainLayouts[5] != null && FullNewsListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim() != null){
                if(FullNewsListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")){
                    allConstrainLayouts[5].setVisibility(View.GONE);
                } else {
                    allConstrainLayouts[5].setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setAllImageViewsInNews(final ImageView[] allImageViews, final ImageView[] allErrorImageViews, final ProgressBar[] allProgressBar, final int position){
        // 0 = AddedItemIcon, 1 = VerifiedIcon, 2 = BusinessIcon, 3 = NewsVideoCoverArtImage, 4 = newsImage
        // 5 = AdvertIcon
        for (int i = 0; i < allImageViews.length; i++){
            if(i == 0 && allImageViews[0] != null && allErrorImageViews[0] != null && allProgressBar[0] != null && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1 && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().length() > 1 ){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim(), allImageViews[0], 0, 10, 10, allErrorImageViews[0], allProgressBar[0]);
                    }
                });
                imageLoaderThread.start();

            } else if (i == 1 && allImageViews[1] != null){
                // 1 = green, 2 = blue
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerAccountVerifiedStatus() == 1){
                    allImageViews[1].setVisibility(View.VISIBLE);
                    allImageViews[1].setImageResource(R.drawable.list_item_suggestedlinkupsactivity_activity_verifiedicon_green_imageview);
                } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerAccountVerifiedStatus() == 2){
                    allImageViews[1].setVisibility(View.VISIBLE);
                    allImageViews[1].setImageResource(R.drawable.list_item_suggestedlinkupsactivity_activity_verifiedicon_blue_imageview);
                } else {
                    allImageViews[1].setVisibility(View.INVISIBLE);
                }
            } else if (i == 2 && allImageViews[2] != null){
                //1 = Personal , 2 = Business
                if(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerAccountType() == 2){
                    allImageViews[2].setVisibility(View.VISIBLE);
                } else {
                    allImageViews[2].setVisibility(View.INVISIBLE);
                }
            } else if(i == 3 && allImageViews[3] != null){

                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!FullNewsListDataGenerator.getAllData().get(position).getNewsVideosCoverArtsLinksSeparatedBySpaces().trim().equalsIgnoreCase("")){
                            Config.loadUrlImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsVideosCoverArtsLinksSeparatedBySpaces().trim(), allImageViews[3], 0, 70, 70);
                        } else {
                            allImageViews[3].setImageDrawable(null);
                        }
                    }
                });
                imageLoaderThread.start();

            } else if(i == 4 && allImageViews[4] != null && allErrorImageViews[4] != null && allProgressBar[4] != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_7_AND_9_JUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
                                || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_7_AND_9_REPOSTEDJUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY) {
                            if(!FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().equalsIgnoreCase("")){
                                Config.loadUrlImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim(), allImageViews[4], R.drawable.news_image, 50, 50);
                            } else {
                                Config.loadErrorImageView(FullNewsActivity.this, R.drawable.news_image, allImageViews[4],  50, 50);
                            }
                        } else {
                            if(!FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().equalsIgnoreCase("")){
                                Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+")[0], allImageViews[4], 0, 50, 50, allErrorImageViews[4], allProgressBar[4]);
                            }
                        }
                    }
                });
                imageLoaderThread.start();
            } else if(i == 5 && allImageViews[5] != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(FullNewsListDataGenerator.getAllData().get(position).getAdvertItemIcon().trim() == ""){
                            Config.loadUrlImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getAdvertItemIcon().trim(), allImageViews[5], 0, 20, 20);
                        }
                    }
                });
                imageLoaderThread.start();
            }
        }
    }

    private void setAllCircleImageViewsInNews(final CircleImageView[] allCircleImageViews, final ImageView[] allErrorImageViews, final ProgressBar[] allProgressBar, final int position){
        // 0 = PottPic, 1 = SharesLogo, 2 = NewsRepostedAddedItemImage
        for (int i = 0; i < allCircleImageViews.length; i++){
            if(i == 0 && allCircleImageViews[0] != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottPic().trim().length() > 15){
                            Config.loadUrlImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottPic().trim(), allCircleImageViews[0], 0, 15, 15);
                        } else {
                            Config.loadErrorImageView(FullNewsActivity.this, R.drawable.setprofilepicture_activity_imageholder_default_image, allCircleImageViews[0], 15, 15);
                        }
                    }
                });
                imageLoaderThread.start();
            } else if(i == 1 && allCircleImageViews[1] != null && allErrorImageViews[1] != null && allProgressBar[1] != null && FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemIcon() != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemIcon(), allCircleImageViews[1], 0, 20, 20, allErrorImageViews[1], allProgressBar[1]);
                    }
                });
                imageLoaderThread.start();
            } else if(i == 2 && allCircleImageViews[2] != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(FullNewsListDataGenerator.getAllData().get(position).getRepostedIcon().trim().length() > 15){
                            Config.loadUrlImage(FullNewsActivity.this, true, FullNewsListDataGenerator.getAllData().get(position).getRepostedIcon().trim(), allCircleImageViews[2], 0, 27, 27);
                        }
                    }
                });
                imageLoaderThread.start();
            }
        }
    }

    public void openPottActivity(int position, int openType){
        if(openType == 1){
            Config.openActivity(FullNewsActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", FullNewsListDataGenerator.getAllData().get(position).getNewsMakerPottName());
        }
    }

    private void allOnClickHandlers2(View v, CheckBox likeIcon, CheckBox dislikeIcon, CheckBox repostLikeIcon, CheckBox repostDislikeIcon, int position){
        //Config.showToastType1(FullNewsActivity.this, "Status : " + String.valueOf(FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()));
        String newsId = "";
        if(
                FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_1_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                        || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_2_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                        || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_3_TO_4_REPOSTEDJUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
                        || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_5_TO_6_REPOSTEDNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                        || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_7_AND_9_REPOSTEDJUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
                        || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_8_REPOSTEDJUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                        || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_14_REPOSTEDSHARESFORSALENEWS_VERTICAL_KEY
                        || FullNewsListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_17_REPOSTEDSHARES4SALEWITHVIDEO_VERTICAL_KEY
                ){
            newsId = FullNewsListDataGenerator.getAllData().get(position).getRepostedNewsId();
        } else {
            newsId = FullNewsListDataGenerator.getAllData().get(position).getNewsId();
        }

        if(v.getId() == R.id.news_like_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(FullNewsActivity.this, R.anim.scale_bigout_and_small_in));
            dislikeIcon.setChecked(false);
            if(FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus() != Config.LIKED){
                FullNewsListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.LIKED);
                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , false
                        , Config.LIKED
                        , FullNewsListDataGenerator.getAllData().get(position).getNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            } else {
                FullNewsListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , false
                        , Config.NO_REACTION
                        , FullNewsListDataGenerator.getAllData().get(position).getNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            }
        } else if(v.getId() == R.id.news_dislike_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(FullNewsActivity.this, R.anim.scale_bigout_and_small_in));
            likeIcon.setChecked(false);
            if(FullNewsListDataGenerator.getAllData().get(position).getNewViewerReactionStatus() != Config.DISLIKED){
                FullNewsListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.DISLIKED);
                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , false
                        , Config.DISLIKED
                        , FullNewsListDataGenerator.getAllData().get(position).getNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            } else {
                FullNewsListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , false
                        , Config.NO_REACTION
                        , FullNewsListDataGenerator.getAllData().get(position).getNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            }
        } else if(v.getId() == R.id.news_reposted_like_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(FullNewsActivity.this, R.anim.scale_bigout_and_small_in));
            repostDislikeIcon.setChecked(false);
            if(FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus() != Config.LIKED){
                FullNewsListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.LIKED);
                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , true
                        , Config.LIKED
                        , FullNewsListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            } else {
                FullNewsListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , true
                        , Config.NO_REACTION
                        , FullNewsListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            }
        } else if(v.getId() == R.id.news_reposted_dislike_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(FullNewsActivity.this, R.anim.scale_bigout_and_small_in));
            repostLikeIcon.setChecked(false);
            if(FullNewsListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus() != Config.DISLIKED){
                FullNewsListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.DISLIKED);

                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , true
                        , Config.DISLIKED
                        , FullNewsListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            } else {
                FullNewsListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getApplicationContext()
                        , true
                        , Config.NO_REACTION
                        , FullNewsListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , FullNewsListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getApplicationContext())
                );
            }
        }
    }


    private void allOnClickHandlers(View v, int position){
        if(v.getId() == R.id.news_maker_pottname_textview || v.getId() == R.id.news_maker_fullname_textview || v.getId() == R.id.chat_full_name_textview || v.getId() == R.id.chat_pott_picture_circleimageview  ){
            openPottActivity(position, 1);
        } else if(v.getId() == R.id.news_text_textview){

        } else if(v.getId() == R.id.news_text_readmore_textview){
            //Config.openActivity(FullNewsActivity.this, FullNewsActivity.class, 1, 0, 1, Config.FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, "POSITION : " + String.valueOf(position) + "FULLNEWS ACTIVITY -- NEWSID : " + FullNewsListDataGenerator.getAllData().get(position).getNewsId());
        } else if(v.getId() == R.id.news_added_item_price_textview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_added_item_quantity_textview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_website_address){
            Config.openActivity(FullNewsActivity.this, WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, FullNewsListDataGenerator.getAllData().get(position).getNewsUrl());
        } else if(v.getId() == R.id.news_title){
            Config.openActivity(FullNewsActivity.this, WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, FullNewsListDataGenerator.getAllData().get(position).getNewsUrl());
        } else if(v.getId() == R.id.news_likes_textview){
            openNewsStatsActivity(position, 0);
        } else if(v.getId() == R.id.news_dislikes_textview){
            openNewsStatsActivity(position, 1);
        } else if(v.getId() == R.id.news_comments_textview){
            openNewsStatsActivity(position, 4);
        } else if(v.getId() == R.id.news_views_textview){
            //2 = Views, 3 = Purchases,  4 = Comments
            openNewsStatsActivity(position, 2);
        } else if(v.getId() == R.id.news_buyers_textview){
            //3 = Purchases
            openNewsStatsActivity(position, 3);
        } else if(v.getId() == R.id.news_reposts_textview){

        } else if(v.getId() == R.id.news_maker_pottpic_circleimageview){
            openPottActivity(position, 1);
        } else if(v.getId() == R.id.news_options_imageview){
            /*
            if(FullNewsListDataGenerator.getAllData().get(position).getRepostedNewsId().trim().equalsIgnoreCase("")){
                Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, NewsOptionsFragment.newInstance(FullNewsListDataGenerator.getAllData().get(position).getNewsId()), "NewsOptionsFragment", 3);
            } else {
                Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, NewsOptionsFragment.newInstance(FullNewsListDataGenerator.getAllData().get(position).getRepostedNewsId()), "NewsOptionsFragment", 3);
            }
            */
        } else if(v.getId() == R.id.news_added_item_icon_imageview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.newswithurl_image_imageview){
            Config.openActivity(FullNewsActivity.this, WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, FullNewsListDataGenerator.getAllData().get(position).getNewsUrl());
        } else if(v.getId() == R.id.reload_addeditem_imageview){
            //mAddedItemImageReloadImageView
        } else if(v.getId() == R.id.play_icon){
            mProfileNewsRecyclerView.playVideo();
        } else if(v.getId() == R.id.news_comment_icon_imageview){
            openNewsStatsActivity(position, 4);
            //Config.showDialogType1(FullNewsActivity.this, "1", "News Sku: " +  String.valueOf(FullNewsListDataGenerator.getAllData().get(position).getNewsSku()), "", null, false, "Okay", "");
            //Config.openActivity(FullNewsActivity.this, CommentsActivity.class, 2, 0, 1, "newsid", "POSITION : " + String.valueOf(position) + "COMMENTS ACTIVITY -- NEWSID : " + FullNewsListDataGenerator.getAllData().get(position).getNewsId());
        } else if(v.getId() == R.id.news_repost_icon_imageview){
            //Config.openActivity(FullNewsActivity.this, CreatePostActivity.class, 1, 0, 1, "newsid", FullNewsListDataGenerator.getAllData().get(position).getNewsId());
            Intent intent = new Intent(FullNewsActivity.this, CreatePostActivity.class);
            intent.putExtra("newsid", FullNewsListDataGenerator.getAllData().get(position).getNewsId());
            startActivityForResult(intent, Config.CREATE_POST_NEWS_POSTED_REQUEST_CODE);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if(v.getId() == R.id.buy_icon_imageview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_added_item_price_background_constraintlayout){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_added_item_icon_background_constraintlayout){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_added_item_quantity_background_constraintlayout){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_reposted_addeditem_image_circleimageview){
            openBuySharesActivity(position, 0);
        } else if(v.getId() == R.id.news_repostmaker_pottname_textview){
            openPottActivity(position, 1);
        } else if(v.getId() == R.id.news_reposted_additem_price_textview){
            openBuySharesActivity(position, 0);
        } else if(v.getId() == R.id.news_reposted_additem_price_background_constraintlayout){
            openBuySharesActivity(position, 0);
        } else if(v.getId() == R.id.list_item_news_type_28_newsfeed_fragment_relativelayout_1){
            openBuySharesActivity(position, 2);
        } else if(v.getId() == R.id.load_more_news_at_bottom_retry_imageview){
            //NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()), -1);
        } else if(v.getId() == R.id.list_item_news_type_15_newsfeed_fragment_pott_pic_circleimageview){
            openPottActivity(position, 2);
        } else if(v.getId() == R.id.horizontal_s4s_buy_button){
            openBuySharesActivity(position, 3);
        } else if(v.getId() == R.id.list_item_news_type_26_newsfeed_fragment_item_pic_circleimageview){
            openPottActivity(position, 3);
        } else if(v.getId() == R.id.list_item_news_type_26_newsfeed_fragment_pottname_textView){
            openPottActivity(position, 3);
        } else if(v.getId() == R.id.list_item_news_type_26_newsfeed_fragment_fullname_textView){
            openPottActivity(position, 3);
        } else if(v.getId() == R.id.linkup_button){
            sendLinkUp(getApplicationContext(), LocaleHelper.getLanguage(FullNewsActivity.this), FullNewsListDataGenerator.getAllData().get(0).getNewsMakerId());
        }

    }

    public void openNewsStatsActivity(int position, int type){
        // 0 = Likes, 1 = Dislikes, 2 = Views, 3 = Purchases, 4= Comments
        if(type == 0){
            Config.openActivity2(FullNewsActivity.this, NewsStatsActivity.class, 1, 0, 1, "id", FullNewsListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "0");
        } else if(type == 1){
            Config.openActivity2(FullNewsActivity.this, NewsStatsActivity.class, 1, 0, 1, "id", FullNewsListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "1");
        } else if(type == 2){
            Config.openActivity2(FullNewsActivity.this, NewsStatsActivity.class, 1, 0, 1, "id", FullNewsListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "2");
        } else if(type == 3){
            Config.openActivity2(FullNewsActivity.this, NewsStatsActivity.class, 1, 0, 1, "id", FullNewsListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "3");
        } else if(type == 4){
            Config.openActivity2(FullNewsActivity.this, NewsStatsActivity.class, 1, 0, 1, "id", FullNewsListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "4");
        }
    }

    public void openBuySharesActivity(int position, int newsType){
        // 0 = Reposted, 1 = Normal Vertical News, 2 = Stories Shares, 3 = Horizontal Shares
        if(newsType == 0){
            String[] buyData = {
                    FullNewsListDataGenerator.getAllData().get(position).getRepostedItemId(),
                    FullNewsListDataGenerator.getAllData().get(position).getRepostedItemParentID(),
                    FullNewsListDataGenerator.getAllData().get(position).getRepostedItemName(),
                    FullNewsListDataGenerator.getAllData().get(position).getRepostedItemQuantity(),
                    FullNewsListDataGenerator.getAllData().get(position).getRepostedIcon()
            };
            Config.openActivity4(FullNewsActivity.this, BuySharesForSaleActivity.class, 1, 0, 1, "BUY_INFO", buyData);
        } else if(newsType == 1) {
            String[] buyData = {
                    FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemId(),
                    FullNewsListDataGenerator.getAllData().get(position).getNewsItemParentID(),
                    FullNewsListDataGenerator.getAllData().get(position).getNewsRealItemName(),
                    FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemQuantity(),
                    FullNewsListDataGenerator.getAllData().get(position).getNewsAddedItemIcon()
            };
            Config.openActivity4(FullNewsActivity.this, BuySharesForSaleActivity.class, 1, 0, 1, "BUY_INFO", buyData);
        }
    }

    public void addDotsIndicator(int totalImagesCount, Context context, int sliderPosition, LinearLayout mDotlayout, TextView[] mDots){

        mDotlayout.removeAllViews();
        mDots = new TextView[totalImagesCount];
        for (int i = 0; i < mDots.length; i++ ){
            mDots[i] = new TextView (context);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.newsBackgroundBlack));
            mDotlayout.addView(mDots[i]);
        }


        if(mDots.length > 0){
            mDots[sliderPosition].setTextColor(getResources().getColor(R.color.colorAccent));
        }

    }

    public void sendLinkUp(Context context, final String language, final String linkupId){
        AndroidNetworking.post(Config.LINK_ADD_LINKUPS)
                .addBodyParameter("log_id_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                .addBodyParameter("linkup_id", linkupId)
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("newsfeed_fragment_sendlinkup")
                .setPriority(Priority.MEDIUM)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {}
            @Override
            public void onError(ANError anError) {}
        });
    }

    public class m1NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m1NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }

    public class m2NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m2NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }

    public class m3NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m3NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }

    public class m4NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m4NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }


    public class m5NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m5NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }

    public class m6NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m6NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }


    public class m7NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m7NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }


    public class m8NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m8NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }

        @Override
        public int getCount() {
            return 8;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }


    public class m9NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m9NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }


        @Override
        public int getCount() {
            return 9;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }


    public class m10NewsImagesViewPagerSliderAdapter extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;
        String[] allImages;

        public m10NewsImagesViewPagerSliderAdapter(Context context, String[] images){
            this.context = context;
            this.allImages = images;
        }


        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (ConstraintLayout) o;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view;

            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.slider_layout_news_images, container, false);

            final ImageView sliderNewsImage = (ImageView) view.findViewById(R.id.news_image_imageview);
            final ImageView sliderErrorImage = (ImageView) view.findViewById(R.id.reload_news_image_imageview);
            final ProgressBar sliderProgressBar = (ProgressBar) view.findViewById(R.id.image_loading_news_image_progressbar);

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(FullNewsActivity.this, true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                        }
                    });
                    imageLoaderThread.start();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((ConstraintLayout) object);
        }
    }

    public PagerAdapter getImagesViewPagerAdapter(int position, Context context){
        if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 1){
            return new m1NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 2) {
            return new m2NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 3) {
            return new m3NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 4) {
            return new m4NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 5) {
            return new m5NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 6) {
            return new m6NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 7) {
            return new m7NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 8) {
            return new m8NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 9) {
            return new m9NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 10) {
            return new m10NewsImagesViewPagerSliderAdapter(context, FullNewsListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else {
            return null;
        }
    }
}