package edu.wisc.drivesense.scoring.neural.offline;

import java.util.ArrayList;

import edu.wisc.drivesense.scoring.DrivingAnalyst;
import edu.wisc.drivesense.scoring.common.DataReceiver;

import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TrainingSet;
import edu.wisc.drivesense.scoring.neural.neuralNetwork.NeuralNetworkTrainer;

import static edu.wisc.drivesense.scoring.neural.offline.TrainingDataHandler.generateInputSeries;
import static edu.wisc.drivesense.scoring.neural.offline.TrainingDataHandler.loadData;
import static edu.wisc.drivesense.scoring.neural.utils.Output.dataInputsToString;
import static edu.wisc.drivesense.scoring.neural.utils.Output.queueForKey;
import static edu.wisc.drivesense.scoring.neural.utils.Output.readingsToString;

import static edu.wisc.drivesense.scoring.neural.offline.FileUtils.*;

/**
 * Created by Damouse on 12/9/2014.
 * <p/>
 * Wraps the Network in an Android-independant wrapper, allowing training and testing from desktop.
 * <p/>
 * Also contains a set of tests for playing with the data
 * <p/>
 * OLD DATA RATES: 30MB/hr
 */
public class OfflineWrapper {
    //log using SOP? If false, log using Android log?
    public static boolean localLog = false;
    static int period = 500; //WARN WARN WARN- this is milliseconds for training data-- sensorEvents are nanoseconds!
    static int memory = 10;
    static int epochs = 10;
    static String baseSourcePath = "D:\\Development\\Projects\\DSTrace\\raw\\";
    static String baseResultPath = "D:\\Development\\Projects\\DSTrace\\output\\basic\\";
    static String allDataFolders[] = {"16-25_12-09\\", "15-27_12-12\\", "15-33_12-12\\", "16-06_12-12\\"};

    /**
     * Manual entry into Scorekeeper. Can be used to train the neuralnetwork or to test data.
     *
     * @param arg
     */
    public static void main(String[] arg) {
        localLog = true;

        trainNetwork();
        //testData();

        DrivingAnalyst.log("Done");
    }


    /* Top level control methods */
    static void trainNetwork() {
        ArrayList<TrainingSet> rawInputSets = new ArrayList<TrainingSet>();
        ArrayList<TimestampQueue> trainingSets = new ArrayList<TimestampQueue>();
        DataReceiver receiver = new DataReceiver(period, memory);
        NeuralNetworkTrainer trainer = new NeuralNetworkTrainer(5, 5);

        for (String s : allDataFolders) {
            TrainingSet trainingSet = loadData(baseSourcePath + s);
            rawInputSets.add(trainingSet);
            TimestampQueue inputSeries = generateInputSeries(receiver, trainingSet.copy());
            trainingSets.add(inputSeries);
            trainer.addTrainingData(inputSeries);
        }

        trainer.trainNetwork(epochs);
        trainer.testNetwork();
        trainer.stopNetwork();
    }

    /**
     * Testing preprocessing and processing.
     * <p/>
     * This method does whatever its written to do. Most likely this is performing different kinds of processing and
     * saving the results to a nicely graphable CSV file.
     */
    static void testData() {
        String targetFile = "16-25_12-09\\";
        String processedTargetKeypath = "preProcessedAcceleration";

        long windowStart = 58000;
        long windowEnd = 66000;

        if (false) {
            String outputFileNameOne = baseResultPath + "accel_raw.txt";
            TimestampQueue queueOne = filteredKeyedInput(targetFile, "acceleration", windowStart, windowEnd);
            String resultOne = readingsToString(queueOne);
            save(outputFileNameOne, resultOne);
        }

        String outputFileNameTwo = baseResultPath + "linear_accel.txt";
        TimestampQueue queueTwo = filteredKeyedTrainingSet(targetFile, 100, windowStart, windowEnd);
        String resultTwo = dataInputsToString(queueTwo, processedTargetKeypath);
        save(outputFileNameTwo, resultTwo);
    }


    /* Management */

    /**
     * Load the input at the given path, then extract one of the queues given by the string key,
     * and return a windowed version of it
     */
    static TimestampQueue filteredKeyedInput(String path, String key, long start, long end) {
        TrainingSet trainingSet = loadData(baseSourcePath + path);
        TimestampQueue targetQueue = queueForKey(trainingSet, key);
        targetQueue.trimInPlace(start, end);
        return targetQueue;
    }

    /**
     * Returns a queue of DataSetInputs trimmed for the times provided
     */
    static TimestampQueue filteredKeyedTrainingSet(String path, int period, long start, long end) {
        TrainingSet trainingSet = loadData(baseSourcePath + path);
        DataReceiver receiver = new DataReceiver(period, memory);

        TimestampQueue inputSeries = generateInputSeries(receiver, trainingSet.copy());
        inputSeries.trimInPlace(start, end);
        return inputSeries;
    }

}