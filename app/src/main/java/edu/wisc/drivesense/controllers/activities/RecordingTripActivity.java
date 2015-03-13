package edu.wisc.drivesense.controllers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.controllers.fragments.PinMapFragment;

/**
 * Shows recording trips on the PinMapFregment. Adds patterns as they are recognized.
 *
 * Should show a loading indicator as trips are loaded or as this activity waits for the
 * background service to load/settle.
 */
public class RecordingTripActivity extends Activity {
    private static final String TAG = "RecordingActivity";

    private PinMapFragment fragmentMap;


    /* Boilerplate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_trip);

        fragmentMap = (PinMapFragment) getFragmentManager().findFragmentById(R.id.map);
        synchronizeBackground();
    }

    @Override
    protected void onStop() {
        super.onStop();
        BackgroundRecordingService.checkAndDestroy(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    /**
     * Synchronize our state with the background state-- register for state callbacks,
     * load the currently recording trip, and map the results.
     */
    private void synchronizeBackground() {

    }


    /* Background State Changes */
}
