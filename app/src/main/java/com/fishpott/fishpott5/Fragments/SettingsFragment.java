package com.fishpott.fishpott5.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fishpott.fishpott5.Activities.AboutActivity;
import com.fishpott.fishpott5.Activities.ChangePasswordActivity;
import com.fishpott.fishpott5.Activities.CreditWalletActivity;
import com.fishpott.fishpott5.Activities.FindBusinessActivity;
import com.fishpott.fishpott5.Activities.MessengerActivity;
import com.fishpott.fishpott5.Activities.MtnMobileMoneyActivity;
import com.fishpott.fishpott5.Activities.MySharesActivity;
import com.fishpott.fishpott5.Activities.RedeemSharesCouponActivity;
import com.fishpott.fishpott5.Activities.RedeemWalletCreditCouponActivity;
import com.fishpott.fishpott5.Activities.TheTellerActivity;
import com.fishpott.fishpott5.Activities.TransactionsActivity;
import com.fishpott.fishpott5.Activities.WebViewActivity;
import com.fishpott.fishpott5.Activities.WithdrawFundsActivity;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private ConstraintLayout mCreditWalletHolderConstraintLayout, mViewMySharesHolderConstraintLayout, mRedeemWalletCreditCouponHolderConstraintLayout,
            mRedeemSharesCouponHolderConstraintLayout, mTransactionsHolderConstraintLayout, mWithdrawFundsHolderConstraintLayout, mPrivacyPolicyHolderConstraintLayout,
            mChangePasswordHolderConstraintLayout, mContactFPHolderConstraintLayout, mAboutHolderConstraintLayout, mTermsOfServiceHolderConstraintLayout,
            mFindBusinessHolderConstraintLayout;
    public static TextView mWithdrawalWalletBalanceTextView, mDebitWalletBalanceTextView, mPottPearlsBalanceTextView,
            mPottIntelligenceTextView, mPottNetWorthTextView, mPottPositionTextView;
    private View view = null;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        mWithdrawalWalletBalanceTextView = view.findViewById(R.id.withdrawal_wallet_textview);
        mDebitWalletBalanceTextView = view.findViewById(R.id.debit_wallet_textview);
        mPottPearlsBalanceTextView = view.findViewById(R.id.pott_pearls_wallet_textview);
        mPottIntelligenceTextView = view.findViewById(R.id.pott_intelligence_textview);
        mPottNetWorthTextView = view.findViewById(R.id.pottworth_textview);
        mPottPositionTextView = view.findViewById(R.id.pottworth_info_textview);
        mAboutHolderConstraintLayout = view.findViewById(R.id.about_holder_contrainlayout);
        mFindBusinessHolderConstraintLayout = view.findViewById(R.id.active_bness_suggest_holder_contrainlayout);
        mCreditWalletHolderConstraintLayout = view.findViewById(R.id.credit_wallet_holder_contrainlayout);
        mViewMySharesHolderConstraintLayout = view.findViewById(R.id.myshares_holder_contrainlayout);
        mRedeemWalletCreditCouponHolderConstraintLayout = view.findViewById(R.id.redeem_wallet_coupon_holder_contrainlayout);
        mRedeemSharesCouponHolderConstraintLayout = view.findViewById(R.id.redeem_shares_coupon_holder_contrainlayout);
        mTransactionsHolderConstraintLayout = view.findViewById(R.id.transactions_holder_contrainlayout);
        mChangePasswordHolderConstraintLayout = view.findViewById(R.id.withdraw_funds_holder_contrainlayout);
        mWithdrawFundsHolderConstraintLayout = view.findViewById(R.id.change_password_holder_contrainlayout);
        mContactFPHolderConstraintLayout = view.findViewById(R.id.contact_fishpott_holder_contrainlayout);
        mPrivacyPolicyHolderConstraintLayout = view.findViewById(R.id.privacy_policy_holder_contrainlayout);
        mTermsOfServiceHolderConstraintLayout = view.findViewById(R.id.tos_holder_contrainlayout);


        String withdrawalBalance = Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET);
        String debitBalance = Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEBIT_WALLET);
        String pottPearls = Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_PEARLS);

        if(withdrawalBalance.trim().equalsIgnoreCase("")){
            withdrawalBalance = "0";
        }
        if(debitBalance.trim().equalsIgnoreCase("")){
            debitBalance = "0";
        }
        if(pottPearls.trim().equalsIgnoreCase("")){
            pottPearls = "0";
        }

        mWithdrawalWalletBalanceTextView.setText(Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + withdrawalBalance);
        mDebitWalletBalanceTextView.setText(Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY) + debitBalance);
        mPottPearlsBalanceTextView.setText(pottPearls);

        mAboutHolderConstraintLayout.setOnClickListener(this);
        mFindBusinessHolderConstraintLayout.setOnClickListener(this);
        mCreditWalletHolderConstraintLayout.setOnClickListener(this);
        mViewMySharesHolderConstraintLayout.setOnClickListener(this);
        mRedeemWalletCreditCouponHolderConstraintLayout.setOnClickListener(this);
        mRedeemSharesCouponHolderConstraintLayout.setOnClickListener(this);
        mTransactionsHolderConstraintLayout.setOnClickListener(this);
        mChangePasswordHolderConstraintLayout.setOnClickListener(this);
        mWithdrawFundsHolderConstraintLayout.setOnClickListener(this);
        mContactFPHolderConstraintLayout.setOnClickListener(this);
        mPrivacyPolicyHolderConstraintLayout.setOnClickListener(this);
        mTermsOfServiceHolderConstraintLayout.setOnClickListener(this);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.freeMemory();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_settings_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.about_holder_contrainlayout){
            Config.openActivity(getActivity(), AboutActivity.class, 0, 0, 0, "", "");
        } else if(view.getId() ==  mFindBusinessHolderConstraintLayout.getId()){
            Config.openActivity(getActivity(), FindBusinessActivity.class, 0, 0, 0, "", "");
        }else if(view.getId() == R.id.credit_wallet_holder_contrainlayout){
            Config.openActivity(getActivity(), CreditWalletActivity.class, 1, 0, 0, "", "");
            //Config.openActivity(getActivity(), MtnMobileMoneyActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.myshares_holder_contrainlayout){
            Config.openActivity(getActivity(), MySharesActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.redeem_wallet_coupon_holder_contrainlayout){
            Config.openActivity(getActivity(), RedeemWalletCreditCouponActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.redeem_shares_coupon_holder_contrainlayout){
            Config.openActivity(getActivity(), RedeemSharesCouponActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.withdraw_funds_holder_contrainlayout){
            Config.openActivity(getActivity(), WithdrawFundsActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.transactions_holder_contrainlayout){
            Config.openActivity(getActivity(), TransactionsActivity.class, 1, 0, 0, "", "");
        }  else if(view.getId() == R.id.change_password_holder_contrainlayout){
            Config.openActivity(getActivity(), ChangePasswordActivity.class, 1, 0, 0, "", "");
        } else if(view.getId() == R.id.contact_fishpott_holder_contrainlayout){
            /*
            String[] chatData = {
                    "s_" + Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID) + Config.FP_ID,
                    "fishpot_inc",
                    "fp"
            };
            Config.openActivity4(getActivity(), MessengerActivity.class, 1, 0, 1, "CHAT_INFO", chatData);
             */
            String fishpottEmail = "info@fishpott.com";
            if(!Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_FISHPOTT_EMAIL).trim().equalsIgnoreCase("")){
                fishpottEmail = Config.getSharedPreferenceString(getActivity().getApplicationContext(), Config.SHARED_PREF_KEY_USER_FISHPOTT_EMAIL);
            }
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{fishpottEmail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "PLEASE TYPE SUBJECT");
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException ex) {
                Config.showToastType1(getActivity(), "No email client installed on your device.");
            }
        }  else if(view.getId() == R.id.privacy_policy_holder_contrainlayout){
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, Config.FISHPOTT_PRIVACY_POLICY);
        }  else if(view.getId() == R.id.tos_holder_contrainlayout){
            Config.openActivity(getActivity(), WebViewActivity.class, 1, 0, 1, Config.WEBVIEW_KEY_URL, Config.FISHPOTT_TERMS_OF_SERVICE);
        }
    }
}
