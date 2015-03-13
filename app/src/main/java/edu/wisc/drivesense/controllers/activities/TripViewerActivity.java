package edu.wisc.drivesense.controllers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.controllers.fragments.PinMapFragment;

/**
 * Shows one or more trips on the map that have been completed and scored.
 *
 * For now, only shows one trip-- more added later.
 *
 * Should show a spinner while map and trip are being loaded.
 */
public class TripViewerActivity extends Activity {
    private static final String TAG = "RecordingActivity";

    private PinMapFragment fragmentMap;
    private Trip trip;


    /* Boilerplate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_viewer);

        //TODO: trip loading and map initialization should be handled asyncrhonously
        //also load the coordinates here before passing them off to the map
        trip = loadTrip();
        fragmentMap = (PinMapFragment) getFragmentManager().findFragmentById(R.id.map);
        fragmentMap.showTrip(trip);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BackgroundRecordingService.checkAndDestroy(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Trip loadTrip() {
        Bundle bundle = getIntent().getExtras();
        long id = (long) bundle.get("tripId");
        List<Trip> result = Trip.find(Trip.class, "id = ?", "" + id);

        if (result.size() != 1) {
            Log.d(TAG, "WARN- trip was not found or multiple trips exist with the given id!");
            return null;
        }

        return result.get(0);
    }
}
