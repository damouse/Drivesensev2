package edu.wisc.drivesense.scoring.projected.patterns;

import java.text.SimpleDateFormat;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;

/**
 * @author lkang
 * @category pattern evaluations
 */


public class PatternEvaluation {


    /*====================brake===========================================*/
    private static double brake_duration_max = 8000;
    private static double brake_duration_min = 3000;

    private static double brake_strength_max = 8.0;
    private static double brake_strength_min = 1.0;

    private static double brake_deviation_max = 2.0;
    private static double brake_deviation_min = 0.5;


    private static double alpha = 0.6;
    /*==========================Acceleration===============================*/
    private static double acceleration_duration_max = 15000;
    private static double acceleration_duration_min = 6000;
    private static double acceleration_strength_max = 2.0;
    private static double acceleration_strength_min = 0.3;
    private static double acceleration_deviation_max = 1.0;
    private static double acceleration_deviation_min = 0.1;
    /*===============================Turns===================================*/
    private static double turn_duration_max = 8000;
    private static double turn_duration_min = 4000;
    private static double turn_strength_max = 5.0;
    private static double turn_strength_min = 1.0;
    private static double turn_deviation_max = 2.5;
    private static double turn_deviation_min = 0.3;
    /*=====================lane changes==================================================*/
    private static double lanechange_duration_max = 8000;
    private static double lanechange_duration_min = 4000;
    private static double lanechange_strength_max = 3.0;
    private static double lanechange_strength_min = 0.1;
    private static double lanechange_deviation_max = 1.2;
    private static double lanechange_deviation_min = 0.3;
    private static double bump_strength_max = 3.0;
    private static double bump_strength_min = 0.1;
    private static double bump_deviation_max = 1.2;
    private static double bump_deviation_min = 0.3;

    private static double normalize_negative(double min, double max, double value) {
        if (value > max) value = max;
        if (value < min) value = min;
        return alpha * (max - value) / (max - min) + (1 - alpha);
    }

    private static double normalize_positive(double min, double max, double value) {
        if (value > max) value = max;
        if (value < min) value = min;
        return alpha * (value - min) / (max - min) + (1 - alpha);
    }

