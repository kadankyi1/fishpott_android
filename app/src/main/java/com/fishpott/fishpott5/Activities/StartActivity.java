package com.fishpott.fishpott5.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;
import com.fishpott.fishpott5.Util.NonUnderlinedClickableSpan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;



public class StartActivity extends AppCompatActivity implements Animation.AnimationListener, View.OnClickListener {

    private ImageView mFpLogoImageView;
    private ImageView mFpCoverImageView;
    private TextView mLangEnglishTextView;
    private TextView mLangFrenchTextView;
    private TextView mLangChineseTextView;
    private Button mStartButton;
    private ImageView mFpLogoStaticImageView;
    private ConstraintLayout mPrivacyPolicyHolderConstraintLayout;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ProgressBar mProgressBar;
    private Thread appInitThread2 = null;
    private Dialog.OnCancelListener cancelListenerActive1;
    private Boolean START_ANIMATION = true;
    private String sharedPrefUpdateDate = "";
    private int sharedPrefUpdateVersionCode = 0;
    private int currentVersionCode = 0;
    private Boolean sharedPrefUpdateForceStatus = false, SHOW_DIALOG_AND_STOP_PROCESSES = false;
    private long updateDateDaysDifference = 0;
    private int viewStartActivityCount = 0;
    private int SPLASH_TIME_OUT = 1000;
    private String pottName = "", newsID = "";
    private Uri uriData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
// Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        };
        // INITIALIZING THE APP FROM THE BACKGROUND    // BACKGROUND ACTIONS COME HERE
        /*
        appInitThread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                initializeApp();
            }
        });
        appInitThread2.start();
        */

        initializeApp();

        if(savedInstanceState != null){
            START_ANIMATION = savedInstanceState.getBoolean("START_ANIMATION");
        }

