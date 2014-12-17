package edu.wisc.drivesense.activities;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.businessLogic.BackgroundState;
import edu.wisc.drivesense.model.SugarDatabse;
import edu.wisc.drivesense.model.Trip;
//import edu.wisc.drivesense.server.DrivesensePreferences;
import edu.wisc.drivesense.views.PinMapFragment;
import edu.wisc.drivesense.views.TripsListViewFragment;
import edu.wisc.drivesense.views.ViewAnimator;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

//////////////////////////////////////////////////////////////////////////////////////////
/**
DriveSense v2.1

The following is a breakdown of class structure based on package folder. See comments on individual
classes to learn more.

 Activities
    Main Activity
        Entry point of the application. Responsible for all major UI interactions with the user.
        Starts the Background service. Owns the mapFragment. Does not directly interact with
        the database (but it should.)

    PreferenceActivity
        Shows settings to the user. Allows the user to login. Could start or stop the Background
        Service based on changes to recording settings.

 Misc
    Miscellanious testing objects. Not important for app functionality.

 Model
    BackgroundRecordingService
        The core of the app. Manages app state, runs in the background perpetually. Opened on
        app start. Performs the recording and saving of trips.

    DatabaseManager
        ORMLite based database wrapper. Provides access to Model objects through asynchronous
        tasks. Saves, creates, updates, etc.

    Objects
        All of the model objects. See individual classes for more information. The Trip object
        is the central object.

 Scoring
    ScoreKeeper
        Scores trips. Called from DatabaseManager and BackgroundService.

 Sensors
    Listener
        Object responsible for recognizint trip starts and trip stops. Owned by SensorMonitor. Turned
        on or off by BackgroundService. Calls back up to BackgroundService when it makes a decision.

    PowerListener
        Listens for changes in power state. Owned by and alerts SensorMonitor when the power
        state changes.

    SensorMonitor
        The core class of this package. Owned by BackgroundService. Receives all input from all sensors
        and routes them accordingly.

    WiFiListener
        Listens for changes in internet connectivity and implements some convenience methods. Owned
        by and alerts BackgroundService.

 Server
    ConnectionManager
        Object responsible for communication with the backend. Implements login and trip uploading.

    ConnectionManagerCallback
        Interface for asynchronous callbacks from ConnectionManager. Should be refactored.

    DrivesensePreferences
        Wraps preference storage for non-database storage. Should be removed.

 Views
    OpenSansTextView
        Simple subclass that makes all textviews share the same font.

    PinMapFragment
        Wrapper class for the main map. Implements map drawing as needed. Owned by MainActivity.

    TripMapInformation
        Wrapper class for a trip coordinate. Makes the coordinate easily mappable for the PinMapFragment.
        Owned by PinMapFragment.

    TripsListViewFragment
        Implementation of the trips list view. Owned by MainActivity.

    ViewAnimator
        Convenience object for simple sliding animations on the MainActivity.
*/
//////////////////////////////////////////////////////////////////////////////////////////


/**
 * The main entry point for the application. Sets up menu, map, and handles events.
 *
 * @author Damouse
 */
public class MainActivity extends Activity implements Observer {
    private static final String TAG = "MainActivity";

    //flag indicating state
    boolean displayingTrips;
    boolean busy = false;

    //view elements pulled for later reference
    private TripsListViewFragment fragmentList;
    private PinMapFragment fragmentMap;
    private Button recordButton;

    private LinearLayout mapLayout;
    private SmoothProgressBar progressBar;

    //animator
    ViewAnimator animator;

    //list of trips loaded from the BackgroundService.
    List<Trip> trips;


    //keeps track of the timer used to update UI with trip timer
    Handler timerHandler;
    boolean shouldUpdateTimer;
    int durationCounter = 0;

    Runnable timerRunnable= new Runnable() {
        @Override
        public void run() {
            durationCounter += 1;
            //Log.d(TAG, "New duration counter: " + durationCounter);
            TextView duration = (TextView)findViewById(R.id.textview_duration);
            duration.setText("" + durationCounter + " seconds");

            if(BackgroundRecordingService.getInstance().recording())
                timerHandler.postDelayed(this, 1000);
            else
                durationCounter = 0;
        }
    };


