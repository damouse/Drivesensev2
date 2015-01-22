package edu.wisc.drivesense.businessLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;


import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;

import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.scoring.common.LocalDataTester;
import edu.wisc.drivesense.sensors.TripListener;
import edu.wisc.drivesense.sensors.WifiListener;
import edu.wisc.drivesense.server.ConnectionManager;
import edu.wisc.drivesense.utilities.Ticker;
import edu.wisc.drivesense.sensors.PowerListener;
import edu.wisc.drivesense.sensors.SensorMonitor;
import edu.wisc.drivesense.server.ServerLogger;
import edu.wisc.drivesense.views.TaskbarNotifications;

/**
 * This is the main "interface" class of the model. This class acts as an intermediary for
 * all model data in or outbound as well as tracking the active trip, managing sensorreadings,
 * and interacting with the database wrapper.
 * <p/>
 * This is (currently!) the only class that is allowed access to the database wrapper. This happens opaquely, each update
 * to trips is pushed to the database automatically. Please see the comment block on top of DatabaseManager for
 * details on db implementation.
 * <p/>
 * --Service/Singleton
 * ModelManager is started as a service in all cases. Its access through the singleton accessor ensures
 * that multiple modules in the application can access this service. DO NOT INSTANTIATE IT YOURSELF.
 * <p/>
 * Main role is managing the state of the background service and recording services as a whole.
 * Inputs
 * - Manual Command (from record button on main activity)
 * - Current State (active, passive, recording, listening)
 * - Listener (advise from sensor montitor on whether a trip has started or stopped)
 * - Power state (connected, disconnected)
 * <p/>
 * TODO
 * Check for GPS avaliability
 * Check saved trips for minimum length and duration before committing
 *
 * @author Damouse
 */
public class BackgroundRecordingService extends Service implements Observer {
    private static final String TAG = "BackgroundService";
    public static final String BACKGROUND_ACTION = "edu.wisc.drivesense.background_status";
    public static final String TRIPS_UPDATE = "edu.wisc.drivesense.trips_update";
    private static BackgroundRecordingService instance = null; //singleton ivar
    public static final boolean DEBUG = false;

    public SensorMonitor monitor;
    public BackgroundState stateManager;
    public TripRecorder recorder;

    boolean recording;
    boolean listening;
    boolean firstLoad = true;

    private PowerListener power;
    private TripListener listener;
    private ServerLogger serverLogger;

    //taskbar status
    private TaskbarNotifications taskbar;

    //testing
    private Ticker ticker;


