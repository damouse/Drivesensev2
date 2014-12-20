package edu.wisc.drivesense.controllers;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import edu.wisc.drivesense.businessLogic.Bengal;
//import edu.wisc.drivesense.server.DrivesensePreferences;
import edu.wisc.drivesense.views.PinMapFragment;
import edu.wisc.drivesense.views.TripsListViewFragment;
import edu.wisc.drivesense.views.ViewAnimator;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


/**
 * The main entry point for the application. Sets up menu, map, and handles events.
 *
 * compile 'com.github.nirhart:parallaxscroll:1.0'
 * https://github.com/nirhart/ParallaxScroll
 *
 *
 *
 * @author Damouse
 */
public class MainActivity extends Activity implements Observer {
    private static final String TAG = "MainActivity";
    public static final String BACKGROUND_ACTION = "edu.wisc.drivesense.background_status";

    //flag indicating state
    boolean displayingTrips;
    boolean busy = false;

    //Manages trips on the front end-- for Map and List
    private Bengal bengal;

    //view elements pulled for later reference
    private Button recordButton;

    private LinearLayout mapLayout;
    private SmoothProgressBar progressBar;

    BackgroundStatusReceiver statusBroadcastReceiver;

    //animator
    ViewAnimator animator;


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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().hide();
        recordButton = (Button)findViewById(R.id.record);
        displayingTrips = false;

        //pull refs to the fragments for later use

        progressBar = (SmoothProgressBar) findViewById(R.id.progress_bar);

        animator = new ViewAnimator((LinearLayout)findViewById(R.id.animation_layout), findViewById(R.id.slide_in), (LinearLayout)findViewById(R.id.slide_in_trips));

        //slide-in timer
        timerHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //if the service is null it hasn't started yet. Start it and register for a calback
        if (BackgroundRecordingService.getInstance() == null) {
            statusBroadcastReceiver = new BackgroundStatusReceiver();
            IntentFilter intentFilter = new IntentFilter(BACKGROUND_ACTION);
            registerReceiver(statusBroadcastReceiver, intentFilter);

            startService(new Intent(this, BackgroundRecordingService.class));
        }
        else {
            setupWorkers();
        }

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

        if (statusBroadcastReceiver != null)
            unregisterReceiver(statusBroadcastReceiver);

        //if background recording is turned off and we are not currently recording, turn off the background service
        if (BackgroundRecordingService.getInstance() != null && !BackgroundRecordingService.getInstance().stateManager.getAutomaticRecording())
            stopService(new Intent(this, BackgroundRecordingService.class));

        teardownWorkers();
    }

    /**
     * The (large) set of worker objects that do the lifting
     */
    private void setupWorkers() {
        FragmentManager fragmentManager = this.getFragmentManager();
        TripsListViewFragment fragmentList = (TripsListViewFragment)fragmentManager.findFragmentById(R.id.list);
        PinMapFragment fragmentMap = (PinMapFragment) fragmentManager.findFragmentById(R.id.map);
        bengal = new Bengal(fragmentMap, fragmentList);


    }

    /**
     * Cleanup for workers
     */
    private void teardownWorkers() {
        //for whatever reason, initialize was never called
        if (bengal == null)
            return;
    }


    /* Inputs from UI */
    public void settingsClick(View view) {
        bengal.load(BackgroundRecordingService.getInstance().concierge.getCurrentUser());
//        Intent myIntent = new Intent(MainActivity.this, PreferenceActivity.class);
//        MainActivity.this.startActivity(myIntent);
    }

    public void toggleRecord(View view) {
        //TODO: debugging, remove
        BackgroundRecordingService.getInstance().localDataTester();
        return;

//        if (BackgroundRecordingService.getInstance().stateManager.getAutomaticRecording())
//            return;
//
//        BackgroundRecordingService.getInstance().stateManager.manualRecordingTrigger();
//        toggleRecordEffectsOn();
    }

    private void toggleRecordEffectsOn() {
        if(!BackgroundRecordingService.getInstance().recording()) {
            //stop recording trip
            animator.dismissSlidein();

            shouldUpdateTimer = false;

            //alert the map: it should stop following the user with a polyline
//            fragmentMap.stopRecording();
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
//            fragmentMap.startRecording();
        }

        updateRecordButton();
    }

    public void toggleDisplayTrips(View view) {
        if(displayingTrips) {
            animator.dismissTrips();
            bengal.showAll();
        }
        else {
            bengal.clear();
            animator.presentTrips();
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


    /* Communicating with the Background Service */
    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof BackgroundState) {
            BackgroundState.State newState = (BackgroundState.State) o;

            //if we started recording, let bengal know
            if (newState == BackgroundState.State.AUTOMATIC_RECORDING || newState == BackgroundState.State.MANUAL_RECORDING)
                bengal.setRecordingTrip(BackgroundRecordingService.getInstance().recorder);

            //turn off recording effects if our state does not agree with service state
            toggleRecordEffectsOn();
        }
    }

    /**
     * Broadcastreceiver called when the background service starts up for the first time
     */
    class BackgroundStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupWorkers();
        }
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