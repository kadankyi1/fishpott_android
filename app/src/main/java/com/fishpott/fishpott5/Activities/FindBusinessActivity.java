package com.fishpott.fishpott5.Activities;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;

public class FindBusinessActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mBusinessCodeEditText;
    ImageView mSearchImageView;
    private ScrollView mBusinessSuggestionHolderScrollView;
    private TextView mSuggestionLoaderTextTextView, mSuggestionBusinessNameTextView, mSuggestionBusinessCountryTextView, mSuggestionBusinessNetworthTextView,
            mBusinessCountInvestorsTextView, mSuggestionBusinessPitchTextView, mSuggestionBusinessCEOTextView, mSuggestionBusinessCOOTextView, mSuggestionBusinessServicesBioTextView,
            mBusinessWebsiteTextView, mBusinessRevenueLastYrTextView, mBusinessDebtTextView, mBusinessInvestmentsInTextView, mSuggestionBusinessFinanceBioTextView,
            mSuggestionBusinessFinanceFullReportTextView;
    private CircleImageView mBusinessLogoCircleImageView;
    private MxVideoPlayerWidget mBusinessPitchVideoMxVideoPlayerWidget;
    private ImageView mSuggestionLoaderImageView;
    private Boolean networkRequestStarted = false;
    private Button mSuggestionBusinessBuySharesButton;
    private String businessID = "", businessNameGlobal = "", businessLogoUrl = "", businessWebsiteUrl = "", businessFullReportUrl = "", canBuy = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_business);

        // SEARCH FORM
        mBusinessCodeEditText = findViewById(R.id.search_edittext);
        mSearchImageView = findViewById(R.id.title_bar_search_icon_imageview);

        // LOADER
        mSuggestionLoaderImageView = findViewById(R.id.fragment_suggestion_loader_imageview);
        mSuggestionLoaderTextTextView = findViewById(R.id.fragment_suggestion_loadertext_textview);

        // BUSINESS SUGGESTION OBJECTS
        mBusinessSuggestionHolderScrollView = findViewById(R.id.fragment_suggestion_business_holder_constraintlayout);
        mSuggestionBusinessNameTextView = findViewById(R.id.fragment_suggestion_business_name_textview);
        mBusinessLogoCircleImageView = findViewById(R.id.fragment_suggestion_business_logo_textview);
        mSuggestionBusinessCountryTextView = findViewById(R.id.fragment_suggestion_business_country_textview);
        mSuggestionBusinessBuySharesButton = findViewById(R.id.buyshares_button_pp_dialog);
        mSuggestionBusinessNetworthTextView = findViewById(R.id.fragment_suggestion_business_networthvalue_textview);
        mBusinessCountInvestorsTextView = findViewById(R.id.fragment_suggestion_business_investorsvalue_textview);
        mSuggestionBusinessPitchTextView = findViewById(R.id.fragment_suggestion_business_pitchtext_textview);
        mBusinessPitchVideoMxVideoPlayerWidget = findViewById(R.id.mpw_video_player);
        mSuggestionBusinessCEOTextView = findViewById(R.id.fragment_suggestion_business_ceotext_textview);
        mSuggestionBusinessCOOTextView = findViewById(R.id.fragment_suggestion_business_cootext_textview);
        mSuggestionBusinessServicesBioTextView = findViewById(R.id.fragment_suggestion_business_servicetext_textview);
        mBusinessWebsiteTextView = findViewById(R.id.fragment_suggestion_business_servicewebsite_textview);
        mBusinessRevenueLastYrTextView = findViewById(R.id.fragment_suggestion_business_lastyearrevenuetext_textview);
        mBusinessDebtTextView = findViewById(R.id.fragment_suggestion_business_lastyeardebttext_textview);
        mBusinessInvestmentsInTextView = findViewById(R.id.fragment_suggestion_business_lastyearinveststext_textview);
        mSuggestionBusinessFinanceBioTextView = findViewById(R.id.fragment_suggestion_business_finance_biotext_textview);
        mSuggestionBusinessFinanceFullReportTextView = findViewById(R.id.fragment_suggestion_business_finance_fullreporttext_textview);

        // SETTING CLICK LISTENERS
        //mSuggestionLoaderImageView.setOnClickListener(this);
        mSuggestionBusinessBuySharesButton.setOnClickListener(this);
        mBusinessWebsiteTextView.setOnClickListener(this);
        mSuggestionBusinessFinanceFullReportTextView.setOnClickListener(this);
        mSearchImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mSearchImageView.getId() && !mBusinessCodeEditText.getText().toString().trim().equalsIgnoreCase("")){
            mSuggestionLoaderImageView.startAnimation(AnimationUtils.loadAnimation(FindBusinessActivity.this, R.anim.suggestion_loading_anim));
            mSuggestionLoaderTextTextView.setText("Finding the business...");
            // DELAYING getLatestSuggestion FOR 5S FOR ANIM TO RUN
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLatestSuggestion(getApplicationContext());
                }
            }, 5000);
        } else if (v.getId() == mBusinessWebsiteTextView.getId() && !businessWebsiteUrl.trim().equalsIgnoreCase("")) {
            Config.openActivity(FindBusinessActivity.this, WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, businessWebsiteUrl);
        } else if (v.getId() == mSuggestionBusinessFinanceFullReportTextView.getId() && !businessFullReportUrl.trim().equalsIgnoreCase("")) {
            Config.openActivity(FindBusinessActivity.this, WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, businessFullReportUrl);
        } else if (v.getId() == mSuggestionBusinessBuySharesButton.getId() && !businessID.trim().equalsIgnoreCase("") && canBuy.trim().equalsIgnoreCase("yes")) {
            String[] buyData = {
                    businessID,
                    businessLogoUrl,
                    businessNameGlobal
            };
            Config.openActivity4(FindBusinessActivity.this, BuyBusinessStockSuggestedActivity.class, 0, 0, 1, "BUY_INFO", buyData);
        }
    }


    private void getLatestSuggestion(Context context){
        networkRequestStarted = true;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
            }
        });

        String final_url = Config.LINK_GET_MY_SUGGESTION + "?user_phone_number=" + Uri.encode(Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)) + "&user_pottname=" + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME) + "&investor_id=" + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID) + "&app_type=ANDROID&app_version_code=" + String.valueOf(Config.getAppVersionCode(getApplicationContext())) + "&user_language=" + LocaleHelper.getLanguage(FindBusinessActivity.this);


        Log.e("GetSuggestion", final_url);
        AndroidNetworking.get(final_url)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
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
                    final String investMessage = o.getString("invest_message");
                    canBuy = o.getString("can_buy");
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
                    if(myStatusMessage.equalsIgnoreCase("business")){
                        Config.showDialogType1(FindBusinessActivity.this, "1", investMessage, "", null, false, "", "");
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


                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){

                        if(MyLifecycleHandler.isApplicationInForeground()){
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
                                    if(myStatusMessage.equalsIgnoreCase("business")){
                                        mSuggestionBusinessNameTextView.setText(finalBusinessName);
                                        Config.loadUrlImage(FindBusinessActivity.this, true, finalBusinessLogoUrl, mBusinessLogoCircleImageView, 0, 60, 60);
                                        mSuggestionBusinessCountryTextView.setText(finalBusinessCountry);
                                        mSuggestionBusinessNetworthTextView.setText(finalBusinessNetworth);
                                        mBusinessCountInvestorsTextView.setText(finalBusinessCountInvestors);
                                        mSuggestionBusinessPitchTextView.setText(finalBusinessPitch);
                                        mBusinessPitchVideoMxVideoPlayerWidget.startPlay(finalBusinessPitchVideo, MxVideoPlayer.SCREEN_LAYOUT_NORMAL, "");
                                        mSuggestionBusinessCEOTextView.setText(finalBusinessCEO);
                                        mSuggestionBusinessCOOTextView.setText(finalBusinessCOO);
                                        mSuggestionBusinessServicesBioTextView.setText(finalBusinessServicesBio);
                                        mBusinessWebsiteTextView.setText(finalBusinessServicesWebsite);
                                        mBusinessRevenueLastYrTextView.setText(finalBusinessLastYrRevenue);
                                        mBusinessDebtTextView.setText(finalBusinessProfitOrLoss);
                                        mBusinessInvestmentsInTextView.setText(finalBusinessInvestments);
                                        mSuggestionBusinessFinanceBioTextView.setText(finalBusinessFinanceBio);
                                        mSuggestionBusinessFinanceFullReportTextView.setText(finalBusinessFinanceFullReport);
                                        mSuggestionLoaderImageView.clearAnimation();
                                        mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                                        mSuggestionLoaderTextTextView.setVisibility(View.INVISIBLE);
                                        mBusinessSuggestionHolderScrollView.setVisibility(View.VISIBLE);
                                    } else {

                                    }
                                }
                            });
                        }
                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mSuggestionLoaderImageView.clearAnimation();
                        mSuggestionLoaderTextTextView.setText("Click the icon to get your next suggestion");
                        Config.showToastType1(FindBusinessActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(FindBusinessActivity.this, myStatusMessage);
                        Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(MyLifecycleHandler.isApplicationInForeground()){
                            /*
                            ADD XML TO FRONT SO USER CAN CLICK TO TRY AGAIN IF IT FAILS
                             */
                    } else {
                        //networkResponse = getString(R.string.login_activity_an_unexpected_error_occured);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                if(MyLifecycleHandler.isApplicationInForeground()){
                            /*
                            ADD XML TO FRONT SO USER CAN CLICK TO TRY AGAIN IF IT FAILS
                             */
                } else {
                    //networkResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                }
            }
        });
    }
}