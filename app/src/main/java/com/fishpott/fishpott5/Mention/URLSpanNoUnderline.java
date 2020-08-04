package com.fishpott.fishpott5.Mention;

import android.annotation.SuppressLint;
import android.text.TextPaint;
import android.text.style.URLSpan;

import com.fishpott.fishpott5.R;

/**
 * Created by zatana on 1/6/20.
 */

public class URLSpanNoUnderline extends URLSpan {

    public URLSpanNoUnderline(String url) {
        super(url);
    }



    @SuppressLint("ResourceAsColor")
    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(R.color.colorDateText);
    }


}