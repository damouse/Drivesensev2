package edu.wisc.drivesense.businessLogic;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.SugarDatabse;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.server.ConnectionManager;
import edu.wisc.drivesense.server.ConnectionManagerCallback;
import edu.wisc.drivesense.views.PinMapFragment;
import edu.wisc.drivesense.views.TripsListViewFragment;

/**
 * Created by Damouse on 12/19/2014.
 * <p/>
 * Trip management on the front end-- i.e. as it relates to the
 * objects that display the trips through UI. Does not implement
 * ant of the UI work itself.
 * <p/>
 * Bengal is assured to start after the background recording service has started
 */
public class Bengal {
    private static final String TAG = "MainActivity";

    private State state;
    private PinMapFragment map;
    private TripsListViewFragment list;

    private List<Trip> trips;
    private List<Trip> tripsInScope;

    private Context context;
    private User activeUser = null;
    private Trip displayingTrip = null;
    private TripRecorder recorder;


    public Bengal(PinMapFragment mapFragment, TripsListViewFragment tripsFragment, Context context) {
        map = mapFragment;
        list = tripsFragment;
        map.setDelegate(this);
        list.setDelegate(this);

        state = State.SHOW_NOTHING;
        this.context = context;
    }

    /* Public Interface */
    public void delete() {
        if (displayingTrip == null)
            throw new NullPointerException();

        SugarDatabse.deleteTrips(displayingTrip);

        trips.remove(displayingTrip);
        tripsInScope.remove(displayingTrip);

        state = State.SHOW_ALL_TRIPS;
        displayingTrip = null;

        refresh();
    }

    public void upload(Trip trip) {
        ConnectionManager api = new ConnectionManager(context);
        api.convertUploadTrip(trip, activeUser, new ConnectionManagerCallback() {
            @Override
            public void onConnectionCompleted(Object... result) {
                //refresh trip list
                refresh();
            }

            @Override
            public void onConnectionFailed(String message) {
                //show alert with message contents
            }
        });
    }

    public void load(User user) {
        trips = Trip.find(Trip.class, "user = ?", "" + user.getId());
        tripsInScope = applyScope();

        refresh();

        Log.d(TAG, "Loaded " + trips.size() + " trips for user " + user.email);
    }

    public void showAll() {
        displayingTrip = null;
        state = State.SHOW_ALL_TRIPS;
        refresh();
    }

    public void clear() {
        displayingTrip = null;
        state = State.SHOW_NOTHING;
        refresh();
    }

    public void setRecordingTrip(TripRecorder activeRecorder) {
        if (activeRecorder == null) {
            endTrip();
        } else {
            if (recorder != null)
                endTrip();

            recorder = activeRecorder;

            //TODO: register for recorder callbacks!!
        }

        refresh();
    }

    /**
     * Pass the trip to be selected
     *
     * @param trip
     */
    public void selectTrip(Trip trip) {
        state = State.SHOW_ONE_TRIP;
        displayingTrip = trip;

        refresh();
    }

    public void selectPattern() {

    }

    public void changeScope() {
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
        if (state == State.SHOW_NOTHING) {
            map.showNothing();
            list.showTrips(tripsInScope);

            if (recorder != null) {
                map.showRecordingTrip(recorder.getTrip());
            }
        }

        else if (tripsInScope == null) {
            Log.e(TAG, "Trying to Show trips before Begnal gets loaded!");
            return;
        }
        else if (state == State.SHOW_ALL_TRIPS) {
            map.showTrips(tripsInScope);
            list.showTrips(tripsInScope);
        }

        else if (state == State.SHOW_ONE_TRIP) {
            map.showTrip(displayingTrip);
            list.showTrip(displayingTrip);
        }
    }

    enum State {
        SHOW_NOTHING,
        SHOW_ALL_TRIPS,
        SHOW_ONE_TRIP
    }
}