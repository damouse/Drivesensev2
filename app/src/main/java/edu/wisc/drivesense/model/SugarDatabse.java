package edu.wisc.drivesense.model;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.sensors.WifiListener;
import edu.wisc.drivesense.server.ConnectionManager;
import edu.wisc.drivesense.server.DrivesensePreferences;

/**
 * This is a database operation wrapper.
 *
 * Notes on Sugar so far:
 *  Relations are one way- like rails, specify only the parent_id
 *  Use @ignore to not persist fields
 *  Use "saveInTx" for bulk inserts
 *
 * Quick little overview on Sugar: https://guides.codepath.com/android/Clean-Persistence-with-Sugar-ORM
 *
 * @author Damouse
 */
public class SugarDatabse {
    private static final String TAG = "Sugar";

    public static void deleteTrips(Trip... trips) {
        new DeleteAsyncTask().execute(trips);
    }

    public static void clearDatabase() {
        Log.i(TAG, "Cleared database contents");
        Trip.deleteAll(Trip.class);
        User.deleteAll(User.class);
        MappableEvent.deleteAll(MappableEvent.class);
    }
}

/* Async workers */
class DeleteAsyncTask extends AsyncTask<Trip, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(Trip... params) {

        for (int i = 0; i < params.length; i++) {
            final Trip trip = params[i];
            List<MappableEvent> events = trip.getEvents();

            MappableEvent.deleteAll(MappableEvent.class, "trip = ?", "" + trip.getId());
            trip.delete();
        }

        Log.i("Sugar", "Finished deleting " + params.length + " trips");
        return true;
    }
}
