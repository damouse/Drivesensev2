package edu.wisc.drivesense.businessLogic;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.SugarDatabse;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.scoring.DrivingAnalyst;
import edu.wisc.drivesense.scoring.common.ScoreKeeperDelegate;

/**
 * Created by Damouse on 12/16/2014.
 *
 * Holds a recording trip and any active readings from that trip, as well as
 * receiving callbacks from the analyst
 */
public class TripRecorder implements ScoreKeeperDelegate {
    private static final String TAG = "TripRecorder";
    private Trip trip;
    private List<MappableEvent> events;
    private DrivingAnalyst analyst;


    /**
     * Begins a new trip with the assigned analyst providing data.
     */
    public TripRecorder() {
        trip = new Trip();
        trip.user = SugarDatabse.getActiveUser();
        trip.save();
    }


    /* Public Interface */
    public void endTrip() {
        //score trip
        //broadcast trips changed
        trip = null;
    }

    public Trip getTrip() {
        return trip;
    }

    public boolean isRecording() {
        return trip != null;
    }


    /* Analyst Callbacks */
    /**
     * Called when the scorekeeper determines new driving events have occured
     */
    public void newPatterns(ArrayList<MappableEvent> events) {
        if (trip == null) {
            Log.d(TAG, "Callbacks from Analyst without an active trip!");
            return;
        }

        //not async! be careful
        for (MappableEvent event: events)
            event.trip = trip;

        MappableEvent.saveInTx(events);

        //only do this if the map is active-- TODO: implement way of notifying
        if (true)
            events.addAll(events);
    }
}
