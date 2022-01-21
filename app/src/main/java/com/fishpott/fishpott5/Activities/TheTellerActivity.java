package com.fishpott.fishpott5.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;

//

public class TheTellerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_teller);

        /*new thetellerManager(this).setAmount(Long.parseLong("12"))
                .setEmail("annodankyikwaku@gmail.com")
                .setfName("Kwaku")
                .setlName("Dankyi")
                .setMerchant_id("TTM-00001479")
                .setNarration("Test Payment")
                .setApiUser("fishpottcompany@gmail.com")
                .setApiKey("8")
                .setTxRef("HKSJS" + Config.generateRandomInteger())
                .set3dUrl("https://test.theteller.net/")
                .acceptCardPayments(true)
                .acceptGHMobileMoneyPayments(true)
                .onStagingEnv(true)
                .initialize();*/
    }
}
