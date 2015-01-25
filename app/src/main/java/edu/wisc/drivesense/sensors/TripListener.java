package edu.wisc.drivesense.sensors;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import java.util.Date;

import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;

/**
 * Created by Damouse on 6/27/14.
 */
public class TripListener {
    private static final String TAG = "Listener";
    public static final long STOP_WAIT_SECONDS = 120; //how long should we wait while stopped before assuming the trip is finished?
    public static final float START_DISTANCE = 20.0f; //meters until a trip is considered to have started

    private Location lastSignificantDisplacement;
    private Location lastLocation;

    private Date lastUpdatedTime;

    private boolean start;
    private boolean stop;

    private TripListener listener;
    Handler silenceTimerHandler = new Handler();

    public TripListener() {
        start = false;
        stop = false;
    }

    public void updateLocation(Location location) {
        //Log.v(TAG, "Examining new location: " + location.toString());
        lastLocation = location;

        if (lastSignificantDisplacement == null) {
            setLastLocation(location);
            return;
        }

        //make usre we're still receiving updates by cancelling and restarting the silence timer
        if (start) {
            silenceTimerHandler.removeCallbacks(silenceTimerRunnable);
            silenceTimerHandler.postDelayed(silenceTimerRunnable, STOP_WAIT_SECONDS * 1000);
        }

        //moving more than 20 meters? Likely moving in a car.
        if (lastSignificantDisplacement.distanceTo(lastLocation) > START_DISTANCE) {
            setLastLocation(location);

            if (!start) {
                BackgroundRecordingService.getInstance().stateManager.adviseTripStart();
                start = true;
                stop = false;
            }
        }
        else {
            Date now = new Date();
            long diffSeconds = (now.getTime() - lastUpdatedTime.getTime()) / 1000;
            if (diffSeconds > STOP_WAIT_SECONDS) {

                if (!stop) {
                    BackgroundRecordingService.getInstance().stateManager.adviseTripEnd();
                    start = false;
                    stop = true;
                    silenceTimerHandler.removeCallbacks(silenceTimerRunnable);
                }
            }

        }
    }

    public void stopListening() {
        start = false;
        stop = true;
        silenceTimerHandler.removeCallbacks(silenceTimerRunnable);
    }

    private void setLastLocation(Location location) {
        lastSignificantDisplacement = location;
        lastUpdatedTime = new Date();
    }


    /* Orphaned Code */
    /**
     * If gps points stop coming in for a given amount of time, assume the trip has ended.
     *
     * onLocationChanged sets the silenceBroken flag, this method unsets it. If this method
     * should run and see the flag unset, assume the location has not been updated since the last
     * time it ran, and advise a trip end.
     */
    Runnable silenceTimerRunnable = new Runnable() {

        @Override
        //posts location updates every second for 2 minutes
        public void run() {
        Log.d(TAG, "No gps updates received, advising trip end");
        BackgroundRecordingService.getInstance().stateManager.adviseTripEnd();
        }
    };
}
