package com.fishpott.fishpott5.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PersonalityActivity extends AppCompatActivity {

    private ConstraintLayout mOceanHolderConstraintLayout, mLoaderHolderConstraintLayout;
    private TextView mAnalysisTextView, mOpennessTextView, mConscientiousnessTextView, mExtraversionTextView, mAgreeablenesssTextView, mNeuroticismTextView;
    private ImageView mLoaderHolderImageView;
    private TextView mLoaderHolderTextView;
    private Thread networkThread = null;
    private Boolean networkRequestStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality);

        mLoaderHolderConstraintLayout= findViewById(R.id.loader_constraintLayout);
        mLoaderHolderImageView = findViewById(R.id.loader_imageview);
        mLoaderHolderTextView = findViewById(R.id.loadertext_textview);
        mOceanHolderConstraintLayout = findViewById(R.id.ocean_constraintLayout);
        mAnalysisTextView = findViewById(R.id.analysis_textview);
        mOpennessTextView = findViewById(R.id.openness_textview);
        mConscientiousnessTextView = findViewById(R.id.conscientiousness_textview);
        mExtraversionTextView = findViewById(R.id.extraversion_textview);
        mAgreeablenesssTextView = findViewById(R.id.agreeableness_textview);
        mNeuroticismTextView = findViewById(R.id.neuroticism_textview);

        mLoaderHolderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOceanValues();
            }
        });
        mLoaderHolderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOceanValues();
            }
        });


        setOceanValues();
    }

    private void setOceanValues(){
        if(networkRequestStarted){
            return;
        }
        networkRequestStarted = true;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                mOceanHolderConstraintLayout.setVisibility(View.INVISIBLE);
                mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                mLoaderHolderTextView.setText("Getting your personality rating...");
                mLoaderHolderImageView.startAnimation(AnimationUtils.loadAnimation(PersonalityActivity.this, R.anim.suggestion_loading_anim));
            }
        });

        AndroidNetworking.post(Config.LINK_UPDATE_ORDER_STATUS)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + Config.getSharedPreferenceString(PersonalityActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("user_phone_number", Config.getSharedPreferenceString(PersonalityActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                .addBodyParameter("user_pottname", Config.getSharedPreferenceString(PersonalityActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                .addBodyParameter("investor_id", Config.getSharedPreferenceString(PersonalityActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("user_language", LocaleHelper.getLanguage(PersonalityActivity.this))
                .addBodyParameter("app_type", "ANDROID")
                .addBodyParameter("app_version_code", String.valueOf(Config.getAppVersionCode(PersonalityActivity.this.getApplicationContext())))
                .addBodyParameter("fcm_token", "")
                .setTag("update_order_status")
                .setPriority(Priority.HIGH)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                networkRequestStarted = false;

                try {
                    Log.e("payresponse", response);
                    final JSONObject o = new JSONObject(response);
                    int myStatus = o.getInt("status");
                    final String myStatusMessage = o.getString("message");
                    final String anaylysisInfo = o.getJSONObject("data").getString("ai_info");
                    final String oo = o.getJSONObject("data").getString("o");
                    final String c = o.getJSONObject("data").getString("c");
                    final String e = o.getJSONObject("data").getString("e");
                    final String a = o.getJSONObject("data").getString("a");
                    final String n = o.getJSONObject("data").getString("n");

                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(PersonalityActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("phone_verification_is_on"));

                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceBoolean(PersonalityActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                    Config.setSharedPreferenceInt(PersonalityActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));

                    if(myStatus == 1){

                        if(!PersonalityActivity.this.isFinishing()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    mLoaderHolderImageView.clearAnimation();
                                    mLoaderHolderTextView.setText("");
                                    mLoaderHolderConstraintLayout.setVisibility(View.INVISIBLE);
                                    mAnalysisTextView.setText(anaylysisInfo);
                                    mOpennessTextView.setText(oo);
                                    mConscientiousnessTextView.setText(c);
                                    mExtraversionTextView.setText(e);
                                    mAgreeablenesssTextView.setText(a);
                                    mNeuroticismTextView.setText(n);
                                    mOceanHolderConstraintLayout.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else if(myStatus == 2){
                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        Config.setSharedPreferenceBoolean(PersonalityActivity.this, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(PersonalityActivity.this, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    } else if(myStatus == 3){
                        // GENERAL ERROR
                        mLoaderHolderImageView.clearAnimation();
                        mLoaderHolderTextView.setText("Click icon to retry...");
                        mOceanHolderConstraintLayout.setVisibility(View.INVISIBLE);
                        mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                        Config.showToastType1(PersonalityActivity.this, myStatusMessage);
                        return;
                    } else if(myStatus == 4){
                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        Config.showToastType1(PersonalityActivity.this, myStatusMessage);
                        Config.signOutUser(PersonalityActivity.this.getApplicationContext(), true, PersonalityActivity.this, StartActivity.class, 0, 2);
                    } else if(myStatus == 5){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Config.showToastType1(PersonalityActivity.this, getString(R.string.failed_if_this_continues_please_update_your_app));
                    if(!PersonalityActivity.this.isFinishing()){
                        mLoaderHolderImageView.clearAnimation();
                        mLoaderHolderTextView.setText("Click icon to retry...");
                        mOceanHolderConstraintLayout.setVisibility(View.INVISIBLE);
                        mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onError(ANError anError) {
                networkRequestStarted = false;
                Config.showToastType1(PersonalityActivity.this, getString(R.string.failed_check_your_internet_and_try_again));
                if(!PersonalityActivity.this.isFinishing()){
                    mLoaderHolderImageView.clearAnimation();
                    mLoaderHolderTextView.setText("Click icon to retry...");
                    mOceanHolderConstraintLayout.setVisibility(View.INVISIBLE);
                    mLoaderHolderConstraintLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}