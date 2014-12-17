package edu.wisc.drivesense.scoring;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.common.ScoreKeeperDelegate;
import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.scoring.neural.offline.OfflineWrapper;
import edu.wisc.drivesense.scoring.projected.ProjectedScoreKeeper;
import edu.wisc.drivesense.scoring.common.DataReceiver;

/*
    Package Documentation

    ScoreKeeper is the main object of this pacakge. Neural contains NeuralNetwork implementation
    of driving behavior recognition, while projected is the (older) algorithmic approach.
 */

/**
 * Identifies driving behaviors and calls back with patterns when identified.
 * 
 * @author Damouse 
 */
public class DrivingAnalyst {
    private static final String TAG = "DrivingAnalyst";
    private static final boolean useNeuralNetork = false;

    //on average, how many GPS coordinates to omit.
    private static final double gpsLossRate = .1;

    //receives incoming data from a sensor providor
    DataReceiver receiver;

    //Represents the object that set up this object and receives notifications
    ScoreKeeperDelegate delegate;

    //How long to wait between scoring attempts and how much data to hold
    private int period;
    private int memorySize;
    private Context context;


    /* Constructors */
    public DrivingAnalyst(ScoreKeeperDelegate delegate,  Context context) {
        this.delegate = delegate;
        receiver = new DataReceiver(1000, 10);
        this.context = context;
    }


    /* Utility */
    public static void log(Object s) {
        if (OfflineWrapper.localLog)
            System.out.println("" + s);
        else
            Log.d("Scorekeeper", "" + s);
    }


    /* Period and Data Window Management */

    /**
     * Called when a new reading is received by whomever is managing the sensors
     *
     * @param reading
     */
    public void newReading(Reading reading) {
        if (receiver != null)
            receiver.newReading(reading);
    }

    /**
     * Get a window of data from the receiver and feed it into whichever scheme we are using
     * to score. Called from a timer function.
     *
     * This is also where the delegate method createPattern is called.
     */
    public void analyzePeriod() {
        if (receiver == null) {
            log("Receiver is unexpectedly null.");
            return;
        }

        DataSetInput period = receiver.getProcessedPeriod();
        ArrayList<MappableEvent> patterns;

        if (period == null) {
            log("Incomplete period.");
            return;
        }

        //which choice of scoring implementation to use depends largely on which one is more accurate
        if (useNeuralNetork)
            patterns = ProjectedScoreKeeper.getDrivingEvents(period, context); //obviously temporary
        else
            patterns = ProjectedScoreKeeper.getDrivingEvents(period, context);

        //Call back to the background service with the results
        delegate.newPatterns(patterns);
    }
}