    /**
     * @param accelerometer one extracted brake pattern
     * @return the score of this brake
     */
    public static double evaluate_brake(DrivingPattern pattern, List<Reading> accelerometer) {
        long start = pattern.start;
        long end = pattern.end;


        List<Reading> brake = (List<Reading>) PreProcess.extractSubList(accelerometer, start, end);
        //double [][] temp = Formulas.absoluteDeviation(brake);

        double average = PreProcess.getAverage(brake).values[1];
        double deviation = Formulas.standardDeviation(brake)[1];

        //double average = temp[0][1];
        //double deviation = temp[1][1];
        double peak = 0.0;
        for (Reading Reading : brake) {
            peak = Math.max(peak, Math.abs(Reading.values[1]));
        }
        double duration = end - start;
        //Log.log("peak", peak);

        double[] scores = new double[4];
        //scores[0] = normalize(speed_min, speed_max, speed);
        scores[0] = normalize_positive(brake_duration_min, brake_duration_max, duration);
        scores[1] = normalize_negative(brake_strength_min, brake_strength_max, Math.abs(average));
        scores[2] = normalize_negative(brake_deviation_min, brake_deviation_max, Math.abs(deviation));
        scores[3] = normalize_negative(brake_strength_min, brake_strength_max, peak);

        double score = (scores[1] + scores[2] + scores[3]) / 3.0;
        //Log.log(average, scores[1]);

        if (Math.abs(peak) > 2.50) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String date = dateFormat.format(start);
            //Log.log("peak", peak, date);
        }
        return score;
    }

    /**
     * @param accelerometer one extracted brake pattern
     * @return the score of this brake
     */
    public static double evaluate_acceleration(DrivingPattern pattern, List<Reading> accelerometer) {
        long start = pattern.start;
        long end = pattern.end;

        List<Reading> acc = (List<Reading>) PreProcess.extractSubList(accelerometer, start, end);
        double average = PreProcess.getAverage(acc).values[1];
        double deviation = Formulas.standardDeviation(acc)[1];

        double peak = 0.0;
        for (Reading Reading : acc) {
            peak = Math.max(peak, Math.abs(Reading.values[1]));
        }
        double duration = end - start;
        double[] scores = new double[4];

		/*
        Log.log("average:", average);
		Log.log("deviation:", deviation);
		Log.log("peak:", peak);
		*/
        scores[0] = normalize_positive(acceleration_duration_min, acceleration_duration_max, duration);
        scores[1] = normalize_negative(acceleration_strength_min, acceleration_strength_max, Math.abs(average));
        scores[2] = normalize_negative(acceleration_deviation_min, acceleration_deviation_max, Math.abs(deviation));
        scores[3] = normalize_negative(acceleration_strength_min, acceleration_strength_max, peak);

        double score = (scores[1] + scores[2] + scores[3]) / 3.0;

        return score;
    }

    public static double evaluate_turn(DrivingPattern pattern, List<Reading> accelerometer) {
        long start = pattern.start;
        long end = pattern.end;

        List<Reading> turn = (List<Reading>) PreProcess.extractSubList(accelerometer, start, end);

        double average = PreProcess.getAverage(turn).values[0];
        double deviation = Formulas.standardDeviation(turn)[0];

        double peak = 0.0;
        for (Reading Reading : turn) {
            peak = Math.max(peak, Math.abs(Reading.values[0]));
        }
        double duration = end - start;
		/*
		Log.log("average value:", average);
		Log.log("deviation:", deviation);
		Log.log("peak:", peak);
		*/
        double[] scores = new double[4];

        scores[0] = normalize_positive(turn_duration_min, turn_duration_max, duration);
        scores[1] = normalize_negative(turn_strength_min, turn_strength_max, Math.abs(average));
        scores[2] = normalize_negative(turn_deviation_min, turn_deviation_max, Math.abs(deviation));
        scores[3] = normalize_negative(turn_strength_min, turn_strength_max, peak);
        double score = (scores[1] + scores[2] + scores[3]) / 3.0;
        return score;
    }

    private static double[] getABSAverage(List<Reading> Readings) {
        int sz = Readings.size();
        int dim = Readings.get(0).dimension;
        double[] res = new double[dim];
        for (int i = 0; i < sz; ++i) {
            for (int j = 0; j < dim; ++j) {
                res[j] += Math.abs(Readings.get(i).values[j]);
            }
        }
        for (int j = 0; j < dim; ++j)
            res[j] /= sz;
        return res;
    }

    public static double evaluate_lanechange(DrivingPattern pattern, List<Reading> accelerometer) {

        long start = pattern.start;
        long end = pattern.end;

        List<Reading> turn = (List<Reading>) PreProcess.extractSubList(accelerometer, start, end);

        //double average = PreProcess.getAverage(turn).values[0];
        double average = getABSAverage(turn)[0];

        double deviation = Formulas.standardDeviation(turn)[0];

        double peak = 0.0;
        for (Reading Reading : turn) {
            peak = Math.max(peak, Math.abs(Reading.values[0]));
        }
        double duration = end - start;
		/*
		Log.log("average:", Math.abs(average));
		Log.log("deviation:", Math.abs(deviation));
		Log.log("peak:", peak);
		*/
        double[] scores = new double[4];

        scores[0] = normalize_positive(lanechange_duration_min, lanechange_duration_max, duration);
        scores[1] = normalize_negative(lanechange_strength_min, lanechange_strength_max, Math.abs(average));
        scores[2] = normalize_negative(lanechange_deviation_min, lanechange_deviation_max, Math.abs(deviation));
        scores[3] = normalize_negative(lanechange_strength_min, lanechange_strength_max, Math.abs(peak));

        double score = (scores[2] + scores[3]) / 2.0;
        return score;
    }

    public static double evaluate_bump(DrivingPattern pattern, List<Reading> accelerometer) {

        long start = pattern.start;
        long end = pattern.end;

        List<Reading> turn = (List<Reading>) PreProcess.extractSubList(accelerometer, start, end);

        double average = getABSAverage(turn)[2];
        double deviation = Formulas.standardDeviation(turn)[2];

        double peak = 0.0;
        for (Reading Reading : turn) {
            peak = Math.max(peak, Math.abs(Reading.values[2]));
        }
        double duration = end - start;
        double[] scores = new double[4];

        scores[0] = normalize_positive(lanechange_duration_min, lanechange_duration_max, duration);
        scores[1] = normalize_negative(lanechange_strength_min, lanechange_strength_max, Math.abs(average));
        scores[2] = normalize_negative(bump_deviation_min, bump_deviation_max, Math.abs(deviation));
        scores[3] = normalize_negative(lanechange_strength_min, lanechange_strength_max, Math.abs(peak));

        double score = (scores[2] + scores[3]) / 2.0;
        return scores[2];
    }

}
