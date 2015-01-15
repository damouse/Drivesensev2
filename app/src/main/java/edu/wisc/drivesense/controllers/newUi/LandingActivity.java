package edu.wisc.drivesense.controllers.newUi;

import android.app.FragmentManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.wisc.drivesense.R;

import edu.wisc.drivesense.model.Trip;
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


    /* Boilerplate and Init */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        //pull fragments
        fragmentList = (TripsListViewFragment) getFragmentManager().findFragmentById(R.id.trips);
        fragmentStats = (StatsFragment) getFragmentManager().findFragmentById(R.id.stats);

        //dummy data
        fragmentList.setUser(null);

        setUpMenu();
    }

    private void setUpMenu() {
        // attach to current activity;a
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(imageForTimeOfDay());
        resideMenu.attachToActivity(this);
//        resideMenu.setMenuListener(menuListener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {
        Log.d("Menu", "Touch");
//        resideMenu.closeMenu();
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu(){
        return resideMenu;
    }


    /* Fragment Callbacks */
    public void onTripSelected(Trip trip) {

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


    /* Misc and helper files */
    /**
     * Choose a different background image based on the time of day
     * @return an image resource
     */
    private int imageForTimeOfDay() {
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

        if (hour < 6)
            return R.drawable.night;
        else if (hour < 8)
            return R.drawable.morning;
        else if (hour < 20)
            return R.drawable.day;
        else
            return R.drawable.evening;
    }
}
