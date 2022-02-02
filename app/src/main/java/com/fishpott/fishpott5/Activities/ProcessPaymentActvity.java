package com.fishpott.fishpott5.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fevziomurtekin.payview.Payview;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;
import com.google.android.material.textfield.TextInputEditText;

//

public class ProcessPaymentActvity extends AppCompatActivity {

    private String orderId = "", itemName = "", itemQuantity = "", preText = "", amountCedis = "", amountDollars = "";
    private TextView mTitleTextView, mItemDescriptioTextView;
    private TextInputEditText mCardOwnerNameTextView, mCardNumberTextView, mCardMonthTextView, mCardYearTextView, mCardCVVTextView;
    private Button mPayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_payment);

        if(getIntent().getExtras() != null) {
            String[] info = (String[]) getIntent().getExtras().get("ORDER_DETAILS");
            orderId = info[0];
            itemName = info[1];
            itemQuantity = info[2];
            preText = info[3];
            amountCedis = info[4];
            amountDollars = info[5];

            if(
                    orderId.trim().equalsIgnoreCase("")
                            || itemName.trim().equalsIgnoreCase("")
                            || itemQuantity.trim().equalsIgnoreCase("")
                            || preText.trim().equalsIgnoreCase("")
                            || amountCedis.trim().equalsIgnoreCase("")
                            || amountDollars.trim().equalsIgnoreCase("")
            ){
                finish();
            }
        } else {
            finish();
        }

        mTitleTextView = findViewById(R.id.title_bar_title_textview);
        mItemDescriptioTextView = findViewById(R.id.item_name_textview);
        mCardOwnerNameTextView = findViewById(R.id.tev_card_name);
        mCardNumberTextView = findViewById(R.id.tev_card_no);
        mCardMonthTextView = findViewById(R.id.tev_card_month);
        mCardYearTextView = findViewById(R.id.tev_card_year);
        mCardCVVTextView = findViewById(R.id.tev_card_cv);
        mPayButton = findViewById(R.id.btn_pay);


        mTitleTextView.setText("Payment: " + amountCedis + " or " + amountDollars);
        mItemDescriptioTextView.setText(preText.trim() + " " + itemQuantity + " " + itemName + " stocks");



    }
}
