package edu.wisc.drivesense.scoring;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.scoring.common.GpsThief;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GpsTheifTest extends TestCase {
    ArrayList<MappableEvent> events;
    TimestampQueue<Reading> gps;
    TimestampQueue<DrivingPattern> patterns;

    double latMin = 43.073214;
    double longMin = -89.400558;
    double coordinateIncrement = .0001;
    long start = 0;
    long step = 100;


    public void setUp()  {
        reset();

        //patterns
//        for (int i = 0; i < n; i++) {
//            DrivingPattern pattern = new DrivingPattern();
//            pattern.start = start + step * i;
//            patterns.push(pattern);
//        }udo
    }

    public void tearDown(  ) {
        events = null;
        gps = null;
        patterns = null;
    }


    public void testCombinesPatternsWithGps() {
        for (int i = 0; i < 3; i++) {
            double values[] = {3, latMin + coordinateIncrement * i, longMin + coordinateIncrement * i};
            Reading reading = new Reading(values, start + step * i, Reading.Type.GPS);
            gps.push(reading);
        }

        DrivingPattern pattern = new DrivingPattern();
        pattern.start = 90;
        pattern.end = 110;
        patterns.push(pattern);

        ArrayList<MappableEvent> mappedPatterns = GpsThief.attachPatternsToCoordinates(gps, patterns);
        mappedPatterns = GpsThief.mergeGpsPatterns(mappedPatterns, gps);

        assertEquals(4, mappedPatterns.size());

        reset();
    }

    public void testDistance() {
        double result = GpsThief.distance(32.9697, -96.80322, 29.46786, -98.53506);
        assertEquals(653679.7609555026, result);
    }

    public void testPointLineDistance() {
        double feetResult = 0.3112638023114618;

        MappableEvent a = new MappableEvent();
        MappableEvent b = new MappableEvent();
        MappableEvent c = new MappableEvent();

        a.latitude = 43.073214;
        b.latitude = 43.073214 + coordinateIncrement;
        c.latitude = 43.073214 + coordinateIncrement / 2;

        a.longitude = -89.400558;
        b.longitude = -89.400558;
        c.longitude = -89.400558 + coordinateIncrement / 2;

        double result = GpsThief.PointLineDistance(a, b, c);
        assertEquals(feetResult, result);
    }

    void reset() {
        events = new ArrayList<MappableEvent>();
        gps = new TimestampQueue<Reading>();
        patterns = new TimestampQueue<DrivingPattern>();
    }
}
