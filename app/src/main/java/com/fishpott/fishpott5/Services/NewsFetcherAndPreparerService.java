package com.fishpott.fishpott5.Services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.ConfirmPhoneNumberActivity;
import com.fishpott.fishpott5.Activities.GovernmentIDVerificationActivity;
import com.fishpott.fishpott5.Activities.MainActivity;
//import com.fishpott.fishpott5.Activities.UpdateActivity;
import com.fishpott.fishpott5.Activities.TransactionsActivity;
import com.fishpott.fishpott5.Adapters.Contacts_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.MyShares_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.News_Type_28_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.SharesHosted_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.Vertical_News_Type_DatabaseAdapter;
import com.fishpott.fishpott5.Fragments.NewsFeedFragment;
import com.fishpott.fishpott5.Fragments.SettingsFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.MyShares_ListDataGenerator;
import com.fishpott.fishpott5.ListDataGenerators.NewsType28_Stories_ListDataGenerator;
import com.fishpott.fishpott5.ListDataGenerators.NewsType_15_Sharesforsale_horizontal_ListDataGenerator;
import com.fishpott.fishpott5.ListDataGenerators.NewsType_26_LinkupsHorizontal_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.NewsType_15_Sharesforsale_Horizontal_Model;
import com.fishpott.fishpott5.Models.NewsType_26_Linkups_Horizontal_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zatana on 1/9/19.
 */

public class NewsFetcherAndPreparerService extends Service {

