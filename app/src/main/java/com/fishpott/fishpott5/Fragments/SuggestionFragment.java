package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.BuyBusinessStockSuggestedActivity;
import com.fishpott.fishpott5.Activities.BuySharesForSaleActivity;
import com.fishpott.fishpott5.Activities.LoginActivity;
import com.fishpott.fishpott5.Activities.ProfileOfDifferentPottActivity;
import com.fishpott.fishpott5.Activities.StartActivity;
import com.fishpott.fishpott5.Activities.UpdateActivity;
import com.fishpott.fishpott5.Activities.WebViewActivity;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.NewsType_15_Sharesforsale_horizontal_ListDataGenerator;
import com.fishpott.fishpott5.ListDataGenerators.Notifications_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestionFragment extends Fragment implements View.OnClickListener {
    private ConstraintLayout mDrillSuggestionHolderConstraintLayout, mAnswerHolderConstraintLayout;
    private ScrollView mBusinessSuggestionHolderScrollView, mAnswersCountScrollView;
    private TextView mDrillQuestionTextView, mSuggestionLoaderTextTextView, mSuggestionBusinessNameTextView, mSuggestionBusinessCountryTextView, mSuggestionBusinessNetworthTextView,
            mBusinessCountInvestorsTextView, mSuggestionBusinessPitchTextView, mSuggestionBusinessCEOTextView, mSuggestionBusinessCOOTextView, mSuggestionBusinessServicesBioTextView,
            mBusinessWebsiteTextView, mBusinessRevenueLastYrTextView, mBusinessDebtTextView, mBusinessInvestmentsInTextView, mSuggestionBusinessFinanceBioTextView,
            mSuggestionBusinessFinanceFullReportTextView, mAnswer1CountTextView, mAnswer2CountTextView, mAnswer3CountTextView, mAnswer4CountTextView,
            mAnswer1TextView, mAnswer2TextView, mAnswer3TextView, mAnswer4TextView, mNextDrillTextView;
    private CircleImageView mBusinessLogoCircleImageView;
    private MxVideoPlayerWidget mBusinessPitchVideoMxVideoPlayerWidget;
    private Button mAnswer1Button, mAnswer2Button, mAnswer3Button, mAnswer4Button;
    private ImageView mSuggestionLoaderImageView;
    private Boolean networkRequestStarted = false;
    private Button mSuggestionBusinessBuySharesButton;
    private String drillID = "", businessID = "", businessNameGlobal = "", businessLogoUrl = "", businessWebsiteUrl = "", businessFullReportUrl = "";

    public SuggestionFragment() {
        // Required empty public constructor
    }


    public static SuggestionFragment newInstance() {
        SuggestionFragment fragment = new SuggestionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_suggestion, container, false);
        // LOADER
        mSuggestionLoaderImageView = view.findViewById(R.id.fragment_suggestion_loader_imageview);
        mSuggestionLoaderTextTextView = view.findViewById(R.id.fragment_suggestion_loadertext_textview);

        // DRILL SUGGESTION OBJECTS
        mDrillSuggestionHolderConstraintLayout = view.findViewById(R.id.fragment_suggestion_drill_holder_constraintlayout);
        mAnswerHolderConstraintLayout = view.findViewById(R.id.fragment_suggestion_allanswers_holder_constraintlayout);
        mDrillQuestionTextView = view.findViewById(R.id.fragment_suggestion_question_textview);
        mAnswer1Button = view.findViewById(R.id.fragment_suggestion_answer1_button);
        mAnswer2Button = view.findViewById(R.id.fragment_suggestion_answer2_button);
        mAnswer3Button = view.findViewById(R.id.fragment_suggestion_answer3_button);
        mAnswer4Button = view.findViewById(R.id.fragment_suggestion_answer4_button);

        // DRILL ANSWERS COUNT OBJECTS
        mAnswersCountScrollView = view.findViewById(R.id.fragment_suggestion_answersoffriends_holder_scrollview);
        mNextDrillTextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_notify_textview);
        mAnswer1CountTextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer1_answerscount_textview);
        mAnswer2CountTextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer2_answerscount_textview);
        mAnswer3CountTextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer3_answerscount_textview);
        mAnswer4CountTextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer4_answerscount_textview);
        mAnswer1TextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer1_answer_textview);
        mAnswer2TextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer2_answer_textview);
        mAnswer3TextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer3_answer_textview);
        mAnswer4TextView = view.findViewById(R.id.fragment_suggestion_answersoffriends_answer4_answer_textview);

        // BUSINESS SUGGESTION OBJECTS
        mBusinessSuggestionHolderScrollView = view.findViewById(R.id.fragment_suggestion_business_holder_constraintlayout);
        mSuggestionBusinessNameTextView = view.findViewById(R.id.fragment_suggestion_business_name_textview);
        mBusinessLogoCircleImageView = view.findViewById(R.id.fragment_suggestion_business_logo_textview);
        mSuggestionBusinessCountryTextView = view.findViewById(R.id.fragment_suggestion_business_country_textview);
        mSuggestionBusinessBuySharesButton = view.findViewById(R.id.buyshares_button_pp_dialog);
        mSuggestionBusinessNetworthTextView = view.findViewById(R.id.fragment_suggestion_business_networthvalue_textview);
        mBusinessCountInvestorsTextView = view.findViewById(R.id.fragment_suggestion_business_investorsvalue_textview);
        mSuggestionBusinessPitchTextView = view.findViewById(R.id.fragment_suggestion_business_pitchtext_textview);
        mBusinessPitchVideoMxVideoPlayerWidget = view.findViewById(R.id.mpw_video_player);
        mSuggestionBusinessCEOTextView = view.findViewById(R.id.fragment_suggestion_business_ceotext_textview);
        mSuggestionBusinessCOOTextView = view.findViewById(R.id.fragment_suggestion_business_cootext_textview);
        mSuggestionBusinessServicesBioTextView = view.findViewById(R.id.fragment_suggestion_business_servicetext_textview);
        mBusinessWebsiteTextView = view.findViewById(R.id.fragment_suggestion_business_servicewebsite_textview);
        mBusinessRevenueLastYrTextView = view.findViewById(R.id.fragment_suggestion_business_lastyearrevenuetext_textview);
        mBusinessDebtTextView = view.findViewById(R.id.fragment_suggestion_business_lastyeardebttext_textview);
        mBusinessInvestmentsInTextView = view.findViewById(R.id.fragment_suggestion_business_lastyearinveststext_textview);
        mSuggestionBusinessFinanceBioTextView = view.findViewById(R.id.fragment_suggestion_business_finance_biotext_textview);
        mSuggestionBusinessFinanceFullReportTextView = view.findViewById(R.id.fragment_suggestion_business_finance_fullreporttext_textview);

        // SETTING CLICK LISTENERS
        mSuggestionLoaderImageView.setOnClickListener(this);
        mAnswer1Button.setOnClickListener(this);
        mAnswer2Button.setOnClickListener(this);
        mAnswer3Button.setOnClickListener(this);
        mAnswer4Button.setOnClickListener(this);
        mSuggestionBusinessBuySharesButton.setOnClickListener(this);
        mBusinessWebsiteTextView.setOnClickListener(this);
        mSuggestionBusinessFinanceFullReportTextView.setOnClickListener(this);

        return view;
    }



    @Override
    public void onClick(final View v) {
        // WHEN THE LOAD SUGGESTION FP LOGO IS CLICKED
        if (v.getId() == mSuggestionLoaderImageView.getId() && !networkRequestStarted) {
            mSuggestionLoaderImageView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.suggestion_loading_anim));
            mSuggestionLoaderTextTextView.setText("Getting your next Pott Suggestion...");
            // DELAYING getLatestSuggestion FOR 5S FOR ANIM TO RUN
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLatestSuggestion(getActivity().getApplicationContext());
                }
            }, 5000);
        } else if ((v.getId() == mAnswer1Button.getId() || v.getId() == mAnswer2Button.getId() || v.getId() == mAnswer3Button.getId() || v.getId() == mAnswer4Button.getId()) && !networkRequestStarted) {
            if (drillID.equalsIgnoreCase("")) {
                Config.showToastType1(getActivity(), "Drill error. Please restart the app.");
                return;
            }
            mAnswerHolderConstraintLayout.setVisibility(View.INVISIBLE);
            mAnswersCountScrollView.setVisibility(View.INVISIBLE);
            mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
            mSuggestionLoaderImageView.setVisibility(View.VISIBLE);
            mSuggestionLoaderTextTextView.setVisibility(View.VISIBLE);
            mSuggestionLoaderImageView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.suggestion_loading_anim));
            mSuggestionLoaderTextTextView.setText("Saving drill answer. Increasing your pott intelligence...");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (v.getId() == mAnswer1Button.getId()) {
                        saveDrillAndGetAnswersCount(getActivity().getApplicationContext(), drillID, "1");
                    } else if (v.getId() == mAnswer2Button.getId()) {
                        saveDrillAndGetAnswersCount(getActivity().getApplicationContext(), drillID, "2");
                    } else if (v.getId() == mAnswer3Button.getId()) {
                        saveDrillAndGetAnswersCount(getActivity().getApplicationContext(), drillID, "3");
                    } else if (v.getId() == mAnswer4Button.getId()) {
                        saveDrillAndGetAnswersCount(getActivity().getApplicationContext(), drillID, "4");
                    }
                }
            }, 2500);
        } else if (v.getId() == mBusinessWebsiteTextView.getId() && !businessWebsiteUrl.trim().equalsIgnoreCase("")) {
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, businessWebsiteUrl);
        } else if (v.getId() == mSuggestionBusinessFinanceFullReportTextView.getId() && !businessFullReportUrl.trim().equalsIgnoreCase("")) {
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, businessFullReportUrl);
        } else if (v.getId() == mSuggestionBusinessBuySharesButton.getId() && !businessID.trim().equalsIgnoreCase("")) {
            String[] buyData = {
                    businessID,
                    businessLogoUrl,
                    businessNameGlobal
            };
            Config.openActivity4(getActivity(), BuyBusinessStockSuggestedActivity.class, 0, 0, 1, "BUY_INFO", buyData);
        }
    }


    private void getLatestSuggestion(Context context){
        networkRequestStarted = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mDrillSuggestionHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                }
            });

            AndroidNetworking.post(Config.LINK_GET_MY_SUGGESTION)
                    .addHeaders("Accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("user_pottname", Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("investor_id", Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                    .addBodyParameter("app_type", "ANDROID")
                    .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(getActivity().getApplicationContext())))
                    .addBodyParameter("user_language", LocaleHelper.getLanguage(getActivity()))
                    .setTag("get_suggestion")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    networkRequestStarted = false;

                    try {
                        Log.e("GetSuggestion", response);
                        final JSONObject o = new JSONObject(response);
                        int myStatus = o.getInt("status");
                        final String myStatusMessage = o.getString("message");
                        // DRILL
                        String drillQuestion = "";
                        String drillAnswer1 = "";
                        String drillAnswer2 = "";
                        String drillAnswer3 = "";
                        String drillAnswer4 = "";
                        // BUSINESS
                        String businessName = "";
                        String businessLogo = "";
                        String businessCountry = "";
                        String businessNetworth = "";
                        String businessCountInvestors = "";
                        String businessPitch = "";
                        String businessPitchVideo = "";
                        String businessCEO = "";
                        String businessCOO = "";
                        String businessServicesBio = "";
                        String businessServicesWebsite = "";
                        String businessLastYrRevenue = "";
                        String businessProfitOrLoss = "";
                        String businessInvestments = "";
                        String businessFinanceBio = "";
                        String businessFinanceFullReport = "";


                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                        if(myStatus == 1){
                            if(myStatusMessage.equalsIgnoreCase("drill")){
                                drillID = o.getJSONObject("data").getString("drill_sys_id");
                                drillQuestion = o.getJSONObject("data").getString("drill_question");
                                drillAnswer1 = o.getJSONObject("data").getString("drill_answer_1");
                                drillAnswer2 = o.getJSONObject("data").getString("drill_answer_2");
                                drillAnswer3 = o.getJSONObject("data").getString("drill_answer_3");
                                drillAnswer4 = o.getJSONObject("data").getString("drill_answer_4");
                            } else if(myStatusMessage.equalsIgnoreCase("business")){
                                Config.showDialogType1(getActivity(), "1", "You have a business suggestion. Carefully look at the business profile and if you want to be a part-owner of the business, simply click 'Buy Shares' and buy share ownership", "", null, false, "", "");
                                businessID = o.getJSONObject("data").getString("business_sys_id");
                                businessName = o.getJSONObject("data").getString("business_full_name");
                                businessNameGlobal = businessName;
                                businessLogo = o.getJSONObject("data").getString("business_logo");
                                businessLogoUrl = businessLogo;
                                businessCountry = o.getJSONObject("data").getString("business_country");
                                businessNetworth = o.getJSONObject("data").getString("business_net_worth_usd");
                                businessCountInvestors = o.getJSONObject("data").getString("business_current_shareholders");
                                businessPitch = o.getJSONObject("data").getString("business_pitch_text");
                                businessPitchVideo = o.getJSONObject("data").getString("business_pitch_video");
                                businessCEO = o.getJSONObject("data").getString("business_executive1_firstname") + " " + o.getJSONObject("data").getString("business_executive1_lastname");
                                businessCOO = o.getJSONObject("data").getString("business_executive2_firstname") + " " + o.getJSONObject("data").getString("business_executive2_lastname");
                                businessServicesBio = o.getJSONObject("data").getString("business_descriptive_bio");
                                businessServicesWebsite = o.getJSONObject("data").getString("business_website");
                                businessWebsiteUrl = businessServicesWebsite;
                                businessLastYrRevenue = o.getJSONObject("data").getString("business_lastyr_revenue_usd");
                                businessProfitOrLoss = o.getJSONObject("data").getString("business_lastyr_profit_or_loss_usd");
                                businessInvestments = o.getJSONObject("data").getString("business_investments_amount_needed_usd");
                                businessFinanceBio = o.getJSONObject("data").getString("business_descriptive_financial_bio");
                                //businessFinanceFullReport = "https://docs.google.com/gview?embedded=true&url=https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf"; //o.getJSONObject("data").getString("business_full_financial_report_pdf_url");
                                businessFinanceFullReport = o.getJSONObject("data").getString("business_full_financial_report_pdf_url");
                                businessFullReportUrl = "https://docs.google.com/gview?embedded=true&url=" + businessFinanceFullReport;
                            }

                            if(!getActivity().isFinishing()){
                                final String finalDrillAnswer = drillAnswer1;
                                final String finalDrillQuestion = drillQuestion;
                                final String finalDrillAnswer1 = drillAnswer2;
                                final String finalDrillAnswer2 = drillAnswer3;
                                final String finalDrillAnswer3 = drillAnswer4;

                                final String finalBusinessName = businessName;
                                final String finalBusinessLogoUrl = businessLogo;
                                final String finalBusinessCountry = businessCountry;
                                final String finalBusinessNetworth = businessNetworth;
                                final String finalBusinessCountInvestors = businessCountInvestors;
                                final String finalBusinessPitch = businessPitch;
                                final String finalBusinessPitchVideo = businessPitchVideo;
                                final String finalBusinessCEO = businessCEO;
                                final String finalBusinessCOO = businessCOO;
                                final String finalBusinessServicesBio = businessServicesBio;
                                final String finalBusinessServicesWebsite = businessServicesWebsite;
                                final String finalBusinessLastYrRevenue = businessLastYrRevenue;
                                final String finalBusinessProfitOrLoss = businessProfitOrLoss;
                                final String finalBusinessInvestments = businessInvestments;
                                final String finalBusinessFinanceBio = businessFinanceBio;
                                final String finalBusinessFinanceFullReport = businessFinanceFullReport;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(myStatusMessage.equalsIgnoreCase("drill")){
                                            mSuggestionLoaderImageView.clearAnimation();
                                            mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                                            mSuggestionLoaderTextTextView.setVisibility(View.INVISIBLE);
                                            mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                                            mDrillQuestionTextView.setText(finalDrillQuestion);
                                            mAnswer1Button.setText(finalDrillAnswer);
                                            mAnswer2Button.setText(finalDrillAnswer1);
                                            mAnswer3Button.setText(finalDrillAnswer2);
                                            mAnswer4Button.setText(finalDrillAnswer3);
                                            mDrillSuggestionHolderConstraintLayout.setVisibility(View.VISIBLE);
                                        } else if(myStatusMessage.equalsIgnoreCase("business")){
                                            mSuggestionBusinessNameTextView.setText(finalBusinessName);
                                            Config.loadUrlImage(getActivity(), true, finalBusinessLogoUrl, mBusinessLogoCircleImageView, 0, 60, 60);
                                            mSuggestionBusinessCountryTextView.setText(finalBusinessCountry);
                                            mSuggestionBusinessNetworthTextView.setText(finalBusinessNetworth);
                                            mBusinessCountInvestorsTextView.setText(finalBusinessCountInvestors);
                                            mSuggestionBusinessPitchTextView.setText(finalBusinessPitch);
                                            mBusinessPitchVideoMxVideoPlayerWidget.startPlay(finalBusinessPitchVideo, MxVideoPlayer.SCREEN_LAYOUT_NORMAL, "");
                                            mSuggestionBusinessCEOTextView.setText(finalBusinessCEO);
                                            mSuggestionBusinessCOOTextView.setText(finalBusinessCOO);
                                            mSuggestionBusinessServicesBioTextView.setText(finalBusinessServicesBio);
                                            //mBusinessWebsiteTextView.setText(finalBusinessServicesWebsite);
                                            mBusinessRevenueLastYrTextView.setText(finalBusinessLastYrRevenue);
                                            mBusinessDebtTextView.setText(finalBusinessProfitOrLoss);
                                            mBusinessInvestmentsInTextView.setText(finalBusinessInvestments);
                                            mSuggestionBusinessFinanceBioTextView.setText(finalBusinessFinanceBio);
                                            //mSuggestionBusinessFinanceFullReportTextView.setText(finalBusinessFinanceFullReport);
                                            mSuggestionLoaderImageView.clearAnimation();
                                            mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                                            mSuggestionLoaderTextTextView.setVisibility(View.INVISIBLE);
                                            mDrillSuggestionHolderConstraintLayout.setVisibility(View.INVISIBLE);
                                            mBusinessSuggestionHolderScrollView.setVisibility(View.VISIBLE);
                                        } else {

                                        }
                                    }
                                });
                            }
                        } else if(myStatus == 2){
                            // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                            Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        } else if(myStatus == 3){
                            // GENERAL ERROR
                            mSuggestionLoaderImageView.clearAnimation();
                            mSuggestionLoaderTextTextView.setText("Click the icon to get your next suggestion");
                            Config.showToastType1(getActivity(), myStatusMessage);
                            return;
                        } else if(myStatus == 4){
                            // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                            Config.showToastType1(getActivity(), myStatusMessage);
                            Config.signOutUser(getActivity().getApplicationContext(), true, getActivity(), StartActivity.class, 0, 2);
                        } else if(myStatus == 5){
                            Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Config.showToastType1(getActivity(), getString(R.string.failed_if_this_continues_please_update_your_app));
                        if(!getActivity().isFinishing()){
                            mSuggestionLoaderImageView.clearAnimation();
                            mSuggestionLoaderTextTextView.setText("Click the icon to get your next suggestion");
                        }
                    }
                }

                @Override
                public void onError(ANError anError) {
                    networkRequestStarted = false;
                    Config.showToastType1(getActivity(), getString(R.string.failed_check_your_internet_and_try_again));
                    if(!getActivity().isFinishing()){
                        mSuggestionLoaderImageView.clearAnimation();
                        mSuggestionLoaderTextTextView.setText("Click the icon to get your next suggestion");
                    }
                }
            });
    }


    private void saveDrillAndGetAnswersCount(Context context, String theDrillID, String theAnswerNumber){
        networkRequestStarted = true;

        AndroidNetworking.post(Config.LINK_SAVE_DRILL_AND_GET_ANSWERS_COUNT)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(getActivity().getApplicationContext())))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(getActivity()))
                .addBodyParameter("drill_id", theDrillID)
                .addBodyParameter("drill_answer", theAnswerNumber)
                .setTag("save_drill_answer")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                networkRequestStarted = false;

                try {
                    Log.e("save_drill_answer", response);
                    final JSONObject o = new JSONObject(response);
                    int myStatus = o.getInt("status");
                    final String myStatusMessage = o.getString("message");

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){

                        final String drill_next_one_time = o.getJSONObject("data").getString("drill_next_one_time");
                        final String answer1 = o.getJSONObject("data").getString("answer_1");
                        final String answer2 = o.getJSONObject("data").getString("answer_2");
                        final String answer3 = o.getJSONObject("data").getString("answer_3");
                        final String answer4 = o.getJSONObject("data").getString("answer_4");
                        final String answer1Count = o.getJSONObject("data").getString("answer_1_count");
                        final String answer2Count = o.getJSONObject("data").getString("answer_2_count");
                        final String answer3Count = o.getJSONObject("data").getString("answer_3_count");
                        final String answer4Count = o.getJSONObject("data").getString("answer_4_count");

                        Config.showToastType1(getActivity(), myStatusMessage);
                        if(!getActivity().isFinishing()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mSuggestionLoaderImageView.clearAnimation();
                                    mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                                    mSuggestionLoaderTextTextView.setVisibility(View.INVISIBLE);
                                    mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                                    mNextDrillTextView.setText(drill_next_one_time);
                                    mAnswer1CountTextView.setText(answer1Count);
                                    mAnswer1TextView.setText(answer1);
                                    mAnswer2CountTextView.setText(answer2Count);
                                    mAnswer2TextView.setText(answer2);
                                    mAnswer3CountTextView.setText(answer3Count);
                                    mAnswer3TextView.setText(answer3);
                                    mAnswer4CountTextView.setText(answer4Count);
                                    mAnswer4TextView.setText(answer4);
                                    mAnswersCountScrollView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mSuggestionLoaderImageView.clearAnimation();
                        mSuggestionLoaderTextTextView.setText("Click the icon to get your next suggestion");
                        Config.showToastType1(getActivity(), myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(getActivity(), myStatusMessage);
                        Config.signOutUser(getActivity().getApplicationContext(), true, getActivity(), StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(getActivity(), getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!getActivity().isFinishing()){
                        mAnswersCountScrollView.setVisibility(View.INVISIBLE);
                        mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                        mSuggestionLoaderImageView.clearAnimation();
                        mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                        mSuggestionLoaderTextTextView.setVisibility(View.INVISIBLE);
                        mAnswerHolderConstraintLayout.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(getActivity(), getString(R.string.failed_check_your_internet_and_try_again));
                if(!getActivity().isFinishing()){
                    mAnswersCountScrollView.setVisibility(View.INVISIBLE);
                    mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                    mSuggestionLoaderImageView.clearAnimation();
                    mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                    mSuggestionLoaderTextTextView.setVisibility(View.INVISIBLE);
                    mAnswerHolderConstraintLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}