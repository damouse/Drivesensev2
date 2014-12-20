package edu.wisc.drivesense.businessLogic;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.SugarDatabse;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.views.PinMapFragment;
import edu.wisc.drivesense.views.TripsListViewFragment;

/**
 * Created by Damouse on 12/19/2014.
 *
 * Trip management on the front end-- i.e. as it relates to the
 * objects that display the trips through UI. Does not implement
 * ant of the UI work itself.
 *
 * Bengal is assured to start after the background recording service has started
 */
public class Bengal {
    private static final String TAG = "MainActivity";

    enum State {
        SHOW_NOTHING,
        SHOW_ALL_TRIPS,
        SHOW_ONE_TRIP
    }

    private State state;

    private PinMapFragment map;
    private TripsListViewFragment list;

    private List<Trip> trips;
    private List<Trip> tripsInScope;

    private Trip displayingTrip = null;
    private TripRecorder recorder;


    public Bengal(PinMapFragment mapFragment, TripsListViewFragment tripsFragment) {
        map = mapFragment;
        list = tripsFragment;
        map.setDelegate(this);
        list.setDelegate(this);

        state = State.SHOW_NOTHING;
    }


    /* Public Interface */
    public void delete() {
        if (displayingTrip == null)
            throw new NullPointerException();

        SugarDatabse.deleteTrips(displayingTrip);

        trips.remove(displayingTrip);
        tripsInScope.remove(displayingTrip);

        state = State.SHOW_ALL_TRIPS;

        refresh();
    }

    public void selectTrip(Trip trip) {
        if (displayingTrip == null) {
            state = State.SHOW_ONE_TRIP;
            displayingTrip = trip;
        }
        else if (displayingTrip == trip) {
            state = State.SHOW_ALL_TRIPS;
            displayingTrip = null;
        }
        else  {
            displayingTrip = trip;
        }

        refresh();
    }

    public void selectPattern() {

    }

    public void upload(Trip trip) {
        //upload the trip

        //change the state of the trip in list
        refresh();
    }

    public void changeScope() {
        refresh();
    }

    public void load(User user) {
        trips = Trip.find(Trip.class, "user = ?", "" + user.getId());
        tripsInScope = applyScope();

        map.setTrips(trips);
        list.setTrips(trips);

        refresh();

        Log.d(TAG, "Loaded " + trips.size() + " trips for user " + user.email);
    }

    public void showAll() {
        state = State.SHOW_ALL_TRIPS;
        refresh();
    }

    public void clear() {
        state = State.SHOW_NOTHING;
        refresh();
    }

    public void setRecordingTrip(TripRecorder activeRecorder) {
        if (activeRecorder == null) {
            endTrip();
        }
        else {
            if (recorder != null)
                endTrip();

            recorder = activeRecorder;

            //TODO: register for recorder callbacks!!
        }

        refresh();
    }


    /* Trip Recorder Callbacks */
    public void newPatterns(List<MappableEvent> patterns) {
        refresh();
    }

    public void endTrip() {
        if (recorder == null)
            return;


        refresh();
    }


    /* Internal Implementation  */
    private List<Trip> applyScope() {
        return trips;
    }

    /**
     * TODO: cant call refresh on the map with every load, it readds the trips to the map
     */
    private void refresh() {
        if (state == State.SHOW_ALL_TRIPS) {
            map.showTrips(tripsInScope);
            list.showAllTrips();
        }

        if (state == State.SHOW_ONE_TRIP) {
            List<Trip> displayingTrips = new ArrayList<Trip>();
            displayingTrips.add(displayingTrip);
            map.showTrips(displayingTrips);

            list.showTrip(displayingTrip);

        }

        if (state == State.SHOW_NOTHING) {
            map.showTrips(null);
            list.showAllTrips();

            if (recorder != null) {
                map.showRecordingTrip(recorder.getTrip());
            }
        }
    }
}