    /* Boilerplate */
    // Singleton accessor
    public static BackgroundRecordingService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "BackgroundService started");

        instance = this;

        //module alloc init
        monitor = new SensorMonitor(this);
        power = new PowerListener(this);
        listener = new TripListener();
        serverLogger = new ServerLogger(this);
        taskbar = new TaskbarNotifications(this);

        stateManager = new BackgroundState();
        stateManager.addObserver(this);

        initState();

        //Broadcast on start
        Intent startupIntent = new Intent(BACKGROUND_ACTION);

        try {
            sendBroadcast(startupIntent);
        } catch (NullPointerException ex) {
            Log.e(TAG, "Intent broadcast to the main activity failed.");
        }

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ModelManager destroyed");
        instance = null;
        monitor = null;
        taskbar = null;

        unregisterReceiver(power);
        stateManager.deleteObserver(this);
        stateManager.setServiceOn(false);
    }

    /**
     * Initialize the state manager with starting values
     */
    private void initState() {
        stateManager.addObserver(this);
        stateManager.setServiceOn(true);
        User user = Concierge.getCurrentUser();

        stateManager.setGpsAvailable(monitor.gpsEnabled());
        stateManager.setPowered(PowerListener.isPluggedIn(this));
        stateManager.setAutomaticRecording(user.isAutomaticRecording());
        stateManager.setAutomaticUnpoweredRecording(user.isAutomaticUnpoweredRecording());
    }


    /* Recording Methods */
    public boolean recording() {
        return recording;
    }

    private void setRecording(boolean state) {
        if (state && !recording) {
            //Check to make sure a trip isnt already being recorded
            if (recorder != null) {
                Log.e(TAG, "Could not create a new trip, one already exists");
                return;
            }

            recorder = new TripRecorder(Concierge.getCurrentUser(), this);
        } else if (!state && recording) {
            if (recorder == null) {
                Log.e(TAG, "Can't end the trip, no trip is active!");
                return;
            }

            recorder.endTrip();
            recorder = null;
        } else {
            //Log.e(TAG, "Mismatched states! Received newRecordingState: " + state + " but old recording was: " + recording);
            return;
        }

        recording = state;
    }

    private void setRecordingAndListenening(boolean rec, boolean listen) {
        //turn check if GPS needs to be turned on or off
        if (rec) {
            monitor.startCollecting();
        } else {
            monitor.stopCollecting();

            if (listen)
                monitor.startCollectingGPS();
            else
                monitor.stopCollectingGPS();
        }

        setRecording(rec);
        listening = listen;

        if (!listening)
            listener.stopListening();
    }


    /* Reading Modifiers */
    public void newGpsReading(Location location) {
        if (listening)
            listener.updateLocation(location);

        //pass off straight to the other method for processing, do no more here
        newReading(new Reading(location));
    }

    public void newReading(Reading reading) {
        if (recorder == null) {
            Log.e(TAG, "Recorder is null as readings come in!");
            return;
        }

        recorder.newReading(reading);
    }


    /* Callbacks from Listeners and State */
    /**
     * Called from BackgroundState upon state changes
     *
     * @param observable The object that is returning the observation
     * @param o          Most likely a State value
     */
    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof BackgroundState) {
            BackgroundState.State newState = (BackgroundState.State) o;

            if (newState == BackgroundState.State.AUTOMATIC_RECORDING || newState == BackgroundState.State.MANUAL_RECORDING)
                setRecordingAndListenening(true, true);

            if (newState == BackgroundState.State.AUTOMATIC_STOP_WAIT || newState == BackgroundState.State.MANUAL_LISTEN)
                setRecordingAndListenening(false, false);

            if (newState == BackgroundState.State.AUTOMATIC_STOP_LISTEN)
                setRecordingAndListenening(false, true);

            taskbar.updateServiceNotifcation(stateManager.getStateString());
        }
    }

    /**
     * Tries to upload trips that have not yet been pushed to the backend.
     * TODO: move to Connection manager-- might have to call this from somewhere else.
     */
    public void uploadTrips() {
        User user = Concierge.getCurrentUser();

        //TODO: optionally load over cellular with a user preference
        if (user.demoUser() || !WifiListener.isConnected(this))
            return;

        List<Trip> trips =  Trip.find(Trip.class, "scored = true and uploaded = false and user = ?", "" + user.getId());

        if (trips.size() == 0)
            return;

        Log.d(TAG, "Attempting to upload " + trips.size() + "trips");
        ConnectionManager api = new ConnectionManager(this);

        for (Trip trip: trips)
            api.convertUploadTrip(trip, user, null);

    }

    /**
     * Called when WiFi changes state
     */
    public void wifiTurnedOn() {
        //try and upload trips that have not yet been uploaded
        uploadTrips();
    }

    public Trip getActiveTrip() {
        if (recorder != null)
            return recorder.getTrip();
        return null;
    }


    /* TESTING */
    public void localDataTester() {
//        Trip.deleteAll(Trip.class);
//        MappableEvent.deleteAll(MappableEvent.class);

        //loadTestData();
        loadAndFeed();
    }

    void loadAndFeed() {
        Log.d(TAG, "Starting file load...");
        recorder = new TripRecorder(Concierge.getCurrentUser(), this);
        LocalDataTester tester = new LocalDataTester(recorder, this);

        tester.readAndLoadTestData(this);
        recorder.endTrip();

        Log.d(TAG, "Finished load");
    }
}