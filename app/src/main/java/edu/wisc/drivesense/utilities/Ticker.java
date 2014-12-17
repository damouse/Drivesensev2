package edu.wisc.drivesense.utilities;

import android.os.Handler;
import android.util.Log;

import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;

public class Ticker {
    int counter = 0;
    boolean tick;
    public BackgroundRecordingService parent;
    Handler timerHandler = new Handler();

    public Ticker(BackgroundRecordingService parent, boolean shouldTick) {
        this.parent = parent;
        tick = shouldTick;
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void kill() {
        parent = null;
        tick = false;
    }


    Runnable timerRunnable = new Runnable() {

        @Override
        //posts location updates every second for 2 minutes
        public void run() {
            if (parent == null)
                return;

            counter++;
            Log.d("Ticker", "State: " + parent.stateManager.getState());

            if (tick)
                timerHandler.postDelayed(this, 3000);
        }
    };
}

