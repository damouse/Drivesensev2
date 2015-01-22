package edu.wisc.drivesense.scoring.neural.modelObjects;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;

import edu.wisc.drivesense.model.Reading;

/**
 * Created by Damouse on 12/9/2014.
 *
 */
public class DataSetInput extends TimestampSortable {
    public long timestamp;

    /* Raw sensor values extracted from DataReceiver Queues */
    public TimestampQueue<Reading> acceleration;
    public TimestampQueue<Reading> gyroscope;
    public TimestampQueue<Reading> gravity;
    public TimestampQueue<Reading> magnet;
    public TimestampQueue<Reading> gps;
    public TimestampQueue<Reading> rotationMatricies;


    /* Inputs after pre-processing
    * Preprocessing will affect all sensor values but will also create new, intermediate
    * values such as linearAcceleration.
    * */
    public double[] preProcessedAcceleration;
    public double[] preProcessedGyroscope;
    public double[] preProcessedGPS;
    public double[] preProcessedMagnet;
    public double[] preProcessedGravity;

    /* Inputs post-processed */
    public double bearingDelta;
    public double speedDelta;
    public double accelerationMagnitude;
    public double gyroscopeMagnitude;
    public double jerk;

    public double[] labels;


    /* Conversion to inputs/outputs */
    /**
     * Converts this object to its MLData representation
     */
    public MLData convertNetworkInput () {
        double[] rawData = {bearingDelta, speedDelta, accelerationMagnitude, gyroscopeMagnitude, jerk};
        return new BasicMLData(rawData);
    }

    /** Converts this object to a representation the NeuralNet understands with output values included
      * (only used while training)
      */
    public MLDataPair convertNetworkTrainingInput() {
        MLData inputs = convertNetworkInput();
        return new BasicMLDataPair(inputs, new BasicMLData(labels));
    }


    /* Data Set Validation */
    /**
     * Returns true if this is both valid and complete-- nothing important is missing and values are
     * ok.
     */
    public boolean completeDataSet() {
        if (!validDataSet())
            return false;

        return validAcceleration() && validGyroscope() && validGravity() && validMagnet() && validGps();
    }

    /** Returns true if this is a valid data set- the important pieces aren't missing  */
    public boolean validDataSet() {
        return true;
    }

    /**
     * Given another datasetinput with invalid state, build its missing pieces from this object
     */
    public void buildMissingFields(DataSetInput invalidInput) {
        if (!invalidInput.validAcceleration())
            invalidInput.acceleration = acceleration;

        if (!invalidInput.validGyroscope())
            invalidInput.gyroscope = gyroscope;

        if (!invalidInput.validGravity())
            invalidInput.gravity = gravity;

        if (!invalidInput.validMagnet())
            invalidInput.magnet = magnet;

        if (!invalidInput.validGps())
            invalidInput.gps = gps;
    }

    public boolean validAcceleration() { return !(acceleration == null || acceleration.size() == 0); }
    public boolean validGyroscope() { return !(gyroscope == null || gyroscope.size() == 0); }
    public boolean validGravity() { return !(gravity == null || gravity.size() == 0); }
    public boolean validMagnet() { return !(magnet == null || magnet.size() == 0); }
    public boolean validGps() { return !(gps == null || gps.size() == 0); }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(" acceleration: " + acceleration.size());
        sb.append(" gyroscope: " + gyroscope.size());
        sb.append(" gravity: " + gravity.size());
        sb.append(" magnet: " + magnet.size());
        sb.append(" gps: " + gps.size());

        return sb.toString();
    }

    /* Inherited Getter */
    public long getTime() {
        return timestamp;
    }
}
