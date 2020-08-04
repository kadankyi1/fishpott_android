package com.fishpott.fishpott5.ViewModels;

import android.arch.lifecycle.ViewModel;

/**
 * Created by zatana on 11/26/18.
 */

public class SignupBusinessStage3ViewModel extends ViewModel {

    private Boolean NETWORK_PROCESS_HAS_STARTED = false;

    public Boolean getNETWORK_PROCESS_HAS_STARTED() {
        return NETWORK_PROCESS_HAS_STARTED;
    }

    public void setNETWORK_PROCESS_HAS_STARTED(Boolean NETWORK_PROCESS_HAS_STARTED) {
        this.NETWORK_PROCESS_HAS_STARTED = NETWORK_PROCESS_HAS_STARTED;
    }
}
