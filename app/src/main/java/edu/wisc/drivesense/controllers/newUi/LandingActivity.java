package edu.wisc.drivesense.controllers.newUi;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cn.pedant.SweetAlert.OptAnimationLoader;
import cn.pedant.SweetAlert.SuccessTickView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.wisc.drivesense.R;

import edu.wisc.drivesense.businessLogic.Concierge;
import edu.wisc.drivesense.controllers.PreferenceActivity;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.views.PinMapFragment;
import edu.wisc.drivesense.views.TripsListViewFragment;
import edu.wisc.drivesense.views.newUi.MenuFragment;
import edu.wisc.drivesense.views.newUi.SettingsFragment;
import edu.wisc.drivesense.views.newUi.StatsFragment;
import edu.wisc.drivesense.views.newUi.resideMenu.ResideMenu;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class LandingActivity extends FragmentActivity implements View.OnClickListener,
        MenuFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        TripsListViewFragment.TripSelectedListener, StatsFragment.OnFragmentInteractionListener {

    private static final String TAG = "LandingActivity";

    private ResideMenu resideMenu;
    private TripsListViewFragment fragmentList;
    private StatsFragment fragmentStats;
    private SettingsFragment fragmentSettings;
    private MenuFragment fragmentMenu;

    //TEMP TESTING
    boolean showingx = true;


    /* Boilerplate and Init */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        //creates the dragging menu
        resideMenu = new ResideMenu(this);
        resideMenu.setMenuListener(menuListener);

        //pull fragments
        fragmentList = (TripsListViewFragment) getFragmentManager().findFragmentById(R.id.trips);
        fragmentStats = (StatsFragment) getFragmentManager().findFragmentById(R.id.stats);

//        resideMenu.addIgnoredView(findViewById(R.id.trips));
    }

    @Override
    protected void onResume() {
        super.onResume();

//        resideMenu.addIgnoredView(fragmentList.getView());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    /* Fragment Callbacks */
    public void onTripSelected(Trip trip) {
        Intent intent = new Intent(LandingActivity.this, TripViewerActivity.class);
        intent.putExtra("tripId", trip.getId());
        LandingActivity.this.startActivity(intent);
    }


    /* User Login and Logout */
    /**
     * Loads the currently logged in user into the list and the menu
     */
    private void loadUser() {
        User user = Concierge.getCurrentUser();
        fragmentList.setUser(user);
    }


    /* Stats Button Callbacks */
    public void onRightButtonClick(View view) {

    }

    public void onButtonLeftClick(View view) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("Look! A dialog!")
                .show();
    }


    /* ORPHANED AND TEMP-- these method will be moved to their respective fragments */
    @Override
    public void onClick(View view) {
        Log.d("Menu", "Touch");
//        resideMenu.closeMenu();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "Something happened in a fragment");
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            Log.d(TAG, "Menu Opened");
//            Toast.makeText(getApplication().getApplicationContext(), "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            Log.d(TAG, "Menu closed");
//      Toast.makeText(getApplication().getApplicationContext(), "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };

    public void onMenuButtonPress(View view) {
        //TESTING METHOD- don't use this in production, spin it off to its own class
        FrameLayout mSuccessFrame = (FrameLayout)findViewById(R.id.success_frame);
//        View mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
//        View mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
        SuccessTickView mSuccessTick = (SuccessTickView)mSuccessFrame.findViewById(R.id.success_tick);
        Animation mSuccessBowAnim = OptAnimationLoader.loadAnimation(this, cn.pedant.SweetAlert.R.anim.success_bow_roate);

        AnimationSet mSuccessLayoutAnimSet = (AnimationSet)OptAnimationLoader.loadAnimation(this, cn.pedant.SweetAlert.R.anim.success_mask_layout);

        FrameLayout mErrorFrame = (FrameLayout)findViewById(R.id.error_frame);
        AnimationSet mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(this, cn.pedant.SweetAlert.R.anim.error_x_in);
        ImageView x = (ImageView) findViewById(R.id.error_x);

        if(showingx) {
            mSuccessFrame.setVisibility(View.VISIBLE);
            mErrorFrame.setVisibility(View.GONE);

            // initial rotate layout of success mask
//            mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
//            mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
//            mSuccessRightMask.startAnimation(mSuccessBowAnim);
            mSuccessTick.startTickAnim();
        }
        else {
            mSuccessFrame.setVisibility(View.GONE);
            mErrorFrame.setVisibility(View.VISIBLE);

            x.startAnimation(mErrorXInAnim);
        }

        showingx = !showingx;
    }
}
