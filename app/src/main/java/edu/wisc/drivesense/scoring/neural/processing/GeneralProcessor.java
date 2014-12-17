package edu.wisc.drivesense.scoring.neural.processing;

import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;

import static edu.wisc.drivesense.scoring.neural.utils.Arrays.averageSeries;

/**
 * Created by Damouse on 12/14/2014.
 */
public class GeneralProcessor {

    public static double vectorMagnitudeFromSeries(TimestampQueue set) {
        double[] result = averageSeries(set);
        result = absoluteValue(result);
        return vectorMagnitude(result);
    }

    /* Function Operations used by the rest of the processor */

    /**
     * Return an array that is the absolute value of each value in the target array
     */
    public static double[] absoluteValue(double[] target) {
        double result[] = new double[target.length];

        for (int i = 0; i < target.length; i++) {
            result[i] = Math.abs(target[i]);
        }

        return result;
    }


    /**
     * Given an array, round each value up or down to the nearest whole number
     */
    public static double[] roundValuesInArray(double[] values) {
        for (int i = 0; i < values.length; i++)
            values[i] = Math.round(values[i]);

        return values;
    }

    public static double vectorMagnitude(double[] target) {
        double result = 0;

        for (int i = 0; i < target.length; i++)
            result += Math.pow(target[i], 2);

        return Math.sqrt(result);
    }


    /**
     * Normalize values in a series that have the range [seriesMin, seriesMax] into a new series that
     * has the range [normalizedMin, normalizedMax].
     * <p/>
     * Values in the series that fall outside the theoretical passed series range will be returned as either normalizedMin or Max.
     * In other words, returned array is guaranteed to not extend the normalized bounds.
     * <p/>
     * For example, if a value = 3 but the seriesMax = 2, value will be reduced to normalizedMax.
     */
    public static double[] normalizeArray(double[] series, double normalizedMin, double normalizedMax, double seriesMin, double seriesMax) {
        double normalized[] = new double[series.length];

        for (int i = 0; i < series.length; i++)
            normalized[i] = normalize(series[i], normalizedMin, normalizedMax, seriesMin, seriesMax);

        return normalized;
    }

    public static double normalize(double value, double normalizedMin, double normalizedMax, double valueMin, double valueMax) {
        if (value < valueMin)
            return normalizedMin;

        if (value > valueMax)
            return normalizedMax;

        return ((normalizedMax - normalizedMin) * (value - valueMin)) / (valueMax - valueMin) + normalizedMin;
    }
}