    /* Boilerplate Activity Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Main Activity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().hide();

        recordButton = (Button)findViewById(R.id.record);

        //set state flags to false
        displayingTrips = false;

        //pull refs to the fragments for later use
        FragmentManager fragmentManager = this.getFragmentManager();
        fragmentList = (TripsListViewFragment)fragmentManager.findFragmentById(R.id.list);
        fragmentMap = (PinMapFragment) fragmentManager.findFragmentById(R.id.map);
        progressBar = (SmoothProgressBar) findViewById(R.id.progress_bar);

        animator = new ViewAnimator((LinearLayout)findViewById(R.id.animation_layout), findViewById(R.id.slide_in), (LinearLayout)findViewById(R.id.slide_in_trips));

        //slide-in timer
        timerHandler = new Handler();

        //calendar widget
//        Calendar nextYear = Calendar.getInstance();
//        nextYear.add(Calendar.YEAR, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //only start ModelManager if the service is not already running
        if (BackgroundState.getState() == BackgroundState.State.UNINITIALIZED) {
            startService(new Intent(this, BackgroundRecordingService.class));
        }
        else {
            //Present a drawing trip to the map
            Trip activeTrip = BackgroundRecordingService.getInstance().getActiveTrip();
            if (activeTrip != null) {
                toggleRecordEffectsOn();
                //TODO: update the map and the slidin with relevant information
            }
        }

        BackgroundRecordingService.stateManager.addObserver(this);
        updateRecordButton();
    }

    @Override
    public void onWindowFocusChanged (boolean focus) {
        if (focus) {
            animator.initTrips();

            //start the spinner off
            progressBar.progressiveStop();

            if (!BackgroundRecordingService.getInstance().recording())
                toggleRecordEffectsOn();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        BackgroundRecordingService.stateManager.deleteObserver(this);

        //if background recording is turned off and we are not currently recording, turn off the background service
        if (!BackgroundRecordingService.getInstance().stateManager.getAutomaticRecording())
            stopService(new Intent(this, BackgroundRecordingService.class));
    }

    //segue to the settingsActivity
    public void settingsClick(View view) {
        Intent myIntent = new Intent(MainActivity.this, PreferenceActivity.class);
        MainActivity.this.startActivity(myIntent);
    }


/* UI Display, including helper methods */
    /**
     * Toggles the recording of the user's location and other sensor parameters. Creates a new trip when this happens.
     * Note that this class doesn't do most of the recording, this is just where the triggering happens
     */
    public void toggleRecord(View view) {
        SugarDatabse.deleteTrips(null);

        if (BackgroundRecordingService.getInstance().stateManager.getAutomaticRecording())
            return;

        BackgroundRecordingService.getInstance().stateManager.manualRecordingTrigger();
        toggleRecordEffectsOn();
    }

    /**
     * Helper function to the above. The above function relies on this to change the visual state of
     * the activity when the recording state changes, but this function does not directly trigger
     * the BackgroundService.
     */
    private void toggleRecordEffectsOn() {
        if(!BackgroundRecordingService.getInstance().recording()) {
            //stop recording trip
            animator.dismissSlidein();

            shouldUpdateTimer = false;

            //alert the map: it should stop following the user with a polyline
            fragmentMap.stopRecording();
        }
        else {
            //start recording trip
            animator.presentSlidein();

            //update slidein labels, counters, etc
            shouldUpdateTimer = true;
            timerHandler.post(timerRunnable);
            TextView name = (TextView)findViewById(R.id.textview_name);
            name.setText("Trip #");

            //alert the map: start drawing a polyline behind the user's tstartRecordingrail
            fragmentMap.startRecording();
        }

        updateRecordButton();
    }

    /**
     * Display all of the trips as pins and overlays on the map as well as managing some animations
     */
    public void toggleDisplayTrips(View view) {
//        progressBar.progressiveStop();

        if(displayingTrips) {
            //animate the trips display offscreen, clear the map
            animator.dismissTrips();
            //animation_layout
            fragmentMap.hideTrips();
        }
        else {
            //present trips list animation, draw the pins and routes
            animator.presentTrips();
            fragmentMap.showTrips();
        }

        displayingTrips = !displayingTrips;
    }

    /**
     * If play is present, switch to pause. If prefs says we're automatic recording, do nothing.
     *
     * Changes the color of the button based on the active/manual recording state. If auto is on,
     * change the button to Grey to indicate its untouchable.
     */
    private void updateRecordButton() {
        int icon;
        BackgroundState.State current = BackgroundState.getState();

        if (current == BackgroundState.State.AUTOMATIC_RECORDING)
            icon = R.drawable.play_grey;
        else if (current == BackgroundState.State.AUTOMATIC_STOP_LISTEN || current == BackgroundState.State.AUTOMATIC_STOP_WAIT)
            icon = R.drawable.pause_grey;
        else if (current == BackgroundState.State.MANUAL_RECORDING)
            icon = R.drawable.pause;
        else if (current == BackgroundState.State.MANUAL_LISTEN)
            icon = R.drawable.play;
        else {
            Log.e(TAG, "Unknown background state- " + current);
            return;
        }

        recordButton.setBackgroundResource(icon);
    }


    /* Methods that communicate with the background service */
    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof BackgroundState) {
            BackgroundState.State newState = (BackgroundState.State) o;

            //turn off recording effects if our state does not agree with service state
            toggleRecordEffectsOn();
        }
    }


    /* External interface: map and list */
    /**
     * Called from the ListViewFragment when a cell is clicked. Notify the map it should
     * display the trip at the given index. If selected is false, then its a deselect, and the
     * map should redisplay all trips.
     *
     * Also returns the trip at the selected index.
     *
     * Assumes we are currently displaying trips (how else would the touch happen?)
     */
    public Trip listFragmentItemClicked(int index, boolean selected) {
        Trip trip = trips.get(index);

        if (selected)
            fragmentMap.selectTrip(trip);
        else
            fragmentMap.deselctTrip();


        return trip;
    }

    /**
     * The list has called back and informed us that the user has deleted a trip. Tell the map
     * the trip was deleted so it can remove it.
     * @param trip
     */
    public void listFragmentDeletedTrip(Trip trip) {
        if (trip == null || fragmentMap == null) {
            Log.e(TAG, "Error! null trip or null map!");
            return;
        }

        fragmentMap.deleteTrip(trip);
    }

    /**
     * Toggle spinner functionality. Called from Background servie on loads and from the list on API
     *
     */
    public void setBusy(boolean set) {
        if (set)
            progressBar.progressiveStart();
        else
            progressBar.progressiveStop();

        busy = set;
    }
}

