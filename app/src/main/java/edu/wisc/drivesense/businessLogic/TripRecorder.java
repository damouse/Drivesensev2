package edu.wisc.drivesense.businessLogic;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.model.SugarDatabse;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.scoring.common.DataReceiver;
import edu.wisc.drivesense.scoring.common.GpsThief;
import edu.wisc.drivesense.scoring.common.ScoreKeeperDelegate;
import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.projected.ProjectedScoreKeeper;
import edu.wisc.drivesense.utilities.Utils;

/**
 * Created by Damouse on 12/16/2014.
 *
 * Holds a recording trip and any active readings from that trip, as well as
 * receiving callbacks from the analyst
 */
public class TripRecorder  {
    private static final String TAG = "TripRecorder";

    private static final boolean useNeuralNetork = false;

    //How long to wait between scoring attempts and how much data to hold
    public int period = 10000; //in milliseconds
    private int memorySize = 10;
    private Context context;

    //receives incoming data from a sensor providor
    DataReceiver receiver;

    private Trip trip;

    private MappableEvent lastEvent;

    //handles Receiver polling based on the period time
    Handler timerHandler;
    boolean recording;
    Runnable timerRunnable;


    /**
     * Begins a new trip with the assigned analyst providing data.
     */
    public TripRecorder(User user, Context context) {
        this.context = context;

        receiver = new DataReceiver(memorySize);
        recording = true;

        trip = new Trip();
        trip.user = user;
        trip.save();

        trip.name = "Trip #" + trip.getId();
        trip.save();

        Log.d(TAG, "Recording trip: " + trip.name);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                analyzePeriod();

                if (recording)
                    timerHandler.postDelayed(this, period);
            }
        };

        //comment this out when operating on local data
        //timerHandler.postDelayed(timerRunnable, period);
    }


    /* Public Interface */
    public void endTrip() {
        recording = false;

        trip.scoreAccels = trip.scoreAccels / trip.numAccels;
        trip.scoreBrakes = trip.scoreBrakes / trip.numBrakes;
        trip.scoreTurns = trip.scoreTurns / trip.numTurns;
        trip.scoreLaneChanges = trip.scoreLaneChanges / trip.numLaneChanges;

        trip.duration = Utils.convertToSeconds(lastEvent.timestamp - trip.timestamp);
        trip.scored = true;

        Log.d(TAG, "Ended trip: " + trip.name);
        trip.save();
        trip = null;

        //try a trip upload
        BackgroundRecordingService.getInstance().uploadTrips();

        //declare that a trip has finished recording and can now be viewed
        Intent update = new Intent(BackgroundRecordingService.TRIPS_UPDATE);

        try {
            BackgroundRecordingService.getInstance().sendBroadcast(update);
        } catch (NullPointerException ex) {
            Log.e(TAG, "Intent broadcast to the main activity failed.");
        }
    }

    public Trip getTrip() {
        return trip;
    }

    public boolean isRecording() {
        return trip != null;
    }


    /* Receiver Callbacks */
    /**
     * Called when the scorekeeper determines new driving events have occured
     *
     * Events must be sorted!
     */
    public void newPatterns(List<MappableEvent> events) {
        Log.d(TAG, "Analyzing Patterns... ");

        if (trip == null) {
            Log.d(TAG, "Callbacks from Analyst without an active trip!");
            return;
        }

        //not async! be careful
        for (MappableEvent event: events) {
            if (event.type == MappableEvent.Type.ACCELERATION) {
                trip.scoreAccels += event.score;
                trip.numAccels++;
            }

            if (event.type == MappableEvent.Type.BRAKE) {
                trip.scoreBrakes += event.score;
                trip.numBrakes++;
            }

            if (event.type == MappableEvent.Type.TURN) {
                trip.scoreTurns += event.score;
                trip.numTurns++;
            }

            if (event.type == MappableEvent.Type.LANE_CHANGE) {
                trip.scoreLaneChanges += event.score;
                trip.numLaneChanges++;
            }

            if (lastEvent != null) {
                trip.distance += GpsThief.distance(lastEvent, event);
            }

            Log.d(TAG, "Saving Patterns... ");
            lastEvent = event;
            event.trip = trip;
        }

        MappableEvent.saveInTx(events);
        trip.save();

        Log.d(TAG, "Done");
    }

    public void newReading(Reading reading) {
        receiver.newReading(reading);
    }

    /* Period and Data Window Management */
    /**
     * Get a window of data from the receiver and feed it into whichever scheme we are using
     * to score. Called from a timer function.
     *
     * This is also where the delegate method createPattern is called.
     */
    public void analyzePeriod() {
        if (receiver == null) {
            Log.e(TAG, "Receiver is unexpectedly null.");
            return;
        }

        DataSetInput period = receiver.getProcessedPeriod();
        TimestampQueue<DrivingPattern> patterns;

        if (period == null) {
            Log.e(TAG, "Incomplete period.");
            return;
        }

        if (useNeuralNetork) {

        }
        else {
            ProjectedScoreKeeper score = new ProjectedScoreKeeper(context);
            patterns = score.getDrivingEvents(period); //obviously temporary
        }

        List<MappableEvent> events = GpsThief.getSparseCoordinates(period.gps, patterns);
        Log.i(TAG, "New Period- GPS coordinates-  " + period.gps.size() + " Patterns: " + patterns.size());
        newPatterns(events);
    }
}