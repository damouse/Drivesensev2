package edu.wisc.drivesense.scoring.projected;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;

import edu.wisc.drivesense.scoring.common.GpsThief;
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
 */
public class ProjectedScoreKeeper {
    private static final String TAG = "ProjectedScoreKeeper";

    private static boolean hasGyro = true;
    private static Context localContext;

    private static List<Reading> accelerometer_projected = null;
    private static List<Reading> gyroscope_projected = null;


    public static ArrayList<MappableEvent> getDrivingEvents(DataSetInput period, Context context) {
        localContext = context;

        ArrayList<DrivingPattern> patterns = scorePeriod(period);

        Log.d(TAG, "Number of patterns: " + patterns.size());

        return GpsThief.getSparseCoordinates(period, patterns);
    }


    /**
     * Return a list of patterns based on scoring. If no patterns were recognized,
     * list returns empty.
     * <p/>
     * Note that the context param is not needed here.
     */
    public static TimestampQueue scorePeriod(DataSetInput period) {
        Log.d(TAG, "Starting Trip Score");

        //Ahh, java generics casting. Wunderbar.
        List<Reading> accelerometer = (List<Reading>) (List<?>) period.acceleration.contents;
        List<Reading> gyroscope = (List<Reading>) (List<?>) period.gyroscope.contents;
        List<Reading> magnetic = (List<Reading>) (List<?>) period.magnet.contents;

        List<Reading> rotation_matrix = PreProcess.calculateRotationMatricies(accelerometer, magnetic, localContext);

        //project accelerometer and gyroscope
        projectSensors(accelerometer, gyroscope, rotation_matrix);
//        Score score = trip.score;

        List<DrivingPattern> brakes = BrakeExtraction.extractBrakeIntervals(accelerometer_projected);
        brakes = DrivingPattern.reduceOverlapIntervals(brakes);
        getScores(brakes, accelerometer_projected);

        //calculate the number of patterns and the score of the patterns
        List<DrivingPattern> accelerations = BrakeExtraction.extractAccelerationIntervals(accelerometer_projected);
        accelerations = DrivingPattern.reduceOverlapIntervals(accelerations);
        getScores(accelerations, accelerometer_projected);

        List<DrivingPattern> turns;
        List<DrivingPattern> lanes;

        if (hasGyro) {
            turns = TurnExtraction.extractTurns(gyroscope_projected);
            lanes = LanechangeExtraction.extractLanechanges(gyroscope_projected);
        } else {
            turns = TurnExtraction.extractTurnsByAccelerometer(accelerometer_projected);
            lanes = LanechangeExtraction.extractLanechangesByAccelerometer(accelerometer_projected);
        }

        turns = DrivingPattern.reduceOverlapIntervals(turns);
        getScores(turns, accelerometer_projected);

        lanes = DrivingPattern.reduceOverlapIntervals(lanes);
        getScores(lanes, accelerometer_projected);

        //Aggregate the patterns and postprocess them before adding them to the score object
        ArrayList<DrivingPattern> allPatterns = new ArrayList<DrivingPattern>();

        //score wrapper object. Set the patterns here, they are saved opaquely
        allPatterns.addAll(brakes);
        allPatterns.addAll(accelerations);
        allPatterns.addAll(turns);
        allPatterns.addAll(lanes);

        return allPatterns;

    }

    /* Pattern Extraction */
    private static TimestampQueue extractAccelerations(DataSetInput period, List<Reading> rotationMatrices) {

    }


    private static void getScores(List<DrivingPattern> patterns, List<Reading> accelerometer) {
        for (DrivingPattern pattern : patterns) {
            if (pattern.type == MappableEvent.Type.BRAKE) {
                double temp = PatternEvaluation.evaluate_brake(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);

                if (pattern.score < 60.0) pattern.score = 60.0;
                pattern.score = (pattern.score / 4.0 - 15.0) * 10.0;
            } else if (pattern.type == MappableEvent.Type.ACCELERATION) {
                double temp = PatternEvaluation.evaluate_brake(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);
            } else if (pattern.type == MappableEvent.Type.TURN) {
                double temp = PatternEvaluation.evaluate_brake(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);
            } else if (pattern.type == MappableEvent.Type.LANE_CHANGE) {
                double temp = PatternEvaluation.evaluate_brake(pattern, accelerometer);
                pattern.score = (100 - (1.0 - temp) / 0.006);
            }
        }
    }

