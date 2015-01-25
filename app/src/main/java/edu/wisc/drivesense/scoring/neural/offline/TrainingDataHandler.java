package edu.wisc.drivesense.scoring.neural.offline;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.scoring.common.DataReceiver;
import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;
import edu.wisc.drivesense.scoring.neural.modelObjects.TrainingSet;

import static edu.wisc.drivesense.scoring.neural.offline.OfflineWrapper.log;
import static edu.wisc.drivesense.scoring.neural.processing.GeneralProcessor.roundValuesInArray;
import static edu.wisc.drivesense.scoring.neural.offline.FileUtils.*;
import static edu.wisc.drivesense.scoring.neural.utils.Arrays.averageSeries;
import static edu.wisc.drivesense.scoring.neural.utils.Timestamp.*;

/**
 * Load saved datasets and return them wrapped together
 */
public class TrainingDataHandler {

    /* Loading */

    /**
     * Loads the training data at the path. trims the data so timestamps place nicely.
     *
     * @return
     */
    public static TrainingSet loadData(String path) {
        TrainingSet data = loadDataAtPath(path);
        return cleanData(data);
    }

    private static TrainingSet cleanData(TrainingSet data) {
//        ArrayList<TimestampQueue<Reading>> allLists = data.getAllQueues();
//        long timestampRange[] = completeTimestampRangeInDataSet(allLists);
//
//        //remove values that lie outside the complete timestamp rane for each queue
//        for (TimestampQueue series : allLists)
//            series.trimInPlace(timestampRange[0], timestampRange[1]);

        return data;
    }


    /**
     * Feed the training set into a mocked DataReceiver to simulate actual operation. Result
     * is a list of inputs ready for consumption by the NN. Assumes the training set is already trimmed
     */
    public static TimestampQueue generateInputSeries(DataReceiver receiver, TrainingSet trainingSet) {
//        ArrayList<TimestampQueue> allLists = TimestampQueue.queueListCopy(trainingSet.getAllQueues());
//        ArrayList<TimestampQueue<Reading>> allLists = trainingSet.getAllQueues();
//        TimestampQueue<Reading> labels = new TimestampQueue<Reading>(trainingSet.labels);
        TimestampQueue<DataSetInput> periodizedData = new TimestampQueue<DataSetInput>();
//        TimestampQueue<Reading> periodizedReadings = new TimestampQueue<Reading>();
//
//        //the first and last timestamp in data where each series has values
//        long timestampRange[] = completeTimestampRangeInDataSet(allLists);
//
//        //the "10" as the last parameter used to read: receiver.period. I removed the period from receiver at some point,
//        //so this needs to be refactored to reflect that
//        for (long currentTime = timestampRange[0]; currentTime < timestampRange[1]; currentTime += 10) {
//            periodizedReadings = dequeueBeforeTimestamp(allLists, currentTime);
//
//            for (TimestampSortable reading : periodizedReadings) {
//                if (reading == null)
//                    continue;
//
//                receiver.newReading((Reading) reading);
//            }
//
//            TimestampQueue periodizedLabels = labels.dequeueBeforeTimestamp(currentTime);
//            double averagedLabels[] = roundValuesInArray(averageSeries(periodizedLabels));
//            DataSetInput period = receiver.getProcessedPeriod();
//
//            if (period != null) {
//                period.labels = averagedLabels;
//                periodizedData.push(period);
//            }
//        }
//
//        log("Finished generating input series");
        return periodizedData;
    }


    /* Saving */
    public static void saveReadings(List<Reading> data) {

    }

    public static void saveInputSeries(DataSetInput data) {

    }

    public static void saveInputSeriesValue(DataSetInput data, String[] fieldNames) {

    }

    public static void saveReadingsWithDataInputSet(DataSetInput series, List<Reading> readings, String[] fieldNames) {

    }
}
