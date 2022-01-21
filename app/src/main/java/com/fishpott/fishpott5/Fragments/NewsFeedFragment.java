package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.BuySharesForSaleActivity;
import com.fishpott.fishpott5.Activities.CreatePostActivity;
import com.fishpott.fishpott5.Activities.FullNewsActivity;
import com.fishpott.fishpott5.Activities.NewsStatsActivity;
import com.fishpott.fishpott5.Activities.ProfileOfDifferentPottActivity;
import com.fishpott.fishpott5.Activities.StockProfileActivity;
import com.fishpott.fishpott5.Activities.WebViewActivity;
import com.fishpott.fishpott5.Adapters.News_Type_28_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.Vertical_News_Type_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.NewsType28_Stories_ListDataGenerator;
import com.fishpott.fishpott5.ListDataGenerators.NewsType_15_Sharesforsale_horizontal_ListDataGenerator;
import com.fishpott.fishpott5.ListDataGenerators.NewsType_26_LinkupsHorizontal_ListDataGenerator;
import com.fishpott.fishpott5.ListDataGenerators.Vertical_NewsType_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.NewsType_28_Story_Model;
import com.fishpott.fishpott5.Models.Vertical_NewsType_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;
import com.fishpott.fishpott5.Util.WrapContentLinearLayoutManager;
import com.fishpott.fishpott5.Views.CircleImageView;
import com.fishpott.fishpott5.Views.ExoPlayerRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedFragment extends Fragment implements View.OnClickListener {

    public static SwipeRefreshLayout mMainSwipeRefreshLayout;
    private OnFragmentInteractionListener mListener;
    private Boolean REQUEST_HAS_STARTED = false;
    public static ExoPlayerRecyclerView mNewsRecyclerView;
    private RecyclerView.OnScrollListener mainNewsScrollListener;
    public static LinearLayoutManager mMainNewsLayoutManager;
    private ConstraintLayout mPostButtonHolderConstraintlayout;
    private Thread newsFetchThread = null, imageLoaderThread = null;
    public static List<Long> allShownNewsDbRowID = new ArrayList<>();
    private View view;
    private static Boolean FETCH_STARTED = false;

    public NewsFeedFragment() {}

    public static NewsFeedFragment newInstance() {
        NewsFeedFragment fragment = new NewsFeedFragment();
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        mNewsRecyclerView = view.findViewById(R.id.activity_main_newsfeed_fragment_recyclerview);
        mPostButtonHolderConstraintlayout = view.findViewById(R.id.create_post_fab_contraintlayout);
        mMainSwipeRefreshLayout = view.findViewById(R.id.activity_main_newsfeed_fragment_swiperefreshayout);

        mNewsRecyclerView.setItemViewCacheSize(20);
        mNewsRecyclerView.setDrawingCacheEnabled(true);
        mNewsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mMainNewsLayoutManager = new LinearLayoutManager(getActivity());
        mPostButtonHolderConstraintlayout.setOnClickListener(this);

        mainNewsScrollListener = new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!REQUEST_HAS_STARTED && mMainNewsLayoutManager.getItemCount() <= (mMainNewsLayoutManager.findLastVisibleItemPosition() + 5) && Vertical_NewsType_ListDataGenerator.getAllData().size() > 2) {
                    // End has been reached
                    Log.e("NewsFeedFetchLocal", "1- End has been reached. NEWS CONTENT BEING FETCHED");
                    //fetchAllNewsToTheirListDataGenerators();
                }



                if (!recyclerView.canScrollVertically(-1)) {
                    //onScrolledToTop();

                } else if (!recyclerView.canScrollVertically(1)) {
                    //onScrolledToBottom();
                    Log.e("NewsFeedFetchLocal", "=-=-=-=-=-=-=-=-onScrolledToBottom-=-=-=-=-=-=-=-=");
                    if(Vertical_NewsType_ListDataGenerator.getAllData() != null && Vertical_NewsType_ListDataGenerator.getAllData().size() > 1){
                        final Vertical_News_Type_DatabaseAdapter vertical_news_type_databaseAdapter1 = new Vertical_News_Type_DatabaseAdapter(getActivity().getApplicationContext());

                        vertical_news_type_databaseAdapter1.openDatabase();
                        Cursor cursor2 = vertical_news_type_databaseAdapter1.getLastRow();
                        int cursorCount = cursor2.getCount();
                        cursor2.close();
                        vertical_news_type_databaseAdapter1.closeNews_Type_1_Database();

                        Log.e("NewsFeedFetchLocal", "cursorCount : " + String.valueOf(cursorCount));
                        if(cursorCount <= 0){
                            Log.e("NewsFeedFetchLocal", "NEWS CONTENT BEING FETCHED ONLINE - 1");
                            NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()), -1);
                        } else {

                            Log.e("NewsFeedFetchLocal", "NEWS CONTENT BEING FETCHED LOCALLY - 1");
                            //fetchVerticalNews(getActivity().getApplicationContext(), false);

                            if(Vertical_NewsType_ListDataGenerator.getAllData().size()-3 < cursorCount){
                                Log.e("NewsFeedFetchLocal", "NEWS CONTENT BEING FETCHED LOCALLY - 1");
                                fetchVerticalNews(getActivity().getApplicationContext(), false);
                            } else {
                                Log.e("NewsFeedFetchLocal", "NEWS CONTENT FETCH FAILED HERE - 1");
                                Log.e("NewsFeedFetchLocal", "LAST SKU : " + String.valueOf(Vertical_NewsType_ListDataGenerator.getAllData().get(0).getNewsSku()));
                                Log.e("NewsFeedFetchLocal", "LAST SKU-2 : " + String.valueOf(Vertical_NewsType_ListDataGenerator.getAllData().get(Vertical_NewsType_ListDataGenerator.getAllData().size()-2).getNewsSku()));
                                NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()), (int) Vertical_NewsType_ListDataGenerator.getAllData().get(Vertical_NewsType_ListDataGenerator.getAllData().size()-2).getNewsSku());
                            }
                        }
                    } else {
                        Log.e("NewsFeedFetchLocal", "NEWS CONTENT BEING FETCHED ONLINE - 2");
                        NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()), 0);
                    }

                } else if (dy < 0) {
                    //onScrolledUp();
                } else if (dy > 0) {
                    //onScrolledDown();
                }
            }
        };


        //mNewsRecyclerView.setLayoutManager(mMainNewsLayoutManager);
        mNewsRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mNewsRecyclerView.addOnScrollListener(mainNewsScrollListener);
        mNewsRecyclerView.setVideoInfoList(Vertical_NewsType_ListDataGenerator.getAllData());
        //mNewsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter();
        //recyclerViewAdapter.setHasStableIds(true);
        mNewsRecyclerView.setAdapter(recyclerViewAdapter);

        newsFetchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("NewsFeedFetchLocal", "NEWS CONTENT BEING FETCHED LOCALLY - 2");
                fetchVerticalNews(getActivity().getApplicationContext(), true);
                fetchAllNewsToTheirListDataGenerators();
            }
        });
        newsFetchThread.start();

        mMainSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        newsFetchThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(Connectivity.isConnected(getActivity())){
                                    Log.e("NewsFeedFetchLocal", "NEWS CONTENT BEING FETCHED ONLINE FROM SWIPE - 3");
                                    NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()), 0);
                                } else {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMainSwipeRefreshLayout.setRefreshing(false);
                                            Config.showToastType1(getActivity(), getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                        }
                                    });
                                }
                            }
                        });
                        newsFetchThread.start();
                    }
                }
        );

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.create_post_fab_contraintlayout){
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            getActivity().startActivityForResult(intent, Config.CREATE_POST_NEWS_POSTED_REQUEST_CODE);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            //Config.openActivity(getActivity(), CreatePostActivity.class, 1, 0, 0, "", "");
        }

    }



    public void fetchAllNewsToTheirListDataGenerators(){
        //FETCHING NEWS TYPE 1
        newsFetchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //FETCHING THE STORIES IS THE LAST FETCH BECAUSE IT UPDATES THE RECYCLERVIEW ADAPTER TO NEW DATA AFTER ALL FETCHES ARE DONE
                fetchNewsType28StoriesToListDataGenerator();
                NewsFetcherAndPreparerService.fetchSharesForSaleHorizontalNewsAndPrepareMedia(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                NewsFetcherAndPreparerService.fetchLinkupsHorizontalNewsAndPrepareMedia(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
            }
        });
        newsFetchThread.start();
    }

    public static void fetchVerticalNews(Context context, Boolean START_FRESH) {
        Log.e("NewsFeedFetchLocal", "------- ------- ------- ------- START ------- ------- ------- ------- ");
        if(!FETCH_STARTED){
            FETCH_STARTED = true;
            int storiesAdded = 0;
            long lastNewsShown = 0;
            long lastNewsSku = 0;
            Boolean FETCH_ONLINE = false;

            if (START_FRESH) {
                if (Vertical_NewsType_ListDataGenerator.getAllData().size() > 1) {
                    Vertical_NewsType_ListDataGenerator.getAllData().clear();
                    mNewsRecyclerView.getAdapter().notifyDataSetChanged();
                }

                Log.e("NewsFeedFetchLocal", "HERE 1 ");
            } else {
                if (Vertical_NewsType_ListDataGenerator.getAllData().size() > 1) {
                    Vertical_NewsType_ListDataGenerator.getAllData().remove(Vertical_NewsType_ListDataGenerator.getAllData().size() - 1);
                    mNewsRecyclerView.getAdapter().notifyItemRemoved(Vertical_NewsType_ListDataGenerator.getAllData().size() - 1);
                    mNewsRecyclerView.getAdapter().notifyItemRangeChanged(Vertical_NewsType_ListDataGenerator.getAllData().size() - 1, Vertical_NewsType_ListDataGenerator.getAllData().size());
                    lastNewsShown = Vertical_NewsType_ListDataGenerator.getAllData().get(Vertical_NewsType_ListDataGenerator.getAllData().size() - 2).getRowId();
                    lastNewsShown = lastNewsShown + 1;
                    lastNewsSku = Vertical_NewsType_ListDataGenerator.getAllData().get(Vertical_NewsType_ListDataGenerator.getAllData().size() - 2).getNewsSku();
                    Log.e("NewsFeedFetchLocal", "HERE 2");
                }
                Log.e("NewsFeedFetchLocal", "HERE 3");
            }

            // GETTING THE LOCAL ROW ID OF THE LAST NEWS SHOWN
            Log.e("NewsFeedFetchLocal", "lastNewsShown : " + String.valueOf(lastNewsShown));
            Log.e("NewsFeedFetchLocal", "lastNewsSku : " + String.valueOf(lastNewsSku));


            //CREATING THE NEWS STORIES DATABASE OBJECT
            Vertical_News_Type_DatabaseAdapter verticalNewsTypeDatabaseAdapter = new Vertical_News_Type_DatabaseAdapter(context);
            // OPENING THE STORIES DATABASE
            verticalNewsTypeDatabaseAdapter.openDatabase();
            //GETTING ALL NEWS IN LOCAL DATABASE AND POINTING ONE-BY-ONE WITH CURSOR
            Cursor cursor = verticalNewsTypeDatabaseAdapter.getAllRows();
            Vertical_NewsType_Model verticalNewsType_model = new Vertical_NewsType_Model();

            if (Vertical_NewsType_ListDataGenerator.getAllData().size() == 0) {
                verticalNewsType_model.setNewsType(Config.HORIZONTAL_NEWS_TYPE_28_KEY);
                Vertical_NewsType_ListDataGenerator.addOneData(verticalNewsType_model);
                //if(START_FRESH){
                //    NewsType28_Stories_ListDataGenerator.getAllStoriesData().clear();
                //}
                Log.e("NewsFeedFetchLocal", "HERE 4");
            }


            if (!cursor.moveToFirst()) {
                FETCH_ONLINE = true;
                cursor.close();
                verticalNewsTypeDatabaseAdapter.closeNews_Type_1_Database();
                Log.e("NewsFeedFetchLocal", "HERE 5");
            } else {
                Log.e("NewsFeedFetchLocal", "HERE 6");
            }


            if (!FETCH_ONLINE) {
                Log.e("NewsFeedFetchLocal", "HERE 9");
                do {
                    if (lastNewsShown != 0 && cursor.getLong(verticalNewsTypeDatabaseAdapter.COL_ROWID) < lastNewsShown) {
                        cursor.moveToNext();
                    }
                    // ADD A SECTION FOR HORIZONTAL SHARES FOR SALE
                    if (storiesAdded == 10) {
                        verticalNewsType_model = new Vertical_NewsType_Model();
                        verticalNewsType_model.setNewsType(Config.HORIZONTAL_NEWS_TYPE_15_KEY);
                        Vertical_NewsType_ListDataGenerator.addOneData(verticalNewsType_model);
                        NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().clear();
                        Log.e("NewsFeedFetchLocal", "HERE 10");
                        storiesAdded++;
                        cursor.moveToPrevious();
                    }

                    // ADD A SECTION FOR HORIZONTAL LINKUPS
                    if (storiesAdded == 15) {
                        verticalNewsType_model = new Vertical_NewsType_Model();
                        verticalNewsType_model.setNewsType(Config.HORIZONTAL_NEWS_TYPE_26_KEY);
                        Vertical_NewsType_ListDataGenerator.addOneData(verticalNewsType_model);
                        NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().clear();
                        Log.e("NewsFeedFetchLocal", "HERE 11");
                        storiesAdded++;
                        cursor.moveToPrevious();
                    }

                    //Log.e("NewsFeedFetchLocalCur", "cursor count : " + String.valueOf(cursor.getCount()));
                    //Log.e("NewsFeedFetchLocalCur", "cursor getColumnCount : " + String.valueOf(cursor.getColumnCount()));
                    //Log.e("NewsFeedFetchLocalCur", "cursor-getInt : " + String.valueOf(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_ROWID)));
                    //Log.e("NewsFeedFetchLocalCur", "LONG-VALUE-OF-cursor-getInt : " + String.valueOf(Long.valueOf(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_ROWID))));


                    if (!cursor.isLast()) {
                        Log.e("NewsFeedFetchLocalCur", "cursor count : " + String.valueOf(cursor.getCount()));
                        if (allShownNewsDbRowID.contains(cursor.getLong(verticalNewsTypeDatabaseAdapter.COL_ROWID)-1)) {
                            cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_ROWID);
                            //cursor.close();
                            //verticalNewsTypeDatabaseAdapter.closeNews_Type_1_Database();
                            //return;
                            cursor.moveToNext();
                            continue;
                        }
                    }


                    //Log.e("NewsFeedFetchLocal", "HERE 13");

                    verticalNewsType_model = new Vertical_NewsType_Model();
                    verticalNewsType_model.setRowId(cursor.getLong(verticalNewsTypeDatabaseAdapter.COL_ROWID));
                    verticalNewsType_model.setNewsSku(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_NEWS_SKU));
                    verticalNewsType_model.setNewsType(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_TYPE));
                    verticalNewsType_model.setNewsId(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ID));
                    verticalNewsType_model.setNewsText(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_TEXT));
                    verticalNewsType_model.setNewsTime(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_TIME));
                    verticalNewsType_model.setNewsLikes(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_LIKES));
                    verticalNewsType_model.setNewsDislikes(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_DISLIKES));
                    verticalNewsType_model.setNewsComments(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_COMMENTS));
                    verticalNewsType_model.setNewsViews(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_VIEWS));
                    verticalNewsType_model.setNewsReposts(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOSTS));
                    verticalNewsType_model.setNewsTransactions(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_TRANSACTIONS));
                    verticalNewsType_model.setNewsMakerId(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_MAKER_ID));
                    verticalNewsType_model.setNewsMakerPottName(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_MAKER_POTTNAME));
                    verticalNewsType_model.setNewsMakerFullName(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_MAKER_FULLNAME));
                    verticalNewsType_model.setNewsMakerPottPic(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_MAKER_POTTPIC));
                    verticalNewsType_model.setNewViewerReactionStatus(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_MAKER_REACTIONS_STATUS));
                    verticalNewsType_model.setNewsAddedItemId(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADDED_ITEM_ID));
                    verticalNewsType_model.setNewsItemParentID(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ITEM_PARENT_ID));
                    verticalNewsType_model.setNewsRealItemName(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REAL_ITEM_NAME));
                    verticalNewsType_model.setNewsAddedItemPrice(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADDED_ITEM_PRICE));
                    verticalNewsType_model.setNewsAddedItemIcon(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADDED_ITEM_ICON));
                    verticalNewsType_model.setNewsAddedItemQuantity(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADDED_ITEM_QUANTITY));
                    verticalNewsType_model.setNewsMakerAccountType(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_MAKER_ACCOUNT_TYPE));
                    verticalNewsType_model.setNewsMakerAccountVerifiedStatus(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_MAKER_VERIFIED_STATUS));
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_IMAGES_LINKS_SEPARATED_BY_SPACES));
                    verticalNewsType_model.setNewsImagesCount(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_IMAGES_COUNT));
                    verticalNewsType_model.setNewsTextReadMoreToggle(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_TEXT_READ_MORE));
                    verticalNewsType_model.setNewsAddedItemType(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADDED_ITEM_TYPE));
                    verticalNewsType_model.setNewsAddedItemStatus(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADDED_ITEM_STATUS));
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_VIDEOS_LINKS_SEPARATED_BY_SPACES));
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_VIDEOS_COVERARTS_LINKS_SEPARATED_BY_SPACES));
                    verticalNewsType_model.setNewsVideosCount(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_VIDEOS_COUNT));
                    verticalNewsType_model.setNewsUrl(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_URL));
                    verticalNewsType_model.setNewsUrlTitle(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_URL_TITLE));
                    verticalNewsType_model.setNewsItemName(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ITEM_NAME));
                    verticalNewsType_model.setNewsItemLocation(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ITEM_LOCATION));
                    verticalNewsType_model.setNewsItemVerifiedStatus(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_ITEM_VERIFIED_STATUS));
                    verticalNewsType_model.setAdvertItemIcon(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADVERT_ICON));
                    verticalNewsType_model.setAdvertTextTitle(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADVERT_TEXT_TITLE));
                    verticalNewsType_model.setAdvertTextTitle2(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADVERT_TEXT_TITLE2));
                    verticalNewsType_model.setAdvertButtonText(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADVERT_BUTTON_TEXT));
                    verticalNewsType_model.setAdvertLink(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_ADVERT_LINK));
                    verticalNewsType_model.setReposterPottName(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOST_MAKER_POTTNAME));
                    verticalNewsType_model.setRepostedText(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOST_TEXT));
                    verticalNewsType_model.setRepostedIcon(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOST_ICON));
                    verticalNewsType_model.setNewRepostedViewerReactionStatus(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOST_VIEWER_REACTION));
                    verticalNewsType_model.setRepostedItemPrice(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOST_ADDITEM_PRICE));
                    verticalNewsType_model.setNewsBackgroundColor(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_BACKGROUND_COLOR));
                    verticalNewsType_model.setNewsViewsRepostOrPurchasesShowStatus(cursor.getInt(verticalNewsTypeDatabaseAdapter.COL_NEWS_VIEWS_REPORT_OR_PUCHASES_SHOWN_STATUS));
                    verticalNewsType_model.setRepostedItemId(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOSTED_ADDED_ITEM_ID));
                    verticalNewsType_model.setRepostedNewsId(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOSTED_ID));
                    verticalNewsType_model.setRepostedItemParentID(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_REPOST_ITEM_PARENT_ID));
                    verticalNewsType_model.setRepostedItemQuantity(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_REPOST_ITEM_QUANTITY));
                    verticalNewsType_model.setRepostedItemName(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_REPOST_ITEM_NAME));

                    //Log.e("NewsFeedFetchLocal", "HERE 14");

                    //ADDING STORY OBJECT TO LIST
                    Vertical_NewsType_ListDataGenerator.addOneData(verticalNewsType_model);
                    allShownNewsDbRowID.add(cursor.getLong(verticalNewsTypeDatabaseAdapter.COL_ROWID));
                    storiesAdded++;

                    //Log.e("NewsFeedFetchLocal", "HERE 15");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mNewsRecyclerView.getAdapter().notifyItemInserted(Vertical_NewsType_ListDataGenerator.getAllData().size() - 1);
                        }
                    });

                    Log.e("NewsFeedFetchLocal", "LIST POSITION: " + String.valueOf(Vertical_NewsType_ListDataGenerator.getAllData().size() - 1) + " --  SKU : " + String.valueOf(Vertical_NewsType_ListDataGenerator.getAllData().get(Vertical_NewsType_ListDataGenerator.getAllData().size() - 1).getNewsSku()));
                    if (storiesAdded == 200) {
                        Log.e("NewsFeedFetchLocal", "HERE 16");
                        break;
                    }

                } while (cursor.moveToNext());

                cursor.close();
                verticalNewsTypeDatabaseAdapter.closeNews_Type_1_Database();
                Log.e("NewsFeedFetchLocal", "HERE 17");
            }

            Log.e("NewsFeedFetchLocal", "HERE 18");
            verticalNewsType_model = new Vertical_NewsType_Model();
            verticalNewsType_model.setNewsType(Config.NEWS_TYPE_30_NEWSLOADINGRETRY_VERTICAL_KEY);
            Vertical_NewsType_ListDataGenerator.addOneData(verticalNewsType_model);
            Log.e("NewsFeedFetchLocal", "HERE 19");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mMainSwipeRefreshLayout.isRefreshing()) {
                        mMainSwipeRefreshLayout.setRefreshing(false);
                        Log.e("NewsFeedFetchLocal", "HERE 20");
                    }
                    mNewsRecyclerView.getAdapter().notifyItemInserted(Vertical_NewsType_ListDataGenerator.getAllData().size());
                    mNewsRecyclerView.getAdapter().notifyItemRangeChanged(Vertical_NewsType_ListDataGenerator.getAllData().size() - 1, 1);
                    mNewsRecyclerView.setVideoInfoList(Vertical_NewsType_ListDataGenerator.getAllData());
                    Log.e("NewsFeedFetchLocal", "HERE 21");
                }
            });

            Log.e("NewsFeedFetchLocal", "storiesAdded : " + String.valueOf(storiesAdded));
            if (FETCH_ONLINE || storiesAdded <= 3) {
                cursor.close();
                verticalNewsTypeDatabaseAdapter.closeNews_Type_1_Database();
                NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(context, LocaleHelper.getLanguage(context), (int) lastNewsSku);
                Log.e("NewsFeedFetchLocal", "HERE 22");
            }

            Log.e("NewsFeedFetchLocal", "------- ------- ------- ------- END ------- ------- ------- ------- ");

            FETCH_STARTED = false;
        }
    }

    public void fetchNewsType28StoriesToListDataGenerator(){
        // populate the message from the cursor
        if(getActivity() != null){
            int storiesAdded = 0;
            //CREATING THE NEWS STORIES DATABASE OBJECT
            News_Type_28_DatabaseAdapter newsType28DatabaseAdapter = new News_Type_28_DatabaseAdapter(getActivity().getApplication());
            // OPENING THE STORIES DATABASE
            newsType28DatabaseAdapter.openDatabase();
            //GETTING ALL NEWS IN LOCAL DATABASE AND POINTING ONE-BY-ONE WITH CURSOR
            Cursor cursor = newsType28DatabaseAdapter.getAllRows();
            if (cursor.moveToLast()) {
                do {
                    // Process the data:
                    int id = cursor.getInt(newsType28DatabaseAdapter.COL_ROWID);
                    int storyType = cursor.getInt(newsType28DatabaseAdapter.COL_NEWS_STORY_TYPE);
                    String storyNewsId = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_ID);
                    String storyNewsMakerId = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_MAKER_ID);
                    String storyNewsMakerPottName = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_MAKER_POTTNAME);
                    String storyNewsMakerProfilePicture = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_MAKER_PROFILE_PICTURE);
                    String storyNewsImage = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_IMAGE);
                    String storyNewsVideo = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_VIDEO);
                    String storyNewsItemPrice = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_ITEM_PRICE);
                    String storyNewsItemParentID = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_ITEM_PARENT_ID);
                    String storyNewsItemName = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_ITEM_NAME);
                    String storyNewsItemQuantity = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_ITEM_QUANTITY);
                    String storyNewsItemShareID = cursor.getString(newsType28DatabaseAdapter.COL_NEWS_STORY_ITEM_SHARES_ID);

                    NewsType_28_Story_Model newsType28_Story_Model = new NewsType_28_Story_Model();
                    newsType28_Story_Model.setStoryType(storyType);
                    newsType28_Story_Model.setStoryNewsId(storyNewsId);
                    newsType28_Story_Model.setStoryMakerPottId(storyNewsMakerId);
                    newsType28_Story_Model.setStoryMakerPottName(storyNewsMakerPottName);
                    newsType28_Story_Model.setStoryMakerPottPic(storyNewsMakerProfilePicture);
                    newsType28_Story_Model.setStoryItemPic(storyNewsImage);
                    newsType28_Story_Model.setStoryItemVideo(storyNewsVideo);
                    newsType28_Story_Model.setStoryItemPrice(storyNewsItemPrice);
                    newsType28_Story_Model.setStoryItemParentID(storyNewsItemParentID);
                    newsType28_Story_Model.setStoryItemName(storyNewsItemName);
                    newsType28_Story_Model.setStoryItemQuantity(storyNewsItemQuantity);
                    newsType28_Story_Model.setStoryItemShareID(storyNewsItemShareID);

                    //ADDING STORY OBJECT TO LIST
                    NewsType28_Stories_ListDataGenerator.addOneStoryData(newsType28_Story_Model);
                    storiesAdded++;
                    if(storiesAdded == 35){
                        break;
                    }

                } while(cursor.moveToPrevious());
            }

            cursor.close();
            newsType28DatabaseAdapter.closeNewsStoriesDatabase();

        }
    }


    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    public class RecyclerViewAdapter extends RecyclerView.Adapter {



        @Override
        public int getItemViewType(int position) {
            if(position >= Vertical_NewsType_ListDataGenerator.getAllData().size()){
                return Config.NEWS_TYPE_30_NEWSLOADINGRETRY_VERTICAL_KEY;
            } else {
                return Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType();
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v;
            if (viewType == Config.HORIZONTAL_NEWS_TYPE_28_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_activity_newsfeed_fragment_horizontal_news_layout, parent, false);
                vh = new Horizontal_NewsType_ViewHolder(v);
            } else if (viewType == Config.HORIZONTAL_NEWS_TYPE_15_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_activity_newsfeed_fragment_horizontal_news_layout, parent, false);
                vh = new Horizontal_NewsType_ViewHolder(v);
            } else if (viewType == Config.HORIZONTAL_NEWS_TYPE_26_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_activity_newsfeed_fragment_horizontal_news_layout, parent, false);
                vh = new Horizontal_NewsType_ViewHolder(v);
            }
             /* else if (viewType == Config.HORIZONTAL_NEWS_TYPE_11_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_activity_newsfeed_fragment_horizontal_news_layout, parent, false);
                 vh = new Horizontal_NewsType_ViewHolder(v);
             }*/
            else if (viewType == Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_1_justnewswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_1_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_2_justnewswithtext_newsfeed_fragment, parent, false);
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
            }
             /*
              else if (viewType == Config.NEWS_TYPE_10_UPFORSALENEWS_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_10_upforsale_newsfeed_fragment, parent, false);
                 vh = new NewsType_10_ViewHolder(v);
             }

             else if (viewType == Config.NEWS_TYPE_12_EVENTNEWS_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_12_event_newsfeed_fragment, parent, false);
                 vh = new NewsType_12_ViewHolder(v);
             }

             else if (viewType == Config.NEWS_TYPE_16_FUNDRAISERNEWS_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_16_fundraiser_newsfeed_fragment, parent, false);
                 vh = new NewsType_16_ViewHolder(v);
             } else if (viewType == Config.NEWS_TYPE_1_SPONSOREDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_1_sponsoredjustnewswithtext_newsfeed_fragment, parent, false);
                 vh = new NewsType_1_Sponsored_ViewHolder(v);
             } else if (viewType == Config.NEWS_TYPE_2_SPONSOREDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_2_sponsoredjustnewswithtext_newsfeed_fragment, parent, false);
                 vh = new NewsType_2_Sponsored_ViewHolder(v);
             } else if (viewType == Config.NEWS_TYPE_3_TO_4_SPONSOREDJUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_3_to_4_sponsored_newswithimage_newsfeed_fragment, parent, false);
                 vh = new NewsType_3_to_4_Sponsored_ViewHolder(v);
             } else if (viewType == Config.NEWS_TYPE_5_TO_6_SPONSOREDJUSTNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_5_to_6_sponsorednewswithvideo_newsfeed_fragment, parent, false);
                 vh = new NewsType_5_to_6_Sponsored_ViewHolder(v);
             } else if (viewType == Config.NEWS_TYPE_10_REPOSTEDUPFORSALENEWS_VERTICAL_KEY) {
                 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_10_reposted_upforsale_newsfeed_fragment, parent, false);
                 vh = new NewsType_10_Reposted_ViewHolder(v);
             }
             */
            else if (viewType == Config.NEWS_TYPE_17_SHARES4SALEWITHVIDEO_VERTICAL_KEY) {
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
            } else if (viewType == Config.NEWS_TYPE_41_SHARESFORSALE_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_41_sharesforsale_newswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_41_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_42_SHARESFORSALE_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_41_sharesforsale_newswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_41_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_43_SHARESFORSALE_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_43_sharesforsale_newswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_43_ViewHolder(v);
            } else if (viewType == Config.NEWS_TYPE_44_SHARESFORSALE_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_41_sharesforsale_newswithtext_newsfeed_fragment, parent, false);
                vh = new NewsType_41_ViewHolder(v);
            }  else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_30_loading_retry_bottom_newsfeed_fragment, parent, false);
                vh = new News_Loading_More_At_Bottom_ViewHolder(v);
            }
            return vh;
        }


        // NEW TYPE 41 VIEW HOLDER
        public class NewsType_41_ViewHolder extends RecyclerView.ViewHolder  {
            private TextView mPottNameTextView, mViewPottTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsAddedAutoInfo, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mSharesNameTextView;
            private CircleImageView mPottPicCircleImageView;
            private ImageView mAddedItemImageView, mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private Button mBuyButton;

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


            public NewsType_41_ViewHolder(View v) {
                super(v);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.shares_logo_imageview);
                mSharesNameTextView = (TextView) itemView.findViewById(R.id.shares_name);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.shares_offering_company_name);
                mViewPottTextView = (TextView) itemView.findViewById(R.id.shares_offering_company_name_view_profile);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mNewsAddedAutoInfo = (TextView) itemView.findViewById(R.id.item_for_sale_info2_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyButton = (Button) itemView.findViewById(R.id.buy_button);

                mPottNameTextView.setOnClickListener(innerClickListener);
                mViewPottTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mSharesNameTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mBuyButton.setOnClickListener(innerClickListener);
            }
        }


        public class NewsType_43_ViewHolder extends RecyclerView.ViewHolder  {
            private TextView mPottNameTextView, mViewPottTextView, mNewsTextTextView, mAddedItemPriceTextView,
                    mAddedItemQuantityTextView, mNewsAddedAutoInfo, mNewsLikesTextView, mNewsDislikesTextView, mNewsCommentsTextView,
                    mNewsViewsTextView, mSharesNameTextView, mNewsRepostedPosterPottNameTextview, mNewsRepostedCommentTextview;
            private CircleImageView mPottPicCircleImageView;
            private ImageView mAddedItemImageView, mCommentIconImageView, mRepostIconImageView, mReloadAddedItemImageView;
            private CheckBox mLikeIconImageView, mDislikeIconImageView;
            private Button mBuyButton;

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

            public NewsType_43_ViewHolder(View v) {
                super(v);
                mNewsRepostedPosterPottNameTextview = (TextView) itemView.findViewById(R.id.news_repostmaker_pottname_textview);
                mNewsRepostedCommentTextview = (TextView) itemView.findViewById(R.id.news_reposted_comment_textview);
                mPottPicCircleImageView = (CircleImageView) itemView.findViewById(R.id.news_maker_pottpic_circleimageview);
                mAddedItemImageView = (ImageView) itemView.findViewById(R.id.shares_logo_imageview);
                mSharesNameTextView = (TextView) itemView.findViewById(R.id.shares_name);
                mPottNameTextView = (TextView) itemView.findViewById(R.id.shares_offering_company_name);
                mViewPottTextView = (TextView) itemView.findViewById(R.id.shares_offering_company_name_view_profile);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mAddedItemPriceTextView = (TextView) itemView.findViewById(R.id.item_for_sale_price_textview);
                mAddedItemQuantityTextView = (TextView) itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mNewsAddedAutoInfo = (TextView) itemView.findViewById(R.id.item_for_sale_info2_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsTextTextView = (TextView) itemView.findViewById(R.id.news_text_textview);
                mNewsLikesTextView = (TextView) itemView.findViewById(R.id.news_likes_textview);
                mNewsDislikesTextView = (TextView) itemView.findViewById(R.id.news_dislikes_textview);
                mNewsCommentsTextView = (TextView) itemView.findViewById(R.id.news_comments_textview);
                mNewsViewsTextView = (TextView) itemView.findViewById(R.id.news_views_textview);
                mLikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_like_icon_imageview);
                mDislikeIconImageView = (CheckBox) itemView.findViewById(R.id.news_dislike_icon_imageview);
                mCommentIconImageView = (ImageView) itemView.findViewById(R.id.news_comment_icon_imageview);
                mRepostIconImageView = (ImageView) itemView.findViewById(R.id.news_repost_icon_imageview);
                mBuyButton = (Button) itemView.findViewById(R.id.buy_button);

                mNewsRepostedPosterPottNameTextview.setOnClickListener(innerClickListener);
                mPottNameTextView.setOnClickListener(innerClickListener);
                mViewPottTextView.setOnClickListener(innerClickListener);
                mAddedItemPriceTextView.setOnClickListener(innerClickListener);
                mAddedItemQuantityTextView.setOnClickListener(innerClickListener);
                mNewsLikesTextView.setOnClickListener(innerClickListener);
                mNewsDislikesTextView.setOnClickListener(innerClickListener);
                mNewsCommentsTextView.setOnClickListener(innerClickListener);
                mNewsViewsTextView.setOnClickListener(innerClickListener);
                mPottPicCircleImageView.setOnClickListener(innerClickListener);
                mAddedItemImageView.setOnClickListener(innerClickListener);
                mSharesNameTextView.setOnClickListener(innerClickListener);
                mLikeIconImageView.setOnClickListener(innerClickListener2);
                mDislikeIconImageView.setOnClickListener(innerClickListener2);
                mCommentIconImageView.setOnClickListener(innerClickListener);
                mRepostIconImageView.setOnClickListener(innerClickListener);
                mBuyButton.setOnClickListener(innerClickListener);
            }
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                mNewsReadMoreTextView.setOnClickListener(innerClickListener);
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                    addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
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


                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                    addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                    addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                    addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
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


                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                    addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
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


                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                    addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                    addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(getAdapterPosition()).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length, getContext(), position, mNewsImagesCountDotLinearlayout, mNewsImagesCountDots);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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
                //mNewsTextTextView.setOnClickListener(innerClickListener);
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

        private void newsType28View(Horizontal_NewsType_ViewHolder holder) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerView.setAdapter(new NewsType28HorizontalRecyclerViewAdapter());
        }

        private void newsType15View(Horizontal_NewsType_ViewHolder holder) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerView.setAdapter(new NewsType15HorizontalRecyclerViewAdapter());
        }

        private void newsType26View(Horizontal_NewsType_ViewHolder holder) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerView.setAdapter(new NewsType26HorizontalRecyclerViewAdapter());
        }

        /*
        private void newsType11View(Horizontal_NewsType_ViewHolder holder) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerView.setAdapter(new NewsType11HorizontalRecyclerViewAdapter());
        }
        */



        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId() != null){
                if (!Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId().trim().equalsIgnoreCase("")){
                    Config.sendNewsViewed(getActivity().getApplicationContext(), Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId(), Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                    //Log.e("NewfeedNewsVIEWS", "VIEWS BEING SENT");
                }
            }
            if (holder.getItemViewType() == Config.HORIZONTAL_NEWS_TYPE_28_KEY){
                newsType28View((Horizontal_NewsType_ViewHolder) holder);

                //THIS IS WHAT HAPPENS WHEN A NEWS TYPE 15 IS BEING ADDED TO THE LIST
            } else if (holder.getItemViewType() == Config.HORIZONTAL_NEWS_TYPE_15_KEY){
                newsType15View((Horizontal_NewsType_ViewHolder) holder);

                //THIS IS WHAT HAPPENS WHEN A NEWS TYPE 26 IS BEING ADDED TO THE LIST
            } else if (holder.getItemViewType() == Config.HORIZONTAL_NEWS_TYPE_26_KEY){
                newsType26View((Horizontal_NewsType_ViewHolder) holder);

            }
            /* else if (holder.getItemViewType() == Config.HORIZONTAL_NEWS_TYPE_11_KEY){
                newsType11View((Horizontal_NewsType_ViewHolder) holder);

            }*/
            else if (holder instanceof NewsType_41_ViewHolder) {
                ((NewsType_41_ViewHolder) holder).mNewsAddedAutoInfo.setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsItemLocation());
                ((NewsType_41_ViewHolder) holder).mPottNameTextView.setText(getString(R.string.offered_by) + " @" + Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerPottName());
                setallTextsInNews(new TextView[] {null, null, null, ((NewsType_41_ViewHolder) holder).mNewsTextTextView, ((NewsType_41_ViewHolder) holder).mNewsLikesTextView, ((NewsType_41_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_41_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_41_ViewHolder) holder).mNewsViewsTextView, ((NewsType_41_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_41_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, null, null, null, null, null, ((NewsType_41_ViewHolder) holder).mSharesNameTextView}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_41_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_41_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_41_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().length() > 1) {
                            Config.loadUrlImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim(), ((NewsType_41_ViewHolder) holder).mAddedItemImageView, 0, 60, 60);
                        }
                    }
                });
                imageLoaderThread.start();

            } else if (holder instanceof NewsType_43_ViewHolder) {
                ((NewsType_43_ViewHolder) holder).mNewsAddedAutoInfo.setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsItemLocation());
                ((NewsType_43_ViewHolder) holder).mPottNameTextView.setText(getString(R.string.offered_by) + " @" + Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerPottName());
                setallTextsInNews(new TextView[] {null, null, null, ((NewsType_43_ViewHolder) holder).mNewsTextTextView, ((NewsType_43_ViewHolder) holder).mNewsLikesTextView, ((NewsType_43_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_43_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_43_ViewHolder) holder).mNewsViewsTextView, ((NewsType_43_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_43_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, null, null, ((NewsType_43_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_43_ViewHolder) holder).mNewsRepostedCommentTextview, null, ((NewsType_43_ViewHolder) holder).mSharesNameTextView}, position);
                setAllCircleImageViewsInNews(
                        new CircleImageView[]{((NewsType_43_ViewHolder) holder).mPottPicCircleImageView}
                        ,new ImageView[]{null}
                        ,new ProgressBar[]{null}
                        , position);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_43_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_43_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().length() > 1) {
                            Config.loadUrlImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim(), ((NewsType_43_ViewHolder) holder).mAddedItemImageView, 0, 60, 60);
                        }
                    }
                });
                imageLoaderThread.start();

            } else if (holder instanceof NewsType_1_ViewHolder) {
                setallTextsInNews(new TextView[] {((NewsType_1_ViewHolder) holder).mPottNameTextView, ((NewsType_1_ViewHolder) holder).mFullNameTextView, ((NewsType_1_ViewHolder) holder).mNewsTimeTextView, ((NewsType_1_ViewHolder) holder).mNewsTextTextView, ((NewsType_1_ViewHolder) holder).mNewsLikesTextView, ((NewsType_1_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_1_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_1_ViewHolder) holder).mNewsViewsTextView, ((NewsType_1_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_1_ViewHolder) holder).mAddedItemQuantityTextView}, position);
                //Config.linkifyAllMentions(((NewsType_1_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_1_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_1_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
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
                setallTextsInNews(new TextView[] {((NewsType_2_ViewHolder) holder).mPottNameTextView, ((NewsType_2_ViewHolder) holder).mFullNameTextView, ((NewsType_2_ViewHolder) holder).mNewsTimeTextView, ((NewsType_2_ViewHolder) holder).mNewsTextTextView, ((NewsType_2_ViewHolder) holder).mNewsLikesTextView, ((NewsType_2_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_2_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_2_ViewHolder) holder).mNewsViewsTextView, ((NewsType_2_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_2_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_2_ViewHolder) holder).mNewsReadMoreTextView}, position);
                //Config.linkifyAllMentions(((NewsType_2_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_2_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_2_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
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
                setallTextsInNews(new TextView[] {((NewsType_3_to_4_ViewHolder) holder).mPottNameTextView, ((NewsType_3_to_4_ViewHolder) holder).mFullNameTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsTimeTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsTextTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsLikesTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsViewsTextView, ((NewsType_3_to_4_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_3_to_4_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_3_to_4_ViewHolder) holder).mNewsReadMoreTextView}, position);
                //Config.linkifyAllMentions(((NewsType_3_to_4_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_3_to_4_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_3_to_4_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_3_to_4_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getContext()));
                addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getContext(), 0, ((NewsType_3_to_4_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_3_to_4_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_3_to_4_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_3_to_4_ViewHolder) holder).viewPagerListener);

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
                setallTextsInNews(new TextView[] {((NewsType_5_to_6_ViewHolder) holder).mPottNameTextView, ((NewsType_5_to_6_ViewHolder) holder).mFullNameTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsTimeTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsTextTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsLikesTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsViewsTextView, ((NewsType_5_to_6_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_5_to_6_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_5_to_6_ViewHolder) holder).mNewsReadMoreTextView}, position);
                //Config.linkifyAllMentions(((NewsType_5_to_6_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_5_to_6_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_5_to_6_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_5_to_6_ViewHolder) holder).parent.setTag(this);
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
                setallTextsInNews(new TextView[] {((NewsType_7_and_9_ViewHolder) holder).mPottNameTextView, ((NewsType_7_and_9_ViewHolder) holder).mFullNameTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsTimeTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsTextTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsLikesTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsViewsTextView, ((NewsType_7_and_9_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_7_and_9_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_7_and_9_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_7_and_9_ViewHolder) holder).mNewsWebsiteTitle}, position);
                //Config.linkifyAllMentions(((NewsType_7_and_9_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_7_and_9_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_7_and_9_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
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
                setallTextsInNews(new TextView[] {((NewsType_8_ViewHolder) holder).mPottNameTextView, ((NewsType_8_ViewHolder) holder).mFullNameTextView, ((NewsType_8_ViewHolder) holder).mNewsTimeTextView, ((NewsType_8_ViewHolder) holder).mNewsTextTextView, ((NewsType_8_ViewHolder) holder).mNewsLikesTextView, ((NewsType_8_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_8_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_8_ViewHolder) holder).mNewsViewsTextView, ((NewsType_8_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_8_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_8_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_8_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_8_ViewHolder) holder).mNewsWebsiteTitle}, position);
                //Config.linkifyAllMentions(((NewsType_8_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_8_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_8_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_8_ViewHolder) holder).parent.setTag(this);
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
                setallTextsInNews(new TextView[] {((NewsType_10_ViewHolder) holder).mPottNameTextView, ((NewsType_10_ViewHolder) holder).mFullNameTextView, ((NewsType_10_ViewHolder) holder).mNewsTimeTextView, ((NewsType_10_ViewHolder) holder).mNewsTextTextView, ((NewsType_10_ViewHolder) holder).mNewsLikesTextView, ((NewsType_10_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_10_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_10_ViewHolder) holder).mNewsViewsTextView, ((NewsType_10_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_10_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_10_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_10_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_10_ViewHolder) holder).mRepostsTextView, ((NewsType_10_ViewHolder) holder).mItemForSaleNameTextView, ((NewsType_10_ViewHolder) holder).mItemForSaleLocationTextView}, position);
                //Config.linkifyAllMentions(((NewsType_10_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_10_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_10_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_10_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getContext()));
                addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getContext(), 0, ((NewsType_10_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_10_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_10_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_10_ViewHolder) holder).viewPagerListener);
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
                setallTextsInNews(new TextView[] {((NewsType_14_ViewHolder) holder).mPottNameTextView, ((NewsType_14_ViewHolder) holder).mFullNameTextView, ((NewsType_14_ViewHolder) holder).mNewsTimeTextView, ((NewsType_14_ViewHolder) holder).mNewsTextTextView, ((NewsType_14_ViewHolder) holder).mNewsLikesTextView, ((NewsType_14_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_14_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_14_ViewHolder) holder).mNewsViewsTextView, ((NewsType_14_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_14_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_14_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_14_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_14_ViewHolder) holder).mRepostsTextView, ((NewsType_14_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_14_ViewHolder) holder).mItemForSaleInfo2TextView}, position);
                //Config.linkifyAllMentions(((NewsType_14_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_14_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_14_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_14_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getContext()));
                addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getContext(), 0, ((NewsType_14_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_14_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_14_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_14_ViewHolder) holder).viewPagerListener);

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
                        if(!Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().equalsIgnoreCase("")){
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().split("\\s+")[0], ((NewsType_14_ViewHolder) holder).mSharesLogoCircleImageView, 0, 50, 50, ((NewsType_14_ViewHolder) holder).mReloadSharesforsaleLogoImageView, ((NewsType_14_ViewHolder) holder).mLoadingSharesforsaleLogoImageView);
                        }
                    }
                });
            } else if (holder instanceof NewsType_17_ViewHolder) {
                setallTextsInNews(new TextView[] {((NewsType_17_ViewHolder) holder).mPottNameTextView, ((NewsType_17_ViewHolder) holder).mFullNameTextView, ((NewsType_17_ViewHolder) holder).mNewsTimeTextView, ((NewsType_17_ViewHolder) holder).mNewsTextTextView, ((NewsType_17_ViewHolder) holder).mNewsLikesTextView, ((NewsType_17_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_17_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_17_ViewHolder) holder).mNewsViewsTextView, ((NewsType_17_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_17_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_17_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_17_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_17_ViewHolder) holder).mRepostsTextView, ((NewsType_17_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_17_ViewHolder) holder).mItemForSaleInfo2TextView}, position);
                //Config.linkifyAllMentions(((NewsType_17_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_17_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_17_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_17_ViewHolder) holder).parent.setTag(this);
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
                setallTextsInNews(new TextView[] {((NewsType_1_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_1_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, ((NewsType_1_Sponsored_ViewHolder) holder).mAdvertInfo1TextView, ((NewsType_1_Sponsored_ViewHolder) holder).mAdvertInfo2TextView}, position);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_1_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_1_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_1_Sponsored_ViewHolder) holder).mAdvertActionButton.setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getAdvertButtonText());
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
                setallTextsInNews(new TextView[] {((NewsType_2_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_2_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, ((NewsType_2_Sponsored_ViewHolder) holder).mAdvertInfo1TextView, ((NewsType_2_Sponsored_ViewHolder) holder).mAdvertInfo2TextView}, position);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_2_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_2_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_2_Sponsored_ViewHolder) holder).mAdvertActionButton.setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getAdvertButtonText());
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
                setallTextsInNews(new TextView[] {((NewsType_3_to_4_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsReadMoreTextView}, position);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getContext()));
                addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getContext(), 0, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_3_to_4_Sponsored_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_3_to_4_Sponsored_ViewHolder) holder).viewPagerListener);

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
                setallTextsInNews(new TextView[] {((NewsType_5_to_6_Sponsored_ViewHolder) holder).mPottNameTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mFullNameTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsTimeTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsTextTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsLikesTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsViewsTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mNewsReadMoreTextView}, position);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_5_to_6_Sponsored_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                ((NewsType_5_to_6_Sponsored_ViewHolder) holder).parent.setTag(this);
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
                setallTextsInNews(new TextView[] {((NewsType_1_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_1_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_1_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_1_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_1_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, null, null, null, null, null, null, null, null, null, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_1_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_1_Reposted_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_1_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_1_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_1_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_1_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
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
                setallTextsInNews(new TextView[] {((NewsType_2_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_2_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_2_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_2_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_2_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, null, null, null, null, null, null, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_2_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_2_Reposted_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_2_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_2_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_2_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_2_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
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
                setallTextsInNews(new TextView[] {((NewsType_3_to_4_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, null, null, null, null, null, null, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsTextTextView);

                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_3_to_4_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                ((RecyclerViewAdapter.NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getContext()));
                addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getContext(), 0, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_3_to_4_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_3_to_4_Reposted_ViewHolder) holder).viewPagerListener);

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
                setallTextsInNews(new TextView[] {((NewsType_5_to_6_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, null, null, null, null, null, null, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_5_to_6_Reposted_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_5_to_6_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                ((NewsType_5_to_6_Reposted_ViewHolder) holder).parent.setTag(this);
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
                setallTextsInNews(new TextView[] {((NewsType_7_and_9_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsWebsiteTitle, null, null, null, null, null, null, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_7_and_9_Reposted_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_7_and_9_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

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
                setallTextsInNews(new TextView[] {((NewsType_8_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_8_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_8_Reposted_ViewHolder) holder).mAddedItemPriceTextView, ((NewsType_8_Reposted_ViewHolder) holder).mAddedItemQuantityTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsReadMoreTextView, ((NewsType_8_Reposted_ViewHolder) holder).mNewsWebsiteAddress, ((NewsType_8_Reposted_ViewHolder) holder).mNewsWebsiteTitle, null, null, null, null, null, null, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_8_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_8_Reposted_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_8_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_8_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_8_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_8_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );
                ((NewsType_8_Reposted_ViewHolder) holder).parent.setTag(this);
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
                setallTextsInNews(new TextView[] {((NewsType_10_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_10_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_10_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_10_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_10_Reposted_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_10_Reposted_ViewHolder) holder).mRepostsTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSaleNameTextView, ((NewsType_10_Reposted_ViewHolder) holder).mItemForSaleLocationTextView, null, null, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_10_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_10_Reposted_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_10_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_10_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_10_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_10_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

                ((RecyclerViewAdapter.NewsType_10_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getContext()));
                addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getContext(), 0, ((NewsType_10_Reposted_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_10_Reposted_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_10_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_10_Reposted_ViewHolder) holder).viewPagerListener);

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
                setallTextsInNews(new TextView[] {((NewsType_14_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_14_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_14_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_14_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_14_Reposted_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_14_Reposted_ViewHolder) holder).mRepostsTextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_14_Reposted_ViewHolder) holder).mItemForSaleInfo2TextView, null, null, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_14_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_14_Reposted_ViewHolder) holder).mNewsTextTextView);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_14_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_14_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );

                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_14_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_14_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

                ((RecyclerViewAdapter.NewsType_14_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.setAdapter(getImagesViewPagerAdapter(position, getContext()));
                addDotsIndicator(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length,getContext(), 0, ((NewsType_14_Reposted_ViewHolder) holder).mNewsImagesCountDotLinearlayout, ((NewsType_14_Reposted_ViewHolder) holder).mNewsImagesCountDots);
                ((NewsType_14_Reposted_ViewHolder) holder).mNewsImagesSliderViewPager.addOnPageChangeListener(((NewsType_14_Reposted_ViewHolder) holder).viewPagerListener);

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
                setallTextsInNews(new TextView[] {((NewsType_17_Reposted_ViewHolder) holder).mPottNameTextView, ((NewsType_17_Reposted_ViewHolder) holder).mFullNameTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsTimeTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsTextTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsLikesTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsDislikesTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsCommentsTextView, ((NewsType_17_Reposted_ViewHolder) holder).mNewsViewsTextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSalePriceTextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSaleQuantity, ((NewsType_17_Reposted_ViewHolder) holder).mNewsReadMoreTextView, null, null, ((NewsType_17_Reposted_ViewHolder) holder).mNewsBuyersTextview, ((NewsType_17_Reposted_ViewHolder) holder).mRepostsTextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSaleInfo1TextView, ((NewsType_17_Reposted_ViewHolder) holder).mItemForSaleInfo2TextView, null, null, ((NewsType_17_Reposted_ViewHolder) holder).mNewsRepostedPosterPottNameTextview, ((NewsType_17_Reposted_ViewHolder) holder).mNewsRepostedCommentTextview, ((NewsType_17_Reposted_ViewHolder) holder).mNewsRepostedAddedItemPriceTextview}, position);
                //Config.linkifyAllMentions(((NewsType_17_Reposted_ViewHolder) holder).mNewsTextTextView);
                ((NewsType_17_Reposted_ViewHolder) holder).parent.setTag(this);
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()
                        , ((NewsType_17_Reposted_ViewHolder) holder).mLikeIconImageView
                        , ((NewsType_17_Reposted_ViewHolder) holder).mDislikeIconImageView
                        , position
                );
                setLikedOrDisliked(
                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus()
                        , ((NewsType_17_Reposted_ViewHolder) holder).mRepostedLikeIconImageView
                        , ((NewsType_17_Reposted_ViewHolder) holder).mRepostedDislikeIconImageView
                        , position
                );

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

                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim().equalsIgnoreCase("")){
                    ((News_Posting_ViewHolder) holder).mNewsTextView.setVisibility(View.GONE);
                } else {
                    ((News_Posting_ViewHolder) holder).mNewsTextView.setVisibility(View.VISIBLE);
                    if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim().length() > 50){
                        ((News_Posting_ViewHolder) holder).mNewsTextView.setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim().substring(0,50) + "...");
                    } else {
                        ((News_Posting_ViewHolder) holder).mNewsTextView.setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim());
                    }
                }

                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsHasBeenPosted() == 2){
                    ((News_Posting_ViewHolder) holder).mPostingTextView.setText(getString(R.string.your_news_has_been_posted));
                    ((News_Posting_ViewHolder) holder).mNewsPostingProgressBar.setVisibility(View.GONE);
                    ((News_Posting_ViewHolder) holder).mRetryButton.setVisibility(View.GONE);

                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsHasBeenPosted() == 1){
                    ((News_Posting_ViewHolder) holder).mPostingTextView.setText(getString(R.string.posting));
                    ((News_Posting_ViewHolder) holder).mRetryButton.setVisibility(View.GONE);
                    ((News_Posting_ViewHolder) holder).mNewsPostingProgressBar.setVisibility(View.VISIBLE);
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsHasBeenPosted() == 0){
                    ((News_Posting_ViewHolder) holder).mPostingTextView.setText(R.string.your_post_failed_please_retry);
                    ((News_Posting_ViewHolder) holder).mNewsPostingProgressBar.setVisibility(View.GONE);
                    ((News_Posting_ViewHolder) holder).mRetryButton.setVisibility(View.VISIBLE);
                }
            } else if (holder instanceof News_Loading_More_At_Bottom_ViewHolder) {

            }
        }

        public class Horizontal_NewsType_ViewHolder extends RecyclerView.ViewHolder {

            RecyclerView recyclerView;

            Horizontal_NewsType_ViewHolder(View itemView) {
                super(itemView);
                recyclerView = itemView.findViewById(R.id.inner_recyclerView);
            }
        }

        @Override
        public int getItemCount() {
            return Vertical_NewsType_ListDataGenerator.getAllData().size();
        }

    }


    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    private class NewsType26HorizontalRecyclerViewAdapter extends RecyclerView.Adapter{

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_26_linkups_horizontal_newsfeed_fragment, parent, false);
            vh = new ViewHolder(v);

            return vh;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            private CircleImageView mLinkupPottPicCircularImageView;
            private ImageView mVerifiedTag;
            private TextView mPottName, mFullName, mLinkupInfo;
            private Button mLinkupButton;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public ViewHolder(View v) {
                super(v);
                mLinkupPottPicCircularImageView = (CircleImageView) itemView.findViewById(R.id.list_item_news_type_26_newsfeed_fragment_item_pic_circleimageview);
                mPottName = (TextView) itemView.findViewById(R.id.list_item_news_type_26_newsfeed_fragment_pottname_textView);
                mFullName = (TextView) itemView.findViewById(R.id.list_item_news_type_26_newsfeed_fragment_fullname_textView);
                mVerifiedTag = (ImageView) itemView.findViewById(R.id.list_item_news_type_26_newsfeed_fragment_verifiedicon_imageView);
                mLinkupInfo = (TextView) itemView.findViewById(R.id.list_item_news_type_26_newsfeed_fragment_linkupinfo_textView);
                mLinkupButton = (Button) itemView.findViewById(R.id.linkup_button);

                // ALL ON-CLICK LISTENERS
                mLinkupPottPicCircularImageView.setOnClickListener(innerClickListener);
                mPottName.setOnClickListener(innerClickListener);
                mFullName.setOnClickListener(innerClickListener);
                mLinkupButton.setOnClickListener(innerClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((ViewHolder) holder).mPottName.setText(NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().get(position).getLinkupPottName());
            ((ViewHolder) holder).mFullName.setText(NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().get(position).getLinkupFullName());
            ((ViewHolder) holder).mLinkupInfo.setText(NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().get(position).getLinkupInfo());
            // 1 = green, 2 = blue
            if(NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().get(position).getVerifiedStatus() == 1){
                ((ViewHolder) holder).mVerifiedTag.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).mVerifiedTag.setImageResource(R.drawable.list_item_suggestedlinkupsactivity_activity_verifiedicon_green_imageview);
            } else if(NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().get(position).getVerifiedStatus() == 2){
                ((ViewHolder) holder).mVerifiedTag.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).mVerifiedTag.setImageResource(R.drawable.list_item_suggestedlinkupsactivity_activity_verifiedicon_blue_imageview);
            } else {
                ((ViewHolder) holder).mVerifiedTag.setVisibility(View.INVISIBLE);
            }
            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImage(getActivity(), true, NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().get(position).getLinkupPottPic(), ((ViewHolder) holder).mLinkupPottPicCircularImageView, 0, 110, 110);
                }
            });
            imageLoaderThread.start();

        }

        @Override
        public int getItemCount() {
            return NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().size();
        }

    }

    /*
    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    private class NewsType11HorizontalRecyclerViewAdapter extends RecyclerView.Adapter{


        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_11_upforsale_horizontal_newsfeed_fragment, parent, false);
            vh = new ViewHolder(v);

            return vh;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView mItemImageCircularImageView;
            private CircleImageView mSellerPottPicCircularImageView;

            public ViewHolder(View v) {
                super(v);
                mItemImageCircularImageView = (ImageView) itemView.findViewById(R.id.list_item_news_type_11_newsfeed_fragment_item_pic_circleimageview);
                mSellerPottPicCircularImageView = (CircleImageView) itemView.findViewById(R.id.list_item_news_type_11_newsfeed_fragment_pott_pic_circleimageview);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImage(getActivity(), true, NewsType_11_Upforsale_Horizontal_ListDataGenerator.getAllData().get(position).getItemPic(), ((ViewHolder) holder).mItemImageCircularImageView, 0, 110, 110);
                }
            });
            imageLoaderThread.start();

            // LOADING A PROFILE PICTURE IF URL EXISTS
            if(NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getSellerPottPic().trim().length() > 1){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadUrlImage(getActivity(), true, NewsType_11_Upforsale_Horizontal_ListDataGenerator.getAllData().get(position).getSellerPottPic().trim(), ((ViewHolder) holder).mSellerPottPicCircularImageView, 0, 60, 60);
                    }
                });
                imageLoaderThread.start();
            } else {
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadErrorImageView(getActivity(), R.drawable.setprofilepicture_activity_imageholder_default_image, ((ViewHolder) holder).mSellerPottPicCircularImageView, 60, 60);
                    }
                });
                imageLoaderThread.start();
            }

        }

        @Override
        public int getItemCount() {
            return NewsType_11_Upforsale_Horizontal_ListDataGenerator.getAllData().size();
        }

    }
    */


    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    private class NewsType15HorizontalRecyclerViewAdapter extends RecyclerView.Adapter{


        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_15_shares4sale_horizontal_newsfeed_fragment, parent, false);
            vh = new ViewHolder(v);

            return vh;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            private CircleImageView mStockImageCircularImageView, mSellerPottPicCircularImageView;
            private TextView mPricePerStock, mStockQuantity, mStockInfo;
            private Button mBuyButton;
            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public ViewHolder(View v) {
                super(v);
                mStockImageCircularImageView = (CircleImageView) itemView.findViewById(R.id.list_item_news_type_15_newsfeed_fragment_item_pic_circleimageview);
                mSellerPottPicCircularImageView = (CircleImageView) itemView.findViewById(R.id.list_item_news_type_15_newsfeed_fragment_pott_pic_circleimageview);
                mPricePerStock = (TextView) itemView.findViewById(R.id.list_item_news_type_15_newsfeed_fragment_price_textView);
                mStockQuantity = (TextView) itemView.findViewById(R.id.list_item_news_type_15_newsfeed_fragment_quantity_textView);
                mStockInfo = (TextView) itemView.findViewById(R.id.list_item_news_type_15_newsfeed_fragment_stockinfo_textView);
                mBuyButton = (Button) itemView.findViewById(R.id.horizontal_s4s_buy_button);


                // ALL ON-CLICK LISTENERS
                mStockImageCircularImageView.setOnClickListener(innerClickListener);
                mSellerPottPicCircularImageView.setOnClickListener(innerClickListener);
                mBuyButton.setOnClickListener(innerClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((ViewHolder) holder).mPricePerStock.setText(NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getStockSellingPrice());
            ((ViewHolder) holder).mStockQuantity.setText(NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getStockQuantity());
            ((ViewHolder) holder).mStockInfo.setText(NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getStockInfo());
            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Config.loadUrlImage(getActivity(), true, NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getStockPic(), ((ViewHolder) holder).mStockImageCircularImageView, 0, 110, 110);
                }
            });
            imageLoaderThread.start();

            // LOADING A PROFILE PICTURE IF URL EXISTS
            if(NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getSellerPottPic().trim().length() > 1){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadUrlImage(getActivity(), true, NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getSellerPottPic().trim(), ((ViewHolder) holder).mSellerPottPicCircularImageView, 0, 60, 60);
                    }
                });
                imageLoaderThread.start();
            } else {
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadErrorImageView(getActivity(), R.drawable.setprofilepicture_activity_imageholder_default_image, ((ViewHolder) holder).mSellerPottPicCircularImageView, 60, 60);
                    }
                });
                imageLoaderThread.start();
            }

        }

        @Override
        public int getItemCount() {
            return NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().size();
        }

    }



    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    private class NewsType28HorizontalRecyclerViewAdapter extends RecyclerView.Adapter{

        //DEFINING A CLICK LISTENER FOR THE RECYCLERVIEW

        @Override
        public int getItemViewType(int position) {

            if (NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryType() == Config.NEWS_TYPE_28_SHARES4SALE_STORY_HORIZONTAL_KEY) {
                return Config.NEWS_TYPE_28_SHARES4SALE_STORY_HORIZONTAL_KEY;
            }
            /* else if (NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryType() == Config.NEWS_TYPE_28_UP4SALE_STORY_HORIZONTAL_KEY) {
                return Config.NEWS_TYPE_28_UP4SALE_STORY_HORIZONTAL_KEY;
            } else if (NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryType() == Config.NEWS_TYPE_28_EVENT_STORY_HORIZONTAL_KEY) {
                return Config.NEWS_TYPE_28_EVENT_STORY_HORIZONTAL_KEY;
            } else if (NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryType() == Config.NEWS_TYPE_28_FUNDRAISER_STORY_HORIZONTAL_KEY) {
                return Config.NEWS_TYPE_28_FUNDRAISER_STORY_HORIZONTAL_KEY;
            }*/
            else {
                return Config.NEWS_TYPE_28_JUSTNEWS_STORY_HORIZONTAL_KEY;
            }

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_28_shares4sale_newsfeed_fragment, parent, false);
            vh = new SharesYardEventFundraiserStoryViewHolder(v);

            /*
            if (viewType == Config.NEWS_TYPE_28_SHARES4SALE_STORY_HORIZONTAL_KEY) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_28_shares4sale_newsfeed_fragment, parent, false);
                vh = new SharesYardEventFundraiserStoryViewHolder(v);
            }
            else if(viewType == Config.NEWS_TYPE_28_UP4SALE_STORY_HORIZONTAL_KEY){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_28_up4sale_newsfeed_fragment, parent, false);
                vh = new SharesYardEventFundraiserStoryViewHolder(v);
            } else if(viewType == Config.NEWS_TYPE_28_EVENT_STORY_HORIZONTAL_KEY){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_28_event_newsfeed_fragment, parent, false);
                vh = new SharesYardEventFundraiserStoryViewHolder(v);
            } else if(viewType == Config.NEWS_TYPE_28_FUNDRAISER_STORY_HORIZONTAL_KEY){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_28_fundraiser_newsfeed_fragment, parent, false);
                vh = new SharesYardEventFundraiserStoryViewHolder(v);
            } else if(viewType == Config.NEWS_TYPE_28_JUSTNEWS_STORY_HORIZONTAL_KEY){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_28_justnews_newsfeed_fragment, parent, false);
                vh = new JustNewsStoryViewHolder(v);
            }
            else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_type_28_justnews_newsfeed_fragment, parent, false);
                vh = new JustNewsStoryViewHolder(v);
            }
             */

            return vh;
        }


        public class SharesYardEventFundraiserStoryViewHolder extends RecyclerView.ViewHolder {
            private ConstraintLayout mCircleConstraintLayout;
            private TextView mStoryPriceTextView;
            private CircleImageView mStoryImageCircularImageView, mStoryMakerPottPicCircularImageView;
            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public SharesYardEventFundraiserStoryViewHolder(View v) {
                super(v);
                mCircleConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.list_item_news_type_28_newsfeed_fragment_relativelayout_1);
                mStoryImageCircularImageView = (CircleImageView) itemView.findViewById(R.id.horizontal_news_reposted_addeditem_image_circleimageview);
                mStoryMakerPottPicCircularImageView = (CircleImageView) itemView.findViewById(R.id.list_item_news_type_28_newsfeed_fragment_pott_pic_circleimageview);
                mStoryPriceTextView = (TextView) itemView.findViewById(R.id.news_reposted_additem_price_textview);

                mCircleConstraintLayout.setOnClickListener(innerClickListener);
                mStoryMakerPottPicCircularImageView.setOnClickListener(innerClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((SharesYardEventFundraiserStoryViewHolder) holder).mStoryPriceTextView.setText(NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryItemPrice());
            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryMakerPottPic().trim().length() > 1){
                        Config.loadUrlImage(getActivity(), true, NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryMakerPottPic().trim(), ((SharesYardEventFundraiserStoryViewHolder) holder).mStoryMakerPottPicCircularImageView, 0, 60, 60);
                    } else {
                        Config.loadErrorImageView(getActivity(), R.drawable.setprofilepicture_activity_imageholder_default_image, ((SharesYardEventFundraiserStoryViewHolder) holder).mStoryMakerPottPicCircularImageView, 60, 60);
                    }
                    Config.loadUrlImage(getActivity(), true, NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryItemPic().trim(), ((SharesYardEventFundraiserStoryViewHolder) holder).mStoryImageCircularImageView, 0, 100, 100);
                }
            });
            imageLoaderThread.start();
        }



        @Override
        public int getItemCount() {
            if(NewsType28_Stories_ListDataGenerator.getAllStoriesData() == null || NewsType28_Stories_ListDataGenerator.getAllStoriesData().size()>0)
                return NewsType28_Stories_ListDataGenerator.getAllStoriesData().size();
            else
                return 0;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e("VIDEOPLAYER", "===============================================================================");
        Log.e("VIDEOPLAYER", "PLAY POSITION: " + String.valueOf(ExoPlayerRecyclerView.playPosition));
        Log.e("VIDEOPLAYER", "FIRST VISIBLE POSITION: " + String.valueOf(mMainNewsLayoutManager.findFirstVisibleItemPosition()));
        Log.e("VIDEOPLAYER", "LAST POSITION: " + String.valueOf(mMainNewsLayoutManager.findLastVisibleItemPosition()));
        Boolean PLAY_VIDEO =  false;

        if(
                (
                        (ExoPlayerRecyclerView.playPosition - mMainNewsLayoutManager.findLastVisibleItemPosition()) == 1
                                || (ExoPlayerRecyclerView.playPosition - mMainNewsLayoutManager.findLastVisibleItemPosition()) == 0
                                || (ExoPlayerRecyclerView.playPosition - mMainNewsLayoutManager.findLastVisibleItemPosition()) == -1
                )

                ){
            PLAY_VIDEO = true;
        }

        if(PLAY_VIDEO){
            if(
                    (
                            (ExoPlayerRecyclerView.playPosition - mMainNewsLayoutManager.findFirstVisibleItemPosition()) == 1
                                    || (ExoPlayerRecyclerView.playPosition - mMainNewsLayoutManager.findFirstVisibleItemPosition()) == 0
                                    || (ExoPlayerRecyclerView.playPosition - mMainNewsLayoutManager.findFirstVisibleItemPosition()) == -1
                    )

                    ){
                PLAY_VIDEO = true;
            } else {
                PLAY_VIDEO = false;
            }
        }

        if(PLAY_VIDEO){
            mNewsRecyclerView.getPlayer().setPlayWhenReady(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mNewsRecyclerView.getPlayer().setPlayWhenReady(false);
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        if(newsFetchThread != null){
            newsFetchThread.interrupt();
            newsFetchThread = null;
        }
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_newsfeed_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        mNewsRecyclerView.getPlayer().setPlayWhenReady(false);
    }

    public void sendBackInfo(String returningString) {
        if (mListener != null) {
            mListener.onFragmentInteraction(returningString);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String returningString);
    }

    private void setallTextsInNews(TextView[] allTextViews, int position){
        // 0 = pottname, 1 = fullname, 2 = newsTime, 3 = newsText
        // 4 = Likes, 5 = Dislikes, 6 = Comments, 7 = Views
        // 8 = AddedItemPrice, 9 = ItemQuantity, 10 = ReadMore
        // 11 = UrlAddress, 12 = UrlTitle, 13 = buyers/Transactions
        // 14 = Reposts, 15 = ItemName, 16 = ItemLocation
        // 17 = AdvertInfo1, 18 = AdvertInfo2
        // 19 = ReposterPottName , 20 = RepostComment, 21 = RepostItemPrice
        // 22 = AddedItemName,

        for (int i = 0; i < allTextViews.length; i++){

            if(i == 0 && allTextViews[0] != null){
                allTextViews[0].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerPottName());
            } else if(i == 1 && allTextViews[1] != null){
                allTextViews[1].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerFullName());
            } else if(i == 2 && allTextViews[2] != null){
                allTextViews[2].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsTime());
            } else if(i == 3 && allTextViews[3] != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                        && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim().length() > 500
                        ){
                    allTextViews[3].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim().substring(0,490) + "...");
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                        && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                        && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim().length() > 200){
                    allTextViews[3].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim().substring(0,190) + "...");
                } else {
                    allTextViews[3].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().trim());
                }
                if (
                                (
                                Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                                        && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemPrice().trim().equalsIgnoreCase("")
                                )
                                ||
                                (
                                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                                                && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemPrice().trim().equalsIgnoreCase("")
                                )

                                ||
                                (
                                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_1_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                                                && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")
                                )
                                ||
                                (
                                        Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() ==  Config.NEWS_TYPE_2_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                                                && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")
                                )
                ){
                            allTextViews[3].setTextColor(getResources().getColor(R.color.colorAccent));
                }
                Config.linkifyAllMentions(getActivity().getApplicationContext(), allTextViews[3]);

            } else if(i == 4 && allTextViews[4] != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsLikes().trim().equalsIgnoreCase("")){
                    allTextViews[4].setVisibility(View.GONE);
                } else {
                    allTextViews[4].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsLikes());
                }
            } else if(i == 5 && allTextViews[5] != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsDislikes().trim().equalsIgnoreCase("")){
                    allTextViews[5].setVisibility(View.GONE);
                } else {
                    allTextViews[5].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsDislikes());
                }
            } else if(i == 6 && allTextViews[6] != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsComments().trim().equalsIgnoreCase("")){
                    allTextViews[6].setVisibility(View.GONE);
                } else {
                    allTextViews[6].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsComments());
                }
            } else if(i == 7 && allTextViews[7] != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsViews().trim().equalsIgnoreCase("")){
                    allTextViews[7].setVisibility(View.GONE);
                } else {
                    allTextViews[7].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsViews());
                }
            } else if(i == 8 && allTextViews[8] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allTextViews[8].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemPrice());
            } else if(i == 9 && allTextViews[8] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allTextViews[9].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemQuantity());
            } else if(i == 10 && allTextViews[10] != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().length() > 500){
                    allTextViews[10].setVisibility(View.VISIBLE);
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() != Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsText().length() > 200){
                    allTextViews[10].setVisibility(View.VISIBLE);
                } else {
                    allTextViews[10].setVisibility(View.GONE);
                }
            } else if(i == 11 && allTextViews[11] != null){
                allTextViews[11].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsUrl());
            } else if(i == 12 && allTextViews[12] != null){
                allTextViews[12].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsUrlTitle());
            } else if(i == 13 && allTextViews[13] != null){
                allTextViews[13].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsTransactions());
            } else if(i == 14 && allTextViews[14] != null){
                allTextViews[14].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsReposts());
            } else if(i == 15 && allTextViews[15] != null){
                allTextViews[15].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsItemName());
            } else if(i == 16 && allTextViews[16] != null){
                allTextViews[16].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsItemLocation());
            } else if(i == 17 && allTextViews[17] != null){
                allTextViews[17].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getAdvertTextTitle());
            } else if(i == 18 && allTextViews[18] != null){
                allTextViews[18].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getAdvertTextTitle2());
            } else if(i == 19 && allTextViews[19] != null){
                allTextViews[19].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getReposterPottName());
            } else if(i == 19 && allTextViews[19] != null){
                allTextViews[19].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getReposterPottName());
            } else if(i == 20 && allTextViews[20] != null){
                allTextViews[20].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedText());
            } else if(i == 21 && allTextViews[21] != null){
                allTextViews[21].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemPrice());
            } else if(i == 22 && allTextViews[22] != null){
                allTextViews[22].setText(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsRealItemName());
            }
        }
    }


    private void setallConstraintLayoutBackgroundsInNews(ConstraintLayout[] allConstrainLayouts, int position){
        // 0 = AddedItemPriceBackground, 1 = AddedItemIconBackground, 2 = AddedItemQuantityBackground
        // 3 = NewsTextBackground, 4 = NewsRepostedAddedItemPriceBackground
        // 5 = NewsRepostedAddedItemHolder
        for (int i = 0; i < allConstrainLayouts.length; i++){
            if(i == 0 && allConstrainLayouts[0] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allConstrainLayouts[0].setVisibility(View.VISIBLE);
            } else if(i == 1 && allConstrainLayouts[1] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allConstrainLayouts[1].setVisibility(View.VISIBLE);
            } else if(i == 2 && allConstrainLayouts[2] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1){
                allConstrainLayouts[2].setVisibility(View.VISIBLE);
            } else if(i == 3 && allConstrainLayouts[3] != null){
                // 0 = black, 1 = red, 2 = yellow, 3 = green, 4 = orange, 5 = blue
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 0){
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 1) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 2) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 3) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 4) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsBackgroundColor() == 5) {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    allConstrainLayouts[3].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                }

            } else if(i == 4 && allConstrainLayouts[4] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim() != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")){
                    allConstrainLayouts[4].setVisibility(View.INVISIBLE);
                } else {
                    allConstrainLayouts[4].setVisibility(View.VISIBLE);
                }
            } else if(i == 5 && allConstrainLayouts[5] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim() != null){
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemPrice().trim().equalsIgnoreCase("")){
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
            if(i == 0 && allImageViews[0] != null && allErrorImageViews[0] != null && allProgressBar[0] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemStatus() == 1 && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim().length() > 1 ){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon().trim(), allImageViews[0], 0, 10, 10, allErrorImageViews[0], allProgressBar[0]);
                    }
                });
                imageLoaderThread.start();

            } else if (i == 1 && allImageViews[1] != null){
                // 1 = green, 2 = blue
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerAccountVerifiedStatus() == 1){
                    allImageViews[1].setVisibility(View.VISIBLE);
                    allImageViews[1].setImageResource(R.drawable.list_item_suggestedlinkupsactivity_activity_verifiedicon_green_imageview);
                } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerAccountVerifiedStatus() == 2){
                    allImageViews[1].setVisibility(View.VISIBLE);
                    allImageViews[1].setImageResource(R.drawable.list_item_suggestedlinkupsactivity_activity_verifiedicon_blue_imageview);
                } else {
                    allImageViews[1].setVisibility(View.INVISIBLE);
                }
            } else if (i == 2 && allImageViews[2] != null){
                //1 = Personal , 2 = Business
                if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerAccountType() == 2){
                    allImageViews[2].setVisibility(View.VISIBLE);
                } else {
                    allImageViews[2].setVisibility(View.INVISIBLE);
                }
            } else if(i == 3 && allImageViews[3] != null){

                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsVideosCoverArtsLinksSeparatedBySpaces().trim().equalsIgnoreCase("")){
                            Config.loadUrlImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsVideosCoverArtsLinksSeparatedBySpaces().trim(), allImageViews[3], 0, 70, 70);
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
                        if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_7_AND_9_JUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
                                || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_7_AND_9_REPOSTEDJUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY) {
                            if(!Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().equalsIgnoreCase("")){
                                Config.loadUrlImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim(), allImageViews[4], R.drawable.news_image, 50, 50);
                            } else {
                                Config.loadErrorImageView(getActivity(), R.drawable.news_image, allImageViews[4],  50, 50);
                            }
                        } else {
                            if(!Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().equalsIgnoreCase("")){
                                Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+")[0], allImageViews[4], 0, 50, 50, allErrorImageViews[4], allProgressBar[4]);
                            }
                        }
                    }
                });
                imageLoaderThread.start();
            } else if(i == 5 && allImageViews[5] != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getAdvertItemIcon().trim() == ""){
                            Config.loadUrlImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getAdvertItemIcon().trim(), allImageViews[5], 0, 20, 20);
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
                        if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerPottPic().trim().length() > 15){
                            Config.loadUrlImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerPottPic().trim(), allCircleImageViews[0], 0, 15, 15);
                        } else {
                            Config.loadErrorImageView(getActivity(), R.drawable.setprofilepicture_activity_imageholder_default_image, allCircleImageViews[0], 15, 15);
                        }
                    }
                });
                imageLoaderThread.start();
            } else if(i == 1 && allCircleImageViews[1] != null && allErrorImageViews[1] != null && allProgressBar[1] != null && Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon() != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon(), allCircleImageViews[1], 0, 20, 20, allErrorImageViews[1], allProgressBar[1]);
                    }
                });
                imageLoaderThread.start();
            } else if(i == 2 && allCircleImageViews[2] != null){
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedIcon().trim().length() > 15){
                            Config.loadUrlImage(getActivity(), true, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedIcon().trim(), allCircleImageViews[2], 0, 27, 27);
                        }
                    }
                });
                imageLoaderThread.start();
            }
        }
    }

    public void openPottActivity(int position, int openType){
        if(openType == 1){
            Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsMakerPottName());
        } else if(openType == 2){
            Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getSellerPottName());
        } else if(openType == 3){
            Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getSellerPottName());
        }
    }

    private void allOnClickHandlers2(View v, CheckBox likeIcon, CheckBox dislikeIcon, CheckBox repostLikeIcon, CheckBox repostDislikeIcon, int position){
        //Config.showToastType1(getActivity(), "Status : " + String.valueOf(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus()));
        String newsId = "";
        if(
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_1_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
                        || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_2_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
                        || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_3_TO_4_REPOSTEDJUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
                        || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_5_TO_6_REPOSTEDNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                        || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_7_AND_9_REPOSTEDJUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
                        || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_8_REPOSTEDJUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                        || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_14_REPOSTEDSHARESFORSALENEWS_VERTICAL_KEY
                        || Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsType() == Config.NEWS_TYPE_17_REPOSTEDSHARES4SALEWITHVIDEO_VERTICAL_KEY
                ){
            newsId = Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedNewsId();
        } else {
            newsId = Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId();
        }

        if(v.getId() == R.id.news_like_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.scale_bigout_and_small_in));
            dislikeIcon.setChecked(false);
            if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus() != Config.LIKED){
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.LIKED);
                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , false
                        , Config.LIKED
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            } else {
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , false
                        , Config.NO_REACTION
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            }
        } else if(v.getId() == R.id.news_dislike_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.scale_bigout_and_small_in));
            likeIcon.setChecked(false);
            if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewViewerReactionStatus() != Config.DISLIKED){
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.DISLIKED);
                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , false
                        , Config.DISLIKED
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            } else {
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , false
                        , Config.NO_REACTION
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            }
        } else if(v.getId() == R.id.news_reposted_like_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.scale_bigout_and_small_in));
            repostDislikeIcon.setChecked(false);
            if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus() != Config.LIKED){
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.LIKED);
                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , true
                        , Config.LIKED
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            } else {
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , true
                        , Config.NO_REACTION
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            }
        } else if(v.getId() == R.id.news_reposted_dislike_icon_imageview){
            v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.scale_bigout_and_small_in));
            repostLikeIcon.setChecked(false);
            if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewRepostedViewerReactionStatus() != Config.DISLIKED){
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.DISLIKED);

                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , true
                        , Config.DISLIKED
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            } else {
                Vertical_NewsType_ListDataGenerator.getAllData().get(position).setNewRepostedViewerReactionStatus(Config.NO_REACTION);
                Config.likeOrDislikeNews(
                        getActivity().getApplicationContext()
                        , true
                        , Config.NO_REACTION
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedNewsId()
                        , Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRowId()
                        , LocaleHelper.getLanguage(getActivity().getApplicationContext())
                );
            }
        }
    }


    private void allOnClickHandlers(View v, int position){
        if(v.getId() == R.id.news_maker_pottname_textview
                || v.getId() == R.id.news_maker_fullname_textview
                || v.getId() == R.id.shares_offering_company_name
                || v.getId() == R.id.shares_offering_company_name_view_profile){
            openPottActivity(position, 1);
        } else if(v.getId() == R.id.shares_logo_imageview || v.getId() == R.id.shares_name){
            Config.openActivity(getActivity(), StockProfileActivity.class, 1, 0, 1, "shareparentid",  Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsItemParentID());
        } else if(v.getId() == R.id.news_text_readmore_textview){
            Config.openActivity2(getActivity(), FullNewsActivity.class, 1, 0, 1, Config.FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId(), Config.FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, "200");
            //Config.openActivity(getActivity(), FullNewsActivity.class, 1, 0, 1, Config.FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId());
        } else if(v.getId() == R.id.news_added_item_price_textview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_added_item_quantity_textview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.news_website_address){
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsUrl());
        } else if(v.getId() == R.id.news_title){
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsUrl());
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
            if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedNewsId().trim().equalsIgnoreCase("")){
                Config.openFragment(getActivity().getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, NewsOptionsFragment.newInstance(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId()), "NewsOptionsFragment", 3);
            } else {
                Config.openFragment(getActivity().getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, NewsOptionsFragment.newInstance(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedNewsId()), "NewsOptionsFragment", 3);
            }
        } else if(v.getId() == R.id.news_added_item_icon_imageview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.newswithurl_image_imageview){
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsUrl());
        } else if(v.getId() == R.id.reload_addeditem_imageview){
            //mAddedItemImageReloadImageView
        } else if(v.getId() == R.id.play_icon){
            mNewsRecyclerView.playVideo();
        } else if(v.getId() == R.id.news_comment_icon_imageview){
            openNewsStatsActivity(position, 4);
            //Config.showDialogType1(getActivity(), "1", "News Sku: " +  String.valueOf(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsSku()), "", null, false, "Okay", "");
        } else if(v.getId() == R.id.news_repost_icon_imageview){
            //Config.openActivity(getActivity(), CreatePostActivity.class, 1, 0, 1, "newsid", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId());
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            intent.putExtra("newsid", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId());
            getActivity().startActivityForResult(intent, Config.CREATE_POST_NEWS_POSTED_REQUEST_CODE);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if(v.getId() == R.id.buy_icon_imageview){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.buy_button){
            openBuySharesActivity(position, 1);
        } else if(v.getId() == R.id.shares_for_sale_logo_circleimageview){
            Config.openActivity(getActivity(), StockProfileActivity.class, 1, 0, 1, "shareparentid", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsItemParentID());
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
        } else if(v.getId() == R.id.list_item_news_type_28_newsfeed_fragment_pott_pic_circleimageview){
            Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryMakerPottName());
        } else if(v.getId() == R.id.load_more_news_at_bottom_retry_imageview){
            NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()), -1);
        } else if(v.getId() == R.id.news_postin_retry_button){
            sendBackInfo("1");
        } else if(v.getId() == R.id.list_item_news_type_15_newsfeed_fragment_item_pic_circleimageview){
            Config.openActivity(getActivity(), StockProfileActivity.class, 1, 0, 1, "shareparentid",  NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getSharesParentId());
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
            if(Connectivity.isConnected(getActivity())) {
                Button mThisButton = (Button) v;
                mThisButton.setText(getResources().getString(R.string.sent));
                mThisButton.setClickable(false);
                sendLinkUp(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity()), NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().get(position).getLinkID()); // This must add the linkup to the local list of linkups for @pottname referencing
                NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().remove(position);
            } else {
                Config.showToastType1(getActivity(), getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
            }
        }

    }

    public void openNewsStatsActivity(int position, int type){
        // 0 = Likes, 1 = Dislikes, 2 = Views, 3 = Purchases, 4= Comments
        if(type == 0){
            Config.openActivity2(getActivity(), NewsStatsActivity.class, 1, 0, 1, "id", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "0");
        } else if(type == 1){
            Config.openActivity2(getActivity(), NewsStatsActivity.class, 1, 0, 1, "id", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "1");
        } else if(type == 2){
            Config.openActivity2(getActivity(), NewsStatsActivity.class, 1, 0, 1, "id", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "2");
        } else if(type == 3){
            Config.openActivity2(getActivity(), NewsStatsActivity.class, 1, 0, 1, "id", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "3");
        } else if(type == 4){
            Config.openActivity2(getActivity(), NewsStatsActivity.class, 1, 0, 1, "id", Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsId(), "stats_type", "4");
        }
    }

    public void openBuySharesActivity(int position, int newsType){
        // 0 = Reposted, 1 = Normal Vertical News, 2 = Stories Shares, 3 = Horizontal Shares
        if(newsType == 0){
            String[] buyData = {
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemId(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemParentID(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemName(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedItemQuantity(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getRepostedIcon()
            };
            Config.openActivity4(getActivity(), BuySharesForSaleActivity.class, 1, 0, 1, "BUY_INFO", buyData);
        } else if(newsType == 1) {
            String[] buyData = {
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemId(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsItemParentID(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsRealItemName(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemQuantity(),
                    Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsAddedItemIcon()
            };
            Config.openActivity4(getActivity(), BuySharesForSaleActivity.class, 1, 0, 1, "BUY_INFO", buyData);
        } else if(newsType == 2) {
            String[] buyData = {
                    NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryItemShareID(),
                    NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryItemParentID(),
                    NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryItemName(),
                    NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryItemQuantity(),
                    NewsType28_Stories_ListDataGenerator.getAllStoriesData().get(position).getStoryItemPic()
            };
            Config.openActivity4(getActivity(), BuySharesForSaleActivity.class, 1, 0, 1, "BUY_INFO", buyData);
        } else if(newsType == 3) {
            String[] buyData = {
                    NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getShareID(),
                    NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getSharesParentId(),
                    NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getShareItemName(),
                    NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getStockQuantity(),
                    NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().get(position).getStockPic()
            };
            Config.openActivity4(getActivity(), BuySharesForSaleActivity.class, 1, 0, 1, "BUY_INFO", buyData);
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
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
                    Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
                }
            });
            imageLoaderThread.start();

            sliderErrorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Config.loadUrlImageWithProgressBarAndReloadImage(getActivity(), true, allImages[position], sliderNewsImage, 0, 160, 160, sliderErrorImage, sliderProgressBar);
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
        if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 1){
            return new m1NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 2) {
            return new m2NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 3) {
            return new m3NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 4) {
            return new m4NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 5) {
            return new m5NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 6) {
            return new m6NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 7) {
            return new m7NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 8) {
            return new m8NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 9) {
            return new m9NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else if(Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+").length == 10) {
            return new m10NewsImagesViewPagerSliderAdapter(context, Vertical_NewsType_ListDataGenerator.getAllData().get(position).getNewsImagesLinksSeparatedBySpaces().trim().split("\\s+"));
        } else {
            return null;
        }
    }
}


        /*
        for (int i = 0; i < 37; i++){
            Vertical_NewsType_Model verticalNewsType_model = new Vertical_NewsType_Model();
            if(i == 0){
                verticalNewsType_model.setNewsType(Config.HORIZONTAL_NEWS_TYPE_28_KEY);
            } else if(i == 15){
                verticalNewsType_model.setNewsType(Config.HORIZONTAL_NEWS_TYPE_15_KEY);
            } else if(i == 9){
                verticalNewsType_model.setNewsType(Config.HORIZONTAL_NEWS_TYPE_26_KEY);
            } else {

                //verticalNewsType_model.setNewsType(Config.HORIZONTAL_NEWS_TYPE_28_KEY);
                //verticalNewsType_model.setNewsId("");
                verticalNewsType_model.setNewsMakerPottPic("https://fishpott.com/pic_upload/uploads/5lv7cjy4GabC6BQhWlxGWQX3HFAbAMY0kENriZAwuIHqzL7ccNw2019-01-091547043378.png");
                verticalNewsType_model.setNewsMakerPottName("@" + "raylight1");
                verticalNewsType_model.setNewsMakerFullName("Anno Dankyi");
                verticalNewsType_model.setNewsMakerAccountVerifiedStatus(1);
                verticalNewsType_model.setNewsMakerAccountType(1);
                verticalNewsType_model.setNewsTime("Today");
                verticalNewsType_model.setNewsLikes("200 Likes • ");
                verticalNewsType_model.setNewsDislikes("2 Dislikes • ");
                verticalNewsType_model.setNewsComments("300 Comments • ");
                verticalNewsType_model.setNewsTransactions("300 Buyers • ");
                verticalNewsType_model.setNewsViews("800 Views");
                verticalNewsType_model.setNewsAddedItemStatus(1);
                verticalNewsType_model.setNewsAddedItemType(1);
                verticalNewsType_model.setNewsAddedItemId("thisIsTheID");
                verticalNewsType_model.setNewsAddedItemIcon("https://www.britcham.co.za/wp-content/uploads/2018/01/Shell-Logo.jpg");
                verticalNewsType_model.setNewsAddedItemPrice("$1000");
                verticalNewsType_model.setNewsAddedItemQuantity("20000 Avail.");
                verticalNewsType_model.setNewsAddedItemQuantity("1000");
                verticalNewsType_model.setNewsAddedItemPrice("¢100");
                verticalNewsType_model.setRepostedItemPrice("¢100");
                verticalNewsType_model.setRepostedIcon("https://fergusscottishfestival.com/wp-content/uploads/2018/02/f.png");
                verticalNewsType_model.setNewsViewsRepostOrPurchasesShowStatus(2);
                verticalNewsType_model.setNewsText(getResources().getString(R.string.sample_news_text2));
                verticalNewsType_model.setNewsUrlTitle("Here we go again!!");
                verticalNewsType_model.setNewsUrl("www.ghanaweb.com");
                verticalNewsType_model.setNewsType(500);
                verticalNewsType_model.setAdvertItemIcon("https://fergusscottishfestival.com/wp-content/uploads/2018/02/f.png");
                verticalNewsType_model.setAdvertTextTitle("www.zabba.com");
                verticalNewsType_model.setAdvertTextTitle2("Connect to people all over the world");
                verticalNewsType_model.setAdvertButtonText("Connect");

                if(i == 1){
                    verticalNewsType_model.setNewsType(Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY);
                    verticalNewsType_model.setNewsText("The Joy of children laughing around you.");
                    verticalNewsType_model.setNewsBackgroundColor(1);
                } else if(i == 2 || i == 19){
                    verticalNewsType_model.setNewsType(Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY);
                    verticalNewsType_model.setNewsBackgroundColor(2);
                } else if(i == 3){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1556436209-ec3995884cef?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://fishpott.com/user/news_files/pics/2018-04-291525024152jcole.jpg https://images.unsplash.com/photo-1501438400798-b40ff50396c8?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=750&q=80 https://images.unsplash.com/photo-1518568814500-bf0f8d125f46?ixlib=rb-1.2.1&auto=format&fit=crop&w=334&q=80");
                } else if(i == 4){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://fishpott.com/user/news_files/pics/2018-07-071530984709AKAN.jpg https://images.unsplash.com/photo-1518568814500-bf0f8d125f46?ixlib=rb-1.2.1&auto=format&fit=crop&w=334&q=80 https://images.unsplash.com/photo-1513477967668-2aaf11838bd6?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=500&q=60");
                } else if(i == 5){
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-07-071530984709WWW.DOWNVIDS.NET-Akan%20-%20Me%20Sika%20Aduro%20%20[Official%20Video].mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("https://fishpott.com/user/news_files/pics/2018-07-071530984709AKAN.jpg");
                } else if(i == 6){
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-06-151529110933WWW.DOWNVIDS.NET-Twinings%20Advert%202011%20-%20Charlene%20Soraia%20Wherever%20You%20Will%20Go%20-%20Twinings%20Advert%20Song.mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("https://fishpott.com/user/news_files/pics/2018-06-151529110933bfa0cf8665fd48abba5e76409a4d5000.png");
                } else if(i == 7){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1556436209-ec3995884cef?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1513477967668-2aaf11838bd6?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=500&q=60 https://images.unsplash.com/photo-1518568814500-bf0f8d125f46?ixlib=rb-1.2.1&auto=format&fit=crop&w=334&q=80");
                } else if(i == 8){
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-04-29152502415228January.mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("https://fishpott.com/user/news_files/pics/2018-04-291525024152jcole.jpg");
                } else if(i == 10) {
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1552346053-c33aa8b3d665?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=500&q=60 https://images.unsplash.com/photo-1536520807309-1f7bae9f8be5?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=500&q=60 https://images.unsplash.com/photo-1516478177764-9fe5bd7e9717?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=500&q=60");
                    verticalNewsType_model.setNewsAddedItemPrice("$1200");
                    verticalNewsType_model.setNewsItemName("Nikey-Addidas Air");
                    verticalNewsType_model.setNewsItemLocation("Marple, Illinois");
                    verticalNewsType_model.setNewsAddedItemQuantity("25000 Avail.");
                } else if(i == 11) {
                    verticalNewsType_model.setNewsAddedItemIcon("https://www.britcham.co.za/wp-content/uploads/2018/01/Shell-Logo.jpg");
                    verticalNewsType_model.setNewsAddedItemPrice("$120");
                    verticalNewsType_model.setNewsItemName("1000 new investors");
                    verticalNewsType_model.setNewsItemLocation("DeJays Shares has increased from $120 to $200 in 32 days");
                    verticalNewsType_model.setNewsAddedItemQuantity("25000");
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("http://ghanahomecoming.com/wp-content/uploads/2018/12/Afrochella.jpeg https://egotickets.com/uploads/event/banner_photo/23793/49c512b1930326f9.jpg");
                } else if(i == 12) {
                    verticalNewsType_model.setNewsAddedItemIcon("http://go-jamaica.com/rw/images/news/20181012_62304.jpg");
                    verticalNewsType_model.setNewsAddedItemPrice("$20");
                    verticalNewsType_model.setNewsItemName("2% sold off");
                    verticalNewsType_model.setNewsItemLocation("Late Afternoon Show SFP value has reduced from $120 to $50 in 2 days");
                    verticalNewsType_model.setNewsAddedItemQuantity("57000");
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://egotickets.com/uploads/event/banner_photo/23793/49c512b1930326f9.jpg http://ghanahomecoming.com/wp-content/uploads/2018/12/Afrochella.jpeg");
                } else if(i == 13) {
                    verticalNewsType_model.setNewsAddedItemIcon("http://go-jamaica.com/rw/images/news/20181012_62304.jpg");
                    verticalNewsType_model.setNewsAddedItemPrice("$520");
                    verticalNewsType_model.setNewsItemName("Universal Oil Shares");
                    verticalNewsType_model.setNewsItemLocation("Universal Oil Shares is the best performing shares with 20% value increase in 2 days");
                    verticalNewsType_model.setNewsAddedItemQuantity("7000");
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1473655762411-be043ade8591?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1482555670981-4de159d8553b?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80");
                } else if(i == 14) {
                    verticalNewsType_model.setNewsAddedItemIcon("https://scontent.cdninstagram.com/vp/f6d6f50d871f0bef3bdf7d3bf2f2d06f/5D56E3D5/t51.2885-15/e35/s480x480/42484979_2214419272169736_2736715559483850393_n.jpg?_nc_ht=scontent-ort2-2.cdninstagram.com");
                    verticalNewsType_model.setNewsAddedItemPrice("$20");
                    verticalNewsType_model.setNewsItemName("Sarkodie Dome SFP");
                    verticalNewsType_model.setNewsItemLocation("Sarkodie Dome has reduced from $220 to $190 in 300 days");
                    verticalNewsType_model.setNewsAddedItemQuantity("600");
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80");
                }
                else if(i == 16) {
                    verticalNewsType_model.setNewsAddedItemIcon("https://scontent.cdninstagram.com/vp/f6d6f50d871f0bef3bdf7d3bf2f2d06f/5D56E3D5/t51.2885-15/e35/s480x480/42484979_2214419272169736_2736715559483850393_n.jpg?_nc_ht=scontent-ort2-2.cdninstagram.com");
                    verticalNewsType_model.setNewsAddedItemPrice("$20");
                    verticalNewsType_model.setNewsItemName("PeterPan Restaurant SFP");
                    verticalNewsType_model.setNewsItemLocation("PeterPan has reduced from $220 to $190 in 300 days");
                    verticalNewsType_model.setNewsAddedItemQuantity("600");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("https://img.artpal.com/07523/18-17-7-20-8-53-39m.jpg");
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-01-121515756050VID-20180112-111129.mp4");
                } else if(i == 17) {
                    verticalNewsType_model.setNewsAddedItemIcon("http://go-jamaica.com/rw/images/news/20181012_62304.jpg");
                    verticalNewsType_model.setNewsAddedItemPrice("$520");
                    verticalNewsType_model.setNewsItemName("Universal Oil Shares");
                    verticalNewsType_model.setNewsItemLocation("Universal Oil Shares is the best performing shares with 20% value increase in 2 days");
                    verticalNewsType_model.setNewsAddedItemQuantity("7000");
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-01-121515756050VID-20180112-111129.mp4");
                    //verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("http://go-jamaica.com/rw/images/news/20181012_62304.jpg");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("");
                } else if(i == 18){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("");
                } else if(i == 20){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("http://go-jamaica.com/rw/images/news/20181012_62304.jpg https://images.unsplash.com/photo-1516876902004-79f4bd1cb0dc?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1482555670981-4de159d8553b?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://egotickets.com/uploads/event/banner_photo/23793/49c512b1930326f9.jpg");
                } else if(i == 21){
                    verticalNewsType_model.setNewsText("Everybody can be a winner");
                    verticalNewsType_model.setNewsBackgroundColor(3);
                    verticalNewsType_model.setAdvertItemIcon("https://fergusscottishfestival.com/wp-content/uploads/2018/02/f.png");
                } else if(i == 22){
                    verticalNewsType_model.setNewsBackgroundColor(4);
                    verticalNewsType_model.setAdvertItemIcon("https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80");
                } else if(i == 23){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1458134580443-fbb0743304eb?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1518568814500-bf0f8d125f46?ixlib=rb-1.2.1&auto=format&fit=crop&w=334&q=80 https://images.unsplash.com/photo-1513477967668-2aaf11838bd6?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=500&q=60");
                    verticalNewsType_model.setAdvertItemIcon("https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80");
                } else if(i == 24){
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-03-071520415724videoplayback%20(1).mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("https://thingsthatmadeanimpression.files.wordpress.com/2013/02/picture-oneofus-osborne.jpg");
                    verticalNewsType_model.setAdvertItemIcon("https://images.unsplash.com/flagged/photo-1551255868-86bbc8e0f971?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80");
                } else if(i == 27){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1506269421513-e7dc9a2003af?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1501438400798-b40ff50396c8?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=750&q=80 https://images.unsplash.com/photo-1518568814500-bf0f8d125f46?ixlib=rb-1.2.1&auto=format&fit=crop&w=334&q=80");
                } else if(i == 28){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1547496726-08e54b67e96b?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1501438400798-b40ff50396c8?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=750&q=80 https://images.unsplash.com/photo-1518568814500-bf0f8d125f46?ixlib=rb-1.2.1&auto=format&fit=crop&w=334&q=80");
                } else if(i == 29){
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-01-081515425578VID-20180108-151413.mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("https://is2-ssl.mzstatic.com/image/thumb/Music128/v4/6e/c6/c2/6ec6c218-3512-aa41-dbea-2a41b78b60ec/UMG_cvrart_00602567261193_01_RGB72_3000x3000_17UM1IM55473.jpg/320x0w.jpg");
                } else if(i == 30){
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1534233650908-b471f2350922?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1513477967668-2aaf11838bd6?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=500&q=60 https://images.unsplash.com/photo-1518568814500-bf0f8d125f46?ixlib=rb-1.2.1&auto=format&fit=crop&w=334&q=80");
                } else if(i == 31){
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-03-081520509521videoplayback%20(2).mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("");
                } else if(i == 32) {
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://images.unsplash.com/photo-1542291026-7eec264c27ff?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1524102724373-bcf6ed410592?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80");
                } else if(i == 33) {
                    verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces("https://scontent.cdninstagram.com/vp/f6d6f50d871f0bef3bdf7d3bf2f2d06f/5D56E3D5/t51.2885-15/e35/s480x480/42484979_2214419272169736_2736715559483850393_n.jpg?_nc_ht=scontent-ort2-2.cdninstagram.com https://images.unsplash.com/photo-1515658323406-25d61c141a6e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80 https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80");
                } else if(i == 34) {
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2017-11-231511447553videoplayback%20(4).mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("http://go-jamaica.com/rw/images/news/20181012_62304.jpg");
                } else if(i == 35 || i == 36) {

                } else {
                    verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces("https://fishpott.com/user/news_files/videos/2018-07-071530984709WWW.DOWNVIDS.NET-Akan%20-%20Me%20Sika%20Aduro%20%20[Official%20Video].mp4");
                    verticalNewsType_model.setNewsVideosCoverArtsLinksSeparatedBySpaces("https://fishpott.com/user/news_files/pics/2018-07-071530984709AKAN.jpg");
                }
                verticalNewsType_model.setNewsVideosCount("2");
                verticalNewsType_model.setNewsImagesCount("3");

                //ADDING STORY OBJECT TO LIST
            }
            Vertical_NewsType_ListDataGenerator.addOneData(verticalNewsType_model);
        }
        */