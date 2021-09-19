package com.fishpott.fishpott5.Fragments.Signup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;

import java.util.Arrays;
import java.util.List;

public class SignupBusinessStage2Fragment extends Fragment implements View.OnClickListener {

    private ImageView mBackImageView;
    private TextView mDateOfBirthMonthTextView, mDateOfBirthDayTextView, mDateOfBirthYearTextView, mCountryTextView;
    private Button mContinueButton;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private NumberPicker.OnValueChangeListener mNumberSetListener;
    private String businessName = "", branchLocation = "", dob = "", countryCode = "",  businessEmail ="";
    private int presetDateDay = 19, presetDateMonth = 6, presetDateYear = 2015, defaultCountry = 0;
    private View view = null;


    public static SignupBusinessStage2Fragment newInstance(String businessName, String branchLocation, String businessEmail) {
        SignupBusinessStage2Fragment fragment = new SignupBusinessStage2Fragment();
        Bundle args = new Bundle();
        args.putString("businessName", businessName);
        args.putString("branchLocation", branchLocation);
        args.putString("businessEmail", businessEmail);
        Log.e("2mBusinessEmail", businessEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            businessName = getArguments().getString("businessName");
            branchLocation = getArguments().getString("branchLocation");
            businessEmail = getArguments().getString("businessEmail");

            Log.e("3mBusinessEmail", businessEmail);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_signup_business_stage2, container, false);

        mBackImageView =  view.findViewById(R.id.fragment_signup_businessstage2_back_imageview);
        mDateOfBirthYearTextView = view.findViewById(R.id.fragment_signup_businessstage2_date_year_textview);
        mDateOfBirthMonthTextView = view.findViewById(R.id.fragment_signup_businessstage2_date_month_textview);
        mDateOfBirthDayTextView = view.findViewById(R.id.fragment_signup_businessstage2_date_day_textview);
        mCountryTextView = view.findViewById(R.id.fragment_signup_businessstage2_country_textview);
        mContinueButton =  view.findViewById(R.id.fragment_signup_businessstage2_continue_button);

        final String[] countriesStringArraySet = getActivity().getResources().getStringArray(R.array.countries_array_starting_with_choose_country);
        final List<String> countriesStringArrayList = Arrays.asList(countriesStringArraySet);
        final List<String> countriesCodesStringArrayList = Arrays.asList(getResources().getStringArray(R.array.fragment_signup_personalstage2_countries_codes_array));

        mDateOfBirthDayTextView.setOnClickListener(this);
        mDateOfBirthMonthTextView.setOnClickListener(this);
        mDateOfBirthYearTextView.setOnClickListener(this);
        mCountryTextView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mContinueButton.setOnClickListener(this);

        mCountryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNumberSetListener = Config.openNumberPickerForCountries(getActivity(), mNumberSetListener, 0, countriesStringArraySet.length-1, true, getActivity().getResources().getStringArray(R.array.countries_array_starting_with_choose_country), defaultCountry);
            }
        });

        mNumberSetListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                defaultCountry = newVal;
                mCountryTextView.setText(countriesStringArrayList.get(newVal));
                countryCode = countriesCodesStringArrayList.get(newVal);
            }
        };

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mDateOfBirthYearTextView.setText(String.valueOf(year));
                mDateOfBirthMonthTextView.setText(Config.getMonthNameFromMonthNumber(getActivity(), month, 2));
                mDateOfBirthDayTextView.setText(String.valueOf(dayOfMonth));
                presetDateDay = dayOfMonth;
                presetDateMonth = month;
                presetDateYear = year;
                dob = String.valueOf(year) + "-" + String.valueOf(month+1) + "-" + String.valueOf(dayOfMonth);
            }
        };

        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fragment_signup_businessstage2_date_day_textview || view.getId() == R.id.fragment_signup_businessstage2_date_month_textview || view.getId() == R.id.fragment_signup_businessstage2_date_year_textview){
            mDateSetListener = Config.openDatePickerDialog(getActivity(), mDateSetListener, true, presetDateDay, presetDateMonth, presetDateYear);
        } else if(view.getId() == R.id.fragment_signup_businessstage2_back_imageview){
            getActivity().onBackPressed();
        } else if(view.getId() == R.id.fragment_signup_businessstage2_continue_button){
            if(!mDateOfBirthDayTextView.getText().toString().trim().equalsIgnoreCase("") && !mDateOfBirthDayTextView.getText().toString().trim().equalsIgnoreCase("day") &&
                    !mDateOfBirthMonthTextView.getText().toString().trim().equalsIgnoreCase("") && !mDateOfBirthMonthTextView.getText().toString().trim().equalsIgnoreCase("month") &&
                    !mDateOfBirthYearTextView.getText().toString().trim().equalsIgnoreCase("") && !mDateOfBirthYearTextView.getText().toString().trim().equalsIgnoreCase("year") &&
                    !dob.trim().equalsIgnoreCase("") && !countryCode.trim().equalsIgnoreCase("") &&
                    !mCountryTextView.getText().toString().trim().equalsIgnoreCase("") && !mCountryTextView.getText().toString().trim().equalsIgnoreCase("country")
                    ){
                Log.e("4mBusinessEmail", businessEmail);
                Config.openFragment(getActivity().getSupportFragmentManager(),R.id.activity_signup_fragment_holder, SignupBusinessStage3Fragment.newInstance(businessName, branchLocation, businessEmail, dob, mCountryTextView.getText().toString().trim(), countryCode), "SignupBusinessStage3Fragment", 1);
            } else {
                Config.showToastType1(getActivity(), getString(R.string.fragment_signup_businessstage2_set_the_business_start_date_and_country_of_operation));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUP-BUSINESS-STAGE_2-FRAGMENT");
        mBackImageView =  view.findViewById(R.id.fragment_signup_businessstage2_back_imageview);
        mDateOfBirthYearTextView = view.findViewById(R.id.fragment_signup_businessstage2_date_year_textview);
        mDateOfBirthMonthTextView = view.findViewById(R.id.fragment_signup_businessstage2_date_month_textview);
        mDateOfBirthDayTextView = view.findViewById(R.id.fragment_signup_businessstage2_date_day_textview);
        mCountryTextView = view.findViewById(R.id.fragment_signup_businessstage2_country_textview);
        mContinueButton =  view.findViewById(R.id.fragment_signup_businessstage2_continue_button);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED SIGNUP-BUSINESS-STAGE_2-FRAGMENT");
        mBackImageView = null;
        mDateOfBirthMonthTextView = null;
        mDateOfBirthDayTextView = null;
        mDateOfBirthYearTextView = null;
        mCountryTextView = null;
        mContinueButton = null;
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED SIGNUP-BUSINESS-STAGE_2-FRAGMENT");
        mBackImageView = null;
        mDateOfBirthMonthTextView = null;
        mDateOfBirthDayTextView = null;
        mDateOfBirthYearTextView = null;
        mCountryTextView = null;
        mContinueButton = null;
        mNumberSetListener = null;
        mDateSetListener = null;
        businessName = null;
        businessEmail = null;
        branchLocation = null;
        dob = null;
        countryCode = null;
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_signupbusinessstage2_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
