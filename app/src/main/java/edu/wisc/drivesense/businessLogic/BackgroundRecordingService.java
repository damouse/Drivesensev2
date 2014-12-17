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
import edu.wisc.drivesense.scoring.common.LocalDataTester;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;
import edu.wisc.drivesense.scoring.neural.modelObjects.TrainingSet;
import edu.wisc.drivesense.sensors.TripListener;
import edu.wisc.drivesense.server.DrivesensePreferences;
import edu.wisc.drivesense.model.ReadingHolder;
import edu.wisc.drivesense.utilities.Ticker;
import edu.wisc.drivesense.scoring.DrivingAnalyst;
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
    public static final boolean DEBUG = false;
    private static final String TAG = "BackgroundService";
    private static BackgroundRecordingService instance = null; //singleton ivar

    //modules
    public SensorMonitor monitor;

    //State Variables
    public static BackgroundState stateManager = new BackgroundState();
    boolean recording;
    boolean listening;

    private PowerListener power;
    private TripListener listener;
    private ServerLogger serverLogger;
    private DrivingAnalyst analyst;
    private TripRecorder recorder;

    //taskbar status
    private TaskbarNotifications taskbar;

    //testing
    private Ticker ticker;
    boolean firstLoad = true;


    /* Boilerplate */
    // Singleton accessor
    public static BackgroundRecordingService getInstance() {
        if (instance == null) {
            instance = new BackgroundRecordingService();
        }

        return instance;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "ModelManager started");

        instance = this;

        //module alloc init
        monitor = new SensorMonitor(this);
        power = new PowerListener(this);
        listener = new TripListener();
        serverLogger = new ServerLogger(this);
        taskbar = new TaskbarNotifications(this);

        //DEBUG
        localDataTester();
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
        DrivesensePreferences preferences = new DrivesensePreferences(this);

        stateManager.setGpsAvailable(monitor.gpsEnabled());
        stateManager.setPowered(PowerListener.isPluggedIn(this));
        stateManager.setAutomaticRecording(preferences.backgroundRecording());
        stateManager.setAutomaticUnpoweredRecording(preferences.backgroundRecordingPower());
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

            recorder = new TripRecorder();
            analyst = new DrivingAnalyst(recorder, this);
        } else if (!state && recording) {
            if (recorder == null) {
                Log.e(TAG, "Can't end the trip, no trip is active!");
                return;
            }

            recorder.endTrip();
            recorder = null;
            analyst = null;
        } else {
            Log.e(TAG, "Mismatched states! Received newRecordingState: " + state + " but old recording was: " + recording);
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
        if (recorder == null)
            return;

        if (analyst == null) {
            Log.d(TAG, "Analyst is null as readings come in!");
            return;
        }

        analyst.newReading(reading);
    }


    /* DrivingAnalyst Callbacks */


    /* Callbacks from Listeners and State */
    /**
     * Called from BackgroundState upon state changes
     * @param observable The object that is returning the observation
     * @param o Most likely a State value
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
     * Called when WiFi changes state
     */
    public void wifiTurnedOn() {
        //try and upload trips that have not yet been uploaded
    }

    public Trip getActiveTrip() {
        if (recorder != null)
            return  recorder.getTrip();
        return null;
    }

    /* TESTING */
    private void localDataTester() {
        //loadTestData();
        //feedTestData();
        loadAndFeed();

//        recorder = new TripRecorder();
//        analyst = new DrivingAnalyst(recorder, this);
//

//
//        Log.d(TAG, "Data Ending");
//        Trip trip = recorder.getTrip();
//        recorder.endTrip();
//
//        List<MappableEvent> events = trip.getEvents();
//
//        Log.d(TAG, "Number of events: " + events.size());
    }

    private void feedTestData() {
        Log.d(TAG, "Starting feed...");
        recorder = new TripRecorder();
        analyst = new DrivingAnalyst(recorder, this);

        LocalDataTester tester = new LocalDataTester(analyst, this);
        tester.feedTestData();

        Log.d(TAG, "Data Ending");
        Trip trip = recorder.getTrip();
        analyst.analyzePeriod();
        recorder.endTrip();

        Log.d(TAG, "Finished feeding test data");
    }

    private void loadTestData() {
        Log.d(TAG, "Starting load...");
        ReadingHolder.deleteAll(ReadingHolder.class);
        LocalDataTester tester = new LocalDataTester(analyst, this);
        tester.saveTestData(this);
        Log.d(TAG, "Finished load");
    }

    void loadAndFeed() {
        Log.d(TAG, "Starting file load...");
        recorder = new TripRecorder();
        analyst = new DrivingAnalyst(recorder, this);
        LocalDataTester tester = new LocalDataTester(analyst, this);

        tester.readAndLoadTestData(this);
        analyst.analyzePeriod();
        recorder.endTrip();

        Log.d(TAG, "Finished load");
    }
}