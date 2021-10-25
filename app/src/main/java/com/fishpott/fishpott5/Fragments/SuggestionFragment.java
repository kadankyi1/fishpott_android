package com.fishpott.fishpott5.Fragments;

import static com.fishpott.fishpott5.Activities.MainActivity.mNotificationMenuIconUpdateIconConstraintLayout;
import static com.fishpott.fishpott5.Fragments.NotificationsFragment.mNotificationsRecyclerView;

import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.MainActivity;
import com.fishpott.fishpott5.Activities.UpdateActivity;
import com.fishpott.fishpott5.Adapters.Notifications_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Notifications_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.Notification_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

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
    private TextView mDrillQuestionTextView, mSuggestionLoaderTextTextView;
    private Button mAnswer1Button, mAnswer2Button, mAnswer3Button, mAnswer4Button;
    private ImageView mSuggestionLoaderImageView;
    private Boolean getSuggestionStarted = false;

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
        mAnswer1Button = view.findViewById(R.id.fragment_suggestion_answer1_button);
        mAnswer2Button = view.findViewById(R.id.fragment_suggestion_answer2_button);
        mAnswer3Button = view.findViewById(R.id.fragment_suggestion_answer3_button);
        mAnswer4Button = view.findViewById(R.id.fragment_suggestion_answer4_button);

        // BUSINESS SUGGESTION OBJECTS
        mBusinessSuggestionHolderScrollView = view.findViewById(R.id.fragment_suggestion_business_holder_constraintlayout);

        // SETTING CLICK LISTENERS
        mSuggestionLoaderImageView.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        // WHEN THE LOAD SUGGESTION FP LOGO IS CLICKED
        if(v.getId() == R.id.fragment_suggestion_loader_imageview && !getSuggestionStarted){
            getLatestSuggestion(getActivity().getApplicationContext());
        }
    }


    private void getLatestSuggestion(Context context){
        getSuggestionStarted = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mDrillSuggestionHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                    mSuggestionLoaderImageView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.suggestion_loading_anim));
                    mSuggestionLoaderTextTextView.setText("Getting your next Pott Suggestion...");
                }
            });

        String final_url = null;
        try {
            final_url = URLEncoder.encode(Config.LINK_GET_MY_SUGGESTION + "?user_phone_number=" + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE) + "&user_pottname=" + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME + "&investor_id=" + Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID) + "&app_type=ANDROID&app_version_code=" + String.valueOf(Config.getAppVersionCode(getActivity().getApplicationContext())) + "&user_language=" + LocaleHelper.getLanguage(getActivity())), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            mSuggestionLoaderImageView.clearAnimation();
            mSuggestionLoaderTextTextView.setText("Oops.. Failed to get suggestion");
        }

        Log.e("GetSuggestion", final_url);
            AndroidNetworking.get(final_url)
                    .setTag("get_suggestion")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    getSuggestionStarted = false;

                    try {
                        Log.e("GetSuggestion", response);
                        JSONObject o = new JSONObject(response);
                        int myStatus = o.getInt("status");
                        final String myStatusMessage = o.getString("message");

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                        if(myStatus == 1){
                            if(MyLifecycleHandler.isApplicationInForeground()){
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(myStatusMessage.equalsIgnoreCase("drill")){
                                            mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                                            mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                                            mDrillSuggestionHolderConstraintLayout.setVisibility(View.VISIBLE);
                                        } else if(myStatusMessage.equalsIgnoreCase("business")){
                                            mSuggestionLoaderImageView.setVisibility(View.INVISIBLE);
                                            mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                                            mDrillSuggestionHolderConstraintLayout.setVisibility(View.VISIBLE);
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