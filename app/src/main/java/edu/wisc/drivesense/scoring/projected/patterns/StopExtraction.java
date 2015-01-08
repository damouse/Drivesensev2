package edu.wisc.drivesense.scoring.projected.patterns;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;

public class StopExtraction {
    /**
     * @param Readings speed
     * @return speed fragements that the speed is constant
     */
    static public List<DrivingPattern> extractConstantSpeedIntervals(List<Reading> Readings) {
        int wnd = 10;
        double threshold = 1.0;
        return extractStopIntervals(Readings, wnd, threshold);
    }

    static public ArrayList<DrivingPattern> extractStopIntervals(List<Reading> Readings) {
        int wnd = Parameters.kAccelerometerSlidingWindowSize;
        double threshold = Parameters.kMovementDetectionThreshold;
        return extractStopIntervals(Readings, wnd, threshold);
    }

    /*
     * get the static intervals over RAW accelerometer values
     * we want to use the raw data
     * */
    static public ArrayList<DrivingPattern> extractStopIntervals(List<Reading> Readings, int wnd, double threshold) {

        ArrayList<DrivingPattern> intervals = new ArrayList<DrivingPattern>();
        int sz = Readings.size();
        //Log.log(Thread.currentThread().getStackReading()[1].getMethodName(), "the size of input Readings is:" + String.valueOf(sz));

        LinkedList<Reading> sliding = new LinkedList<Reading>();

        boolean in_static = false;

        int d = Readings.get(sz - 1).dimension;

        DrivingPattern inter = null;
        for (int i = 0; i < sz; ++i) {
            Reading Reading = Readings.get(i);

            sliding.add(Reading);
            int len = sliding.size();

            if (len == wnd) {
                //Reading p = sliding.getFirst();

                double[] deviation = Formulas.standardDeviation(sliding);

                //Log.error(deviation[0], deviation[1], deviation[2]);
                /*detect movement*/
                boolean moving = false;
                for (int j = 0; j < d; ++j) {
                    if (deviation[j] > threshold) {
                        moving = true;
                    }
                }

				/**/
                if (!moving) {
					/*static*/
                    if (false == in_static) {
                        in_static = true;

                        inter = new DrivingPattern();
                        inter.start_index = i - wnd + 1;
                        inter.start = Readings.get(i - wnd + 1).timestamp;
                    }
                } else {
                    if (true == in_static) {
                        in_static = false;
                        inter.end_index = i - 1;
                        inter.end = Readings.get(i - 1).timestamp;
                        intervals.add(inter);
                    }
                }
				/**/
                sliding.removeFirst();
            }
        }
		/*
		 * Bug found by a continues points that are static
		 * if all the points are static*/
        if (null != inter && inter.end == -1) {
            inter.end_index = sz - 1;
            inter.end = Readings.get(sz - 1).timestamp;
            intervals.add(inter);
        }

        return intervals;
    }

}