        // BINDING VIEWS
        mFpLogoImageView = findViewById(R.id.start_activity_fpLogoimageView);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mFpCoverImageView = findViewById(R.id.fpCoverimageView);
        }

        mProgressBar = findViewById(R.id.start_activity_progress_bar1);
        mLangEnglishTextView = findViewById(R.id.start_activity_englishLanguageTextView);
        mLangFrenchTextView = findViewById(R.id.start_activity_frenchLanguageTextView);
        mLangChineseTextView = findViewById(R.id.start_activity_chineseLanguageTextView);
        mStartButton = findViewById(R.id.start_activity_startbutton);
        mFpLogoStaticImageView = findViewById(R.id.start_activity_fpStaticimageView);
        mPrivacyPolicyHolderConstraintLayout = findViewById(R.id.start_activity_privacypolicy_holder);

        /*
        mLangEnglishTextView.setOnClickListener(this);
        mLangFrenchTextView.setOnClickListener(this);
        mLangChineseTextView.setOnClickListener(this);
         */
        //mPrivacyPolicyHolderConstraintLayout.setOnClickListener(this);
        mStartButton.setOnClickListener(this);

        // STARTING ANIMATION
        if(START_ANIMATION) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){

                mFpCoverImageView.setVisibility(View.GONE);

            }
            /*
            mLangEnglishTextView.setVisibility(View.GONE);
            mLangFrenchTextView.setVisibility(View.GONE);
            mLangChineseTextView.setVisibility(View.GONE);
             */
            mFpLogoStaticImageView.setVisibility(View.GONE);

            Animation moveFBLogoAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.start_fp_logo_move);
            moveFBLogoAnimation.setFillAfter(true);
            moveFBLogoAnimation.setAnimationListener(this);
            mFpLogoImageView.startAnimation(moveFBLogoAnimation);

        } else {
            // IF USER IS ALREADY LOGGED IN, WE DO NOT SHOW THE START BUTTON
            if(Config.userIsLoggedIn(StartActivity.this)){
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mStartButton.setVisibility(View.VISIBLE);
                //mPrivacyPolicyHolderConstraintLayout.setVisibility(View.VISIBLE);
            }
            mFpLogoImageView.setVisibility(View.GONE);
        }


        //STARTING THE SERVICE THAT GETS THE NEWSFEED AND POPULATE THE LOCAL SQL DATABASE
        Intent myIntent = new Intent(getApplicationContext(), NewsFetcherAndPreparerService.class);
        getApplication().startService(myIntent);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() ==  R.id.start_activity_englishLanguageTextView){
            //boldenLanguageChosenText("en");
        } else if(view.getId() ==  R.id.start_activity_frenchLanguageTextView){
            //boldenLanguageChosenText("fr");
        } else if(view.getId() ==  R.id.start_activity_chineseLanguageTextView){
            //boldenLanguageChosenText("zh");
        } else if(view.getId() ==  R.id.start_activity_startbutton){
            Config.openActivity(StartActivity.this, SliderActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.start_activity_privacypolicy_holder){
            Config.openActivity(StartActivity.this, WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, Config.CURRENT_HTTP_IN_USE + "www.fishpott.com/pp.html");
            return;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mFpLogoStaticImageView.setVisibility(View.VISIBLE);
        mFpLogoImageView.clearAnimation();
        mFpLogoImageView.setVisibility(View.GONE);
        if(Config.userIsLoggedIn(StartActivity.this)){
            mProgressBar.setAlpha(0f);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mStartButton.setAlpha(0f);
            mStartButton.setVisibility(View.VISIBLE);
            //mPrivacyPolicyHolderConstraintLayout.setAlpha(0f);
            //mPrivacyPolicyHolderConstraintLayout.setVisibility(View.VISIBLE);
        }
        /*
        mLangEnglishTextView.setAlpha(0f);
        mLangEnglishTextView.setVisibility(View.VISIBLE);
        mLangFrenchTextView.setAlpha(0f);
        mLangFrenchTextView.setVisibility(View.VISIBLE);
        mLangChineseTextView.setAlpha(0f);
        mLangChineseTextView.setVisibility(View.VISIBLE);
         */
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mFpCoverImageView.setAlpha(0f);
            mFpCoverImageView.setVisibility(View.VISIBLE);
        }
        int mediumAnimationTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        /*
        mLangEnglishTextView.animate().alpha(1f).setDuration(mediumAnimationTime).setListener(null);
        mLangFrenchTextView.animate().alpha(1f).setDuration(mediumAnimationTime).setListener(null);
        mLangChineseTextView.animate().alpha(1f).setDuration(mediumAnimationTime).setListener(null);
         */
        mStartButton.animate().alpha(1f).setDuration(mediumAnimationTime).setListener(null);
        //mPrivacyPolicyHolderConstraintLayout.animate().alpha(1f).setDuration(mediumAnimationTime).setListener(null);
        mProgressBar.animate().alpha(1f).setDuration(mediumAnimationTime).setListener(null);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mFpCoverImageView.animate().alpha(1f).setDuration(mediumAnimationTime).setListener(null);
        }
        if(Config.userIsLoggedIn(StartActivity.this))
        {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mStartButton.setVisibility(View.VISIBLE);
            //mPrivacyPolicyHolderConstraintLayout.setVisibility(View.VISIBLE);
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if(Config.userIsLoggedIn(StartActivity.this)){
                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_START_ACTIVITY_VIEWING_CYCLE_COUNT, viewStartActivityCount + 1);
                    if(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS) == 1){
                        Config.openActivity(StartActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                        return;
                    }
                    if(Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS)){
                        Config.openActivity(StartActivity.this, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
                        return;
                    }
                    if(!Config.userProfilePictureIsSet(StartActivity.this)){
                        Config.openActivity(StartActivity.this, SetProfilePictureActivity.class, 1, 1, 1, Config.KEY_ACTIVITY_FINISHED, "yes");
                        return;
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(!pottName.trim().equalsIgnoreCase("")){
                                    Config.openActivity(StartActivity.this, ProfileOfDifferentPottActivity.class, 1, 2, 1, "pottname", pottName);
                                } else if(!newsID.trim().equalsIgnoreCase("")){
                                    Config.openActivity(StartActivity.this, FullNewsActivity.class, 1, 2, 1, "newsid", newsID);
                                } else {
                                    if(SHOW_DIALOG_AND_STOP_PROCESSES){
                                        Config.showDialogType1(StartActivity.this, "", getString(R.string.Oops_what_you_are_looking_for_was_not_found), "", cancelListenerActive1, true, getString(R.string.setprofilepicture_activity_okay), "");
                                        finish();
                                    } else {
                                        Config.openActivity(StartActivity.this, MainActivity.class, 1, 2, 0, "", "");
                                        //Config.openActivity(StartActivity.this, SetProfilePictureActivity.class, 1, 2, 0, "", "");
                                        return;
                                    }
                                }
                            }
                        });
                    }


                }

            }
        }, SPLASH_TIME_OUT);

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // STORING THE STATE OF THE ANIMATION IN CASE THE STATE OF THE APP CHANGES, SAY ORIENTATION
        outState.putBoolean("START_ANIMATION", false);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    // FUNCTION TO BOLDEN LANGUAGE TEXT BASED ON CURRENT LANGUAGE
    public void boldenLanguageChosenText(String languageChosen){

        Context context;
        Resources resources;
        if (languageChosen.equalsIgnoreCase("zh")) {
            context = LocaleHelper.setLocale(StartActivity.this, "zh");
            resources = context.getResources();
            mStartButton.setText(resources.getString(R.string.start_activity_start));
            mLangEnglishTextView.setTypeface(null, Typeface.NORMAL);
            mLangFrenchTextView.setTypeface(null, Typeface.NORMAL);
            mLangChineseTextView.setTypeface(null, Typeface.BOLD);
        } else if (languageChosen.equalsIgnoreCase("fr")) {
            context = LocaleHelper.setLocale(StartActivity.this, "fr");
            resources = context.getResources();
            mStartButton.setText(resources.getString(R.string.start_activity_start));
            mLangEnglishTextView.setTypeface(null, Typeface.NORMAL);
            mLangChineseTextView.setTypeface(null, Typeface.NORMAL);
            mLangFrenchTextView.setTypeface(null, Typeface.BOLD);
        } else {
            context = LocaleHelper.setLocale(StartActivity.this, "en");
            resources = context.getResources();
            mStartButton.setText(resources.getString(R.string.start_activity_start));
            mLangFrenchTextView.setTypeface(null, Typeface.NORMAL);
            mLangChineseTextView.setTypeface(null, Typeface.NORMAL);
            mLangEnglishTextView.setTypeface(null, Typeface.BOLD);
        }

    }


    private void initializeApp(){
        FirebaseMessaging.getInstance().subscribeToTopic("FISHPOT_TIPS");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FBToken", "Fetching FCM registration token failed", task.getException());
                            return;
                        } else {
                            Log.e("FBToken", "task.getResult(): " + task.getResult());
                            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEVICE_TOKEN, task.getResult());
                            NewsFetcherAndPreparerService.updateUserInfo(getApplicationContext(), task.getResult(), LocaleHelper.getLanguage(getApplicationContext()));
                        }
                    }
                });

        /*
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEVICE_TOKEN, instanceIdResult.getToken());
                NewsFetcherAndPreparerService.updateUserInfo(getApplicationContext(), instanceIdResult.getToken(), LocaleHelper.getLanguage(getApplicationContext()));

            }
        });
         */
        // BACKGROUND ACTIONS COME HERE
        AndroidNetworking.initialize(getApplicationContext());

        // GETTING UPDATE NOT NOW DATE, FORCE UPDATE STATUS AND VERSION CODE FROM SHARED PREFERENCE
        sharedPrefUpdateDate = Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE);
        sharedPrefUpdateVersionCode = Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE);
        sharedPrefUpdateForceStatus = Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE);

        currentVersionCode = Config.getAppVersionCode(StartActivity.this);

        // GETTING DATE DIFFERENCE BETWEEN UPDATE NOT NOW DATE AND CURRENT DATE
        updateDateDaysDifference = Config.getDateDifferenceBetweenDates(sharedPrefUpdateDate, Config.getCurrentDate());
        //Config.showDialogType1(StartActivity.this, getString(R.string.login_activity_login_failed), "sharedPrefUpdateForceStatus : " + sharedPrefUpdateForceStatus + " --  sharedPrefUpdateVersionCode : " + sharedPrefUpdateVersionCode + " -- currentVersionCode : " + currentVersionCode);

        //CHECKING IF AN UPDATE IS AVAILABLE AND DIRECTING TO UPDATE ACTIVITY
        if((sharedPrefUpdateForceStatus &&  sharedPrefUpdateVersionCode >  currentVersionCode) || sharedPrefUpdateVersionCode >  currentVersionCode && updateDateDaysDifference > 1){
            Config.openActivity(StartActivity.this, UpdateActivity.class, 1, 1, 1, Config.KEY_ACTIVITY_FINISHED, "1");
            return;
        }


        // CHECKING IF THE CYCLE HAS REACHED IT'S LIMIT OR NOT AND ALLOWING THE ANIMATION TO SHOW
        viewStartActivityCount = Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_START_ACTIVITY_VIEWING_CYCLE_COUNT);
        if(viewStartActivityCount < 5){

            // REDIRECTING USER IF LOGGED IN OR NOT
            if(Config.userIsLoggedIn(StartActivity.this)){
                //INCREASING THE VIEW CYCLE COUNT
                Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_START_ACTIVITY_VIEWING_CYCLE_COUNT, viewStartActivityCount + 1);

                // CHECKING IF ACCOUNT IS SUSPENDED
                if(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS) == 1){
                    Config.openActivity(StartActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                    return;
                }

                // CHECKING IF ACCOUNT ID VERIFICATION IS NEEDED
                if(Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS)){
                    Config.openActivity(StartActivity.this, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
                    return;
                }

                // CHECKING IF THE USER HAS TO VERIFY THEIR PHONE NUMBER
                if(Config.getSharedPreferenceBoolean(StartActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON) && !Config.getSharedPreferenceString(StartActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE).trim().equalsIgnoreCase("")){
                    Config.openActivity(StartActivity.this, ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, Config.getSharedPreferenceString(StartActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
                    return;
                }

                //REDIRECTING IF USER HAS SET PROFILE PICTURE OR NOT
                if(!Config.userProfilePictureIsSet(StartActivity.this)){
                    Config.openActivity(StartActivity.this, SetProfilePictureActivity.class, 1, 2, 1,Config.KEY_ACTIVITY_FINISHED, "yes");
                    return;
                } else {
                    // CHANGE TO MAIN ACTIVTY
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(!pottName.trim().equalsIgnoreCase("")){
                                Config.openActivity(StartActivity.this, ProfileOfDifferentPottActivity.class, 1, 2, 1, "pottname", pottName);
                            } else if(!newsID.trim().equalsIgnoreCase("")){
                                Config.openActivity(StartActivity.this, FullNewsActivity.class, 1, 2, 1, "newsid", newsID);
                            } else {
                                if(SHOW_DIALOG_AND_STOP_PROCESSES){
                                    Config.showDialogType1(StartActivity.this, "", getString(R.string.Oops_what_you_are_looking_for_was_not_found), "", cancelListenerActive1, true, getString(R.string.setprofilepicture_activity_okay), "");
                                    finish();
                                } else {
                                    Config.openActivity(StartActivity.this, MainActivity.class, 1, 2, 0, "", "");
                                    //Config.openActivity(StartActivity.this, SetProfilePictureActivity.class, 1, 2, 0, "", "");
                                    return;
                                }
                            }
                        }
                    });
                }
            }

        } else {

            // RESETTING THE VIEW ANIMATION COUNT
            Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_START_ACTIVITY_VIEWING_CYCLE_COUNT, 0);

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED  START-ACTIVITY");
        mProgressBar = findViewById(R.id.start_activity_progress_bar1);
        mLangEnglishTextView = findViewById(R.id.start_activity_englishLanguageTextView);
        mLangFrenchTextView = findViewById(R.id.start_activity_frenchLanguageTextView);
        mLangChineseTextView = findViewById(R.id.start_activity_chineseLanguageTextView);
        mStartButton = findViewById(R.id.start_activity_startbutton);
        mFpCoverImageView = findViewById(R.id.fpCoverimageView);
        mFpLogoImageView = findViewById(R.id.start_activity_fpLogoimageView);
        mFpLogoStaticImageView = findViewById(R.id.start_activity_fpStaticimageView);
        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        };


        Intent intent = getIntent();
        pottName = "";
        newsID = "";

        if(intent != null) {
            uriData = intent.getData();
            if (uriData != null) {
                pottName = uriData.getQueryParameter("pottname");
                newsID = uriData.getQueryParameter("newsid");
                if(pottName == null){
                    pottName = "";
                }
                if(newsID == null){
                    newsID = "";
                }
                if(newsID.trim().equalsIgnoreCase("") && pottName.trim().equalsIgnoreCase("")){
                    SHOW_DIALOG_AND_STOP_PROCESSES = true;
                }

            }
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED START-ACTIVITY");
        mFpLogoImageView = null;
        mFpCoverImageView = null;
        mLangEnglishTextView = null;
        mLangFrenchTextView = null;
        mLangChineseTextView = null;
        mStartButton = null;
        mFpLogoStaticImageView = null;
        mProgressBar = null;

        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED START-ACTIVITY");
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED START-ACTIVITY");

        cancelListenerActive1 = null;
        mFpLogoImageView = null;
        mFpCoverImageView = null;
        mLangEnglishTextView = null;
        mLangFrenchTextView = null;
        mLangChineseTextView = null;
        mStartButton = null;
        mFpLogoStaticImageView = null;
        mProgressBar = null;

        if(appInitThread2 != null){
            appInitThread2.interrupt();
            appInitThread2 = null;
        }
        Config.unbindDrawables(findViewById(R.id.mainConstraintLayout_start_activity));
        Config.freeMemory();
    }

}
