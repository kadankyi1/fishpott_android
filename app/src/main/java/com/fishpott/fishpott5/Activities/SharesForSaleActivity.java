package com.fishpott.fishpott5.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.fishpott.fishpott5.R;

public class SharesForSaleActivity extends AppCompatActivity {

    private String activityFinished = "";
    private TextView mDisplayTextTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shares_for_sale);

        mDisplayTextTextView = (TextView) findViewById(R.id.displaytext);


        // GETTING THE INFORMATION SENT FROM THE PRIOR ACTIVITY/FRAGMENT
        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle !=null) {
            activityFinished =(String) intentBundle.get("id");
        } else {
            activityFinished = "No Text To Display";
        }

        mDisplayTextTextView.setText(activityFinished);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }
}