    // constant
    private static final long NOTIFY_INTERVAL = 1 * 60 * 1000; // 1 minutes
    private static final long COLLECTING_CONTACTS_INTERVAL = 15 * 60 * 1000; // 15 minutes
    // run on another Thread to avoid crash
    // timer handling
    private Timer mTimer = null;
    private Thread newsFetchThread = null;
    private static Thread imageLoaderThread = null;
    private static String allPhonesNumbers = "", allPhonesNames = "", sessionId = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }

        newsFetchThread = new Thread(new Runnable() {
            @Override
            public void run () {
                // schedule task
                mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
                mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask2(), 0, COLLECTING_CONTACTS_INTERVAL);
            }
        });

        newsFetchThread.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // CLOSE BACKGROUND THREAD START
        imageLoaderThread =  Config.closeBackgroundThread2(imageLoaderThread);
        newsFetchThread = Config.closeBackgroundThread2(newsFetchThread);
        //CLOSE BACKGROUND THREAD END
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {

            /*
            fetchVerticalNewsAndPrepareMedia(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()), 0);
            //fetchVerticalNewsAndPrepareMedia(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()), -1);
            fetchStoriesHorizontalNewsAndPrepareMedia(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));
            fetchSharesForSaleHorizontalNewsAndPrepareMedia(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));
            fetchLinkupsHorizontalNewsAndPrepareMedia(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));
            fetchMyShares(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));
            fetchAvailableHostedShares(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));
             */
            updateUserInfo(getApplicationContext(), "", LocaleHelper.getLanguage(getApplicationContext()));

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w("FBToken", "Fetching FCM registration token failed", task.getException());
                                return;
                            } else {
                                Log.e("FBToken", "task.getResult(): " + task.getResult());
                                updateUserInfo(getApplicationContext(), task.getResult(), LocaleHelper.getLanguage(getApplicationContext()));
                            }
                        }
                    });
        }

    }

    class TimeDisplayTimerTask2 extends TimerTask {

        @Override
        public void run() {

            fetchMyContacts(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));

        }

    }

    public static void fetchVerticalNewsAndPrepareMedia(final Context context, String language, int latestSku){
        final Vertical_News_Type_DatabaseAdapter vertical_news_type_databaseAdapter1 = new Vertical_News_Type_DatabaseAdapter(context);
        vertical_news_type_databaseAdapter1.openDatabase();
        Cursor cursor = vertical_news_type_databaseAdapter1.getAllRows();
        long mostCurrentLocalNews = -1;
        final long cursorCount = cursor.getCount();
        if(cursorCount !=  0){
            if(latestSku == -1){
                cursor.moveToLast();
                latestSku = cursor.getInt(vertical_news_type_databaseAdapter1.COL_NEWS_NEWS_SKU);
            }
            cursor.moveToFirst();
            //cursor = vertical_news_type_databaseAdapter1.getFirstRow();
            mostCurrentLocalNews = cursor.getInt(vertical_news_type_databaseAdapter1.COL_NEWS_NEWS_SKU);
        } else {
            if(latestSku == -1){
                latestSku = 0;
            }
        }


        Log.e("NewsFeedFetchLocal-ON2", "===========================================================================");
        Log.e("NewsFeedFetchLocal-ON2", "cursor.getCount() : " + String.valueOf(cursor.getCount()));
        final int finalLatestSku = latestSku;
        Log.e("NewsFeedFetchLocal-ON2", "latestSku : " + String.valueOf(latestSku));
        Log.e("NewsFeedFetchLocal-ON2", "mostCurrentLocalNews : " + String.valueOf(mostCurrentLocalNews));
        cursor.close();
        vertical_news_type_databaseAdapter1.closeNews_Type_1_Database();

        final int finalLatestSku1 = latestSku;
        final long finalMostCurrentLocalNews = mostCurrentLocalNews;
        AndroidNetworking.post(Config.LINK_GET_VERTICAL_NEWS_SKU_DESCENDING)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("lastest_sku", String.valueOf(latestSku))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("config_fetch_vertical_news")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("NewsFeedFetchLocal-ON", "response : " + response);
                long mostCurrentOnlineSku = 0;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("data_returned");

                    JSONObject o = array.getJSONObject(0);
                    int myStatus = o.getInt("1");
                    String statusMsg = o.getString("2");

                    // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                    if(myStatus == 2){
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        //Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }

                    // GENERAL ERROR
                    if(myStatus == 3){
                        return;
                    }

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 4){
                        Config.signOutUser(context, false, null, null, 0, 2);
                    }

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                    Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                    Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_TRANSFER_FEE, o.getString("8"));

                    if (myStatus == 1) {
                        JSONArray newsArray = jsonObject.getJSONArray("news_returned");
                        // LIST RESULTS SETTING COMES HERE
                            if(newsArray.length() > 0 && finalLatestSku == 0){
                                vertical_news_type_databaseAdapter1.openDatabase();
                                vertical_news_type_databaseAdapter1.deleteAll();
                                Log.e("NewsFeedFetchLocal-ON", "HERE NOW 2 : ");
                            }
                            for (int i = 0; i < newsArray.length(); i++){
                                final JSONObject k = newsArray.getJSONObject(i);
                                if(i == 0){
                                    mostCurrentOnlineSku = Long.valueOf(k.getString("0a")).longValue();
                                }
                                Log.e("NewsFeedFetchLocal-ON2", "NEWS SKU : " + k.getString("0a"));
                                Log.e("fetchVerticalNews", "NEWS TEXT : " + k.getString("3"));
                                Log.e("fetchVerticalNews", "--------------------------------------------------");

                                // ADDING THE NEW TO THE LOCAL DATABASE
                                if(!vertical_news_type_databaseAdapter1.isVertNewsDbOpen()){
                                    vertical_news_type_databaseAdapter1.openDatabase();
                                }
                                vertical_news_type_databaseAdapter1.insertRow(
                                        k.getString("2"),
                                        k.getString("3"),
                                        k.getString("4"),
                                        k.getString("5"),
                                        k.getString("6"),
                                        k.getString("7"),
                                        k.getString("8"),
                                        k.getString("10"),
                                        k.getString("9"),
                                        k.getString("11"),
                                        k.getString("12"),
                                        k.getString("13"),
                                        k.getString("14"),
                                        k.getString("15"),
                                        k.getString("16"),
                                        k.getString("17"),
                                        k.getString("18"),
                                        k.getString("19"),
                                        k.getInt("1"),
                                        k.getInt("20"),
                                        k.getInt("21"),
                                        k.getString("22"),
                                        k.getString("23"),
                                        k.getInt("24"),
                                        k.getInt("25"),
                                        k.getInt("26"),
                                        k.getString("27"),
                                        k.getString("28"),
                                        k.getString("29"),
                                        k.getString("30"),
                                        k.getString("31"),
                                        k.getString("32"),
                                        k.getString("33"),
                                        k.getInt("34"),
                                        k.getString("35"),
                                        k.getString("36"),
                                        k.getString("37"),
                                        k.getString("38"),
                                        k.getString("39"),
                                        k.getString("40"),
                                        k.getString("41"),
                                        k.getString("42"),
                                        k.getInt("43"),
                                        k.getString("44"),
                                        k.getInt("45"),
                                        k.getInt("46"),
                                        k.getInt("0a"),
                                        k.getString("47"),
                                        k.getString("48"),
                                        k.getString("49"),
                                        k.getString("52"), //newsRepostAddedItemParentID
                                        k.getString("53"), //newsRepostAddedItemName
                                        k.getString("50"), //newsRealAddedItemName
                                        k.getString("54")  //newsRepostAddedItemQuantity
                                );

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
                            }

                        if(MyLifecycleHandler.isApplicationVisible()) {

                            Log.e("NewsFeedFetchLocal-ON", "fetchVerticalNews LOCALLY STARTED: - 1");
                            Log.e("NewsFeedFetchLocal-ON", "fetchVerticalNews LOCALLY STARTED: - 1");
                            if(MyLifecycleHandler.getCurrentActivity() != null && MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())) {

                                Log.e("NewsFeedFetchLocal-ON2", "fetchVerticalNews mostCurrent-LOCAL-SKU : " + String.valueOf(finalMostCurrentLocalNews));
                                Log.e("NewsFeedFetchLocal-ON2", "fetchVerticalNews mostCurrent-ONLINE-SKU : " + String.valueOf(mostCurrentOnlineSku));

                                if(finalLatestSku1 == 0){
                                    Log.e("NewsFeedFetchLocal-ON2", "HERE 1");
                                    if(cursorCount <= 0){
                                        Log.e("NewsFeedFetchLocal-ON2", "HERE 2");
                                        NewsFeedFragment.fetchVerticalNews(context, true);
                                    } else {
                                        Log.e("NewsFeedFetchLocal-ON2", "HERE 3");
                                        if(mostCurrentOnlineSku > finalMostCurrentLocalNews){
                                            Log.e("NewsFeedFetchLocal-ON2", "HERE 4");
                                            MainActivity.mNewsfeedMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                        }
                                    }
                                } /*else if(cursorCount > 0 && finalMostCurrentLocalNews < mostCurrentOnlineSku){
                                    MainActivity.mNewsfeedMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                    Log.e("NewsFeedFetchLocal-ON", "fetchVerticalNews LOCALLY STARTED: - 1");
                                }*/
                            } else {
                                Log.e("NewsFeedFetchLocal-ON", "ACTIVITY IS NULL OR NOT ON MAIN ACTIVITY ");
                            }
                        } else {
                            Log.e("NewsFeedFetchLocal-ON", "APPLICATION NOT VISIBLE ");
                        }
                            Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 1 ");
                    }

                    if(MyLifecycleHandler.isApplicationVisible()) {
                        Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 2 ");
                        if(MyLifecycleHandler.getCurrentActivity() != null && MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())) {
                            Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 3 ");
                        if(NewsFeedFragment.mMainSwipeRefreshLayout != null){
                            if(NewsFeedFragment.mMainSwipeRefreshLayout.isRefreshing()){
                                NewsFeedFragment.mMainSwipeRefreshLayout.setRefreshing(false);
                                Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 4 ");
                            }
                        }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    if(MyLifecycleHandler.isApplicationVisible()) {
                        Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 2 ");
                        if(MyLifecycleHandler.getCurrentActivity() != null && MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())) {
                            Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 3 ");
                            if(NewsFeedFragment.mMainSwipeRefreshLayout.isRefreshing()){
                                NewsFeedFragment.mMainSwipeRefreshLayout.setRefreshing(false);
                                Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 4 ");
                            }
                        }
                    }
                }

                vertical_news_type_databaseAdapter1.closeNews_Type_1_Database();
            }

            @Override
            public void onError(ANError anError) {
                vertical_news_type_databaseAdapter1.closeNews_Type_1_Database();
                if(MyLifecycleHandler.isApplicationVisible()) {
                    Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 5 ");
                    if(MyLifecycleHandler.getCurrentActivity() != null && MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())) {
                        Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 6 ");
                        if(NewsFeedFragment.mMainSwipeRefreshLayout.isRefreshing()){
                            NewsFeedFragment.mMainSwipeRefreshLayout.setRefreshing(false);
                            Log.e("NewsFeedFetchLocal-ON", "COMPLETE - 7 ");
                        }
                    }
                }
            }
        });

    }

    public void fetchStoriesHorizontalNewsAndPrepareMedia(final Context context, String language){

        AndroidNetworking.post(Config.LINK_GET_NEWS_STORIES_SKU_DESCENDING)
                .addBodyParameter("log_id_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("session_id", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_SERVER_SESSION_ID))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("config_fetch_stories")
                .setPriority(Priority.MEDIUM)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("data_returned");

                    JSONObject o = array.getJSONObject(0);
                    int myStatus = o.getInt("1");

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 3){
                        Config.signOutUser(context, false, null, null, 0, 2);
                    }

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("5"));

                    // UPDATING THE SERVER SESSION ID
                    Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_SERVER_SESSION_ID, o.getString("4"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("8"));
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("7"));
                    Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("6"));

                    if (myStatus == 1) {
                        JSONArray storiesArray = jsonObject.getJSONArray("stories_returned");
                        // LIST RESULTS SETTING COMES HERE
                            //CREATING THE NEWS STORIES DATABASE OBJECT
                            News_Type_28_DatabaseAdapter newsType1DatabaseAdapter = new News_Type_28_DatabaseAdapter(context);
                            // OPENING THE STORIES DATABASE
                            newsType1DatabaseAdapter.openDatabase();
                            newsType1DatabaseAdapter.deleteAll();

                        Log.e("StoriesOnTop", "DATABASE DELETED");
                            if(!NewsType28_Stories_ListDataGenerator.getAllStoriesData().isEmpty()){
                                NewsType28_Stories_ListDataGenerator.getAllStoriesData().clear();

                                Log.e("StoriesOnTop", "LIST HOLDER CLEARED");
                            }

                        Log.e("StoriesOnTop", "COMPLETE DELETION");

                            for (int i = 0; i<storiesArray.length(); i++){
                                final JSONObject k = storiesArray.getJSONObject(i);

                                // ADDING THE STORY TO THE LOCAL DATABASE
                                newsType1DatabaseAdapter.insertRow(
                                        k.getInt("1"),
                                        k.getString("2"),
                                        k.getString("3"),
                                        k.getString("4"),
                                        k.getString("5"),
                                        k.getString("6"),
                                        k.getString("7"),
                                        k.getString("8"),
                                        k.getString("9"),
                                        k.getString("10"),
                                        k.getString("11"),
                                        k.getString("12")
                                );

                                if(!k.getString("3").trim().equalsIgnoreCase("")){
                                    final String storyMakerPottPic = k.getString("5").trim();
                                    final String storyItemPic = k.getString("6").trim();
                                    imageLoaderThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Config.loadUrlImage(context, false, storyMakerPottPic, null, 0, 60, 60);
                                            Config.loadUrlImage(context, false, storyItemPic, null, 0, 145, 145);
                                        }
                                    });
                                    imageLoaderThread.start();
                                }

                            }
                            newsType1DatabaseAdapter.closeNewsStoriesDatabase();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {}
        });
    }


    public static void fetchSharesForSaleHorizontalNewsAndPrepareMedia(final Context context, String language){

        Log.e("fetchHoriSFSNews", "STARTED ");
        AndroidNetworking.post(Config.LINK_GET_HORIZONTAL_SHARES_FOR_SALE_NEWS_SKU_DESCENDING)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("config_fetch_horizontal_sharesforsale_news")
                .setPriority(Priority.MEDIUM)
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
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        //Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }

                    // GENERAL ERROR
                    if(myStatus == 3){
                        return;
                    }

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 4){
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
                        if(newsArray.length() > 0){
                            NewsType_15_Sharesforsale_horizontal_ListDataGenerator.getAllData().clear();
                        }
                        for (int i = 0; i < newsArray.length(); i++){
                            final JSONObject k = newsArray.getJSONObject(i);
                            NewsType_15_Sharesforsale_Horizontal_Model newsType_15_Sharesforsale_Horizontal_Model = new NewsType_15_Sharesforsale_Horizontal_Model();
                            newsType_15_Sharesforsale_Horizontal_Model.setSellerPottPic(k.getString("0a"));
                            newsType_15_Sharesforsale_Horizontal_Model.setStockPic(k.getString("1"));
                            newsType_15_Sharesforsale_Horizontal_Model.setStockInfo(k.getString("2"));
                            newsType_15_Sharesforsale_Horizontal_Model.setStockSellingPrice(k.getString("3"));
                            newsType_15_Sharesforsale_Horizontal_Model.setStockQuantity(k.getString("4"));
                            newsType_15_Sharesforsale_Horizontal_Model.setSharesNewsId(k.getString("5"));
                            newsType_15_Sharesforsale_Horizontal_Model.setSharesParentId(k.getString("6"));
                            newsType_15_Sharesforsale_Horizontal_Model.setSellerPottName(k.getString("7"));
                            newsType_15_Sharesforsale_Horizontal_Model.setShareID(k.getString("8"));
                            newsType_15_Sharesforsale_Horizontal_Model.setShareItemName(k.getString("9"));

                            NewsType_15_Sharesforsale_horizontal_ListDataGenerator.addOneData(newsType_15_Sharesforsale_Horizontal_Model);

                            imageLoaderThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Config.loadUrlImage(context, false, k.getString("0a"), null, 0, 60, 60);
                                        Config.loadUrlImage(context, false, k.getString("1"), null, 0, 100, 100);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            imageLoaderThread.start();
                        }
                        Log.e("fetchHoriSFSNews", "COMPLETE ");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {}
        });

    }


    public static void fetchLinkupsHorizontalNewsAndPrepareMedia(final Context context, String language){
        if(allPhonesNumbers.equalsIgnoreCase("") && allPhonesNames.equalsIgnoreCase("")){
            String[] allContacts = Config.getContactList2(context);
            allPhonesNumbers = allContacts[0];
            allPhonesNames = allContacts[1];
        }
        Log.e("fetchHoriL-UPSNews", "STARTED ");

        AndroidNetworking.post(Config.LINK_GET_HORIZONTAL_LINKUPS_NEWS)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("all_contacts_names", allPhonesNames)
                .addBodyParameter("all_contacts_numbers", allPhonesNumbers)
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("config_fetch_horizontal_linkups_news")
                .setPriority(Priority.MEDIUM)
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
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        //Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }

                    // GENERAL ERROR
                    if(myStatus == 3){
                        return;
                    }

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 4){
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
                        if(newsArray.length() > 0){
                            NewsType_26_LinkupsHorizontal_ListDataGenerator.getAllData().clear();
                        }
                        for (int i = 0; i < newsArray.length(); i++){
                            final JSONObject k = newsArray.getJSONObject(i);
                            NewsType_26_Linkups_Horizontal_Model newsType_26_linkups_horizontal_model = new NewsType_26_Linkups_Horizontal_Model();
                            newsType_26_linkups_horizontal_model.setLinkupPottPic(k.getString("0a"));
                            newsType_26_linkups_horizontal_model.setLinkupFullName(k.getString("1"));
                            newsType_26_linkups_horizontal_model.setLinkupPottName(k.getString("2"));
                            newsType_26_linkups_horizontal_model.setVerifiedStatus(k.getInt("3"));
                            newsType_26_linkups_horizontal_model.setLinkupInfo(k.getString("4"));
                            newsType_26_linkups_horizontal_model.setLinkID(k.getString("5"));
                            NewsType_26_LinkupsHorizontal_ListDataGenerator.addOneData(newsType_26_linkups_horizontal_model);

                            imageLoaderThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Config.loadUrlImage(context, false, k.getString("0a"), null, 0, 100, 100);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            imageLoaderThread.start();
                        }
                        Log.e("fetchHoriL-UPSNews", "COMPLETE ");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {}
        });

    }


    public static void fetchMyShares(final Context context, String language){

        Log.e("fetchMyShares", "START ");
        AndroidNetworking.post(Config.LINK_GET_MY_SHARES)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("fetch_my_shares")
                .setPriority(Priority.MEDIUM)
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
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        //Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }

                    // GENERAL ERROR
                    if(myStatus == 3){
                        return;
                    }

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 4){
                        Config.signOutUser(context, false, null, null, 0, 2);
                    }

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                    Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                    Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                    Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_TRANSFER_FEE, o.getString("8"));

                    if (myStatus == 1) {
                        JSONArray newsArray = jsonObject.getJSONArray("news_returned");
                        if(newsArray.length() > 0){
                            MyShares_ListDataGenerator.getAllData().clear();
                        }
                        MyShares_DatabaseAdapter myShares_databaseAdapter = new MyShares_DatabaseAdapter(context);
                        // OPENING THE STORIES DATABASE
                        myShares_databaseAdapter.openDatabase();
                        myShares_databaseAdapter.deleteAll();

                        for (int i = 0; i < newsArray.length(); i++){
                            final JSONObject k = newsArray.getJSONObject(i);

                            myShares_databaseAdapter.insertRow(
                                    k.getString("0a"),
                                    k.getString("1"),
                                    k.getString("3"),
                                    k.getString("4"),
                                    k.getString("5"),
                                    k.getString("6"),
                                    k.getString("2")
                            );

                        }
                        myShares_databaseAdapter.closeNewsStoriesDatabase();
                        Log.e("fetchMyShares", "COMPLETE ");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {}
        });

    }

    public static void fetchAvailableHostedShares(final Context context, String language){

        Log.e("fetchAvailHostedShares", "START ");
        AndroidNetworking.post(Config.LINK_GET_HOSTED_SHARES)
                .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("fetch_hosted_shares")
                .setPriority(Priority.MEDIUM)
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
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        //Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }

                    // GENERAL ERROR
                    if(myStatus == 3){
                        return;
                    }

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 4){
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
                        if(newsArray.length() > 0){
                            MyShares_ListDataGenerator.getAllData().clear();
                        }
                        SharesHosted_DatabaseAdapter sharesHostedDatabaseAdapter = new SharesHosted_DatabaseAdapter(context);
                        // OPENING THE STORIES DATABASE
                        sharesHostedDatabaseAdapter.openDatabase();
                        sharesHostedDatabaseAdapter.deleteAll();

                        for (int i = 0; i < newsArray.length(); i++){
                            final JSONObject k = newsArray.getJSONObject(i);

                            sharesHostedDatabaseAdapter.insertRow(
                                    k.getString("0a"), //share id
                                    k.getString("1"), //share name
                                    k.getString("2"), //share logo
                                    k.getString("3"), // value per share
                                    k.getString("4"), // dividend per share
                                    k.getString("5"), // company name
                                    k.getString("6"), // pottname name
                                    k.getString("7")  // share info
                            );

                        }
                        sharesHostedDatabaseAdapter.closeDatabase();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {}
        });

    }


    public static void fetchMyContacts(final Context context, String language){
        Config.sePersistentNotification(context, "149", "FishPott", context.getString(R.string.finding_new_people_and_stocks),  R.drawable.notification_icon);
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Log.e("fetchMyContacts", "START");
        String[] permissions = {
                Manifest.permission.READ_CONTACTS
        };
        if(Config.permissionsHaveBeenGranted(context, permissions) && allPhonesNames.trim().equalsIgnoreCase("") && allPhonesNumbers.trim().equalsIgnoreCase("")){
            String[] allContacts = Config.getContactList2(context);
            allPhonesNumbers = allContacts[0];
            allPhonesNames = allContacts[1];

            Log.e("fetchMyContacts", "allPhonesNumbers : " + allPhonesNumbers);
            Log.e("fetchMyContacts", "allPhonesNames : " + allPhonesNames);
        }

        //Show Notification


        AndroidNetworking.post(Config.LINK_GET_MY_CONTACTS)
                .addBodyParameter("log_id_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("all_contacts_names", "")
                .addBodyParameter("all_contacts_numbers", "")
                .addBodyParameter("session_id", sessionId)
                .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("config_fetch_horizontal_linkups_news")
                .setPriority(Priority.MEDIUM)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("fetchMyContacts", "response : " + response);
                mNotificationManager.cancel(149);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("data_returned");

                    JSONObject o = array.getJSONObject(0);
                    int myStatus = o.getInt("1");
                    String statusMsg = o.getString("2");

                    if (myStatus == 1) {
                        //sessionId = o.getString("4");// session_id
//                      STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("5"));

                        // SENDING THE USER TO THE PHONE NUMBER VERIFICATION ACTIVITY IF VERIFICATION IS TURNED ON
                        if(o.getBoolean("5")){
                            Config.openActivity3(context, ConfirmPhoneNumberActivity.class, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
                            return;
                        }

                        // GOVERNMENT ID VERIFICATION NEEDED
                        if(o.getBoolean("9")){
                            Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS, true);
                            Config.openActivity3(context, GovernmentIDVerificationActivity.class, 1, "", "");
                            return;
                        }

                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("8"));
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("7"));
                        Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("6"));

                        Log.e("fetchMyContacts", "2 response HERE ");
                        JSONArray newsArray = jsonObject.getJSONArray("news_returned");
                        Contacts_DatabaseAdapter contacts_databaseAdapter = new Contacts_DatabaseAdapter(context);
                        // OPENING THE STORIES DATABASE
                        contacts_databaseAdapter.openDatabase();

                        for (int i = 0; i < newsArray.length(); i++){
                            final JSONObject k = newsArray.getJSONObject(i);

                            long rowId = contacts_databaseAdapter.insertRow(
                                    k.getString("0a"), //chat id
                                    k.getString("1"), //receiver pottpic
                                    k.getString("2"), //receiver pottname
                                    k.getString("3") // receiver fullname
                            );
                            Log.e("fetchMyContacts", "rowId : " + rowId);
                        }
                        contacts_databaseAdapter.closeDatabase();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {mNotificationManager.cancel(149);}
        });


        Log.e("fetchMyContacts", "NOTIFICATION CANCELLED");
    }


    public static void updateUserInfo(final Context context, String deviceToken, String language){

        Log.e("updateUserInfo", "START updateUserInfo");
        Log.e("updateUserInfo", "deviceToken : " + deviceToken);
        AndroidNetworking.post(Config.LINK_UPDATE_USER_INFO)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(context))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(context)))
                .addBodyParameter("fcm_token", deviceToken)
                .setTag("background_userinfo_update")
                .setPriority(Priority.MEDIUM)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.e("updateUserInfo", "response : " + response);
                try {
                    final JSONObject o = new JSONObject(response);
                    int myStatus = o.getInt("status");
                    final String statusMsg = o.getString("message");

                    // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                    if(myStatus == 2){
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        return;
                    }

                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    if(myStatus == 4){
                        Config.signOutUser(context, false, null, null, 0, 2);
                    }

                    if (myStatus == 1) {
                        //STORING THE USER DATA

                        /*
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN, o.getString("8"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE, o.getString("9"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO, o.getString("10"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN_NAME, o.getString("11"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE_NAME, o.getString("12"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO_NAME, o.getString("13"));

                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_TRANSFER_FEE, o.getString("17"));
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET, o.getString("14"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEBIT_WALLET, o.getString("15"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_PEARLS, o.getString("16"));
                        */

                        if(SettingsFragment.mPottIntelligenceTextView != null){
                            SettingsFragment.mPottIntelligenceTextView.setText(o.getJSONObject("data").getString("pott_intelligence"));
                        }

                        if(SettingsFragment.mPottNetWorthTextView != null){
                            SettingsFragment.mPottNetWorthTextView.setText(o.getJSONObject("data").getString("pott_networth"));
                        }

                        if(SettingsFragment.mPottPositionTextView != null){
                            SettingsFragment.mPottPositionTextView.setText(o.getJSONObject("data").getString("pott_position"));
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                        Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                        // FISHPOTT USER ACCOUNT INFO
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_INTELLIGENCE, o.getJSONObject("data").getString("pott_intelligence"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NET_WORTH, o.getJSONObject("data").getString("pott_networth"));
                        Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_POSITION, o.getJSONObject("data").getString("pott_position"));


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