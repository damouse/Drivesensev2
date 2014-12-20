package edu.wisc.drivesense.scoring.neural.utils;

import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;

import static edu.wisc.drivesense.scoring.DrivingAnalyst.log;

/**
 * Created by Damouse on 12/13/2014.
 * <p/>
 * Utils dealing with arrays or lists
 */
public class Arrays {
    /**
     * Concatenates the given arrays of doubles
     */
    public static double[] concatDoubleArrays(double[]... arrays) {
        int totalLength = 0;
        int lengthOfBuildingArray = 0;

        for (int i = 0; i < arrays.length; i++) {
            totalLength += arrays[i].length;
        }

        double newArray[] = new double[totalLength];

        for (int i = 0; i < arrays.length; i++) {
            System.arraycopy(arrays[i], 0, newArray, lengthOfBuildingArray, arrays[i].length);
            lengthOfBuildingArray += arrays[i].length;
        }

        return newArray;
    }

    public static double[] addArrays(double[] one, double[] two) {
        if (one.length != two.length) {
            log("ERROR- arrays dont match in dimension!");
            return null;
        }

        double ret[] = new double[one.length];
        for (int i = 0; i < one.length; i++) {
            ret[i] = one[i] + two[i];
        }

        return ret;
    }


    /**
     * Given time-ordered data series, return a flat double array where each value
     * is an average over all the values in the series at that index.
     */
    public static double[] averageSeries(TimestampQueue<? extends TimestampSortable> series) {
        if (series.size() == 0) {
            log("Series has no values. Returning 0 values");
            double ret[] = {0.0, 0.0, 0.0};
            return ret;
        }

        //create array of appropriate dimension
        int readingDimension = 3;//series.peek().dimension;
        double averagedSeries[] = new double[readingDimension];

        //iterate once per dimension, summing each of the values and then averaging at the end
        for (int i = 0; i < readingDimension; i++) {
            for (TimestampSortable reading : series) {
                Reading cast = (Reading) reading;
                averagedSeries[i] += cast.values[i];
            }

            averagedSeries[i] = averagedSeries[i] / series.size();
        }

        return averagedSeries;
    }
}
