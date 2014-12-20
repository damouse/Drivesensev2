package edu.wisc.drivesense.scoring.neural.utils;

import java.lang.reflect.Field;

import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;

import static edu.wisc.drivesense.scoring.DrivingAnalyst.log;

/**
 * Created by Damouse on 12/14/2014.
 * <p/>
 * Utility methods for converting objects or results to some sort of output format
 */
public class Output {
    /* Conversion methods */
    public static String readingsToString(TimestampQueue<Reading> readings) {
        StringBuilder sb = new StringBuilder();

        for (Reading reading : readings)
            sb.append(readingToString(reading) + "\n");


        return sb.toString();
    }

    public static String dataInputsToString(TimestampQueue<DataSetInput> series, String... keys) {
        TabbingStringBuilding sb = new TabbingStringBuilding();

        for (DataSetInput set : series) {
            sb.append(set.timestamp);

            for (int i = 0; i < keys.length; i++) {
                double values[] = doublesForKey(set, keys[i]);

                for (int j = 0; j < values.length; j++)
                    sb.append(values[j]);
            }

            sb.newLine();
        }

        return sb.toString();
    }


    /* Individual model object conversion */
    public static String readingToString(Reading reading) {
        TabbingStringBuilding sb = new TabbingStringBuilding();
        sb.append(reading.timestamp);

        for (int i = 0; i < reading.values.length; i++)
            sb.append(reading.values[i]);

        return sb.toString();
    }

    public static String inputToString(DataSetInput input, String key) {
        TabbingStringBuilding sb = new TabbingStringBuilding();
        sb.append(input.timestamp);

        double values[] = doublesForKey(input, key);

        for (int i = 0; i < values.length; i++)
            sb.append(values[i]);

        return sb.toString();
    }


    /* Reflection */
    public static double[] doublesForKey(Object target, String key) {
        try {
            Field field = target.getClass().getDeclaredField(key);
            double result[] = (double[]) field.get(target);
            return result;
        } catch (Exception ex) {
            log("Error reflecting field.");
            ex.printStackTrace();

            return null;
        }
    }

    public static TimestampQueue queueForKey(Object target, String key) {
        try {
            Field field = target.getClass().getDeclaredField(key);
            TimestampQueue result = (TimestampQueue) field.get(target);
            return result;
        } catch (Exception ex) {
            log("Error reflecting field.");
            ex.printStackTrace();

            return null;
        }
    }
}

/* Nothing special, just a wrapper around SB to provide custom delination
 * for CSV vs tabbed input */
class TabbingStringBuilding {
    StringBuilder stringBuilder;

    public TabbingStringBuilding() {
        stringBuilder = new StringBuilder();
    }

    public void append(Object o) {
        stringBuilder.append("" + o + "\t");
    }

    public void newLine() {
        stringBuilder.append("\n");
    }

    public String toString() {
        return stringBuilder.toString();
    }
}
