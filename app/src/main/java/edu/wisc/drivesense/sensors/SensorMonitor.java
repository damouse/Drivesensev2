package edu.wisc.drivesense.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.utilities.SensorSimulator;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;

/**
 * Records sensor data, creates Reading objects, and sends them back to the ModelManager
 *
 * @author Damouse
 */
public class SensorMonitor implements LocationListener, SensorEventListener, GpsStatus.Listener {
    private static final String TAG = "SensorMonitor";

    //A debugging module that feeds fake sensor data to this class. Used for testing and demo purposes
    public SensorSimulator simulator;

    //Sensors
    private SensorManager sensorManager;
    private Sensor acceleromenter;
    private Sensor gyroscope;
    private Sensor compass;
    private LocationManager locationManager;

    private long lastAccelReadingTime = 0;


    /* Boilerplate*/
    public SensorMonitor(BackgroundRecordingService manager) {

        //init sensors
        sensorManager = (SensorManager) manager.getSystemService(Context.SENSOR_SERVICE);
        acceleromenter = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (BackgroundRecordingService.DEBUG) simulator = new SensorSimulator();
        else {
            locationManager = (LocationManager) manager.getSystemService(Context.LOCATION_SERVICE);
            locationManager.addGpsStatusListener(this);
        }
    }


    /* Public Methods*/
    // Begin collecting data from sensors.
    public void startCollecting() {
        //start sensors
        startCollectingGPS();

        sensorManager.registerListener(this, acceleromenter, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void startCollectingGPS() {
        if (BackgroundRecordingService.DEBUG) simulator.startSendingLocations(this, 15);
        else locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    // Stop collecting data
    public void stopCollecting() {
        stopCollectingGPS();
        sensorManager.unregisterListener(this);
        lastAccelReadingTime = 0;
    }

    public void stopCollectingGPS() {
        if (BackgroundRecordingService.DEBUG) simulator.stopSendingLocations();
        else {
            locationManager.removeUpdates(this);
//            locationManager.removeGpsStatusListener(this);
        }
    }


/* Sensor Callbacks */

    /**
     * Reports GPS position
     * <p/>
     * If listening, stores the last GPS coordinate where the displacement was at least 20 meteres.
     * <p/>
     * If this location is updated, inform the service we are likely moving
     * <p/>
     * If this location has not changed in 3 minutes, inform the service the trip has most likely ended
     */
    @Override
    public void onLocationChanged(Location location) {
        BackgroundRecordingService.getInstance().newGpsReading(location);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        ReadingType type = null;
//
//        switch (event.sensor.getType()) {
//            case Sensor.TYPE_ACCELEROMETER:
//                type = ReadingType.ACCELERATION;
//                break;
//
//            case Sensor.TYPE_GYROSCOPE:
//                type = ReadingType.GYROSCOPE;
//                break;
//
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                type = ReadingType.MAGNETIC;
//                break;
//
//            default:
//                Log.d(TAG, "Received sensor reading of unknown type.");
//                break;
//        }
//
//        //sad and unfortunate casting here...
//        double[] values = new double[event.values.length];
//        for (int i = 0; i < event.values.length; i++)
//            values[i] = (double) event.values[i];
//
//        BackgroundRecordingService.getInstance().newReading(new Reading(values, event.timestamp, type));
    }


    /* Stock Callbacks */
    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "status changed for  " + provider);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.i(TAG, "accuracy changed for " + arg0);
    }

    public boolean gpsEnabled() {
        if (BackgroundRecordingService.DEBUG)
            return true;

        //LocationManager manager = (LocationManager) BackgroundRecordingService.getInstance().getSystemService(BackgroundRecordingService.getInstance().LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        BackgroundRecordingService.getInstance().stateManager.setGpsAvailable(event == GpsStatus.GPS_EVENT_STARTED);
    }
}
