package edu.wisc.drivesense.utils;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;


public class TimestampQueueTest extends TestCase {
    private TimestampQueue queue;
    private ArrayList<Reading> readings;

    public void setUp()  {
        queue = new TimestampQueue();

        double values[] = {0, 0, 0};
        long startTime = 1234567;
        long distance = 1000;

        for (int i = 0; i < 50; i++) {
            Reading reading = new Reading(values, startTime + distance * i, Reading.Type.ACCELERATION);
            queue.push(reading);
            readings.add(reading);
        }
    }

    public void tearDown(  ) {
        queue = null;
        readings = null;
    }


    @Test
    public void testUniforSearchWorks() {
        Reading reading = readings.get(20);
//        Reading result = queue.
    }
}
