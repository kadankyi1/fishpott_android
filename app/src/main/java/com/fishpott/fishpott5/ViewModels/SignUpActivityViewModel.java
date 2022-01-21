package com.fishpott.fishpott5.ViewModels;

import androidx.lifecycle.ViewModel;

/**
 * Created by zatana on 11/19/18.
 */

public class SignUpActivityViewModel  extends ViewModel {

    private String currentFragment;

    public void setCurrentFragment(String currentFragment) {
        this.currentFragment = currentFragment;
    }

    public String getCurrentFragment() {
        if(currentFragment == null){

            return "SignupStartFragment";
        }
        return currentFragment;
    }

}
