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

    /**
     * If no users exist, create a demo user.
     * Else, check preferences for a saved user and load him
     */
    public static User getActiveUser() {
        List<User> users = User.find(User.class, "logged_in = ?", "true");


        if (users.size() == 1)
            return users.get(0);

        //no logged in users
        if (users.size() == 0) {
            return getDemoUser();
        }
        else {
            Log.e(TAG, "HCF-- MULTIPLE LOGGED IN USERS");
            for (User user: users)
                user.logOut();
            return getDemoUser();
        }
    }

    public static void deleteTrips(Trip... trips) {
        new DeleteAsyncTask().execute(trips);
    }

    public static void clearDatabase() {
        List<Trip> all = Trip.listAll(Trip.class);
        new DeleteAsyncTask().execute();
    }

    public static void scoreTrip(final Trip trip) {
        //Score the trip, try an upload
//        new ScoreTripAsyncTask() {
//            protected void onPostExecute(boolean scoredSuccessfully) {
//                if (!scoredSuccessfully)
//                    deleteTrips(trip);
//                else
//                    uploadTrip(trip);
//            }
//        }.execute(trip);
    }

    /**
     * Get or create and return the demo user
     * @return
     */
    private static User getDemoUser() {
//        User.
        List<User> demo = User.find(User.class, "user_id = ?", "-7");

        if (demo.size() == 1)
            return demo.get(0);

        //TODO: fix this. Merge all of their trips and delete all but one
        if (demo.size() > 1) {
            Log.e(TAG, "HCF-- multiple demo users!");
            throw new NullPointerException();
        }

        User newDemo = new User();
        newDemo.userId = -7;
        newDemo.save();
        return newDemo;
    }


    /* Old Code */
    public static void uploadTrip(Trip trip) {
//        DrivesensePreferences prefs = new DrivesensePreferences(BackgroundRecordingService.getInstance());

        //UPLOAD
        //DELETE

        //remove this code
        //check for wifi if preferences indicates WiFi-only is turned on
//        if (prefs.autoUploadWifi() && !WifiListener.isConnected(BackgroundRecordingService.getInstance()))
//            return;

        //trip.id is -1 if it hasn't been uploaded to the backend
//        if (prefs.autoUpload() && trip.trip_id == -1 && prefs.loggedIn()) {
//            Log.d(TAG, "Attempting to upload trip " + trip.name);



//            if (prefs.autoUploadDelete())
//                deleteTrip(trip);
//        }
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