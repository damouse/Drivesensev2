package edu.wisc.drivesense.scoring.common;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import edu.wisc.drivesense.scoring.neural.processing.AccelerometerProcessor;
import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;

import static edu.wisc.drivesense.scoring.neural.processing.AccelerometerProcessor.processAcceleration;
import static edu.wisc.drivesense.scoring.neural.processing.GpsProcessor.processGps;
import static edu.wisc.drivesense.scoring.neural.processing.GyroscopeProcessor.processGyroscope;
import static edu.wisc.drivesense.scoring.neural.utils.Arrays.averageSeries;
import static edu.wisc.drivesense.scoring.neural.utils.Timestamp.timestampRangeForSet;

/*
 * Package Documentation-- README
 *
 * Descriptions and documentation of the rest of this package.
 *
 * Data
 *  Direct manipulation of sensor data.
 *
 * ModelObjects
 *  All objects used as containers for data. The objects do not do processing on their own, they are used
 *  as transactional pieces by the rest of the package.
 *
 * NeuralNetwork
 *  Neural network implementation.
 *
 * Offline
 *  Code relating to running this package NOT on an Android device. Primarily used for training.
 *
 * Utils
 *  Utility code in static methods that can be useful acreoss multiple packages.
 */

/**
 *
 *
 * Created by Damouse on 12/9/2014.
 *
 * Receives sensor data of variable frequency and enqueues it in a series of queues. Some of
 * this data is preprocessed right away as needed.
 *
 * Ever samplingPeriod miliseconds, it takes all data in queues and performs one sweeping preprocessing
 * action. The resulting DataSetInput is ready for consumption by the neural network. Once completed,
 * the assigned DataReceiverCallback gets called with this data.
 *
 * This class is largely functional. It doesn't interact with the neural network and doesn't care
 * about what the app is doing.
 *
 * This is the main entry point into the the rest of the files in this package.
 *
 * TODO: missing sensors or sensor values
 */
public class DataReceiver {
    //queues for storing incomign data streams
    public TimestampQueue<Reading> acceleration = new TimestampQueue<>();
    public TimestampQueue<Reading> magnet = new TimestampQueue<>();
    public TimestampQueue<Reading> gravity = new TimestampQueue<>();
    public TimestampQueue<Reading> gyroscope = new TimestampQueue<>();
    public TimestampQueue<Reading> gps = new TimestampQueue<>();
    public TimestampQueue<Reading> rotationMatrix = new TimestampQueue<>();

    //The last (inputMemorySize) input results are remembered if needed. Measured in number of periods,
    // not milliseconds
    private int inputMemorySize;
    private TimestampQueue inputMemory;

    //processors-- These hold some relevant pieces of data as needed
    private AccelerometerProcessor accelerometerProcessor = new AccelerometerProcessor();


    /* Boilerplate */
    /**
     * @param memory The number of past periods to remember
     */
    public DataReceiver(int memory) {
        inputMemorySize = memory;
        inputMemory = new TimestampQueue();
    }


    /* Sensor inputs- called from whomever is listening to sensors */
    /**
     * Take a new reading, process it if needed, and enqueue it
     */
    public void newReading(Reading reading) {

        switch (reading.type) {
            case ACCELERATION:
//                accelerometerProcessor.lastAcceleration = reading;
//                Reading linearAccel = accelerometerProcessor.getLinearAcceleration();
//
//                if (linearAccel != null)
//                    acceleration.push(linearAccel);

                //save the raw values
                acceleration.push(reading);

                //calculate a rotation matrix for the new reading
                if (magnet.size() != 0) {
                    rotationMatrix.push(accelerometerProcessor.getRotationMatrix(reading, magnet.peekLast()));
                }
                break;

            case GYROSCOPE:
                gyroscope.push(reading);
                break;

            case GRAVITY:
                gravity.push(reading);
                break;

            case MAGNETIC:
                magnet.push(reading);
                break;
            
            case GPS:
                gps.push(reading);
                break;

            default:
                break;
        }
    }


