package com.fishpott.fishpott5.customphoto.customcropper.gestures;

import android.view.MotionEvent;

public interface GestureDetector {

    boolean onTouchEvent(MotionEvent ev);

    boolean isScaling();

    void setOnGestureListener(OnGestureListener listener);

}