    private static void projectSensors(List<Reading> accelerometer, List<Reading> gyroscope, List<Reading> rotation_matrix) {
        //Preprocess, smooth and interpolate first
        List<Reading> accelerometer_smoothed = PreProcess.exponentialMovingAverage(accelerometer);
        //List<Reading> accelerometer_interpolate = PreProcess.interpolate(accelerometer_smoothed, 10);

        //projection
        //ensure the passed array has enough entries to meas
        int accelIndex = 100;
        if (accelIndex >= accelerometer.size())
            accelIndex = accelerometer.size() - 1;

        List<DrivingPattern> pats = StopExtraction.extractStopIntervals(accelerometer_smoothed);
        long start = accelerometer.get(0).timestamp;
        long end = accelerometer.get(accelIndex).timestamp;

        if (null != pats && pats.size() != 0) {
            start = pats.get(0).start;
            end = pats.get(0).end;
        }

        TransformationHelper helper = new TransformationHelper();
        List<Reading> subrm = PreProcess.extractSubList(rotation_matrix, start, end);
        Reading rotation = PreProcess.getAverage(subrm);
        accelerometer_projected = helper.transformByAccelerometer(accelerometer_smoothed, rotation);

		/*=======gyroscope=========*/

        if (null != gyroscope) {
            hasGyro = true;
            List<Reading> gyroscope_smoothed = PreProcess.exponentialMovingAverage(gyroscope);
            List<Reading> gyroscope_interpolate = PreProcess.interpolate(gyroscope_smoothed, 10);
            gyroscope_projected = helper.transform(gyroscope_interpolate, true);
        }
    }
}

/* Orphaned Code

        //link patterns to GPS coordinates-- the returnged list is an array of gps ids that are associated
//        List<GPSReading> undeleteGPS = associatePatternsWithGps(trip, allPatterns);
//        List<GPSReading> deleteGPS = getGPSToDelete(trip, undeleteGPS);

//        score.patterns.addAll(allPatterns);
//
//        //account for any missing items
//        if (Float.isNaN(score.scoreBreaks) || Float.isInfinite(score.scoreBreaks))
//            score.scoreBreaks = 100;
//        if (Float.isNaN(score.scoreAccels) || Float.isInfinite(score.scoreAccels))
//            score.scoreAccels = 100;
//        if (Float.isNaN(score.scoreTurns) || Float.isInfinite(score.scoreTurns))
//            score.scoreTurns = 100;
//        if (Float.isNaN(score.scoreLaneChanges) || Float.isInfinite(score.scoreLaneChanges))
//            score.scoreLaneChanges = 100;
//
//        Float doubleScore = (score.scoreBreaks / 4 + score.scoreAccels / 4 + score.scoreTurns / 4 + score.scoreLaneChanges / 4);
//        score.score = doubleScore.intValue();

        //return deleteGPS;
    }

    /**
     * Score the given trip based on readings present within the trip object.
     * <p/>
     * Assumption: calls to this method come from an async block, thus running it in the background
     *
     * @param trip object to be scored
     * @return the list of GPS readings to delete
     */
//    public static List<GPSReading> scoreTrip(Context context) {
//        Log.d(TAG, "Starting Trip Score");
//        localContext = context;
//
//        List<Reading> accelerometer = trip.getAccelReadings();
//        List<Reading> gyroscope = trip.getGyroReadings();
//        List<Reading> magnetic = trip.getMegneticReadings();
//
//        List<Reading> rotation_matrix = PreProcess.calculateRotationMatricies(accelerometer, magnetic, context);
//
//        calculateDrivingRate(accelerometer, gyroscope, rotation_matrix);
//
//        Log.d(TAG, "Score for [" + trip.name + "]: " + trip.score.toString());
//        return delete;
//    }

