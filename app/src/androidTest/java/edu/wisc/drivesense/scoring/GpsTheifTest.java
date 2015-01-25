package edu.wisc.drivesense.scoring;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.common.GpsThief;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;


public class GpsTheifTest extends TestCase {
    ArrayList<MappableEvent> events;
    TimestampQueue<Reading> gps;
    TimestampQueue<DrivingPattern> patterns;

    MappableEvent a;
    MappableEvent b;
    MappableEvent c;

    double latMin = 43.073214;
    double longMin = -89.400558;
    double coordinateIncrement = .001;
    long start = 0;
    long step = 100;


    public void setUp()  {
        reset();

        a = new MappableEvent();
        b = new MappableEvent();
        c = new MappableEvent();

        a.type = MappableEvent.Type.gps;
        b.type = MappableEvent.Type.gps;
        c.type = MappableEvent.Type.gps;

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

        a = null;
        b = null;
        c = null;
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
        assertEquals(1384030.45357375, result);
    }

    public void testPointLineDistance() {
        double feetResult = 364.829396001742;

        a.latitude = 43.073214;
        b.latitude = 43.073214 + coordinateIncrement;
        c.latitude = 43.073214 + coordinateIncrement / 2;

        a.longitude = -89.400558;
        b.longitude = -89.400558;
        c.longitude = -89.400558 - coordinateIncrement;

        double result = GpsThief.pointLineDistance(c, a, b);
        assertEquals(feetResult, result);
    }

    public void testRDPLeavesThree() {
        a.latitude = 43.073214;
        b.latitude = 43.073214;
        c.latitude = 43.073214 + coordinateIncrement;

        a.longitude = -89.400558;
        b.longitude = -89.400558 - coordinateIncrement;
        c.longitude = -89.400558 - coordinateIncrement;

        ArrayList<MappableEvent> points = new ArrayList<MappableEvent>();
        points.add(a);
        points.add(b);
        points.add(c);

        List<MappableEvent> result = GpsThief.ramerDouglasPeuckerFunction(points);
        assertEquals(3, result.size());
    }

    public void testRDPLeavesTwo() {
        //given three colinear coordinates, RDP should remove the middle one
        a.latitude = 43.073214;
        b.latitude = 43.073214;
        c.latitude = 43.073214;

        a.longitude = -89.400558;
        b.longitude = -89.400558 - coordinateIncrement;
        c.longitude = -89.400558 - coordinateIncrement * 2;

        ArrayList<MappableEvent> points = new ArrayList<MappableEvent>();
        points.add(a);
        points.add(b);
        points.add(c);

        List<MappableEvent> result = GpsThief.ramerDouglasPeuckerFunction(points);
        assertEquals(2, result.size());
    }

    public void testRDPLeavesThreeWithPattern() {
        //given three colinear coordinates, RDP would normally remove the middle one, but in this test case
        //its a pattern and therefor must be retained
        a.latitude = 43.073214;
        b.latitude = 43.073214;
        c.latitude = 43.073214;

        a.longitude = -89.400558;
        b.longitude = -89.400558 - coordinateIncrement;
        c.longitude = -89.400558 - coordinateIncrement * 2;

        b.type = MappableEvent.Type.acceleration;

        ArrayList<MappableEvent> points = new ArrayList<MappableEvent>();
        points.add(a);
        points.add(b);
        points.add(c);

        List<MappableEvent> result = GpsThief.ramerDouglasPeuckerFunction(points);
        assertEquals(3, result.size());

        b.type = MappableEvent.Type.gps;
    }

    void reset() {
        events = new ArrayList<MappableEvent>();
        gps = new TimestampQueue<Reading>();
        patterns = new TimestampQueue<DrivingPattern>();
    }
}
