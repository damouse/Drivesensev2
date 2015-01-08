package edu.wisc.drivesense.scoring.neural.processing;

import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;

import static edu.wisc.drivesense.scoring.neural.processing.GeneralProcessor.absoluteValue;
import static edu.wisc.drivesense.scoring.neural.processing.GeneralProcessor.vectorMagnitudeFromSeries;
import static edu.wisc.drivesense.scoring.neural.utils.Arrays.averageSeries;

/**
 * Created by Damouse on 12/12/2014.
 */
public class GyroscopeProcessor {


    /* Processing */
    public static void processGyroscope(DataSetInput data) {
        //Make positive. PLACEHOLDER FOR TESTING INTERMEDIATE VALUES
        double[] result = averageSeries(data.gyroscope);
        data.preProcessedGyroscope = absoluteValue(result);

        //set magnitude of data object
        data.gyroscopeMagnitude = vectorMagnitudeFromSeries(data.gyroscope);
    }

    /* Preprocessing */
}
