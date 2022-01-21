package com.fishpott.fishpott5.ViewModels;


import androidx.lifecycle.ViewModel;

/**
 * Created by zatana on 11/26/18.
 */

public class ChangePasswordWithResetCodeViewModel  extends ViewModel {
    Boolean PASSWORD_CHANGE_IS_SUCESSFUL = false;

    public Boolean getPASSWORD_CHANGE_IS_SUCESSFUL() {
        return PASSWORD_CHANGE_IS_SUCESSFUL;
    }

    public void setPASSWORD_CHANGE_IS_SUCESSFUL(Boolean PASSWORD_CHANGE_IS_SUCESSFUL) {
        this.PASSWORD_CHANGE_IS_SUCESSFUL = PASSWORD_CHANGE_IS_SUCESSFUL;
    }
}
