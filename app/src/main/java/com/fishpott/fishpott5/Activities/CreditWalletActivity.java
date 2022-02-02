package com.fishpott.fishpott5.Activities;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;

public class CreditWalletActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackImageView;
    private ConstraintLayout mVisa, mMTN, mMtnAuto, mVoda, mAirtelTigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_wallet);

        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mVisa = findViewById(R.id.visa_mastercard_option_contrainlayout);
        mMtnAuto = findViewById(R.id.automated_mtn_mobile_money_option_holder_contrainlayout);
        mMTN = findViewById(R.id.mtn_mobile_money_option_holder_contrainlayout);
        mVoda = findViewById(R.id.vodafone_cash_holder_contrainlayout);
        mAirtelTigo = findViewById(R.id.airteltigo_money_holder_contrainlayout);

        mBackImageView.setOnClickListener(this);
        mVisa.setOnClickListener(this);
        mMtnAuto.setOnClickListener(this);
        mMTN.setOnClickListener(this);
        mVoda.setOnClickListener(this);
        mAirtelTigo.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.title_bar_back_icon_imageview){
            onBackPressed();
        } else if(view.getId() == R.id.visa_mastercard_option_contrainlayout){
            //Config.openActivity(CreditWalletActivity.this, ProcessPaymentActvity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.automated_mtn_mobile_money_option_holder_contrainlayout){
            //Config.openActivity(CreditWalletActivity.this, MtnMobileMoneyActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.mtn_mobile_money_option_holder_contrainlayout){
            if(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN).trim().equalsIgnoreCase("")){
                Config.showToastType1(CreditWalletActivity.this, getString(R.string.this_credit_option_is_currently_unavailable));
            } else {
                Config.openActivity(CreditWalletActivity.this, MobileMoneyActivity.class, 1, 0, 1, "MOMO_TYPE", "MTN");
            }
        } else if(view.getId() == R.id.vodafone_cash_holder_contrainlayout){
            if(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE).trim().equalsIgnoreCase("")){
                Config.showToastType1(CreditWalletActivity.this, getString(R.string.this_credit_option_is_currently_unavailable));
            } else {
                Config.openActivity(CreditWalletActivity.this, MobileMoneyActivity.class, 1, 0, 1, "MOMO_TYPE", "VODAFONE");
            }
        } else if(view.getId() == R.id.airteltigo_money_holder_contrainlayout){
            if(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO).trim().equalsIgnoreCase("")){
                Config.showToastType1(CreditWalletActivity.this, getString(R.string.this_credit_option_is_currently_unavailable));
            } else {
                Config.openActivity(CreditWalletActivity.this, MobileMoneyActivity.class, 1, 0, 1, "MOMO_TYPE", "AIRTELTIGO");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.title_bar_back_icon_imageview);
        mVisa = findViewById(R.id.visa_mastercard_option_contrainlayout);
        mMTN = findViewById(R.id.mtn_mobile_money_option_holder_contrainlayout);
        mVoda = findViewById(R.id.vodafone_cash_holder_contrainlayout);
        mAirtelTigo = findViewById(R.id.airteltigo_money_holder_contrainlayout);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackImageView = null;
        mVisa = null;
        mMTN = null;
        mVoda = null;
        mAirtelTigo = null;
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackImageView = null;
        mVisa = null;
        mMTN = null;
        mVoda = null;
        mAirtelTigo = null;
        Config.unbindDrawables(findViewById(R.id.root_creditwalletoptions_activity));
        Config.freeMemory();
    }

}
