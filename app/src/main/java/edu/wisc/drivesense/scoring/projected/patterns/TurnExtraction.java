package edu.wisc.drivesense.scoring.projected.patterns;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;


/**
 *
 *
 */

public class TurnExtraction {
    //sliding windows size

    /*a really small turn threshold*/
    private static double kTurnThreshold = 0.08;
    private static int kSlidingWindowSize = 20;
    private static int kZIndex = 2;
    private static double kPercent = 0.7;
    private static double kAccelerometerTurnThreshold = 0.5;
    private static int kXIndex = 0;
    private static double kAccelerometerPercent = 0.5;

    private static boolean isTurn(List<Reading> gyroscope, DrivingPattern pattern) {

        if (pattern.end - pattern.start < 2000) return false;

        List<Reading> sub = (List<Reading>) PreProcess.extractSubList(gyroscope, pattern.start, pattern.end);
        int sz = sub.size();
        double rads = 0.0;

        for (int i = 0; i < sz - 1; ++i) {
            Reading Reading = sub.get(i);
            long time_diff = gyroscope.get(i + 1).timestamp - gyroscope.get(i).timestamp;
            double z = Reading.values[kZIndex];
            rads += Math.abs(z * (time_diff / 1000.0));
        }
        //Log.log(rads, Math.toDegrees(rads));
        if (Math.abs(Math.toDegrees(rads)) > 60.0)
            return true;
        return false;
    }

    public static List<DrivingPattern> extractTurns(List<Reading> gyroscope) {
        List<DrivingPattern> patterns = new ArrayList<DrivingPattern>();
        int sz = gyroscope.size();
        int counter = 0;
        boolean inTurn = false;

        DrivingPattern new_pattern = null;
        for (int i = 0; i < sz; i++) {
            Reading Reading = gyroscope.get(i);
            double value = Reading.values[kZIndex];
            if (Math.abs(value) >= kTurnThreshold) counter++;
            if (i < kSlidingWindowSize) continue;

			/*when i >= window size, we track the first item in the window size*/
            Reading past = gyroscope.get(i - kSlidingWindowSize);
            double pv = past.values[kZIndex];
            if (Math.abs(pv) >= kTurnThreshold) counter--;


            boolean turning = false;
            if ((double) counter / (double) kSlidingWindowSize > kPercent) {
                turning = true;
            }

            if (turning) {
                if (!inTurn) {
                    inTurn = true;
                    new_pattern = new DrivingPattern();
                    new_pattern.start = gyroscope.get(i - kSlidingWindowSize + 1).timestamp;
                }
            } else {
                if (inTurn) {
                    inTurn = false;
                    new_pattern.end = gyroscope.get(i - 1).timestamp;
                    new_pattern.type = MappableEvent.Type.TURN;
                    if (isTurn(gyroscope, new_pattern)) {
                        patterns.add(new_pattern);
                    }
                    new_pattern = null;
                }
            }
        }
        if (null != new_pattern) {
            new_pattern.end = gyroscope.get(sz - 1).timestamp;
            new_pattern.type = MappableEvent.Type.TURN;
            if (isTurn(gyroscope, new_pattern)) {
                patterns.add(new_pattern);
            }
        }
        return patterns;
    }

    /**
     * Extract the turns by accelerometer if gyroscope is not available on device
     *
     * @param accelerometer
     * @return the extracted patterns
     */
    public static List<DrivingPattern> extractTurnsByAccelerometer(List<Reading> accelerometer) {
        List<DrivingPattern> patterns = new ArrayList<DrivingPattern>();
        int sz = accelerometer.size();
        int counter = 0;
        boolean inTurn = false;

        DrivingPattern new_pattern = null;
        for (int i = 0; i < sz; i++) {
            Reading Reading = accelerometer.get(i);
            double value = Reading.values[kXIndex];
            if (Math.abs(value) >= kAccelerometerTurnThreshold) counter++;
            if (i < kSlidingWindowSize) continue;

			/*when i >= window size, we track the first item in the window size*/
            Reading past = accelerometer.get(i - kSlidingWindowSize);
            double pv = past.values[kXIndex];
            if (Math.abs(pv) >= kAccelerometerTurnThreshold) counter--;


            boolean turning = false;
            if ((double) counter / (double) kSlidingWindowSize > kAccelerometerPercent) {
                turning = true;
            }

            if (turning) {
                if (!inTurn) {
                    inTurn = true;
                    new_pattern = new DrivingPattern();
                    new_pattern.start = accelerometer.get(i - kSlidingWindowSize + 1).timestamp;
                }
            } else {
                if (inTurn) {
                    inTurn = false;
                    new_pattern.end = accelerometer.get(i - 1).timestamp;
                    new_pattern.type = MappableEvent.Type.TURN;
                    if (new_pattern.end - new_pattern.start >= 2000) {
                        patterns.add(new_pattern);
                    }
                    new_pattern = null;
                }
            }
        }
        if (null != new_pattern) {
            new_pattern.end = accelerometer.get(sz - 1).timestamp;
            new_pattern.type = MappableEvent.Type.TURN;
            if (new_pattern.end - new_pattern.start >= 2000) {
                patterns.add(new_pattern);
            }
        }
        return patterns;
    }

}