//        for(Pattern pattern: brakes)
//            score.scoreBreaks += pattern.score;
//        score.scoreBreaks /= brakes.size();
//
//        for(Pattern pattern: accelerations)
//            score.scoreAccels += pattern.score;
//        score.scoreAccels /= accelerations.size();
//
//        for(Pattern pattern: turns)
//            score.scoreTurns += pattern.score;
//        score.scoreTurns /= turns.size();
//
//        for(Pattern pattern: lanes)
//            score.scoreLaneChanges += pattern.score;
//        score.scoreLaneChanges /= lanes.size();

    /**
     * Given a trip, iterate over its GPS coordinates and its Patterns and tie the patterns to
     * individual coordinates for easier parsing later.
     * <p/>
     * The method iterates over Pattern objects and maintains an index into the ordered GPSReadings
     * list. For each pattern, it moves the GPS index forward until the pattern time is no longer earlier than the
     * GPS time. The appropriate index ivar in the Pattern is set, and the next pattern is considered.
     * <p/>
     * Returns a list of GPSReadings ids that should NOT be deleted-- the coordinates that have patterns
     * attached to them.
     *
     * @param trip
     */
//    public static List<MappableEvent> associatePatternsWithGps(Trip trip, List<DrivingPattern> patterns) {
//        int gpsIndex = 0;
//        List<MappableEvent> gps = trip.getGPSReadings();
//        List<MappableEvent> undelete = new ArrayList<MappableEvent>();
//
//        for (DrivingPattern pattern : patterns) {
//            MappableEvent reading = null;
//
//            gpsIndex = findNextClosestIndex(gps, gpsIndex, new Date(pattern.start));
//            reading = gps.get(gpsIndex);
//            pattern.gps_index_start = reading.id;
//            undelete.add(reading);
//
//            gpsIndex = findNextClosestIndex(gps, gpsIndex, new Date(pattern.end));
//            reading = gps.get(gpsIndex);
//            pattern.gps_index_end = reading.id;
//            undelete.add(reading);
//        }
//
//        return undelete;
//    }

/**
 * Helper method for the associatePatterns method.
 * <p/>
 * Takes the array of GPS coordinates, the current index inside the list, and the date you're trying
 * to match. Rewinds the index until the target date < current indexed GPS reading, then moves forward
 * until the target reading passes the GPS reading's timestamp. Returns the index.
 *
 * @param gps
 * @param gpsIndex
 * @param targetTime
 */
//    private static int findNextClosestIndex(List<MappableEvent> gps, int gpsIndex, Date targetTime) {
//        //rewind the gpsIndex to account for pattern overlap
//        MappableEvent tempHolder = gps.get(gpsIndex);
//
//        while (targetTime.compareTo(gps.get(gpsIndex).timestamp) == -1 && gpsIndex > 0)
//            gpsIndex--;
//
//        while (targetTime.compareTo(gps.get(gpsIndex).timestamp) == 1 && gpsIndex < gps.size() - 1)
//            gpsIndex++;
//
//        return gpsIndex;
//    }

/**
 * Returns a list of GPSReadings to delete. Accepts a trip (with coordinates attached) and
 * a list of the GPS coordinates already attached to patterns (that should not/ cannot be deleted)
 *
 * @param trip
 * @return
 */
//    public static List<MappableEvent> getGPSToDelete(Trip trip, List<MappableEvent> patternReadings) {
//        List<MappableEvent> deleteReadings = new ArrayList<MappableEvent>();
//        int lastCoordinate = 0;
//
//        //iterate over trip readings, pull aside 9/10 readings to delete
//        for (MappableEvent reading : trip.getGPSReadings()) {
//            //if tenth coordinate OR coordinate is a pattern link, save it and reset counter
//            if (lastCoordinate > 9 || patternReadings.contains(reading)) {
//                lastCoordinate = 0;
//            } else {
//                deleteReadings.add(reading);
//                lastCoordinate++;
//            }
//        }
//
//        return deleteReadings;
//    }

 */
