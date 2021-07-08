package com.fishpott.fishpott5.Util;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import com.fishpott.fishpott5.Activities.ProfileOfDifferentPottActivity;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;

/**
 * Created by zatana on 5/27/20.
 */

public class NonUnderlinedClickableSpan extends ClickableSpan {
    private Context context;

    int type;// 0-hashtag , 1- mention, 2- url link
    String text;// Keyword or url
    String time;

    public NonUnderlinedClickableSpan(Context current, String text, int type) {
        this.context = current;
        this.text = text;
        this.type = type;
        this.time = time;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        //adding colors
        if (type == 1)
            ds.setColor(context.getResources().getColor(R.color.colorSharesForSale));
        else if (type == 2)
            ds.setColor(context.getResources().getColor(R.color.colorSharesForSale));
        else
            ds.setColor(context.getResources().getColor(R.color.colorSharesForSale));
        ds.setUnderlineText(false);
        // ds.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    public void onClick(View v) {

        if (type == 0) {
            //pass hashtags to activity using intents
        } else if (type == 1) {
            //do for mentions
            text = text.substring(1, text.length());
            Log.e("LINKIFYCLICK", "text: " + text);
            //Config.openActivityWithNonActivityContext(context, ProfileOfDifferentPottActivity.class, 1, 1, "pottname", text);
        } else {
            // passing weblinks urls to webview activity
            //startWebViewActivity(text);
        }
    }
}
