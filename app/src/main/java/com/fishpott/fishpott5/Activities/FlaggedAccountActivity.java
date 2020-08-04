package com.fishpott.fishpott5.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fishpott.fishpott5.R;

public class FlaggedAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flagged_account);
    }


    @Override
    public void finish() {
        super.finish();
        ExitActivity.exitApplicationAndRemoveFromRecent(FlaggedAccountActivity.this);
    }
}
