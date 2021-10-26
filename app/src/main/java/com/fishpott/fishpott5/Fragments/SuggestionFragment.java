package com.fishpott.fishpott5.Fragments;

import static com.fishpott.fishpott5.Activities.MainActivity.mNotificationMenuIconUpdateIconConstraintLayout;
import static com.fishpott.fishpott5.Fragments.NotificationsFragment.mNotificationsRecyclerView;

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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.FlaggedAccountActivity;
import com.fishpott.fishpott5.Activities.FullNewsActivity;
import com.fishpott.fishpott5.Activities.GovernmentIDVerificationActivity;
import com.fishpott.fishpott5.Activities.MainActivity;
import com.fishpott.fishpott5.Activities.ProfileOfDifferentPottActivity;
import com.fishpott.fishpott5.Activities.SetProfilePictureActivity;
import com.fishpott.fishpott5.Activities.StartActivity;
import com.fishpott.fishpott5.Activities.UpdateActivity;
import com.fishpott.fishpott5.Adapters.Notifications_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Notifications_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.Notification_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestionFragment extends Fragment implements View.OnClickListener {

    private ConstraintLayout mDrillSuggestionHolderConstraintLayout;
    private ScrollView mBusinessSuggestionHolderScrollView;
    private TextView mDrillQuestionTextView, mSuggestionLoaderTextTextView, mSuggestionBusinessNameTextView, mSuggestionBusinessCountryTextView, mSuggestionBusinessNetworthTextView,
            mBusinessCountInvestorsTextView, mSuggestionBusinessPitchTextView, mSuggestionBusinessCEOTextView, mSuggestionBusinessCOOTextView, mSuggestionBusinessServicesBioTextView,
            mBusinessWebsiteTextView, mBusinessRevenueLastYrTextView, mBusinessDebtTextView, mBusinessInvestmentsInTextView, mSuggestionBusinessFinanceBioTextView, mSuggestionBusinessFinanceFullReportTextView;
    private CircleImageView mBusinessLogoCircleImageView;
    private WebView mBusinessPitchVideoWebView;
    private Button mAnswer1Button, mAnswer2Button, mAnswer3Button, mAnswer4Button;
    private ImageView mSuggestionLoaderImageView;
    private Boolean getSuggestionStarted = false;
    private String drillID = "", businessID = "";

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
        mDrillQuestionTextView = view.findViewById(R.id.fragment_suggestion_question_textview);
        mAnswer1Button = view.findViewById(R.id.fragment_suggestion_answer1_button);
        mAnswer2Button = view.findViewById(R.id.fragment_suggestion_answer2_button);
        mAnswer3Button = view.findViewById(R.id.fragment_suggestion_answer3_button);
        mAnswer4Button = view.findViewById(R.id.fragment_suggestion_answer4_button);

        // BUSINESS SUGGESTION OBJECTS
        mBusinessSuggestionHolderScrollView = view.findViewById(R.id.fragment_suggestion_business_holder_constraintlayout);
        mSuggestionBusinessNameTextView = view.findViewById(R.id.fragment_suggestion_business_name_textview);
        mBusinessLogoCircleImageView = view.findViewById(R.id.fragment_suggestion_business_logo_textview);
        mSuggestionBusinessCountryTextView = view.findViewById(R.id.fragment_suggestion_business_country_textview);
        mSuggestionBusinessNetworthTextView = view.findViewById(R.id.fragment_suggestion_business_networthvalue_textview);
        mBusinessCountInvestorsTextView = view.findViewById(R.id.fragment_suggestion_business_investorsvalue_textview);
        mSuggestionBusinessPitchTextView = view.findViewById(R.id.fragment_suggestion_business_pitchtext_textview);
        mBusinessPitchVideoWebView = view.findViewById(R.id.fragment_suggestion_business_pitchvideo_webview);
        mSuggestionBusinessCEOTextView = view.findViewById(R.id.fragment_suggestion_business_ceotext_textview);
        mSuggestionBusinessCOOTextView = view.findViewById(R.id.fragment_suggestion_business_cootext_textview);
        mSuggestionBusinessServicesBioTextView = view.findViewById(R.id.fragment_suggestion_business_servicetext_textview);
        mBusinessWebsiteTextView = view.findViewById(R.id.fragment_suggestion_business_servicewebsite_textview);
        mBusinessRevenueLastYrTextView = view.findViewById(R.id.fragment_suggestion_business_lastyearrevenuelabel_textview);
        mBusinessDebtTextView = view.findViewById(R.id.fragment_suggestion_business_lastyeardebtlabel_textview);
        mBusinessInvestmentsInTextView = view.findViewById(R.id.fragment_suggestion_business_lastyearinvestslabel_textview);
        mSuggestionBusinessFinanceBioTextView = view.findViewById(R.id.fragment_suggestion_business_finance_biotext_textview);
        mSuggestionBusinessFinanceFullReportTextView = view.findViewById(R.id.fragment_suggestion_business_finance_fullreporttext_textview);

        // SETTING CLICK LISTENERS
        mSuggestionLoaderImageView.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        // WHEN THE LOAD SUGGESTION FP LOGO IS CLICKED
        if(v.getId() == R.id.fragment_suggestion_loader_imageview && !getSuggestionStarted){

            mSuggestionLoaderImageView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.suggestion_loading_anim));
            mSuggestionLoaderTextTextView.setText("Getting your next Pott Suggestion...");
            // DELAYING getLatestSuggestion FOR 5S FOR ANIM TO RUN
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLatestSuggestion(getActivity().getApplicationContext());
                }
            }, 5000);
        }
    }


    private void getLatestSuggestion(Context context){
        getSuggestionStarted = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mDrillSuggestionHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                }
            });

            String final_url = Config.LINK_GET_MY_SUGGESTION + "?user_phone_number=" + Uri.encode(Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)) + "&user_pottname=" + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME) + "&investor_id=" + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID) + "&app_type=ANDROID&app_version_code=" + String.valueOf(Config.getAppVersionCode(getActivity().getApplicationContext())) + "&user_language=" + LocaleHelper.getLanguage(getActivity());


        Log.e("GetSuggestion", final_url);
            AndroidNetworking.get(final_url)
                    .addHeaders("Accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .setTag("get_suggestion")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    getSuggestionStarted = false;

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
                        String businessLogoUrl = "";
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

                        if(myStatusMessage.equalsIgnoreCase("drill")){
                            drillID = o.getJSONObject("data").getString("drill_sys_id");
                            drillQuestion = o.getJSONObject("data").getString("drill_question");
                            drillAnswer1 = o.getJSONObject("data").getString("drill_answer_1");
                            drillAnswer2 = o.getJSONObject("data").getString("drill_answer_2");
                            drillAnswer3 = o.getJSONObject("data").getString("drill_answer_3");
                            drillAnswer4 = o.getJSONObject("data").getString("drill_answer_4");
                        } else if(myStatusMessage.equalsIgnoreCase("business")){
                            businessID = o.getJSONObject("data").getString("business_sys_id");
                            businessName = o.getJSONObject("data").getString("business_full_name");
                            businessLogoUrl = o.getJSONObject("data").getString("business_logo");
                            businessCountry = o.getJSONObject("data").getString("business_country");
                            businessNetworth = o.getJSONObject("data").getString("business_net_worth_usd");
                            businessCountInvestors = o.getJSONObject("data").getString("business_current_shareholders");
                            businessPitch = o.getJSONObject("data").getString("business_pitch_text");
                            businessPitchVideo = o.getJSONObject("data").getString("business_pitch_video");
                            businessCEO = o.getJSONObject("data").getString("business_executive1_firstname") + " " + o.getJSONObject("data").getString("business_executive1_lastname");
                            businessCOO = o.getJSONObject("data").getString("business_executive2_firstname") + " " + o.getJSONObject("data").getString("business_executive2_lastname");
                            businessServicesBio = o.getJSONObject("data").getString("business_descriptive_bio");
                            businessServicesWebsite = o.getJSONObject("data").getString("business_website");
                            businessLastYrRevenue = o.getJSONObject("data").getString("business_lastyr_revenue_usd");
                            businessProfitOrLoss = o.getJSONObject("data").getString("business_lastyr_profit_or_loss_usd");
                            businessInvestments = o.getJSONObject("data").getString("business_investments_amount_needed_usd");
                            businessFinanceBio = o.getJSONObject("data").getString("business_descriptive_financial_bio");
                            businessFinanceFullReport = o.getJSONObject("data").getString("business_full_financial_report_pdf_url");
                        }


                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                        if(myStatus == 1){

                            if(MyLifecycleHandler.isApplicationInForeground()){
                                final String finalDrillAnswer = drillAnswer1;
                                final String finalDrillQuestion = drillQuestion;
                                final String finalDrillAnswer1 = drillAnswer2;
                                final String finalDrillAnswer2 = drillAnswer3;
                                final String finalDrillAnswer3 = drillAnswer4;

                                final String finalBusinessName = businessName;
                                final String finalBusinessLogoUrl = businessLogoUrl;
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
                                            //mBusinessPitchVideoWebView.setText(finalBusinessPitchVideo);
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
                            Config.signOutUser(getActivity().getApplicationContext(), false, null, null, 0, 2);
                        } else if(myStatus == 5){
                            Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
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
                    getSuggestionStarted = false;
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