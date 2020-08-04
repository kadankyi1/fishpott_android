package com.fishpott.fishpott5.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;

/**
 * Created by zatana on 27/10/17.
 */

public class BootStartedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("FpBroadcastReceiver", "onReceive Started");
        Intent myIntent = new Intent(context, NewsFetcherAndPreparerService.class);
        context.startService(myIntent);

    }

}