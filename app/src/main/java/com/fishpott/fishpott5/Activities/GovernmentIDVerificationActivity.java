package com.fishpott.fishpott5.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.fishpott.fishpott5.R;

public class GovernmentIDVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_government_idverification);
    }


    @Override
    public void finish() {
        super.finish();
        ExitActivity.exitApplicationAndRemoveFromRecent(GovernmentIDVerificationActivity.this);
    }
}
