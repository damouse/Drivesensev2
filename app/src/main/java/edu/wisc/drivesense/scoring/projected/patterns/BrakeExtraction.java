package edu.wisc.drivesense.scoring.projected.patterns;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;

public class BrakeExtraction {

    //originally 20!
    private static int wnd_ = 20;
    private static double accelerationThreshold_ = 0.5;
    private static double decelerationThreshold_ = -0.1;
    private static double percent_ = 0.7;

/* New pulled code
    private static int wnd_ = 10;
    private static double accelerationThreshold_ = 0.4;
    private static long duration_ = 4000;
    private static long brake_duration_ = 3000;
    
    private static double decelerationThreshold_ = -1.0;
    private static double percent_ = 0.7;
*/

    public static ArrayList<DrivingPattern> extractBrakeIntervals(List<Reading> accelerometer) {

        int countDeceleration = 0;
        ArrayList<DrivingPattern> decelerations = new ArrayList<DrivingPattern>();
        boolean in_deceleration = false;

        DrivingPattern interDec = null;
        int sz = accelerometer.size();
        for (int i = 0; i < sz; i++) {

            Reading Reading = accelerometer.get(i);
            double value = Reading.values[1];
            if (value < decelerationThreshold_) {
                countDeceleration++;
            }
            if (i >= wnd_) {
                Reading past = accelerometer.get(i - wnd_);
                double pv = past.values[1];

                if (pv < decelerationThreshold_) {
                    countDeceleration--;
                }
            } else
                continue;

            boolean dec = false;

            if ((double) countDeceleration / (double) wnd_ > percent_) {
                dec = true;
            }

			/*extract deceleration*/

            if (dec) {
                if (!in_deceleration) {
                    in_deceleration = true;

                    interDec = new DrivingPattern();
                    interDec.start = accelerometer.get(i - wnd_ + 1).timestamp;

                }
            } else {
                if (in_deceleration) {
                    in_deceleration = false;

                    interDec.end = accelerometer.get(i - 1).timestamp;
                    interDec.type = MappableEvent.Type.BRAKE;
                    decelerations.add(interDec);
                }
            }
        }

		/*
         * Bug found by a continues points that are static
		 * if all the points are static*/
        if (null != interDec && interDec.end == -1) {
            interDec.end = accelerometer.get(sz - 1).timestamp;
            interDec.type = MappableEvent.Type.BRAKE;
            decelerations.add(interDec);
        }
        return decelerations;

    }


    public static ArrayList<DrivingPattern> extractAccelerationIntervals(List<Reading> accelerometer) {
        ArrayList<DrivingPattern> intervals = new ArrayList<DrivingPattern>();
        int countAcceleration = 0;
        boolean in_acceleration = false;
        DrivingPattern interAcc = null;
        int sz = accelerometer.size();
        for (int i = 0; i < sz; i++) {
            Reading currReading = accelerometer.get(i);
            double value = currReading.values[1];
            if (value > accelerationThreshold_) {
                countAcceleration++;
            }
            if (i >= wnd_) {
                Reading past = accelerometer.get(i - wnd_);
                double pv = past.values[1];

                if (pv > accelerationThreshold_) {
                    countAcceleration--;
                }
            } else
                continue;

            boolean dec = false;
            if ((double) countAcceleration / (double) wnd_ > percent_) {
                dec = true;
            }
            if (dec) {
                if (!in_acceleration) {
                    in_acceleration = true;
                    interAcc = new DrivingPattern();
                    interAcc.start = accelerometer.get(i - wnd_ + 1).timestamp;
                }
            } else {
                if (in_acceleration) {
                    in_acceleration = false;
                    interAcc.end = accelerometer.get(i - 1).timestamp;
                    interAcc.type = MappableEvent.Type.ACCELERATION;
                    intervals.add(interAcc);
                }
            }
            if (null != interAcc && interAcc.end == -1) {
                interAcc.end = accelerometer.get(sz - 1).timestamp;
                interAcc.type = MappableEvent.Type.ACCELERATION;
                intervals.add(interAcc);
            }
        }
        return intervals;
    }


}
