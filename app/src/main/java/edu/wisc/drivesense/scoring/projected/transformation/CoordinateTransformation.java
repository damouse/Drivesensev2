package edu.wisc.drivesense.scoring.projected.transformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;
import edu.wisc.drivesense.scoring.projected.patterns.*;

public class CoordinateTransformation {

    /**
     * Given a Reading of accelerations coming after computed through rotation matrix,
     * calculate the projected accelerations.
     *
     * @param smoothed_before
     * @param thres-->        the threshold starting movement
     */
    public static ArrayList<Reading> Project(List<Reading> input, double thres) {
        // set the movement detection window to be 10 for default
        AccelerationDetection md = new AccelerationDetection(thres, 10);
        /*get the start and stop index of a given Reading*/
        md.setValueThreshold(0.02);
        int start = md.startMoving(input);

        int stop = md.SwitchMovement(input, start);
        ArrayList<PairDouble> raw_xy = new ArrayList<PairDouble>();
        for (int i = start; i < stop; i++) {
            PairDouble xy = new PairDouble(input.get(i).values[0], input.get(i).values[1]);
            raw_xy.add(xy);
        }
        double slope = BestFitOrigin(raw_xy);
        /*two unit vectors of mapped x and y*/
        PairDouble[] newAxes = AxesUnitVector(raw_xy, slope);
        ArrayList<Reading> mapped = new ArrayList<Reading>();

        for (Reading tr : input) {
            PairDouble vec = new PairDouble(tr.values[0], tr.values[1]);
            Reading newtr = new Reading(tr);

            newtr.values[0] = Formulas.DotProduct(vec, newAxes[0]);
            newtr.values[1] = Formulas.DotProduct(vec, newAxes[1]);
            newtr.values[2] = tr.values[2];

            mapped.add(newtr);
        }
        return mapped;
    }

    /**
     * Given a Reading of accelerations coming after computed through rotation matrix,
     * calculate the projected accelerations.
     *
     * @param smoothed_before
     * @param thres-->        the threshold starting movement
     */
    public static ArrayList<Reading> ProjectNew(List<Reading> input) {
        List<Reading> head_straight = getHeadingStraightReadings(input);
        ArrayList<PairDouble> raw_xy = new ArrayList<PairDouble>();
        for (Reading t : head_straight) {
            PairDouble xy = new PairDouble(t.values[0], t.values[1]);
            raw_xy.add(xy);
        }
        double slope = BestFitOrigin(raw_xy);
		/*two unit vectors of mapped x and y*/
        PairDouble[] rawAxes = AxesUnitVector(raw_xy, slope);
        //adjust the coordinate system
        ArrayList<Reading> mapped = new ArrayList<Reading>();

        for (Reading tr : input) {
            PairDouble vec = new PairDouble(tr.values[0], tr.values[1]);
            double values[] = new double[tr.dimension];

            values[0] = Formulas.DotProduct(vec, rawAxes[0]);
            values[1] = Formulas.DotProduct(vec, rawAxes[1]);
            values[2] = tr.values[2];

            mapped.add(new Reading(values, tr.timestamp, tr.type));
        }
        correctProjectDirection(mapped);
        return mapped;
    }


    public static void correctProjectDirection(List<Reading> projected) {
        if (isCoordinateMappingReversed(projected)) {
            for (Reading t : projected) {
                t.values[1] *= -1;
                t.values[0] *= -1;
            }
        }
    }

    public static boolean isCoordinateMappingReversed(List<Reading> projected) {
        List<DrivingPattern> stoppingReadings = DrivingPattern.reduceOverlapIntervals(StopExtraction.extractStopIntervals(projected));
        int index = 0;

        //the time that the car start moving
        List<Long> stoppingTime = new ArrayList<Long>();
        int stopSz = stoppingReadings.size();

        //remove the cases that two stop pattern are very close (within 3 seconds)
        while (index < stopSz - 1) {
            DrivingPattern curr = stoppingReadings.get(index);
            DrivingPattern next = stoppingReadings.get(index + 1);
            if (next.start - curr.end <= 3000) {
                stoppingTime.add(next.end);
                index += 2;
            } else {
                stoppingTime.add(curr.end);
                index++;
            }
        }

        //including the last case
        if (index != stopSz) {
            DrivingPattern last = stoppingReadings.get(stopSz - 1);
            stoppingTime.add(last.end);
        }

        int up = 0, down = 0, bigger = 0, smaller = 0;
        for (Long s : stoppingTime) {
            List<Reading> subReading = (List<Reading>) PreProcess.extractSubList(projected, s, s + 3000);
            //compare each Reading the the first Reading
            for (Reading t : subReading) {
                if (t.values[1] >= subReading.get(0).values[1]) {
                    bigger++;
                } else {
                    smaller++;
                }
            }
            if (bigger > smaller) {
                up++;
            } else {
                down++;
            }
        }
        if (up >= down) {
            return false;
        }
        return true;
    }

    /**
     * @param Readings
     * @return a sub list that has the minimum degree deviation
     */
    @SuppressWarnings("unchecked")
    public static List<Reading> getHeadingStraightReadings(List<Reading> Readings) {
        int wnd = 30;
        List<Reading> smallestSliding = new LinkedList<Reading>();
        LinkedList<Reading> sliding = new LinkedList<Reading>();
        double smallestDeviation = Double.POSITIVE_INFINITY;
        int sz = Readings.size();
        for (int i = 0; i < sz; i++) {
            Reading currReading = Readings.get(i);
            //calculate the degree of X and Y in each Reading
            currReading.calculateDegrees();
            sliding.add(currReading);
            int len = sliding.size();
            if (len == wnd) {
                //only return the deviation of the degree
                double deviation = Formulas.standardDeviationDegree(sliding);
                //System.out.println(smallestDeviation + " " + deviation);
                if (deviation < smallestDeviation) {
                    smallestDeviation = deviation;
                    smallestSliding = (List<Reading>) sliding.clone();
                }
                sliding.removeFirst();
            }
        }

        return smallestSliding;
    }


