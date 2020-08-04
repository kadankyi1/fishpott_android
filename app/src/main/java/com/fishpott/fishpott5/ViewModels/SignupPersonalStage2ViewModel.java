package com.fishpott.fishpott5.ViewModels;

import android.arch.lifecycle.ViewModel;

/**
 * Created by zatana on 11/21/18.
 */

public class SignupPersonalStage2ViewModel extends ViewModel {
    private String dob = "1995-07-19";
    private int day = 19, month = 6, year = 1995;
    private String country = "", countryCode = "";

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
