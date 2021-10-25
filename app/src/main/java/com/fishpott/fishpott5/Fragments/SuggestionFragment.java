package com.fishpott.fishpott5.Fragments;

import static com.fishpott.fishpott5.Activities.MainActivity.mNotificationMenuIconUpdateIconConstraintLayout;
import static com.fishpott.fishpott5.Fragments.NotificationsFragment.mNotificationsRecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestionFragment extends Fragment {

    private ConstraintLayout mDrillSuggestionHolderConstraintLayout
    private ScrollView mBusinessSuggestionHolderScrollView;
    private TextView mDrillQuestionTextView;
    private Button mAnswer1Button, mAnswer2Button, mAnswer3Button, mAnswer4Button;
    private ProgressBar mSuggestionLoaderProgressBar;

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
        mSuggestionLoaderProgressBar = view.findViewById(R.id.fragment_suggestion_loader);

        // DRILL SUGGESTION OBJECTS
        mDrillSuggestionHolderConstraintLayout = view.findViewById(R.id.fragment_suggestion_drill_holder_constraintlayout);
        mAnswer1Button = view.findViewById(R.id.fragment_suggestion_answer1_button);
        mAnswer2Button = view.findViewById(R.id.fragment_suggestion_answer2_button);
        mAnswer3Button = view.findViewById(R.id.fragment_suggestion_answer3_button);
        mAnswer4Button = view.findViewById(R.id.fragment_suggestion_answer4_button);

        // BUSINESS SUGGESTION OBJECTS
        mBusinessSuggestionHolderScrollView = view.findViewById(R.id.fragment_suggestion_business_holder_constraintlayout);

        return view;
    }


    private void startSharesTransfer(Context context, final String receiverPottName){

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mDrillSuggestionHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mBusinessSuggestionHolderScrollView.setVisibility(View.INVISIBLE);
                    mSuggestionLoaderProgressBar.setVisibility(View.VISIBLE);
                }
            });

            AndroidNetworking.post(Config.LINK_TRANSFER_SHARES)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("added_item_id", chosenSharesInfo[0])
                    .addBodyParameter("added_item_quantity", chosenSharesInfo[2])
                    .addBodyParameter("added_item_price", chosenSharesInfo[3])
                    .addBodyParameter("myrawpass", chosenSharesInfo[4])
                    .addBodyParameter("receiver_pottname", receiverPottName)
                    .addBodyParameter("language", LocaleHelper.getLanguage(getActivity()))
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("transfer_my_shares")
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
                        networkResponse = statusMsg;

                        if(myStatus != 1){
                            if(MyLifecycleHandler.isApplicationInForeground()){
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                                        mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getActivity().getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(getActivity(), statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(getActivity(), statusMsg);
                            Config.signOutUser(getActivity().getApplicationContext(), false, null, null, 0, 2);
                        }

                        if(myStatus == 4){
                            Config.showToastType1(getActivity(), statusMsg);
                            Config.signOutUser(getActivity().getApplicationContext(), false, null, null, 0, 2);
                        }

                        if(myStatus == 5){
                            Config.showToastType1(getActivity(), getString(R.string.incorrect_password));
                            return;
                        }

                        if(myStatus == 6){
                            Config.showToastType1(getActivity(), getString(R.string.insufficient_shares));
                            return;
                        }

                        if(myStatus == 7){
                            Config.showToastType1(getActivity(), getString(R.string.receiver_pott_not_found));
                            return;
                        }

                        if(myStatus == 8){

                            Config.showDialogType1(getActivity(), "1", getString(R.string.process_incomplete_contact_fishpot_llc_and_report_the_issue_transfer_with_share_id) + statusMsg, "", null, false, "", "");
                            return;
                        }

                        if(myStatus == 9){
                            Config.showDialogType1(getActivity(), "1", getString(R.string.you_cannot_transfer_shares_until_after_a_week_of_ownership) + statusMsg, "", null, false, "", "");
                            return;
                        }


                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if (myStatus == 1) {
                            mSharesInfoTextView.setText("");
                            mTransferFeeTextView.setText("");
                            transferThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    NewsFetcherAndPreparerService.fetchMyShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                                }
                            });
                            transferThread.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fetchMySharesInfo(false);
                                    if(MyLifecycleHandler.isApplicationInForeground()) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSharesInfoTextView.setText("");
                                                mTransferFeeTextView.setText("");
                                                mPricePerShareEditText.setText("");
                                                mQuantityToSellEditText.setText("");
                                                mPasswordEditText.setText("");
                                                mPricePerShareTextInputLayout.setHint(getResources().getString(R.string.receiver_pott_name));
                                                mQuantityToSellTextInputLayout.setHint(getResources().getString(R.string.quantity_to_transfer));

                                            }
                                        });
                                    } else {
                                        networkResponse = getString(R.string.transfer_successful);
                                    }
                                    Config.setUserNotification(getActivity().getApplicationContext(), String.valueOf(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER), getString(R.string.transfer_successful), getString(R.string.your_transfer_has_completed_successfully), 1, R.drawable.notification_icon);
                                    Notifications_DatabaseAdapter notifications_databaseAdapter = new Notifications_DatabaseAdapter(getActivity().getApplicationContext());
                                    // OPENING THE STORIES DATABASE
                                    notifications_databaseAdapter.openDatabase();
                                    long rowId = notifications_databaseAdapter.insertRow(
                                            Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER,
                                            "",
                                            Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME),
                                            0,
                                            "f",
                                            getString(R.string.your_transfer_of) + " " + chosenSharesInfo[2] + " " + chosenSharesInfo[1] + " " + getString(R.string.to) + " " + getString(R.string.fragment_signup_personalstage3_at) + receiverPottName + " " + getString(R.string.has_completed_successfully),
                                            Config.getCurrentDateTime3("MMM d, yyyy"),
                                            ""
                                    );
                                    notifications_databaseAdapter.closeDatabase();
                                    notifications_databaseAdapter = null;

                                    Notification_Model notification_model = new Notification_Model();
                                    notification_model.setRowId(rowId);
                                    notification_model.setNotificationType(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER);
                                    notification_model.setRelevantId_1("");
                                    notification_model.setRelevantId_2(Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
                                    notification_model.setRelevantId_3("");
                                    notification_model.setReadStatus(0);
                                    notification_model.setPottPic("f");
                                    notification_model.setNotificationMessage(getString(R.string.your_transfer_of) + " " + chosenSharesInfo[2] + " " + chosenSharesInfo[1] + " " + getString(R.string.to) + " " + getString(R.string.fragment_signup_personalstage3_at) + receiverPottName + " " + getString(R.string.has_completed_successfully));
                                    notification_model.setNotificationDate(Config.getCurrentDateTime3("MMM d, yyyy"));
                                    Notifications_ListDataGenerator.addOneDataToDesiredPosition(0, notification_model);


                                    if (MyLifecycleHandler.getCurrentActivity() != null && MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mNotificationMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                                mNotificationMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.main_activity_onclick_icon_anim));
                                                mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
                                            }
                                        });

                                    }
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                                            mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                                            if (MyLifecycleHandler.isApplicationVisible()) {
                                                Config.showDialogType1(getActivity(), "1", getString(R.string.transfer_successful), "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                                            }
                                        }
                                    });
                                }
                            }, 6000);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(MyLifecycleHandler.isApplicationInForeground()){
                            mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                            mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                        } else {
                            networkResponse = getString(R.string.login_activity_an_unexpected_error_occured);
                        }
                    }
                }

                @Override
                public void onError(ANError anError) {
                    if(MyLifecycleHandler.isApplicationInForeground()){
                        mStage2ConstraintLayout.setVisibility(View.INVISIBLE);
                        mStage1ConstraintLayout.setVisibility(View.VISIBLE);
                    } else {
                        networkResponse = getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                    }
                }
            });
    }
}