    /**
     * @param raw_xys
     * @param slope
     * @return
     */

    public static PairDouble[] AxesUnitVector(ArrayList<PairDouble> raw_xys, double slope) {
        PairDouble unit_x = new PairDouble();
        PairDouble unit_y = new PairDouble();

        int rightnum = 0;
        double perpendicular_slope = -1 / slope;

        for (int i = 0; i < raw_xys.size(); i++) {
            PairDouble xy = raw_xys.get(i);
            if (slope > 0 ^ xy.x * perpendicular_slope > xy.y)
                rightnum++;
        }

        int y_indicator = (rightnum / raw_xys.size() > Constants.PERCENT_) ? 1 : -1;
        int x_indicator = ((y_indicator > 0) ^ (slope > 0)) ? -1 : 1;

        unit_x = Formulas.UnitVector(new PairDouble(x_indicator, x_indicator * perpendicular_slope));
        unit_y = Formulas.UnitVector(new PairDouble(y_indicator, y_indicator * slope));

        PairDouble[] res = {unit_x, unit_y};

        return res;
    }

    /**
     * Get the best fit line through origin.
     *
     * @param points
     * @return: the slope of the line, (from 0--45--90, 0--1--infinite)
     */
    public static double BestFitOrigin(ArrayList<PairDouble> points) {
        int n = points.size();
        double sum_xy, sum_x2, sum_y2;
        sum_xy = sum_x2 = sum_y2 = 0;
        for (int i = 0; i < n; i++) {
            double x = points.get(i).x;
            double y = points.get(i).y;
            sum_xy += x * y;
            sum_x2 += Math.pow(x, 2);
            sum_y2 += Math.pow(y, 2);
        }

        double temp = sum_y2 - sum_x2;

        // get both + and - slopes.
        double slope1 = (temp + Math.sqrt(Math.pow(temp, 2) + 4 * Math.pow(sum_xy, 2))) / (2 * sum_xy);
        double slope2 = (temp - Math.sqrt(Math.pow(temp, 2) + 4 * Math.pow(sum_xy, 2))) / (2 * sum_xy);
        // plug into the distance calculation equation to compare which one is smaller.
        double ds1 = Formulas.DistanceSquare(slope1, sum_x2, sum_y2, sum_xy);
        double ds2 = Formulas.DistanceSquare(slope2, sum_x2, sum_y2, sum_xy);
        return (ds1 < ds2) ? slope1 : slope2;
    }

    /**
     * @param raw_accels: the raw accelerations
     * @param parameters: the parameter, could be orientaion or rotation matrix.
     * @param method:     orientation or rotation matrix.
     * @return
     */
    public static ArrayList<Reading> Calculate(List<Reading> sampled, Reading parameters, String method) {
        ArrayList<Reading> true_accels = new ArrayList<Reading>();

        double[] arg = parameters.values;

        for (Reading tr : sampled) {
            Reading true_tr = RotationMethod(tr, arg);
            true_accels.add(true_tr);
        }

        return true_accels;
    }

    /**
     * Using 3D angular mathematical mapping.
     *
     * @param raw_tr
     * @param orientation
     * @return
     */
    public static Reading OrientationMethod(Reading raw_tr, double[] orientation) {
        Reading calculated_tr = new Reading(raw_tr);

        double x = raw_tr.values[0];
        double y = raw_tr.values[1];
        double z = raw_tr.values[2];

        double azimuth = orientation[0];
        double pitch = orientation[1];
        double yaw = orientation[2];

        calculated_tr.values[0] = (double) (x
                * (Math.cos(yaw) * Math.cos(azimuth) + Math.sin(yaw)
                * Math.sin(pitch) * Math.sin(azimuth)) + y
                * (Math.cos(pitch) * Math.sin(azimuth)) + z
                * (-Math.sin(yaw) * Math.cos(azimuth) + Math.cos(yaw)
                * Math.sin(pitch) * Math.sin(azimuth)));
        calculated_tr.values[1] = (double) (x
                * (-Math.cos(yaw) * Math.sin(azimuth) + Math.sin(yaw)
                * Math.sin(pitch) * Math.cos(azimuth)) + y
                * (Math.cos(pitch) * Math.cos(azimuth)) + z
                * (Math.sin(yaw) * Math.sin(azimuth) + Math.cos(yaw)
                * Math.sin(pitch) * Math.cos(azimuth)));
        calculated_tr.values[2] = (double) (x * (Math.sin(yaw) * Math.cos(pitch)) + y
                * (-Math.sin(pitch)) + z * (Math.cos(yaw) * Math.cos(pitch)));

        return calculated_tr;
    }

    /**
     * Using rotational matrix.
     *
     * @param raw_tr
     * @param rM
     * @return
     */
    public static Reading RotationMethod(Reading raw_tr, double[] rM) {
        Reading calculated_tr = new Reading(raw_tr);

        double x, y, z;
        x = raw_tr.values[0];
        y = raw_tr.values[1];
        z = raw_tr.values[2];

        calculated_tr.values[0] = x * rM[0] + y * rM[1] + z * rM[2];
        calculated_tr.values[1] = x * rM[3] + y * rM[4] + z * rM[5];
        calculated_tr.values[2] = x * rM[6] + y * rM[7] + z * rM[8];

        return calculated_tr;
    }

}
