package com.fishpott.fishpott5.customphoto.customcropper;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {

    private CropperViewAttacher photoViewAttacher;

    /**
     * Default constructor
     *
     * @param photoViewAttacher CropperViewAttacher to bind to
     */
    public DefaultOnDoubleTapListener(CropperViewAttacher photoViewAttacher) {
        setPhotoViewAttacher(photoViewAttacher);
    }

    /**
     * Allows to change CropperViewAttacher within range of single instance
     *
     * @param newPhotoViewAttacher CropperViewAttacher to bind to
     */
    public void setPhotoViewAttacher(CropperViewAttacher newPhotoViewAttacher) {
        this.photoViewAttacher = newPhotoViewAttacher;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (this.photoViewAttacher == null)
            return false;

        ImageView imageView = photoViewAttacher.getImageView();

        if (null != photoViewAttacher.getOnPhotoTapListener()) {
            final RectF displayRect = photoViewAttacher.getDisplayRect();

            if (null != displayRect) {
                final float x = e.getX(), y = e.getY();

                // Check to see if the user tapped on the photo
                if (displayRect.contains(x, y)) {

                    float xResult = (x - displayRect.left)
                            / displayRect.width();
                    float yResult = (y - displayRect.top)
                            / displayRect.height();

                    photoViewAttacher.getOnPhotoTapListener().onPhotoTap(imageView, xResult, yResult);
                    return true;
                }
            }
        }
        if (null != photoViewAttacher.getOnViewTapListener()) {
            photoViewAttacher.getOnViewTapListener().onViewTap(imageView, e.getX(), e.getY());
        }

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent ev) {
        if (photoViewAttacher == null)
            return false;

        try {
            float scale = photoViewAttacher.getScale();
            float x = ev.getX();
            float y = ev.getY();

            if (scale < photoViewAttacher.getMediumScale()) {
                photoViewAttacher.setScale(photoViewAttacher.getMediumScale(), x, y, true);
            } else if (scale >= photoViewAttacher.getMediumScale() && scale < photoViewAttacher.getMaximumScale()) {
                photoViewAttacher.setScale(photoViewAttacher.getMaximumScale(), x, y, true);
            } else {
                photoViewAttacher.setScale(photoViewAttacher.getMinimumScale(), x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Can sometimes happen when getX() and getY() is called
        }

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Wait for the confirmed onDoubleTap() instead
        return false;
    }

}
