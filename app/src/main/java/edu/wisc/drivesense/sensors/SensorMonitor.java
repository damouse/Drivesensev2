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

import java.util.ArrayList;
import java.util.List;

/**
 * Records sensor data, creates Reading objects, and sends them back to the ModelManager
 *
 * @author Damouse
 */
public class SensorMonitor implements LocationListener, SensorEventListener, GpsStatus.Listener {
    private static final String TAG = "SensorMonitor";
    private final boolean EXTRACT_ROTATION = true;

    //A debugging module that feeds fake sensor data to this class. Used for testing and demo purposes
    public SensorSimulator simulator;

    //Sensors
    private SensorManager sensorManager;
    private List<Sensor> sensors;
    private LocationManager locationManager;

    //In milliseconds, how long to wait before accepting a new value.
    private double minSamplingPeriod = 180;

    private double lastAccelTime = 0;
    private double lastGyroTime = 0;
    private double lastMagnetTime = 0;
    private double lastGravityTime = 0;

    int a = 0;
    int gy = 0;
    int m = 0;
    int gr = 0;


    /* Boilerplate*/
    public SensorMonitor(BackgroundRecordingService manager) {
        Log.d(TAG, "Sensor Monitor Init");
        sensorManager = (SensorManager) manager.getSystemService(Context.SENSOR_SERVICE);

        sensors = new ArrayList<>();
        sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));

        if (BackgroundRecordingService.DEBUG) simulator = new SensorSimulator();
        else {
            locationManager = (LocationManager) manager.getSystemService(Context.LOCATION_SERVICE);
            locationManager.addGpsStatusListener(this);
        }
    }


    /* Public Methods*/
    // Begin collecting data from sensors.
    public void startCollecting() {
        Log.d(TAG, "Starting collection");

        //start sensors
        startCollectingGPS();

        for (Sensor sensor: sensors) {
            if (sensor != null)
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void startCollectingGPS() {
        if (BackgroundRecordingService.DEBUG) simulator.startSendingLocations(this);
        else locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    // Stop collecting data
    public void stopCollecting() {
        stopCollectingGPS();
        sensorManager.unregisterListener(this);

    }

    public void stopCollectingGPS() {
        if (BackgroundRecordingService.DEBUG) simulator.stopSendingLocations(this);
        else {
            locationManager.removeUpdates(this);
//            locationManager.removeGpsStatusListener(this);
        }
    }


/* Sensor Callbacks */

    /**
     * Reports gps position
     * <p/>
     * If listening, stores the last gps coordinate where the displacement was at least 20 meteres.
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
        Reading.Type type;

//        Log.d(TAG, "Reported Timestamp: " + event.timestamp + " Real Time: " + System.currentTimeMillis());
//        return;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
//                Log.d(TAG, "Accel " + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + " time: " + event.timestamp);
                type = Reading.Type.ACCELERATION;

                double diff = milisecondDifference(event, lastAccelTime);
                if (milisecondDifference(event, lastAccelTime) < minSamplingPeriod)
                    return;

                lastAccelTime = event.timestamp;
                a++;
                break;

            case Sensor.TYPE_GYROSCOPE:
                type = Reading.Type.GYROSCOPE;
                gy++;
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                type = Reading.Type.MAGNETIC;
                m++;
                break;

            case Sensor.TYPE_GRAVITY:
                type = Reading.Type.GRAVITY;
                gr++;
                break;

            default:
                Log.e(TAG, "Received sensor reading of unknown type.");
                return;
        }

        double[] values = new double[event.values.length];
        for (int i = 0; i < event.values.length; i++)
            values[i] = (double) event.values[i];

        BackgroundRecordingService.getInstance().newReading(new Reading(values, System.currentTimeMillis(), type));
    }

    public double milisecondDifference(SensorEvent event, double nanosecondsEarlier) {
        return (event.timestamp - nanosecondsEarlier) / 1000000;
    }

    /* Stock Callbacks */
    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "Provider disabled:  " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "Provider enabled:  " + provider);
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


    //TESTING
    public void readCounts() {
        Log.w(TAG, "Accel: " + a + " Gyro: " + gy + " Magnetic: " + m + " Gravity: " + gr);
    }

    public void clearCounts() {
        a = 0;
        m = 0;
        gr = 0;
        gy = 0;
    }
}
