package edu.wisc.drivesense.utilities;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import edu.wisc.drivesense.sensors.SensorMonitor;
import edu.wisc.drivesense.views.PinMapFragment;

/**
 * Creates fake sensor updates. Hook this up to a map or the SensorMonitor to fake location updates.
 *
 * debug locations: (43.073214, -89.400558) to (43.059508, -89.400687)
 * @author Damouse
 *
 */
public class SensorSimulator {
    private final static String TAG = "SensorSimulator";

    //the counter variables for figuring out time
    int counter = 0;
    private int maxCounter;
    private int iteration = 0;

    //the location coordinates
    static private double latMin = 43.073214;
    static private double latMax = 43.059508;
    static private double longMin = -89.400558;
    static private double longMax = -89.400687;

    private double lastLong = 0;
    private double lastLat = 0;

    boolean shouldUpdateLocation = false;
    Handler timerHandler = new Handler();

    boolean firstUpdate = true;

    //debug and control flags
    boolean allowMovement = true;

    //potentially registered classes for callbacks
    SensorMonitor sensorMonitor = null;
    PinMapFragment pinMap = null;

    /**
     *  The timer callback runnable. Calls the receiver with location updates when needed,
     *  calls self recursively
     *  */
    Runnable timerRunnable = new Runnable() {

        @Override
        //posts location updates every second for 2 minutes
        public void run() {
            if (firstUpdate) {
                firstUpdate = false;
                Log.d(TAG, "Starting to update location");
            }

            if(sensorMonitor != null)
                sensorMonitor.onLocationChanged(getLocation());

            if (pinMap != null)
                pinMap.onLocationChanged(getLocation());

            if(counter > 360) {
                shouldUpdateLocation = false;
                Log.d(TAG, "Stopped updating location");
            }

            if (allowMovement)
                counter++;

            if(shouldUpdateLocation)
                timerHandler.postDelayed(this, 1000);
        }
    };

    public SensorSimulator() {

    }

    /*
     * Starts updating the class that called this method with location updates for the
     * given amount of time.
     */
    public void startSendingLocations(Object receiver, int time) {
        shouldUpdateLocation = true;
        maxCounter = time;

        if (receiver instanceof PinMapFragment)
            pinMap = (PinMapFragment) receiver;
        if (receiver instanceof SensorMonitor)
            sensorMonitor = (SensorMonitor)receiver;

        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void stopSendingLocations() {
        shouldUpdateLocation = false;
        counter = 0;
        iteration++;
    }


    /* Private Methods */
	/*
	 * Method returns a location object with lat and long between the two debug points 
	 * proportional to the counter's progress
	 */
    private Location getLocation() {
        Location ret = new Location("t");
        if (allowMovement || lastLong == 0) {
            double progress = (double) counter / (double) maxCounter;
            double lat = (latMax - latMin) * progress + latMin;
            double lon = (longMax - longMin) * progress + longMin + iteration * .001;

            ret.setLatitude(lat);
            ret.setLongitude(lon);

            lastLat = lat;
            lastLong = lon;
        }
        else {
            ret.setLatitude(lastLat);
            ret.setLongitude(lastLong);
        }

        Log.d(TAG, "Pushing new location: " + ret.toString());

        return ret;
    }
}