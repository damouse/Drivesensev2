package edu.wisc.drivesense.scoring.projected;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;

import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.projected.patterns.BrakeExtraction;
import edu.wisc.drivesense.scoring.projected.patterns.LanechangeExtraction;
import edu.wisc.drivesense.scoring.projected.patterns.PatternEvaluation;
import edu.wisc.drivesense.scoring.projected.patterns.StopExtraction;
import edu.wisc.drivesense.scoring.projected.patterns.TurnExtraction;
import edu.wisc.drivesense.scoring.projected.processing.PreProcess;
import edu.wisc.drivesense.scoring.projected.transformation.TransformationHelper;

/**
 * Created by Damouse on 12/15/2014.
 * <p/>
 * The intro point for all manner of projected driving analysis.
 *
 * Methods here are called as periods are collected from teh data receiver.
 *
 * Make no assumptions about the frequency of these updates! If the period is
 * changed, then those assumptions go out the window.
 *
 * TODO: move calculateRotationMatricies to dataReceiver
 */
public class ProjectedScoreKeeper {
    private static final String TAG = "ProjectedScoreKeeper";

    private Context context;

    private boolean hasGyro = true;

    private List<Reading> accelerometer_projected = null;
    private List<Reading> gyroscope_projected = null;
    private List<Reading> rotationMatrix = null;


    public ProjectedScoreKeeper(Context context) {
        this.context = context;
    }

    /**
     * Returns an array list of the driving events that occured in this period.
     *
     * These events are scored (if they're patterns) with redundant gps coordinates removed.
     */
    public TimestampQueue<DrivingPattern> getDrivingEvents(DataSetInput period) {
//        rotationMatrix = PreProcess.calculateRotationMatricies(period, context).getContents();

        rotationMatrix = period.rotationMatricies.getContents();
        projectSensors(period.acceleration, period.gyroscope);
        TimestampQueue<DrivingPattern> patterns = extractPatterns(period);
        getScores(patterns.getContents(), accelerometer_projected);

        //Log.i(TAG, "Done");
        return patterns;
    }


    /* Scoring */
    private void scorePatterns(TimestampQueue patterns) {

    }

    /**
     * Return a list of patterns based on scoring. If no patterns were recognized,
     * list returns empty.
     * <p/>
     * Note that the context param is not needed here.
     */
    public TimestampQueue<DrivingPattern> extractPatterns(DataSetInput period) {
        //Log.i(TAG, "Extracting patterns...");

        TimestampQueue<DrivingPattern> patterns = new TimestampQueue<DrivingPattern>();

        ArrayList<DrivingPattern> brakes = BrakeExtraction.extractBrakeIntervals(accelerometer_projected);
        brakes = DrivingPattern.reduceOverlapIntervals(brakes);
        getScores(brakes, accelerometer_projected);
        patterns.addList(brakes);

        //calculate the number of patterns and the score of the patterns
        ArrayList<DrivingPattern> accelerations = BrakeExtraction.extractAccelerationIntervals(accelerometer_projected);
        accelerations = DrivingPattern.reduceOverlapIntervals(accelerations);
        getScores(accelerations, accelerometer_projected);
        patterns.addList(accelerations);

        ArrayList<DrivingPattern> turns;
        ArrayList<DrivingPattern> lanes;

        if (hasGyro) {
            turns = TurnExtraction.extractTurns(gyroscope_projected);
            lanes = LanechangeExtraction.extractLanechanges(gyroscope_projected);
        } else {
            turns = TurnExtraction.extractTurnsByAccelerometer(accelerometer_projected);
            lanes = LanechangeExtraction.extractLanechangesByAccelerometer(accelerometer_projected);
        }

        turns = DrivingPattern.reduceOverlapIntervals(turns);
        getScores(turns, accelerometer_projected);
        patterns.addList(turns);

        lanes = DrivingPattern.reduceOverlapIntervals(lanes);
        getScores(lanes, accelerometer_projected);
        patterns.addList(lanes);

        patterns.sort();

        return patterns;
    }


    /* Scoring */
    private static void getScores(ArrayList<DrivingPattern> patterns, List<Reading> accelerometer) {
        //Log.i(TAG, "Scoring...");
        for (DrivingPattern pattern : patterns) {
            if (pattern.type == MappableEvent.Type.brake) {
                double temp = PatternEvaluation.evaluate_brake(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);

                if (pattern.score < 60.0) pattern.score = 60.0;
                pattern.score = (pattern.score / 4.0 - 15.0) * 10.0;
            } else if (pattern.type == MappableEvent.Type.acceleration) {
                double temp = PatternEvaluation.evaluate_acceleration(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);
            } else if (pattern.type == MappableEvent.Type.turn) {
                double temp = PatternEvaluation.evaluate_turn(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);
            } else if (pattern.type == MappableEvent.Type.lanechange) {
                double temp = PatternEvaluation.evaluate_lanechange(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);
            }
        }
    }


    /* Move this somewhere else, please */
    private void projectSensors(TimestampQueue<Reading> accelerometer, TimestampQueue<Reading> gyroscope) {
        //Log.i(TAG, "Projecting sensors...");
        //Preprocess, smooth and interpolate first

        TimestampQueue<Reading> accelerometer_smoothed = PreProcess.exponentialMovingAverage(accelerometer);
        //List<Reading> accelerometer_interpolate = PreProcess.interpolate(accelerometer_smoothed, 10);

        //projection
        //ensure the passed array has enough entries to meas
        int accelIndex = 100;
        if (accelIndex >= accelerometer.size())
            accelIndex = accelerometer.size() - 1;

        List<DrivingPattern> pats = StopExtraction.extractStopIntervals(accelerometer_smoothed.getContents());
        long start = accelerometer.startTime();
        long end = accelerometer.endTime();

        if (null != pats && pats.size() != 0) {
            start = pats.get(0).start;
            end = pats.get(0).end;
        }

        TransformationHelper helper = new TransformationHelper();
        List<Reading> subrm = PreProcess.extractSubList(rotationMatrix, start, end);
        Reading rotation = PreProcess.getAverage(subrm);
        accelerometer_projected = helper.transformByAccelerometer(accelerometer_smoothed.getContents(), rotation);

		/*=======gyroscope=========*/

        if (null != gyroscope) {
            hasGyro = true;
            List<Reading> gyroscope_smoothed = PreProcess.exponentialMovingAverage(gyroscope).getContents();
            List<Reading> gyroscope_interpolate = PreProcess.interpolate(gyroscope_smoothed, 10);
            gyroscope_projected = helper.transform(gyroscope_interpolate, true);
        }
    }
}
