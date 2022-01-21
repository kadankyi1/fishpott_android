package com.fishpott.fishpott5.Activities;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;


public class UpdateActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mUpdateCoverImage;
    private TextView mUpdateTitle, mUpdateDescription, mNotNowText;
    private Button mUpdateButton;
    private String backLinkActivityClosedStatus = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        if(getIntent().getExtras() !=null) {
            backLinkActivityClosedStatus =(String) getIntent().getExtras().get(Config.KEY_ACTIVITY_FINISHED);
        }

        mUpdateCoverImage = findViewById(R.id.update_activity_imageView1);
        mUpdateTitle = findViewById(R.id.update_activity_textview1);
        mUpdateDescription = findViewById(R.id.update_activity_textview2);
        mNotNowText = findViewById(R.id.update_activity_textView3);
        mUpdateButton = findViewById(R.id.update_activity_button1);

        if(Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE)){
            mNotNowText.setVisibility(View.INVISIBLE);
            mNotNowText.setClickable(false);
        } else {
            mNotNowText.setVisibility(View.VISIBLE);
            mNotNowText.setClickable(true);
        }

        mUpdateCoverImage.setAlpha(0f);
        mUpdateCoverImage.setVisibility(View.VISIBLE);

        mUpdateTitle.setAlpha(0f);
        mUpdateTitle.setVisibility(View.VISIBLE);

        mUpdateDescription.setAlpha(0f);
        mUpdateDescription.setVisibility(View.VISIBLE);

        mUpdateButton.setAlpha(0f);
        mUpdateButton.setVisibility(View.VISIBLE);

        // TURNING ON ALPHA TO SLOWLY FADE IN VIEWS
        int mediumAnimationTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mUpdateCoverImage.animate()
                .alpha(1f)
                .setDuration(mediumAnimationTime)
                .setListener(null);
        mUpdateTitle.animate()
                .alpha(1f)
                .setDuration(mediumAnimationTime)
                .setListener(null);
        mUpdateDescription.animate()
                .alpha(1f)
                .setDuration(mediumAnimationTime)
                .setListener(null);
        mNotNowText.animate()
                .alpha(1f)
                .setDuration(mediumAnimationTime)
                .setListener(null);
        mUpdateButton.animate()
                .alpha(1f)
                .setDuration(mediumAnimationTime)
                .setListener(null);

        mNotNowText.setOnClickListener(this);
        mUpdateButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.update_activity_button1){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.fishpott.fishpott5")));
        } else if(view.getId() == R.id.update_activity_textView3){
            Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, Config.getCurrentDate());
            if(!Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE).equalsIgnoreCase("")
                    && !Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN).equalsIgnoreCase("")
                    ){
                if(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PROFILE_PICTURE).equalsIgnoreCase("")){
                    Config.openActivity(UpdateActivity.this, SetProfilePictureActivity.class, 1, 1, 0,"","");
                } else {
                    Config.openActivity(UpdateActivity.this, MainActivity.class, 1, 1, 0, "", "");
                }


            } else {

                Config.openActivity(UpdateActivity.this, StartActivity.class, 1, 1, 0, "", "");

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED UPDATE-ACTIVITY");
        mUpdateCoverImage = findViewById(R.id.update_activity_imageView1);
        mUpdateTitle = findViewById(R.id.update_activity_textview1);
        mUpdateDescription = findViewById(R.id.update_activity_textview2);
        mNotNowText = findViewById(R.id.update_activity_textView3);
        mUpdateButton = findViewById(R.id.update_activity_button1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED UPDATE-ACTIVITY");
        mUpdateCoverImage = null;
        mUpdateTitle = null;
        mUpdateDescription = null;
        mNotNowText = null;
        mUpdateButton = null;
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED UPDATE-ACTIVITY");
        if(Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE)){
            ExitActivity.exitApplicationAndRemoveFromRecent(UpdateActivity.this);
        } else if(backLinkActivityClosedStatus.equalsIgnoreCase("1")){

        } else {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED UPDATE-ACTIVITY");
        mUpdateCoverImage = null;
        mUpdateTitle = null;
        mUpdateDescription = null;
        mNotNowText = null;
        mUpdateButton = null;
        backLinkActivityClosedStatus = null;
        Config.unbindDrawables(findViewById(R.id.root_update_activity));
        Config.freeMemory();
    }
}
