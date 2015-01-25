package edu.wisc.drivesense.scoring.projected.patterns;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;

public class LanechangeExtraction {


    /*a really small turn threshold*/
    private static double kLaneChangeThreshold = 0.2;
    private static long kMinimumDuration = 2000;
    private static int kSlidingWindowSize = 20;
    private static int kZIndex = 2;
    private static int kXIndex = 0;
    private static double kLaneChangeThresholdByAccelerometer = 2;

    /*
    private static double kLaneChangeThreshold = 0.17;
    private static long kMinimumDuration = 2000;
    private static int kSlidingWindowSize = 20;
    private static int kZIndex = 2;
    */

    private static boolean isLanechange(List<Reading> gyroscope, DrivingPattern pattern) {

        if (pattern.end - pattern.start < kMinimumDuration) return false;


        List<Reading> sub = (List<Reading>) PreProcess.extractSubList(gyroscope, pattern.start, pattern.end);
        int sz = sub.size();
        double rads = 0.0;

        for (int i = 0; i < sz - 1; ++i) {
            Reading Reading = sub.get(i);
            long time_diff = gyroscope.get(i + 1).timestamp - gyroscope.get(i).timestamp;
            double z = Reading.values[kZIndex];
            rads += z * (time_diff / 1000.0);
        }
        //Log.log(pattern.start - start*1000, pattern.end - start*1000, Math.toDegrees(rads));
        if (Math.abs(Math.toDegrees(rads)) < 10.0)
            return true;
        return false;
    }

    static public ArrayList<DrivingPattern> /*List<Reading>*/ extractLanechanges(List<Reading> gyroscope) {

        ///List<Reading> div = new ArrayList<Reading>();

        int wnd = kSlidingWindowSize;
        ArrayList<DrivingPattern> patterns = new ArrayList<DrivingPattern>();
        int sz = gyroscope.size();
        //Log.log(Thread.currentThread().getStackReading()[1].getMethodName(), "the size of input Readings is:" + String.valueOf(sz));
        LinkedList<Reading> sliding = new LinkedList<Reading>();
        boolean in_turn = false;
        //int d = gyroscope.get(sz - 1).dimension;
        DrivingPattern new_pattern = null;
        for (int i = 0; i < sz; ++i) {
            Reading Reading = gyroscope.get(i);
            sliding.add(Reading);
            int len = sliding.size();
            if (len == wnd) {
                double[] deviation = Formulas.absoluteDeviation(sliding);
                boolean turnning = false;

                if (deviation[kZIndex] > kLaneChangeThreshold)
                    turnning = true;
                if (turnning) {
                    /*static*/
                    if (false == in_turn) {
                        in_turn = true;
                        new_pattern = new DrivingPattern();
                        new_pattern.start = gyroscope.get(i - wnd + 1).timestamp;
                    }
                } else {
                    if (true == in_turn) {
                        in_turn = false;
                        new_pattern.end = gyroscope.get(i - 1).timestamp;
                        if (isLanechange(gyroscope, new_pattern)) {
                            new_pattern.start = new_pattern.start - 1000;
                            new_pattern.end = new_pattern.end + 1000;
                            new_pattern.type = MappableEvent.Type.lanechange;
                            patterns.add(new_pattern);
                        }
                        new_pattern = null;
                    }
                }
                sliding.removeFirst();
            }
        }
        if (null != new_pattern && new_pattern.end == -1) {
            new_pattern.end = gyroscope.get(sz - 1).timestamp;
            if (isLanechange(gyroscope, new_pattern)) {
                new_pattern.start = new_pattern.start - 1000;
                new_pattern.end = new_pattern.end + 1000;
                new_pattern.type = MappableEvent.Type.lanechange;
                patterns.add(new_pattern);
            }
            new_pattern = null;
        }
        return patterns;
    }

    private static boolean isLanechangeByAccelerometer(List<Reading> accelerometer, DrivingPattern pattern) {

        if (pattern.end - pattern.start < kMinimumDuration) return false;
        List<Reading> sub = (List<Reading>) PreProcess.extractSubList(accelerometer, pattern.start, pattern.end);
        int sz = sub.size();
        double sum = 0.0;

        for (int i = 0; i < sz - 1; ++i) {
            Reading Reading = sub.get(i);
            sum += Reading.values[0];
        }
        //Log.log(sum/sz);
        if (Math.abs(sum / sz) < 0.2)
            return true;
        return false;
    }

    /**
     * Extract lane changes by accelerometer
     *
     * @param accelerometer
     * @return
     */

    static public ArrayList<DrivingPattern> extractLanechangesByAccelerometer(List<Reading> accelerometer) {

        int wnd = kSlidingWindowSize;
        ArrayList<DrivingPattern> patterns = new ArrayList<DrivingPattern>();
        int sz = accelerometer.size();
        LinkedList<Reading> sliding = new LinkedList<Reading>();
        boolean in_turn = false;
        DrivingPattern new_pattern = null;
        for (int i = 0; i < sz; ++i) {
            Reading Reading = accelerometer.get(i);
            sliding.add(Reading);
            int len = sliding.size();
            if (len == wnd) {
                double[] deviation = Formulas.absoluteDeviation(sliding);
                boolean turnning = false;
                //Log.log(deviation[0]);
                if (deviation[kXIndex] > kLaneChangeThresholdByAccelerometer)
                    turnning = true;
                if (turnning) {
                    /*static*/
                    if (false == in_turn) {
                        in_turn = true;
                        new_pattern = new DrivingPattern();
                        new_pattern.start = accelerometer.get(i - wnd + 1).timestamp;
                    }
                } else {
                    if (true == in_turn) {
                        in_turn = false;
                        new_pattern.end = accelerometer.get(i - 1).timestamp;
                        if (isLanechangeByAccelerometer(accelerometer, new_pattern)) {
                            new_pattern.start = new_pattern.start - 1000;
                            new_pattern.end = new_pattern.end + 1000;
                            new_pattern.type = MappableEvent.Type.lanechange;
                            patterns.add(new_pattern);
                        }
                        new_pattern = null;
                    }
                }
                sliding.removeFirst();
            }
        }
        if (null != new_pattern && new_pattern.end == -1) {
            new_pattern.end = accelerometer.get(sz - 1).timestamp;
            if (isLanechangeByAccelerometer(accelerometer, new_pattern)) {
                new_pattern.start = new_pattern.start - 1000;
                new_pattern.end = new_pattern.end + 1000;
                new_pattern.type = MappableEvent.Type.lanechange;
                patterns.add(new_pattern);
            }
            new_pattern = null;
        }
        return patterns;
    }

}
