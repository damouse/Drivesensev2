package edu.wisc.drivesense.scoring.projected.patterns;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;

public class BumpExtraction {

    /*
     * get the static intervals over RAW accelerometer values
     * we want to use the raw data
     * */
    //private static double threshold = 0.5;
    private static double threshold = 0.8;

    static public List<DrivingPattern> extractBumpIntervals(List<Reading> Readings) {
        int wnd = Parameters.kAccelerometerSlidingWindowSize;

        List<DrivingPattern> intervals = new ArrayList<DrivingPattern>();
        int sz = Readings.size();
        //Log.log(Thread.currentThread().getStackReading()[1].getMethodName(), "the size of input Readings is:" + String.valueOf(sz));

        LinkedList<Reading> sliding = new LinkedList<Reading>();

        boolean in_bump = false;

        int d = Readings.get(sz - 1).dimension;

        DrivingPattern inter = null;


        List<Reading> res = new ArrayList<Reading>();
        for (int i = 0; i < sz; ++i) {
            Reading Reading = Readings.get(i);

            sliding.add(Reading);
            int len = sliding.size();

            if (len == wnd) {

                double[] deviation = Formulas.standardDeviation(sliding);

                //Log.error(Reading.timestamp, deviation[2], Reading.values[2]);
                /*
                Reading nt = new Reading();
				nt.timestamp = Reading.timestamp;
				nt.values[0] = Reading.values[0];
				nt.values[1] = Reading.values[2];
				nt.values[2] = deviation[2];
				res.add(nt);
				*/
				/*detect movement*/
                boolean bumping = false;

                if (deviation[2] > threshold)
                    bumping = true;
				/**/
                if (bumping) {
                    if (false == in_bump) {
                        in_bump = true;
                        inter = new DrivingPattern();
                        inter.start = Readings.get(i - wnd + 1).timestamp;
                    }
                } else {
                    if (true == in_bump) {
                        in_bump = false;
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
            inter.end = Readings.get(sz - 1).timestamp;
            intervals.add(inter);
        }


        //return res;

        return intervals;
    }

}
