package edu.wisc.drivesense.controllers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.controllers.fragments.PinMapFragment;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Trip;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Shows recording trips on the PinMapFregment. Adds patterns as they are recognized.
 *
 * Should show a loading indicator as trips are loaded or as this activity waits for the
 * background service to load/settle.
 */
public class RecordingTripActivity extends Activity implements Observer {
    private static final String TAG = "RecordingActivity";
    private Trip trip;
    private PinMapFragment fragmentMap;


    /* Boilerplate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_trip);
        fragmentMap = (PinMapFragment) getFragmentManager().findFragmentById(R.id.map);

    }

    @Override
    protected void onResume() {
        super.onResume();
        trip = BackgroundRecordingService.getInstance().recorder.getTrip();
        fragmentMap.showRecordingTrip(trip);
        BackgroundRecordingService.getInstance().recorder.addObserver(this);
    }

    @Override
    protected void onPause() {
        super.onStop();
        Log.d(TAG, "RecordingTripActivity.java onPause");
        BackgroundRecordingService.getInstance().recorder.deleteObserver(this);
        BackgroundRecordingService.checkAndDestroy(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recording_trip, menu);
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

    @Override
    public void update(Observable observable, Object data) {
        List<MappableEvent> events = (List<MappableEvent>) data;
        fragmentMap.updateRecordingTrip(events);
    }
}