    /* Data periodization */
    /**
     * Hit the queues, remove all data that fits this period, processes it.
     *
     * @return A DataSetInput, ready for consumption by the NeuralNetwork
     */
    public DataSetInput getProcessedPeriod() {
        DataSetInput inputSet = buildRawPeriod();

        Log.d("Receiver period: ", inputSet.toString());

        //this could many any of a hundred things...
        if(!validatePeriod(inputSet))
            return null;

//        //old processessing
//        inputSet.preProcessedAcceleration = averageSeries(inputSet.acceleration);
//        inputSet.preProcessedGyroscope = averageSeries(inputSet.gyroscope);
//        inputSet.preProcessedGPS = averageSeries(inputSet.gps);
//        inputSet.preProcessedMagnet = averageSeries(inputSet.magnet);
//        inputSet.preProcessedGravity = averageSeries(inputSet.gravity);
//
//        //up to date processesing
//        processGps(inputSet, inputMemory);
//        processAcceleration(inputSet);
//        processGyroscope(inputSet);

        rememberLastInput(inputSet);

        //clear the queues on a successful period parse
        clearQueuesBeforeTime(inputSet.timestamp);

        return inputSet;
    }

    /**
     * Create a periodized chunk of data and return it.
     *
     * Does not actually dequeue the values in case of invalid data, simply returns them.
     *
     * TODO: this strategy of removing elements may lead to weird behavior due to value buildups
     *
     * @return DataSetInput before processing-- only contains raw values, not ready for Neural Network
     */
    private DataSetInput buildRawPeriod() {
        DataSetInput inputSet = new DataSetInput();
        ArrayList<TimestampQueue> allQueues = getAllQueues();
        long seriesTimestampRange[] = timestampRangeForSet(allQueues);

        long periodStopTime = seriesTimestampRange[1];
        inputSet.timestamp = periodStopTime;

        //set all data set values
        inputSet.acceleration = acceleration.getBeforeTimestamp(periodStopTime);
        inputSet.gyroscope = gyroscope.getBeforeTimestamp(periodStopTime);
        inputSet.gravity = gravity.getBeforeTimestamp(periodStopTime);
        inputSet.magnet = magnet.getBeforeTimestamp(periodStopTime);
        inputSet.gps = gps.getBeforeTimestamp(periodStopTime);
        inputSet.rotationMatricies = rotationMatrix.getBeforeTimestamp(periodStopTime);

        return inputSet;
    }

    /**
     * Ensure this input set is valid. If not, try to correct it as best as possible. If the input set is beyond saving,
     * return false.
     *
     * @param set
     * @return
     */
    private boolean validatePeriod(DataSetInput set) {
        if (set.completeDataSet() == false) {

            //If no valid inputs, then we can't do anything
            if (inputMemory.size() == 0)
                return false;

            DataSetInput lastInput = (DataSetInput) inputMemory.peekLast();
            lastInput.buildMissingFields(set);
        }

        return true;
    }

    /**
     * Clear all the values in the queue before the given time. Occurs once a valid period is set
     */
    private void clearQueuesBeforeTime(long time) {
        acceleration.dequeueBeforeTimestamp(time);
        gyroscope.dequeueBeforeTimestamp(time);
        gravity.dequeueBeforeTimestamp(time);
        magnet.dequeueBeforeTimestamp(time);
        gps.dequeueBeforeTimestamp(time);
    }

    /* Saving Previous Results-- Input Memory */
    /**
     * Remember the last input in case we need to recall the data
     */
    private void rememberLastInput(DataSetInput input) {
        inputMemory.push(input);

        if (inputMemory.size() > inputMemorySize)
            inputMemory.pop();
    }


    /* Utility Methods*/
    /**
     * Returns all of the queues this objects owns as a flat list
     */
    public ArrayList<TimestampQueue> getAllQueues() {
        return new ArrayList<TimestampQueue>(Arrays.asList(acceleration, magnet, gravity, gyroscope, gps));
    }
}


