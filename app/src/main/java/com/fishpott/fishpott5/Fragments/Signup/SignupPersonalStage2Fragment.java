package com.fishpott.fishpott5.Fragments.Signup;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import com.fishpott.fishpott5.ViewModels.SignupPersonalStage2ViewModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SignupPersonalStage2Fragment extends Fragment implements View.OnClickListener {

    private ImageView mBackImageView;
    private TextView mDateOfBirthMonthTextView, mDateOfBirthDayTextView, mDateOfBirthYearTextView, mCountryTextView;
    private Button mContinueButton;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private NumberPicker.OnValueChangeListener mNumberSetListener;
    private String firstName = "", lastName = "", gender = "", dob = "", countryCode = "";
    private int presetDateDay = 19, presetDateMonth = 6, presetDateYear = 1995, defaultCountry = 0;
    View view = null;

    public static SignupPersonalStage2Fragment newInstance(String firstName, String lastName, String gender) {
        SignupPersonalStage2Fragment fragment = new SignupPersonalStage2Fragment();
        Bundle args = new Bundle();
        args.putString("firstName", firstName);
        args.putString("lastName", lastName);
        args.putString("gender", gender);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            firstName = getArguments().getString("firstName");
            firstName = getArguments().getString("firstName");
            lastName = getArguments().getString("lastName");
            gender = getArguments().getString("gender");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_signup_personal_stage2, container, false);

        mBackImageView =  view.findViewById(R.id.fragment_signup_personalstage2_back_imageview);
        mDateOfBirthYearTextView = view.findViewById(R.id.fragment_signup_personalstage2_date_year_textview);
        mDateOfBirthMonthTextView = view.findViewById(R.id.fragment_signup_personalstage2_date_month_textview);
        mDateOfBirthDayTextView = view.findViewById(R.id.fragment_signup_personalstage2_date_day_textview);
        mCountryTextView = view.findViewById(R.id.fragment_signup_personalstage2_country_textview);
        mContinueButton =  view.findViewById(R.id.fragment_signup_personalstage2_continue_button);

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
                if((Calendar.getInstance().get(Calendar.YEAR) - year) > 12){
                    dob = String.valueOf(year) + "-" + String.valueOf(month+1) + "-" + String.valueOf(dayOfMonth);
                } else {
                    dob = "";
                    Config.showDialogType1(getActivity(), "Error", getString(R.string.fragment_signup_personalstage2_fishpott_is_not_to_be_used_by_people_under_age_13), "", null, false, "", "");
                }
            }
        };

        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fragment_signup_personalstage2_date_day_textview || view.getId() == R.id.fragment_signup_personalstage2_date_month_textview || view.getId() == R.id.fragment_signup_personalstage2_date_year_textview){
            mDateSetListener = Config.openDatePickerDialog(getActivity(), mDateSetListener, true, presetDateDay, presetDateMonth, presetDateYear);
        } else if(view.getId() == R.id.fragment_signup_personalstage2_back_imageview){
            getActivity().onBackPressed();
        } else if(view.getId() == R.id.fragment_signup_personalstage2_continue_button){
            if(!mDateOfBirthDayTextView.getText().toString().trim().equalsIgnoreCase("") && !mDateOfBirthDayTextView.getText().toString().trim().equalsIgnoreCase("day") &&
                    !mDateOfBirthMonthTextView.getText().toString().trim().equalsIgnoreCase("") && !mDateOfBirthMonthTextView.getText().toString().trim().equalsIgnoreCase("month") &&
                    !mDateOfBirthYearTextView.getText().toString().trim().equalsIgnoreCase("") && !mDateOfBirthYearTextView.getText().toString().trim().equalsIgnoreCase("year") &&
                    !dob.trim().equalsIgnoreCase("") && !countryCode.trim().equalsIgnoreCase("") &&
                    !mCountryTextView.getText().toString().trim().equalsIgnoreCase("") && !mCountryTextView.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.choose_country))
                    ){
                Config.openFragment(getActivity().getSupportFragmentManager(),R.id.activity_signup_fragment_holder, SignupPersonalStage3Fragment.newInstance(firstName, lastName, gender, dob, mCountryTextView.getText().toString().trim(), countryCode), "SignupPersonalStage3Fragment", 1);
            } else {
                Config.showToastType1(getActivity(), getString(R.string.fragment_signup_personalstage2_set_your_date_of_birth_and_country));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUP-PERSONAL-STAGE_2-FRAGMENT");
        mBackImageView =  view.findViewById(R.id.fragment_signup_personalstage2_back_imageview);
        mDateOfBirthYearTextView = view.findViewById(R.id.fragment_signup_personalstage2_date_year_textview);
        mDateOfBirthMonthTextView = view.findViewById(R.id.fragment_signup_personalstage2_date_month_textview);
        mDateOfBirthDayTextView = view.findViewById(R.id.fragment_signup_personalstage2_date_day_textview);
        mCountryTextView = view.findViewById(R.id.fragment_signup_personalstage2_country_textview);
        mContinueButton =  view.findViewById(R.id.fragment_signup_personalstage2_continue_button);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED SIGNUP-PERSONAL-STAGE_2-FRAGMENT");
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
        Log.e("memoryManage", "onDestroy STARTED SIGNUP-PERSONAL-STAGE_2-FRAGMENT");
        mBackImageView = null;
        mDateOfBirthMonthTextView = null;
        mDateOfBirthDayTextView = null;
        mDateOfBirthYearTextView = null;
        mCountryTextView = null;
        mContinueButton = null;
        mDateSetListener = null;
        mNumberSetListener = null;
        firstName = null;
        lastName = null;
        gender = null;
        dob = null;
        countryCode = null;
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_signuppersonalstage2_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }
}
