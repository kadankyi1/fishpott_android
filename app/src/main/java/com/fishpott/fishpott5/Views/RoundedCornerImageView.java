package com.fishpott.fishpott5.Views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.fishpott.fishpott5.R;


/**
 * Created by zatana on 8/22/19.
 */

public class RoundedCornerImageView extends AppCompatImageView {
    public RoundedCornerImageView(Context context) {
        super(context);
        init();
    }

    public RoundedCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundedCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setClipToOutline(boolean clipToOutline) {
        super.setClipToOutline(clipToOutline);
    }


    private void init() {
        setClipToOutline(true);
        setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corner_shape, null));
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
    }
}
