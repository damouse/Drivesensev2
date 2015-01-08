package edu.wisc.drivesense.scoring.neural.processing;

import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.model.Reading;

import static edu.wisc.drivesense.scoring.neural.utils.Arrays.averageSeries;

/**
 * Created by Damouse on 12/12/2014.
 */
public class AccelerometerProcessor {
    //the last acceleration and gravity values from the sensors
    public Reading lastAcceleration;
    public Reading lastGravity;


    /* Processing */
    public static void processAcceleration(DataSetInput data) {
        //Make Accel positive. PLACEHOLDER FOR TESTING INTERMEDIATE VALUES
        double[] result = averageSeries(data.acceleration);
        data.preProcessedAcceleration = GeneralProcessor.absoluteValue(result);

        //set magnitude of data object
        data.accelerationMagnitude = GeneralProcessor.vectorMagnitudeFromSeries(data.acceleration);

        //Jerk-- which is acceleration derivative
        data.jerk = 0;
    }


    /* Preprocessing */

        /**
         * Subtract gravity from acceleration. Returns null if the lastReading values have not been set yet.
         * YOU ARE DOING THIS TWICE! buggity bugitty bug.
         * @return
         */
            public Reading getLinearAcceleration() {
            if (lastAcceleration == null || lastGravity == null)
            return null;

        double[] gravity = lastGravity.values;
        double[] acceleration = lastAcceleration.values;
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
        double alpha = 0.8;
        double linear_acceleration[] = new double[3];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * acceleration[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * acceleration[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * acceleration[2];

        linear_acceleration[0] = acceleration[0] - gravity[0];
        linear_acceleration[1] = acceleration[1] - gravity[1];
        linear_acceleration[2] = acceleration[2] - gravity[2];

        Reading linear = new Reading(linear_acceleration, lastAcceleration.timestamp, Reading.Type.LINEAR_ACCELERATION);

        return linear;
    }
}
