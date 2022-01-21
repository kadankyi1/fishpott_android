package com.fishpott.fishpott5.ViewModels;

import androidx.lifecycle.ViewModel;

/**
 * Created by zatana on 11/21/18.
 */

public class SignupPersonalStage1ViewModel extends ViewModel {

    private String gender = "";

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
