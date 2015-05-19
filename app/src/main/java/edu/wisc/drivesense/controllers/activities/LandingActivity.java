package edu.wisc.drivesense.controllers.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.businessLogic.BackgroundState;
import edu.wisc.drivesense.businessLogic.Concierge;
import edu.wisc.drivesense.businessLogic.Seatbelt;
import edu.wisc.drivesense.controllers.fragments.MenuFragment;
import edu.wisc.drivesense.controllers.fragments.SettingsFragment;
import edu.wisc.drivesense.controllers.fragments.StatsFragment;
import edu.wisc.drivesense.controllers.fragments.TripsListViewFragment;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.SugarDatabse;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.server.ConnectionManager;
import edu.wisc.drivesense.utilities.BroadcastHelper;
import edu.wisc.drivesense.views.resideMenu.ResideMenu;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class LandingActivity extends FragmentActivity implements View.OnClickListener,
        MenuFragment.MenuDelegate, TripsListViewFragment.TripSelectedListener, Observer {

    private static final String TAG = "LandingActivity";

    private boolean isListeningTripUpdate;

    private ResideMenu resideMenu;
    private TripsListViewFragment fragmentList;
    private StatsFragment fragmentStats;
    private SettingsFragment fragmentSettings;
    private MenuFragment fragmentMenu;

    private Button buttonStatsLeft;
    private Button buttonStatsRight;

    BroadcastReceiver tripsUpdatedReceiver;
    BroadcastReceiver stateUpdateReceiver;


    /* Boilerplate and Init */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        SugarDatabse.clearDatabase();
        isListeningTripUpdate = false;

        //creates the drawer menu
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(ResideMenu.imageForTimeOfDay());
        resideMenu.attachToActivity(this);
        //resideMenu.addIgnoredView(findViewById(R.id.trips));

        //pull views
        fragmentList = (TripsListViewFragment) getFragmentManager().findFragmentById(R.id.trips);
        fragmentStats = (StatsFragment) getFragmentManager().findFragmentById(R.id.stats);
        buttonStatsLeft = (Button) findViewById(R.id.buttonStatsLeft);
        buttonStatsRight = (Button) findViewById(R.id.buttonStatsRight);

        //initialize table with trips
        loadUser();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //if we're recieving updates from the recorder turn them off
        if (isListeningTripUpdate) {
            //if the recording stopped, then the recorder is set to null. This occurs if recording while
            // this activity is still visible
            if (BackgroundRecordingService.getInstance().recorder != null)
                BackgroundRecordingService.getInstance().recorder.deleteObserver(this);

            //but no matter what, set the flag to false
            isListeningTripUpdate = false;
        }

        BackgroundRecordingService.checkAndDestroy(this);
        BroadcastHelper.unregisterReceiver(tripsUpdatedReceiver, this);
        BroadcastHelper.unregisterReceiver(stateUpdateReceiver, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackgroundRecordingService.checkAndStart(this);
        checkRegisterBackground();
        displayLastTrip();
        setButtonStates();

        //notify us of changes in trips (when new trips are added, for example)
        tripsUpdatedReceiver = BroadcastHelper.registerForBroadcast(BackgroundRecordingService.TRIPS_UPDATE, this, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //reloads the user on every trip update? That seems excessive
                loadUser();
            }
        });

        stateUpdateReceiver = BroadcastHelper.registerForBroadcast(BackgroundRecordingService.STATE_UPDATE, this, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                backgroundStateChanged();
                checkRegisterBackground();
            }
        });

        if (BackgroundState.getState() != BackgroundState.State.UNINITIALIZED && BackgroundRecordingService.getInstance() != null)
            BackgroundRecordingService.getInstance().uploadTrips();
    }

    /**
     * Conditional registration for updates from the trip recorder on new events
     */
    private void checkRegisterBackground() {
        if (BackgroundState.getState() == BackgroundState.State.AUTOMATIC_RECORDING ||
                BackgroundState.getState() == BackgroundState.State.MANUAL_RECORDING) {
            if (!isListeningTripUpdate) {
                isListeningTripUpdate = true;
                BackgroundRecordingService.getInstance().recorder.addObserver(this);
            }
        }
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
        setButtonStates();
        displayLastTrip();

        if (BackgroundState.getState() != BackgroundState.State.UNINITIALIZED)
            BackgroundRecordingService.getInstance().uploadTrips();
    }

    /**
     * Called when the user changes a setting in the menus. The important setting here is
     * AutomaticRecording-- start the service is this is called here
     *
     * The background is started or stopped here.
     */
    public void userStateChanged() {
        User user = Concierge.getCurrentUser();
        displayLastTrip();

        //update BackgroundState with any change in state
        if (BackgroundRecordingService.getInstance() != null) {
            BackgroundState background = BackgroundRecordingService.getInstance().stateManager;
            background.setAutomaticRecording(user.isAutomaticRecording());
            background.setAutomaticUnpoweredRecording(user.isAutomaticUnpoweredRecording());
        }
    }

    private void backgroundStateChanged() {
        setButtonStates();
        checkRegisterBackground();
    }

    /**
     * If this is the first time the app launches show a tutorial screen
     */
    private void showTutorial() {
        User user = Concierge.getCurrentUser();
        if (user.demoUser()) {
            List<Trip> trips = Trip.find(Trip.class, "user = ?", "" + user.getId());
            if(trips.size() == 0) {
                String tutorial = "Welcome! KnowMyDrive is an app for tracking and assesing your driving. You can " +
                        "either manually record your drives or turn on automatic recording. With automatic recording" +
                        " KnowMyDrive will detect driving while plugged in and moving. Swipe from left or right to see settings. ";
                new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("KnowMyDrive")
                        .setContentText(tutorial)
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
        }
    }


    /* Button Callbacks and Management */
    /**
     * This button has two states: start recording or stop recording.
     *
     * If we are actively recording, ask for confirmation before starting or stopping.
     *
     * This call will trigger sensor warnings if they are disabled.
     * @param view
     */
    public void onRightButtonClick(View view) {
        if (false) {
            onLoadLocal();
            return;
        }

        User user = Concierge.getCurrentUser();
        BackgroundState.State currentState = BackgroundState.getState();

        String message = Seatbelt.cantRecordMessage(this, user);
        if (message != null) {
            Seatbelt.notifyUser("Can't record now! " + message, this);
        }

        //show access to the recording trip
        if (BackgroundRecordingService.getInstance().recording()) {
            Intent intent = new Intent(LandingActivity.this, RecordingTripActivity.class);
            LandingActivity.this.startActivity(intent);
        }

        setButtonStates();
    }

    /**
     * Button displays the current state.
     *
     * If recording, show the recording trip.
     * If not recording, say WHY: listening, waiting for trip to start, or missing sensors.
     *
     * Show alert if this is the case.
     * @param view
     */
    public void onButtonLeftClick(View view) {
        if (false) {
            uploadingTest();
            return;
        }

        User user = Concierge.getCurrentUser();
        boolean recording = BackgroundRecordingService.getInstance().recording();

        if (user.isAutomaticRecording()) {
            SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Automatic Recording is on!")
                    .showCancelButton(true);

            if (recording) {
                String message = "You are currently recording a trip using Automatic Recording. KnowMyDrive " +
                        "will automatically stop recording when the trip ends.";

                dialog.setContentText(message)
                        .setConfirmText("Stop Recording")
                        .setCancelText("End Automatically")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                BackgroundRecordingService.getInstance().stateManager.adviseTripEnd();
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
            else {
                String message = "You are currently listening for a trip to start using Automatic Recording. KnowMyDrive " +
                        "will automatically start recording when it detects driving.";

                dialog.setContentText(message)
                        .setConfirmText("Record")
                        .setCancelText("Keep Listening")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                BackgroundRecordingService.getInstance().stateManager.adviseTripStart();
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
        }

        //Not automatic recording. If we can start recording manually do so, else explain why not
        else {
            String recordingError = Seatbelt.cantRecordMessage(this, user);

            if (recordingError == null) {
                Log.d(TAG, "Sending manual trigger to service");
                BackgroundRecordingService.getInstance().stateManager.manualRecordingTrigger();
            }
            else {
                Log.d(TAG, "Cannot start manual recording-- showing an alert!");
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Missing sensors!")
                        .setContentText(recordingError)
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
        }

        setButtonStates();
    }

    private void setButtonStates() {
        User user = Concierge.getCurrentUser();
        BackgroundState.State currentState = BackgroundState.getState();

        if (currentState == BackgroundState.State.UNINITIALIZED) {
            Log.w(TAG, "There is no background state. Cannot set buttons");
            return;
        }

        String rightMessage;
        String leftMessage = "START RECORDING";
        if (currentState == BackgroundState.State.AUTOMATIC_RECORDING || currentState == BackgroundState.State.MANUAL_RECORDING) {
            rightMessage = "SEE RECORDING TRIP";
            leftMessage = "STOP RECORDING";
        }
        else if (currentState == BackgroundState.State.AUTOMATIC_STOP_LISTEN) {
            rightMessage = "LISTENING FOR TRIP START";
        }
        else {
            //At this point we are either in STOP_WAIT or MANUAL_LISTEN-- check if errors appear
            rightMessage = Seatbelt.cantRecordMessageShort(this, user);
            if (rightMessage == null) {
                //null message means everything is good to go-- we have to be in MANUAL_LISTEN
                rightMessage = "READY TO RECORD";
            }
        }

        buttonStatsRight.setText(rightMessage);
        buttonStatsLeft.setText(leftMessage);
    }


    /* Changes in Trip State */
    /**
     * Updates the stats fragment with the most recent trip
     */
    public void displayLastTrip() {
        //If currently recording then show that trip
        if (BackgroundState.getState() == BackgroundState.State.AUTOMATIC_RECORDING || BackgroundState.getState() == BackgroundState.State.MANUAL_RECORDING) {
            if (BackgroundRecordingService.getInstance() != null &&
                    BackgroundRecordingService.getInstance().recorder != null &&
                    BackgroundRecordingService.getInstance().recorder.getTrip() != null) {

                fragmentStats.setTrip(BackgroundRecordingService.getInstance().recorder.getTrip());
                return;
            }
        }

        User user = Concierge.getCurrentUser();
        List<Trip> trips = Trip.find(Trip.class, "user = ?", "" + user.getId());

        if(trips.size() == 0)
            fragmentStats.setTrip(null);
        else {
            //TODO: get most recent trip-- not the last trip!
            fragmentStats.setTrip(trips.get(0));
        }
    }

    /**
     * Be warned-- the data is events, not the trip. It is meant for processing by the map, this
     * is just a notification that patterns (and the trip's score) have changed.
     * @param observable
     * @param data
     */
    @Override
    public void update(Observable observable, Object data) {
        Trip trip = BackgroundRecordingService.getInstance().recorder.getTrip();

        if (trip == null) {
            Log.e(TAG, "Received an update for a trip that doesnt exist!");
        }
        fragmentStats.setTrip(trip);
    }

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

        IntentFilter intentFilter = new IntentFilter(BackgroundRecordingService.BACKGROUND_STARTED);
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

    private void uploadingTest() {
        User user = Concierge.getCurrentUser();
        List<Trip> trips = Trip.find(Trip.class, "user = ?", "" + user.getId());

        if (trips.size() == 0) {
            Log.e(TAG, "Cant test upload, no trips found");
            return;
        }

        user.backendId = 1;
        ConnectionManager api = new ConnectionManager(this);
        api.convertUploadTrip(trips.get(0), user, null);
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "Unknown view was touched! " + v.toString());
    }
}
