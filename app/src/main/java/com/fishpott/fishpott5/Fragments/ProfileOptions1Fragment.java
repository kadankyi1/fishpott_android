package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Activities.MessengerActivity;
import com.fishpott.fishpott5.Activities.UpdateActivity;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileOptions1Fragment extends Fragment {
    private static final String NEWS_ID = "NEWS_ID";

    private String currPottName = "";
    private ProgressBar mLoaderProgressBar;
    private TextView  mShieldPottTextView, mShieldPottInfoTextTextView, mBlockPottTextTextView, mBlockPottInfoTextTextView;
    private Boolean REQUEST_HAS_STARTED_2 = false;

    public ProfileOptions1Fragment() {
        // Required empty public constructor
    }

    public static ProfileOptions1Fragment newInstance(String pottName) {
        ProfileOptions1Fragment fragment = new ProfileOptions1Fragment();
        Bundle args = new Bundle();
        args.putString(NEWS_ID, pottName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currPottName = getArguments().getString(NEWS_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_options1, container, false);

        mLoaderProgressBar = (ProgressBar) view.findViewById(R.id.profile_options_fragment_pott_options);
        mShieldPottTextView = (TextView) view.findViewById(R.id.copy_news_link_textview);
        mShieldPottInfoTextTextView = (TextView) view.findViewById(R.id.copy_news_link_info_textview);
        mBlockPottTextTextView = (TextView) view.findViewById(R.id.report_news_textview);
        mBlockPottInfoTextTextView = (TextView) view.findViewById(R.id.report_news_info_textview);

        mShieldPottTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shieldPott(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
            }
        });

        mShieldPottInfoTextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shieldPott(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
            }
        });

        mBlockPottTextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockPott(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
            }
        });

        mBlockPottInfoTextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockPott(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
            }
        });

        return view;
    }

    public void shieldPott(final Context context, final String language){
        if(!REQUEST_HAS_STARTED_2){
            REQUEST_HAS_STARTED_2 = true;
            mLoaderProgressBar.setVisibility(View.VISIBLE);
            AndroidNetworking.post(Config.LINK_SHIELD_POTT)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("victim_pottname", currPottName)
                    .addBodyParameter("language", language)
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("shield_pott")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    if (MyLifecycleHandler.isApplicationVisible() && mLoaderProgressBar != null) {
                        Log.e("shieldPott", "response : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("data_returned");

                            JSONObject o = array.getJSONObject(0);
                            int myStatus = o.getInt("1");
                            String statusMsg = o.getString("2");

                            // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                            if (myStatus == 2) {
                                Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                                Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                                return;
                            }

                            // GENERAL ERROR
                            if (myStatus == 3) {
                                Config.showToastType1(getActivity(), statusMsg);
                                return;
                            }

                            // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                            if (myStatus == 4) {
                                Config.showToastType1(getActivity(), statusMsg);
                                Config.signOutUser(context, false, null, null, 0, 2);
                            }

                            //STORING THE USER DATA
                            Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                            // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                            Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                            Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                            Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                            if (myStatus == 1) {
                                Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_PEARLS, o.getString("8"));
                                Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEBIT_WALLET, o.getString("9"));
                                Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET, o.getString("10"));
                                Config.showDialogType1(getActivity(), "1", statusMsg, "", null, false, "Okay", "");
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        REQUEST_HAS_STARTED_2 = false;
                        mLoaderProgressBar.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onError(ANError anError) {
                    REQUEST_HAS_STARTED_2 = false;
                    if (MyLifecycleHandler.isApplicationVisible() && mLoaderProgressBar != null) {
                        mLoaderProgressBar.setVisibility(View.GONE);
                        Config.showToastType1(getActivity(), getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                    }
                }
            });
        } else {
            Config.showToastType1(getActivity(), getResources().getString(R.string.please_wait_for_the_pending_process_to_complete));
        }
    }



    public void blockPott(final Context context, final String language){
        if(!REQUEST_HAS_STARTED_2){
            REQUEST_HAS_STARTED_2 = true;
            mLoaderProgressBar.setVisibility(View.VISIBLE);
            AndroidNetworking.post(Config.LINK_BLOCK_POTT)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("victim_pottname", currPottName)
                    .addBodyParameter("language", language)
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("shield_pott")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    Log.e("blockPott", "response : " + response);
                    if (MyLifecycleHandler.isApplicationVisible() && mLoaderProgressBar != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("data_returned");

                            JSONObject o = array.getJSONObject(0);
                            int myStatus = o.getInt("1");
                            String statusMsg = o.getString("2");

                            // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                            if (myStatus == 2) {
                                Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                                Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                                return;
                            }

                            // GENERAL ERROR
                            if (myStatus == 3) {
                                Config.showToastType1(getActivity(), statusMsg);
                                return;
                            }

                            // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                            if (myStatus == 4) {
                                Config.showToastType1(getActivity(), statusMsg);
                                Config.signOutUser(context, false, null, null, 0, 2);
                            }

                            //STORING THE USER DATA
                            Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                            // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                            Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                            Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                            Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                            if (myStatus == 1) {
                                Config.showDialogType1(getActivity(), "1", o.getString("8"), "", null, false, "Okay", "");
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        REQUEST_HAS_STARTED_2 = false;
                        mLoaderProgressBar.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onError(ANError anError) {
                    if (MyLifecycleHandler.isApplicationVisible() && mLoaderProgressBar != null) {
                        REQUEST_HAS_STARTED_2 = false;
                        mLoaderProgressBar.setVisibility(View.GONE);
                        Config.showToastType1(getActivity(), getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                    }
                }
            });
        } else {
            Config.showToastType1(getActivity(), getResources().getString(R.string.please_wait_for_the_pending_process_to_complete));
        }
    }

    @Override
    public void onAttach(Context context) {super.onAttach(context);}

    @Override
    public void onDetach() {super.onDetach();}

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
