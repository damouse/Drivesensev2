package edu.wisc.drivesense.controllers.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import edu.wisc.drivesense.R;

import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.businessLogic.Concierge;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.controllers.fragments.TripsListViewFragment;
import edu.wisc.drivesense.controllers.fragments.MenuFragment;
import edu.wisc.drivesense.controllers.fragments.SettingsFragment;
import edu.wisc.drivesense.controllers.fragments.StatsFragment;
import edu.wisc.drivesense.server.ConnectionManager;
import edu.wisc.drivesense.views.resideMenu.ResideMenu;

import java.util.List;


public class LandingActivity extends FragmentActivity implements View.OnClickListener,
        MenuFragment.MenuDelegate, SettingsFragment.OnFragmentInteractionListener,
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

        //TESTING
        localTEST();
//        Concierge.initializeConcierge();
//        localTEST();

        //creates the dragging menu
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(ResideMenu.imageForTimeOfDay());
        resideMenu.attachToActivity(this);
//        resideMenu.addIgnoredView(findViewById(R.id.trips));

        //pull fragments
        fragmentList = (TripsListViewFragment) getFragmentManager().findFragmentById(R.id.trips);
        fragmentStats = (StatsFragment) getFragmentManager().findFragmentById(R.id.stats);

        //initialize table with trips
        loadUser();

        //register for updates to the saved trips
        //TODO: unregister the receiver onPause
        IntentFilter intentFilter = new IntentFilter(BackgroundRecordingService.TRIPS_UPDATE);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadUser();
            }
        }, intentFilter);
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
    public void loadUser() {
        User user = Concierge.getCurrentUser();
        fragmentList.setUser(user);
    }


    /* Button Callbacks */
    public void onRightButtonClick(View view) {
        //test data method
        onLoadLocal();
    }

    public void onButtonLeftClick(View view) {
        //uploading test

        User user = Concierge.getCurrentUser();
        List<Trip> trips = Trip.find(Trip.class, "user = ?", "" + user.getId());

        if (trips.size() == 0) {
            Log.e(TAG, "Cant test upload, no trips found");
            return;
        }

        ConnectionManager api = new ConnectionManager(this);
        api.convertUploadTrip(trips.get(0), user, null);
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


    /* TESTING and DEBUG */
    /**
     * Debug method used for whatever is needed-- most likely loading local sensor traces as trips.
     * All existing trips are dropped, new trip is loaded into the database.
     *
     * Intentionally on the main thread so you cant break things while the load is running.
     *
     * You don't have to do this more than once-- the trip stays loaded.
     */
    public void onLoadLocal() {
        Log.d(TAG, "Starting local trip load. This will take a while.");

        IntentFilter intentFilter = new IntentFilter(BackgroundRecordingService.BACKGROUND_ACTION);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BackgroundRecordingService.getInstance().localDataTester();
                Log.d(TAG, "Finsihed local trip load. Press the 'Trips' button to see the loaded trip.");
                stopServiceTEST();

            }
        }, intentFilter);

        startService(new Intent(this, BackgroundRecordingService.class));

    }

    private void stopServiceTEST() {
        stopService(new Intent(this, BackgroundRecordingService.class));
    }

    /**
     * Miscellanious testing method
     */
    private void localTEST() {
        List<User> users = User.find(User.class, "logged_in = '1'");
        Log.e(TAG, "Number of users logged in: " + users.size());

        users = User.find(User.class, "logged_in = ?", "1");
        Log.e(TAG, "Number of users logged in: " + users.size());

        List<User> demo = User.find(User.class, "backend_id = ?", "-7");
        Log.e(TAG, "Number of demo users: " + demo.size());
    }
}