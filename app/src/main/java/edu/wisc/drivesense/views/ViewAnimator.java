package edu.wisc.drivesense.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

/**
 * Created by Damouse on 6/24/14.
 *
 * Wrapper for simple animation calls. Pass in the views to the respective animation methods.
 *
 *
 */
public class ViewAnimator implements ValueAnimator.AnimatorUpdateListener {
    private final String TAG = "ViewAnimator";

    private LinearLayout layoutView;
    private View slideIn;

    private float weightClosed = 1.0f;
    private float weightOpen = 2.0f;

    //TODO DEBUG
    private LinearLayout tripsSliding;

    public ViewAnimator(LinearLayout weight, View slideIn, LinearLayout tripsSliding) {
        this.slideIn = slideIn;
        this.layoutView = weight;
        this.tripsSliding = tripsSliding;
    }


    /* Public Interface */
    public void presentSlidein() {
        Log.d(TAG, "present Slidein");
        slideIn(slideIn.getMeasuredHeight(), 0f);
    }

    public void dismissSlidein() {
        Log.d(TAG, "Dismiss Slidein");
        slideIn(0f, slideIn.getMeasuredHeight());
    }

    public void presentTrips() {
        changeLinearLayoutWeight(true);
    }

    public void dismissTrips() {
        changeLinearLayoutWeight(false);
    }

    public void initTrips() {
        //slide in the
        ObjectAnimator tripsListMover = ObjectAnimator.ofFloat(tripsSliding, "translationY", 0f, tripsSliding.getMeasuredHeight());
        tripsListMover.setDuration(1);

        tripsListMover.start();
        Log.d(TAG, "Animation started");
    }


    /* Animation implementation */
    /**
     * Change the weight of a layout so the internal elements adjust accordingly.
     */
    private void changeLinearLayoutWeight(boolean present) {
        float weight = 0;
        float fromCoordinate = 0;
        float toCoordinate = 0;

        //method overridden for both presentation and dismissal
        if (present) {
            weight = weightOpen;
            fromCoordinate = tripsSliding.getMeasuredHeight();
            toCoordinate = 0f;
        }
        else {
            weight = weightClosed;
            fromCoordinate = 0f;
            toCoordinate = tripsSliding.getMeasuredHeight();
        }

        //cjhange the height of the map
        ObjectAnimator mover = ObjectAnimator.ofFloat(layoutView, "weightSum", layoutView.getWeightSum(), weight);
        mover.setDuration(500);
        mover.addUpdateListener(this);

        //slide in the
        ObjectAnimator tripsListMover = ObjectAnimator.ofFloat(tripsSliding, "translationY", fromCoordinate, toCoordinate);
        tripsListMover.setDuration(500);

        tripsListMover.start();
        mover.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        layoutView.requestLayout();
    }

    /**
     * Second animation: bringing in the recording slidein
     * @param fromHeight
     * @param toHeight
     */
    private void slideIn(float fromHeight, float toHeight) {
        ObjectAnimator mover = ObjectAnimator.ofFloat(slideIn, "translationY", fromHeight, toHeight);
        slideIn.setVisibility(View.VISIBLE);
        mover.setDuration(500);
        mover.start();
    }
}
