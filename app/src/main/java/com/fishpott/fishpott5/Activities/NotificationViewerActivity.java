package com.fishpott.fishpott5.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;

public class NotificationViewerActivity extends AppCompatActivity {

    private ImageView mBackImageView;
    private TextView mNotificationTitleTextView, mNotificationMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_viewer);


        mBackImageView = findViewById(R.id.activity_aboutfp_back_imageview);
        mNotificationTitleTextView = findViewById(R.id.noti_title);
        mNotificationMessageTextView = findViewById(R.id.noti_message);


        // GETTING VALUES FROM OLD-TO-NEW ACTIVITY TRANSITION
        if(getIntent().getExtras() != null) {
            String[] chatInfo =(String[]) getIntent().getExtras().get("NOTIFICATION_DATA");
            mNotificationTitleTextView.setText(chatInfo[0]);
            mNotificationMessageTextView.setText(chatInfo[1]);
        }


        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }
}